
package org.netbeans.modules.uml.core.coreapplication;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllCoreApplicationTests
{

	public static Test suite()
	{
	   TestSuite suite = new TestSuite("CoreApplication Tests");
      
	   //$JUnit-BEGIN$
	   suite.addTest(new TestSuite(CoreProductManagerTestCase.class));
	   suite.addTest(new TestSuite(CoreProductTestCase.class));	   
	   //$JUnit-END$
	   return suite;
	}
	
	public static void main(String args[]) 
	{
		junit.textui.TestRunner.run(suite());
	}

}


