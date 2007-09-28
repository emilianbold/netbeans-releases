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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.typemanagement.IPickListManager;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.applicationmanager.ApplicationManagerResource;
import org.netbeans.modules.uml.ui.support.messaging.IPickListDialog;
import org.netbeans.modules.uml.ui.support.messaging.PickListDialog;

/**
 * @author sumitabhk
 *
 */
public class NameCollisionHandler implements INameCollisionHandler
{
	/**
	 * 
	 */
	public NameCollisionHandler()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INameCollisionHandler#onPreNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public long onPreNameCollision(INamedElement pElement, String sProposedName, ETList<INamedElement> pCollidingElements, IResultCell pCell)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INameCollisionHandler#onNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public long onNameCollision(INamedElement pElement, ETList<INamedElement> pCollidingElements, IResultCell pCell)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INameCollisionHandler#listenerDisabled()
	 */
	public long listenerDisabled()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INameCollisionHandler#onPreAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public long onPreAliasNameModified(INamedElement element, String proposedName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INameCollisionHandler#onAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public long onAliasNameModified(INamedElement element, IResultCell cell)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Returns true if the input element does not have a valid name
	 */
	protected boolean isElementUnnamed(INamedElement pElement )
	{
		boolean bIsElementUnnamed = false;

		if ( pElement != null )
		{
			String elName = pElement.getName();
			if(elName == null || elName.length() == 0 ||	elName.equals(" "))
			{
				bIsElementUnnamed = true;
			}
			else
			{
				String defaultName = "";
				IPreferenceAccessor cpPref = PreferenceAccessor.instance();
				if (cpPref != null)
				{
					defaultName = cpPref.getDefaultElementName();
				}

				if(elName.equals(defaultName))
				{
					bIsElementUnnamed = true;
				}
			}
		}

		return bIsElementUnnamed;
	}
	
	/**
	 * Uses the picklist manager to get a list of class names
	 */
	protected ETPairT<IStrings, IPickListManager> getPickListNames(IElement pElement)
	{
		ETPairT<IStrings, IPickListManager> retVal = new ETPairT<IStrings, IPickListManager>();
		IStrings ppNames = null;
		IPickListManager ppPickMgr = null;
		
		IProject cpProject = pElement.getProject();
		if(cpProject != null)
		{
			ITypeManager cpTypeMgr = cpProject.getTypeManager();
			if (cpTypeMgr != null)
			{
				IPickListManager cpPickMgr = cpTypeMgr.getPickListManager();
				if (cpPickMgr != null)
				{
					String bsType = pElement.getElementType();
					if(bsType != null && bsType.length() > 0)
					{
						ppNames = cpPickMgr.getTypeNamesOfType(bsType);
					}
					ppPickMgr = cpPickMgr;
				}
			}
		}
			
		retVal.setParamOne(ppNames);
		retVal.setParamTwo(ppPickMgr);
		return retVal;
	}
	
	public ETPairT<Integer, INamedElement> handlePreAliasNameModified(INamedElement ppElement, String bsProposedName )
	{
		int choice = UserAliasChoice.UAC_UNKNOWN;
		INamedElement tempElement = null;

		if(isElementUnnamed(ppElement))
		{
			IPickListDialog cpPickListDialog = new PickListDialog();
			if(cpPickListDialog != null)
			{
				String bsType = ppElement.getElementType();

				ETPairT<IStrings, IPickListManager> val = getPickListNames(ppElement);
				IStrings cpStrings = val.getParamOne();
				IPickListManager cpPickMgr = val.getParamTwo();
				
				if(cpStrings != null)
				{
					cpPickListDialog.addPickListItems(cpStrings);
				}

				String bsTitle = ApplicationManagerResource.getString("IDS_NAME_ELEMENT_TITLE");
				String bsText = "";
				if(bsType != null && bsType.length() > 0)
				{
					bsText = ApplicationManagerResource.getString("IDS_NAME_ELEMENT_WNAME");
					bsText = StringUtilities.replaceSubString(bsText, "%s", bsType);
				}
				else
				{
					bsText = ApplicationManagerResource.getString("IDS_NAME_ELEMENT");
				}

//				ETPairT<Integer, String>  dispalyVal = cpPickListDialog.display(bsTitle, bsText, SimpleQuestionDialogKind.SQDK_OKCANCEL, SimpleQuestionDialogResultKind.SQDRK_RESULT_OK, bsProposedName, null);
				ETPairT<Integer, String>  dispalyVal = cpPickListDialog.display(bsTitle,
                                        bsText, 
                                        SimpleQuestionDialogResultKind.SQDRK_RESULT_OK, 
                                        bsProposedName);
                                
                                int nResult = ((Integer)dispalyVal.getParamOne()).intValue();
				String bsNewElementName = (String)dispalyVal.getParamTwo();
				if(SimpleQuestionDialogResultKind.SQDRK_RESULT_OK == nResult )
				{
					// Fix W7899 we need to handle the case where the name already is an existing element
					boolean bIsInList = false;

					if (cpStrings != null)
					{
						bIsInList = cpStrings.isInList(bsNewElementName, true);
					}

					if(bIsInList)
					{

						IElement cpElement = cpPickMgr.getElementByNameAndType(bsNewElementName, bsType);

						// Just because the name is in the list doesn't mean there's an element tied to it.
						// So we need to handle a null element as if they typed in a new name.  See 9592.
						if(cpElement != null)
						{
							// The input model element must be deleted here, and
							// any presentation elements need to be reattached
							//ppElement = (INamedElement)cpElement;
							tempElement = (INamedElement)cpElement;
							choice = UserAliasChoice.UAC_CHANGED_ELEMENT;
						}
						else
						{
							ppElement.setName(bsNewElementName);
							choice = UserAliasChoice.UAC_NAMED_ELEMENT;
						}
					}
					else
					{
						ppElement.setName(bsNewElementName);
						choice = UserAliasChoice.UAC_NAMED_ELEMENT;
					}
				}
				else
				{
					choice = UserAliasChoice.UAC_CANCEL;
				}
			}
		}

		ETPairT<Integer, INamedElement> retVal = new ETPairT<Integer, INamedElement>();
		retVal.setParamOne(new Integer(choice));
		retVal.setParamTwo(tempElement);
		return retVal;
	}
}



