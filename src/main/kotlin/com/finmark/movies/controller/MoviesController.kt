package com.finmark.movies.controller

import com.finmark.movies.dto.MoviesDTO
import com.finmark.movies.service.MoviesService
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/movies")
class MoviesController(val moviesService: MoviesService) {

    companion object : KLogging()

    @GetMapping("/{id}")
    fun getMovie(@PathVariable("id") id:Long): ResponseEntity<MoviesDTO> {
        logger.info( "Getting movie with id $id")
        return moviesService.getMovie(id)
    }

    @GetMapping()
    fun getMovies(): ResponseEntity<List<MoviesDTO>> {
        logger.info( "Getting all movies")
        return moviesService.getMovies()
    }

    @PostMapping
    fun addMovie(@RequestBody moviesDTO: MoviesDTO): ResponseEntity<MoviesDTO> {
        return moviesService.addMovie(moviesDTO)
    }

    @PutMapping("/{id}")
    fun updateMovie(@PathVariable("id") id:Long, @RequestBody moviesDTO: MoviesDTO): ResponseEntity<MoviesDTO> {
        return moviesService.updateMovie(id, moviesDTO)
    }

    @DeleteMapping("/{id}")
    fun deleteMovie (@PathVariable("id") id:Long): ResponseEntity<HttpStatus> {
        return moviesService.deleteMovie(id)
    }
}