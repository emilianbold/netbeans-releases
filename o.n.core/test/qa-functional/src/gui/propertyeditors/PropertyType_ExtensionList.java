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

//import org.netbeans.test.oo.gui.jelly.propertyEditors.customizers.StringArrayCustomizer;

//import org.netbeans.test.oo.gui.jam.JamButton;
import org.netbeans.test.oo.gui.jam.JamTextField;
import org.netbeans.test.oo.gui.jam.JamList;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.oo.gui.jello.JelloBundle;
import org.netbeans.test.oo.gui.jelly.propertyEditors.customizers.CustomizerContainer;



/**
 * Tests of Extension List Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_ExtensionList extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    private final String ADD = "Add:";
    private final String REMOVE = "Remove:";
    private final String EDIT = "Edit:";
    private final String UP = "Up:";
    private final String DOWN = "Down:";
    
    private final String EE = "; ";
    
    /** Creates a new instance of PropertyType_ExtensionList */
    public PropertyType_ExtensionList(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "p_extensionList";
        useForm = false;
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_ExtensionList("testByInPlace"));
        suite.addTest(new PropertyType_ExtensionList("testCustomizerCancel"));
        suite.addTest(new PropertyType_ExtensionList("testCustomizerAdd"));
        suite.addTest(new PropertyType_ExtensionList("testCustomizerRemove"));
        suite.addTest(new PropertyType_ExtensionList("testCustomizerEdit"));
        return suite;
    }
    
    
    public void testCustomizerAdd() {
        propertyValue_L = ADD + "java";
        propertyValueExpectation_L = "gif, html, java, jpg, png";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerEdit() {
        propertyValue_L = EDIT + "html" + EE + "newHtml";
        propertyValueExpectation_L = "java, jpg, newHtml, png";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerRemove() {
        propertyValue_L = REMOVE + "gif";
        propertyValueExpectation_L = "html, java, jpg, png";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }

    public void testCustomizerCancel(){
        propertyValue_L = ADD + "cancel";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "gif, jpg, png, html";
        propertyValueExpectation_L = "gif, html, jpg, png";
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void setCustomizerValue() {
        ExtensionCustomizer customizer = new ExtensionCustomizer(propertyCustomizer);
        
        if(propertyValue_L.startsWith(ADD)){
            customizer.addItem(getItem(propertyValue_L,ADD));
        }
        
        if(propertyValue_L.startsWith(REMOVE)){
            customizer.removeItem(getItem(propertyValue_L,REMOVE));
        }
        
        if(propertyValue_L.startsWith(EDIT)){
            customizer.editItem(getItem(propertyValue_L,EDIT), getItem(propertyValue_L,EE));
        }
        
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    
    private String getItem(String str, String delim) {
        int first = str.indexOf(delim);
        int end = str.indexOf(EE);

        if(end > 0 && !delim.equals(EE)){
            return str.substring(delim.length(), end);
        } else {
            return str.substring(first + delim.length());
        }
    }
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_ExtensionList.class));
        junit.textui.TestRunner.run(suite());
    }
    
}


class ExtensionCustomizer extends CustomizerContainer {
    
    public JamTextField textField;
    public JamList itemList;
    
    public ExtensionCustomizer(org.netbeans.test.oo.gui.jelly.propertyEditors.PropertyCustomizer propertyCustomizer) {
        super(propertyCustomizer);
    }

    /** Verify constitution of this customizer - container. */
    public void verify(){
        textField = getJamTextField(0);
        itemList = getJamList(0);
    }
    
    /** Add string.                                                                                                                                                    
     * @param item string to be added */                                                                                                                               
    public void addItem(String item){                                                                                                                                  
        textField.setText(item);                                                                                                                                       
        getJamButton(JelloBundle.getString(propertySheetEditorsBundle, "CTL_Add_StringArrayCustomEditor")).doClick();
    }                                                                                                                                                                  
                                                                                                                                                                       
    /** Remove string.                                                                                                                                                 
     * @param item string to be removed */                                                                                                                             
    public void removeItem(String item) {                                                                                                                              
        selectItem(item);                                                                                                                                              
        getJamButton(JelloBundle.getString(propertySheetEditorsBundle, "CTL_Remove")).doClick();                                                                                                                                        
    }                                                                                                                                                                  
                                                                                                                                                                       
    /** Edit string.                                                                                                                                                   
     * @param item string to be edite                                                                                                                                  
     * @param newValue new value of string */                                                                                                                          
    public void editItem(String item, String newValue) {                                                                                                               
        selectItem(item);                                                                                                                                              
        textField.setText(newValue);                                                                                                                                   
        getJamButton(JelloBundle.getString(propertySheetEditorsBundle, "CTL_Change_StringArrayCustomEditor")).doClick();                                                                                                                                          
    }                                                                                                                                                                  

   /** Select string.                                                                                                                                                 
     * @param item string to be selected */                                                                                                                            
    public void selectItem(String item) {                                                                                                                              
        itemList.selectItem(item);                                                                                                                                     
        org.netbeans.test.oo.gui.jam.JamUtilities.waitEventQueueEmpty(100);                                                                                            
    }                                                                                                                                                                  
     
}
