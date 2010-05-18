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


package org.netbeans.modules.uml.ui.support.contextmenusupport;

import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import javax.swing.JSeparator;
import org.netbeans.modules.uml.ui.swing.testbed.addin.menu.Separator;
import org.netbeans.modules.uml.ui.products.ad.application.action.BaseAction;
import org.netbeans.modules.uml.common.generics.ETPairT;

public class SorterHelper
{
    protected ETList< ETPairT<String, Integer> > m_TopSortItems = new ETArrayList< ETPairT<String, Integer> >();
    protected ETList< ETPairT<String, Integer> > m_BottomSortItems = new ETArrayList< ETPairT<String, Integer> >();
    
    /**
     * Sorts the context menu
     *
     * @param pMenuItems [in] The unsorted list of menu items
     * @param pSortedMenuItems [in,out] The menu buttons found in sortItems, extracted from pMenuItems
     * @param sortItems [in] The buttons in their desired sort orders.
     */
    public ETList<Object> extractSortButtons(IMenuManager pMenuManager, ETList< ETPairT<String, Integer> > sortItems)
    {
        ETList<Object> pSortedMenuItems = new ETArrayList<Object>();
        
        if (sortItems != null)
        {
            int count = sortItems.size();
            for (int i = 0; i < count; i++)
            {
                ETPairT<String, Integer> item = sortItems.get(i);
                String itemID = item.getParamOne();
                int itemType = ((Integer)item.getParamTwo()).intValue();
                
                if (itemType == IMenuKind.MK_SEPARATOR)
                {
                    // Add a separator
                    pSortedMenuItems.add(new Separator());
                }
                else if(itemType == IMenuKind.MK_BUTTON)
                {
                    // Extract the button and add it to the sorted list
                    Object pNewItem = extractButton(pMenuManager, itemID);
                    if (pNewItem != null)
                    {
                        pSortedMenuItems.add(pNewItem);
                    }
                }
                else if (itemType == IMenuKind.MK_PULLRIGHT)
                {
                    // Extract the button and add it to the sorted list
                    IMenuManager pNewItem = extractCascadeButton(pMenuManager, itemID);
                    if (pNewItem != null)
                    {
                        pSortedMenuItems.add(pNewItem);
                    }
                }
                else if (itemType == IMenuKind.MK_PULLRIGHTTITLE)
                {
                    // Extract the button and add it to the sorted list
                    IMenuManager pNewItem = extractCascadeButtonByCascadeButtonSource(pMenuManager, itemID);
                    if (pNewItem != null)
                    {
                        pSortedMenuItems.add(pNewItem);
                    }
                }
            }
        }
        
        return pSortedMenuItems;
    }
    
    /**
     * This routine sorts the project tree context menu
     */
    public void sortMenu(IMenuManager pContextMenu)
    {
        ETList<Object> pContributionItems = getSortedMenuItems(pContextMenu);
        if (pContributionItems != null)
        {
            for (int i = 0; i< pContributionItems.size(); i++)
            {
                Object obj = pContributionItems.get(i);
                if(obj instanceof IMenuManager)
                {
                    pContextMenu.add((IMenuManager)obj);
                }
                else
                {
                    pContextMenu.add((BaseAction)obj);
                }
            }
        }
    }
    
