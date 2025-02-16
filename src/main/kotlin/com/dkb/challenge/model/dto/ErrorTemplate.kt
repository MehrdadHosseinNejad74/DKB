package com.dkb.challenge.model.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Represents an error response with a message describing the issue.",
    example = """
    {
      "message": "An unexpected error occurred while processing your request.",
      "errorDetails": {
        "field1": "Description of the issue with field1",
        "field2": "Description of the issue with field2"
      }
    }
    """
)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorTemplate(
    val message: String,
    val errorDetails: Map<String, String>? = null
)
