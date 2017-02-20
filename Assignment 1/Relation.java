//Database Theory Assignment 2

import java.io.*;
import java.util.*;
import java.util.function.*;

public class Relation{
	String name;
	int cols;
	int rows;
	String[] attributes;
	String[][] tuples;

	public Relation(){ }

	public Relation(String filename){
		Scanner in = null;
		try {
			in = new Scanner(new File(filename));
		} catch (FileNotFoundException e){
			System.err.println(filename + " not found.");
			System.exit(1);
		}
		String[] terms = in.nextLine().split("\t");
		name = terms[0];
		cols = Integer.parseInt(terms[1]);
		rows = Integer.parseInt(terms[2]);
		attributes = new String[cols];
		tuples = new String[rows][cols];
		terms = in.nextLine().split("\t");
		for (int c = 0; c < cols; c++) attributes[c] = terms[c];
		for (int r = 0; r < rows; r++){
			terms = in.nextLine().split("\t");
			for (int c = 0; c < cols; c++) tuples[r][c] = terms[c];
		}
		in.close();
		}

		public void showRelation(){
		System.out.println(name + "\t" + cols + "\t" + rows);
		System.out.print(attributes[0]);
		for (int c = 1; c < cols; c++) System.out.print("\t" + attributes[c]);
		System.out.println();
		for (int r = 0; r < rows; r++){
			System.out.print(tuples[r][0]);
			for (int c = 1; c < cols; c++) System.out.print("\t" + tuples[r][c]);
			System.out.println();
		}
	}

	public Relation Project(String... attrs){
		int[] columnIndex = new int[attrs.length];
		for (int c = 0; c < attrs.length; c++){
			int j = 0; for (; j < cols; j++) if (attrs[c].equals(attributes[j])) break;
			if (j == cols){
				System.err.println("attribute " + attrs[c] + " not found.");
				System.exit(1);
			}
			columnIndex[c] = j;
		}
		Relation PR = new Relation();
		PR.cols = attrs.length;
		PR.name = "Project(" + name;
		for (int c = 0; c < PR.cols; c++) PR.name += "," + attrs[c];
		PR.name += ")";
		PR.rows = rows;
		PR.attributes = new String[PR.cols];
		for (int c = 0; c < PR.cols; c++) PR.attributes[c] = attrs[c];
		PR.tuples = new String[rows][PR.cols];
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < PR.cols; c++) PR.tuples[r][c] = tuples[r][columnIndex[c]];
		return PR;
	}

	public Relation select(Function<Integer, Boolean> condition){
		Relation SR = new Relation();
		SR.cols= cols;
		SR.name = "select(" + name + ",condition)";
		int[] selected = new int[rows];
		int n = 0;
		for (int r = 0; r < rows; r++) if(condition.apply(r)) selected[n++] = r;
		SR.rows = n;
		SR.attributes = new String[cols];
		for (int c = 0; c < cols; c++) SR.attributes[c] = attributes[c];
		SR.tuples = new String[SR.rows][cols];
		for (int r = 0; r < SR.rows; r++)
			for (int c = 0; c < cols; c++) SR.tuples[r][c] = tuples[selected[r]][c];
		return SR;
	}

	public Relation join(Relation other, Function<int[], Boolean> condition){
		boolean[][] theta = new boolean[rows][other.rows];
		int[] pair = new int[2];
		int newRows = 0;
		for (int r1 = 0; r1 < rows; r1++){
			pair[0] = r1;
			for (int r2 = 0; r2 < other.rows; r2++){
				pair[1] = r2;
				theta[r1][r2] = condition.apply(pair);
				if (theta[r1][r2]) newRows++;
			}
		}
		Relation JR = new Relation();
		JR.name = name + "X" + other.name;
		JR.cols = cols + other.cols;
		JR.rows = newRows;
		JR.attributes = new String[JR.cols];
		for (int c = 0; c < cols; c++) JR.attributes[c] = attributes[c];
		for (int c = 0; c < other.cols; c++) JR.attributes[cols + c] = other.attributes[c];
		JR.tuples = new String[JR.rows][JR.cols];
		int r = 0;
		for (int r1 = 0; r1 < rows; r1++) for (int r2 = 0; r2 < other.rows; r2++)
			if (theta[r1][r2]){
				for (int c = 0; c < cols; c++) JR.tuples[r][c] = tuples[r1][c];
				for (int c = 0; c < other.cols; c++)
					JR.tuples[r][cols + c] = other.tuples[r2][c];
				r++;
			}
		return JR;
		}

	public Relation intersection(Relation other){
		if (cols != other.cols) return null;
		boolean[] intersects = new boolean[rows];
		int newRows = 0;
		for (int r = 0; r < rows; r++){
			int r1 = 0; for (; r1 < other.rows; r1++){
				int c = 0; for (; c < cols; c++)
					if (!tuples[r][c].equals(other.tuples[r1][c])) break;
				if (c == cols) break;
			}
			intersects[r] = r1 < other.rows;
			if (intersects[r]) newRows++;
		}
		Relation R = new Relation();
		R.name = name + " n " + other.name;
		R.cols = cols;
		R.rows = newRows;
		R.attributes = new String[cols];
		for (int c = 0; c < cols; c++) R.attributes[c] = attributes[c];
		R.tuples = new String[R.rows][cols];
		int n = 0;
		for (int r = 0; r < rows; r++) if (intersects[r]){
			for (int c = 0; c < cols; c++) R.tuples[n][c] = tuples[r][c];
			n++;
		}
		return R;
	}

	public Relation union(Relation other){
		if (cols != other.cols) return null;
		boolean[] intersects = new boolean[rows];
		int newRows = other.rows;
		for (int r = 0; r < rows; r++){
			int r1 = 0; for (; r1 < other.rows; r1++){
				int c = 0; for (; c < cols; c++)
					if (!tuples[r][c].equals(other.tuples[r1][c])) break;
				if (c == cols) break;
			}
			intersects[r] = r1 < other.rows;
			if (!intersects[r]) newRows++;
		}
		Relation R = new Relation();
		R.name = name + " u " + other.name;
		R.cols = cols;
		R.rows = newRows;
		R.attributes = new String[cols];
		for (int c = 0; c < cols; c++) R.attributes[c] = attributes[c];
		R.tuples = new String[R.rows][cols];
		for (int r = 0; r < other.rows; r++)
			for (int c = 0; c < cols; c++) R.tuples[r][c] = other.tuples[r][c];
		int n = other.rows;
		for (int r = 0; r < rows; r++) if (!intersects[r]){
			for (int c = 0; c < cols; c++) R.tuples[n][c] = tuples[r][c];
			n++;
		}
		return R;
	 }

	public Relation difference(Relation other){
		if (cols != other.cols) return null;
		boolean[] intersects = new boolean[rows];
		int newRows = rows;
		for (int r = 0; r < rows; r++){
			int r1 = 0; for (; r1 < other.rows; r1++){
				int c = 0; for (; c < cols; c++)
					if (!tuples[r][c].equals(other.tuples[r1][c])) break;
				if (c == cols) break;
			}
			intersects[r] = r1 < other.rows;
			if (intersects[r]) newRows--;
		}
		Relation R = new Relation();
		R.name = name + " - " + other.name;
		R.cols = cols;
		R.rows = newRows;
		R.attributes = new String[cols];
		for (int c = 0; c < cols; c++) R.attributes[c] = attributes[c];
		R.tuples = new String[R.rows][cols];
		int n = 0;
		for (int r = 0; r < rows; r++) if (!intersects[r]){
			for (int c = 0; c < cols; c++) R.tuples[n][c] = tuples[r][c];
			n++;
		}
		return R;
	}
