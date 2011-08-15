/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simplej2seapp;

/**
 *
 * @author pflaska
 */
public class Main extends Object {
    
    public static final int a = 4;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(a);
        I i = new D();
        i.run();
    }

    static class A extends Main {
        public static void main(String[] args) {
            System.out.println(a);
        }
    }

    static class B extends Main {
        public static void main(String[] args) {
            System.out.println(a);
        }
    }

    static class Aa extends A {
        public static void main(String[] args) {
            System.out.println(a);
        }
    }
}
