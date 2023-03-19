package com.finmark.movies.utils

import com.finmark.movies.dto.MoviesDTO

fun validateMovieRequest(moviesDTO: MoviesDTO): Boolean {

    if (moviesDTO.title.isEmpty() || moviesDTO.releaseDate == null || moviesDTO.stars == null || moviesDTO.stars.isEmpty()) {
        return false;
    }

    return true;
}