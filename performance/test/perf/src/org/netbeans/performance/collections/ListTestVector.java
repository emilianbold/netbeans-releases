package org.netbeans.performance.collections;

import org.netbeans.performance.Benchmark;
import java.util.*;

public class ListTestVector extends ListTest {

    /** Creates new FSTest */
    public ListTestVector(String name) {
        super( name );
    }
    
    protected List createList( int size ) {
        return new Vector();
    }

    public static void main( String[] args ) {
	junit.textui.TestRunner.run( new junit.framework.TestSuite( ListTestVector.class ) );
    }
}
