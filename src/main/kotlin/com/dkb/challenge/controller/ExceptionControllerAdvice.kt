package com.dkb.challenge.controller

import com.dkb.challenge.exception.OriginalUrlNotFoundException
import com.dkb.challenge.model.dto.ErrorTemplate
import jakarta.validation.ConstraintViolationException
import org.hibernate.validator.internal.engine.path.PathImpl
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class ExceptionControllerAdvice {
    private val logger = LoggerFactory.getLogger(ExceptionControllerAdvice::class.java)

    @ExceptionHandler
    fun methodArgTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorTemplate> {
        logger.error(ex.message, ex)
        val errorMessage = ErrorTemplate("Invalid request parameter. Please verify your input and try again.")
        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorTemplate> {
        logger.error(ex.message, ex)
        val details = mutableMapOf<String, String>()
        ex.bindingResult.fieldErrors.map { error ->
            details[error.field] = error.defaultMessage ?: "Invalid argument"
        }
        val errorMessage = ErrorTemplate("Invalid argument. Please verify your input and try again.", details)
        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(OriginalUrlNotFoundException::class)
    fun handleNotFoundException(ex: Exception): ResponseEntity<ErrorTemplate> {
        logger.error(ex.message, ex)
        val errorMessage = ErrorTemplate(ex.message ?: "Entity Not Found")
        return ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorTemplate> {
        logger.error(ex.message, ex)
        val details =
            ex.constraintViolations.associate { violation ->
                val fieldName = (violation.propertyPath as PathImpl).leafNode.toString()
                fieldName to violation.message
            }
        val errorMessage = ErrorTemplate("Invalid request parameter. Please verify your input and try again.", details)
        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ErrorTemplate> {
        logger.error("Invalid request body: ${ex.message}", ex)

        val errorMessage = "Invalid request: Missing or unreadable request body. Please provide a valid JSON payload."
        return ResponseEntity(ErrorTemplate(errorMessage), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingRequestParam(ex: MissingServletRequestParameterException): ResponseEntity<ErrorTemplate> {
        logger.error("Missing request parameter: ${ex.parameterName}", ex)

        val errorMessage = "Invalid request: Missing required parameter '${ex.parameterName}'. Please provide a valid value."
        return ResponseEntity(ErrorTemplate(errorMessage), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    fun handleGeneralException(ex: Exception): ResponseEntity<ErrorTemplate> {
        logger.error(ex.message, ex)
        val errorMessage = ErrorTemplate("Something has happened. Please try again later.")
        return ResponseEntity(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
