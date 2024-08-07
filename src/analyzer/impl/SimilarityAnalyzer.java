package analyzer.impl;

public class SimilarityAnalyzer {
    public static int SimilarityCalculator(String s1, String s2) {
 
        // Garantindo que duas strings são passadas como parâmetro
        if (s1 == null || s2 == null) {
            throw new IllegalArgumentException("As strings não podem ser nulas.");
        }

        // Verificando se as strings são vazias
        if (s1.isEmpty()) {
            return s2.length();
        }
        if (s2.isEmpty()) {
            return s1.length();
        }

        if (s1.length() < s2.length()) {
            return SimilarityCalculator(s2, s1);
        }

        int[] previousRow = new int[s2.length() + 1];
        for (int i = 0; i < previousRow.length; i++) {
            previousRow[i] = i;
        }

        for (int i = 0; i < s1.length(); i++) {
            int[] currentRow = new int[s2.length() + 1];
            currentRow[0] = i + 1;
            for (int j = 0; j < s2.length(); j++) {
                int cost = (s1.charAt(i) == s2.charAt(j)) ? 0 : 1;
                int insertions = previousRow[j + 1] + 1;
                int deletions = currentRow[j] + 1;
                int substitutions = previousRow[j] + cost;
                currentRow[j + 1] = Math.min(Math.min(insertions, deletions), substitutions);
            }
            previousRow = currentRow;
        }

        return previousRow[s2.length()];
    }





}
