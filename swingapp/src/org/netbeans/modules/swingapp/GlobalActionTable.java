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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.swingapp;

import java.awt.BorderLayout;
import org.netbeans.modules.form.FormUtils;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * GlobalActionTableTC is the actual top component which contains the
 * global action table.
 * @author joshua.marinacci@sun.com
 */
public class GlobalActionTable extends TopComponent {
    
    private static GlobalActionTable instance;
    
    private GlobalActionPanel panel;
    
    public static synchronized GlobalActionTable getDefault() {
        if (instance == null)
            instance = new GlobalActionTable();
        return instance;
    }
    
    /** Finds default instance. Use in client code instead of {@link #getDefault()}. */
    public static synchronized GlobalActionTable getInstance() {
        if (instance == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent("GlobalActionTable"); // NOI18N
            if (instance == null) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException(
                    "Can not find GlobalActionTable component for its ID. Returned " + tc)); // NOI18N
                instance = new GlobalActionTable();
            }
        }
        return instance;
    }
    
    /** Overriden to explicitely set persistence type of GlobalActionTable
     * to PERSISTENCE_ALWAYS */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    private GlobalActionTable() {
        //setName("Application Actions");
        //setName(FormUtils.getBundleString("CTL_GlobalActionTable_Title"));
        setName(NbBundle.getMessage(GlobalActionTable.class, "CTL_GlobalActionTable_Title"));
        setLayout(new BorderLayout());
        createComponents();
    }
    
    private void createComponents() {
        panel = new GlobalActionPanel();
        add(panel,"Center"); //NOI18N
    }
    
    protected void componentActivated() {
    }
    
    protected void componentDeactivated() {
    }
    
    protected String preferredID() {
        return getClass().getName();
    }

    /** Replaces this in object stream. */
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    public static final class ResolvableHelper implements java.io.Serializable {
        static final long serialVersionUID = 3558109100863533811L;
        public Object readResolve() {
            return GlobalActionTable.getDefault();
        }
    }
}
