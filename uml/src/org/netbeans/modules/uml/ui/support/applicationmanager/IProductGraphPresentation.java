/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
