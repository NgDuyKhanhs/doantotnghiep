package lms.doantotnghiep.repository;

import lms.doantotnghiep.domain.Enrollment;
import lms.doantotnghiep.dto.EnrollmentDTO;
import lms.doantotnghiep.dto.PdfDTO;
import lms.doantotnghiep.dto.response.PdfResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface EnrollmentRepository extends CrudRepository<Enrollment, Integer> {
    @Query(value = "select e.enrollid as enrollId, " +
            "                   e.available, " +
            "                   e.registered, " +
            "                   e.start_time as startTime, " +
            "                   e.end_time as endTime, " +
            "                   e.course_id as courseId, " +
            "                   e.locked, " +
            "                   c.user_id as userId, " +
            "                   cl.name as className" +
            "            from enrollmenttbl e " +
            "                     join dbo.coursetbl c on e.course_id = c.course_id " +
            "                     left join classtbl cl on cl.class_id = c.class_id", nativeQuery = true)
    List<EnrollmentDTO> getAllEnrollments(Integer classId, Integer userId);


    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Enrollment e " +
            "WHERE e.course.id = :courseId " +
            "AND :start <= e.endTime " +
            "AND :end >= e.startTime")
    boolean existsByCourseIdAndTimeOverlap(
            @Param("courseId") Integer courseId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = "select pf.enrollid as idEnroll, pf.pdf_url as pdfFile, pf.name_file as nameFile, pf.created_at as createdAt from enrollment_pdf_files pf join enrollmenttbl e on pf.enrollid = e.enrollid where e.enrollid = ?1", nativeQuery = true)
    List<PdfResponse> getPDFFilesByEnrollmentId(Integer enrollmentId);

    @Query(value = "select pf.pdf_url as pdfFile, pf.name_file as nameFile, pf.created_at as createdAt " +
            "from enrollment_pdf_files pf " +
            "         join enrollmenttbl e on pf.enrollid = e.enrollid " +
            "where e.enrollid = ?1 and pf.pdf_url = ?2", nativeQuery = true)
    PdfDTO findPDFByIDAndURL(Integer enrollmentId, String url);

    @Query(value = "SELECT CASE WHEN EXISTS ( " +
            "    SELECT 1 " +
            "    FROM enrollmenttbl e " +
            "   LEFT JOIN enrollment_pdf_files p ON e.enrollid = p.enrollid " +
            "   LEFT JOIN enrollmentusertbl eu  ON e.enrollid = eu.enrollid " +
            "   LEFT JOIN coursetbl c ON c.course_id = e.course_id " +
            "    WHERE ( " +
            "        (eu.userid = ?1 AND p.pdf_url = ?2) " +
            "        OR (c.user_id = ?1 AND p.pdf_url = ?2) " +
            "    ) " +
            ") " +
            "THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END;", nativeQuery = true)
    boolean existsByUserIdAndPdfId(Integer userId, String url);
}
