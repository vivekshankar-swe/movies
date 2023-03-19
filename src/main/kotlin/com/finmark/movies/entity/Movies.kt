package com.finmark.movies.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "Movies")
data class Movies(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long?,
    @Column(nullable = false)
    val title : String,
    @Column(nullable = false)
    val releaseDate : LocalDate,
    @Column(nullable = false)
    val stars : Set<String>
)