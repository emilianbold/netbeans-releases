/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.testbed;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.UMLCreationFactory;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.LanguageManager;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.DataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElementManager;

/**
 * @author sumitabhk
 *
 */
public class TestLanguageManager
{

	/**
	 * 
	 */
	public TestLanguageManager()
	{
		super();
	}

	public static void main(String[] args)
	{
		try {
			ICoreProduct prod = ProductRetriever.retrieveProduct();
			IConfigManager conMan = new ConfigManager();
			if (prod != null)
			{
				prod.setConfigManager(conMan);
			}
			LanguageManager pMan = new LanguageManager();
			String str1 = pMan.getConfigLocation();
			ETSystem.out.println("got config location as " + str1);
		
			String str2 = pMan.getDefaultForLanguage("Java", "extension");
			ETSystem.out.println("got language default " + str2);
		
			DataFormatter formatter = new DataFormatter();

			UMLCreationFactory fact = new UMLCreationFactory();
			fact.setConfigManager(conMan);
			IClass clazz = fact.createClass(null);
			clazz.setName("Test1");
			clazz.setIsAbstract(false);
			
			IPropertyElementManager propMan = formatter.getElementManager();
			IPropertyElement ele = formatter.getPropertyElement(clazz);
			
			String str3 = formatter.formatElement(clazz);
			ETSystem.out.println("Got the formatted String as " + str3);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}



