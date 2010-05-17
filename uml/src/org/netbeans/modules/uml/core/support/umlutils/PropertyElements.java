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
 * Created on Mar 12, 2004
 *
 */
package org.netbeans.modules.uml.core.support.umlutils;

import java.util.Vector;

import org.netbeans.modules.uml.common.generics.ETPairT;

/**
 * @author jingmingm
 *
 */
public class PropertyElements
{
	protected ETList<IPropertyElement> m_PropertyElements = new ETArrayList<IPropertyElement>();
	
	public PropertyElements()
	{
	}
	
	public PropertyElements(IPropertyElement[] pProperties)
	{
		for (int i = 0; i < pProperties.length; i++)
		{
			m_PropertyElements.add(pProperties[i]);
		}
	}
	
	public PropertyElements(ETList<IPropertyElement> pProperties)
	{
		m_PropertyElements = pProperties;
	}
	
	public PropertyElements(Vector<IPropertyElement> pProperties)
	{
		for (int i = 0; i < pProperties.size(); i++)
		{
			m_PropertyElements.add(pProperties.get(i));
		}
	}
	
	public IPropertyElement retrieveProperty(int index)
	{
		IPropertyElement pVal = null;
		if (index >= 0 && index < m_PropertyElements.size())
		{
			pVal = m_PropertyElements.get(index);
		}
		return pVal;
	}
	
	public ETList<IPropertyElement> getList()
	{
		return m_PropertyElements;
	}
	
	/**
	 * Is this element in the list?
	 */
	public boolean isInList (IPropertyElement pElement)
	{
		boolean bIsInList = false;
		
		int count = m_PropertyElements.size();
		for (int i = 0 ; i < count; i++)
		{
			IPropertyElement pThisElement = m_PropertyElements.get(i);
			if (pThisElement != null && pThisElement.equals(pElement))
			{
				bIsInList = true;
				break;
			}
		}
		return bIsInList;
	}

	/**
	 * Adds if this item is not already in the list
	 */
	public void addIfNotInList(IPropertyElement pElement)
	{
		if (pElement != null)
		{
			boolean bIsInList = isInList(pElement);
			if (!bIsInList)
			{
				m_PropertyElements.add(pElement);
			}
		}
	}
	
	/**
	 * Returns an IPropertyElement of the path and name, recursive check.  If path is "" then just name is checked.
	 */
	public IPropertyElement getElementRecursive(String sPath, 
												String sName, 
												boolean bRemoveFromList)
	{
		IPropertyElement pElement = null;

		// See if it's in our immediate children
		pElement = getElement(sPath,sName,bRemoveFromList);

		if (pElement == null)
		{
			// Search our children's children
			int count = m_PropertyElements.size();
			for (int i = 0 ; i < count ; i++)
			{
				IPropertyElement pThisElement = m_PropertyElements.get(i);
				if (pThisElement != null)
				{
					Vector<IPropertyElement> pSubElements = pThisElement.getSubElements();
					PropertyElements tempPropertyElements = new PropertyElements(pSubElements);
					if (tempPropertyElements != null)
					{
						pElement = tempPropertyElements.getElementRecursive(sPath,sName,bRemoveFromList);
						if (pElement != null)
						{
							// Break if we find it.
							break;
						}
					}
				}
			}
		}

		return pElement;
	}
	
	/**
	 * Returns an IPropertyElement of the path and name.  If path is "" then just name is checked.
	 */
	public IPropertyElement getElement( String sPath, 
										String sName, 
										boolean bRemoveFromList)
	{
		IPropertyElement pElement = null;

		if (sName != null && sName.length() > 0)
		{
			int count = m_PropertyElements.size();
			for (int i = 0 ; i < count ; i++)
			{
				IPropertyElement pThisElement = m_PropertyElements.get(i);
				if (pThisElement != null)
				{
					String sThisName = pThisElement.getName();
					if (sName.equals(sThisName))
					{
						if (sPath != null && sPath.length() > 0)
						{
							// If path is provided check that.  If not then
							// continue and go to the next element.
							String sThisPath = pThisElement.getPath();
							if (!(sPath.equals(sThisPath)))
							{
								continue;
							}
						}

						// We found the element
						pElement = pThisElement;

						// Remove if we're told to
						if (bRemoveFromList)
						{
							m_PropertyElements.remove(i);
						}

						break;
					}
				}
			}
		}
		
		return pElement;
	}

