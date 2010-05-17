/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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



