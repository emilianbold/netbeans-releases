/*
 * ExceptionConstruct.java
 *
 * Created on 28. bre 2001, 17:44
 */

package org.netbeans.performance.platform;

import org.netbeans.performance.*;
import java.util.*;
import java.lang.reflect.Method;

/**
 * The benchmark comparing time to construct and fire an anonymous Runnable
 * versus time to construct a single named Runable passing it a method.
 *
 * @author  Petr Nejedly
 * @version 0.0
 */
public class RunnableMethod extends TestDescription implements Test {

    private static final String[] names = {
            "Runnable", "Delegating Runnable", "Method",
	    "Method cached data", "Cached Method", "Cached MethodRunner" };
            
    private static final int[] workloads = {
            1, 100, 10000 };

    public Test getTest() {
        return this;
    }
    
    public String getTestDescription() {
        return "Fires a Runnable by either creating real Runnable or by" +
	    " using special Runnable to run passed method with several" +
	    " degrees of caching";
    }
    
    public int getImplementationsCount() {
        return names.length;
    }

    public String getImplementationName( int imp ) {
        return names[imp];
    }
    
    public int getProblemsCount() {
        return workloads.length;
    }

    public String getProblemName( int problem ) {
        return "n=" + workloads[problem];
    }
    
    public TestCase getTestCase( int imp, int problem, int iterations ) {
	switch( imp ) {
	    case 0:  // Runnable
		return new InternalTestCase( workloads[problem], iterations ) {
		    public void doTheWork() {
			new Runnable() {
			    public void run() {
				for( int i=0; i < workload; i++ );
			    }
			}.run();
		    }
		};
	    case 1:  // Delegating Runnable
		return new InternalTestCase( workloads[problem], iterations ) {
		    public void doTheWork() {
			new Runnable() {
			    public void run() {
				worker();
			    }
			}.run();
		    }
		};
	    
	    case 2:  // Method
		return new InternalTestCase( workloads[problem], iterations ) {
		    public void doTheWork() {
			try {
			    Method mtd = getClass().getMethod( "worker", new Class[0] );
			} catch( Throwable t ) {
			    t.printStackTrace();
			};
			new MethodRunner( mtd, this, new Object[0] ).run();
		    }
		    
		};
		
	    case 3:  // Method cached data
		return new InternalTestCase( workloads[problem], iterations ) {
		    
		    public void doTheWork() {
			try {
			    Method mtd = getClass().getMethod( "worker", clsArray );
			} catch( Throwable t ) {
			    t.printStackTrace();
			}
			new MethodRunner( mtd, this, objArray ).run();
		    }
		    
		};

	    case 4:  // Cached Method
		return new InternalTestCase( workloads[problem], iterations ) {
		    public void doTheWork() {
			new MethodRunner( mtd, this, objArray ).run();
		    }		    
		};
		
	    case 5:  // Cached MethodRunner
		return new InternalTestCase( workloads[problem], iterations ) {
		    MethodRunner mr = new MethodRunner( mtd, this, objArray );
		    
		    public void doTheWork() {
			mr.run();
		    }
		    
		};
		
	    default:
		return null;
	}
    }

    

    /* -------------------- Test implementation -------------------- */
    public long doTest( TestCase tc ) {
        InternalTestCase itc = (InternalTestCase)tc;
	
	int iterations = itc.getIterations();
        
        TestBed.cooling();
        
        long time = System.currentTimeMillis();
        for( int iter = 0; iter < iterations; iter++ ) {
	    itc.doTheWork();
        }
        time = System.currentTimeMillis() - time;
        
        return time;
    }
    
    
    private static abstract class InternalTestCase implements TestCase {
        int workload;
        int repeats;
	static Class[] clsArray = new Class[0];
	static Object[] objArray = new Object[0];
	Method mtd;
        
        public InternalTestCase( int workload, int repeats ) {
            this.workload = workload;
            this.repeats = repeats;
	    try {
		mtd = getClass().getMethod( "worker", new Class[0] );
	    } catch( Throwable t ) {
		t.printStackTrace();
	    }
        }
        
	public int getIterations() {
	    return repeats;
	}
	
	public abstract void doTheWork();

        public void worker() {
	    for( int i=0; i < workload; i++ );
        }
    }

    public static final class MethodRunner implements Runnable {
	private Method mtd;
	private Object obj;
	private Object[] args;
    
	public MethodRunner( Method mtd, Object obj, Object[] args ) {
	    this.mtd = mtd;
	    this.obj = obj;
	    this.args = args;
	}
	
	public void run() {
	    try {
		mtd.invoke( obj, args );
	    } catch( Throwable t ) {
		t.printStackTrace();
	    }
	}
    }

    public static void main( String[] args ) {
	TestBed.doTest( new RunnableMethod() );
    }

}
