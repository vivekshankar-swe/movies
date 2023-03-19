package com.finmark.movies.repository

import com.finmark.movies.entity.Movies
import org.springframework.data.repository.CrudRepository
import java.time.LocalDate
import java.util.*

interface MoviesRepository : CrudRepository<Movies, Long> {
    fun findByTitleAndReleaseDate(title: String, releaseDate: LocalDate): Optional<Movies>
}