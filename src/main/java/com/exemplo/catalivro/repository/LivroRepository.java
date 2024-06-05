package com.exemplo.catalivro.repository;

import com.exemplo.catalivro.model.Autor;
import com.exemplo.catalivro.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface LivroRepository extends JpaRepository<Livro, Long> {
    Optional<Livro> findByTitulo(String titulo);

    @Query("SELECT l FROM Livro l WHERE l.idioma = :idioma")
    List<Livro> findByIdioma(@Param("idioma") String idioma);

    List<Livro> findAllByAutor(Autor autor);
}