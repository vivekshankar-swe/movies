package com.finmark.movies.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.finmark.movies.dto.MoviesDTO
import com.finmark.movies.entity.Movies
import com.finmark.movies.repository.MoviesRepository
import com.finmark.movies.utils.transformEntityToDTO
import org.hibernate.internal.util.collections.CollectionHelper.listOf
import org.hibernate.internal.util.collections.CollectionHelper.setOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.LocalDate
import java.util.*


internal class MoviesServiceTest {

    @Mock
    lateinit var repository: MoviesRepository

    @InjectMocks
    lateinit var service: MoviesService

    private val mapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mapper.registerModule(JavaTimeModule())
    }

    @Test
    fun `getMovie should return the movie with the specified ID`() {
        val movie = Movies(
            id = 1L,
            title = "Pulp Fiction",
            releaseDate = LocalDate.of(1999,10,14),
            stars = setOf("John Travolta", "Uma Thurman", "Samuel L. Jackson")
        )
        given(repository.findById(anyLong())).willReturn(java.util.Optional.of(movie))

        val result: ResponseEntity<MoviesDTO> = service.getMovie(1)

        val expected = mapper.writeValueAsString(transformEntityToDTO(movie))
        val actual =  mapper.writeValueAsString(result.body)
        assertEquals(expected, actual)
    }

    @Test
    fun `getMovie should return 404 response when movie with specified ID is not found`() {
        given(repository.findById(anyLong())).willReturn(java.util.Optional.empty())
        val result: ResponseEntity<MoviesDTO> = service.getMovie(2)
        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
    }

    @Test
    fun `getMovies should return all movies stored in the repository`() {
        val movies = listOf(Movies(
            id = 1L,
            title = "Pulp Fiction",
            releaseDate = LocalDate.of(1999,10,14),
            stars = setOf("John Travolta", "Uma Thurman", "Samuel L. Jackson")
        ),
            Movies(
                id = 2L,
                title = "Inception",
                releaseDate = LocalDate.of(2010,6,13),
                stars = setOf("Leonardo DiCaprio", "Ken Watanabe", "Elliot Page")
            ))

        given(repository.findAll()).willReturn(movies)

        val result: ResponseEntity<List<MoviesDTO>> = service.getMovies()


        val expected = mapper.writeValueAsString(movies.map {
            transformEntityToDTO(it)
        })
        val actual =  mapper.writeValueAsString(result.body)
        assertEquals(expected, actual)
    }

    @Test
    fun `addMovie should add a new movie`() {
        val movie = Movies(
            id = null,
            title = "Pulp Fiction",
            releaseDate = LocalDate.of(1999,10,14),
            stars = setOf("John Travolta", "Uma Thurman", "Samuel L. Jackson")
        )
        given(repository.findByTitleAndReleaseDate(movie.title, movie.releaseDate)).willReturn(Optional.empty())
        given(repository.save(movie)).willReturn(movie.copy(id = 1L))

        val result: ResponseEntity<MoviesDTO> = service.addMovie(transformEntityToDTO(movie))

        val expected = "/movies/1"
        val actual = result.headers.get("Location")?.get(0)

        assertEquals(expected, actual)
        assertEquals(result.statusCode, HttpStatus.CREATED)
    }

    @Test
    fun `addMovie should not update duplicate stars for a new movie`() {
        val movie = Movies(
            id = null,
            title = "Pulp Fiction",
            releaseDate = LocalDate.of(1999,10,14),
            stars = setOf("John Travolta", "John Travolta", "Uma Thurman", "Samuel L. Jackson")
        )
        given(repository.findByTitleAndReleaseDate(movie.title, movie.releaseDate)).willReturn(Optional.empty())
        given(repository.save(movie)).willReturn(movie.copy(id = 1L))

        val result: ResponseEntity<MoviesDTO> = service.addMovie(transformEntityToDTO(movie))

        val expectedMovie = Movies(
            id = null,
            title = "Pulp Fiction",
            releaseDate = LocalDate.of(1999,10,14),
            stars = setOf("John Travolta", "Uma Thurman", "Samuel L. Jackson")
        )

        verify(repository).save(expectedMovie)

        val expected = "/movies/1"
        val actual = result.headers.get("Location")?.get(0)

        assertEquals(expected, actual)
        assertEquals(result.statusCode, HttpStatus.CREATED)
    }

    @Test
    fun `addMovie should return BAD_REQUEST if no stars are passed for a new movie`() {
        val movie = Movies(
            id = null,
            title = "Pulp Fiction",
            releaseDate = LocalDate.of(1999,10,14),
            stars = setOf()
        )
        given(repository.findByTitleAndReleaseDate(movie.title, movie.releaseDate)).willReturn(Optional.empty())
        given(repository.save(movie)).willReturn(movie.copy(id = 1L))

        val result: ResponseEntity<MoviesDTO> = service.addMovie(transformEntityToDTO(movie))

        assertEquals(result.statusCode, HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `updateMovie should update an existing movie`() {
        val existingMovie = Movies(
            id = 1L,
            title = "Pulp Fiction",
            releaseDate = LocalDate.of(1999,10,14),
            stars = setOf("John Travolta", "Uma Thurman", "Samuel L. Jackson")
        )

        val movie = Movies(
            id = 1L,
            title = "Pulp Fiction",
            releaseDate = LocalDate.of(1999,10,14),
            stars = setOf("John Travolta", "Uma Thurman", "Samuel L. Jackson", "Anonymous")
        )
        given(repository.findById(1L)).willReturn(Optional.of(existingMovie))
        given(repository.save(movie)).willReturn(movie)

        val result: ResponseEntity<MoviesDTO> = service.updateMovie(1L, transformEntityToDTO(movie))

        verify(repository).save(movie)
        assertEquals(result.statusCode, HttpStatus.OK)
    }

    @Test
    fun `updateMovie should return 404 response when movie with specified ID is not found`() {
        val movie = Movies(
            id = 1L,
            title = "Pulp Fiction",
            releaseDate = LocalDate.of(1999,10,14),
            stars = setOf("John Travolta", "Uma Thurman", "Samuel L. Jackson", "Anonymous")
        )
        given(repository.findById(1L)).willReturn(Optional.empty())
        given(repository.save(movie)).willReturn(movie)

        val result: ResponseEntity<MoviesDTO> = service.updateMovie(1L, transformEntityToDTO(movie))

        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)
    }

    @Test
    fun `updateMovie should return BAD_REQUEST if no stars are passed for an existing movie being updated`() {
        val existingMovie = Movies(
            id = 1L,
            title = "Pulp Fiction",
            releaseDate = LocalDate.of(1999,10,14),
            stars = setOf("John Travolta", "Uma Thurman", "Samuel L. Jackson")
        )

        val movie = Movies(
            id = 1L,
            title = "Pulp Fiction",
            releaseDate = LocalDate.of(1999,10,14),
            stars = setOf()
        )
        given(repository.findById(1L)).willReturn(Optional.of(existingMovie))
        given(repository.save(movie)).willReturn(movie)

        val result: ResponseEntity<MoviesDTO> = service.updateMovie(1L, transformEntityToDTO(movie))

        assertEquals(result.statusCode, HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `deleteMovie should delete an existing movie`() {
        val existingMovie = Movies(
            id = 1L,
            title = "Pulp Fiction",
            releaseDate = LocalDate.of(1999,10,14),
            stars = setOf("John Travolta", "Uma Thurman", "Samuel L. Jackson")
        )

        given(repository.findById(1L)).willReturn(Optional.of(existingMovie))
        given(repository.deleteById(1L)).will {  }

        val result: ResponseEntity<HttpStatus> = service.deleteMovie(1L)
        assertEquals(result.statusCode, HttpStatus.NO_CONTENT)
    }

    @Test
    fun `deleteMovie should return 404 response when movie with specified ID is not found`() {
        given(repository.findById(1L)).willReturn(Optional.empty())
        val result: ResponseEntity<HttpStatus> = service.deleteMovie(1L)
        assertEquals(result.statusCode, HttpStatus.NOT_FOUND)
    }
}