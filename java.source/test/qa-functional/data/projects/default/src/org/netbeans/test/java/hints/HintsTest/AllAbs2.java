
package org.netbeans.test.java.hints.HintsTest;

import javax.swing.table.AbstractTableModel;

public class AllAbs2 extends AbstractTableModel{

    public AllAbs2() {
    }
    
    class Inner<T> implements Comparable<T> {
               
    }

}
