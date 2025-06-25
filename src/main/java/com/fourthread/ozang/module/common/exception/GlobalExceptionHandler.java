package com.fourthread.ozang.module.common.exception;

import com.fourthread.ozang.module.domain.weather.exception.InvalidCoordinateException;
import com.fourthread.ozang.module.domain.weather.exception.WeatherApiException;
import com.fourthread.ozang.module.domain.weather.exception.WeatherDataFetchException;
import com.fourthread.ozang.module.domain.weather.exception.WeatherNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidCoordinateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCoordinateException(InvalidCoordinateException e) {
        log.error("잘못된 좌표: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            e.getClass().getSimpleName(),
            e.getMessage(),
            null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(WeatherNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWeatherNotFoundException(WeatherNotFoundException e) {
        log.error("날씨 정보 없음: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            e.getClass().getSimpleName(),
            e.getMessage(),
            null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(WeatherApiException.class)
    public ResponseEntity<ErrorResponse> handleWeatherApiException(WeatherApiException e) {
        log.error("날씨 API 오류: {} (코드: {})", e.getMessage(), e.getResultCode());

        Map<String, String> details = new HashMap<>();
        details.put("resultCode", e.getResultCode());

        ErrorResponse errorResponse = new ErrorResponse(
            e.getClass().getSimpleName(),
            e.getMessage(),
            details
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(WeatherDataFetchException.class)
    public ResponseEntity<ErrorResponse> handleWeatherDataFetchException(WeatherDataFetchException e) {
        log.error("날씨 데이터 조회 실패: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            e.getClass().getSimpleName(),
            e.getMessage(),
            null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    //  Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
            "ValidationException",
            "입력값이 올바르지 않습니다.",
            errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        Map<String, String> errors = new HashMap<>();

        e.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        });

        ErrorResponse errorResponse = new ErrorResponse(
            "ConstraintViolationException",
            "제약 조건 위반",
            errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException e) {
        String paramName = e.getParameterName();
        String message = String.format("필수 파라미터 '%s'가 누락되었습니다.", paramName);

        ErrorResponse errorResponse = new ErrorResponse(
            e.getClass().getSimpleName(),
            message,
            null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = String.format("파라미터 '%s'의 타입이 올바르지 않습니다.", e.getName());

        Map<String, String> details = new HashMap<>();
        details.put("parameter", e.getName());
        details.put("expectedType", e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "Unknown");
        details.put("actualValue", e.getValue() != null ? e.getValue().toString() : "null");

        ErrorResponse errorResponse = new ErrorResponse(
            e.getClass().getSimpleName(),
            message,
            details
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("예상치 못한 오류 발생", e);

        ErrorResponse errorResponse = new ErrorResponse(
            "InternalServerError",
            "서버 내부 오류가 발생했습니다.",
            null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    public record ErrorResponse(
        String exceptionName,
        String message,
        Map<String, String> details
    ) {}
}
