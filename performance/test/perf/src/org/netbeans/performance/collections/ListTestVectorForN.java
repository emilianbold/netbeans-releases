package org.netbeans.performance.collections;

import org.netbeans.performance.Benchmark;
import java.util.*;

public class ListTestVectorForN extends ListTest {

    /** Creates new FSTest */
    public ListTestVectorForN(String name) {
        super( name );
    }

    protected List createList( int size ) {
        return new Vector( size );
    }

    public static void main( String[] args ) {
	simpleRun( ListTestVectorForN.class );
    }
}
