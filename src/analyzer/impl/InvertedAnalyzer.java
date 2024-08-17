package analyzer.impl;

import java.io.*;
import java.util.*;

public class InvertedAnalyzer {

    private StopWordsAnalyzer stopWordsAnalyzer;
    private AlphabetAnalyzer alphabetAnalyzer;
    private KeywordAnalyzer keywordsAnalyzer;
    private TokenAnalyzer tokenAnalyzer;

    private Map<String, List<Index>> invertedMap;
    private Map<String, String> tabelaDeSimbolos; // Tabela de símbolos
    private Map<Integer, String> answers = new HashMap<>();

    public InvertedAnalyzer(Map<String, String> tabelaDeSimbolos) throws IOException {
        this.invertedMap = new HashMap<>();
        this.stopWordsAnalyzer = new StopWordsAnalyzer();
        this.alphabetAnalyzer = new AlphabetAnalyzer();
        this.tabelaDeSimbolos = tabelaDeSimbolos;
        this.keywordsAnalyzer = new KeywordAnalyzer();
        this.tokenAnalyzer = new TokenAnalyzer();
    }
    private static final String COUNT_FILE = "src/files/previousFileCount.txt";
    private int previousFileCount = loadPreviousFileCount();
    // Indexar um conjunto de respostas-padrão
    public void indexarRespostasPadrao(String respostasDiretorio) throws IOException {
        File dir = new File(respostasDiretorio);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null) return;

        // Verifica se o número de arquivos mudou para atualizar apenas quando o conjunto de respostas-padrão mudar
        if (files.length == previousFileCount) {
            // Se o número de arquivos não mudou, apenas carregar o índice invertido
            System.out.println("conjunto de respostas-padrão eh o mesmo");
            loadInvertedIndex();
            //System.out.println(invertedMap);
            return;
        }

        // Atualiza a contagem de arquivos processados
        previousFileCount = files.length;
        savePreviousFileCount(previousFileCount); // Salva a contagem de arquivos

        // Atualiza o arquivo invertido
        int position = 0;
        for (File file : files) {
            Scanner scnFile = new Scanner(file);
            while (scnFile.hasNextLine()) {
                String text = scnFile.nextLine();
                String[] words = alphabetAnalyzer.format(text);

                for (String word : words) {
                    if (stopWordsAnalyzer.analyzeWord(word) == null) {
                        if (keywordsAnalyzer.analyzeWord(word) != null) {
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

    // Carrega a contagem de arquivos processados anteriormente
    private int loadPreviousFileCount() throws IOException {
        File countFile = new File(COUNT_FILE);

        try (BufferedReader reader = new BufferedReader(new FileReader(countFile))) {
            String line = reader.readLine();
            return line != null ? Integer.parseInt(line) : -1;
        }
    }

    // Salva a contagem de arquivos processados em um arquivo de texto
    private void savePreviousFileCount(int count) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(COUNT_FILE))) {
            writer.println(count);
        }
    }

    // Carrega o índice invertido de arquivo
    private void loadInvertedIndex() throws IOException {
        invertedMap.clear();
        File dir = new File("src/files/answers");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null) return;

        int position = 0;
        for (File file : files) {
            Scanner scnFile = new Scanner(file);
            while (scnFile.hasNextLine()) {
                String text = scnFile.nextLine();
                String[] words = alphabetAnalyzer.format(text);
                answers.put(position, text); // Adiciona resposta ao mapa de respostas
                position++;
            }
            scnFile.close();
        }
        //System.out.println(answers);

        try (BufferedReader br = new BufferedReader(new FileReader("src/files/invertedIndex.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(line);
            }
        }

    }

    private void processLine(String line) {
        String[] parts = line.split(" ", 2);
        String word = parts[0];
        String indicesPart = parts[1].trim().replaceAll("[\\[\\]]", ""); // Remove colchetes

        List<Index> indices = new ArrayList<>();
        String[] indexPairs = indicesPart.split(", ");
        for (String pair : indexPairs) {
            String[] positionCount = pair.split(":");
            int position = Integer.parseInt(positionCount[0]);
            int count = Integer.parseInt(positionCount[1]);
            indices.add(new Index(position, count));
        }

        invertedMap.put(word, indices);
    }



    // Método para confrontar tabela de símbolos com arquivo invertido usando TF-IDF
    public String tfidf(Map<String, String> queryTabelaDeSimbolos) {
        List<String> tokens = new ArrayList<>(queryTabelaDeSimbolos.keySet());
        System.out.println(tokens);

        List<Integer> usages = new LinkedList<>(Collections.nCopies(answers.size(), 0));
        List<Set<String>> uniqueTokenSets = new ArrayList<>(answers.size());

        // Inicializando a lista de conjuntos para armazenar tokens únicos por resposta
        for (int i = 0; i < answers.size(); i++) {
            uniqueTokenSets.add(new HashSet<>());
        }

        // Atualizar a lista de usos e armazenar os tokens únicos encontrados por resposta
        for (String token : tokens) {
            if (invertedMap.get(token) != null) {
                for (Index ind : invertedMap.get(token)) {
                    usages.set(ind.getIndex(), usages.get(ind.getIndex()) + ind.getUsage());
                    uniqueTokenSets.get(ind.getIndex()).add(token); // Armazena tokens únicos
                }
            }
        }

        int position = 0;

        // Selecionar com base na variação de palavras (número de tokens únicos)
        for (int i = 0; i < uniqueTokenSets.size(); i++) {
            if (uniqueTokenSets.get(position).size() < uniqueTokenSets.get(i).size()) {
                position = i;
            } else if (uniqueTokenSets.get(position).size() == uniqueTokenSets.get(i).size()) {
                // Critério de desempate baseado no valor de uso
                if (usages.get(position) < usages.get(i)) {
                    position = i;
                } else if (Objects.equals(usages.get(position), usages.get(i))) {
                    // Critério de desempate aleatório em caso de igualdade de variação e uso
                    Random random = new Random();
                    int ran = random.nextInt(2);
                    if (ran != 1) position = i;
                }
            }
        }

        return answers.get(position);
    }



    // Adiciona palavra ao índice invertido
    public void addKey(String word, int index){
        if (invertedMap.containsKey(word)){
            for (Index key: invertedMap.get(word)) {
                if(key.getIndex() == index) {
                    key.incrementUsage();
                    return;
                }
            }
            invertedMap.get(word).add(new Index(index,1));

        } else {
            Index key = new Index(index,1);
            List<Index> list = new ArrayList<>();
            list.add(key);
            invertedMap.put(word,list);
        }
    }

    // Grava o índice invertido em arquivo
    private void dataWriter() throws IOException {
        FileWriter fw = new FileWriter("src/files/invertedIndex.txt");
        PrintWriter printWriter = new PrintWriter(fw);
        invertedMap.forEach((key, value) -> {
            value.stream().sorted();
            printWriter.println(key + " " + value.toString());
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
