package org.netbeans.test.java.hints;

import java.io.FileInputStream;
import java.io.InputStream;

public class Test {
    public Test() {
        this(new FileInputStream("")); //the hint should not be proposed here
    }
    
    public Test(int i) {
        this(new aa(new FileInputStream(""))); //the hint should not be proposed here
    }

    public Test(InputStream i) {
        new FileInputStream("");
    }
    
    Test(Runnable r) {}

    Test(aa a) {
       this(new Runnable() {
            public void run() {
                new FileInputStream(""); //the hint should be proposed here.
            }
        });
    }
}

class aa {
    public aa(FileInputStream is) {
    }
}
    
