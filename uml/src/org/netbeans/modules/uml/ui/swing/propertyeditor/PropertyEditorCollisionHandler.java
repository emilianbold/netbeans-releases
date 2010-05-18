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


/*
 * Created on Jun 6, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.propertyeditor;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NameCollisionHandler;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.ui.controls.newdialog.NewElementUI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


/**
 * @author sumitabhk
 *
 */
public class PropertyEditorCollisionHandler extends NameCollisionHandler implements IPropertyEditorCollisionHandler
{
	private IPropertyEditor m_rawPropertyEditorControl = null;
	private boolean handled = false;
	private IPropertyElement pPropertyElement;
	/**
	 * 
	 */
	public PropertyEditorCollisionHandler()
	{
		super();
	}
	
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditorCollisionHandler#getPropertyEditor()
	 */
	public IPropertyEditor getPropertyEditor()
	{
		return m_rawPropertyEditorControl;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditorCollisionHandler#setPropertyEditor(org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor)
	 */
	public void setPropertyEditor(IPropertyEditor value)
	{
		m_rawPropertyEditorControl = value;
	}

	public void setPropertyElement(IPropertyElement element)
	{
		pPropertyElement = element;
	}
	
	public IPropertyElement getPropertyElement()
	{
		return pPropertyElement;
	}
	
	
	/**
	 * Notification that a name collision event is about to happen
	 */
	public long onPreNameCollision(INamedElement pElement, String sProposedName, ETList<INamedElement> pCollidingElements, IResultCell pCell)
	{
		if (!handled)
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

			questionUserAboutNameCollision(pElement,
											sProposedName,
											pFirstCollidingElement,
											pCell);

			handled = true;
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

//		if(pFirstCollidingElement != null)
//		{
//			if (m_rawPropertyEditorControl != null)
//			{
//				m_rawPropertyEditorControl.loadElement(pFirstCollidingElement);
//			}
//		}
		
		loadElement(pFirstCollidingElement);
		
		return 0;
	}
	/**
	 * Notification that the parent listener was disabled
	 */	
	public long listenerDisabled()
	{
		return 0;
	}

	/**
	 * Reattaches the presentation element to a new model element
	 */
	protected void reattachPresentationElement(INamedElement pElement, INamedElement pFirstCollidingElement)
	{
		if (pElement != null && pFirstCollidingElement != null)
		{
			if (getPropertyElement() != null)
			{
				Object pDispElement = getPropertyElement().getElement();
				if (pDispElement instanceof IPresentationElement)
				{
					IPresentationElement pPE = (IPresentationElement)pDispElement;
					boolean bIsSubject = pPE.isSubject(pElement);
					if (bIsSubject)
					{	
						// This presentation element should be reattached.
                                            // TODO: meteora
//						if ( pPE instanceof IProductGraphPresentation)
//						{
//							IProductGraphPresentation pGraphPresentation = (IProductGraphPresentation)pPE;
//							// Reconnect the presentation element
//							pGraphPresentation.reconnectPresentationElement(pFirstCollidingElement);
//						}
					}
				}
			}
		}
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
			{
				CComPtr < IPropertyElements > pPropertyElements;
				long count = 0;
				if (m_rawPropertyEditorControl)
				{
					_VH(m_rawPropertyEditorControl->get_PropertyElements(&pPropertyElements));
					if (pPropertyElements)
					{
						_VH(pPropertyElements->get_Count(&count));
					}

					for (long i = 0 ; i < count ; i++)
					{
						CComPtr < IPropertyElement > pThisPropertyElement;
						_VH(pPropertyElements->Item(CComVariant(i), &pThisPropertyElement));
						if (pThisPropertyElement)
						{
							CComPtr < IDispatch > pDispElement;
							VARIANT_BOOL bIsSubject = VARIANT_FALSE;
							_VH(pThisPropertyElement->get_Element(&pDispElement));

							CComQIPtr < IProductGraphPresentation > pThisPE(pDispElement);
							if (pThisPE)
							{
								_VH(pThisPE->IsSubject(element, &bIsSubject));

								if (bIsSubject)
								{
									// Need to reattach this presentation element to the new element
									_VH(pThisPE->ReconnectPresentationElement(cpNamedElement));
								}
							}
						}
					}
				}

				// Delete the old element
				_VH( element->Delete() );
			}
			break;

		default:
			break;
		}
		*/
		return 0;
	}

