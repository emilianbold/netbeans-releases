package org.netbeans.performance.collections;

import org.netbeans.performance.Benchmark;
import java.util.*;

public class ListTestArrayList extends ListTest {

    /** Creates new FSTest */
    public ListTestArrayList(String name) {
        super( name );
    }
    
    protected List createList( int size ) {
        return new ArrayList();
    }
    
    public static void main( String[] args ) {
	simpleRun( ListTestArrayList.class );
    }
}


