// Travis Brundage
// 11/26/13
// CAP 5636
// Recursive Transition Network

import java.io.*;
import java.util.*;

public class grammar {
	
	// Fail condition
	public static boolean ACCEPTED = false;
	
	// Database
	public static HashMap<String, ArrayList<String>> db;
	
	public static HashMap<String, ArrayList<String>> getDB() throws IOException {
		db = new HashMap<String, ArrayList<String>>();
		Scanner fin = new Scanner(new File("database.in"));
		ArrayList<String> words = new ArrayList<String>();
		String word, pos = "";
		StringTokenizer line;
		
		// Read all words for each pos
		do {
			// each iteration, get line, first token is pos, all next tokens are words for this pos
			line = new StringTokenizer(fin.nextLine().toLowerCase());
			pos = line.nextToken();
			while (line.hasMoreTokens()) {
				word = line.nextToken();
				words.add(word);
			}
			
			// add this array list of words and its pos to hash, and clear it
			db.put(pos, (ArrayList<String>)words.clone());
			words.clear();
		} while(fin.hasNext());
		
		return db;
	}
	
	public static LinkedList<String> getSentence() throws IOException {
		LinkedList<String> s = new LinkedList<String>();
		Scanner fin = new Scanner(new File("sentence.in"));
		StringTokenizer input = new StringTokenizer(fin.nextLine().toLowerCase());
		
		while(input.hasMoreTokens()) { s.add(input.nextToken()); }
		
		return s;
	}
	
	public static boolean POS(String pos, String word) {
		if (word == null) { return false; }
		
		ArrayList<String> words = db.get(pos);
		
		for (String w : words) {
			if (word.compareTo(w) == 0) {
				return true;
			}
		}
		
		return false;
	}
	
	public static void Die() {
		System.out.print("No acceptable parse found for the sentence.");
		System.exit(1);
	}
	
	public static void main(String[] args) throws IOException {
		// Create database of words: Key: String for POS, Value: List of Strings for every word in the database with this POS
		db = getDB();
		// Get sentence to parse: each String in the sentence is another word
		LinkedList<String> sentence = getSentence();
		System.out.println("Parsing the sentence: " + sentence);
		// Parse the sentence using recursive transition network, producing all parses
		parse(sentence);
		// Check if we did not find any acceptable parses
		if (ACCEPTED == false) {
			Die();
		}
	}
	
	
	// Recursive Transition Network
	// Parse the sentence using NFA rules
	public static void parse(LinkedList<String> sentence) {
		S(sentence);
	}
	
	public static void S(LinkedList<String> sentence) {
		State state = new State("(S ", sentence, 0, false);
		Stack<State> finalstack = new Stack<State>();
		String current;
		
		// NP
		Stack<State> stack = NP(state);
		
		// If stack is empty, NP call failed, so Die
		if (stack.isEmpty()) {
			Die();
		}
		
		// Verb
		while (!stack.isEmpty()) {
			state = stack.pop();
			current = state.sentence.poll();
			if (POS("verb", current)) {
				state.answer += "(VERB " + current + ") ";
				finalstack.push(state);
			}
		}
		
		// Last NP and PP
		while (!finalstack.isEmpty()) {
			state = finalstack.pop();
			
			// If goal conditions have been met, we accept this state
			if (state.sentence.isEmpty() && state.pp == 0) {
				System.out.println(state.answer);
				ACCEPTED = true;
				continue;
			}
			
			current = state.sentence.peek();
			// If this state can still grab a NP
			if (state.np == false && (POS("det", current) || POS("adj", current) || POS("noun", current))) {
				stack = NP(state);
				
				while (!stack.isEmpty()) {
					state = stack.pop();
					state.np = true;
					finalstack.push(state);
				}
			}
			
			current = state.sentence.peek();
			// If this state can grab a PP
			if (POS("prep", current)) {
				stack = PP(state);
				
				while (!stack.isEmpty()) {
					state = stack.pop();
					state.np = true;
					finalstack.push(state);
				}
			}
		}
	}
	
	public static Stack<State> NP(State state) {
		Stack<State> NPstack = new Stack<State>();
		Stack<State> finalstack = new Stack<State>();
		String current;
		State copy;
		Stack<State> PPstack;
		
		// Open NP
		state.answer += "(NP ";
		
		// Det
		current = state.sentence.peek();
		if (POS("det", current)) {
			current = state.sentence.poll();
			state.answer += "(DET " + current + ") ";
		}
		
		// Adj
		current = state.sentence.poll();
		while (POS("adj", current)) {
			if (POS("noun", current)) {
				copy = state.Copy();
				copy.answer += "(NOUN " + current + ") ";
				NPstack.push(copy);
			}
			
			state.answer += "(ADJ " + current + ") ";
			
			current = state.sentence.poll();
		}
		
		// Noun
		if (POS("noun", current)) {
			state.answer += "(NOUN " + current + ") ";
			NPstack.push(state);
		}
		
		// PP
		while (!NPstack.isEmpty()) {
			state = NPstack.pop();
			
			// Take option of closing NP
			copy = state.Copy();
			copy.answer += ") ";
			finalstack.push(copy);
			
			// Take the option of eating a PP
			current = state.sentence.peek();
			if (POS("prep", current)) {
				PPstack = PP(state);
				while (!PPstack.isEmpty()) {
					NPstack.push(PPstack.pop());
				}
			}
		}
		
		return finalstack;
	}
	
	public static Stack<State> PP(State state) {
		Stack<State> finalstack = new Stack<State>();
		Stack<State> PPstack;
		
		// Open PP
		state.answer += "(PP ";
		state.pp++;
		
		// Prep
		String current = state.sentence.poll();
		if (POS("prep", current)) {
			state.answer += "(PREP " + current + ") ";
		}
		
		// NP
		PPstack = NP(state);
		
		// Close PP
		while (!PPstack.isEmpty()) {
			state = PPstack.pop();
			state.answer += ") ";
			state.pp--;
			finalstack.push(state);
		}
		
		return finalstack;
	}
}