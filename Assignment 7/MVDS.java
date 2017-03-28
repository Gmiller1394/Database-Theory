// MVDS.java CS6051/EECE6010 2017 Cheng
// Usage: java MVDS F
// F is a file that has the first line all the attributes and 
// then an FD a line with a space between the left-hand side and the right-hand side
// after an empty line, MVDs are listed

import java.io.*;
import java.util.*;

class FD{
  HashSet<Integer> lhs; int rhs;
  public FD(HashSet<Integer> l, int r){ lhs = l; rhs = r; }
  public boolean equals(Object obj){
    FD fd2 = (FD)obj;
    return lhs.equals(fd2.lhs) && rhs == fd2.rhs;
  }
};

class MVD{
  HashSet<Integer> lhs; HashSet<Integer> rhs;
  public MVD(HashSet<Integer> l, HashSet<Integer> r){ lhs = l; rhs = r; }
  public boolean equals(Object obj){
    FD fd2 = (FD)obj;
    return lhs.equals(fd2.lhs) && rhs.equals(fd2.rhs);
  }
};

public class MVDS{
  int numberOfAttributes = 0;
  char[] R = null; // all attributes
  HashMap<Character, Integer> attr2i = new HashMap<Character, Integer>();
  HashSet<FD> F = new HashSet<FD>(); // the set of FDs
  HashSet<MVD> M = new HashSet<MVD>();  // the set of MVDs

