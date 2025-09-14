package io.knowledgebase.demo.repository;

import io.knowledgebase.demo.entity.User;
import io.knowledgebase.demo.enums.Role;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsernameIgnoreCase(String username);

    Boolean existsByEmail(String email);

    interface UserSpec {

        static Specification<User> fullnameContains(String fullname) {
            return (root, query, criteriaBuilder) -> fullname == null ? null :
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("fullname")),
                            "%" + fullname.toLowerCase() + "%"
                    );
        }

        static Specification<User> usernameContains(String username) {
            return (root, query, criteriaBuilder) -> username == null ? null :
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("username")),
                            "%" + username.toLowerCase() + "%"
                    );
        }

        static Specification<User> emailContains(String email) {
            return (root, query, criteriaBuilder) -> email == null ? null :
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("email")),
                            "%" + email.toLowerCase() + "%"
                    );
        }

        static Specification<User> roleEquals(Role role) {
            return (root, query, criteriaBuilder) -> role == null ? null :
                    criteriaBuilder.equal(root.get("role"), role);
        }

        static Specification<User> isActive(Boolean active) {
            return (root, query, criteriaBuilder) -> active == null ? null :
                    criteriaBuilder.equal(root.get("active"), active);
        }

        static Specification<User> dateBetween(
                Function<Root<User>, Path<LocalDateTime>> dateField,
                LocalDate from, LocalDate to) {
            return (root, query, criteriaBuilder) -> {
                if (from == null || to == null) {
                    return null;
                }
                return criteriaBuilder.between(
                        dateField.apply(root),
                        from.atStartOfDay(),
                        to.plusDays(1).atStartOfDay()
                );
            };
        }

        static Specification<User> createdAtBetween(LocalDate from, LocalDate to) {
            return dateBetween(root -> root.get("createdAt"), from, to);
        }

        static Specification<User> updatedAtBetween(LocalDate from, LocalDate to) {
            return dateBetween(root -> root.get("updatedAt"), from, to);
        }

    }

}
