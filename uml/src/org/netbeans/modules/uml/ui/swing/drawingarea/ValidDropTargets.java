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



package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.dom.DOMDocument;
import java.util.List;

/**
 * @author KevinM
 *
 * This class hides the complexity of looking up valid ElementType Diagram Drop Targets.
 */
public class ValidDropTargets {

	/**
	 *
	 */
	protected ValidDropTargets() {
		super();
		fill();
	}
	
	public static ValidDropTargets instance()
	{
		if (m_Instance == null)
		{
			m_Instance = new ValidDropTargets();
		}
		return m_Instance;
	}

	/*
	 * Returns true if the input element type is allowed on the diagram.
	 */
	public boolean isValidDropTarget(String ElementType, String diagramShortName) 
	{
		if (m_ValidDropDiagrams != null)
		{
			String vaildDropTargets = (String) m_ValidDropDiagrams.get(ElementType);
			if (vaildDropTargets != null) 
			{
				return vaildDropTargets.indexOf(diagramShortName) >= 0 || vaildDropTargets.indexOf("ALL") >= 0;
			}
		}
		return false;
	}

	protected void fill() {
		try {
			IConfigManager config = ProductHelper.getConfigManager();

			if (config != null) 
			{
				String location = config.getDefaultConfigLocation();
				location += "ProjectTreeEngine.etc";

				Document pDocument = XMLManip.getDOMDocument(location);
				if (pDocument != null) {
					// Query the displayed items
					String query = new String("//DisplayedItems");

					List pDisplayedItems = pDocument.selectNodes(query);
					if (pDisplayedItems != null) 
					{
						Iterator < Node > domNodeIter = pDisplayedItems.iterator();
						while (domNodeIter.hasNext()) 
						{
							Node pNode = domNodeIter.next();

							Element pElement = pNode instanceof Element ? (Element) pNode : null;
							if (pElement != null) 
							{
								String name = pElement.attributeValue("name");
								String validDropDiagrams = pElement.attributeValue("dragAndDropDiagrams");

								if (name != null && validDropDiagrams != null) 
								{
									if (validDropDiagrams.length() > 0 && !validDropDiagrams.equals(",,")) 
									{
										m_ValidDropDiagrams.put(name, validDropDiagrams);
									}
								}
							}
						}
					}
				} else {
					//  UMLMessagingHelper messageService(_Module.GetModuleInstance(), IDS_MESSAGINGFACILITY);
					//  _VH(messageService.SendWarningMessage(_Module.GetModuleInstance(), IDS_COULDNOTLOADENGINE ) );
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/// Identifies the valid drop diagram for this element type
	/* The element type */ /* Comma delimited list of drop diagrams */;
	protected HashMap m_ValidDropDiagrams = new HashMap();
	private static ValidDropTargets m_Instance = null;

}
