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

package org.netbeans.modules.vmd.componentssupport.ui.wizard;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import javax.swing.JPanel;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Represents <em>Libraries</em> panel in J2SE Library Descriptor Wizard.
 */
final class SelectLibraryVisualPanel extends JPanel {

    private static final long serialVersionUID = -3903508171781536427L;
    
    public SelectLibraryVisualPanel(SelectLibraryWizardPanel panel) {
        myPanel = panel;
        getAccessibleContext().setAccessibleName(getMessage("ACS_SelectLibraryPanel"));
        getAccessibleContext().setAccessibleDescription(getMessage("ACS_SelectLibraryPanel"));

        myInnerPanel = LibraryChooser.createPanel(null, new ClassLibraryFilter() );
        myInnerPanel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (LibraryChooser.Panel.PROP_SELECTED_LIBRARIES.equals(evt.getPropertyName())) {
                    checkValidity();
                }
            }
        });
        setLayout(new BorderLayout());
        add(myInnerPanel.getVisualComponent(), BorderLayout.CENTER);
    }
    
    private void checkValidity() { 
        mySettings.putProperty(
                CustomComponentWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, null);
        if (getSelectedLibrary() != null) {
            setValid(true);
        } else {
            setValid(false);
        }
    }
    
    private final void setValid(boolean valid) {
        myPanel.setValid(valid);
    }

    private Library getSelectedLibrary() {
        Set<Library> selection = myInnerPanel.getSelectedLibraries();
        return selection.size() == 1 ? selection.iterator().next() : null;
    }
    
    void storeData() {
        Library oldLib = (Library) mySettings.getProperty(NewLibraryDescriptor.LIBRARY);
        Library newLib = getSelectedLibrary();
        mySettings.putProperty( NewLibraryDescriptor.LIBRARY , newLib);
        // clean library info if library was changed
        if (!newLib.equals(oldLib)){
            mySettings.putProperty(NewLibraryDescriptor.LIB_NAME,  null );
            mySettings.putProperty(NewLibraryDescriptor.DISPLAY_NAME, null );
        }
    }
    
    void readData( WizardDescriptor settings ) {
        mySettings = settings;
        checkValidity();
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(SelectLibraryVisualPanel.class);
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(SelectLibraryVisualPanel.class, key);
    }
    
    private class ClassLibraryFilter implements LibraryChooser.Filter{

        public boolean accept(Library library) {
            if (NewLibraryDescriptor.LIBRARY_TYPE_J2SE.equals(library.getType())) {
                return true;
            }
            return false;
        }
        
    }
    
    private WizardDescriptor mySettings;
    private final LibraryChooser.Panel myInnerPanel;
    private SelectLibraryWizardPanel myPanel;
    
}
