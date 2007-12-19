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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.rt.providers.impl.local;

import java.io.File;
import javax.swing.JPanel;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.ProjectWizardComponent;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author avk
 */
public class LocalServerProjectWizardPanel extends LocalServerProjectVisual 
        implements ProjectWizardComponent
{
    
    /**
     * The property name for error messages in {@link WizardDescriptor}.
     */
    private static final String WIZARD_PANEL_ERROR_MSG = "WizardPanel_errorMessage"; //NOI18N
    
    /**
     * The wizard descriptor providing the settings for this panel.
     */
    private WizardDescriptor descriptor;
    
    public LocalServerProjectWizardPanel(Host host) {
        myHost = host;
    }

    public void store(WizardDescriptor descriptor) {
        descriptor.putProperty(PROP_CONTEXT, getContextPath().getText());
    }

    public void read(WizardDescriptor settings) {
        this.descriptor = settings;
        String context = (String) settings.getProperty( PROP_CONTEXT );
        if (context == null){
            context = "/"+(String) settings.getProperty( PROP_PROJECT_NAME );
        }
        setContextValue(context);
    }

    public boolean isContentValid() {
        return validateDocumentPath();
    }

    /**
     * Checks whether the document path already exists 
     * and sets an error message to the descriptor if it does.
     * 
     * @return false if the document path represents an existing folder, true
     * otherwise.
     */
    private boolean validateDocumentPath(){
        
        if (descriptor != null){
            descriptor.putProperty(WIZARD_PANEL_ERROR_MSG, "");
        }

        String docPath = getDocumentPath().getText();
        if (docPath != null){
            File file = new File(docPath);
            if (file.exists()){
                if (descriptor != null){
                    descriptor.putProperty(WIZARD_PANEL_ERROR_MSG, 
                            NbBundle.getMessage(LocalServerProjectWizardPanel.class, "MSG_DocumentPathExists"));
                }
                return true;
            }
        }
        return true;
    }


    public JPanel getPanel() {
        return this;
    }

    protected void contextUpdated() {
        if (getHost() instanceof LocalHostImpl) {
            LocalHostImpl host = (LocalHostImpl) getHost();
            updateDocumentPath(host);
            updateHttpPath(host);
            validateDocumentPath();
        }
    }

    protected void setDefaults() {
        setContextValue(DEFAULT_CONTEXT);
    }
    
    private void setContextValue(String context) {
        getContextPath().setText(context);

        contextUpdated();
    }

    protected Host getHost() {
        return myHost;
    }

    private Host myHost;
}
