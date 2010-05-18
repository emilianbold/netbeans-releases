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
 * File       : RequestProcessorUtilities.java
 * Created on : Oct 28, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.coreapplication.ICoreMessenger;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.LanguageManager;
import org.netbeans.modules.uml.core.support.umlmessagingcore.UMLMessagingHelper;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;
import org.openide.util.NbPreferences;

/**
 * @author Aztec
 */
public class RequestProcessorUtilities implements IRequestProcessorUtilities
{
    private ICoreMessenger m_Messenger = null;
    private IPreferenceManager2 m_Manager = null;

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#displayErrorDialog(java.lang.String, int)
     */
    public void displayErrorDialog(String message, String title, int iconKind)
    {
        IQuestionDialog pDialog = new SwingQuestionDialogImpl();
        pDialog.displaySimpleQuestionDialogWithCheckbox(MessageDialogKindEnum.SQDK_OK,
                                                        iconKind,
                                                        message, "", title,
                                                        MessageResultKindEnum.SQDRK_RESULT_YES,
                                                        true);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#getBooleanPreferenceValue(java.lang.String)
     */
    public boolean getBooleanPreferenceValue(String prefName, boolean defValue)
    {
        //kris richards - changing to NbPreferences. This was setup to return false
        //if something went wrong. Mimicing that behavior.
        return NbPreferences.forModule(RequestProcessorUtilities.class).getBoolean (prefName, defValue) ;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#getClassOfAttribute(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute)
     */
    public IClassifier getClassOfAttribute(IAttribute pAttr)
    {
        if(pAttr instanceof INavigableEnd)
            return ((INavigableEnd)pAttr).getReferencingClassifier();
        return pAttr.getFeaturingClassifier();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#getLanguage()
     */
    public String getLanguage()
    {
        return "Java";
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#getLanguage(java.lang.String)
     */
    public ILanguage getLanguage(String lang)
    {
        ILanguageManager langMan = new LanguageManager();
        return langMan.getLanguage(lang);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#getLanguageForFile(java.lang.String)
     */
    public ILanguage getLanguageForFile(String fileName)
    {
        ILanguageManager langMan = new LanguageManager();
        return langMan.getLanguageForFile(fileName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#getMessenger()
     */
    public ICoreMessenger getMessenger()
    {
        if(m_Messenger == null)
        {
            ICoreProduct pProduct = ProductRetriever.retrieveProduct();
            if(pProduct != null)
            {
                m_Messenger = pProduct.getCoreMessenger();                
            }
        }
        return m_Messenger;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#getPreferenceKey()
     */
    public String getPreferenceKey()
    {
        return "Default";
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#getPreferenceManager(org.netbeans.modules.uml.core.metamodel.core.foundation.IPreferenceManager)
     */
    public IPreferenceManager2 getPreferenceManager()
    {
        if(m_Manager == null)
        {
            ICoreProduct pProduct = ProductRetriever.retrieveProduct();
            if(pProduct != null)
            {
                m_Manager = pProduct.getPreferenceManager();                
            }
        }
        return m_Manager;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#getPreferencePath()
     */
    public String getPreferencePath()
    {
        return "RoundTrip|Java";
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#getPreferenceValue(java.lang.String)
     */
    public String getPreferenceValue(String prefName, String def)
    {
        return NbPreferences.forModule (RequestProcessorUtilities.class).get (prefName, def) ;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#getRelationType(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy)
     */
    public String getRelationType(IRelationProxy pRelation)
    {
        if(pRelation == null) return null;
        
        String relationType = null;
        
        IElement pLink = pRelation.getConnection();
        if(pLink != null)
        {
            relationType = pLink.getElementType();
        }
        else
        {
            relationType = pRelation.getConnectionElementType();
        }
        
        return relationType;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#isSilent()
     */
    public boolean isSilent()
    {
        if(m_Messenger == null) getMessenger();
        if(m_Messenger != null)
        {
            return m_Messenger.getDisableMessaging();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#sendCriticalMessage(java.lang.String)
     */
    public void sendCriticalMessage(String message, String title)
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendCriticalMessage(message);
        
        displayErrorDialog(message, 
                            title, 
                            MessageIconKindEnum.EDIK_ICONEXCLAMATION);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#sendDebugMessage(java.lang.String)
     */
    public void sendDebugMessage(String message, String title)
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendDebugMessage(message);
        
        displayErrorDialog(message,
                            title, 
                            MessageIconKindEnum.EDIK_ICONHAND);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#sendErrorMessage(java.lang.String)
     */
    public void sendErrorMessage(String message, String title)
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendErrorMessage(message);
        
        displayErrorDialog(message,
                            title, 
                            MessageIconKindEnum.EDIK_ICONERROR);        
        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#sendInfoMessage(java.lang.String)
     */
    public void sendInfoMessage(String message, String title)
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendInfoMessage(message);
        
        displayErrorDialog(message,
                            title, 
                            MessageIconKindEnum.EDIK_ICONINFORMATION);        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestProcessorUtilities#sendWarningMessage(java.lang.String)
     */
    public void sendWarningMessage(String message, String title)
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendWarningMessage(message);
        
        displayErrorDialog(message,
                            title, 
                            MessageIconKindEnum.EDIK_ICONWARNING);        
    }

}
