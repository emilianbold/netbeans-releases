package org.netbeans.performance.collections;

import org.netbeans.performance.Benchmark;
import java.util.*;

public class ListTestLinkedList extends ListTest {

    /** Creates new FSTest */
    public ListTestLinkedList(String name) {
        super( name );
    }

    protected List createList( int size ) {
        return new LinkedList();
    }

    public static void main( String[] args ) {
	simpleRun( ListTestLinkedList.class );
    }    
}
