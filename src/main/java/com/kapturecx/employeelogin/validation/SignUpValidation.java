package com.kapturecx.employeelogin.validation;

public class SignUpValidation {
    public static boolean validateSignUp(String username, String password, int client_id) {
        return !username.isEmpty() && !password.isEmpty() && client_id > 0;
    }
}
