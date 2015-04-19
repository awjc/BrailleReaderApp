package com.asliced.braillereader;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import android.graphics.Point;

public class DotProcessor {
//	private static class Point{
//		int x;
//		int y;
//		
//		public Point(int x, int y){
//			this.x = x;
//			this.y = y;
//		}
//		
//		@Override
//		public String toString(){
//			return "Point(" + x + ", " + y + ")";
//		}
//	}
	
	private static int colThresh = 10;
	private static int rowThresh = 10;
	private static int charThresh = 45;
	
	private static HashMap<String, Character> brailleDic = null;
	
	public static void main(String[] args){
		List<Point> dots = new ArrayList<Point>();
		
		dots.add(new Point(65, 119));
		dots.add(new Point(107, 84));
		dots.add(new Point(107, 118));
		dots.add(new Point(109, 53));
		dots.add(new Point(165, 53));
		dots.add(new Point(230, 52));
		dots.add(new Point(233, 76));
		dots.add(new Point(293, 51));
		dots.add(new Point(323, 50));
		
		processDots(dots);
	}

	public static void getDic(InputStream is) throws FileNotFoundException{
		brailleDic = new HashMap<String, Character>();
		
		Scanner in = new Scanner(is);
		while(in.hasNextLine()){
			String line = in.nextLine();
			Scanner lineScan = new Scanner(line);
			String dots = lineScan.next();
			char result = lineScan.next().charAt(0);

			brailleDic.put(dots, result);
			lineScan.close();
		}
		in.close();
	}
	
	private static char getChar(String dot){
		try{
			if(brailleDic == null){
				return '!';
			}
			
			return brailleDic.get(dot);
		} catch(Exception e){
			e.printStackTrace();
			return '!';
		}
	}
	
	public static String processDots(List<Point> dots){
		if(brailleDic == null){
			return "";
		}
		
		List<String> dotz = new ArrayList<String>();
		
		for(int i=0; i < 5; i++){
			System.out.println("<--");
		}
		Collections.sort(dots, new Comparator<Point>(){
			@Override
			public int compare(Point lhs, Point rhs){
				int dx = Math.abs(lhs.x - rhs.x);
				if(dx >= colThresh){
					return lhs.x - rhs.x;
				} 
				
				return lhs.y - rhs.y;
			}
		});
		
		
		alignCols(dots);
		alignRows(dots);
		System.out.println(dots);
		
		List<Integer> runIdxs = new ArrayList<Integer>();
		List<Integer[]> chars = new ArrayList<Integer[]>();
		int base = 0;
		runIdxs.clear();
		runIdxs.add(base);
		for(int i=1; i < dots.size(); i++){
			if(Math.abs(dots.get(base).x - dots.get(i).x) < charThresh){
				runIdxs.add(i);
			} else{
				base = i;
				chars.add(runIdxs.toArray(new Integer[runIdxs.size()]));
				runIdxs.clear();
				runIdxs.add(base);
			}
		}
		chars.add(runIdxs.toArray(new Integer[runIdxs.size()]));
		
		for(Integer[] ii : chars){
			System.out.println(Arrays.toString(ii));
		}
		

		List<Integer> ypts = new ArrayList<Integer>();
		for(Point p : dots){
			if(!ypts.contains(p.y)){
				ypts.add(p.y);
			}
		}
		
		Collections.sort(ypts);

		System.out.println("-------");
		System.out.println("Y:" + ypts);
		
		if(ypts.size() != 3){
			return null;
		}
		
		for(Integer[] ii : chars){
			List<Integer> xpts = new ArrayList<Integer>();
			for(int i : ii){
				if(!xpts.contains(dots.get(i).x)){
					xpts.add(dots.get(i).x);
				}
			}
			
//			System.out.println("X: " + xpts);
			
			int[][] character = new int[3][2];
			for(int i : ii){
				Point p = dots.get(i);
				int xidx = xpts.indexOf(p.x);
				int yidx = ypts.indexOf(p.y);
//				System.out.println("DOT: " + xidx + ", " + yidx);
				
				character[yidx][xidx] = 1;
			}
			System.out.println("================== <--");
			for(int j=0; j < character.length; j++){
				for(int k=0; k < character[j].length; k++){
					System.out.print(character[j][k] + " ");
				}
				System.out.println(" <--");
			}
			
			String dotChar = getString(character);
			dotz.add(dotChar);
		}
		
		boolean num = false;
		for(String dot : dotz){
			if(brailleDic.get(dot) == '#'){
				num = true;
			}
		}
		
		String result = "";
		for(String dot : dotz){
			if(num){
				dot += '#';
			}
			
			char cc = getChar(dot);
			System.out.println("DOT: " + dot + " => " + cc);
			
			result += cc;
		}
		
		System.out.println("FINAL RESULT: " + result);
		
		return result;
	}
	