    /**
     * Sorts the buttons with in the pullright
     *
     * @param pContextMenu [in] The list of buttons where the cascade lives with a source of sPullrightButtonSource
     * @param sPullrightButtonSource [in] The button source of the cascade we need to sort.
     */
    public void sortMenu(IMenuManager pContextMenu, String sPullrightButtonSource)
    {
        if (sPullrightButtonSource != null && sPullrightButtonSource.length() > 0)
        {
            IMenuManager pMenuItem = null;
            Object[] pContributionItems = pContextMenu.getItems();
            if (pContributionItems != null)
            {
                int count = pContributionItems.length;
                for (int i = 0 ; i < count ; i++)
                {
                    Object pTempMenuItem = pContributionItems[i];
                    if (pTempMenuItem instanceof IMenuManager)
                    {
                        String label = ((IMenuManager)pTempMenuItem).getLabel();
                        if (label != null && (label.equals(sPullrightButtonSource)))
                        {
                            pMenuItem = (IMenuManager)pTempMenuItem;
                        }
                        if (pMenuItem != null)
                        {
                            break;
                        }
                    }
                    else if (pTempMenuItem instanceof BaseAction)
                    {                        
                    }
                }
            }
            
            if (pMenuItem != null && pMenuItem instanceof IMenuManager)
            {
                // Get the submenus and start sorting them
                IMenuManager mgr = (IMenuManager)pMenuItem;
                ETList<Object> pSortedMenuItems = getSortedMenuItems(mgr);
                
                if (pSortedMenuItems != null)
                {
                    for (int i = 0; i < pSortedMenuItems.size(); i++)
                    {
                        Object curItem = pSortedMenuItems.get(i);
                        if(curItem instanceof BaseAction)
                        {
                            mgr.add((BaseAction)curItem);
                        }
                        else
                        {
                            mgr.add((IMenuManager)curItem);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Sorts these buttons
     *
     * @param pMenuItems [in] The unsorted menu items
     * @param pSortedMenuItems [in] The sorted menu items
     */
    public ETList<Object> getSortedMenuItems(IMenuManager pMenuItems)
    {
        ETList<Object> pSortedMenuItems = new ETArrayList<Object>();
        
        if (pMenuItems != null)
        {
            // Extract the menu items that should be on top
            ETList<Object> pTopSortedItems = extractSortButtons(pMenuItems, m_TopSortItems);
            if (pTopSortedItems != null)
            {
                pSortedMenuItems.addAll(pTopSortedItems);
            }
            
            // Extract the menu items that should be on the bottom
            ETList<Object> pBottomSortedMenuItems = extractSortButtons(pMenuItems, m_BottomSortItems);
            
            Object[] otherItems = pMenuItems.getItems();
            if (otherItems != null)
            {
                int count = otherItems.length;
                
                // Now add the rest
                boolean bFoundFirstButton = false; // used to clear out an separators that bubbled to the top
                for (int i = 0 ; i < count ; i++)
                {
                    Object pMenuItem = otherItems[i];
                    
                    if (pMenuItem != null)
                    {
                        if (pMenuItem instanceof IMenuManager)
                        {
                            boolean bIsSeparator = ((IMenuManager)pMenuItem).isSeparator();
                            if (bIsSeparator == false && bFoundFirstButton == false)
                            {
                                // This is the first non-separator button
                                bFoundFirstButton = true;
                                // Add a leading separator
                                pSortedMenuItems.add(new Separator());
                            }
                            
                            if ((bFoundFirstButton) || (bIsSeparator == true))
                            {
                                // Extract the button and add it to the sorted list
                                pSortedMenuItems.add(pMenuItem);
                            }
                        }
                        else if(pMenuItem instanceof BaseAction)
                        {
                            pSortedMenuItems.add((BaseAction)pMenuItem);
                        }
                    }
                }
                
                if (bFoundFirstButton)
                {
                    // Add a trailing separator
                    pSortedMenuItems.add(new Separator());
                }
            }
            
            
            // Add the bottom sorted items to the bottom of the sort list
            if (pBottomSortedMenuItems != null)
            {
                pSortedMenuItems.add(new Separator());
                pSortedMenuItems.addAll(pBottomSortedMenuItems);
            }
        }
        
        pMenuItems.removeAll();
        return pSortedMenuItems;
    }
    
    /**
     * Extracts the button matching sButtonSource from IProductContextMenuItems.  The extracted
     * button is removed from pContextMenu and returned in pExtractedButton.
     *
     * @param pMenuItems [in] The menu items to look through
     * @param sButtonSource [in] The button source of the child button
     * @param pExtractedButton [out,retval] The found button, NULL if no button is found.
     */
    public Object extractButton(IMenuManager pMenuItems, String sButtonSource)
    {
        Object pExtractedButton = pMenuItems.find(sButtonSource);
        if (pExtractedButton != null)
        {
            pMenuItems.remove(pExtractedButton);
        }
        
        return pExtractedButton;
    }
    
    /**
     * Extracts the popup(cascade) button that has sChildButtonSource as a child and returns that button in pExtractedButton.
     *
     * @param pMenuItems [in] The menu items to look through
     * @param sChildButtonSource [in] The button source of the child button
     * @param pExtractedButton [out,retval] The found cascade, NULL if no button is found.
     */
    public IMenuManager extractCascadeButton(IMenuManager pMenuItems, String sChildButtonSource)
    {
        IMenuManager pExtractedButton = null;
        
        Object[] pContributionItems = pMenuItems.getItems();
        if (pContributionItems != null)
        {
            int count = pContributionItems.length;
            for (int i = 0 ; i < count ; i++)
            {
                Object pMenuItem = pContributionItems[i];
                if (pMenuItem instanceof IMenuManager)
                {
                    IMenuManager pSubMenuManager = (IMenuManager)pMenuItem;
                    if (pSubMenuManager.find(sChildButtonSource) != null)
                    {
                        pExtractedButton = (IMenuManager)pMenuItem;
                    }
                }
                
                if (pExtractedButton != null)
                {
                    // We found the subbutton so remove the parent popup (cascade)
                    pMenuItems.remove(pExtractedButton);
                    break;
                }
            }
        }
        
        return pExtractedButton;
    }
    
    /**
     * Takes a cascade button ou of the list if the button source for the cascade button matches sButtonSource
     *
     * @param pMenuItems [in] The menu items to look through
     * @param sButtonSource [in] The button source of the cascade button
     * @param pExtractedButton [out,retval] The found cascade, NULL if no button is found.
     */
    public IMenuManager extractCascadeButtonByCascadeButtonSource(IMenuManager pMenuItems, String sButtonSource)
    {
        IMenuManager pExtractedButton = null;
        
        Object[] pContributionItems = pMenuItems.getItems();
        if (pContributionItems != null)
        {
            int count = pContributionItems.length;
            for (int i = 0 ; i < count ; i++)
            {
                Object pMenuItem = pContributionItems[i];
                if (pMenuItem instanceof IMenuManager)
                {
                    String label = ((IMenuManager)pMenuItem).getLabel();
                    if ((label != null) && label.equals(sButtonSource))
                    {
                        pExtractedButton = (IMenuManager)pMenuItem;
                    }
                }
                
                if (pExtractedButton != null)
                {
                    // We found the subbutton so remove the parent popup (cascade)
                    pMenuItems.remove(pExtractedButton);
                    break;
                }
            }
        }
        
        return pExtractedButton;
    }
}
