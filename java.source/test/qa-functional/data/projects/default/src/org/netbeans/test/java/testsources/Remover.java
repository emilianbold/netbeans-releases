/*
 * EtalonForRemover.java
 *
 * Created on May 30, 2000, 4:55 PM
 */

package org.netbeans.test.java.testsources;

/**
 *
 * @author  jbecicka
 * @version 
 */
public abstract class Remover extends Object {

    /** fields
     *
     */
    
    public static int a,b,c,d,e,f;

    /** Creates new EtalonForRemover */
    public Remover() {
    }
    
    /** javadoc comment
     * blah blah blah
     */
    
    /* non javadoc comment */
    public abstract int doNothing();
    
    /** javadoc comment doImportant*/
    public void doImportant(){
        int b=0;
        for(int i=0; i<0; i++){
            b+=i;
        }
    }
    
    
    /** Overloaded method1
     */
    public static int method1(int parameter1) throws java.io.IOException {
        /*This is body of mehodmethod1*/
        for (int i=0;i<100;i++){
            System.out.println(new Integer(i).toString());
        }
        return 0;
    }
    
    /** Overloaded method1
     */

    public static int method1(int parameter1,int parameter2) throws java.io.IOException {
        /*This is body of mehodmethod1*/
        for (int i=0;i<100;i++){
            System.out.println(new Integer(i).toString());
        }
        return 0;
    }
    
    /** Overloaded method1
     */

    public static int method1(float parameter1,float parameter2,final int parameter3) throws java.io.IOException {
        /*This is body of mehodmethod1*/
        for (int i=0;i<100;i++){
            System.out.println(new Integer(i).toString());
        }
        return 0;
    }
    
    /** Overloaded method2
     */
    public static int method2(int parameter1) throws java.io.IOException {
        /*This is body of mehodmethod2*/
        for (int i=0;i<100;i++){
            System.out.println(new Integer(i).toString());
        }
        return 0;
    }
    
    public double fa=3.1,fb=3.2,fc=3.3,fe=3.4,fd=3.5;
    
    /** Overloaded method2
     */
    public static short method2(int parameter1,int parameter2) throws java.io.IOException {
        /*This is body of mehodmethod2*/
        for (int i=0;i<100;i++){
            System.out.println(new Integer(i).toString());
        }
        return 0;
    }
    
    /** overloaded method 2
     */
    public static long method2(float parameter1,float parameter2,final int parameter3) throws java.io.IOException {
        /*This is body of mehodmethod2*/
        for (int i=0;i<100;i++){
            System.out.println(new Integer(i).toString());
        }
        return 0;
    }
    
    /** InnerClass
     */
    public class InnerClass {

        /** method from Inner Class */ 
        public void newMethod() {
        }
    }
    
    /** Inner Interface
     */
    public interface InnerInterface {

        /** filed from Inner Interface*/
        public static int newField=123;
        
        /** method from Inner Interface */
        public void newMethod();
    }
}
