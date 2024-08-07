package analyzer.impl;
import analyzer.Analyzer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.text.Normalizer;

public class AlphabetAnalyzer implements Analyzer {

    private final Pattern regex = Pattern.compile("[a-zA-Zá-ú-Á-Ú0-9_@.\\/#?!&+-ç]*");

    @Override
    public List<String> analyze(List<String> words) {
        List<String> invalidWords = new ArrayList<>();
        List<String> validWords = new ArrayList<>();
        words.forEach(word -> {
            if (!word.trim().isEmpty() && word.matches(regex.pattern())) {
                validWords.add(word); // Adiciona a palavra à lista de palavras válidas se não estiver vazia e corresponder ao padrão
            } else{
                invalidWords.add(word); 
            }
        });
        return validWords;
    }

    public String[] format(String text) {
        text = text.toLowerCase();
        text = text.replaceAll("\\p{Punct}", "");
        text = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        String[] words = text.trim().split("[,.!?'@_] *| +");
        return words;
    }



}
