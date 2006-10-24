package org.netbeans.test.java.hints;

import java.io.IOException;

public class AddThrowsClause4 {
    
    public AddThrowsClause4() {
    }
    
    public void test() {
        throw exc();
    }
    
    public IOException exc() {
        return new IOException();
    }
}
