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


package org.netbeans.modules.uml.ui.support.commondialogs;

import org.netbeans.modules.uml.common.generics.ETTripleT;
import java.awt.Frame;

/**
 */
public class PreferenceQuestionDialog implements IPreferenceQuestionDialog
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog#getRunSilent()
     */
    public boolean getRunSilent()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog#setRunSilent(boolean)
     */
    public void setRunSilent(boolean value)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog#displayFromResource(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, int, int, int, int, int, int)
     */
    public int displayFromResource(String sPreferenceKey,
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
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog#displayFromStrings(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, int, java.lang.String, int, int, int)
     */
    public int displayFromStrings(String sPreferenceKey, 
                                  String sPreferencePath,
                                  String sPreferenceName, 
                                  String sAffirmative, 
                                  String sNegative, 
                                  String sAsk, 
                                  String sMessageString, 
                                  /* SimpleQuestionDialogResultKind */ int nDefaultResult, 
                                  String sTitle, 
                                  /* SimpleQuestionDialogKind */ int nDialogType, 
                                  /* ErrorDialogIconKind */ int nDialogIcon, 
                                  Frame parent)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog#setDefaultButton(int)
     */
    public void setDefaultButton(int value)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog#displayDeleteWithAlso(java.lang.String, java.lang.String, java.lang.String, boolean, java.lang.String, int, boolean, boolean)
     */
    public ETTripleT<Integer, Boolean, Boolean> displayDeleteWithAlso(String sKey, String sPath, String sName, boolean bDefaultAffectDataModel, String sAlsoQuestion)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceControlledDialog#setAutoUpdatePreference(boolean)
     */
    public void setAutoUpdatePreference(boolean value)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceControlledDialog#getAutoUpdatePreference()
     */
    public boolean getAutoUpdatePreference()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceControlledDialog#setPrefKey(java.lang.String)
     */
    public void setPrefKey(String value)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceControlledDialog#getPrefKey()
     */
    public String getPrefKey()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceControlledDialog#setPrefPath(java.lang.String)
     */
    public void setPrefPath(String value)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceControlledDialog#getPrefPath()
     */
    public String getPrefPath()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceControlledDialog#setPrefName(java.lang.String)
     */
    public void setPrefName(String value)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceControlledDialog#getPrefName()
     */
    public String getPrefName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceControlledDialog#setPreferenceValue(java.lang.String)
     */
    public void setPreferenceValue(String value)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceControlledDialog#getPreferenceValue()
     */
    public String getPreferenceValue()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceControlledDialog#preferenceInformation(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public long preferenceInformation(String sPreferenceKey,
            String sPreferencePath, String sPreferenceName,
            boolean bAutoUpdatePreference)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.ISilentDialog#isRunSilent()
     */
    public boolean isRunSilent()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.commondialogs.ISilentDialog#setIsRunSilent(boolean)
     */
    public void setIsRunSilent(boolean value)
    {
        // TODO Auto-generated method stub

    }

}
