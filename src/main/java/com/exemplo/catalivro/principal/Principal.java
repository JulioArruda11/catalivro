package com.exemplo.catalivro.principal;

import com.exemplo.catalivro.model.Autor;
import com.exemplo.catalivro.model.Livro;
import com.exemplo.catalivro.model.LivroRecord;
import com.exemplo.catalivro.repository.AutorRepository;
import com.exemplo.catalivro.repository.LivroRepository;
import com.exemplo.catalivro.service.ConverteDados;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class Principal {
    @Autowired
    private AutorRepository autorRepository;
    @Autowired
    private LivroRepository livroRepository;

    private final ConverteDados conversor;

    @Autowired
    public Principal(LivroRepository livroRepository, ConverteDados conversor) {
        this.livroRepository = livroRepository;
        this.conversor = conversor;
    }

    public void exibeMenu() throws IOException, InterruptedException {
        Scanner leitura = new Scanner(System.in);
        int opcao = -1;

        while (opcao != 0) {
            System.out.println("""
                ***************************************************
                                    CataLivro
                ***************************************************                   

                [ 1 ] Buscar livro pelo título
                [ 2 ] Listar livros registrados
                [ 3 ] Listar autores registrados
                [ 4 ] Listar autores vivos em um determinado ano
                [ 5 ] Listar livros em um determinado idioma
                [ 0 ] SAIR                    
                """);
            System.out.print("Digite a sua opção: ");

            // Verifica se a entrada é um número inteiro
            if (leitura.hasNextInt()) {
                opcao = leitura.nextInt();
                leitura.nextLine(); // Limpa o buffer do scanner
            } else {
                System.out.println("Por favor, insira um número inteiro.");
                leitura.nextLine(); // Limpa o buffer do scanner
                continue; // Volta para o início do loop
            }

            switch (opcao) {
                case 0:
                    break;
                case 1:
                    System.out.println("Qual o nome do livro que você deseja buscar?");
                    var nomeLivro = leitura.nextLine();
                    buscaLivro(nomeLivro);
                    break;
                case 2:
                    listarLivrosRegistrados();
                    break;
                case 3:
                    listarAutoresRegistrados();
                    break;
                case 4:
                    System.out.println("Digite o ano que deseja pesquisar:");
                    int ano = leitura.nextInt();
                    leitura.nextLine();
                    listarAutoresVivosEmAno(ano);
                    break;
                case 5:
                    System.out.println("Escolha o idioma desejado:");
                    System.out.println("1. Espanhol (es)");
                    System.out.println("2. Inglês (en)");
                    System.out.println("3. Francês (fr)");
                    System.out.println("4. Português (pt)");
                    System.out.print("\nDigite o número correspondente ao idioma: \n");
                    int opcaoIdioma = leitura.nextInt();
                    leitura.nextLine(); // Limpa o buffer do scanner
                    String idioma;
                    switch (opcaoIdioma) {
                        case 1:
                            idioma = "es";
                            break;
                        case 2:
                            idioma = "en";
                            break;
                        case 3:
                            idioma = "fr";
                            break;
                        case 4:
                            idioma = "pt";
                            break;
                        default:
                            System.out.println("Opção inválida.");
                            return; // Retorna ao menu principal
                    }
                    listarLivrosPorIdioma(idioma);
                    break;
                default:
                    System.out.println("\nOpção inválida, tente novamente!\n");
            }
        }
    }

    public void buscaLivro(String nomeBusca) throws IOException, InterruptedException {
        String nomeBuscaFormatado = nomeBusca.replace(" ", "%20");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://gutendex.com/books/?search=" + nomeBuscaFormatado)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String json = response.body();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        JsonNode resultsNode = rootNode.get("results");
        List<LivroRecord> livros = new ArrayList<>();
        if (resultsNode != null && resultsNode.isArray()) {
            for (JsonNode livroNode : resultsNode) {
                long id = livroNode.get("id").asLong();
                String title = livroNode.get("title").asText();
                List<Autor> autores = new ArrayList<>();
                JsonNode authorsNode = livroNode.get("authors");
                if (authorsNode != null && authorsNode.isArray()) {
                    for (JsonNode autorNode : authorsNode) {
                        String nomeAutor = autorNode.get("name").asText();
                        int birthYear = autorNode.get("birth_year").asInt();
                        int deathYear = autorNode.get("death_year").asInt();
                        Optional<Autor> autorOptional = autorRepository.findByNameAndBirthYearAndDeathYear(nomeAutor, birthYear, deathYear);
                        Autor autor;
                        if (autorOptional.isPresent()) {
                            autor = autorOptional.get();
                        } else {
                            autor = new Autor(nomeAutor, birthYear, deathYear);
                            autorRepository.save(autor);
                        }
                        autores.add(autor);
                    }
                }
                List<String> idiomas = new ArrayList<>();
                JsonNode languagesNode = livroNode.get("languages");
                if (languagesNode != null && languagesNode.isArray()) {
                    for (JsonNode idiomaNode : languagesNode) {
                        idiomas.add(idiomaNode.asText());
                    }
                }
                int downloadCount = livroNode.get("download_count").asInt();
                LivroRecord livroRecord = new LivroRecord(id, title, autores, idiomas, downloadCount);
                livros.add(livroRecord);
            }
        }
        if (livros.isEmpty()) {
            System.out.println("\nNenhum livro encontrado para a busca: " + nomeBusca + "\n");
        } else {
            for (LivroRecord livroRecord : livros) {
                System.out.println("---- LIVRO ----");
                System.out.println("Título: " + livroRecord.getTitle());
                System.out.print("Autor: ");
                for (Autor autor : livroRecord.getAuthors()) {
                    System.out.print(autor.getNome() + ", ");
                }
                System.out.println();
                System.out.println("Idioma: " + livroRecord.getLanguages().get(0));
                System.out.println("Downloads: " + livroRecord.getDownloadCount());
                System.out.println();
            }
        }

        List<Livro> livrosSalvos = new ArrayList<>();
        for (LivroRecord livroRecord : livros) {
            Optional<Livro> livroExistente = livroRepository.findByTitulo(livroRecord.getTitle());
            if (livroExistente.isPresent()) {
                Livro livroAtualizado = livroExistente.get();
                livroAtualizado.setDownloads(livroRecord.getDownloadCount());
                livroRepository.save(livroAtualizado);
            } else {
                Livro livro = new Livro();
                livro.setTitulo(livroRecord.getTitle());
                livro.setIdioma(livroRecord.getLanguages().get(0)); // Supondo que cada livro tenha apenas um idioma
                livro.setDownloads(livroRecord.getDownloadCount());

                // Verifica se autor já existe, se não, salva novo autor
                for (Autor autor : livroRecord.getAuthors()) {
                    Optional<Autor> autorExistente = autorRepository.findByNameAndBirthYearAndDeathYear(autor.getNome(), autor.getAnoNascimento(), autor.getAnoMorte());
                    if (autorExistente.isPresent()) {
                        livro.setAutor(autorExistente.get());
                    } else {
                        livro.setAutor(autor);
                        autorRepository.save(autor);
                    }
                }

                livroRepository.save(livro);
            }
        }
    }


    public void adicionarLivro(Livro livro) {

        int maxTituloLength = 255; // Defina o comprimento máximo permitido conforme necessário
        if (livro.getTitulo().length() > maxTituloLength) {
            // Trunca o título para o comprimento máximo permitido
            String tituloTruncado = livro.getTitulo().substring(0, maxTituloLength);
            livro.setTitulo(tituloTruncado);
            System.out.println("O título do livro foi truncado para " + maxTituloLength + " caracteres.");
        }

        // Verifica se o livro já existe no banco de dados
        Optional<Livro> livroExistente = livroRepository.findByTitulo(livro.getTitulo());
        if (livroExistente.isPresent()) {
            // Se existir, atualiza as informações do livro
            Livro livroAtualizado = livroExistente.get();
            livroAtualizado.setDownloads(livro.getDownloads());
            livroRepository.save(livroAtualizado);
        } else {
            // Se não existir, salva o novo livro no banco de dados
            livroRepository.save(livro);
        }
    }

    public void listarLivrosRegistrados() {
        List<Livro> livros = livroRepository.findAll();
        if (livros.isEmpty()) {
            System.out.println("Nenhum livro registrado.");
        } else {
            for (Livro livro : livros) {
                System.out.println("---- Livro ----");
                System.out.println("Título: " + livro.getTitulo());
                System.out.println("Autor: " + livro.getAutor().getNome());
                System.out.println("Idioma: " + livro.getIdioma());
                System.out.println("Downloads: " + livro.getDownloads());
                System.out.println();
            }
        }
    }


    public void listarAutoresRegistrados() {
        List<Autor> autores = autorRepository.findAll();
        if (autores.isEmpty()) {
            System.out.println("Nenhum autor registrado.");
        } else {
            for (Autor autor : autores) {
                System.out.println("---- Autor ----");
                System.out.println("Nome: " + autor.getNome());
                System.out.println("Ano de Nascimento: " + autor.getAnoNascimento());
                System.out.println("Ano de Falecimento: " + autor.getAnoMorte());
                System.out.println("Livros:");

                // Lista os livros associados a este autor
                List<Livro> livrosDoAutor = livroRepository.findAllByAutor(autor);
                if (livrosDoAutor.isEmpty()) {
                    System.out.println("Nenhum livro associado a este autor.");
                } else {
                    for (Livro livro : livrosDoAutor) {
                        System.out.println("   - " + livro.getTitulo());
                    }
                }
                System.out.println();
            }
        }
    }


    public void listarAutoresVivosEmAno(int ano) {
        List<Autor> autores = autorRepository.findByBirthYearLessThanEqualAndDeathYearGreaterThanEqual(ano, ano);
        if (autores.isEmpty()) {
            System.out.println("Nenhum autor vivo em " + ano);
        } else {
            System.out.println("Autores vivos em " + ano + ":");
            for (Autor autor : autores) {
                System.out.println("---- Autor ----");
                System.out.println("Nome: " + autor.getNome());
                System.out.println("Ano de Nascimento: " + autor.getAnoNascimento());
                System.out.println("Ano de Falecimento: " + autor.getAnoMorte());
                System.out.println("Livros:");

                // Lista os livros associados a este autor
                List<Livro> livrosDoAutor = livroRepository.findAllByAutor(autor);
                if (livrosDoAutor.isEmpty()) {
                    System.out.println("Nenhum livro associado a este autor.");
                } else {
                    for (Livro livro : livrosDoAutor) {
                        System.out.println("   - " + livro.getTitulo());
                    }
                }
                System.out.println();
            }
        }
    }


    public void listarLivrosPorIdioma(String idioma) {
        List<Livro> livros = livroRepository.findByIdioma(idioma);
        if (livros.isEmpty()) {
            System.out.println("Nenhum livro disponível em " + idioma);
        } else {
            System.out.println("---- Livros em " + idioma + " ----");
            for (Livro livro : livros) {
                System.out.println("Título: " + livro.getTitulo());
                System.out.println("Autor: " + livro.getAutor().getNome());
                System.out.println("Idioma: " + livro.getIdioma());
                System.out.println("Downloads: " + livro.getDownloads());
                System.out.println();
            }
        }
    }





}
