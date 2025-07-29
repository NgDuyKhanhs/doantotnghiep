package lms.doantotnghiep.repository;

import lms.doantotnghiep.domain.ViolationReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViolationRepository extends JpaRepository<ViolationReport, Integer> {

}
