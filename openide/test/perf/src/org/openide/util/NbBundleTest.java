package org.openide.util;

import org.openide.utildata.UtilClass;
import org.netbeans.performance.Benchmark;
import java.util.ResourceBundle;

public class NbBundleTest extends Benchmark {

    public NbBundleTest(String name) {
        super( name, new Integer[] {
            new Integer(1), new Integer(10), new Integer(100), new Integer(1000)
        });
    }

    private String[] keys;
    
    protected void setUp() {
        int count = getIterationCount();
        int param = ((Integer)getArgument()).intValue();
        keys = new String[param];
        for( int i=0; i<param; i++ ) {
            keys[i] = "MSG_BundleTest_" + i;
        }
    }
    
    protected void tearDown() {
        keys=null;
    }
        
    public void testGetMessageUsingClass() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();

        while( count-- > 0 ) {
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                NbBundle.getMessage( UtilClass.class, keys[number] );
            }
        }
    }    

    private ResourceBundle bundle;
    private synchronized ResourceBundle getBundle() {
        if( bundle == null ) {
            bundle = NbBundle.getBundle( UtilClass.class );
        }
        return bundle;
    }
    
    private synchronized void clearBundle() {
        bundle = null;
    }
    
    public void testGetMessageUsingLazyCache() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();

        while( count-- > 0 ) {
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                getBundle().getString( keys[number] );
            }
            clearBundle();
        }
    }    

    public void testGetMessageUsingCachedBundle() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();

        while( count-- > 0 ) {
            ResourceBundle bundle = NbBundle.getBundle( UtilClass.class );
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                bundle.getString( keys[number] );
            }
        }
    }    
}
