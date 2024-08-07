package analyzer.impl;

import analyzer.impl.*;

import java.io.*;
import java.util.*;
import utils.Utils;

public class InvertedIndex {

    private StopWordsAnalyzer stopWordsAnalyzer;
    private AlphabetAnalyzer alphabetAnalyzer;
    private KeywordAnalyzer keywordsAnalyzer;
    private TokenAnalyzer tokenAnalyzer;

    private Map<String, List<Index>> invertedMap;
    private Map<String, String> tabelaDeSimbolos; // Tabela de símbolos
    private Map<Integer, String> answers = new HashMap<>();

    public InvertedIndex(Map<String, String> tabelaDeSimbolos) throws FileNotFoundException {
        this.invertedMap = new HashMap<>();
        this.stopWordsAnalyzer = new StopWordsAnalyzer();
        this.alphabetAnalyzer = new AlphabetAnalyzer();
        this.tabelaDeSimbolos = tabelaDeSimbolos;
        this.keywordsAnalyzer = new KeywordAnalyzer();
        this.tokenAnalyzer = new TokenAnalyzer();
    }

    // Indexar um conjunto de respostas-padrão
    public void indexarRespostasPadrao(String respostasDiretorio) throws IOException {
        File dir = new File(respostasDiretorio);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null) return;

        int position = 0;
        for (File file : files) {
            Scanner scnFile = new Scanner(file);
            while (scnFile.hasNextLine()) {
                String text = scnFile.nextLine();
                String[] words = alphabetAnalyzer.format(text);

                for (String word : words) {
                    if (stopWordsAnalyzer.analyzeWord(word) == null) {
                        if (keywordsAnalyzer.analyzeWord(word) != null) {
                            //System.out.println(word);
                            tokenAnalyzer.analyzeWord((keywordsAnalyzer.analyzeWord(word)));
                        }
                        for (String key : tokenAnalyzer.getTokens()) {
                            addKey(key, position);
                        }
                        tokenAnalyzer.getTokens().clear();
                    }
                }
                answers.put(position, text); // Adiciona resposta ao mapa de respostas
                position++;
            }
            scnFile.close();
        }
        dataWriter(); // Grava o índice invertido em arquivo
    }

    // Método para confrontar tabela de símbolos com arquivo invertido usando TF-IDF
    public String tfidf(Map<String, String> queryTabelaDeSimbolos) {
        List<String> tokens = new ArrayList<>();
        // Adicionar a chave na lista
        tokens.addAll(queryTabelaDeSimbolos.keySet());
        System.out.println(tokens);
        List<Integer> usages = new LinkedList<>(Collections.nCopies(answers.size(), 0));
        for (String token : tokens) {
            if (invertedMap.get(token) != null) {
                for (Index ind : invertedMap.get(token)) {
                    usages.set(ind.getIndex(), usages.get(ind.getIndex()) + ind.getUsage());
                }
            }
        }
        int position = 0;
        for (int i = 0; i < usages.size() - 1; i++) {
            if (usages.get(position) < usages.get(i + 1)) {
                position = i + 1;
            } else if (Objects.equals(usages.get(position), usages.get(i + 1))) {
                Random random = new Random();
                int ran = random.nextInt(2);
                if (ran != 1) position = i + 1;
            }
        }
        return answers.get(position);
    }

    // Adiciona palavra ao índice invertido
    private void addKey(String word, int index) {
        invertedMap.computeIfAbsent(word, k -> new ArrayList<>()).add(new Index(index, 1));
    }

    // Grava o índice invertido em arquivo
    private void dataWriter() throws IOException {
        FileWriter fw = new FileWriter("src/files/invertedIndex.txt");
        PrintWriter printWriter = new PrintWriter(fw);
        invertedMap.forEach((key, value) -> {
            value.sort(Comparator.naturalOrder());
            printWriter.println(key + " " + value);
        });
        printWriter.close();
    }

    // Carrega o índice invertido de arquivo
}

// Classe auxiliar Index
class Index implements Comparable<Index> {
    private final int index;
    private int usage;

    public Index(int index, int usage) {
        this.index = index;
        this.usage = usage;
    }

    public int getIndex() {
        return index;
    }

    public int getUsage() {
        return usage;
    }

    public void incrementUsage() {
        usage++;
    }

    @Override
    public int compareTo(Index other) {
        return Integer.compare(this.index, other.index);
    }

    @Override
    public String toString() {
        return index + ":" + usage;
    }
}
