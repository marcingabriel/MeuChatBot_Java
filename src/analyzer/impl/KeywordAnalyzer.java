package analyzer.impl;
import analyzer.Analyzer;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class KeywordAnalyzer implements Analyzer {

    private List<String> keywords;

    public KeywordAnalyzer() {
        try {
            init();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void init() throws FileNotFoundException {
        File f = new File("src/files/keywords_ptbr.txt");
        Scanner s = new Scanner(f);
        keywords = new ArrayList<>();
        while (s.hasNext()) {
            keywords.add(s.nextLine().toLowerCase());
        }
    }

    @Override
    public List<String> analyze(List<String> words) {
        List<String> nonKeywords = new ArrayList<>();
        List<String> foundKeywords = new ArrayList<>();

        

        for (String word : words) {
            if (keywords.contains(word)) {
                foundKeywords.add(word);
            } else {
                nonKeywords.add(word);
            }
        }

     
        return nonKeywords;
    }


    public String analyzeWord(String word) {
        return isSimilar(word, this.keywords) ? word : null;
    }


    private boolean isSimilar(String palavra, List<String> set) {
        for (String palavraDoSet : set) {
            if (SimilarityAnalyzer.SimilarityCalculator(palavra.toLowerCase(), palavraDoSet.toLowerCase()) <= 2) {
                return true;
            }
        }
        return false;
    }

}

