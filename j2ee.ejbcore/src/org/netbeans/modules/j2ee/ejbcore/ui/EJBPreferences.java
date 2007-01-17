/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-200? Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ejbcore.ui;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

public class EJBPreferences {

    private static final String AGREED_DELETE_EJB_GENERATED_SOURCES = "agreeDeleteEJBGeneratedSources"; // NOI18N
    private static final String AGREED_CREATE_SERVER_RESOURCES = "agreeCreateServerResources"; // NOI18N

    public boolean isAgreedDeleteEJBGeneratedSources() {
        return prefs().getBoolean(AGREED_DELETE_EJB_GENERATED_SOURCES, false);
    }
    
    public void setAgreedDeleteEJBGeneratedSources(boolean agreed) {
        prefs().putBoolean(AGREED_DELETE_EJB_GENERATED_SOURCES, agreed);
    }
    
    public boolean isAgreedCreateServerResources() {
        return prefs().getBoolean(AGREED_CREATE_SERVER_RESOURCES, true);
    }
    
    public void setAgreedCreateServerResources(boolean agreed) {
        prefs().putBoolean(AGREED_CREATE_SERVER_RESOURCES, agreed);
    }

    private Preferences prefs() {
        return NbPreferences.forModule(EJBPreferences.class);
    }

}
