package com.finmark.movies.service

import com.finmark.movies.dto.MoviesDTO
import com.finmark.movies.repository.MoviesRepository
import com.finmark.movies.utils.transformDTOToEntity
import com.finmark.movies.utils.transformEntityToDTO
import com.finmark.movies.utils.validateMovieRequest
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

const val MANDATORY_PARAMS_NOT_SET = "constant value defined at the top-level"
const val ENTITY_ALREADY_EXISTS = "Movie already exists"

@Service
class MoviesService(val moviesRepository: MoviesRepository) {

    companion object: KLogging()

    fun getMovie(id:Long):  ResponseEntity<MoviesDTO>{
        val moviesEntity = moviesRepository.findById(id)
        if(moviesEntity.isEmpty) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
        return ResponseEntity.ok(transformEntityToDTO(moviesEntity.get()));
    }

    //TODO: Add pagination support to handle large responses
    fun getMovies(): ResponseEntity<List<MoviesDTO>> {
        return ResponseEntity.ok(moviesRepository.findAll().map{
            transformEntityToDTO(it)
        })
    }

    fun addMovie(moviesDTO: MoviesDTO): ResponseEntity<MoviesDTO> {

        if (!validateMovieRequest(moviesDTO)) {
            moviesDTO.errorMessage = MANDATORY_PARAMS_NOT_SET
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(moviesDTO)
        }

        if (moviesRepository.findByTitleAndReleaseDate(moviesDTO.title, moviesDTO.releaseDate).isPresent) {
            moviesDTO.errorMessage = ENTITY_ALREADY_EXISTS
            return ResponseEntity.status(HttpStatus.CONFLICT).body(moviesDTO)
        }

        var moviesEntity = transformDTOToEntity(moviesDTO)
        moviesEntity = moviesRepository.save(moviesEntity)
        logger.info { "Saved course is $moviesEntity" }

        return ResponseEntity.created(UriComponentsBuilder.fromPath("/movies/${moviesEntity.id}").build().toUri()).build()
    }

    fun updateMovie(id:Long, moviesDTO: MoviesDTO): ResponseEntity<MoviesDTO>{
        if (!validateMovieRequest(moviesDTO)) {
            moviesDTO.errorMessage = MANDATORY_PARAMS_NOT_SET
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(moviesDTO)
        }

        val existingMovieEntity = moviesRepository.findById(id)

        if(existingMovieEntity.isEmpty) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        var moviesEntity = transformDTOToEntity(moviesDTO)
        moviesRepository.save(moviesEntity)

        logger.info { "Updated movie is $moviesEntity" }

        return ResponseEntity.ok().build()
    }

    fun deleteMovie (id:Long): ResponseEntity<HttpStatus> {
        val existingMovieEntity = moviesRepository.findById(id)

        if(existingMovieEntity.isEmpty) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
        moviesRepository.deleteById(id);
        return ResponseEntity.noContent().build()
    }
}