package sourceutils;

public class TestIsDeprecated2 {

    /** @deprecated **/
    public void methodDeprecated() {
    }

    public void methodNotDeprecated() {
    }
    
    /** @deprecated **/
    private int fieldDeprecated;

    private int fieldNotDeprecated;
    
    /** @deprecated **/
    public static class classDeprecated {
    }

    public static class classNotDeprecated {
    }
    
}
