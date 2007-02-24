package org.netbeans.modules.uml.core.coreapplication;

import org.netbeans.modules.uml.core.coreapplication.CoreProductInitEventsAdapter;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 *
 */
public class TestCoreProductChangeListener extends CoreProductInitEventsAdapter
    implements ICoreProductInitEventsSink
{
	public TestCoreProductChangeListener()
	{
		super();
	}

	public void onCoreProductInitialized(ICoreProduct newVal, IResultCell cell)
	{
		//ETSystem.out.println("Calling onCoreProductInitialized ");
		//call back method to indicate that this method has been called successfully
		CoreProductTestCase.callingInit = true;		
	}
	
	public void onCoreProductPreQuit(ICoreProduct prod, IResultCell payload)
	{
		//ETSystem.out.println("Calling onCoreProductPreQuit ");
		//call back method to indicate that this method has been called successfully
		CoreProductTestCase.callingPreQuit = true;
	}
	
	public void onCoreProductPreInit(ICoreProduct newVal, IResultCell cell)
	{
		//ETSystem.out.println("Calling OnCoreProductPreInit ");
		//call back method to indicate that this method has been called successfully
		CoreProductTestCase.callingPreInit = true;	
	}
}


