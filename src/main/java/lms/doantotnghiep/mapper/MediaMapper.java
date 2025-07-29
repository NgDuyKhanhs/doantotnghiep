package lms.doantotnghiep.mapper;


import lms.doantotnghiep.domain.Media;
import lms.doantotnghiep.dto.response.MediaResponse;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface MediaMapper {

    MediaResponse map(Media media);

}
