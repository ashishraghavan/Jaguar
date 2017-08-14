package com.jaguar.test;


import com.jaguar.exception.ErrorMessage;

public class ExceptionMain {
    public static void main(String[] args) {
        final ErrorMessage errorMessage = new ErrorMessage.Builder()
                .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                .withMessage("Your Ass")
                .build();
        System.out.println(errorMessage);
    }
}
