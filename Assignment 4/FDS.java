// FDS.java CS6051/EECE6010 2017 Cheng
// Algorithm 3.7 closure of X under F
// Usage: java FDS F X
// F is a file that has the first line all the attributes and 
// then an FD a line with a space between the left-hand side and the right-hand side
// X is a string of characters represent a set of attributes

import java.io.*;
import java.util.*;

class FD{
  HashSet<Character> lhs; char rhs;
  public FD(HashSet<Character> l, char r){ lhs = l; rhs = r; }
  public boolean equals(Object obj){
    FD fd2 = (FD)obj;
    return lhs.equals(fd2.lhs) && rhs == fd2.rhs;
  }
};

public class FDS{
  HashSet<Character> R = new HashSet<Character>(); // all attributes
  HashSet<FD> F = new HashSet<FD>(); // the set of FDs

  public FDS(String filename){  // 1. split FDs so each FD has a single attribute on the right
    Scanner in = null;
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
       System.err.println(filename + " not found");
       System.exit(1);
    }
    String line = in.nextLine();
    for (int i = 0; i < line.length(); i++) R.add(line.charAt(i));
    while (in.hasNextLine()){
      HashSet<Character> l = new HashSet<Character>();
      String[] terms = in.nextLine().split(" ");
      for (int i = 0; i < terms[0].length(); i++) l.add(terms[0].charAt(i));
      for (int i = 0; i < terms[1].length(); i++) F.add(new FD(l, terms[1].charAt(i)));
    }
    in.close();
  }

  HashSet<Character> string2set(String X){
    HashSet<Character> Y = new HashSet<Character>();
    for (int i = 0; i < X.length(); i++) Y.add(X.charAt(i));
    return Y;
  }

  void printSet(Set<Character> X){
    for (char c: X) System.out.print(c);
    System.out.println();
  }

  HashSet<Character> closure(HashSet<Character> X){ // Algorithm 3.7
    HashSet<Character> Xplus = new HashSet<Character>(X); // 2. initialize
    int len = 0;
    do { // 3. push out
      len = Xplus.size();
      for (FD fd: F)
        if (Xplus.containsAll(fd.lhs) && !Xplus.contains(fd.rhs)) Xplus.add(fd.rhs);
    } while (Xplus.size() > len);  
    return Xplus; // 4. found closure of X
  }

  boolean follows(FD fd){  // fd follows from FDS
    return closure(fd.lhs).contains(fd.rhs);
		//equivelent (FDS (T));
		//return covers(T) && T.covers(this);
  }

  boolean equivalent(FDS T){
    for (FD fd: F){
      if (!T.follows(fd)){
        return false;
      }
    }
    for (FD fd: T.F){
      if (!this.follows(fd)){
        return false;
      }
    }
    return true;
  }

  HashSet<Character> findAKey(){ // returns a key to the relation
    HashSet<Character> Y = new HashSet<Character>(R);
    for(char c: R){
    	Y.remove(c);
    	if(!closure(Y).containsAll(R)){
    		Y.add(c);
    	}
    }
    return Y;
  }

 public static void main(String[] args){
 //Choose a txt file, comment out one of them at a time
    //FDS fds = new FDS("Elmasri1424.txt"); 
    FDS fds = new FDS("Elmasri1425.txt");
    HashSet<Character> X = fds.string2set(args[1]);
    System.out.println("-------------------------------------------");
    System.out.println("Attributes: "+fds.R);
    System.out.println("-------------------------------------------");
    System.out.println("FD's: ");
    for(FD fd: fds.F){
      System.out.println(fd.lhs+" -> "+fd.rhs);
    }
    System.out.println("-------------------------------------------");
    System.out.println("Closure of "+args[1]+": "+fds.closure(X));
    System.out.println("-------------------------------------------");
    System.out.println("Relation key: "+fds.findAKey());
    System.out.println("-------------------------------------------");

 }
}
