package lms.doantotnghiep.repository;


import lms.doantotnghiep.domain.User;
import lms.doantotnghiep.dto.UserDTO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    public <T> T getReferenceById(Integer id);
    @Query(value = "SELECT * FROM usertbl WHERE email =  ?1 and active = 1", nativeQuery = true)
    Optional<User> findByEmail(String studentCode);

    @Query(value = "SELECT * FROM usertbl WHERE email =  ?1", nativeQuery = true)
    List<User> findAllByEmail(String studentCode);

    @Query(value = "SELECT * FROM usertbl WHERE email =  ?1", nativeQuery = true)
    Optional<User> findByEmailSigned(String email);

    @Query(value = "select u.user_id as id, u.email, u.fullname, u.avatar from usertbl u join roleusertbl ru on u.user_id = ru.userid where ru.roleid = 3", nativeQuery = true)
    List<UserDTO> getListTeachers();

    @Query(value = "select u.user_id, u.email, u.fullname, u.avatar from usertbl u join roleusertbl ru on u.user_id = ru.userid where user_id = ?1", nativeQuery = true)
    UserDTO getUserByID(Integer id);

    @Query(value = "select u.user_id, u.email, u.avatar, u.created_at, u.fullname, r.role_name " +
            "from usertbl u " +
            "         join roleusertbl ru on u.user_id = ru.userid " +
            "         join roletbl r on r.role_id = ru.roleid " +
            "where user_id = ?1", nativeQuery = true)
    UserDTO getDetailUserByID(Integer id);
    @Query(value = "select u.user_id as id, u.email, u.fullname, u.avatar from usertbl u join dbo.enrollmentusertbl e on u.user_id = e.userid where e.enrollid = ?1", nativeQuery = true)
    List<UserDTO> getListUserFromEnrollment(Integer id);


}