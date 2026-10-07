package org.example;

import java.util.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.println("Hello and welcome!");
        List<String[]> conversionList = new ArrayList<>();
        conversionList.add(new String[]{"USD","JPY","0.86"});
        conversionList.add(new String[]{"JPY","INR","1.86"});
        conversionList.add(new String[]{"INR","USD","2.86"});
        conversionList.add(new String[]{"USD","JPY","5.86"});
        conversionList.add(new String[]{"JPY","AUD","11.86"});
        conversionList.add(new String[]{"AUD","NZD","22.86"});

        Map<String, Map<String,Double>> result = createMap(conversionList, true);
        List<String> resultPath = pathFromSourceToDest("USD","INR",result);
        double bestRate = bestPathFromSourceToDest("USD", "INR", result);
        System.out.println("Result 1: " + result);
        System.out.println("Path :" + resultPath);
        System.out.println("Best Path :" + bestRate);
    }
    /*Build a graph from an edge list (15 min). Write a method that turns List<String[]> rows like ["USD","JPY","0.86"] into Map<String, Map<String, Double>>. Add the reverse edge with rate 1/rate. Write it twice from memory until it feels automatic.*/

    public static Map<String, Map<String, Double>> createMap(List<String[]> conversionList, Boolean allowReverse){
        Map<String, Map<String, Double>> map = new HashMap<>();
        for (String[] strings: conversionList){
            String first = strings[0];
            String second = strings[1];
            double rate = Double.parseDouble(strings[2]);
            if (!map.containsKey(first)) map.put(first, new HashMap<>());
            map.get(first).merge(second, rate, Math::max);
            if (!map.containsKey(second)) map.put(second, new HashMap<>());
            if (allowReverse) map.get(second).merge(first, 1/rate, Math::max);
        }
        return  map;
    }
    /*"is there any path from source to target?"*/
    public static List<String> pathFromSourceToDest(String src, String dest, Map<String, Map<String, Double>> map){
        List<String> path = new ArrayList<>();
        HashSet<String> visited = new HashSet<>();
        dfs(src, dest, map, visited, path);
        return path;
    }
    public static double bestPathFromSourceToDest(String src, String dest, Map<String, Map<String, Double>> map){
        HashSet<String> visited = new HashSet<>();
        return dfs(src, dest, map, visited, 1);
    }
    public static boolean dfs(String node, String dest, Map<String, Map<String, Double>> map, HashSet<String> visited, List<String> path){
        path.add(node);
        if (node.equals(dest)) return true;
        visited.add(node);
        for (String neigh: map.get(node).keySet()){
            if (!visited.contains(neigh) && dfs(neigh, dest, map, visited, path)) return true;
        }
        return false;
    }

    public static double dfs(String node, String dest, Map<String, Map<String, Double>> map, HashSet<String> visited, double product){
        if (node.equals(dest)) return product;
        visited.add(node);
        double best = -1;
        for (String neigh: map.get(node).keySet()){
            if (!visited.contains(neigh)){
                double rate = map.get(node).get(neigh);
                best = Math.max(best, dfs(neigh, dest, map, visited, rate*product));
            }
        }
        return best;
    }
}