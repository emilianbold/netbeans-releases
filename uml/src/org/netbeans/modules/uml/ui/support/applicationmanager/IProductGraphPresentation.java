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



package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;


public interface IProductGraphPresentation extends IGraphPresentation
{
	/*
	 * Get the product element attached to this presentation element
	 */ 
	public IElement getElement();
  
	/*
	* Is this item the same as the passed in one.
	 */ 
	public boolean isModelElement(IElement pQueryItem);
	  
	/*
	* Returns the name of the TS object
	*/
	public String getName();
	
	/*
	* Sets the name of the TS object
	*/	
	public void setName(String name);

	/*
	* Returns the bounding rectangle for this TS object
	*/	
	public IETRect getBoundingRect();

	/*
	 * Returns the view bounding rectangle for this TS object, unioning any label bounding rects
	 */
	public IETRect viewBoundingRect();
	
	/*
	 * Returns the IDiagram interface.
	 */
	 public IDiagram getDiagram();

	/*
	 * The synch state of this element, SynchStateKind
	 */
	public int getSynchState();

	/*
	 *  SynchStateKind newVal
	 */
	public void setSynchState(int SynchStateKind);

	/*
	 * Synchronizes this element with its data.
	 */
	public boolean performDeepSynch();
                                   
	/*
	 * Returns the label manager for this node or edge
	 */
	public ILabelManager getLabelManager();
                                   
	/*
	 * Returns the edge manager for this node
	 */
	public IEventManager getEventManager();
  
	/*
	 * Select or Deselect all the labels.
	 */
	public void selectAllLabels(boolean bSelect);

	/*
	 * Reconnects this presentation element to a new model element.
	 */
	public boolean reconnectPresentationElement(IElement pNewModelElement);
}
