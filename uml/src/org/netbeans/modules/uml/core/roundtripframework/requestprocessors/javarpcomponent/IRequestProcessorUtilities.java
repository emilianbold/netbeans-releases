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
 * File       : IRequestProcessorUtilities.java
 * Created on : Oct 28, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.coreapplication.ICoreMessenger;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;

/**
 * @author Aztec
 */
public interface IRequestProcessorUtilities
{
    public String getRelationType(IRelationProxy pRelation);

    public boolean getBooleanPreferenceValue(String prefName);
    public String getPreferenceValue (String prefName);

    public IPreferenceManager2 getPreferenceManager();
    public ICoreMessenger getMessenger();

    public String getPreferenceKey();
    public String getPreferencePath ();

    public String getLanguage ();

    public boolean isSilent();

    public void sendCriticalMessage(String message, String title);

    public void sendErrorMessage(String message, String title);

    public void sendWarningMessage(String message, String title);

    public void sendInfoMessage(String message, String title);

    public void sendDebugMessage(String message, String title);

    public void displayErrorDialog(String message, String title, /*ErrorDialogIconKind*/int iconKind);
    
    //virtual HRESULT GetParentHandle ( HWND& hwnd );

    public ILanguage getLanguageForFile(String fileName);
    public ILanguage getLanguage(String lang); 

    public IClassifier getClassOfAttribute(IAttribute pAttr);

}
