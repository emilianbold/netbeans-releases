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


/*
 *
 * Created on Jun 11, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support.projecttreesupport;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

/**
 *
 * @author Trey Spiva
 */
public interface ITreeElement extends ITreeItem
{

   /**
    * Get the element this item represents.
    *
    * @return The element.
    */
   public IElement getElement();

   /**
    * Set the element this item represents.
    *
    * @return The element.
    */
   public void setElement(IElement value);
   
   /** 
    * Get the XMIID of the element this node represents.
    * 
    * @return the XMI ID
    */
   public String getXMIID();
   
   /**
    * Get the element type for this node.
    * @return the type name.
    */
   public String getElementType();
   
   /**
    * Get the expanded element type for this node (used for icons).
    * 
    * @return The expanded type name.
    */
   public String getExpandedElementType();
   
   /**
	* The type of the tree element.
	* 
	* @return The type.
	*/
   public String getType();
   
   public Node getXMLNode();
   
   public boolean getTranslateName();
   public void setTranslateName(boolean val);
   
   public String getDisplayName();
   
}
