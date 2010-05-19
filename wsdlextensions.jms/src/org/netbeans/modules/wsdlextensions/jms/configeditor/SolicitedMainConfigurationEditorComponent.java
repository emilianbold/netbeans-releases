/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.jms.configeditor;

import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author jalmero
 */
public class SolicitedMainConfigurationEditorComponent
        implements ExtensibilityElementConfigurationEditorComponent {

    private SolicitedMainPanel editorPanel = null;
    private WSDLComponent component;
    private QName mQName;
  
    private InboundPersistenceController mInController = null;
    private SynchronousReadConsumerPersistenceController mReadController = null;
    
    public SolicitedMainConfigurationEditorComponent(QName qName,
            WSDLComponent component) {
        if (editorPanel == null) {
            editorPanel = new SolicitedMainPanel(component);
        } else {
            editorPanel.populateView(component);
        }

        Project project = JMSUtilities.getProject(component);
        if (project != null) {
            editorPanel.setProject(project);
        }
        
        if (mInController == null) {
            mInController = new InboundPersistenceController(component, 
                    editorPanel.getInboundPanel(), true);           
        }
        mInController.setAdvancedPanel(editorPanel.getAdvancedPanel());
        
        if (mReadController == null) {
            mReadController = new 
                    SynchronousReadConsumerPersistenceController(component,
                    editorPanel.getReadPanel());        
        }        
        
        mQName = qName;
        this.component = component;
        editorPanel.setName(getTitle());
    }

    /**
     * Return the main panel
     * @return
     */
    public JPanel getEditorPanel() {

        return editorPanel;
    }

    /**
     * Return the title
     * @return
     */
    public String getTitle() {
        return NbBundle.getMessage(SolicitedMainConfigurationEditorComponent.class,
                "InboundOneWayConfigurationEditorComponent.CONFIGURE_TITLE");
    }

    /**
     * Return the Help
     * @return
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Return the action listeners if any
     * @return
     */
    public ActionListener getActionListener() {
        return null;
    }
    
    /**
     * Commit all changes
     * @return
     */
    public boolean commit() {
        boolean status = true;
        status = mInController.commit();
        if (status) {
            status = mReadController.commit();
        }
        return status;
    }
    
    /**
     * Rollback any changes
     * @return
     */
    public boolean rollback() {
        return true;
    }     
    
    /**
     * Check if the model is valid or not
     * @return boolean true if model is valid; otherwise false
     */
    public boolean isValid() {
        return editorPanel.validateContent();
    }
    
    /**
     * Set the operation to populate the visual panels with
     * @param operation
     */
    public void setOperation(Operation operation) {
        if ((editorPanel != null) && (operation != null)) {
            editorPanel.setOperation(operation);
        }
    }
    
    /**
     * Enable the Processing Payload section accordingly
     * @param enable
     */
    public void enablePayloadProcessing(boolean enable) {
        if (editorPanel != null) {
            editorPanel.enablePayloadProcessing(enable);
        }
    }     
}
