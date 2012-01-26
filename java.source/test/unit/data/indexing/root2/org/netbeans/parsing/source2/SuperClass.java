/*
 * 
 * 
 * 
 * 
 * 
 * 
 */

package org.netbeans.parsing.source2;

import javax.swing.table.DefaultTableModel;

/**
 *
 * 
 */
public class SuperClass extends DefaultTableModel implements Runnable {
   
    /**
     * public ctor
     */
    public SuperClass() {
        Integer i;
        
    }

    /**
     * public method 
     */
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * package private inner class
     */
    class Inner {
        
    }
    
    /**
     * private field
     */
    private String field;
    
    /**
     * protected field
     */    
    protected  int field2;
    
    /**
     * package private enum
     * containt 3 constants, privat  ctor (implicit) and two static methods(implicit)
     */
    enum Color {R,G,B};

}
