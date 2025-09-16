package io.knowledgebase.demo.dto.user;

import io.knowledgebase.demo.enums.Role;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class UserDtoTestBase {

    protected static final String VALID_FULLNAME = "Firstname Lastname";
    protected static final String VALID_USERNAME = "username";
    protected static final String VALID_EMAIL = "firstname.lastname@gmail.com";
    protected static final String VALID_PASSWORD = "Password123@";
    protected static final Role VALID_ROLE = Role.USER;
    protected static final Boolean VALID_ACTIVE = true;

    protected static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    protected static Stream<Arguments> provideInvalidFullnames() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("    "),
                Arguments.of("A"),
                Arguments.of("Name123"),
                Arguments.of(" Firstname Lastname"),
                Arguments.of("Fullname Lastname "),
                Arguments.of("Имя Фамилия"),
                Arguments.of("12345678"),
                Arguments.of("@#$%@#$"),
                Arguments.of("First''name"),
                Arguments.of("Last-'name"),
                Arguments.of("Firstname Фамилия"),
                Arguments.of("!Fullname"),
                Arguments.of(".-'"),
                Arguments.of("Full--name"),
                Arguments.of("Firstname  Lastname"),
                Arguments.of("A".repeat(129))
        );
    }

    protected static Stream<Arguments> provideValidFullnames() {
        return Stream.of(
                Arguments.of("John Doe"),
                Arguments.of("Anna-Marie Smith"),
                Arguments.of("O'Connor James"),
                Arguments.of("Mary Jane Watson"),
                Arguments.of("J.R.R.Tolkien"),
                Arguments.of("Alex"),
                Arguments.of("Christopher Jonathan Richardson"),
                Arguments.of("A".repeat(128))
        );
    }

    protected static Stream<Arguments> provideInvalidUsernames() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("    "),
                Arguments.of("U"),
                Arguments.of(" Username"),
                Arguments.of("user name"),
                Arguments.of("_username_"),
                Arguments.of("username "),
                Arguments.of("юзернейм"),
                Arguments.of("12345678"),
                Arguments.of("@#$%@#$"),
                Arguments.of("User''name"),
                Arguments.of("User-'name"),
                Arguments.of("userнейм"),
                Arguments.of("!Username"),
                Arguments.of(".-'"),
                Arguments.of("User--name"),
                Arguments.of("U".repeat(33))
        );
    }

    protected static Stream<Arguments> provideValidUsernames() {
        return Stream.of(
                Arguments.of("user123"),
                Arguments.of("john_doe"),
                Arguments.of("anna.smith"),
                Arguments.of("Mark-OConnor"),
                Arguments.of("alex99"),
                Arguments.of("user-name_123"),
                Arguments.of("john'doe"),
                Arguments.of("U".repeat(32))
        );
    }

    protected static Stream<Arguments> provideInvalidEmails() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of(" "),
                Arguments.of("a@b"),
                Arguments.of("plainaddress"),
                Arguments.of("@no-local-part.com"),
                Arguments.of("no-domain-part@"),
                Arguments.of("user@.com"),
                Arguments.of("user@com."),
                Arguments.of("user@domain..com"),
                Arguments.of("E".repeat(64) + "@gmail.com")
        );
    }

    protected static Stream<Arguments> provideValidEmails() {
        return Stream.of(
                Arguments.of("test@example.com"),
                Arguments.of("john.doe@gmail.com"),
                Arguments.of("anna_smith123@yahoo.co.uk"),
                Arguments.of("user.name+tag@sub.domain.com"),
                Arguments.of("a".repeat(58) + "@g.com")
        );
    }

    protected static Stream<Arguments> provideInvalidPasswords() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of(" "),
                Arguments.of("Password!"),
                Arguments.of("password1!"),
                Arguments.of("PASSWORD1!"),
                Arguments.of("Password1"),
                Arguments.of("Pass word1!"),
                Arguments.of("Short1!"),
                Arguments.of("P".repeat(33))
        );
    }

    protected static Stream<Arguments> provideValidPasswords() {
        return Stream.of(
                Arguments.of("Password1!"),
                Arguments.of("StrongPass99@"),
                Arguments.of("A1b2C3d4$"),
                Arguments.of("My_Secure-Password1@"),
                Arguments.of("XyZ12345#"),
                Arguments.of("P".repeat(24) + "1aA!")
        );
    }

    protected static Stream<Arguments> provideInvalidRoles() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of(" "),
                Arguments.of("manager"),
                Arguments.of("superadmin"),
                Arguments.of("root"),
                Arguments.of("123"),
                Arguments.of("user admin"),
                Arguments.of("_admin"),
                Arguments.of("admin_"),
                Arguments.of("admin".repeat(4))
        );
    }

    protected static Stream<Arguments> provideValidRoles() {
        return Stream.of(
                Arguments.of("USER"),
                Arguments.of("EXPERT"),
                Arguments.of("ADMIN"),
                Arguments.of("user"),
                Arguments.of("expert"),
                Arguments.of("admin"),
                Arguments.of("User"),
                Arguments.of("Expert"),
                Arguments.of("Admin"),
                Arguments.of("uSeR"),
                Arguments.of("eXpErT"),
                Arguments.of("aDmIn")
        );
    }

    protected static Stream<Arguments> provideInvalidActives() {
        return Stream.of(
                Arguments.of(1),
                Arguments.of("1"),
                Arguments.of(0),
                Arguments.of("0"),
                Arguments.of(2),
                Arguments.of("2"),
                Arguments.of("true"),
                Arguments.of("false"),
                Arguments.of("abc")
        );
    }

    protected static Stream<Arguments> provideValidActives() {
        return Stream.of(
                Arguments.of(true),
                Arguments.of(false)
        );
    }

}
