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

import org.netbeans.test.oo.gui.jelly.propertyEditors.customizers.NbProcessDescriptorCustomizer;

import org.netbeans.junit.NbTestSuite;
import gui.propertyeditors.utilities.PropertyEditorsSupport;



/**
 * Tests of NbProcessDescriptor Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_NbProcessDescriptor extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    private String DELIM = ";";
    
    private static String FS_Data_path;
    
    /** Creates a new instance of PropertyType_NbProcessDescriptor */
    public PropertyType_NbProcessDescriptor(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "p_NbProcessDescriptor";
        useForm = false;
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        String path = PropertyEditorsSupport.getSystemPath(PropertyEditorsSupport.Resources, PropertyEditorsSupport.beanName, "java");
        FS_Data_path = path.substring(0,path.lastIndexOf(System.getProperty("file.separator")));
        
        NbTestSuite suite = new NbTestSuite();
        //suite.addTest(new PropertyType_NbProcessDescriptor("testByInPlace"));
        //suite.addTest(new PropertyType_NbProcessDescriptor("testCustomizerCancel"));
        suite.addTest(new PropertyType_NbProcessDescriptor("testCustomizerOk"));
        return suite;
    }
    
    
    public void testCustomizerOk() {
        propertyValue_L = FS_Data_path + DELIM + "okArgument";
        propertyValueExpectation_L =  FS_Data_path + " okArgument";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "cancel" + DELIM + "cancel_argument";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "process argument";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void setCustomizerValue() {
        NbProcessDescriptorCustomizer customizer = new NbProcessDescriptorCustomizer(propertyCustomizer);
        
        int index = propertyValue_L.indexOf(DELIM);
        String process = propertyValue_L.substring(0,index);
        String argument = propertyValue_L.substring(index + DELIM.length());
        
        System.out.println("PREOCESS="+process+"   ARGUMENT="+argument);
        customizer.setProcess(process,false);
        customizer.setArguments(argument);
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_NbProcessDescriptor.class));
        junit.textui.TestRunner.run(suite());
    }
    
    
}
