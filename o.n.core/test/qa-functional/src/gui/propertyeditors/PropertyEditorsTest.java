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

import gui.propertyeditors.data.PropertiesTest;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NbFrameOperator;
import org.netbeans.jellytools.TopComponentOperator;

import org.netbeans.jellytools.properties.ComboBoxProperty;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.PropertySheetTabOperator;
import org.netbeans.jellytools.properties.TextFieldProperty;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;

import org.netbeans.jemmy.operators.FrameOperator;
import org.netbeans.jemmy.operators.Operator;


/**
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public abstract class PropertyEditorsTest extends JellyTestCase {
    
    protected PrintStream err;
    protected PrintStream log;
    
    public String propertyInitialValue;
//    public String propertyName;
    public String propertyValue;
    
    protected NbDialogOperator propertyCustomizer;
    
    //protected static NbDialogOperator beanCustomizer = null;
    protected static FrameOperator propertiesWindow = null;
    
    private static final String CAPTION = "\n===========================";
    
    /** Creates a new instance of PropertyEditorsTest */
    public PropertyEditorsTest(String testName) {
        super(testName);
    }
    
    
    public void setUp() {
        //err = System.out;
        err = getLog();
        log = getRef();
        
        try {
            JemmyProperties.getProperties().setOutput(new TestOut(null, new PrintWriter(err, true), new PrintWriter(err, true), null));
            initializeWorkplace();
        }catch(Exception exc) {
            failTest(exc, "SetUp failed. It seems like initializeWorkplace cause exception:"+exc.getMessage());
        }
    }
    
    
    
    public void setByCustomizerOk(String propertyName, boolean expectance){
        try {
            err.println(CAPTION + " Trying to set value by customizer-ok {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            openAndGetPropertyCustomizer(propertyName);
            setCustomizerValue();
            
            if(propertyCustomizer.isShowing())
                propertyCustomizer.ok();
            
            err.println(CAPTION + " Trying to set value by customizer-ok {name="+propertyName+" / value="+propertyValue+"} - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCustomizer("+propertyName+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    
    
    public void setByCustomizerCancel(String propertyName, boolean expectance) {
        try {
            err.println(CAPTION + " Trying to set value by customizer-cancel {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            openAndGetPropertyCustomizer(propertyName);
            setCustomizerValue();

            if(propertyCustomizer.isShowing())
                propertyCustomizer.cancel();
            
            err.println(CAPTION + " Trying to set value by customizer-cancel {name="+propertyName+" / value="+propertyValue+"} - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCustomizerCancel("+propertyName+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    
    
    public void setByInPlace(String propertyName, String propertyValue, boolean expectance) {
        try {
            err.println(CAPTION + " Trying to set value by in-place {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            
            //H1 PropertySheetTabOperator propertiesTab = new PropertySheetTabOperator(new PropertySheetOperator(propertiesWindow));
            //H1 new TextFieldProperty(propertiesTab, propertyName).setValue(propertyValue);
            ((TextFieldProperty) findProperty(propertyName, "TextFieldProperty")).setValue(propertyValue);
            
            err.println(CAPTION + " Trying to set value by in-place {name="+propertyName+" / value="+propertyValue+"}  - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByInPlace("+propertyName+", "+propertyValue+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    
    
    public void setByCombo(String propertyName, String propertyValue, boolean expectance) {
        try {
            err.println(CAPTION + " Trying to set value by combo box {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            
            //H1 PropertySheetTabOperator propertiesTab = new PropertySheetTabOperator(new PropertySheetOperator(propertiesWindow));
            //H1 new ComboBoxProperty(propertiesTab, propertyName).setValue(propertyValue);
            ((ComboBoxProperty) findProperty(propertyName,"ComboBoxProperty")).setValue(propertyValue);
            
            err.println(CAPTION + " Trying to set value by combo box {name="+propertyName+" / value="+propertyValue+"}  - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCombo("+propertyName+", "+propertyValue+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    
    
    public void setByCombo(String propertyName, int propertyValueIndex, boolean expectance) {
        try {
            err.println(CAPTION + " Trying to set value by combo box {name="+propertyName+" / value="+propertyValueIndex+"} .");
            propertyInitialValue = getValue(propertyName);
            
            //H1 PropertySheetTabOperator propertiesTab = new PropertySheetTabOperator(new PropertySheetOperator(propertiesWindow));
            //H1 new ComboBoxProperty(propertiesTab, propertyName).setValue(propertyValueIndex);
            ((ComboBoxProperty) findProperty(propertyName, "ComboBoxProperty")).setValue(propertyValueIndex);
            
            err.println(CAPTION + " Trying to set value by combo box {name="+propertyName+" / value="+propertyValueIndex+"}  - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCombo("+propertyName+", "+propertyValueIndex+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    private void verifyCustomizer(String propertyName){
        try {
            err.println(CAPTION + " Trying to verify customizer {name="+propertyName+"} .");
            openAndGetPropertyCustomizer(propertyName);
            verifyCustomizerLayout();

            if(propertyCustomizer.isShowing())
                propertyCustomizer.cancel();
            
            err.println(CAPTION + " Trying to verify customizer {name="+propertyName+"}  - finished.");
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: Verification of Property Customizer Layout for property("+propertyName+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    private NbDialogOperator openAndGetPropertyCustomizer(String propertyName) {
        // open Property Editor
        err.println(CAPTION + " Trying to open Property Customizer{"+JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout")+"}.");

        //H1 PropertySheetTabOperator propertiesTab = new PropertySheetTabOperator(new PropertySheetOperator(propertiesWindow));
        //H1 new Property(propertiesTab, propertyName).openEditor();
        findProperty(propertyName, "").openEditor();
        
        err.println(CAPTION + " Trying to open Property Customizer - finished.");
        propertyCustomizer = findPropertyCustomizer(propertyName);
        
        return propertyCustomizer;
    }
    
    public NbDialogOperator getPropertyCustomizer() {
        return propertyCustomizer;
    }
    
    public NbDialogOperator getInformationDialog() {
        String title = "Information";
        err.println(CAPTION + " Waiting dialog {"+title+"} .");
        NbDialogOperator dialog = new NbDialogOperator(title);
        err.println(CAPTION + " Waiting dialog {"+title+"} - finished.");
        return dialog;
    }
    
    
    public String getValue(String propertyName) {
        String returnValue;
        
        //H1 PropertySheetTabOperator propertiesTab = new PropertySheetTabOperator(new PropertySheetOperator(propertiesWindow));
        //H1 returnValue = new Property(propertiesTab, propertyName).getValue();
        returnValue = findProperty(propertyName, "").getValue();
        
        err.println("GET VALUE = [" + returnValue + "].");
        
        return returnValue;
    }
    
    
    /* find Property Customizer 
     */
    private NbDialogOperator findPropertyCustomizer(String propertyName){
        err.println(CAPTION + " Trying to find Property Customizer.");
        NbDialogOperator propertyCustomizer = new NbDialogOperator(propertyName);
        err.println(CAPTION + " Trying to find Property Customizer - finished.");
        return propertyCustomizer;
    }
    
    
    public void verifyExpectationValue(String propertyName, boolean expectation, String propertyValueExpectation, String propertyValue, boolean waitDialog){
        
        // Dialog isn't used for informing user about Invalid new value: Class,
        if(waitDialog) {
            getInformationDialog().ok();
            err.println(CAPTION + " Dialog closed by [Ok].");
            
            if(propertyCustomizer!=null && propertyCustomizer.isShowing()){
                err.println(CAPTION + " Property Customizer is still showing.");
                propertyCustomizer.cancel();
                err.println(CAPTION + " Property Customizer closed by [Cancel].");
            }
            
        }
        
        String newValue = getValue(propertyName);
        String log = "Actual value is {"+newValue+"} and initial is{"+propertyInitialValue+"} - set value is {"+propertyValue+"} / expectation value is {"+propertyValueExpectation+"}";
        
        err.println(CAPTION + " Trying to verify value ["+log+"].");
        
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
        openPropertySheet();
    }
    
    
    private void openPropertySheet() {
        err.println(CAPTION + " Trying to run PropertiesTest");
        
        if(propertiesWindow==null){
            new PropertiesTest();
            // beanCustomizer = new TopComponentOperator(Bundle.getString("org.openide.nodes.Bundle", "Properties"));
            //beanCustomizer = new TopComponentOperator(Bundle.getString("org.netbeans.core.Bundle","CTL_FMT_LocalProperties",new Object[] {new Integer(1), "TestN"}));
            propertiesWindow = new FrameOperator("Properties of Tes");
            
            // Next code doesn't work because seDefaultStringComparator sets comparator for all Operators
            // PropertySheetOperator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));
        }
        err.println(CAPTION + " Trying to run PropertiesTest - finished.");
    }
    
    
    /* 
     * Find Property in Property Sheet and return them. 
     * This is first hack for new Jelly2, because it isn't possible to set String Comparator only for one operator.
     * H1
     */
    private Property findProperty(String propertyName, String type) {
        Operator.StringComparator oldComparator = Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));
        
        PropertySheetTabOperator propertiesTab = new PropertySheetTabOperator(new PropertySheetOperator(propertiesWindow));
        Property property;
        
        if(type.indexOf("TextFieldProperty")!=-1)
            property = new TextFieldProperty(propertiesTab, propertyName);
        else if(type.indexOf("ComboBoxProperty")!=-1)
            property = new ComboBoxProperty(propertiesTab, propertyName);
        else
            property = new Property(propertiesTab, propertyName);
        
        Operator.setDefaultStringComparator(oldComparator);
        
        return property;
    }
    
/*
    public String hackForJamPropertyButtonGetValue(String value) {
        // line 246 in JamPropertyButton , call getValue return not right value but some truncated value
        // this is the same code as for getValue, means excpectation value must be the same as this one
        return value.substring(value.lastIndexOf(':') + 2); // extra ++ for space
    }
 */
    
    public void tearDown() {
        closeAllModal();
        
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
        err.println("################################");
        exc.printStackTrace(err);
        err.println("################################");
        fail(message);
    }
    
    
    public abstract void setCustomizerValue();
    
    public abstract void verifyCustomizerLayout();
    
    public abstract void verifyPropertyValue(boolean expectation);
    
}
