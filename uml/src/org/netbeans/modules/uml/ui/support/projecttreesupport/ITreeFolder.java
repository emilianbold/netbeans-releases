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
 * Created on Jun 12, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support.projecttreesupport;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

/**
 *
 * @author Trey Spiva
 */
public interface ITreeFolder extends ITreeItem
{

   /**
    * @param value The ID of the element
    */
   public void setID(String value);

   /**
    *
    * @return value The ID of the element
    */
   public String getID();

   /**
    * @param value
    */
   public void setName(String value);

   public String getName();

   /**
    * Sets display Name of the property definition.
    * 
    * @param value the display name.
    */
   public void setDisplayName(String value);

   /**
    * Sets display Name of the property definition.
    * 
    * @param value the display name.
    * @param initializing specifing that we are actually initalizing the node
    */
   public void setDisplayName(String value, boolean initializing);

   /**
    * Retrieves display Name of the property definition.
    * 
    * @return the display name.
    */
   public String getDisplayName();
   
   /**
    * Set the method to use to populate the children.
    * 
    * @param name The name of the method to be invoked.
    */
   public void setGetMethod(String name);

   /**
    * Retrieve the method to use to populate the children.
    * 
    * @return The name of the method to be invoked. 
    */
   public String getGetMethod();
   
   /**
    * Sets the element this folder represents.
    * 
    * @param pEle The model element.
    */
   public void setElement(IElement pEle);

   /**
    * Retrieves the element this folder represents.
    * 
    * @return  The model element.
    */
   public IElement getElement();
   
   /**
    * @param defPath
    */
   public void setPath(ITreeItem[] defPath);
   
   /**
    * Is the get method the one used to get imported packages or elements?
    * 
    * @return <b>true</b> if the get method is an import, <b>false</b>
    *         if the get method is not an import.
    */
   public boolean isGetMethodAnImport();
   
   /**
    * Is the get method the one used to get imported elements?
    * 
    * @return <b>true</b> if the get method is an import, <b>false</b>
    *         if the get method is not an import.
    */
   public boolean isGetMethodAnImportElement();
   
   /**
    * Is the get method the one used to get imported packages?
    * 
    * @return <b>true</b> if the get method is an import, <b>false</b>
    *         if the get method is not an import.
    */
   public boolean isGetMethodAnImportPackage();
   
   /**
	* The type of the tree element.
	* 
	* @return The type.
	*/
   public String getType();
   
}
