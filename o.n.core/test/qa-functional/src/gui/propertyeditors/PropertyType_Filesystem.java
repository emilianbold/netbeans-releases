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

import org.netbeans.test.oo.gui.jelly.propertyEditors.customizers.FilesystemCustomizer;

import org.netbeans.junit.NbTestSuite;
import gui.propertyeditors.utilities.PropertyEditorsSupport;



/**
 * Tests of Identifier Array Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_Filesystem extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    private final String ADDDIRECTORY = "Add Directory:";
    private final String ADDJAR = "Add JAR:";
    
    private static String FS_Data_path;
    private static String FS_Data_path_data_jar;
    
    /** Creates a new instance of PropertyType_Filesystem */
    public PropertyType_Filesystem(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "p_filesystem";
        useForm = false;
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        String path = PropertyEditorsSupport.getSystemPath(PropertyEditorsSupport.Resources, PropertyEditorsSupport.beanName, "java");
        FS_Data_path = path.substring(0,path.lastIndexOf(System.getProperty("file.separator")));
        FS_Data_path_data_jar = FS_Data_path + System.getProperty("file.separator") + "data.jar";
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Filesystem("testCustomizerCancel"));
        suite.addTest(new PropertyType_Filesystem("testCustomizerAddDirectory"));
        suite.addTest(new PropertyType_Filesystem("testCustomizerAddJar"));
        return suite;
    }
    
    
    public void testCustomizerAddDirectory() {
        propertyValue_L = ADDDIRECTORY + FS_Data_path;
        propertyValueExpectation_L = FS_Data_path;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerAddJar() {
        propertyValue_L = ADDJAR + FS_Data_path_data_jar;
        propertyValueExpectation_L = FS_Data_path_data_jar;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = ADDJAR + FS_Data_path_data_jar;
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void setCustomizerValue() {
        FilesystemCustomizer customizer = new FilesystemCustomizer(propertyCustomizer);
        
        if(propertyValue_L.startsWith(ADDDIRECTORY)){
            customizer.addDirectory(getPath(propertyValue_L, ADDDIRECTORY),true);
        }
        
        if(propertyValue_L.startsWith(ADDJAR)){
            customizer.addJar(getPath(propertyValue_L, ADDJAR), true);
        }
        
        
    }
    
    public void verifyPropertyValue(boolean expectation) {
        //verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
        
        if(expectation){
            String newValue = getValue(propertyName_L);
            String log = "Actual value is {"+newValue+"} - set value is {"+propertyValue_L+"} / expectation value is {"+propertyValueExpectation_L+"}";
            
            err.println("=========================== Trying to verify value ["+log+"].");
            
            if(newValue.indexOf(propertyValueExpectation_L)!=-1) {
                log(log + " --> PASS");
            }else {
                fail(log + " --> FAIL");
            }
        }else {
            verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
        }
        
    }
    
    
    private String getPath(String str, String delim) {
        int index = str.indexOf(delim);
        
        if(index > -1)
            return str.substring(index + delim.length());
        
        return str;
    }
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Filesystem.class));
        junit.textui.TestRunner.run(suite());
    }
    
    
}
