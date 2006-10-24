package sourceutils;

public class TestGetEnclosingTypeElement {

    public void testMethod() {
    
        class ClassInMethod {
	
	    public void classInMethodMethod() {
	    }
	    
	    private int classInMethodField;
	    
	    class ClassInMethodNestedClass {
	    }
	    
	}
	
    }
    
    private int testField;
    
    public static class NestedClass {
    
        public void nestedClassMethod() {
	}
	
	private int nestedClassField;
	
	public static class NestedClassNestedClass {
	}
	
    }
    
}