  public MVDS(String filename){  // 1. split FDs so each FD has a single attribute on the right
    Scanner in = null;
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
       System.err.println(filename + " not found");
       System.exit(1);
    }
    String line = in.nextLine();
    numberOfAttributes = line.length();
    R = new char[numberOfAttributes];
    for (int i = 0; i < numberOfAttributes; i++){
       R[i] = line.charAt(i);
       attr2i.put(R[i], i);
    }
    while (in.hasNextLine()){
      String[] terms = in.nextLine().split(" ");
      if (terms.length < 2) break;
      HashSet<Integer> l = new HashSet<Integer>();
      for (int i = 0; i < terms[0].length(); i++) l.add(attr2i.get(terms[0].charAt(i)));
      for (int i = 0; i < terms[1].length(); i++) 
           F.add(new FD(l, attr2i.get(terms[1].charAt(i))));
    }
    while (in.hasNextLine()){
      HashSet<Integer> l = new HashSet<Integer>();
      HashSet<Integer> r = new HashSet<Integer>();
      String[] terms = in.nextLine().split(" ");
      for (int i = 0; i < terms[0].length(); i++) l.add(attr2i.get(terms[0].charAt(i)));
      for (int i = 0; i < terms[1].length(); i++) r.add(attr2i.get(terms[1].charAt(i)));
      M.add(new MVD(l, r));
    }
    in.close();
  }

  void printSet(Set<Integer> X){
    for (int x: X) System.out.print(R[x]);
  }

  HashSet<Integer> closure(HashSet<Integer> X){  // 3.7.1 of Ullman/Widom
     // can be used to implement boolean is4NF()
    int[] row0 = new int[numberOfAttributes];
    int[] row1 = new int[numberOfAttributes];
    for (int i = 0; i < numberOfAttributes; i++){
	row0[i] = 1; row1[i] = 2;
    }
    for (int i: X) row0[i] = row1[i] = 0;
    boolean changed = true;
    do {
	changed = false;
	for (FD f: F){
		boolean applicable = true;
		for (int i: f.lhs) if (row0[i] != row1[i]){
			applicable = false; break; 
		}
		if (applicable) if (row0[f.rhs] != row1[f.rhs]){
			row1[f.rhs] = row0[f.rhs];
			changed = true;
		}
	}
    } while (changed);
    HashSet<Integer> clo = new HashSet<Integer>();
    for (int i = 0; i < numberOfAttributes; i++) if (row0[i] == row1[i]) clo.add(i);
    return clo;
   }

  boolean follows(HashSet<Integer> left, HashSet<Integer> right, HashSet<Integer> subset){
   // true if MVD left ->> right can be projected to subset
   // if right is empty, the tableau can be used to show all FD left -> a 
   // where the column a contains identical values

    int[][] tableau = new int[20][numberOfAttributes];
    int[][] temp = new int[2][numberOfAttributes];
    int numberOfRows = 2;
    for (int i = 0; i < numberOfAttributes; i++) if (left.contains(i))
		tableau[0][i]= tableau[1][i] = 0; // 0 is for the unsubscripted
	else if (right.contains(i)) {
		tableau[0][i] = 0; tableau[1][i] = 2;
	}else{ 	tableau[0][i] = 1; tableau[1][i] = 0; }
for (int iter = 0; iter < 2; iter++){
    for (int r = 1; r < numberOfRows; r++)
	for (int s = 0; s < r; s++){
	    boolean changed = true;
	    while (changed){
		changed = false;
		for (FD fd: F){
			boolean match = true;
			for (int l: fd.lhs) if (tableau[r][l] != tableau[s][l]){
				match = false; break;
			}
			if (match) if (tableau[r][fd.rhs] < tableau[s][fd.rhs]){
				 tableau[s][fd.rhs] = tableau[r][fd.rhs];
				 changed = true;
				}else if (tableau[r][fd.rhs] > tableau[s][fd.rhs]){
				 tableau[r][fd.rhs] = tableau[s][fd.rhs];
				 changed = true;
				}
		}
	    }
	    for (MVD m: M){
		boolean match = true;
		for (int l: m.lhs) if (tableau[r][l] != tableau[s][l]){
			match = false; break;
		}
		if (match){ 
			boolean diff1 = false; boolean diff2 = false;
			for (int t: m.rhs) if (tableau[r][t] != tableau[s][t]) diff1 = true;
			for (int i = 0; i < numberOfAttributes; i++) 
				if (!m.lhs.contains(i) && !m.rhs.contains(i)) diff2 = true;
			if (diff1 && diff2) for (int i = 0; i < numberOfAttributes; i++)
				if (m.lhs.contains(i)) temp[0][i] = temp[1][i] = tableau[r][i];
				else if (m.rhs.contains(i)){
					temp[0][i] = tableau[r][i]; temp[1][i] = tableau[s][i];
				}else{	temp[0][i] = tableau[s][i]; temp[1][i] = tableau[r][i]; }
			int k = 0; for (; k < numberOfRows; k++){
				int i = 0; for (; i < numberOfAttributes; i++)
					if (tableau[k][i] != temp[0][i]) break;
				if (i == numberOfAttributes) break;
			}
			if (k == numberOfRows){ for (int i = 0; i < numberOfAttributes; i++)
					tableau[numberOfRows][i] = temp[0][i];
				numberOfRows++;
			}
			k = 0; for (; k < numberOfRows; k++){
				int i = 0; for (; i < numberOfAttributes; i++)
					if (tableau[k][i] != temp[1][i]) break;
				if (i == numberOfAttributes) break;
			}
			if (k == numberOfRows){ for (int i = 0; i < numberOfAttributes; i++)
					tableau[numberOfRows][i] = temp[1][i];
				numberOfRows++;
			}
		}
	    }
	}
	for (int k = 0; k < numberOfRows; k++){
		for (int i = 0; i < numberOfAttributes; i++) 
			System.out.print(tableau[k][i]);
		System.out.println();
	}
	System.out.println();
       }
	int k = 0; for (; k < numberOfRows; k++){
		boolean unsubscripted = true;
		for (int i: subset) if (tableau[k][i] != 0){
			unsubscripted = false; break; }
		if (unsubscripted) return true;
	}
	return false;
 } 		

 void verify(){
  // read three sets of attributes from stdin and apply follows() with them.
  Scanner in = new Scanner(System.in);
  String line = in.nextLine();
  HashSet<Integer> lhs = new HashSet<Integer>();
  for (int i = 0; i < line.length(); i++) lhs.add(attr2i.get(line.charAt(i)));
  line = in.nextLine();
  HashSet<Integer> rhs = new HashSet<Integer>();
  for (int i = 0; i < line.length(); i++) rhs.add(attr2i.get(line.charAt(i)));
  line.in.nextLine();
  HashSet<Integer> subset = new HashSet<Integer>();
  for (int i = 0; i < line.length(); i++) subset.add(attr2i.get(line.charAt(i)));
  System.out.println(follows(lhs, rhs, subset));
 }
		

 public static void main(String[] args){
    MVDS mvds = new MVDS(args[0]);
    mvds.verify();
 }
}