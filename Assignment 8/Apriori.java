// Apriori.java CS6051/EECS6010 Cheng 2017
// the apriori algorithm, applied to itemsets generated with XQuery on medline citation XML 
// Input format: first line ignored.  Each line is the MeshHeadings to a medline citation.
// MeshHeading/DescriptorNames separated with the semicolon ;
// Compile: javac -Xlint:unchecked Apriori.java and ignore four warnings
// Usage: java XQueryOut 0.01 (or other min support)

import java.io.*;
import java.util.*;

public class Apriori{

   int numberOfItems = 0;
   int numberOfTransactions = 0;
   boolean[][] data = null;
   String[] items = null;
   int[] counts = null;
   int threshold = 0;
   HashSet<HashSet<Integer>> L = new HashSet<HashSet<Integer>>();
   HashSet<HashSet<Integer>> L2 = new HashSet<HashSet<Integer>>();


  void readTransactions(String filename){
    Scanner in = null;
    try {
       in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
       System.err.println(filename + " not found");
       System.exit(1);
    }
    in.nextLine();
    TreeSet<String> tset = new TreeSet<String>();
    while (in.hasNextLine()){
      String[] terms = in.nextLine().split(";");
      for (String s: terms) tset.add(s);
      numberOfTransactions++;
    }
    in.close();

    numberOfItems = tset.size();
    items = new String[numberOfItems];
    int n = 0;
    for (String s: tset) items[n++] = s;
    data = new boolean[numberOfTransactions][numberOfItems];
    try {
       in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
       System.err.println(filename + " not found");
       System.exit(1);
    }
    in.nextLine();
    for (int i = 0; i < numberOfTransactions; i++){
      for (int j = 0; j < numberOfItems; j++) data[i][j] = false;
      String[] terms = in.nextLine().split(";");
      for (String s: terms){ 
        int k = 0; for (; k < numberOfItems; k++) if (s.equals(items[k])) break;
        if (k < numberOfItems) data[i][k] = true;
      }
    }
    in.close();
  }

  void countSupport(double minSupport){
    threshold = (int)(Math.ceil(minSupport * numberOfTransactions));    
    counts = new int[numberOfItems];
    for (int j = 0; j < numberOfItems; j++){
      counts[j] = 0;
      for (int i = 0; i < numberOfTransactions; i++) if (data[i][j]) counts[j]++;
    }
    for (int j = 0; j < numberOfItems; j++) if (counts[j] >= threshold) {
      HashSet<Integer> subset = new HashSet<Integer>();
      subset.add(j);
      L.add(subset);
    }     
  }

  boolean pairing(int k){
    L2.clear();
    int size = L.size();
    Object[] itemsets = L.toArray();
    for (int i = 0; i < size; i++)
      for (int j = i + 1; j < size; j++){
         int intersectionSize = 0;
         for (Integer x: (Collection<Integer>)(itemsets[j])) if (((Collection<Integer>)(itemsets[i])).contains(x)) intersectionSize++;
         if (intersectionSize == k - 1){
            HashSet<Integer> union = new HashSet<Integer>((Collection<Integer>)(itemsets[i]));
            union.addAll((Collection<Integer>)(itemsets[j]));
            int support = 0;
            for (int p = 0; p < numberOfTransactions; p++){
               boolean allIn = true;
               for (int y: union) if (!data[p][y]){ allIn = false; break; }
               if (allIn) support++;
            }
            if (support >= threshold) L2.add(union);
         }
     }
     return L2.size() > 0;
   }

  void apriori(){
    if (L.size() > 0){
       listing();  int k = 1;
       while (pairing(k++)){
         L.clear(); L.addAll(L2);
         listing();
       }
    }
  }

  void listing(){
    for (Set<Integer> s: L){
      for (int i: s) System.out.print(items[i] + ";");
      System.out.println();
    }
   }
    

  public static void main(String[] args){
    if (args.length < 2){
      System.err.println("Usage: java Apriori transactions mins");
      System.exit(1);
    }
    Apriori a = new Apriori();
    a.readTransactions(args[0]);
    a.countSupport(Double.parseDouble(args[1]));
    a.apriori();
  }
}

