package analyzer.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import utils.Utils;

public class SyntaxAnalyzer {
    private static final Set<String> finalidades = new HashSet<>(Arrays.asList("tocar","gravar", "ensaiar", "performar", "performances", "compor", "estudar", "começar", "aprender", "praticar", "ao vivo", "estúdio", "iniciante", "profissional", "profissionalmente", "para o ano novo", "para o natal", "tocar rock", "natal", "rock", "presentar", "jazz", "clássico", "pop"));
    private static final Set<String> acoes = new HashSet<>(Arrays.asList("afinar", "ajustar", "configurar", "limpar", "conectar", "conecta", "testar", "consertar", "comprar", "vender", "ligar", "desligar", "lubrificar", "desmontar"));
    private static final Set<String> produtos = new HashSet<>(Arrays.asList("guitarra", "violão", "piano", "teclado", "bateria", "baixo", "saxofone", "trompete", "flauta", "violino", "microfone", "amplificador", "mixer","fone", "ukulele"));
    private static final Set<String> locais = new HashSet<>(Arrays.asList("em um piano padrão", "em uma guitarra"));
    private static final Set<String> verbos = new HashSet<>(Arrays.asList("está", "é", "estão", "são"));
    private static final Set<String> valores = new HashSet<>(Arrays.asList("preço", "valor", "custa"));
    private static final Set<String> problemas = new HashSet<>(Arrays.asList("desafinado", "quebrado", "sem som", "ruído", "mal contato", "rachado", "não liga", "não conecta", "quebrou", "não responde", "defeito", "parou", "pesado", "difícil", "pesada","chiado"));
    private static final Set<String> caracteristicas = new HashSet<>(Arrays.asList("acústico", "elétrico", "clássico", "moderno", "profissional", "iniciante","compacto","portátil"));
    private static final Set<String> fabricantes = new HashSet<>(Arrays.asList("fender", "gibson", "yamaha", "roland", "korg", "shure", "sennheiser", "behringer", "boss", "marshall", "peavey", "pearl"));
    private static final String NEGACAO = "não";

    public boolean analyzePhrase(List<String> palavrasValidas, Map<String, String> tabelaDeSimbolos) throws IOException {
        boolean resultado = false;
        InvertedAnalyzer invertedIndex = new InvertedAnalyzer(tabelaDeSimbolos);
        invertedIndex.indexarRespostasPadrao("src/files/answers");
        AlphabetAnalyzer alphabetAnalyzer = new AlphabetAnalyzer();
        StopWordsAnalyzer stopWordsAnalyzer = new StopWordsAnalyzer() ;

        while (!resultado) {
            if(palavrasValidas.getFirst().equals("0") || palavrasValidas.getFirst().equals("exit") ){
                resultado = true;
            }

            if (processGrammar(palavrasValidas, tabelaDeSimbolos)) {
                String resposta = invertedIndex.tfidf(tabelaDeSimbolos);
                System.out.println("Resposta: " + resposta);
                System.out.print("Fazer outra pergunta: ");
                palavrasValidas = readWordsFromInput();
                palavrasValidas =  alphabetAnalyzer.analyze(palavrasValidas);
                palavrasValidas =  Utils.processWords(palavrasValidas);
                palavrasValidas = stopWordsAnalyzer.analyze(palavrasValidas);
                resultado = false;

            } else {
                //System.out.print("Digite 0 ou exit para sair a qualquer momento: ");
                System.out.println("Não entendi.");
                System.out.print("Por favor, reescreva a frase: ");
                palavrasValidas = readWordsFromInput();
                palavrasValidas =  alphabetAnalyzer.analyze(palavrasValidas);
                palavrasValidas =  Utils.processWords(palavrasValidas);
                palavrasValidas = stopWordsAnalyzer.analyze(palavrasValidas);
                resultado = false;
            }
        }
        // Imprime a tabela de símbolos
        System.out.println("\nTabela de Símbolos:");
        for (Map.Entry<String, String> entry : tabelaDeSimbolos.entrySet()) {
            System.out.println(entry.getValue() + ": " + entry.getKey());
        }
        System.out.println("");

        return resultado;
    }

