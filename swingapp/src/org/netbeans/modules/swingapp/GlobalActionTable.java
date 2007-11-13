/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.swingapp;

import java.awt.BorderLayout;
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
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    private GlobalActionTable() {
        //setName("Application Actions");
        //setName(FormUtils.getBundleString("CTL_GlobalActionTable_Title"));
        setName(NbBundle.getMessage(GlobalActionTable.class, "CTL_GlobalActionTable_Title"));
        setLayout(new BorderLayout());
        createComponents();
        this.getAccessibleContext().setAccessibleName(NbBundle.getMessage(GlobalActionTable.class, "CTL_GlobalActionTable_Title"));
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GlobalActionTable.class, "CTL_GlobalActionTable_Description"));
    }
    
    private void createComponents() {
        panel = new GlobalActionPanel();
        add(panel,"Center"); //NOI18N
    }
    
    @Override
    protected void componentActivated() {
    }
    
    @Override
    protected void componentDeactivated() {
    }
    
    @Override
    protected String preferredID() {
        return getClass().getName();
    }

    /** Replaces this in object stream. */
    @Override
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
