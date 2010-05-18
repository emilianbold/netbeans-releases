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

package org.netbeans.modules.vmd.componentssupport.ui.wizard;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.JavaMELibsConfigurationHelper;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.JavaMELibsPreviewHelper;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Represents <em>Name and Location</em> panel in J2ME Library Descriptor Wizard.
 *
 * @author ads
 */
final class ComponentPresentersVisualPanel extends JPanel {
    
    /** Creates new NameAndLocationPanel */
    ComponentPresentersVisualPanel(ComponentPresentersWizardPanel panel) {
        myPanel = panel;
        initComponents();
        
        DocumentListener dListener = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                // TODO check all fields and notify panel about changes
            }
        };
        //libraryNameValue.getDocument().addDocumentListener(dListener);
        
    }
    

    void storeData(WizardDescriptor descriptor) {
        // TODO store
    }
    
    void readData( WizardDescriptor descriptor) {
        mySettings = descriptor;
        // TODO read
        checkValidity();
    }

    private boolean checkValidity(){
        // TODO add library verification ( e.g.: was not added yet )
        //if (!isValidLibraryName()){
        //    setError( getMessage(MSG_EMPTY_LIB_NAME) );
        //    return false;
        //}
        markValid();
        return true;
    }
    
    //private boolean isValidLibraryName() {
    //    return getLibNameValue() != null &&
    //            getLibNameValue().trim().length() != 0;
    //}


    /**
     * Set an error message and mark the panel as invalid.
     */
    protected final void setError(String message) {
        assert message != null;
        setMessage(message);
        setValid(false);
    }

    /**
     * Mark the panel as valid and clear any error or warning message.
     */
    protected final void markValid() {
        setMessage(null);
        setValid(true);
    }
    
    private final void setMessage(String message) {
        mySettings.putProperty(
                CustomComponentWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, 
                message);
    }

    private final void setValid(boolean valid) {
        myPanel.setValid(valid);
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(ComponentPresentersVisualPanel.class);
    }
    
    private static String getMessage(String key, Object... args) {
        return NbBundle.getMessage(ComponentPresentersVisualPanel.class, key, args);
    }
    
    public void addNotify() {
        super.addNotify();
        checkValidity();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    private WizardDescriptor mySettings;
    private ComponentPresentersWizardPanel myPanel;
    
}
