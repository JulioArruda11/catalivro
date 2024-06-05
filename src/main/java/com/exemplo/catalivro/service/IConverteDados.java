package com.exemplo.catalivro.service;

public interface IConverteDados {
    <T> T obterDados(String json, Class<T> classe);
}