public int getAttrIndex(String attr) {
		int idx = 0;
		for (; idx < cols; idx++) {
			if (attr.equals(attributes[idx])) break;
		}

		if (idx == cols){
			System.err.println("attribute " + attr + " not found.");
			System.exit(1);
		}

		return idx;
	}
	public Relation unique(){   // remove duplicate tuples
		boolean[] intersects = new boolean[rows];
		int newRows = 0;
		for (int r = 0; r < rows; r++){
			int r1 = 0; for (; r1 < r; r1++){
				int c = 0; for (; c < cols; c++)
					if (!tuples[r][c].equals(tuples[r1][c])) break;
				if (c == cols) break;
			}
			intersects[r] = r1 < r;
			if (!intersects[r]) newRows++;
		}
		Relation R = new Relation();
		R.name = name + ".uniq";
		R.cols = cols;
		R.rows = newRows;
		R.attributes = new String[cols];
		for (int c = 0; c < cols; c++) R.attributes[c] = attributes[c];
		R.tuples = new String[R.rows][cols];
		int n = 0;
		for (int r = 0; r < rows; r++) if (!intersects[r]){
			for (int c = 0; c < cols; c++) R.tuples[n][c] = tuples[r][c];
			n++;
		}
		return R;
	}
	public Boolean isEmpty() {
		return this.rows == 0;
	}

	public Boolean isSubsetOf(Relation other) {
		return this.difference(other).isEmpty();
	}
	public boolean fd(String right, String... left) {
		boolean isFD = true;
		int leftLength = left.length;
		int[] columnIndex = new int[leftLength];
		for(int a = 0; a < leftLength; a++) {
			columnIndex[a] = getAttrIndex(left[a]);
		}

		int rightColumn = this.getAttrIndex(right);
		for(int r1 = 0; r1 < rows; r1++) {
			for(int r2 = r1 + 1; r2 < rows; r2++) {
				int c1 = 0;
				for(; c1 < left.length; c1++) {
					int c = columnIndex[c1];
					if(!this.tuples[r1][c].equals(this.tuples[r2][c])) {
						break;
					}
					if(!this.tuples[r1][rightColumn].equals(this.tuples[r2][rightColumn])) {
						isFD = false;
						System.out.println(/*"Violating constraints: " +*/ (this.tuples[r1][rightColumn] + " & " + this.tuples[r2][rightColumn]));
					}
				}
			}
		}

		return isFD;
	}

	public static void main(String[] args){
		Relation Product = new Relation("Product.txt");
		Relation PC = new Relation("PC.txt");
		Relation Laptop = new Relation("Laptop.txt");
		Relation Printer = new Relation("Printer.txt");

		// Question 1
		System.out.println("Question 1:");
		Relation checkPart1 = PC.select(x -> Double.parseDouble(PC.tuples[x][1]) < 2.00 && Double.parseDouble(PC.tuples[x][4]) < 500);
		Boolean isEmpty1 = checkPart1.isEmpty();
		if(isEmpty1) {
			System.out.println("This constraint is satisfied.\n");
		} else {
			System.out.println("This constraint isn't satisfied.\n");
		}

		// Question 2
		System.out.println("Question 2:");
		Relation checkPart2 = Laptop.select(x -> Double.parseDouble(Laptop.tuples[x][4]) < 15.4 && (Double.parseDouble(Laptop.tuples[x][3]) <= 100 && Double.parseDouble(Laptop.tuples[x][5]) > 1000));
		Boolean isEmpty2 = checkPart2.isEmpty();
		if(isEmpty2) {
			System.out.println("This constraint is satisfied.\n");
		} else {
			System.out.println("This constraint isn't satisfied.\n");
		}

		// Question 3
		System.out.println("Question 3:");
		Relation pcModels = PC.Project("model");
		Relation pcMakers = pcModels.join(Product, x -> pcModels.tuples[x[0]][0].equals(Product.tuples[x[1]][1])).Project("maker");
		Relation LaptopModels = Laptop.Project("model");
		Relation LaptopMakers = LaptopModels.join(Product, x -> LaptopModels.tuples[x[0]][0].equals(Product.tuples[x[1]][1])).Project("maker");
		Relation pcAndLaptopMakers = LaptopMakers.join(pcMakers, x -> LaptopMakers.tuples[x[0]][0].equals(pcMakers.tuples[x[1]][0])).unique().Project("maker");
		Boolean isEmpty3 = pcAndLaptopMakers.isEmpty();
		if(isEmpty3) {
			System.out.println("This constraint is satisfied.\n");
		} else {
			System.out.println("This constraint isn't satisfied.\n");
		}

		// Question 4
		System.out.println("Question 4:");
		Relation pcModels4 = PC.Project("model");
		Relation pcMakers4 = pcModels4.join(Product, x -> pcModels4.tuples[x[0]][0].equals(Product.tuples[x[1]][1]));
		Relation LaptopModels4 = Laptop.Project("model");
		Relation LaptopMakers4 = LaptopModels4.join(Product, x -> LaptopModels4.tuples[x[0]][0].equals(Product.tuples[x[1]][1]));
		Relation pcData4 = pcMakers4.join(PC, x -> pcMakers4.tuples[x[0]][0].equals(PC.tuples[x[1]][0])).Project("maker", "model", "speed");
		Relation LaptopData4 = LaptopMakers4.join(Laptop, x -> LaptopMakers4.tuples[x[0]][0].equals(Laptop.tuples[x[1]][0])).Project("maker", "model", "speed");
		Relation allData4 = LaptopData4.join(pcData4, x -> Double.parseDouble(LaptopData4.tuples[x[0]][2]) < Double.parseDouble(pcData4.tuples[x[1]][2]) && LaptopData4.tuples[x[0]][0].equals(pcData4.tuples[x[1]][0]));
		Boolean isEmpty4 = allData4.isEmpty();
		if(isEmpty4) {
			System.out.println("This constraint is satisfied.\n");
		} else {
			System.out.println("This constraint isn't satisfied.\n");
		}
		
		// Question 5
		System.out.println("Question 5:");
		Relation LaptopData5 = Laptop.Project("ram", "price");
		Relation pcData5 = PC.Project("ram", "price");
		Relation violatingTuples = LaptopData5.join(pcData5, x -> Double.parseDouble(LaptopData5.tuples[x[0]][0]) > Double.parseDouble(pcData5.tuples[x[1]][0]) && Double.parseDouble(LaptopData5.tuples[x[0]][1]) < Double.parseDouble(pcData5.tuples[x[1]][1]));
		boolean isEmpty5 = violatingTuples.isEmpty();
		if(isEmpty5) {
			System.out.println("This constraint is satisfied.\n");
		} else {
			System.out.println("This constraint isn't satisfied.\n");
		}

		// Question 6
		System.out.println("Question 6:");
		Relation pcModels6 = PC.Project("model");
		Relation ProductModels6 = Product.Project("model");
		boolean isSubset6 = pcModels6.isSubsetOf(ProductModels6);
		if(isSubset6) {
			System.out.println("All models of PCs are also listed in Products\n");
		} else {
			System.out.println("The Product relation does not list all PC models\n");
		}

		// Question 7
		System.out.println("Question 7:");
		System.out.println("Violating constraints: ");
		boolean isFD = Laptop.fd("hd", "ram");
		if(isFD) {
			System.out.println("\n HD functionally determines RAM for Laptops\n");
		} else {
			System.out.println("\n HD doesn't functionally determine RAM for Laptops\n");
		}
	}
}