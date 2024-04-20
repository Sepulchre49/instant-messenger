package server;

class ForwardingException extends Exception {
    public ForwardingException() {
        super("The server could not forward the message.");
    }

    public ForwardingException(Throwable cause) {
        super("The server could not forward the message.", cause);
    }
}

class UsernameValidationException extends Exception {
    public UsernameValidationException(String username) {
        super("The username " + username + " is invalid.");
    }
}

class PasswordValidationException extends Exception {
    public PasswordValidationException(String password) {
        super("The password " + password + " is invalid");
    }
}

class LogoutException extends Exception {
    public LogoutException(Throwable cause) {
        super("Could not log the user out of the server.");
    }
}

class AuthenticationException extends Exception {
    public AuthenticationException(String password) {
        super("The password " + password + " is invalid");
    }
}


