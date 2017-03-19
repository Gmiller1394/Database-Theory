// FDS3.java CS6051/EECE6010 2017 Cheng
// Algorithm 3.26 3NF decomposition
// Find all prime attributes
// Usage: java FDS3 F
// F is a file that has the first line all the attributes and 
// then an FD a line with a space between the left-hand side and the right-hand side

import java.io.*;
import java.util.*;

class FD{
  HashSet<Character> lhs; char rhs;
  public FD(HashSet<Character> l, char r){ lhs = l; rhs = r; }
  public boolean equals(Object obj){
    FD fd2 = (FD)obj;
    return lhs.equals(fd2.lhs) && rhs == fd2.rhs;
  }
  public void printout(){
    for (char c: lhs) System.out.print(c);
    System.out.print(" "); System.out.print(rhs); System.out.println();
  }
};

public class FDS3{
  HashSet<Character> R = new HashSet<Character>(); // all attributes
  HashSet<FD> F = new HashSet<FD>(); // the set of FDs
  HashSet<Character> aKey = null;
  HashSet<Character> primeAttributes = new HashSet<Character>();

  public FDS3(String filename){  // 1. split FDs so each FD has a single attribute on the right
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

  public FDS3(HashSet<Character> r, HashSet<FD> f){ R = r; F = f; }

  HashSet<Character> string2set(String X){
    HashSet<Character> Y = new HashSet<Character>();
    for (int i = 0; i < X.length(); i++) Y.add(X.charAt(i));
    return Y;
  }

  void printSet(Set<Character> X){
    for (char c: X) System.out.print(c);
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

 HashSet<HashSet<Character>> findKeys(HashSet<Character> candidates, HashSet<Character> subset){
	// starts with candidates = R and subset = empty
	// returns all keys
	HashSet<HashSet<Character>> keys = new HashSet<HashSet<Character>>();
	HashSet<Character> clo = closure(subset);
	if (!clo.containsAll(R)){  // adding one candidate attribute to subset and recursion on it
		HashSet<Character> candidates2 = new HashSet<Character>(candidates);
		for (char c: candidates){
			candidates2.remove(c);
			subset.add(c);
			keys.addAll(findKeys(new HashSet<Character>(candidates2), 
				new HashSet<Character>(subset))); // recursive call
			subset.remove(c);
		}
	}else{  // subset may be a key
		if (isKey(subset)){ 
			// printSet(subset); System.out.println(); // printing key
			keys.add(subset);  // gathering prime attributes
		}
	}
	return keys;
  }

  boolean isKey(HashSet<Character> possibleKey){
	for (char c: possibleKey){
		HashSet<Character> set2 = new HashSet<Character>(possibleKey);
		set2.remove(c);
		if (closure(set2).containsAll(R)) return false;
	}
	return true;
  }

  void findAllPrimeAttributes(){
	HashSet<HashSet<Character>> keys = findKeys(R, new HashSet<Character>());
	for (HashSet<Character> key: keys){
		if (aKey == null) aKey = key;
		primeAttributes.addAll(key);
	}
	// printSet(primeAttributes); System.out.println();  // print out prime attributes
  }

  boolean is3NF(){
    /* Your code here
	for (FD fd: F)
		if ( your code for fd violating 3NF) 
			return false;
    */
	return true;
  }

  void decompose(){  // Algorithm 3.26
    // HashSet<FD> G = minCover(F);  // step 1 of Algorithm 3.26, skip it for now
    HashSet<FD> G = new HashSet<FD>(F);
    // step 2 of Algorithm 3.26 FDs with identical lhs will be combined
    HashSet<HashSet<Character>> H = new HashSet<HashSet<Character>>();
    for (FD fd: G) H.add(fd.lhs); // all the lhs's
    HashSet<HashSet<Character>> schemas = new HashSet<HashSet<Character>>();
    for (HashSet<Character> cs: H){  // for each identical lhs add all the rhs's to form a schema
      HashSet<Character> schema = new HashSet<Character>();
      schema.addAll(cs);
      for (FD fd: G) if (fd.lhs.equals(cs)) schema.add(fd.rhs);
      schemas.add(schema);
    }
    // step 3 of Algorithm 3.26
    boolean keyIn = false;
    for (HashSet<Character> s: schemas) if (closure(s).containsAll(R)){ keyIn = true; break; }
    if (!keyIn) schemas.add(aKey);
    // additional step to rid redundant relations (projections of other relations)
    for (HashSet<Character> s: schemas) for (HashSet<Character> t: schemas)
     if (t.size() >= s.size() && !t.equals(s) && t.containsAll(s)) schemas.remove(s);
    for (HashSet<Character> s: schemas){ printSet(s); System.out.println(); }
  }

 public static void main(String[] args){
    FDS3 fds = new FDS3(args[0]);     
    fds.findAllPrimeAttributes();
    if (fds.is3NF()) System.out.println("Relation is in 3NF");
    else fds.decompose();
 }
}