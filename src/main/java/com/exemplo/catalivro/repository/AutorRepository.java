package com.exemplo.catalivro.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.exemplo.catalivro.model.Autor;

public interface AutorRepository extends JpaRepository<Autor, Long> {

    @Query("SELECT a FROM Autor a WHERE a.nome = :nome AND a.birthYear = :birthYear AND a.deathYear = :deathYear")
    Optional<Autor> findByNameAndBirthYearAndDeathYear(@Param("nome") String nome,
                                                       @Param("birthYear") int birthYear,
                                                       @Param("deathYear") int deathYear);

    @Query("SELECT a FROM Autor a WHERE a.birthYear <= :birthYear AND (a.deathYear IS NULL OR a.deathYear >= :deathYear)")
    List<Autor> findByBirthYearLessThanEqualAndDeathYearGreaterThanEqual(@Param("birthYear") int birthYear, @Param("deathYear") int deathYear);
}
