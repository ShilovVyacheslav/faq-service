package io.knowledgebase.demo.common.validation;

public final class ValidationPatterns {

    // Must start and end with a Latin letter;
    // May contain letters, spaces, dots, apostrophes, and hyphens;
    // Two special characters in a row are not allowed (e.g., "..", "-'", " .").
    public static final String FULLNAME_REGEXP =
            "^(?!.*([ .'-])([ .'-]))[A-Za-z][A-Za-z .'-]*[A-Za-z]$";

    // Must contain at least one Latin letter;
    // May include Latin letters, digits, dots, underscores, apostrophes, and hyphens;
    // Cannot start or end with a special character;
    // Two identical special characters in a row are not allowed (e.g., "..", "_-", "'.").
    public static final String USERNAME_REGEXP =
            "^(?=.*[a-zA-Z])(?!.*([._'-])([._'-]))[a-zA-Z0-9][a-zA-Z0-9._'-]*[a-zA-Z0-9]$";

    // Must contain at least one digit (0–9);
    // Must contain at least one lowercase Latin letter (a–z);
    // Must contain at least one uppercase Latin letter (A–Z);
    // Must contain at least one special character (e.g., !@#$%^&*);
    // Cannot contain spaces.
    public static final String PASSWORD_REGEXP =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`])\\S*$";

}
