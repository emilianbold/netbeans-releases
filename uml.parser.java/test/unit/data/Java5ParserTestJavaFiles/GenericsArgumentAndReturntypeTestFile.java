import java.util.ArrayList;

public class GenericsArgumentAndReturntypeTestFile {
	// Simple Generics Declaration
	ArrayList<Integer> arrayList = new ArrayList<Integer>();
	
	public void method() {
		ArrayList<Integer>  a1 = addCollection(arrayList);
	}

	// Return Type and Parameters are Generics
	ArrayList<Integer> addCollection(ArrayList<Integer> arrayList) {		
		return arrayList;
	}

}
