package com.epam.brn.controller.advice

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.exception.FileFormatException
import java.lang.reflect.Method
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

internal class ExceptionControllerAdviceTest {
    private val exceptionControllerAdvice: ExceptionControllerAdvice =
        ExceptionControllerAdvice()

    @Test
    fun `should handle EntityNotFoundException`() {
        // GIVEN
        val exception = EntityNotFoundException("tasks were not found")
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleEntityNotFoundException(exception)
        // THEN
        assertTrue((responseEntity.body as BaseResponseDto).errors.toString().contains("tasks were not found"))
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType())
    }

    @Test
    fun `should handle MethodArgumentNotValidException`() {

        // GIVEN
        val bindingResult = mock(BindingResult::class.java)
        val methodParameter = MethodParameter(mock(Method::class.java), -1)
        val exception = MethodArgumentNotValidException(methodParameter, bindingResult)

        `when`(bindingResult.fieldErrors).thenReturn(
            listOf(
                FieldError("TestEntity", "field1", "Incorrect field1 format"),
                FieldError("TestEntity", "field2", "Incorrect field2 format")
            )
        )

        // WHEN
        val responseEntity = exceptionControllerAdvice.handleMethodArgumentNotValidException(exception)

        // THEN
        assertTrue(
            (responseEntity.body as BaseResponseDto).errors.containsAll(
                listOf(
                    "Incorrect field1 format",
                    "Incorrect field2 format"
                )
            )
        )
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)
    }

    @Test
    fun `should handle Exception`() {
        // GIVEN
        val exception = Exception("some exception")
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleException(exception)
        // THEN
        assertTrue((responseEntity.body as BaseResponseDto).errors.toString().contains("some exception"))
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType())
    }

    @Test
    fun `should handle FileFormatException`() {
        // GIVEN
        val exception = FileFormatException()
        // WHEN
        val responseEntity = exceptionControllerAdvice.handleFileFormatException(exception)
        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)
        assertTrue(
            (responseEntity.body as BaseResponseDto).errors
                .contains("Formatting error. Please upload file with csv extension.")
        )
    }

    @Test
    fun `should make InternalServerErrorResponseEntity`() {
        // GIVEN
        val exception = Exception("some test exception")
        // WHEN
        val responseEntity = exceptionControllerAdvice.createInternalErrorResponse(exception)
        // THEN
        assertTrue((responseEntity.body as BaseResponseDto).errors.toString().contains("some test exception"))
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType())
    }
}
