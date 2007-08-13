package test;

public class ExitPoints {
    
    public ExitPoints() {
    }
    
    public void test() {
        if (true)
            return ;
        
        if(true)
            throw new NullPointerException();
        
        return;
    }
    
    public int test(int a) {
        if (true)
            return 0;
        
        if(true)
            throw new NullPointerException();
        
        return 0;
    }
    
    public Object test(Object a) {
        if (true)
            return null;
        
        if(true)
            throw new NullPointerException();
        
        return null;
    }
    
    public void test(String s) throws NullPointerException, javax.swing.text.BadLocationException {
        if(true)
            return ;
        
        throwNPE();
        throwBLE();
        
        try{
            throwNPE();
            throwBLE();
        } catch (NullPointerException e) {}
        
        try{
            throwNPE();
            throwBLE();
        } catch (javax.swing.text.BadLocationException e) {}
        
        try{
            throwNPE();
            throwBLE();
        } catch (Exception e) {}
        
        try{
            try{
                throwNPE();
            } catch (NullPointerException e) {}
            throwBLE();
        } catch (NullPointerException e) {}

        try{
            try{
                throwNPE();
                throwBLE();
            } catch (NullPointerException e) {}
        } catch (javax.swing.text.BadLocationException e) {}
        
        try{
            try{
                throwNPE();
            } catch (NullPointerException e) {}
            throwBLE();
        } catch (javax.swing.text.BadLocationException e) {}
        
        try{
            throwBLE();
            try{
                throwNPE();
            } catch (NullPointerException e) {}
        } catch (NullPointerException e) {}
    }
    
    public void test(double x) throws NullPointerException, javax.swing.text.BadLocationException {
        new ConstructorThrows();
    }
    
    private void throwNPE() throws NullPointerException {
        
    }
    
    private void throwBLE() throws javax.swing.text.BadLocationException {
        
    }
    
    private java.util.List<String> testListString() {
        return null;
    }
    
    private String[] testArray() {
        return new String[]{new String()};
    }
}
