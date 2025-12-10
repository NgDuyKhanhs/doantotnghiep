package lms.doantotnghiep.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lms.doantotnghiep.domain.Enrollment;
import lms.doantotnghiep.domain.User;
import lms.doantotnghiep.dto.CourseDTO;
import lms.doantotnghiep.dto.EnrollmentDTO;
import lms.doantotnghiep.dto.PdfDTO;
import lms.doantotnghiep.dto.response.PdfResponse;
import lms.doantotnghiep.dto.response.ScanResult;
import lms.doantotnghiep.enums.AppException;
import lms.doantotnghiep.enums.ErrorConstant;
import lms.doantotnghiep.repository.CourseRepository;
import lms.doantotnghiep.repository.EnrollmentRepository;
import lms.doantotnghiep.repository.UserRepository;
import lms.doantotnghiep.security.PDFSecurityScanner;
import lms.doantotnghiep.security.PlainPdfScanner;
import lms.doantotnghiep.service.EnrollmentService;
import lms.doantotnghiep.service.PdfWatermarkService;
import lms.doantotnghiep.service.upload.ImageService;
import lms.doantotnghiep.service.upload.PdfService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    @PersistenceContext(unitName = "entityManagerFactory")
    private EntityManager entityManager;

    @Autowired
    private PdfService pdfService;
    @Autowired
    private PdfWatermarkService pdfWatermarkService;
    @Autowired
    private PDFSecurityScanner pdfSecurityScanner;
    @Autowired
    private PlainPdfScanner plainPdfScanner;
    @Override
    public List<EnrollmentDTO> getAllEnrollments(Integer classId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImple userDetails = (UserDetailsImple) authentication.getPrincipal();
        List<EnrollmentDTO> enrollmentDTOList = new ArrayList<>();
        enrollmentDTOList = enrollmentRepository.getAllEnrollments(classId, userDetails.getId());
        enrollmentDTOList.forEach(enrollmentDTO -> {
            enrollmentDTO.setCourse(courseRepository.findCourseByID(enrollmentDTO.getCourseId()));
        });
        return enrollmentDTOList;
    }

    @Override
    public void registerEnrollment(List<Integer> enrollIds) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImple userDetails = (UserDetailsImple) authentication.getPrincipal();

        Optional<User> userOptional = userRepository.findByEmail(userDetails.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            for (Integer id : enrollIds) {
                Optional<Enrollment> enrollment = enrollmentRepository.findById(id);
                if (enrollment.isPresent()) {
                    Enrollment enrollment1 = enrollment.get();
                    if (!enrollment1.isLocked()) {
                        enrollment1.setRegistered(enrollment1.getRegistered() + 1);
                        if (Objects.equals(enrollment1.getRegistered(), enrollment1.getAvailable()) && enrollment1.isLockWhenFull()) {
                            enrollment1.setLocked(true);
                        }
                        enrollmentRepository.save(enrollment1);
                        user.getEnrollments().add(enrollment1);
                    } else {
                        throw new AppException(ErrorConstant.REGISTER_INVALID);
                    }
                }
            }
            userRepository.save(user);
        } else {
            throw new AppException(ErrorConstant.UNAUTHENTICATED);
        }

    }

    @Override
    public List<EnrollmentDTO> getEnrollmentFilter(String type, Integer userId, String currentRole) {
        List<EnrollmentDTO> enrollmentDTOS;
        Map<String, Object> params = new HashMap<>();
        String sql = "";
        if (type.equals("Chưa đăng ký")) {
            sql = "SELECT e.enrollid   AS enrollId, " +
                    "       e.available, " +
                    "       e.registered, " +
                    "       e.start_time AS startTime, " +
                    "       e.end_time   AS endTime, " +
                    "       e.course_id  AS courseId, " +
                    "       e.locked, " +
                    "       c.user_id    AS userId, " +
                    "       c.banner as banner " +
                    "FROM enrollmenttbl e " +
                    "         LEFT JOIN coursetbl c ON e.course_id = c.course_id " +
                    "         LEFT JOIN classtbl cl ON cl.class_id = c.class_id " +
                    "         LEFT JOIN enrollmentusertbl eu " +
                    "               ON eu.enrollid = e.enrollid AND eu.userid = :id " +
                    "WHERE eu.userid IS NULL";

        } else if (type.equals("Đã đăng ký") && currentRole.equals("ROLE_USER")) {
            sql = "SELECT e.enrollid   AS enrollId, " +
                    "       e.available, " +
                    "       e.registered, " +
                    "       e.start_time AS startTime, " +
                    "       e.end_time   AS endTime, " +
                    "       e.course_id  AS courseId, " +
                    "       e.locked, " +
                    "       c.user_id    AS userId, " +
                    "       c.banner as banner " +
                    "FROM enrollmenttbl e " +
                    "         LEFT JOIN coursetbl c ON e.course_id = c.course_id " +
                    "         LEFT JOIN classtbl cl ON cl.class_id = c.class_id " +
                    "         INNER JOIN enrollmentusertbl eu ON eu.enrollid = e.enrollid " +
                    "WHERE eu.userid = :id";
        } else if (type.equals("Đã đăng ký") && currentRole.equals("ROLE_TEACHER")) {
            sql = "SELECT e.enrollid   AS enrollId, " +
                    "                           e.available, " +
                    "                           e.registered, " +
                    "                           e.start_time AS startTime, " +
                    "                           e.end_time   AS endTime, " +
                    "                           e.course_id  AS courseId, " +
                    "                           e.locked, " +
                    "                           c.user_id    AS userId, " +
                    "                           c.banner as banner " +
                    "                    FROM enrollmenttbl e " +
                    "                             LEFT JOIN coursetbl c ON e.course_id = c.course_id " +
                    "                    WHERE c.user_id = :id";
        }
        params.put("id", userId);
        Query query = entityManager.createNativeQuery(sql, "EnrollmentDTO");
        setParams(query, params);
        enrollmentDTOS = query.getResultList();
        enrollmentDTOS.forEach(enrollmentDTO -> {
            enrollmentDTO.setCourse(courseRepository.findCourseByID(enrollmentDTO.getCourseId()));
        });
        return enrollmentDTOS;
    }

    public void addPDFInEnrollment(EnrollmentDTO enrollmentDTO, Integer userId) throws IOException {
        Optional<Enrollment> enrollment = enrollmentRepository.findById(enrollmentDTO.getEnrollId());
        if (enrollment.isEmpty()) return;

        Enrollment en = enrollment.get();

        if (enrollmentDTO.getPdfFiles() != null && !enrollmentDTO.getPdfFiles().isEmpty()) {
            List<PdfDTO> pdfDtos = enrollmentDTO.getPdfFiles();
            List<PdfDTO> savedPdfs = new ArrayList<>();

            for (PdfDTO pdfDto : pdfDtos) {
                String base64File = pdfDto.getPdfFile();
                if (base64File == null || base64File.trim().isEmpty()) {
                    throw new AppException(ErrorConstant.FILE_ERROR);
                }
                if (base64File.contains(",")) {
                    base64File = base64File.split(",")[1];
                }
                base64File = base64File.replaceAll("\\s+", "");

                byte[] pdfBytes;
                try {
                    pdfBytes = Base64.getDecoder().decode(base64File);
                } catch (IllegalArgumentException e) {
                    throw new AppException(ErrorConstant.FILE_ERROR);
                }
                if (!plainPdfScanner.hasPdfHeader(pdfBytes)) {
                    throw new AppException(ErrorConstant.FILE_ERROR);
                }

                ScanResult scan = plainPdfScanner.scan(pdfBytes);
                if (scan.isDangerous()) {
                    throw new AppException(ErrorConstant.FILE_ERROR); // hoặc tạo ErrorConstant.PDF_DANGEROUS
                }

                ByteArrayInputStream pdfInput = new ByteArrayInputStream(pdfBytes);
                File watermarkedFile = pdfWatermarkService.addWatermark(pdfInput, userId);

                byte[] watermarkedBytes = Files.readAllBytes(watermarkedFile.toPath());
                ScanResult postScan = plainPdfScanner.scan(watermarkedBytes);
                if (postScan.isDangerous()) {
                    watermarkedFile.delete();
                    throw new AppException(ErrorConstant.FILE_ERROR);
                }

                String publicId = pdfService.uploadPdfBytes(watermarkedBytes, pdfDto.getNameFile());

                watermarkedFile.delete();
                PdfDTO savedPdf = new PdfDTO();
                savedPdf.setPdfFile(publicId);
                savedPdf.setNameFile(pdfDto.getNameFile());
                savedPdf.setCreatedAt(
                        pdfDto.getCreatedAt() != null
                                ? pdfDto.getCreatedAt()
                                : Timestamp.valueOf(LocalDateTime.now())
                );

                savedPdfs.add(savedPdf);
            }

            en.getPdfFiles().addAll(savedPdfs);
            enrollmentRepository.save(en);
        }
    }




    @Override
    public List<PdfResponse> getPDFFilesByEnrollmentId(Integer enrollmentId) {
        return enrollmentRepository.getPDFFilesByEnrollmentId(enrollmentId);
    }

    @Override
    public boolean hasEnrolledCourse(Integer userId, String pdfId) {
        return enrollmentRepository.existsByUserIdAndPdfId(userId, pdfId);
    }

    public static void setParams(Query query, Map<String, Object> params) {
        if (params != null && !params.isEmpty()) {
            Set<Map.Entry<String, Object>> set = params.entrySet();
            for (Map.Entry<String, Object> obj : set) {
                if (obj.getValue() == null) query.setParameter(obj.getKey(), "");
                else query.setParameter(obj.getKey(), obj.getValue());
            }
        }
    }
}