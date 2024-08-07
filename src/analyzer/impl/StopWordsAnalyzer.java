package analyzer.impl;

import analyzer.Analyzer;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import utils.Utils;

public class StopWordsAnalyzer implements Analyzer {

    private List<String> stopWords;

    public StopWordsAnalyzer() throws FileNotFoundException {
        init();
    }

    private void init() throws FileNotFoundException {
        //System.out.println("Current working directory: " + System.getProperty("user.dir"));
        File f = new File("src/files/stopwords_ptbr.txt");
        Scanner s = new Scanner(f);
        StringBuilder stopWordsString = new StringBuilder(" ");
        while (s.hasNext()) {
            stopWordsString.append(s.nextLine().toLowerCase()).append("\n");
        }
        this.stopWords = Utils.getStringList(stopWordsString.toString(), "\n");
    }

    @Override
    public List<String> analyze(List<String> words) {
        List<String> toRemoveList = new ArrayList<>();
        words.forEach(word -> {
            if(this.stopWords.contains(word)) {
                toRemoveList.add(word);
            }
        });
        words.removeAll(toRemoveList);
    
        
       // System.out.print(" Stopwords:");
        //for (String remove : toRemoveList) {
        //System.out.print("[" + remove + "] "); 
        //}
       // System.out.println(" ");

       
        return words;

       
    }

    public String analyzeWord(String word) {
        return this.stopWords.contains(word) ? word : null;
    }
}
