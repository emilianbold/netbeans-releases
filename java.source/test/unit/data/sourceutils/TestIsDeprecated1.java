package sourceutils;

public class TestIsDeprecated1 {

    @Deprecated
    public void methodDeprecated() {
    }

    public void methodNotDeprecated() {
    }
    
    @Deprecated
    private int fieldDeprecated;

    private int fieldNotDeprecated;
    
    @Deprecated
    public static class classDeprecated {
    }

    public static class classNotDeprecated {
    }
    
}
