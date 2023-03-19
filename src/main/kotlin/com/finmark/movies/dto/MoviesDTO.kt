package com.finmark.movies.dto

import java.time.LocalDate

data class MoviesDTO(
    val id : Long?,
    val title : String,
    val releaseDate : LocalDate,
    val stars : Set<String>,
    var errorMessage : String? = null
    )
