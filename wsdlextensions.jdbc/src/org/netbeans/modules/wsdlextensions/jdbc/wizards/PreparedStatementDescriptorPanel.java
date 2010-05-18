/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.jdbc.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBConnectionDefinition;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.util.HelpCtx;

import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DatabaseModel;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.impl.DatabaseModelImpl;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBTable;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.impl.DBTableImpl;
import org.netbeans.modules.wsdlextensions.jdbc.builder.DBMetaData;
import org.netbeans.modules.wsdlextensions.jdbc.builder.PrepStmt;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.impl.DatabaseObjectFactory;
import org.netbeans.modules.wsdlextensions.jdbc.builder.Table;
import org.netbeans.modules.wsdlextensions.jdbc.builder.TableColumn;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBColumn;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.impl.DBColumnImpl;
import org.netbeans.modules.wsdlextensions.jdbc.builder.util.XMLCharUtil;
import org.openide.WizardDescriptor;
/**
 *
 * @author jalmero
 */
public class PreparedStatementDescriptorPanel extends WSDLWizardDescriptorPanel {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /* Set <ChangeListeners> */
    protected final Set listeners = new HashSet(1);

    DBConnectionDefinition def;
    
    DefaultComboBoxModel providers;
    
    int visibleCt;

    String dbtype;
    
    private int selTableLen = 0;
    /**
     * Model to represent this wizard panel with
     */
    WSDLComponent mComponent = null;
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private PreparedStatementPanel mPanel;    
    
    String mTitle = "";
    
    public PreparedStatementDescriptorPanel(WSDLWizardContext context,
            String title) {
        super(context);
        mTitle = title;
        if (title != null && title.trim().length() != 0) {
            if (mPanel != null) {
                mPanel.setName(title);
            }
        }        
    }
    
    public PreparedStatementDescriptorPanel(WSDLWizardContext context) {
        this(context, "");
    }
    
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (mPanel == null) {
            mPanel = new PreparedStatementPanel(this, mTitle);
        }         
            
        return mPanel;
    }

    public boolean isFinishPanel() {
        return false;
    }

    public HelpCtx getHelp() {
        return mPanel.getHelp();
    }

    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return mPanel.isValid();
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.     
    // when prev panel's 'Next' is called, our readSettings is called
    public void readSettings(Object settings) {
        mPanel.readSettings(settings);   
    }
    
    // when 'Finish' is called while we're in this panel
    public void storeSettings(Object settings) {
        mPanel.storeSettings(settings);              
    }

    @Override
    public String getName() {
        return mPanel.getName();
    }
    
    public void setConnection(DatabaseConnection dbConn) {
        mPanel.setConnection(dbConn);
    }    
    
    public String getSQL(){
    	return mPanel.getSQL();
    }    
    
    public PrepStmt getPrepStmt(){
    	return mPanel.getPrepStmt();
    }   
    
    public void setTextArea(String sql){
    	mPanel.setTextArea(sql);
    }    
}
