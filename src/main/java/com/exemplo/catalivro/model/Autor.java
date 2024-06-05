package com.exemplo.catalivro.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private int birthYear;
    private int deathYear;

    // Construtor
    public Autor() {
    }

    public Autor(String nome, int birthYear, int deathYear) {
        this.nome = nome;
        this.birthYear = birthYear;
        this.deathYear = deathYear;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getAnoNascimento() {
        return birthYear;
    }

    public void setAnoNascimento(int anoNascimento) {
        this.birthYear = anoNascimento;
    }

    public int getAnoMorte() {
        return deathYear;
    }

    public void setAnoMorte(int anoMorte) {
        this.deathYear = anoMorte;
    }
}
