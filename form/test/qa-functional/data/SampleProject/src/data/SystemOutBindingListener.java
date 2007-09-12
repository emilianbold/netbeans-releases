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

package data;

import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.Binding.SyncFailure;
import org.jdesktop.beansbinding.BindingListener;

/**
 * Very very very simple binding listener ;)
 * 
 * @author Jiri Vagner
 */
class SystemOutBindingListener implements BindingListener {
    
    public void bindingBecameBound(Binding arg) {
        System.out.println("bindingBecameBound");  // NOI18N
    }

    public void bindingBecameUnbound(Binding arg) {
        System.out.println("bindingBecameBound");  // NOI18N
    }

    public void syncFailed(Binding arg, SyncFailure... arg1) {
        System.out.println("syncFailed: " + arg1[0].getValidationResult().getDescription());  // NOI18N
    }

    public void synced(Binding arg) {
        System.out.println("synced");  // NOI18N
    }

    public void sourceEdited(Binding arg) {
        System.out.println("sourceEdited");  // NOI18N
    }

    public void targetEdited(Binding arg) {
        System.out.println("targetEdited");  // NOI18N
    }
}
