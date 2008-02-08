package org.netbeans.test.java.hints.HintsTest;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;



public class Surround {

    public void test1() {
        System.out.println("line1");
        URL u = new URL("a");
        System.out.println("line2");
    }
    
    public void test2() {
        FileReader fr = new FileReader("file");
    }
    
    public void test3() {
        try {
            new FileReader("b");
            new URL("c");
        } catch (FileNotFoundException exception) {
            
        }
    }
    
    public void test4() {
        try {
            new FileReader("d");
        } catch(FileNotFoundException exception) {
            new FileReader("e");
        }
    }
}
