package org.netbeans.test.java.hints;

public abstract class RedundantCast1 {
    
    public RedundantCast1() {
    }
    
    public void test() {
        String s = (String) get(String.class);
    }
    
    public <T> T get(Class<T> c) {
        return null;
    }
    
}
