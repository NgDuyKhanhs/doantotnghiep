package lms.doantotnghiep.service.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lms.doantotnghiep.enums.AppException;
import lms.doantotnghiep.enums.ErrorConstant;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageService {

    final Cloudinary cloudinary;

    public String uploadImage(MultipartFile multipartFile, String publicId) {
        String url = "";
        try {
            Map<String, Object> params = ObjectUtils.asMap("public_id", publicId);
            Map<String, Object> uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(), params);
            url = uploadResult.get("url").toString();
        } catch (Exception e) {
            throw new AppException(ErrorConstant.FAIL_UPLOAD_CLOUD);
        }
        return url;
    }


    public String uploadVideo(MultipartFile file, String publicId) {
        try {
            // Thay đổi tham số upload để có thể gán publicId
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", "video", "public_id", publicId));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to upload video");
        }
    }


    //    https://res.cloudinary.com/dhrcu7xli/image/upload/v1723193648/Image_084dec90-7afc-41cf-99d4-99f60c1c12d4.jpg
    public String getUrlImage(String base64Image){
        String uuid = UUID.randomUUID().toString();
        MultipartFile multipartFile = Base64ToMultipartFileConverter.convert(base64Image);
        String urlCould =  uploadImage(multipartFile, "Image_" + uuid);
        return urlCould;
    }

//    https://res.cloudinary.com/dhrcu7xli/video/upload/v1722854068/Video_6fb9a621-4ec8-440b-8e6f-84245bf4d131.mp4
public String getUrlVideo(String base64Video) {
        String uuid = UUID.randomUUID().toString();
        MultipartFile multipartFile = Base64ToMultipartFileConverter.convert(base64Video);
        String urlCould =  uploadVideo(multipartFile, "Video_" + uuid);
        return urlCould;
    }

    private String getPublicId(String url) {
        String[] parts = url.split("/");
        // Lấy phần trước dấu "." trong phần cuối cùng của URL
        String publicIDWithFormat = parts[parts.length - 1]; // Image_/Video_625737be-5b6c-49ef-be49-bfd1787cd552.jpg
        String[] publicIDParts = publicIDWithFormat.split("\\."); // {"Image_/Video_625737be-5b6c-49ef-be49-bfd1787cd552", "jpg"}

        return publicIDParts[0]; // Trả về "Image_/Video_625737be-5b6c-49ef-be49-bfd1787cd552"
    }

    public void deleteImage(String imageUrl) {
        try {
            String publicId = getPublicId(imageUrl);
            Map<String, Object> params = ObjectUtils.asMap("resource_type", "image");
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, params);
            System.out.println("Kết quả xóa ảnh: " + result);
        } catch (Exception e) {
            System.out.println("Lỗi khi xóa ảnh");
            e.printStackTrace();
        }
    }

    public void deleteVideo(String videoUrl) {
        try {
            String publicId = getUrlVideo(videoUrl);
            Map<String, Object> params = ObjectUtils.asMap("resource_type", "video");
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, params);
            System.out.println("Kết quả xóa video: " + result);
        } catch (Exception e) {
            System.out.println("Lỗi khi xóa video");
            e.printStackTrace();
        }
    }


}
