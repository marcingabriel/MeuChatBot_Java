package analyzer.impl;

import analyzer.Analyzer;
import java.util.ArrayList;
import java.util.List;

public class TokenAnalyzer implements Analyzer {

    private List<String> tokens;

    public TokenAnalyzer() {
        tokens = new ArrayList<>();
    }

    @Override
    public List<String> analyze(List<String> words) {
        return null;
    }

    public String analyzeWord(String word) {
        if(!tokens.contains(word)) {
            tokens.add(word);
        }
        return null;
    }

    public List<String> getTokens() {
        return tokens;
    }
}