	/**
	 * Ask the user what to do about a name collision
	 *
	 * @param pElement [in] The element being renamed
	 * @param sProposedName [in] The new name
	 * @param pFirstCollidingElement [in] The first colliding element
	 * @param pCell [in] The result cell.  Used to cancel the rename.
	 */
	public long questionUserAboutNameCollision(INamedElement pElement, String sProposedName, INamedElement pFirstCollidingElement, IResultCell pCell)
	{
		if (pFirstCollidingElement != null && pElement != null && pCell != null)
		{
			
			DialogDisplayer.getDefault().notify(
						new NotifyDescriptor.Message(NbBundle.getMessage(
								NewElementUI.class, "IDS_NAMESPACECOLLISION")));
			// Cancel the editing to abort the name collision
			pCell.setContinue(false);
			// refresh the property to revert back to the original value
			Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
			if (activatedNodes != null)
			{             
				int arrayLength = activatedNodes.length;
				for (int i = 0; i < arrayLength; i++)
				{
				 // The setDisplayName causes the property sets to be updated.
				 Node myNode = activatedNodes[i];
				 myNode.setDisplayName(myNode.getDisplayName());
				}
			}
			
//			// Ask the user if he wants to reconnect the presentation element to a different model element
//			IQuestionDialog pDiag = new SwingQuestionDialogImpl();
//			if ( pDiag != null )
//			{
//				String title = PropertyEditorResources.getString("PropertyEditor.NameCollisionTitle");
//				String msg = PropertyEditorResources.getString("PropertyEditor.NameCollision");
//				QuestionResponse result = pDiag.displaySimpleQuestionDialog(SimpleQuestionDialogKind.SQDK_YESNOCANCEL, MessageIconKindEnum.EDIK_ICONWARNING, msg, 0, null, title);
//				if (result.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES)
//				{
//					// TODO: User wants to allow the name collision.
//					reattachPresentationElement(pElement, pFirstCollidingElement);
//				}
//				else if (result.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_CANCEL)
//				{
//					// Cancel the editing to abort the name collision
//					pCell.setContinue(false);
//					// refresh the property to revert back to the original value
//					Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
//					if (activatedNodes != null)
//					{             
//						int arrayLength = activatedNodes.length;
//						for (int i = 0; i < arrayLength; i++)
//						{
//						 // The setDisplayName causes the property sets to be updated.
//						 Node myNode = activatedNodes[i];
//						 myNode.setDisplayName(myNode.getDisplayName());
//						}
//					}
//				}
//				else if (result.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_NO)
//				{
//					// go ahead with the same name
//				}
//			}
		}
		return 0;
	}

	
	/**
	 * Load the passed in IElement into the property editor
	 *
	 * @param pElement[in]		The element to put into the editor
	 *
	 * @return HRESULT
	 */
	public long loadElement(IElement pElement)
	{
//		if (PropertyEditorBlocker.inProcess() == false /*&& m_Model != null*/)
//		{
//			processLastCell(true);
//			clear();
//			//
//			// the property editor is loaded by creating property definitions(which
//			// represent the structure of the data) and property elements(which represent
//			// the actual data)
//			//
//			String kind = ""; //$NON-NLS-1$
//			if (pElement instanceof IDiagram)
//			{
//				kind = "Diagram"; //$NON-NLS-1$
//			}
//			
//			Vector<IPropertyElement> propElems = new Vector<IPropertyElement>();
//			Vector<IPropertyDefinition> propDefs = new Vector<IPropertyDefinition>();
//			IPropertyElement pEle = processSelectedItem(kind, propDefs, pElement);
//			if (pEle != null)
//			{
//				// since we are coming from the drawing area, we will also want the
//				// capability of showing the presentation information, so store the
//				// presentation element on the property element
//				pEle.setElement(pElement);
//				propElems.add(pEle);
//			}
//			setPropertyDefinitions(propDefs);
//			setPropertyElements(propElems);
//			m_CurLoadedObject = pElement;
//		}
		// TODO Auto-generated method stub
		return 0;
	}
}




