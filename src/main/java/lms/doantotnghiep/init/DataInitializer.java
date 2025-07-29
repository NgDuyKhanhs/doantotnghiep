package lms.doantotnghiep.init;

import lms.doantotnghiep.domain.Course;
import lms.doantotnghiep.domain.Role;
import lms.doantotnghiep.domain.User;
import lms.doantotnghiep.enums.RoleType;
import lms.doantotnghiep.enums.UserStatus;
import lms.doantotnghiep.repository.ClassRepository;
import lms.doantotnghiep.repository.CourseRepository;
import lms.doantotnghiep.repository.RoleRepository;
import lms.doantotnghiep.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    private final ClassRepository classRepository;
    private final RoleRepository roleRepository;
    private final CourseRepository courseRepository;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(ClassRepository classRepository, RoleRepository roleRepository, CourseRepository courseRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.classRepository = classRepository;
        this.roleRepository = roleRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (classRepository.count() == 0) {
            classRepository.saveAll(List.of(
                    new lms.doantotnghiep.domain.Class("CT", "Công nghệ thông tin"),
                    new lms.doantotnghiep.domain.Class("AT", "An toàn thông tin"),
                    new lms.doantotnghiep.domain.Class("DT", "Điện tử viễn thông")
            ));
        }

        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                    new Role(RoleType.ROLE_USER),
                    new Role(RoleType.ROLE_ADMIN),
                    new Role(RoleType.ROLE_TEACHER)
            ));
        }

        if (courseRepository.count() == 0) {
            courseRepository.saveAll(List.of(
                    new Course("Giải tích 1", 3),
                    new Course("Giải tích 2", 3),
                    new Course("Lập trình căn bản", 3),
                    new Course("Thể chất 1", 1),
                    new Course("Kinh tế chính trị", 2),

                    new Course("Toán chuyên đề", 3),
                    new Course("Xác xuất thống kê", 3),
                    new Course("Java", 3),
                    new Course("Thể chất 2", 1),
                    new Course("Chính trị mác Lênin", 2),

                    new Course("Đặc tả hình thức", 3),
                    new Course("Mật mã ứng dụng", 2),
                    new Course("Phân tích thiết kế hệ thống", 3),
                    new Course("Chuyên đề công nghệ phần mềm", 3),
                    new Course("Cấu trúc dữ liệu và giải thuật", 3),

                    new Course("Thực tập tốt nghiệp", 5),
                    new Course("Mật mã chuyên sâu", 4),
                    new Course("Đồ án", 10)
            ));
        }
        createUserIfNotExists("admin@gmail.com", "enzo","Quản trị viên", RoleType.ROLE_ADMIN);
        createUserIfNotExists("gv1@gmail.com", "enzo", "Giảng viên 1",RoleType.ROLE_TEACHER);
        createUserIfNotExists("gv2@gmail.com", "enzo","Giảng viên 2", RoleType.ROLE_TEACHER);
    }

    private void createUserIfNotExists(String email, String password,String fullName, RoleType roleName) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            User newUser = User.builder()
                    .email(email)
                    .active(true)
                    .password(passwordEncoder.encode(password))
                    .fullname(fullName)
                    .build();

            Optional<Role> role = roleRepository.findByRoleName(roleName);
            Set<Role> roles = new HashSet<>();
            if (role.isPresent()) {
                roles.add(role.get());
                newUser.setRoles(roles);
                userRepository.save(newUser);
            } else {
                throw new RuntimeException("Role '" + roleName + "' not found");
            }
        }
    }
}
