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
 * Created on Jun 9, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.netbeans.modules.uml.ui.support.messaging;

import java.awt.Dialog;

import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * @author jingmingm
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PickListDialog implements IPickListDialog
{
	private boolean m_bRunSilent = false;
	ETList<String> m_PickListItems = new ETArrayList<String>();

	/**
	 * Inherit the run silent dialog off the main messenger
	 */
	public PickListDialog()
	{
		IMessenger pMsg = ProductHelper.getMessenger();
		if (pMsg != null)
		{
			m_bRunSilent = pMsg.getDisableMessaging();
		}
	}
	
	/**
	 * Returns the silent flag for this dialog.  If silent then any Display calls will
	 * not display a dialog, but rather immediately return S_OK;
	 *
	 * @param pVal Has this dialog been silenced
	 *
	 * @return HRESULT
	 */
	public boolean getRunSilent()
	{
		return m_bRunSilent;
	}
	
	/**
	 * Sets the silent flag for this dialog.  If silent then any Display calls will
	 * not display a dialog, but rather immediately return S_OK;
	 *
	 * @param newVal Whether or not this dialog should be silent
	 *
	 * @return HRESULT
	 */
	public void setRunSilent(boolean newVal)
	{
		m_bRunSilent = newVal;
	}
	
	/**
	 * Add a selection to the dialog.
	 *
	 * @param sItem[in]
	 *
	 * @return HRESULT
	 */
   public void addPickListItem(String sItem)
   {
		if (sItem != null && sItem.length() > 0)
		{
			m_PickListItems.add(sItem);
		}
   }

	/**
	 * Adds all the string items to the pick list dialog
	 */
   public void addPickListItems(ETList<String> pItems)
   {
		int lCnt = pItems.size();
		for(int lIndx=0; lIndx < lCnt; lIndx++ )
		{
			String bsItem = pItems.get(lIndx);
         
			if(bsItem != null && bsItem.length() > 0)
			{
				m_PickListItems.add(bsItem);
			}
		};
   }
   
	public void addPickListItems(IStrings pItems)
	{
		int lCnt = pItems.getCount();
		for(int lIndx=0; lIndx < lCnt; lIndx++ )
		{
			String bsItem = pItems.item(lIndx);
         
			if(bsItem != null && bsItem.length() > 0)
			{
				m_PickListItems.add(bsItem);
			}
		};
	}

        
	/**
	 * Display the dialog if not silent.  If silent then the default value is given as the return value.
	 *
	 * @param sTitle[in]
	 * @param sDefaultValue[in]
	 * @param bUserHitOK[out]
	 * @param pReturnValue[out]
	 * @param parent[in]
	 *
	 * @return HRESULT
	 */
//   public ETPairT<Integer, String> display(String sTitle, String sText, int nDialogType, int nDefaultResult, String sDefaultValue, Dialog parent)
//   {
//		ETPairT<Integer, String> retVal = new ETPairT<Integer, String>();
//		int nResult = nDefaultResult;
//		String pReturnValue = sDefaultValue;
//		if (!m_bRunSilent)
//		{
//			SimplePickListDialog dlg = new SimplePickListDialog();
//
//			dlg.setTitle(sTitle);
//			dlg.setMessageText(sText);
//			dlg.setType(nDialogType);
//			//dlg.m_DefaultButton = IDOK;   // TODO, allow the user to pass this value in
//			dlg.setDefaultValue(sDefaultValue);
//			dlg.setPickListItems(m_PickListItems);
//
//			dlg.show();
//			pReturnValue = dlg.getResult();
//			nResult = dlg.getResultKind();
//		}
//		else
//		{
//			nResult = nDefaultResult;
//			pReturnValue = sDefaultValue;
//		}
//		
//		retVal.setParamOne(new Integer(nResult));
//		retVal.setParamTwo(pReturnValue);
//		return retVal;
//   }
   
        
   /**
     * Display the dialog if not silent.  If silent then the default value is given as the return value.
     *
     * @param sTitle Dialog title
     * @param sText  Dialog lable message
     * @param nDefaultResult Default dialog action (Ok or Cancel)
     * @param sDefaultValue Default selected value
     *
     * @return HRESULT
     */
    
    // Added this method to use NB dialog. Aslo fixed CR 6331548.
    public ETPairT<Integer, String> display(String sTitle, String sText, int nDefaultResult, String sDefaultValue)
    {
        ETPairT<Integer, String> retVal = new ETPairT<Integer, String>();
        int nResult = nDefaultResult;
        String pReturnValue = sDefaultValue;
        
        if (m_bRunSilent)
        {
            nResult = nDefaultResult;
            pReturnValue = sDefaultValue;
        }
        else
        {
            SimplePickListDialog pickListPanel = new SimplePickListDialog(sText, sDefaultValue, m_PickListItems);
            DialogDescriptor dialogDescriptor = new DialogDescriptor(pickListPanel, sTitle);
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
            try
            {
                dialog.setVisible(true);
                if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION)
                {
                    nResult = SimpleQuestionDialogResultKind.SQDRK_RESULT_OK;
                    pickListPanel.performOKAction();
                    pReturnValue = pickListPanel.getResult();
                }
                else
                {
                    nResult = SimpleQuestionDialogResultKind.SQDRK_RESULT_ABORT;
                    pReturnValue = pickListPanel.getResult();
                }
            }
            finally
            {
                dialog.dispose();
            }
        }
        retVal.setParamOne(new Integer(nResult));
        retVal.setParamTwo(pReturnValue);
        return retVal;
    }
}
