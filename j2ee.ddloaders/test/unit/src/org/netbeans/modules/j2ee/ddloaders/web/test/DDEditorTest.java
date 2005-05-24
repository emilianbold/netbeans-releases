/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web.test;
        
import java.io.File;
import java.io.IOException;
import junit.textui.TestRunner;
//import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.AssertionFailedErrorException;
import junit.framework.AssertionFailedError;
//import org.netbeans.junit.ide.ProjectSupport;

import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

import org.netbeans.modules.j2ee.ddloaders.web.test.util.Helper;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.DDBeanTableModel;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;

/**
 *
 * @author Milan Kuchtiak
 */
public class DDEditorTest extends NbTestCase {
    
    private static DDDataObject dObj;
    public DDEditorTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(DDEditorTest.class);
        //NbTestSuite suite = new NbTestSuite();
        //suite.addTest(new DDDataObjectTest("testDDDataObject"));
        return suite;
    }

    /** Renames
     *      tag handler
     *      servlet
     *      filter
     *      listener
     *  and checks renamed files, mytld.tld and web.xml for differences
     */
    public void testOpenDataObject() throws IOException {
        File f = Helper.getDDFile(getDataDir());
        FileObject fo = FileUtil.toFileObject(f);
        dObj = ((DDDataObject)DataObject.find(fo));
        
        assertNotNull("DD DataObject not found",dObj);
        
        EditCookie editCookie = (EditCookie)dObj.getCookie(EditCookie.class);
        editCookie.edit();
        // wait for editor
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex){}
    }
    
    public void testReplaceParamValueFromDDAPI() throws IOException {
        File original;
        String golden = "ReplaceParamValue.pass";
        assertNotNull("DD DataObject not found",dObj);
        
        File f = Helper.getDDFile(getDataDir());
        FileObject fo = FileUtil.toFileObject(f);
        
        WebApp webApp = DDProvider.getDefault().getDDRoot(fo);
        webApp.getContextParam()[0].setParamValue("Volvo");
        webApp.write(fo);
        
        // wait for saving file
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex){}
        
        original = Helper.getDDFile(getDataDir());
        assertFile(original, getGoldenFile(golden), getWorkDir());
    }
    
    public void testCheckParamValueInDesignView() {
        assertNotNull("DD DataObject not found",dObj);
        try {
            dObj.showElement(dObj.getWebApp().getContextParam()[0]);
        } catch (Exception ex) {
            throw new AssertionFailedErrorException("Failed to switch to Design View",ex);
        }
        
        // wait for opening Design View
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex){}
        
        try {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
               public void run(){
                   // get context params table model 
                   DDBeanTableModel ddBeanModel = Helper.getContextParamsTableModel(dObj);
                   assertNotNull("Table Model Not Found", ddBeanModel);
                   assertEquals("Context Params Table wasn't changed: ","Volvo",(String)ddBeanModel.getValueAt(0,1));
               } 
            });
        } catch (Exception ex) {
            throw new AssertionFailedErrorException("Failed to open Context Params section",ex);
        }
    }
    
    public void testAddParamValueInDesignView() throws IOException {
        assertNotNull("DD DataObject not found",dObj);
        try {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
               public void run(){
                   // get context params table model 
                   DDBeanTableModel ddBeanModel = Helper.getContextParamsTableModel(dObj);
                   assertNotNull("Table Model Not Found", ddBeanModel);
                   dObj.modelUpdatedFromUI();
                   ddBeanModel.addRow(new Object[]{"color","Blue",""});
                   // test the model
                   assertEquals("Context Param wasn't added to the model",2,dObj.getWebApp().sizeContextParam());
               } 
            });
        } catch (Exception ex) {
            throw new AssertionFailedErrorException("Failed to open Context Params section",ex);
        }
        // wait until model is updated
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex){}
        // open the XML View
        EditCookie editCookie = (EditCookie)dObj.getCookie(EditCookie.class);
        editCookie.edit();
        
        // wait to see the changes in XML view
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex){}       
        //test the editor document
        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport)dObj.getCookie(EditorCookie.class);
        javax.swing.text.Document document = editor.getDocument();
        try {
            String text = document.getText(0,document.getLength());
            int index = text.indexOf("<param-value>Blue</param-value>");
            assertEquals("Cannot find new context param element in XML view (editor document)",true,index>0);
        } catch (javax.swing.text.BadLocationException ex) {
            throw new AssertionFailedErrorException("Failed to read the document: ",ex);
        }
        
        // chack if save cookie was created
        SaveCookie cookie = (SaveCookie)dObj.getCookie(SaveCookie.class);
        assertNotNull("Data Object Not Modified",cookie);
        cookie.save();
        
        // compare to golden file
        String golden = "AddInitParam1.pass";
        File original = Helper.getDDFile(getDataDir());
        assertFile(original, getGoldenFile(golden), getWorkDir());
    }
    
    public void testAddParamValueInXmlView() throws IOException {
        assertNotNull("DD DataObject not found",dObj);

        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport)dObj.getCookie(EditorCookie.class);
        javax.swing.text.Document document = editor.getDocument();
        try {
            String text = document.getText(0,document.getLength());
            int index = text.lastIndexOf("</context-param>");
            document.insertString(index+16, "\n  <context-param>\n    <param-name>cylinders</param-name>\n    <param-value>6</param-value>\n  </context-param>", null);
        } catch (javax.swing.text.BadLocationException ex) {
            throw new AssertionFailedErrorException("Failed to read the document: ",ex);
        }

        // wait to see the changes in XML view
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex){}
        
        // open the Design View
        dObj.openView(0);
        
        // wait to see the changes in Design view
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex){}
        
        // check context params table in Design View
        try {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
               public void run(){
                   // get context params table model 
                   DDBeanTableModel ddBeanModel = Helper.getContextParamsTableModel(dObj);
                   assertNotNull("Table Model Not Found", ddBeanModel);
                   assertEquals("Context Params Table wasn't changed: ","cylinders",(String)ddBeanModel.getValueAt(2,0));
               } 
            });
        } catch (Exception ex) {
            throw new AssertionFailedErrorException("Failed to open Context Params section",ex);
        }

        // chack if save cookie was created
        SaveCookie cookie = (SaveCookie)dObj.getCookie(SaveCookie.class);
        assertNotNull("Data Object Not Modified",cookie);
        cookie.save();

        // compare to golden file
        String golden = "AddInitParam2.pass";
        File original = Helper.getDDFile(getDataDir());
        assertFile(original, getGoldenFile(golden), getWorkDir());
    }
    
    /**
     * Used for running test from inside the IDE by internal execution.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }    
}
