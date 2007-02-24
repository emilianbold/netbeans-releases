import java.util.ArrayList;

public class AutoUnBoxingTestFile {
	// Simple Generics Declaration
	ArrayList<Integer> arrayList = new ArrayList<Integer>();
	
	public void method() {		
		// Calling Method with Generics
		for (Integer array : arrayList) {
			// Auto UnBoxing
			int i = array;			
		}
	}
}
