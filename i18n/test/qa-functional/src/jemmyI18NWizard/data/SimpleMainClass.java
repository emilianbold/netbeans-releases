/*
 * SimpleMainClass.java
 *
 * Created on 29. duben 2001, 22:53
 */

package jemmyI18NWizard.data;

/**
 *
 * @author  vn104997
 * @version 1.0
 */
public class SimpleMainClass {
    String string1 = null;

    /** Creates new SimpleMainClass */
    public SimpleMainClass() {
        string1 = "Testing sequence: \"{}()[]";
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        System.out.println("there is something rotten in the state of Danemark...");
        System.out.println("\u0025\t\u0026");
    }

}