	private static String getString(int[][] character){
		StringBuilder result = new StringBuilder();
		for(int i=0; i < character.length * character[0].length; i++){
			if(character[i / character[0].length][i % character[0].length] != 0){
				result.append(i+1);
			}
		}
		
		return result.toString();
	}
	
	private static void alignCols(List<Point> dots){
		int base = 0;
		List<Integer> runIdxs = new ArrayList<Integer>();
		List<Integer[]> chars = new ArrayList<Integer[]>();
		runIdxs.add(base);
		for(int i=1; i < dots.size(); i++){
			if(Math.abs(dots.get(base).x - dots.get(i).x) < colThresh){
				runIdxs.add(i);
			} else{
				base = i;
				chars.add(runIdxs.toArray(new Integer[runIdxs.size()]));
				runIdxs.clear();
				runIdxs.add(base);
			}
		}
		chars.add(runIdxs.toArray(new Integer[runIdxs.size()]));
		
		System.out.println();
		for(Integer[] ii : chars){
			System.out.println(Arrays.toString(ii));
			int sum = 0;
			for(int i=0; i < ii.length; i++){
				sum += dots.get(ii[i]).x;
			}
			sum /= ii.length;
			
			for(int i=0; i < ii.length; i++){
				dots.get(ii[i]).x = sum;
			}
		}
	}
	
	private static void alignRows(List<Point> dots){
		Collections.sort(dots, new Comparator<Point>(){
			@Override
			public int compare(Point lhs, Point rhs){
				return lhs.y - rhs.y;
			}
		});
		System.out.println(dots);
		
		int base = 0;
		List<Integer> runIdxs = new ArrayList<Integer>();
		List<Integer[]> chars = new ArrayList<Integer[]>();
		runIdxs.add(base);
		for(int i=1; i < dots.size(); i++){
			if(Math.abs(dots.get(base).y - dots.get(i).y) < rowThresh){
				runIdxs.add(i);
			} else{
				base = i;
				chars.add(runIdxs.toArray(new Integer[runIdxs.size()]));
				runIdxs.clear();
				runIdxs.add(base);
			}
		}
		chars.add(runIdxs.toArray(new Integer[runIdxs.size()]));
		
		System.out.println();
		for(Integer[] ii : chars){
			System.out.println(Arrays.toString(ii));
			int sum = 0;
			for(int i=0; i < ii.length; i++){
				sum += dots.get(ii[i]).y;
			}
			sum /= ii.length;
			
			for(int i=0; i < ii.length; i++){
				dots.get(ii[i]).y = sum;
			}
		}
		
		Collections.sort(dots, new Comparator<Point>(){
			@Override
			public int compare(Point lhs, Point rhs){
				int dx = Math.abs(lhs.x - rhs.x);
				if(dx >= colThresh){
					return lhs.x - rhs.x;
				} 
				
				return lhs.y - rhs.y;
			}
		});
	}
}
