package gui;

import org.netbeans.test.oo.gui.jelly.*;
import org.netbeans.test.oo.gui.jam.*;
import org.netbeans.test.oo.gui.jello.*;
import org.netbeans.test.oo.gui.jelly.beans.JBWizard;

import java.util.Hashtable;
import java.io.File;
import java.io.PrintWriter;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
//import java.io.File;


public class BeansTemplates extends NbTestCase {
    
    private static final String NAME_JAVA_BEAN          = "MyBean";
    private static final String NAME_BEAN_INFO          = "MyBeanInfo";
    private static final String NAME_BEAN_INFO_NO_ICON  = "MyBeanInfoNoIcon";
    private static final String NAME_CUSTOMIZER         = "MyCustomizer";
    private static final String NAME_PROPERTY_EDITOR    = "MyPropertyEditor";

    
    
    private String sampleDir;
    
    /** Need to be defined because of JUnit */
    public BeansTemplates(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new BeansTemplates("testJavaBean"));
        suite.addTest(new BeansTemplates("testBeanInfo"));
        suite.addTest(new BeansTemplates("testBeanInfoNoIcon"));
        suite.addTest(new BeansTemplates("testCustomizer"));
        suite.addTest(new BeansTemplates("testPropertyEditor"));
        suite.addTest(new BeansTemplates("testRemoveMyObjects"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new BeansTemplates("testJavaBean"));
    }
    
    public void setUp() {
        // redirect jemmy trace and error output to a log
        JellyProperties.setJemmyOutput(new PrintWriter(getLog(), true), new PrintWriter(getLog(), true));
        JellyProperties.setDefaults();
        sampleDir = mountSampledir();
    }
    
    public void tearDown() {       
        JamUtilities.waitEventQueueEmpty(3000);
    }

    public void testJavaBean() {
        JBWizard jbw = JBWizard.launch(JelloBundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/Bean.java"), sampleDir);
        jbw.setName(NAME_JAVA_BEAN);
        jbw.finish();
        Editor editor = new Editor(NAME_JAVA_BEAN);      
        editor.select(1,5);
        editor.deleteSelectedText();

        editor.select(4,7);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles();
    }   

    public void testBeanInfo() {
        JBWizard jbw = JBWizard.launch(JelloBundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/BeanInfo.java"), sampleDir);
        jbw.setName(NAME_BEAN_INFO);
        jbw.finish();
        Editor editor = new Editor(NAME_BEAN_INFO);      
        ref(editor.getText());
        compareReferenceFiles();
    }   

    public void testBeanInfoNoIcon() {
        JBWizard jbw = JBWizard.launch(JelloBundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/BeanInfoNoIcon.java"), sampleDir);
        jbw.setName(NAME_BEAN_INFO_NO_ICON);
        jbw.finish();
        Editor editor = new Editor(NAME_BEAN_INFO_NO_ICON);      
        ref(editor.getText());
        compareReferenceFiles();
    }   

    public void testCustomizer() {
        JBWizard jbw = JBWizard.launch(JelloBundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/Customizer.java"), sampleDir);
        jbw.setName(NAME_CUSTOMIZER);
        jbw.finish();
        Editor editor = new Editor(NAME_CUSTOMIZER);      
        editor.select(1,10);
        editor.deleteSelectedText();
        ref(editor.getText());
        compareReferenceFiles();
    }   

    public void testPropertyEditor() {
        JBWizard jbw = JBWizard.launch(JelloBundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/PropertyEditor.java"), sampleDir);
        jbw.setName(NAME_PROPERTY_EDITOR);
        jbw.finish();
        Editor editor = new Editor(NAME_PROPERTY_EDITOR);      
        editor.select(1,5);
        editor.deleteSelectedText();

        editor.select(4,7);
        editor.deleteSelectedText();        
        ref(editor.getText());
        compareReferenceFiles();
    }   
    
    public void testRemoveMyObjects() {
        Explorer explorer = new Explorer();
        
        explorer = Explorer.find();
        explorer.switchToFilesystemsTab();       
        
        String userdir = System.getProperty("netbeans.user");        
        String myObjects [] = {userdir+File.separator+"sampledir"+explorer.delim+NAME_JAVA_BEAN,
                              userdir+File.separator+"sampledir"+explorer.delim+NAME_BEAN_INFO,
                              userdir+File.separator+"sampledir"+explorer.delim+NAME_BEAN_INFO_NO_ICON,
                              userdir+File.separator+"sampledir"+explorer.delim+NAME_CUSTOMIZER,
                              userdir+File.separator+"sampledir"+explorer.delim+NAME_PROPERTY_EDITOR
                            };        
        for (int i=0;i<myObjects.length;i++) {
            System.out.println("myObjects["+i+"] = "+myObjects[i]);
            explorer.pushPopupMenu("Delete", myObjects[i]);
            new JelloYesNoDialog("Confirm Object Deletion").yes();
        }                            
        explorer.pushPopupMenu("Unmount Filesystem", userdir+File.separator+"sampledir");                            
    }
    
    
    /** Mounts <userdir>/sampledir through API
     * @return absolute path of mounted dir
     */
    public String mountSampledir() {
        String userdir = System.getProperty("netbeans.user");
        String mountPoint = userdir+File.separator+"sampledir";
        new JelloRepository().findOrMount(mountPoint);
        return mountPoint;
    }
    
}
