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
package org.netbeans.modules.subversion.ui.copy;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public abstract class CopyDialog {

    private DialogDescriptor dialogDescriptor;
    private JButton okButton, cancelButton;
    private JPanel panel;

    private Map<String, JComboBox> urlComboBoxes;
    
    CopyDialog(JPanel panel, String title, String okLabel) {                
        this.panel = panel;
        dialogDescriptor = new DialogDescriptor(panel, title); 
        
        okButton = new JButton(okLabel);
        okButton.getAccessibleContext().setAccessibleDescription(okLabel);
        okButton.setEnabled(false);
        cancelButton = new JButton(org.openide.util.NbBundle.getMessage(CopyDialog.class, "CTL_Copy_Cancel"));                                      // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CopyDialog.class, "CTL_Copy_Cancel"));    // NOI18N
        dialogDescriptor.setOptions(new Object[] {okButton, cancelButton}); 
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CopyDialog.class, "CTL_Title"));                // NOI18N
    }

    protected void resetUrlComboBoxes() {
        getUrlComboBoxes().clear();
    }
    
    protected void setupUrlComboBox(JComboBox cbo, String key) {
        if(cbo==null) {
            return;
        }
        List<String> recentFolders = Utils.getStringList(SvnModuleConfig.getDefault().getPreferences(), key);
        ComboBoxModel rootsModel = new DefaultComboBoxModel(new Vector<String>(recentFolders));
        cbo.setModel(rootsModel);        
                
        getUrlComboBoxes().put(key, cbo);
    }    
    
    private Map<String, JComboBox> getUrlComboBoxes() {
        if(urlComboBoxes == null) {
            urlComboBoxes = new HashMap<String, JComboBox>();
        }
        return urlComboBoxes;
    }
    
    protected JPanel getPanel() {
        return panel;
    }       
    
    boolean showDialog() {                        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CopyDialog.class, "CTL_Title"));                     // NOI18N        
        
        dialog.setVisible(true);
        boolean ret = dialogDescriptor.getValue()==okButton;
        if(ret) {
            storeValidValues();
        }
        return ret;        
    }        
    
    private void storeValidValues() {
        for (Iterator it = urlComboBoxes.keySet().iterator(); it.hasNext();) {
            String key = (String)  it.next();
            JComboBox cbo = (JComboBox) urlComboBoxes.get(key);
            Object item = cbo.getEditor().getItem();
            if(item != null && !item.equals("")) { // NOI18N
                Utils.insert(SvnModuleConfig.getDefault().getPreferences(), key, (String) item, 8);
            }            
        }                
    }       
    
    protected JButton getOKButton() {
        return okButton;
    }

}
