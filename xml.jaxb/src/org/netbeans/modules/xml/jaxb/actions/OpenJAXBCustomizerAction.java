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

package org.netbeans.modules.xml.jaxb.actions;

import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schema;
import org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSource;
import org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSources;
import org.netbeans.modules.xml.jaxb.cfg.schema.XjcOption;
import org.netbeans.modules.xml.jaxb.cfg.schema.XjcOptions;
import org.netbeans.modules.xml.jaxb.ui.JAXBWizardIterator;
import org.netbeans.modules.xml.jaxb.ui.JAXBWizBindingCfgPanel;
import org.netbeans.modules.xml.jaxb.ui.JAXBWizardSchemaNode;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;


/**
 * @author lgao
 * @author $Author$
 */

public class OpenJAXBCustomizerAction extends CookieAction  {
    
    private void populateSchemaBindingValues(WizardDescriptor wiz,
                                             Project prj,
                                             Schema schema){
        String name = ProjectUtils.getInformation(prj).getName();
        wiz.putProperty(JAXBWizBindingCfgPanel.SCHEMA_NAME, schema.getName());
        wiz.putProperty(JAXBWizBindingCfgPanel.PROJECT_NAME, name);
        wiz.putProperty(JAXBWizBindingCfgPanel.PACKAGE_NAME, schema.getPackage());
        wiz.putProperty(JAXBWizBindingCfgPanel.SCHEMA_TYPE, schema.getType());
        
        XjcOptions opts = schema.getXjcOptions();
        if (opts != null){
            int i = opts.sizeXjcOption();
            if (i > 0){
                Map<String, Boolean> options = new HashMap<String, Boolean>();                
                String key = null;
                String value = null;
                Boolean boolVal = null;
                for (int j =0; j < i; j++){
                    XjcOption xo = opts.getXjcOption(j);
                    key = xo.getName();
                    value = xo.getValue();
                    boolVal = Boolean.FALSE;
                    if ((value != null) 
                            && ("true".equals(value.toLowerCase()))){ // No I18N
                        boolVal = Boolean.TRUE;
                    }
                    options.put(key, boolVal);
                }                
                wiz.putProperty(JAXBWizBindingCfgPanel.XJC_OPTIONS, options);                
            }
        }
        
        SchemaSources sss = schema.getSchemaSources();
        SchemaSource ss = null;
        if (sss != null){
            int sssSize = sss.sizeSchemaSource();
            String origSrcLocType = null;
            if (sssSize > 0){
                List xsdFileList = new ArrayList<String>();                            
                for (int i=0; i < sssSize; i++){
                    ss = sss.getSchemaSource(i);
                    xsdFileList.add(ss.getOrigLocation());
                    origSrcLocType = ss.getOrigLocationType();
                }
                
                wiz.putProperty(JAXBWizBindingCfgPanel.XSD_FILE_LIST, 
                                                                xsdFileList);                
                wiz.putProperty(JAXBWizBindingCfgPanel.SOURCE_LOCATION_TYPE, 
                                                                origSrcLocType); 
            }
        }

    }
    
    protected void performAction(Node[] activatedNodes) {
        JAXBWizardSchemaNode schemaNode = null;
        Project project = null;
        Schema schema = null;
        
        if (activatedNodes.length == 1){
            final Node theNode = activatedNodes[0];            
            schemaNode =  (JAXBWizardSchemaNode) theNode.getLookup().lookup(
                                                JAXBWizardSchemaNode.class );
            project = schemaNode.getProject();
            schema = schemaNode.getSchema();
            
            if ( project != null ) {
                JAXBWizardIterator wizardIter = new JAXBWizardIterator(project);
                final WizardDescriptor descriptor = new WizardDescriptor(
                                                                wizardIter );
                descriptor.putProperty("WizardPanel_autoWizardStyle", 
                                                                Boolean.TRUE);                
                //descriptor.putProperty("WizardPanel_errorMessage", null);
                descriptor.putProperty("WizardPanel_contentDisplayed",
                                                                Boolean.TRUE);
                descriptor.putProperty("WizardPanel_contentNumbered",
                                                                Boolean.TRUE);    
                wizardIter.initialize(descriptor);                
                populateSchemaBindingValues(descriptor, project, schema);
                descriptor.setTitleFormat(new MessageFormat("{0}"));
                DialogDisplayer dd = DialogDisplayer.getDefault();
                Dialog dlg = dd.createDialog( descriptor );
                dlg.setTitle(getDialogTitle()); 
                dlg.setVisible( true );
                
                if ( descriptor.getValue() == descriptor.FINISH_OPTION ) {
                    String pkgName = (String) descriptor.getProperty(
                                          JAXBWizBindingCfgPanel.PACKAGE_NAME);
                    ProjectHelper.removeSchema(project, schema);
                    ProjectHelper.addSchema(project, descriptor);
                    ProjectHelper.compileXSDs(project, pkgName, true);
                }
            }
        }
    }
    
    
    public String getName() {
        return NbBundle.getMessage( 
                                this.getClass(), "LBL_CustomizeJAXBOptions" );
    }

    protected String getDialogTitle(){
        return NbBundle.getMessage(this.getClass(),  
                            "LBL_DialogTitleChangeBindingOptions"); // No I18N  
    }
    
    
   protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {
            DataObject.class
        };
    }
    
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource()
        // javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        if ( activatedNodes.length != 1 )
            return false;
        
        DataObject dataobj = activatedNodes[0].getCookie(DataObject.class);
        if ( dataobj != null ) {
            FileObject fo = dataobj.getPrimaryFile();
        }
        
        return true;
    }    
}