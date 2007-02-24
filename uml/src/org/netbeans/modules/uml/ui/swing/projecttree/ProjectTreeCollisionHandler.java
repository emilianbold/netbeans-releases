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
 * Created on Jun 6, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.projecttree;

import java.util.Vector;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NameCollisionHandler;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
/**
 * @author sumitabhk
 *
 */
public class ProjectTreeCollisionHandler extends NameCollisionHandler implements IProjectTreeCollisionHandler
{
	private JProjectTree m_rawProjectTreeControl = null;
	/**
	 * 
	 */
	public ProjectTreeCollisionHandler()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditorCollisionHandler#getPropertyEditor()
	 */
	public JProjectTree getProjectTree()
	{
		return m_rawProjectTreeControl;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditorCollisionHandler#setPropertyEditor(org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor)
	 */
	public void setProjectTree(JProjectTree value)
	{
		m_rawProjectTreeControl = value;
	}

	/**
	 * Notification that a name collision event is about to happen
	 */
	public long onPreNameCollision(INamedElement pElement, String sProposedName, ETList<INamedElement> pCollidingElements, IResultCell pCell)
	{
		// Get the first colliding element
		INamedElement pFirstCollidingElement = null;
		if (pCollidingElements != null)
		{
			int count = pCollidingElements.getCount();
			if (count > 0)
			{
				pFirstCollidingElement = pCollidingElements.get(0);
			}
		}

		if(pFirstCollidingElement != null)
		{
			if (m_rawProjectTreeControl != null)
			{
				m_rawProjectTreeControl.questionUserAboutNameCollision(pElement,
																									sProposedName,
																									pCollidingElements,
																									pCell);
			}
		}
		
		return 0;
	}
	
	/**
	 * Notification that a name collision event occurred
	 */
	public long onNameCollision(INamedElement pElement, ETList<INamedElement> pCollidingElements, IResultCell pCell)
	{
		// Get the first colliding element
		INamedElement pFirstCollidingElement = null;
		if (pCollidingElements != null)
		{
			int count = pCollidingElements.getCount();
			if (count > 0)
			{
				pFirstCollidingElement = pCollidingElements.get(0);
			}
		}

		if(pFirstCollidingElement != null)
		{
			if (m_rawProjectTreeControl != null)
			{
				m_rawProjectTreeControl.findAndSelectInTree(pFirstCollidingElement);
			}
		}
		return 0;
	}
	/**
	 * Notification that the parent listener was disabled
	 */	
	public long listenerDisabled()
	{
		return 0;
	}

	public long onPreAliasNameModified(INamedElement element, String proposedName, IResultCell cell)
	{
		/*
      CComPtr< INamedElement > cpNamedElement( element );
      const UserAliasChoice choice = HandlePreAliasNameModified( &(cpNamedElement.p), proposedName );

      switch( choice )
      {
      case UAC_CANCEL:
         _VH( cell->put_Continue( VARIANT_FALSE ));
         break;

      case UAC_NAMED_ELEMENT:
         // do nothing, the element's name changed
         break;

      case UAC_CHANGED_ELEMENT:
         // The input model element must be deleted here.

         _VH( element->Delete() );
         break;

      default:
         break;
      }
		*/
		return 0;
	}

}




