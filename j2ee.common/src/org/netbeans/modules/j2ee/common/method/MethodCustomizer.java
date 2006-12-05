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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.common.method;

import java.beans.PropertyChangeListener;

/**
 *
 * @author Martin Adamek
 */
public final class MethodCustomizer {
    
    @Deprecated
    public static final String OK_ENABLED = "ok_enabled"; //NOI18N

    public MethodCustomizer(MethodModel methodModel) {
    }

    @Deprecated
    public void setEjbQL(String ql) {
    }
    
    @Deprecated
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Deprecated
    public boolean isOK() {
        return false;
    }
    
    @Deprecated
    public boolean finderReturnIsSingle() {
        return false;
    }

    @Deprecated
    public boolean publishToLocal() {
        return true;
    }

    @Deprecated
    public boolean publishToRemote() {
        return false;
    }
    
    @Deprecated
    public String getEjbQL() {
        return null;
    }
    
}
