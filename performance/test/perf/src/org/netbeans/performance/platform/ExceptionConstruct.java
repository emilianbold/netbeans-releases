/*
 * ExceptionConstruct.java
 *
 * Created on 28. bre 2001, 17:44
 */

package org.netbeans.performance.platform;

import org.netbeans.performance.*;
import java.util.*;

/**
 * The implementation of a benchmark measuring how long would it take
 * to construct an instance of Exception. Measured bacause the Exception
 * contains native-filled structure describing the shape of the thread
 * stack in the moment of constructing and because OpenIde uses manually
 * created exceptions as a holders for RequestProcessor tasks to allow
 * it to annotate the possible exception inside the RequestProcessor
 * with the information about the caller.
 *
 * @author  Petr Nejedly
 * @version 0.0
 */
public class ExceptionConstruct extends TestDescription implements Test {

    private static final String[] names = {
            "Object", "Exception", "thrown Exception" };
            
    private static final int[] depths = {
            1, 2, 5, 10, 20, 50, 100, 200 };

    /** Creates new ListAppendTest */
    public ExceptionConstruct() {
    }
    
    public Test getTest() {
        return this;
    }
    
    public String getTestDescription() {
        return "Create an object n calls deep in recursion";
    }
    
    public int getImplementationsCount() {
        return names.length;
    }

    public String getImplementationName( int imp ) {
        return names[imp];
    }
    
    public int getProblemsCount() {
        return depths.length;
    }

    public String getProblemName( int problem ) {
        return "n=" + depths[problem];
    }
    
    public TestCase getTestCase( int imp, int problem, int iterations ) {
	if( imp == 0 ) {
	    return new InternalTestCase( depths[problem], iterations ) {
		public Object createObject() {
		    return new Object();
		}
	    };
	} else if( imp == 1 ) {
	    return new InternalTestCase( depths[problem], iterations ) {
		public Object createObject() {
		    return new Exception();
		}
	    };
	} else {
	    return new InternalTestCase( depths[problem], iterations ) {
		public Object createObject() throws Exception {
		    throw new Exception();
		}
	    };
	}
    }

    

    private static final Object create( int depth, InternalTestCase itc ) throws Exception {
	if( depth == 0 ) return itc.createObject();
	return create( depth-1, itc );
    }


    /* -------------------- Test implementation -------------------- */
    public long doTest( TestCase tc ) {
        InternalTestCase itc = (InternalTestCase)tc;
	
	int iterations = itc.getIterations();
	int depth = itc.getDepth();
        
        TestBed.cooling();
        
        long time = System.currentTimeMillis();
        for( int iter = 0; iter < iterations; iter++ ) {
	    try {
		Object o = create( depth, itc );
	    } catch( Exception e ) {}
        }
        time = System.currentTimeMillis() - time;
        
        return time;
    }
    
    private static abstract class InternalTestCase implements TestCase {
        int depth;
        int repeats;
        
        public InternalTestCase( int depth, int repeats ) {
            this.depth = depth;
            this.repeats = repeats;
        }
        
        public int getDepth() {
            return depth;
        }
	
	public int getIterations() {
	    return repeats;
	}
	
	public abstract Object createObject() throws Exception;
    }

    public static void main( String[] args ) {
        TestBed.doTest( new ExceptionConstruct() );
    }
    
}