	/**
	 * Returns any property elements that represents colors.
	 */
	public ETPairT<ETList<IPropertyElement>, ETList<IPropertyElement> > getColorsAndFonts(boolean bRemoveFromList)
	{
		PropertyElements pColorElements = new PropertyElements();
		PropertyElements pFontElements = new PropertyElements();
		ETList<Integer> itemsToRemove = new ETArrayList <Integer>();

		int count = m_PropertyElements.size();

		// Colors and fonts are under "Presentation" hive in
		// the preferences.  They may have an intermediate parent
		// which is their name (ie "ClassFont") the grandparent which
		// would be "Classes" and the great grandparent which is
		// "Presentation".  Note that most fonts are in this order, but
		// not all so if any of the names above are "Presentation" then
		// consider this a presentation types font and make a change.
		for (int i = 0 ; i < count ; i++)
		{
			IPropertyElement pThisElement = m_PropertyElements.get(i);
			if (pThisElement != null)
			{
				IPropertyElement pParentElement = null;
				IPropertyElement pGrandParentElement = null;
				IPropertyElement pGreatGrandParentElement = null;
				String sThisName = pThisElement.getName();
				String sParentName = "";
				String sGrandParentName = "";
				String sGreatGrandParentName = "";

				pParentElement = pThisElement.getParent();
				if (pParentElement != null)
				{
					sParentName = pParentElement.getName();
					pGrandParentElement = pParentElement.getParent();
					if (pGrandParentElement != null)
					{
						sGrandParentName = pGrandParentElement.getName();
						pGreatGrandParentElement = pGrandParentElement.getParent();
						if (pGreatGrandParentElement != null)
						{
							sGreatGrandParentName = pGreatGrandParentElement.getName();
						}
					}
				}

				if (sParentName.equals("Presentation") || 
					sGrandParentName.equals("Presentation") || 
					sGreatGrandParentName.equals("Presentation"))
				{
					// We have a color or a font. See if the name of this
					// is one of the font types
					if (sThisName.equals("CharSet") ||
						sThisName.equals("FaceName") ||
						sThisName.equals("Height") ||
						sThisName.equals("Italic") ||
						sThisName.equals("Strikeout") ||
						sThisName.equals("Underline") ||
						sThisName.equals("Weight") ||
						sThisName.equals("Color") )
					{
						// Its a font.  Add the parent so if the user changes multiple font
						// properties we just return the parent as the one that changed.
						pFontElements.addIfNotInList(pParentElement);

						itemsToRemove.add(new Integer(i));
					}
					else
					{
						// Assume it's a color
						pColorElements.addIfNotInList(pThisElement);

						itemsToRemove.add(new Integer(i));
					}
				}
			}
		}

		// Now remove if we're told to
		if (bRemoveFromList)
		{
			for (int j = 0; j < itemsToRemove.size(); j++)
			{
				Integer nIndex = itemsToRemove.get(j);
				m_PropertyElements.remove(nIndex.intValue());
			}
		}

		ETPairT<ETList<IPropertyElement>, ETList<IPropertyElement> > retVal = new ETPairT<ETList<IPropertyElement>, ETList<IPropertyElement> >();
		retVal.setParamOne(pColorElements.getList());
		retVal.setParamTwo(pFontElements.getList());

		return retVal;
	}
	
	/**
	 * Remove this element from the collection
	 */
	public void remove2(IPropertyElement pToBeRemoved)
	{
		if (pToBeRemoved != null)
		{
			int count = m_PropertyElements.size();
			String sName = pToBeRemoved.getName();
			for (int i = 0; i < count; i++)
			{
				IPropertyElement pThisElement = m_PropertyElements.get(i);
				if (pThisElement != null)
				{
					String sThisName = pThisElement.getName();
					if (sName.equals(sThisName))
					{
						m_PropertyElements.remove(i);
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Remove these elements from the collection
	 */
	public void removeThese(PropertyElements pToBeRemoved)
	{
		ETList<IPropertyElement> toBeRemovedList = pToBeRemoved.getList();
		int count = toBeRemovedList.size();
		for (int i = 0 ; i < count ; i++)
		{
			IPropertyElement pPropertyElement = toBeRemovedList.get(i);
			if (pPropertyElement != null)
			{
				remove2(pPropertyElement);
			}
	   }
	}
}



