package com.finmark.movies.utils

import com.finmark.movies.dto.MoviesDTO
import com.finmark.movies.entity.Movies

fun transformEntityToDTO (movies: Movies): MoviesDTO {
    return movies.let {
        MoviesDTO(it.id, it.title, it.releaseDate, it.stars)
    }
}

fun transformDTOToEntity (moviesDTO: MoviesDTO): Movies {
    return moviesDTO.let {
        Movies(it.id, it.title, it.releaseDate, it.stars)
    }
}