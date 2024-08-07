package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class Utils {

    public static List<String> getStringList(String text, String regex) {
        String[] strArray = text.split(regex);
        List<String> list = asList(strArray);
        List<String> returnList = new ArrayList<>(list);
        returnList.removeIf(String::isEmpty);

        return returnList.stream()
                .map(Utils::validWord)
                .collect(Collectors.toList());
    }

    public static List<String> processWords(List<String> words) {
        return words.stream()
                .map(Utils::validWord)
                .collect(Collectors.toList());
    }

    private static String validWord(String word) {
        return word
                .trim()
                .toLowerCase()
                .replace("?", "")
                .replace("!", "")
                .replace(",", "")
                .replace(".", "")
                .replace("\n", "");
    }

}