    private boolean processGrammar(List<String> palavras, Map<String, String> tabelaDeSimbolos) {
        // Define as regras gramaticais como transições de um autômato finito
        String[][] regras = {
                {"produto", "finalidade", "finalidade"}, //<produto> <finalidade> <finalidade>
                {"produto", "finalidade"}, //<produto> <finalidade>
                {"produto", "problema"}, //.
                {"produto", "verbo", "problema"}, //.
                {"produto", "fabricante", "problema"}, //.
                {"produto", "fabricante", "finalidade"},  //<produto> <fabricante> <finalidade>
                {"produto", "fabricante", "verbo", "problema"},
                {"produto", "fabricante", "característica"}, //<produto> <fabricante> <caracteristica>
                {"valor", "produto", "fabricante"}, //<valor> <produto> <fabricante>
                {"ação", "produto"}, //<acao> <produto>
                {"ação", "produto", "fabricante"}, //<acao> <produto> <fabricante>
                {"ação", "produto", "problema"}, //.
                {"ação", "produto", "finalidade"}, //.
                {"ação", "produto", "produto"},  //<ação> <produto> <produto>
                {"ação", "produto", "verbo","problema"}, //<ação> <produto> <verbo> <problema>
                {"ação", "produto", "não", "ação"}, //<ação> <produto> <não> <ação>
                {"produto", "não", "ação"}, //<produto> <não> <ação>
                {"produto", "característica"}, //.
                {"finalidade"} //.
        };

        List<String> tags = new ArrayList<>();
        for (String palavra : palavras) {
            String tag = classifyWord(palavra);
            tags.add(tag);
            //System.out.println("Palavra: " + palavra + " | Categoria: " + tag); // Depuração
        }

        // Tenta encontrar uma correspondência exata
        for (String[] regra : regras) {
            if (matchExactRule(tags, regra)) {
                anotarTabela(palavras, tags, tabelaDeSimbolos);
                //System.out.println("A frase segue a regra exata: <" + String.join("> <", regra) + ">.");
                return true;
            }
        }

        // Se nenhuma correspondência exata for encontrada, tenta encontrar a melhor correspondência com palavras extras
        String[] melhorRegra = null;
        int menorNumeroDePalavrasExtras = Integer.MAX_VALUE;

        Scanner scanner = new Scanner(System.in);
        for (String[] regra : regras) {
            int numeroDePalavrasExtras = matchRuleWithExtras(tags, regra);
            if (numeroDePalavrasExtras != -1 && numeroDePalavrasExtras < menorNumeroDePalavrasExtras) {
                menorNumeroDePalavrasExtras = numeroDePalavrasExtras;
                melhorRegra = regra;
            }
        }

        //preenchimento partes faltantes
        if (palavras.size() == 2 && isSimilar(palavras.get(0), produtos) && isSimilar(palavras.get(1), verbos) ) {
            System.out.print("Qual o problema do produto? ");
            String problema = scanner.nextLine();
            palavras.add(problema);
            if (processGrammar(palavras,tabelaDeSimbolos)) {
                return true;
            }
            return false; // Chamar novamente a análise sintática
        }
        else if (palavras.size() == 2 && isSimilar(palavras.get(0), produtos) && isSimilar(palavras.get(1), fabricantes)) { // produto fabricante problema ou caracteristica
            System.out.print("Qual o problema ou caracteristica do seu produto? Ex: fender,gibson,yamaha,acustico,eletrico...  ");
            String word1 = scanner.nextLine();
            palavras.add(word1);
            if (processGrammar(palavras,tabelaDeSimbolos)) {
                return true;
            }
            return false;

        } else if (palavras.size() == 2 && isSimilar(palavras.get(0), produtos) &&  ("não".equals(palavras.get(1))||"nao".equals(palavras.get(1))) ) {  //Preenchimento 3: <produto> <nao> <acao>
            System.out.print("O produto nao realiza oque?  ex: conecta,liga,desliga,configura...  ");
            String acao = scanner.nextLine();
            palavras.add(acao);
            if (processGrammar(palavras,tabelaDeSimbolos)) {
                return true;
            }
            return false;

        }else if (palavras.size() == 3 && isSimilar(palavras.get(0), acoes)  && isSimilar(palavras.get(1), produtos) &&  ("não".equals(palavras.get(1))||"nao".equals(palavras.get(2))) ) {  //Preenchimento 3: <acao> <produto> <nao>
            System.out.print("Voce nao quer oque?  ex: afinar,ajustar,configurar,limpar,conectar,testar...  ");
            String produto = scanner.nextLine();
            palavras.add(produto);
            if (processGrammar(palavras,tabelaDeSimbolos)) {
                return true;
            }
            return false;
        } else if (palavras.size() == 1 && isSimilar(palavras.get(0), produtos)) {        // Preenchimento : <produto> <finalidade> ou produto caracteristica
            System.out.print("Qual a finalidade,caracteristica ou problema do produto? Ex: compor,gravar,praticar,eletrico,acustico... ");
            String word1 = scanner.nextLine();
            palavras.add(word1);
            if (processGrammar(palavras,tabelaDeSimbolos)) {
                return true;
            }
            return false;

        } else if (palavras.size() == 1 && isSimilar(palavras.get(0), acoes)) {  //Preenchimento 2: <ação> <produto>
            System.out.print("Sobre qual produto você deseja saber? Ex: piano,teclado,bateria... ");
            String produto = scanner.nextLine();
            palavras.add(produto);
            if (processGrammar(palavras,tabelaDeSimbolos)) {
                return true;
            }
            return false;

        }else if (palavras.size() == 1 && isSimilar(palavras.get(0), valores)) {  //Preenchimento 2: <valor>
            System.out.print("Sobre qual produto você deseja saber o valor? Ex: piano,teclado,bateria... ");
            String produto = scanner.nextLine();
            palavras.add(produto);
            if (processGrammar(palavras,tabelaDeSimbolos)) {
                return true;
            }
            return false;
        }else if (palavras.size() == 2 && isSimilar(palavras.get(0), valores) && isSimilar(palavras.get(1), produtos)) {  //Preenchimento 2: <valor> <produto>
            System.out.print("Sobre qual  fabricante do produto você deseja saber o valor? Ex: fender | gibson | yamaha... ");
            String fabricante = scanner.nextLine();
            palavras.add(fabricante);
            if (processGrammar(palavras,tabelaDeSimbolos)) {
                return true;
            }
            return false;
        }



        if (melhorRegra != null) {
            anotarTabela(palavras, tags, tabelaDeSimbolos);
            System.out.println("A frase segue a regra com palavras extras: <" + String.join("> <", melhorRegra) + ">.");
            return true;
        }

        return false;
    }

