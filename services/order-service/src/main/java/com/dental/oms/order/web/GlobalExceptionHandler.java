package com.dental.oms.order.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidOrderException.class)
    ProblemDetail invalid(InvalidOrderException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(OutOfStockException.class)
    ProblemDetail outOfStock(OutOfStockException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setProperty("shortages", ex.getShortages());
        return pd;
    }

    /** Downstream returned 5xx (after any mesh retries). */
    @ExceptionHandler(DownstreamException.class)
    ProblemDetail downstream(DownstreamException ex) {
        log.warn("Downstream failure: {}", ex.getMessage());
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        pd.setProperty("downstream", ex.getService());
        pd.setProperty("downstreamStatus", ex.getStatus());
        return pd;
    }

    /** Connect/read timeout or connection refused — the app-level safety net fired. */
    @ExceptionHandler(ResourceAccessException.class)
    ProblemDetail unreachable(ResourceAccessException ex) {
        log.warn("Downstream unreachable: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "Downstream unreachable: " + ex.getMessage());
    }
}
