package org.netbeans.performance.collections;

import org.netbeans.performance.Benchmark;
import java.util.*;

public class ListTestArrayListForN extends ListTest {

    /** Creates new FSTest */
    public ListTestArrayListForN(String name) {
        super( name );
    }
    
    protected List createList( int size ) {
        return new ArrayList( size );
    }
    
    public static void main( String[] args ) {
	junit.textui.TestRunner.run( new junit.framework.TestSuite( ListTestArrayListForN.class ) );
    }
}
