package org.netbeans.modules.uml.core.eventframework;

public class EventFrameworkTestListener1 
{

	protected static boolean isCalled;
	protected static String functRet = null;;
	public void testMethod1()
	{
		isCalled = true;
	}
	
	public void testMethod2(String str)
	{
		functRet = str + "world";  
	}
}
