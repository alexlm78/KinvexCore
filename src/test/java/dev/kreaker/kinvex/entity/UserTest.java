package dev.kreaker.kinvex.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createUser_WithValidData_ShouldPass() {
        // Given
        User user =
                new User("testuser", "test@example.com", "hashedpassword", User.UserRole.OPERATOR);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashedpassword");
        assertThat(user.getRole()).isEqualTo(User.UserRole.OPERATOR);
        assertThat(user.getActive()).isTrue();
    }

    @Test
    void createUser_WithBlankUsername_ShouldFailValidation() {
        // Given
        User user = new User("", "test@example.com", "hashedpassword", User.UserRole.OPERATOR);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
    }

    @Test
    void createUser_WithBlankEmail_ShouldFailValidation() {
        // Given
        User user = new User("testuser", "", "hashedpassword", User.UserRole.OPERATOR);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(1); // At least @NotBlank will fail
    }

    @Test
    void createUser_WithInvalidEmail_ShouldFailValidation() {
        // Given
        User user = new User("testuser", "invalid-email", "hashedpassword", User.UserRole.OPERATOR);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must be a well-formed email address");
    }

    @Test
    void createUser_WithBlankPassword_ShouldFailValidation() {
        // Given
        User user = new User("testuser", "test@example.com", "", User.UserRole.OPERATOR);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
    }

    @Test
    void createUser_WithTooLongUsername_ShouldFailValidation() {
        // Given
        String longUsername = "a".repeat(51); // Max is 50
        User user =
                new User(
                        longUsername, "test@example.com", "hashedpassword", User.UserRole.OPERATOR);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("size must be between 0 and 50");
    }

    @Test
    void createUser_WithTooLongEmail_ShouldFailValidation() {
        // Given
        String longEmail = "a".repeat(101); // Exactly 101 chars, exceeds max of 100
        User user = new User("testuser", longEmail, "hashedpassword", User.UserRole.OPERATOR);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        boolean hasSizeViolation =
                violations.stream()
                        .anyMatch(v -> v.getMessage().contains("size must be between 0 and 100"));
        assertThat(hasSizeViolation).isTrue();
    }

    @Test
    void userRole_AllRoles_ShouldBeValid() {
        // Test all user roles
        User adminUser = new User("admin", "admin@example.com", "hash", User.UserRole.ADMIN);
        User managerUser =
                new User("manager", "manager@example.com", "hash", User.UserRole.MANAGER);
        User operatorUser =
                new User("operator", "operator@example.com", "hash", User.UserRole.OPERATOR);
        User viewerUser = new User("viewer", "viewer@example.com", "hash", User.UserRole.VIEWER);

        assertThat(adminUser.getRole()).isEqualTo(User.UserRole.ADMIN);
        assertThat(managerUser.getRole()).isEqualTo(User.UserRole.MANAGER);
        assertThat(operatorUser.getRole()).isEqualTo(User.UserRole.OPERATOR);
        assertThat(viewerUser.getRole()).isEqualTo(User.UserRole.VIEWER);
    }

    @Test
    void defaultConstructor_ShouldSetDefaultValues() {
        // Given & When
        User user = new User();

        // Then
        assertThat(user.getActive()).isTrue();
    }
}
