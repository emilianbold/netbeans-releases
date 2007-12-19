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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.php.rt.actions;

import java.awt.Dialog;

import java.util.logging.Logger;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.ui.AddHostWizard;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

/**
 * Add host action launches the Add Server wizard.
 *
 * @author ads
 */
public class AddHostAction extends NodeAction {


    private static final String LBL_ADD_HOST = "LBL_Add_Host";      // NOI18N

    private static final String A11_ADD_HOST = "A11_Add_Host";      // NOI18N
    
    private static final long serialVersionUID = -8122136893415347979L;
    
    private static Logger LOGGER = Logger.getLogger(AddHostAction.class.getName());

    public static AddHostAction findInstance() {
        return SharedClassObject.findObject(AddHostAction.class, true);
    }
    
    public void performAction(Node[] nodes) {
        showCustomizer();
    }
    
    /**
     * invokes showCustomizer(Host, AddHostWizard.Mode)
     * wihh Host set to null and mode to AddHostWizard.Mode.CREATE_NEW.
     */
    public void showCustomizer(){
        showCustomizer(null, AddHostWizard.Mode.CREATE_NEW);
    }
    
    /**
     * shows customizer dialog in 'mode' mode with predefined values from 'pattern'
     * @param pattern Host to take default values from. can be null
     * @param mode AddHostWizard.Mode wizard state. will be CREATE_NEW in case of null.
     */
    public void showCustomizer(Host pattern, AddHostWizard.Mode mode){
        AddHostWizard wizard = new AddHostWizard(
                pattern, mode);
        
        Dialog dialog = null;
        try {
            dialog = DialogDisplayer.getDefault().createDialog(wizard);
            dialog.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(AddHostAction.class, A11_ADD_HOST));
            dialog.setVisible(true);
        } finally {
            if (dialog != null) {
                dialog.dispose();
            }
        }
    }
    
    
    public String getName() {
        return NbBundle.getMessage(AddHostAction.class, LBL_ADD_HOST);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public boolean enable(Node[] nodes) {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.openide.util.actions.CallableSystemAction#asynchronous()
     */
    @Override
    protected boolean asynchronous()
    {
        return false;
    }
    
}
