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
