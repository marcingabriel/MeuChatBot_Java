import analyzer.impl.AlphabetAnalyzer;
import analyzer.impl.KeywordAnalyzer;
import analyzer.impl.StopWordsAnalyzer;
import analyzer.impl.SyntaxAnalyzer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import utils.Utils;


//Realizado no IntelliJ IDEA
//Autor:Marcio Gabriel Gonçalves Soares
//Referencia stopwords: https://github.com/thiagoscouto/stopwords_ptbr/blob/master/stopwords_ptbr.txt
//Referencia Metodo Similaridade: https://medium.com/@everton.tomalok/calculando-similaridades-entre-strings-ebbea21d5b7a


public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("************************************************************************");
        System.out.println("*                                                                      *");
        System.out.println("*      Bem-Vindo ao meu ChatBot Musical!                               *");
        System.out.println("*                                                                      *");
        System.out.println("*  Essas são os tipos de perguntas  que eu posso responder:            *");
        System.out.println("*  1. Como afinar meu violão?                                          *");
        System.out.println("*  2. Quero um teclado para iniciantes.                                *");
        System.out.println("*  3. Qual o preço da guitarra gibson?                                 *");
        System.out.println("*                                                                      *");
        System.out.println("*  Digite '0' ou 'exit' a qualquer momento para encerrar a conversa.   *");
        System.out.println("*                                                                      *");
        System.out.println("************************************************************************");

        List<String> wordsList = readWordsFromInput();

        // Verificação léxica, com avaliação do alfabeto usado. 
        AlphabetAnalyzer alphabetAnalyzer = new AlphabetAnalyzer();
        List<String> validWords =  alphabetAnalyzer.analyze(wordsList);

        //Removendo pontuacoes !.? ...
        validWords =  Utils.processWords(validWords);
        //System.out.println(" Palavras validas:" +  validWords);

        
        //Identificando e removendo stopwords
        StopWordsAnalyzer stopWordsAnalyzer = new StopWordsAnalyzer();
        List<String> palavras = stopWordsAnalyzer.analyze(validWords);
        //System.out.println("Lista de Palavras sem StopWords:" +  palavras); // tabela com todas as palavras no texto [EXCETO STOPWORDS]

        //Identificando Keywords
        KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
        List<String> simbolos =  keywordAnalyzer.analyze(validWords);
        //System.out.println(" Tabela de Simbolos: " +  simbolos); //tabela com todas as palavras presentes no texto que não sejam palavras-chaves e stopwords

        //Analise Sintatica 
        SyntaxAnalyzer analyzer = new SyntaxAnalyzer();
        List<String> palavrasValidas = new ArrayList<>(); 
        palavrasValidas = palavras;
        Map<String, String> tabelaDeSimbolos = new HashMap<>();
        Scanner scanner = new Scanner(System.in);
        System.out.println("");

        try {
            analyzer.analyzePhrase(palavrasValidas, tabelaDeSimbolos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String removerAcentos(String palavra) {
        String normalized = Normalizer.normalize(palavra, Normalizer.Form.NFD);
        return normalized.replaceAll("[^\\p{ASCII}]", "");
    }

    // Método que lê palavras da entrada e remove os acentos
    private static List<String> readWordsFromInput() {
        List<String> wordsList = new ArrayList<>();
        Scanner scanner = new Scanner(System.in, "UTF-8");
        System.out.println("Pergunte: ");
        String input = scanner.nextLine(); // Lê a linha digitada pelo usuário
        String[] words = input.split(" "); // Divide a linha em palavras
        for (String word : words) {
            String wordSemAcento = removerAcentos(word.toLowerCase()); // Remove acento e converte para minúsculas
            wordsList.add(wordSemAcento); // Adiciona a palavra à lista
        }
        System.out.println(); // Quebra de linha

        return wordsList;
    }



}
