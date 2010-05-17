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



package org.netbeans.modules.uml.ui.support;

import java.awt.Frame;

import javax.swing.JDialog;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.helpers.GUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker.GBK;
import org.netbeans.modules.uml.ui.swing.commondialogs.DefaultCommonDialogResource;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;

/**
 * @author sumitabhk
 *
 *
 */
public class SwingPreferenceQuestionDialog extends SwingPreferenceControlledDialog
    implements IPreferenceQuestionDialog
{
    private String 		m_sAffirmative = "";
    private String     	m_sNegative = "";
    private String     	m_sAsk = "";
    
    private int m_lDefaultButton = -1;
    
    private Frame m_ParentFrame = null;
    private JDialog m_ParentDialog = null;
    
    public SwingPreferenceQuestionDialog()
    {
        super();
        IProxyUserInterface ui =  ProductHelper.getProxyUserInterface();
        
        if (ui != null)
            m_ParentFrame = ui.getWindowHandle();
    }
    
    
    public SwingPreferenceQuestionDialog(Frame pFrame)
    {
        m_ParentFrame = pFrame;
        
        if (pFrame == null)
        {
            IProxyUserInterface ui = ProductHelper.getProxyUserInterface();
            
            if (ui != null)
                m_ParentFrame = ui.getWindowHandle();
        }
    }
    
    
    public SwingPreferenceQuestionDialog(JDialog pDialog)
    {
        if (pDialog != null)
            m_ParentDialog = pDialog;
        
        else
        {
            IProxyUserInterface ui =  ProductHelper.getProxyUserInterface();

            if (ui != null)
                m_ParentFrame = ui.getWindowHandle();
        }
    }
    
    
    /**
     * Displays a question dialog.  The result is provided in the out parameter.
     *
     * @param hInstance [in] Handle to the instance that contains the resource strings
     * @param sPreferenceKey [in] The main hive for the preferences
     * @param sPreferencePath [in] The path to the preference
     * @param sPreferenceName [in] The name of the preference
     * @param sAffirmative [in] The affirmative value for the combo box string
     * @param sNegative [in] The negative value for the combo box string
     * @param sAsk [in] The ask value for the combo box string
     * @param lMessageID [in] The resource ID for the string that is the question being asked
     * @param nDefaultResult [in] The result to send back if the preference
     * @param nResult [out] The result of the users selection
     * @param lTitleID [in] The title for the dialog
     * @param nDialogType [in] Type of the dialog
     * @param nDialogIcon [in] Dialog icon
     * @param parent [in] The HWND parent of this dialog
     *
     * @return
     */
    public int displayFromResource(
        String sPreferenceKey,
        String sPreferencePath,
        String sPreferenceName,
        String sAffirmative,
        String sNegative,
        String sAsk,
        String message,
        int nDefaultResult,
        String title,
        int nDialogType,
        int nDialogIcon,
        Frame parent)
    {
        int nResult = -1;
        // Set the preference strings
        m_sAffirmative = sAffirmative;
        m_sNegative = sNegative;
        m_sAsk = sAsk;
        
        nResult = displayFromStrings( 
            sPreferenceKey,
            sPreferencePath,
            sPreferenceName,
            sAffirmative,
            sNegative,
            sAsk,
            message,
            nDefaultResult,
            title,
            nDialogType,
            nDialogIcon,
            parent);
        
        return nResult;
    }
    
    
    /**
     * Displays a question dialog.  The result is provided in the out parameter.
     *
     * @param sPreferenceKey [in] The main hive for the preferences
     * @param sPreferencePath [in] The path to the preference
     * @param sPreferenceName [in] The name of the preference
     * @param sAffirmative [in] The affirmative value for the combo box string
     * @param sNegative [in] The negative value for the combo box string
     * @param sAsk [in] The ask value for the combo box string
     * @param sMessageString [in] The resource ID for the string that is the question being asked
     * @param nDefaultResult [in] The result to send back if the preference
     * @param nResult [out] The result of the users selection
     * @param sTitle [in] The title for the dialog
     * @param nDialogType [in] Type of the dialog
     * @param nDialogIcon [in] Dialog icon
     * @param parent [in] The HWND parent of this dialog
     */
    public int displayFromStrings(
        String sPreferenceKey,
        String sPreferencePath,
        String sPreferenceName,
        String sAffirmative,
        String sNegative,
        String sAsk,
        String sMessageString,
        int nDefaultResult,
        String sTitle,
        int nDialogType,
        int nDialogIcon,
        Frame parent)
    {
        int nResult = -1;
        
        // Create an IDiagramBlocker to prevent delayed actions from firing.
        IGUIBlocker blocker = new GUIBlocker();
        if (blocker != null)
        {
            blocker.setKind(GBK.DIAGRAM_DELAYEDACTION);
            // Set the preference file information
            preferenceInformation(sPreferenceKey, sPreferencePath, sPreferenceName, true);
            String bsPreferenceValue = getPreferenceValue();
            // Set the preference strings
            m_sAffirmative = sAffirmative;
            m_sNegative = sNegative;
            m_sAsk = sAsk;
            
            SwingQuestionDialogImpl dlg = createTheDialog(parent);
            
            if (dlg != null)
            {
                int userAnswer = displaySimpleDialog(
                    sMessageString, nDefaultResult, sTitle, nDialogType,
                    nDialogIcon, bsPreferenceValue, dlg);
                
                extractDialogData(dlg);
                String bsPrefValue = "";
                
                if ((nDialogType == SimpleQuestionDialogKind.SQDK_YESNOALWAYS) ||
                    (nDialogType == SimpleQuestionDialogKind.SQDK_YESNONEVER))
                {
                    ETPairT<Integer, String> pResult = 
                        getAlwaysNeverResult(nDialogType, userAnswer);
                    
                    if (pResult != null)
                    {
                        nResult = pResult.getParamOne().intValue();
                        bsPrefValue = pResult.getParamTwo();
                    }
                }
                
                else
                {
                    ETPairT<Integer, String> pResult = 
                        getDefaultResult(userAnswer, dlg.getCheckboxIsChecked());
                    
                    if (pResult != null)
                    {
                        nResult = pResult.getParamOne().intValue();
                        bsPrefValue = pResult.getParamTwo();
                    }
                }
                
                // Because we do not want to screw up any bodies preference by 
                // putting a blank into the value, do not set if blank.
                if (bsPrefValue != null && bsPrefValue.length() > 0)
                    setPreferenceValue( bsPrefValue );
            }
        }
        
        return nResult;
    }
    
    
    /**
     * Initializes and displays the dialog.  The dialog will be configured based on the nDialogType
     * parameter.  The users response to the dialog will be returned.
     *
     * @param sMessageString [in] The message to display to the user.
     * @param nDefaultResult [in] The default return value.
     * @param sTitle [in] The title of the dialog box.
     * @param nDialogType [in] The type of dialog to display.
     * @param nDialogIcon [in] The icon to display.
     * @param bsPreferenceValue [in] The current preference value.
     * @param dlg [out] The dialog to display.
     *
     * @return The users response.
     */
    private int displaySimpleDialog(
        String sMessageString,
        /*SimpleQuestionDialogResultKind*/ int nDefaultResult,
        String sTitle,
        /*SimpleQuestionDialogKind*/ int nDialogType,
        /*ErrorDialogIconKind*/ int nDialogIcon,
        String bsPreferenceValue,
        SwingQuestionDialogImpl dlg)
    {
        int retVal = nDefaultResult;
        boolean bIsYesNo = false;
        if ((nDialogType == SimpleQuestionDialogKind.SQDK_YESNO) ||
            (nDialogType == SimpleQuestionDialogKind.SQDK_YESNOCANCEL))
        {
            bIsYesNo = true;
        }

        populateDialog(dlg);
        
        if (bsPreferenceValue.equals(m_sAffirmative))
        {
            if (nDialogType == SimpleQuestionDialogKind.SQDK_YESNOALWAYS)
                retVal = SimpleQuestionDialogResultKind.SQDRK_RESULT_ALWAYS;

            else
            {
                retVal = bIsYesNo 
                    ? SimpleQuestionDialogResultKind.SQDRK_RESULT_YES 
                    : SimpleQuestionDialogResultKind.SQDRK_RESULT_OK;
                
                dlg.setCheckboxIsChecked(true);
            }
        }
        
        else if (bsPreferenceValue.equals(m_sNegative))
        {
            if (nDialogType == SimpleQuestionDialogKind.SQDK_YESNONEVER)
                retVal = SimpleQuestionDialogResultKind.SQDRK_RESULT_NEVER;
            
            else
            {
                retVal = bIsYesNo ? SimpleQuestionDialogResultKind.SQDRK_RESULT_NO : SimpleQuestionDialogResultKind.SQDRK_RESULT_CANCEL;
                dlg.setCheckboxIsChecked(true);
            }
        }
        
        else
        {
            if (isRunSilent())
                retVal = nDefaultResult;

            else
            {
                if (m_lDefaultButton == -1)
                    m_lDefaultButton = bIsYesNo 
                        ? IQuestionDialog.IDYES 
                        : IQuestionDialog.IDOK;

                ETPairT<String, Boolean> checkBoxResult = initializeCheckbox(nDialogType, dlg);
                String checkBoxText = "";
                boolean checkBoxDefault = false;
                
                if (checkBoxResult != null)
                {
                    checkBoxText = checkBoxResult.getParamOne();
                    checkBoxDefault = checkBoxResult.getParamTwo().booleanValue();
                }
                
                QuestionResponse qr = 
                    dlg.displaySimpleQuestionDialogWithCheckbox(
                        nDialogType, nDialogIcon, sMessageString, checkBoxText, 
                        sTitle, m_lDefaultButton, checkBoxDefault);
                
                retVal = qr.getResult();
            }
        }
        
        return retVal;
    }
    
    
    private SwingQuestionDialogImpl createTheDialog(Frame parent)
    {
        if (parent != null)
            return new SwingQuestionDialogImpl(parent);

        else if (m_ParentFrame != null)
            return new SwingQuestionDialogImpl(m_ParentFrame);

        else if (m_ParentDialog != null)
            return new SwingQuestionDialogImpl(m_ParentDialog);

        else
            return new SwingQuestionDialogImpl();
    }
    
    
    private void populateDialog(SwingQuestionDialogImpl dlg)
    {
        // NOTHING TO DO IN THIS BASE CLASS
    }
    
    
    
    private void extractDialogData(SwingQuestionDialogImpl dlg)
    {
        // NOTHING TO DO IN THIS BASE CLASS
    }
    
    
    /**
     * The default button (ie IDOK).  See the return values for AfxMessageBox
     */
    public void setDefaultButton(int value)
    {
        m_lDefaultButton = value;
    }
    
    /**
     * Display the dialog box asks the standard delete question with an also question/preference
     */
    public ETTripleT<Integer, Boolean, Boolean> displayDeleteWithAlso(
        String sKey, 
        String sPath, 
        String sName, 
        boolean bDefaultAffectDataModel, 
        String sAlsoQuestion)
    {
        ETTripleT<Integer, Boolean, Boolean> result = 
            new ETTripleT<Integer, Boolean, Boolean>();
        
        if (!isRunSilent())
        {
            // Set the preference file information
            preferenceInformation( sKey, sPath, sName, true );
            
            String bsPreferenceValue = getPreferenceValue();
            // TESTING String bsPreferenceValue = ;
            
            SwingDeleteWithAlso dlg = 
                new SwingDeleteWithAlso(bsPreferenceValue, sAlsoQuestion, null);
            
            dlg.setDeleteModel(bDefaultAffectDataModel);
            dlg.show();
        
            if (!dlg.getCanceled())
            {
                result.setParamOne(new Integer(0));
                result.setParamTwo(new Boolean(dlg.getDeleteModel()));
                result.setParamThree(new Boolean(dlg.getAlso()));
            }
            
            else
            {
                result.setParamOne(new Integer(1));
                result.setParamTwo(new Boolean(false));
                result.setParamThree(new Boolean(false));
            }
            
            if (dlg.getNever() && bsPreferenceValue.equals("PSK_ASK")) // NOI18N
            {
                String bsPreference = dlg.getAlso() 
                    ? "PSK_ALWAYS"  // NOI18N
                    : "PSK_NEVER"; // NOI18N
                
                setPreferenceValue(bsPreference);
            }
        }
        
        return result;
    }
    /**
     * Initialize the checkbox.  When the dialog type is SQDK_YESNOALWAYS or SQDK_YESNONEVER
     * the check box will be hidden.
     *
     * @param nDialogType [in] The dialog type that is displayed.
     * @param dlg [out] The dialog to display.
     */
    private ETPairT<String, Boolean> initializeCheckbox(
        /*SimpleQuestionDialogKind*/ int nDialogType, 
        SwingQuestionDialogImpl dlg)
    {
        ETPairT<String, Boolean> bsCheckBox = new ETPairT<String, Boolean>();
        
        if
            ((SimpleQuestionDialogKind.SQDK_YESNOALWAYS != nDialogType) &&
            (SimpleQuestionDialogKind.SQDK_YESNONEVER  != nDialogType))
        {
            if (nDialogType == SimpleQuestionDialogKind.SQDK_OK)
            {
                String str = DefaultCommonDialogResource
                    .getString("IDS_NEVER_SHOW"); // NOI18N
                
                bsCheckBox.setParamOne(str);
            }
            
            else
            {
                String str = DefaultCommonDialogResource
                    .getString("IDS_DONT_SHOW"); // NOI18N
                
                bsCheckBox.setParamOne(str);
            }
            
            bsCheckBox.setParamTwo(new Boolean(false));
        }
        
        else
        {
            bsCheckBox.setParamOne(""); // NOI18N
            bsCheckBox.setParamTwo(new Boolean(false));
        }
        
        return bsCheckBox;
    }
    
    
    /**
     * Retrieves the questions answer and preference value for Always and Never dialogs.
     * When the user selects <I>Always</I> nResult will be set to SQDRK_RESULT_YES and
     * the preference will be m_sAffirmative.  When the user selects <I>Never</> nResult
     * will be set to SQDRK_RESULT_NO and the preference will be m_sNegative.
     *
     * @param nDialogType [in] The type of dialog to display.
     * @param userAnswer [in] The answer that the user choose.
     * @param nResult [in] The final result.
     * @param prefValue [out] The preference value that represent the users answer.
     */
    private ETPairT<Integer, String> getAlwaysNeverResult(
        /*SimpleQuestionDialogKind*/ int nDialogType,
        /*SimpleQuestionDialogResultKind*/ int userAnswer)
    {
        ETPairT<Integer, String> pResult = new ETPairT<Integer, String>();
        
        if (pResult != null)
        {
            pResult.setParamOne(new Integer(userAnswer));
            
            if (SimpleQuestionDialogResultKind.SQDRK_RESULT_ALWAYS == userAnswer)
            {
                pResult.setParamOne(new Integer(SimpleQuestionDialogResultKind.SQDRK_RESULT_YES));
                pResult.setParamTwo(m_sAffirmative);
            }
            
            else if (
                SimpleQuestionDialogResultKind.SQDRK_RESULT_NEVER == userAnswer)
            {
                pResult.setParamOne(new Integer(
                    SimpleQuestionDialogResultKind.SQDRK_RESULT_NO));
                
                pResult.setParamTwo(m_sNegative);
            }
            
            else
                pResult.setParamTwo(m_sAsk);
        }

        return pResult;
    }
    
    
    /**
     * Retrieves the questions answer and preference value for Always and Never dialogs.
     * When the user selects <I>Yes</I> and checks the checkbox nResult will be set to
     * SQDRK_RESULT_YES or SQDRK_RESULT_OK and the preference will be m_sAffirmative.
     * When the user selects <I>No</> of <I>Cancel</> nResult will be set to
     * SQDRK_RESULT_NO or SQDRK_RESULT_CANCEL and the preference will be m_sNegative.
     *
     * @param userAnswer [in] The answer that the user choose.
     * @param nResult [in] The final result.
     * @param isChecked [in] True if the user selected the check box, false otherwise.
     * @param prefValue [out] The preference value that represent the users answer.
     */
    private ETPairT<Integer, String> getDefaultResult(
        /*SimpleQuestionDialogResultKind*/int userAnswer,
        boolean isChecked)
    {
        ETPairT<Integer, String> pResult = new ETPairT<Integer, String>();
        
        if (pResult != null)
        {
            pResult.setParamOne(new Integer(userAnswer));
            
            if (userAnswer != SimpleQuestionDialogResultKind.SQDRK_RESULT_CANCEL)
            {
                // Update the preference

                if (isChecked)
                {
                    if (SimpleQuestionDialogResultKind.SQDRK_RESULT_YES == userAnswer ||
                        SimpleQuestionDialogResultKind.SQDRK_RESULT_OK == userAnswer )
                    {
                        pResult.setParamTwo(m_sAffirmative);
                    }

                    else
                        pResult.setParamTwo(m_sNegative);
                }

                else
                    pResult.setParamTwo(m_sAsk);
            }
        }

        return pResult;
    }
}
