package org.netbeans.modules.uml.core.eventframework;

import org.dom4j.Document;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;

public class GetMechanismTestCase extends AbstractUMLTestCase
{
	EventDispatcher eventDispather;
	
	protected void setUp()
	{
		// Initializes the EventDispatcher
		eventDispather = new EventDispatcher();
	}
	
	
	public void testGetMechanism()
	{
			
		// Creates a new ConfigManager and sets it to the product
		IConfigManager conMan = new ConfigManager();
		product.setConfigManager(conMan);
		
		// Get the document that represents the event mechanism framework configuration file.
		// and tests its been retrieved correctly
		Document doc = eventDispather.getMechanism();
		assertNotNull(doc);
	}
	
}
