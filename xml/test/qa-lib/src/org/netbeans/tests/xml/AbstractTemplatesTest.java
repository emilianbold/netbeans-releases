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
package org.netbeans.tests.xml;

import org.netbeans.modules.css.CSSObject;
import org.netbeans.test.oo.gui.jelly.Explorer;
import org.netbeans.test.oo.gui.jelly.Editor;
import org.openide.filesystems.FileObject;
import org.netbeans.test.oo.gui.jelly.xml.NewWizardDialog;
import java.io.File;
import org.netbeans.modules.xml.core.DTDDataObject;
import org.netbeans.modules.xml.core.XMLDataObject;
import org.netbeans.tests.xml.TestUtil;
import org.netbeans.tests.xml.JXTest;
import org.openide.loaders.DataObject;
import junit.textui.TestRunner;
import org.netbeans.test.oo.gui.jello.JelloBundle;
import org.openide.util.NbBundle;

/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module Jemmy Test: NewFromTemplate
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 *
 * This test tests New From Template action on all XML's templates.
 *
 * <BR><BR><B>How it works:</B><BR>
 *
 * 1) create new documents from template<BR>
 * 2) write the created documents to output<BR>
 * 3) close source editor<BR>
 *
 * <BR><BR><B>Settings:</B><BR>
 * none<BR>
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * Set XML documents.<BR>
 *
 * <BR><B>To Do:</B><BR>
 * none<BR>
 *
 * <P>Created on Januar 09, 2001, 12:33 PM
 * <P>
 */

public abstract class AbstractTemplatesTest extends JXTest {
    private final static String TEMPLATE_FOLDER = NbBundle.getBundle("org.netbeans.api.xml.resources.Bundle").getString("OpenIDE-Module-Display-Category");
    //private final static String TEMPLATE_FOLDER = JelloBundle.getStringFormat("org.netbeans.api.xml.resources.Bundle", "Templates/XML", null);
    
    /** Creates new TemplatesTest */
    public AbstractTemplatesTest(String testName) {
        super(testName);
    }
    
    // ABSTRACT ////////////////////////////////////////////////////////////////
    
    /**
     *  Returns Sring array with tested templates. The array have to this format:
     *  <code>String [][} {{"Template Name (localized)",  "file extension"}, {...}}</code>
     */
    protected abstract String[][] getTemplateList();
    
    /* templates names for futre use */
    //JelloBundle.getString(THIS_BUNDLE, "Templates/XML/XMLSchema.xml"),
    //JelloBundle.getString(CORE_BUNDLE, "Templates/XML/XMLCatalog.xml"),
    //JelloBundle.getString(CSS_BUNDLE,  "Templates/XML/CascadeStyleSheet.css"),
    
    // TESTS ///////////////////////////////////////////////////////////////////
    
    public void testNewFromTemplate() throws Exception {
        String testDirName = "XMLTestDir";
        String treePath = getWorkDir().getAbsolutePath() + "|" + testDirName;
        
        // create or clean test dir and mount it
        File testDir =  new File(getWorkDir(), testDirName);
        if (!testDir.exists()) {
            testDir.mkdir();
        } else {
            File fs[] = testDir.listFiles();
            for (int i=0; i<fs.length; i++) {
                fs[i].delete();
            }
        }
        TestUtil.mountDirectory(getWorkDir());
        
        String templates[][] = getTemplateList();
        Explorer exp = Explorer.find();
        exp.switchToFilesystemsTab();
        // weak up lazy menus
        exp.pushPopupMenuNoBlock("New|" + TEMPLATE_FOLDER, treePath);//!!!
        try {Thread.sleep(1000);} catch (Exception e) {}; //!!!
        exp.clickNode(treePath, 1);//!!!
        
        for (int i=0; i < templates.length; i++) {
            String name = templates[i][0];
            newFromTemplate(name, treePath, exp);
            Editor.find(name);
        }
        
        // write the created documents to output
        for (int i = 0; i < templates.length; i++) {
            String name = templates[i][0];
            String ext = templates[i][1];
            FileObject fo = TestUtil.findFileObject("XMLTestDir/" + name + "." + ext);
            DataObject dataObject = DataObject.find(fo);
            ref("\n+++ Document: " + dataObject.getName());
            
            String str = TestUtil.dataObjectToString(dataObject);
            if (dataObject instanceof CSSObject) {
                str = TestUtil.replaceString(str, "/*", "*/", "/* REMOVED */");
            } else {
                str = TestUtil.replaceString(str, "<!--", "-->", "<!-- REMOVED -->");
            }
            ref(str);
        }
        // close source editor
        new Editor().close();
        compareReferenceFiles();
    }
    
    // create new document from templates
    private void newFromTemplate(String templateName, String treePath, Explorer exp) {
        String menuPath = "New|" + TEMPLATE_FOLDER + "|" + templateName;
        try {
            exp.clickNode(treePath, 1);//!!!
            try {Thread.sleep(500);} catch (Exception e) {}; //!!!
            exp.pushPopupMenuNoBlock(menuPath, treePath);
            NewWizardDialog wizard = new NewWizardDialog();
            wizard.setJTextField(templateName);
            wizard.finish();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Cannot create document from : " + menuPath + "in: " + treePath);
        }
    }
}
