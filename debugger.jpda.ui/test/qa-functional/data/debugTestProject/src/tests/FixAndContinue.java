/*
 * FixAndContinue.java
 *
 * Created on April 26, 2005, 2:58 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package tests;

/**
 *
 * @author lolo
 */
public class FixAndContinue {
    
    /** Creates a new instance of FixAndContinue */
    public FixAndContinue() {
    }
    
    public static void main(String args[]) {
        FixAndContinue fc = new FixAndContinue();
        fc.method();
        fc.method();
    }
    
    public void method() {
        beforeFix();
    }
    
    public void beforeFix() {
        System.out.println("Before code changes");
    }
    
    public void afterFix() {
        System.out.println("After code changes");
    }
    
    /*public void newMethod() {
        for (int i = 10; i > 0; i--)
            System.out.println(i);
    }*/
}
