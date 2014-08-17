import java.util.*;

public class State {
	public String answer;
	public LinkedList<String> sentence;
	public int pp;
	public boolean np;
	
	State(String answer, LinkedList<String> sentence, int pp, boolean np) {
		this.answer = answer;
		this.sentence = sentence;
		this.pp = pp;
		this.np = np;
	}
	
	public State Copy() {
		return new State(this.answer, (LinkedList<String>)this.sentence.clone(), this.pp, this.np);
	}
}