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


/*
 * Created on Jun 6, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NameCollisionHandler;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UserAliasChoice;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
/**
 * @author sumitabhk
 *
 */
public class DrawingAreaCollisionHandler extends NameCollisionHandler implements IDrawingAreaCollisionHandler
{
	private ADDrawingAreaControl m_rawDrawingAreaControl = null;
	private ICompartment m_CompartmentBeingEdited = null;

	/**
	 * 
	 */
	public DrawingAreaCollisionHandler()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditorCollisionHandler#getPropertyEditor()
	 */
	public ADDrawingAreaControl getDrawingArea()
	{
		return m_rawDrawingAreaControl;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditorCollisionHandler#setPropertyEditor(org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor)
	 */
	public void setDrawingArea(ADDrawingAreaControl value)
	{
		m_rawDrawingAreaControl = value;		
	}

	public ICompartment getCompartment()
	{
		return m_CompartmentBeingEdited;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditorCollisionHandler#setPropertyEditor(org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor)
	 */
	public void setCompartment(ICompartment value)
	{
		m_CompartmentBeingEdited = value;
	}

	/**
	 * Notification that a name collision event is about to happen
	 */
	public long onPreNameCollision(INamedElement pElement, String sProposedName, ETList<INamedElement> pCollidingElements, IResultCell pCell)
	{
		if(pElement != null)
		{
			if (m_rawDrawingAreaControl != null)
			{
				m_rawDrawingAreaControl.questionUserAboutNameCollision(m_CompartmentBeingEdited,
                                        pElement,
                                        sProposedName,
                                        pCollidingElements,
                                        pCell);
}
		}
		// Deselect everything so the property editor doesn't show the incorrect item
		if (m_rawDrawingAreaControl != null && pCell != null)
		{
			boolean bContinue = pCell.canContinue();
			m_rawDrawingAreaControl.selectAll(false);
		}
		
		return 0;
	}
	/**
	 * Notification that a name collision event occurred
	 */
	public long onNameCollision(INamedElement pElement, ETList<INamedElement> pCollidingElements, IResultCell pCell)
	{
		return 0;
	}
	/**
	 * Notification that the parent listener was disabled
	 */	
	public long listenerDisabled()
	{
		m_CompartmentBeingEdited = null;
		return 0;
	}

	/**
	 * Fired whenever the alias name of the passed in element is about to change.
	 */
	public long onPreAliasNameModified(INamedElement element, String proposedName, IResultCell cell)
	{
		INamedElement cpNamedElement = element;
		ETPairT<Integer, INamedElement> val = handlePreAliasNameModified(cpNamedElement, proposedName);
      int choice = ((Integer)val.getParamOne()).intValue();

      switch( choice )
      {
      case UserAliasChoice.UAC_CANCEL:
         cell.setContinue(false);
         break;

      case UserAliasChoice.UAC_NAMED_ELEMENT:
         // do nothing, the element's name changed
         break;

      case UserAliasChoice.UAC_CHANGED_ELEMENT:
         // The input model element must be deleted here.

         if( m_CompartmentBeingEdited != null && m_rawDrawingAreaControl != null)
         {
            IDiagramEngine cpDiagramEngine = m_rawDrawingAreaControl.getDiagramEngine();
            if(cpDiagramEngine != null)
            {
					element = (INamedElement)val.getParamTwo();
//               cpDiagramEngine.handlePresentationElementReattach(m_CompartmentBeingEdited,
//                                                                 element,
//                                                                 cpNamedElement);

               // Now, the original input model element must be deleted here.
//               element.delete();
//               element = null;
//
//               cpNamedElement.setAlias(proposedName);
//               cell.setContinue(false);
					cpDiagramEngine.handlePresentationElementReattach(m_CompartmentBeingEdited,
					cpNamedElement,
					element);
					cpNamedElement.delete();
					cpNamedElement = null;

					element.setAlias(proposedName);
					cell.setContinue(false);
            }
         }
         break;

      default:
         break;
      }
		return 0;
	}

}