    private String classifyWord(String palavra) {
        if (SimilarityAnalyzer.SimilarityCalculator(palavra.toLowerCase(), NEGACAO) <= 2) return "não";
        else if (isSimilar(palavra, finalidades)) return "finalidade";
        else if (isSimilar(palavra, acoes) && !isSimilar(palavra, verbos)) return "ação";
        else if (isSimilar(palavra, produtos)) return "produto";
        else if (isSimilar(palavra, locais)) return "local";
        else if (isSimilar(palavra, verbos)) return "verbo";
        else if (isSimilar(palavra, problemas)) return "problema";
        else if (isSimilar(palavra, caracteristicas)) return "característica";
        else if (isSimilar(palavra, fabricantes)) return "fabricante";
        else if (isSimilar(palavra, valores)) return "valor";
        else return "desconhecido"; // Categoria padrão para palavras desconhecidas
    }

    private boolean matchExactRule(List<String> tags, String[] regra) {
        return tags.equals(Arrays.asList(regra));
    }

    private int matchRuleWithExtras(List<String> tags, String[] regra) {
        int regraIndex = 0;
        int extraWordsCount = 0;

        for (String tag : tags) {
            if (regraIndex < regra.length && tag.equals(regra[regraIndex])) {
                regraIndex++;
            } else {
                extraWordsCount++;
            }
        }

        // Retorna o número de palavras extras se todos os elementos da regra foram encontrados na ordem correta
        return (regraIndex == regra.length) ? extraWordsCount : -1;
    }


    private void anotarTabela(List<String> palavras, List<String> tags, Map<String, String> tabelaDeSimbolos) {
        // Adiciona todas as palavras e suas categorias na tabela de símbolos
        for (int i = 0; i < palavras.size(); i++) {
            if (!palavras.get(i).equals("não"))tabelaDeSimbolos.put(palavras.get(i), tags.get(i));
        }
    }

    private boolean isSimilar(String palavra, Set<String> set) {
        for (String palavraDoSet : set) {
            if (SimilarityAnalyzer.SimilarityCalculator(palavra.toLowerCase(), palavraDoSet.toLowerCase()) <= 2) {
                return true;
            }
        }
        return false;
    }

    private List<String> readWordsFromInput() {
        List<String> wordsList = new ArrayList<>();
        Scanner scanner = new Scanner(System.in, "UTF-8");

        String input = scanner.nextLine(); // Lê a linha digitada pelo usuário
        String[] words = input.split(" "); // Divide a linha em palavras
        for (String word : words) {
            //     System.out.print(word + " "); // Imprime a palavra com um espaço
            wordsList.add(word.toLowerCase()); // Adiciona a palavra em minúscula à lista
        }
        System.out.println(); // Quebra de linha

        return wordsList;
    }

}
