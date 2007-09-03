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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * CommonAttributePanel.java
 *
 * Created on October 10, 2002
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.Component;
import javax.swing.JTextField;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import org.openide.filesystems.FileObject;

import org.openide.loaders.TemplateWizard;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceUtils;

import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper;

/** A single panel descriptor for a wizard.
 * You probably want to make a wizard iterator to hold it.
 *
 * @author  shirleyc
 */
public class CommonAttributePanel extends ResourceWizardPanel {
    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private CommonAttributeVisualPanel component;
    private ResourceConfigHelper helper;    
    private Wizard wizardInfo;
    private String[] groupNames;
    private boolean setupValid = true;
    
    /** Create the wizard panel descriptor. */
    public CommonAttributePanel(ResourceConfigHelper helper, Wizard wizardInfo, String[] groupNames) {
        this.helper = helper;
        this.wizardInfo = wizardInfo;
        this.groupNames = groupNames;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
               FieldGroup[] groups = new FieldGroup[groupNames.length];
                for (int i = 0; i < this.groupNames.length; i++) {
                    groups[i] = FieldGroupHelper.getFieldGroup(wizardInfo, this.groupNames[i]);  //NOI18N
                }
                String panelType = CommonAttributeVisualPanel.TYPE_JDBC_RESOURCE;
                if (wizardInfo.getName().equals(__JdbcConnectionPool)) {
                     panelType = CommonAttributeVisualPanel.TYPE_CP_POOL_SETTING;
                }else if (wizardInfo.getName().equals(__PersistenceManagerFactoryResource)) {
                    panelType = CommonAttributeVisualPanel.TYPE_PERSISTENCE_MANAGER;
                }
                component = new CommonAttributeVisualPanel(this, groups, panelType, this.helper);
        }
        return component;
    }
    
    public boolean isNewResourceSelected() {
        if (component == null)
            return false;
        else
            return component.isNewResourceSelected();
    }
    
    public void setInitialFocus(){
        if(component != null) {
            component.setInitialFocus();
        }
    }
    
    public void setPropInitialFocus(){
        if(component != null) {
            component.setPropInitialFocus();
        }
    }
    
    public String getResourceName() {
        return this.wizardInfo.getName();
    }
    
    public HelpCtx getHelp() {
        if (wizardInfo.getName().equals(__JdbcConnectionPool)) {
            return new HelpCtx("AS_Wiz_ConnPool_poolSettings"); //NOI18N
        }else if (wizardInfo.getName().equals(__JdbcResource)) {
            return new HelpCtx("AS_Wiz_DataSource_general"); //NOI18N
        }else if (wizardInfo.getName().equals(__PersistenceManagerFactoryResource)) {
            return new HelpCtx("AS_Wiz_PMF_general"); //NOI18N
        }else {
            return HelpCtx.DEFAULT_HELP;
        }
    }
    
    public ResourceConfigHelper getHelper() {
        return helper;
    }
    
    public Wizard getWizard() {
        return wizardInfo;
    }
    
    public String getJndiName() {
        if (component != null && component.jLabels != null && component.jFields != null) {
            int i;
            for (i=0; i < component.jLabels.length; i++) {
                String jLabel = (String)component.jLabels[i].getText();
                if (jLabel.equals(bundle.getString("LBL_" + __JndiName))) { // NO18N
                    return (String)((JTextField)component.jFields[i]).getText();
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if the JNDI Name in the wizard is duplicate name in the
     * Unregistered resource list for JDBC Data Sources, Persistenc Managers, 
     * and Java Mail Sessions.
     *
     * @return boolean true if there is a duplicate name.
     * false if not.
     */
      public boolean isValid () {
          // If it is always OK to press Next or Finish, then:
          if(! setupValid){
              setErrorMsg (bundle.getString ("Err_InvalidSetup"));
              return false;
          }
          setErrorMsg (bundle.getString ("Empty_String"));
          if (component != null && component.jLabels != null && component.jFields != null) {
              int i;
              for (i=0; i < component.jLabels.length; i++) {
                  String jLabel = (String)component.jLabels[i].getText ();
                  if (jLabel.equals (bundle.getString ("LBL_" + __JndiName))) { // NO18N
                      String jndiName = (String)((JTextField)component.jFields[i]).getText ();
                      if (jndiName == null || jndiName.length () == 0) {
                          setErrorMsg (bundle.getString ("Err_InvalidJndiName"));
                          return false;
                      }else if(! ResourceUtils.isLegalResourceName (jndiName)){
                          return false;
                      }else{
                          FileObject resFolder = this.helper.getData ().getTargetFileObject ();
                          if(resFolder != null){
                              if (wizardInfo.getName ().equals (__JdbcResource)){
                                  if(! ResourceUtils.isUniqueFileName (jndiName, resFolder, __JDBCResource)){
                                      setErrorMsg (bundle.getString ("Err_DuplFileJndiName"));
                                      return false;
                                  }
                              }else if(wizardInfo.getName ().equals (__PersistenceManagerFactoryResource)){
                                  if(! ResourceUtils.isUniqueFileName (jndiName, resFolder, __PersistenceResource)){
                                      setErrorMsg (bundle.getString ("Err_DuplFileJndiName"));
                                      return false;
                                  }
                              }
                          }
                      }
                  }else{
                      Set commonAttr =  new HashSet (Arrays.asList (COMMON_ATTR_INTEGER));
                      if(commonAttr.contains (jLabel.trim ())){
                          String fieldValue = (String)((JTextField)component.jFields[i]).getText ();
                          if (fieldValue == null || fieldValue.length () == 0) {
                              setErrorMessage (bundle.getString ("Err_EmptyValue"), jLabel);
                              return false;
                          }
                      }
                  }
              }
              
          }
          if(!isNewResourceSelected ()){
              //Need to check the poolname for jdbc
              if((this.helper.getData ().getResourceName ()).equals (__JdbcResource)){
                  String cpname = this.helper.getData ().getString (__PoolName);
                  if(cpname == null || cpname.trim ().equals ("") ) { //NOI18N
                      setErrorMsg (bundle.getString ("Err_ChooseOrCreatePool"));
                      return false;
                  }
              }
              //Need to get jdbc data if pmf and make sure it has a poolname
              if((this.helper.getData ().getResourceName ()).equals (__PersistenceManagerFactoryResource)){
                  if(this.helper.getData ().getHolder ().hasDSHelper ()){
                      String cpname = this.helper.getData ().getHolder ().getDSHelper ().getData ().getString (__PoolName);
                      if(cpname == null || cpname.trim ().equals ("") ) {//NOI18N
                          setErrorMsg (bundle.getString ("Err_ChooseOrCreatePool"));
                          return false;
                      }
                  }else{
                      String dsname = this.helper.getData ().getString (__JdbcResourceJndiName);
                      if(dsname == null || dsname.trim ().equals ("") ) {//NOI18N
                          setErrorMsg (bundle.getString ("Err_ChooseOrCreateDS"));
                          return false;
                      }
                  }
              }
          }
          return true;
      }
    
    public void initData() {
        this.component.initData();
    }
    
    public void readSettings(Object settings) {
        this.wizDescriptor = (WizardDescriptor)settings;
        TemplateWizard wizard = (TemplateWizard)settings;
        String targetName = wizard.getTargetName();
        FileObject resFolder = ResourceUtils.getResourceDirectory(this.helper.getData().getTargetFileObject());
        this.helper.getData().setTargetFileObject (resFolder);
        if(component == null)
            getComponent();
        if(resFolder != null){
            if (wizardInfo.getName ().equals (__JdbcResource)){
                if(this.helper.getData ().getString (__DynamicWizPanel).equals ("true")){ //NOI18N
                    targetName = null;
                }
                targetName = ResourceUtils.createUniqueFileName (targetName, resFolder, __JDBCResource);
                this.helper.getData ().setTargetFile (targetName);
                component.setHelper (this.helper);
            }else if(wizardInfo.getName ().equals (__PersistenceManagerFactoryResource)){
                targetName = ResourceUtils.createUniqueFileName (targetName, resFolder, __PersistenceResource);
                this.helper.getData ().setTargetFile (targetName);
                component.setHelper (this.helper);
            }
        }else
            setupValid = false;
    }
    
    public boolean isFinishPanel() {
       if(isNewResourceSelected())
            return false;
       else
           return isValid();
    }
    
    private boolean setupValid(){
        return setupValid;
    }
}

