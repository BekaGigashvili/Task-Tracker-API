package com.javaprojects.tasktrackerapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            UserNotFoundException.class,
            ProjectNotFoundException.class,
    })
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(Exception ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(UserAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyRegisteredException(Exception ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(NonExistentRoleException.class)
    public ResponseEntity<ErrorResponse> handleNonExistentRoleException(Exception ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(WrongEmailOrPasswordException.class)
    public ResponseEntity<ErrorResponse> handleWrongEmailOrPasswordException(Exception ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(ProjectAccessException.class)
    public ResponseEntity<ErrorResponse> handleProjectAccessException(Exception ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

}
