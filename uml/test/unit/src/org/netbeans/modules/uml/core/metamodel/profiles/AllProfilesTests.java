
/*
 * Created on Sep 25, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.profiles;

import junit.framework.Test;
import junit.framework.TestSuite;
/**
 * @author aztec
 *
 */
public class AllProfilesTests
{
	public AllProfilesTests()
	{
		super();
	}
	
	public static Test suite()
	{
	   TestSuite suite = new TestSuite("Profiles Tests");
	   
	   suite.addTest(new TestSuite(ProfileApplicationTestCase.class));
	   suite.addTest(new TestSuite(StereotypeTestCase.class));
	   
	   return suite;
	}
	
	public static void main(String args[]) 
	{
		junit.textui.TestRunner.run(suite());
	}
}



