/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.propertyeditors;

import gui.propertyeditors.utilities.PropertyEditorsSupport;
import org.netbeans.test.oo.gui.jelly.BeanCustomizer;
import org.netbeans.test.oo.gui.jelly.propertyEditors.PropertyCustomizer;
import org.netbeans.test.oo.gui.jelly.propertyEditors.FormPropertyCustomizer;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.netbeans.test.oo.gui.jelly.JellyProperties;
import org.netbeans.test.oo.gui.jelly.ExplorerNode;
import org.netbeans.test.oo.gui.jelly.Explorer;
import org.netbeans.test.oo.gui.jelly.FileSystem;
import org.netbeans.test.oo.gui.jelly.FileSystems;
import org.netbeans.test.oo.gui.jelly.Editor;
import org.netbeans.test.oo.gui.jelly.FormEditorWindow;
import org.netbeans.test.oo.gui.jelly.form.ComponentInspector;

import org.netbeans.test.oo.gui.jello.JelloOKOnlyDialog;

import org.netbeans.test.oo.gui.jam.JamPropertyButton;
import org.netbeans.test.oo.gui.jam.JamUtilities;

import org.netbeans.junit.NbTestCase;
import org.netbeans.jemmy.JemmyProperties;


/**
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public abstract class PropertyEditorsTest extends NbTestCase {
    
    protected PrintStream err;
    protected PrintStream log;
    
    protected String beanName;
    protected String packageName;
    protected String errorCaption;
    
    protected boolean useForm;
    
    public String propertyInitialValue;
    public String propertyName;
    public String propertyValue;
    
    protected PropertyCustomizer propertyCustomizer;
    
    protected static BeanCustomizer beanCustomizer = null;
    protected static ComponentInspector componentInspector = null;
    protected static boolean lastTest = false;
    
    
    /** Creates a new instance of PropertyEditorsTest */
    public PropertyEditorsTest(String testName) {
        super(testName);
    }
    
    
    public void setUp() {
        //err = System.out;
        err = getLog();
        log = getRef();
        
        beanName = PropertyEditorsSupport.beanName;
        packageName = PropertyEditorsSupport.Resources;
        errorCaption = PropertyEditorsSupport.errorCaption;
        
        try {
            
            // set defaults
            JellyProperties.setDefaults();
            JellyProperties.setJemmyOutput(new PrintWriter(err, true), new PrintWriter(err, true), null);
            //JellyProperties.setJemmyDebugTimeouts();
            
            initializeWorkplace();
            
        }catch(Exception exc) {
            failTest(exc, "SetUp failed. It seems like initializeWorkplace cause exception:"+exc.getMessage());
        }
    }
    
    
    
    public void setByCustomizerOk(String propertyName, boolean expectance){
        try {
            err.println("\n=========================== Trying to set value by customizer-ok {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            openAndGetPropertyCustomizer(propertyName);
            setCustomizerValue();
            //JamUtilities.waitEventQueueEmpty(500);
            
            if(propertyCustomizer.isShowing())
                propertyCustomizer.ok();
            
            err.println("=========================== Trying to set value by customizer-ok {name="+propertyName+" / value="+propertyValue+"} - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCustomizer("+propertyName+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    
    
    public void setByCustomizerCancel(String propertyName, boolean expectance) {
        try {
            err.println("\n=========================== Trying to set value by customizer-cancel {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            openAndGetPropertyCustomizer(propertyName);
            setCustomizerValue();
            propertyCustomizer.cancel();
            err.println("=========================== Trying to set value by customizer-cancel {name="+propertyName+" / value="+propertyValue+"} - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCustomizerCancel("+propertyName+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    
    
    public void setByInPlace(String propertyName, String propertyValue, boolean expectance) {
        try {
            err.println("\n=========================== Trying to set value by in-place {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            
            if(!useForm) {
                //beanCustomizer = openBeanCustomizer(propertyName);
                beanCustomizer.setText(propertyName, propertyValue);
            }else {
                //componentInspector = openComponentInspector(propertyName);
                componentInspector.setText(propertyName, propertyValue);
            }
            
            err.println("=========================== Trying to set value by in-place {name="+propertyName+" / value="+propertyValue+"}  - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByInPlace("+propertyName+", "+propertyValue+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    
    
    public void setByCombo(String propertyName, String propertyValue, boolean expectance) {
        try {
            err.println("\n=========================== Trying to set value by combo box {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            
            if(!useForm) {
                //beanCustomizer = openBeanCustomizer(propertyName);
                beanCustomizer.setSelectedItem(propertyName, propertyValue);
            }else {
                //componentInspector = openComponentInspector(propertyName);
                componentInspector.setSelectedItem(propertyName, propertyValue);
            }
            
            err.println("=========================== Trying to set value by combo box {name="+propertyName+" / value="+propertyValue+"}  - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCombo("+propertyName+", "+propertyValue+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    
    
    public void setByCombo(String propertyName, int propertyValueIndex, boolean expectance) {
        try {
            err.println("\n=========================== Trying to set value by combo box {name="+propertyName+" / value="+propertyValueIndex+"} .");
            propertyInitialValue = getValue(propertyName);
            
            if(!useForm) {
                //beanCustomizer = openBeanCustomizer(propertyName);
                beanCustomizer.setSelectedItem(propertyName, propertyValueIndex);
            }else {
                //componentInspector = openComponentInspector(propertyName);
                componentInspector.setSelectedItem(propertyName, propertyValueIndex);
            }
            
            err.println("=========================== Trying to set value by combo box {name="+propertyName+" / value="+propertyValueIndex+"}  - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCombo("+propertyName+", "+propertyValueIndex+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    
    private PropertyCustomizer openAndGetPropertyCustomizer(String propertyName) {
        
        if(!useForm) {
            //openPropertyCustomizer_from_BeanCustomizer(propertyName);
                    // open Property Editor
                    err.println("=========================== Trying to open Property Customizer{"+JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout")+"}.");
                    beanCustomizer.openEditDialog(propertyName);
                    err.println("=========================== Trying to open Property Customizer - finished.");

            propertyCustomizer = findPropertyCustomizer(propertyName);
        }else{
            //openPropertyCustomizer_from_ComponentInspector(propertyName);
                    // open Property Editor
                    err.println("=========================== Trying to open Property Customizer{"+JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout")+"}.");
                    componentInspector.openEditDialog(propertyName);
                    err.println("=========================== Trying to open Property Customizer - finished.");

            propertyCustomizer = findFormPropertyCustomizer(propertyName);
        }
        
        return propertyCustomizer;
    }
    
    
    public PropertyCustomizer getPropertyCustomizer() {
        return propertyCustomizer;
    }
    
    public JelloOKOnlyDialog getInformationDialog() {
        String title = "Information";
        err.println("=========================== Waiting dialog {"+title+"} .");
        JelloOKOnlyDialog dialog = new JelloOKOnlyDialog(title);
        err.println("=========================== Waiting dialog {"+title+"} - finished.");
        return dialog;
    }
    
    
    public String getValue(String propertyName) {
        if(!useForm) {
            return beanCustomizer.getValue(propertyName);
        }else{
            return componentInspector.getValue(propertyName);
        }
    }
    
    
    
    private void openBeanCustomizer() {

        if(beanCustomizer==null){
            // open Customize Bean
            err.println("=========================== Trying to Customize Bean");
            beanCustomizer = PropertyEditorsSupport.openForTestCustomizeBean(packageName, beanName, err);
            err.println("=========================== Trying to Customize Bean - finished.");
        }
        
        // find Bean Customizer
        err.println("=========================== Trying to find dialog Customize Bean.");
        //TEMP beanCustomizer = new BeanCustomizer(packageName+"."+beanName);
        //TEMP beanCustomizer.switchToPropertiesTab();
        beanCustomizer.verify();
        beanCustomizer.switchToPropertiesTab();
        err.println("=========================== Trying to find dialog Customize Bean - finished.");
        
        //propertyInitialValue = getValue(propertyName);
        
        //return beanCustomizer;
    }
    
    
    private void openComponentInspector() {
        
        if(componentInspector==null) {
            // open Form file
            err.println("=========================== Trying to open form file.");
            FormEditorWindow formWindow = PropertyEditorsSupport.openForTestPropertyEditors(err);
            err.println("=========================== Trying to open form file - finished.");
            
            // find Component Inspector
            err.println("=========================== Trying to find Component Inspector.");
            componentInspector = formWindow.getComponentInspector();
            err.println("=========================== Trying to find Component Inspector - finished.");
        }
        
        
        // select Other Components | PropertyEditorsBean
        err.println("=========================== Trying to find Other Components | " +beanName+".");
        componentInspector.selectNode("Other Components"+componentInspector.delim+beanName,false);
        JamUtilities.waitEventQueueEmpty(500);  // must wait, because sometimes properties tab isn't updated immediately after selection node 
        componentInspector.switchToPropertiesTab();
        err.println("=========================== Trying to find Other Components | " +beanName+" - finished.");
        
        //return componentInspector;
    }
    
    
    /* find Property Customizer - CUSTOMIZE BEAN
     */
    private PropertyCustomizer findPropertyCustomizer(String propertyName){
        err.println("=========================== Trying to find Property Customizer.");
        PropertyCustomizer propertyCustomizer = new PropertyCustomizer(propertyName);
        err.println("=========================== Trying to find Property Customizer - finished.");
        
        return propertyCustomizer;
    }
    
    
    /* find Property Customizer - Form Editor Customizer
     */
    private FormPropertyCustomizer findFormPropertyCustomizer(String propertyName){
        err.println("=========================== Trying to find Property Customizer.");
        FormPropertyCustomizer propertyCustomizer = new FormPropertyCustomizer(propertyName);
        err.println("=========================== Trying to find Property Customizer - finished.");
        
        return propertyCustomizer;
    }
    
    public void verifyExpectationValue(String propertyName, boolean expectation, String propertyValueExpectation, String propertyValue, boolean waitDialog){
        
        // Dialog isn't used for informing user about Invalid new value: Class,
        if(waitDialog) {
            getInformationDialog().ok();
            err.println("=========================== Dialog closed by [Ok].");
            
            if(propertyCustomizer!=null && propertyCustomizer.isShowing()){
                err.println("=========================== Property Customizer is still showing.");
                propertyCustomizer.cancel();
                err.println("=========================== Property Customizer closed by [Cancel].");
            }
            
        }
        
        String newValue = getValue(propertyName);
        String log = "Actual value is {"+newValue+"} and initial is{"+propertyInitialValue+"} - set value is {"+propertyValue+"} / expectation value is {"+propertyValueExpectation+"}";
        
        err.println("=========================== Trying to verify value ["+log+"].");
        
        if(expectation){
            if(newValue.equals(propertyValueExpectation) ) {
                log(log + " --> PASS");
            }else {
                fail(log + " --> FAIL");
            }
        }else {
            if(newValue.equals(propertyInitialValue)){
                log(log + " --> PASS");
            }else{
                fail(log + " --> FAIL");
            }
            
        }
    }
    
    
    public void initializeWorkplace() {
        
        if(!useForm) {
            openBeanCustomizer();
            //beanCustomizer.setText(propertyName, propertyValue);
        }else {
            openComponentInspector();
            //componentInspector.setText(propertyName, propertyValue);
        }
    }
    
    
/*    
    public String hackForJamPropertyButtonGetValue(String value) {
        // line 246 in JamPropertyButton , call getValue return not right value but some truncated value
        // this is the same code as for getValue, means excpectation value must be the same as this one
        return value.substring(value.lastIndexOf(':') + 2); // extra ++ for space
    }
 */   
    
    public void tearDown() {
        PropertyEditorsSupport.closeAllModal();
        
        /*
        FileSystem fs = new FileSystems().getFileSystem(PropertyEditorsSupport.getFS("data", "ClearJFrameWithPropertyEditorTestBean", "java"));
        if(useForm)
            ExplorerNode.find(fs, "data, ClearJFrameWithPropertyEditorTestBean").getActions().save();
       */ 
        
        /*        
        if(lastTest) {
            Editor e = Editor.find();
            e.switchToTab(PropertyEditorsSupport.beanName);
            e.pushPopupMenu("Save");
        }
        */
        
        /*
        if(useForm && lastTest) {
            org.netbeans.test.oo.gui.jelly.FileSystem fs = new org.netbeans.test.oo.gui.jelly.FileSystems().getFileSystem(PropertyEditorsSupport.getFS(PropertyEditorsSupport.Resources, "ClearJFrameWithPropertyEditorTestBean", "java"));
            org.netbeans.test.oo.gui.jelly.ExplorerNode.find(fs, PropertyEditorsSupport.Resources+", "+"ClearJFrameWithPropertyEditorTestBean").select();
            org.netbeans.test.oo.gui.jelly.MainFrame.getMainFrame().pushFileMenu("Save");
        }
         */
    }

    private void failTest(Exception exc, String message) {
        PropertyEditorsSupport.makeIDEScreenshot(this);
        err.println("################################");
        exc.printStackTrace(err);
        err.println("################################");
        fail(message);
    }
    
    
    public abstract void setCustomizerValue();
    
    public abstract void verifyPropertyValue(boolean expectation);
    
}
