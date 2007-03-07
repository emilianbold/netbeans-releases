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

package org.netbeans.jellytools.properties.editors;

import java.io.File;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.TestNode;

/** Tests of all custom editors which reside in package org.netbeans.jellytools.properties.editors.
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class CustomEditorOperatorsTest extends org.netbeans.jellytools.JellyTestCase {
    
    /** Node with all customizable properties */
    private static TestNode testNode;

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public CustomEditorOperatorsTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new org.netbeans.junit.NbTestSuite();
        suite.addTest(new CustomEditorOperatorsTest("testStringCustomEditorOperator"));
        suite.addTest(new CustomEditorOperatorsTest("testStringArrayCustomEditorOperator"));
        suite.addTest(new CustomEditorOperatorsTest("testPointCustomEditorOperator"));
        suite.addTest(new CustomEditorOperatorsTest("testDimensionCustomEditorOperator"));
        suite.addTest(new CustomEditorOperatorsTest("testRectangleCustomEditorOperator"));
        suite.addTest(new CustomEditorOperatorsTest("testColorCustomEditorOperator"));
        suite.addTest(new CustomEditorOperatorsTest("testFontCustomEditorOperator"));
        suite.addTest(new CustomEditorOperatorsTest("testFileCustomEditorOperator"));
        suite.addTest(new CustomEditorOperatorsTest("testClasspathCustomEditorOperator"));
        suite.addTest(new CustomEditorOperatorsTest("testProcessDescriptorCustomEditorOperator"));
        suite.addTest(new CustomEditorOperatorsTest("testServiceTypeCustomEditorOperator"));
        // don't know how to show filesystem editor
        //suite.addTest(new CustomEditorOperatorsTest("testFilesystemCustomEditorOperator"));
        // don't know how to show icon editor
        //suite.addTest(new CustomEditorOperatorsTest("testIconCustomEditorOperator"));
        suite.addTest(new CustomEditorOperatorsTest("testClose"));
        return suite;
    }
    
    
    /** Method called before each testcase. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");  // NOI18N
        if(testNode == null) {
            testNode = new TestNode();
            testNode.showProperties();
        }
    }
    
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Test of org.netbeans.jellytools.properties.editors.StringCustomEditorOperator. */
    public void testStringCustomEditorOperator() {
        StringCustomEditorOperator editor=null;
        Property p = new Property(new PropertySheetOperator(TestNode.NODE_NAME), "String");
        p.openEditor();
        editor = new StringCustomEditorOperator("String");
        editor.setStringValue("tested text");
        assertEquals("tested text", editor.getStringValue());
        editor.ok();
        assertEquals("tested text", p.getValue());
    }
    
    /** Test of org.netbeans.jellytools.properties.editors.StringArrayCustomEditorOperator. */
    public void testStringArrayCustomEditorOperator() {
        StringArrayCustomEditorOperator editor=null;
        Property p=new Property(new PropertySheetOperator(TestNode.NODE_NAME), "String []");
        p.openEditor();
        editor=new StringArrayCustomEditorOperator("String []");
        editor.setItemText("tested text 1");
        assertEquals("tested text 1", editor.getItemText());
        editor.add();
        editor.setItemText("tested text 2");
        editor.add();
        editor.lstItemList().selectItem("tested text 1");
        assertEquals(0, editor.lstItemList().getSelectedIndex());
        editor.down();
        assertEquals(1, editor.lstItemList().getSelectedIndex());
        editor.up();
        assertEquals(0, editor.lstItemList().getSelectedIndex());
        editor.down("tested text 1");
        assertEquals("down(String) failed.", 1, editor.lstItemList().getSelectedIndex());
        editor.up("tested text 1");
        assertEquals("up(String) failed.", 0, editor.lstItemList().getSelectedIndex());
        editor.setItemText("tested text 3");
        editor.edit();
        editor.remove("tested text 3");
        assertEquals(1, editor.lstItemList().getModel().getSize());
        editor.edit("tested text 2", "tested text 4");
        assertEquals("edit(String, String) failed.", "tested text 4", editor.getItemText());
        String s[]=new String[]{"aa","bb","cc"};
        editor.setStringArrayValue(s);
        String s2[]=editor.getStringArrayValue();
        assertEquals(s[0], s2[0]);
        assertEquals(s[1], s2[1]);
        assertEquals(s[2], s2[2]);
        editor.ok();
        assertEquals("aa, bb, cc", p.getValue());
    }
    
    /** Test of org.netbeans.jellytools.properties.editors.PointCustomEditorOperator. */
    public void testPointCustomEditorOperator() {
        PointCustomEditorOperator editor=null;
        Property p=new Property(new PropertySheetOperator(TestNode.NODE_NAME), "Point");
        p.openEditor();
        editor=new PointCustomEditorOperator("Point");
        editor.setPointValue("10","20");
        assertEquals("10", editor.getXValue());
        assertEquals("20", editor.getYValue());
        editor.setXValue("30");
        assertEquals("30", editor.getXValue());
        editor.setYValue("40");
        assertEquals("40", editor.getYValue());
        editor.ok();
        assertEquals("[30, 40]", p.getValue());
    }
    
    /** Test of org.netbeans.jellytools.properties.editors.RectangleCustomEditorOperator. */
    public void testRectangleCustomEditorOperator() {
        RectangleCustomEditorOperator editor=null;
        Property p=new Property(new PropertySheetOperator(TestNode.NODE_NAME), "Rectangle");
        p.openEditor();
        editor=new RectangleCustomEditorOperator("Rectangle");
        editor.setRectangleValue("10","20","30","40");
        assertEquals("10", editor.getXValue());
        assertEquals("20", editor.getYValue());
        assertEquals("30", editor.getWidthValue());
        assertEquals("40", editor.getHeightValue());
        editor.setXValue("50");
        assertEquals("50", editor.getXValue());
        editor.setYValue("60");
        assertEquals("60", editor.getYValue());
        editor.setWidthValue("70");
        assertEquals("70", editor.getWidthValue());
        editor.setHeightValue("80");
        assertEquals("80", editor.getHeightValue());
        editor.ok();
        assertEquals("[50, 60, 70, 80]", p.getValue());
    }
    
    /** Test of org.netbeans.jellytools.properties.editors.DimensionCustomEditorOperator. */
    public void testDimensionCustomEditorOperator() {
        DimensionCustomEditorOperator editor=null;
        Property p=new Property(new PropertySheetOperator(TestNode.NODE_NAME), "Dimension");
        p.openEditor();
        editor=new DimensionCustomEditorOperator("Dimension");
        editor.setDimensionValue("10","20");
        assertEquals("10", editor.getWidthValue());
        assertEquals("20", editor.getHeightValue());
        editor.setWidthValue("30");
        assertEquals("30", editor.getWidthValue());
        editor.setHeightValue("40");
        assertEquals("40", editor.getHeightValue());
        editor.ok();
        assertEquals("[30, 40]", p.getValue());
    }
    
    /** Test of org.netbeans.jellytools.properties.editors.ColorCustomEditorOperator. */
    public void testColorCustomEditorOperator() {
        ColorCustomEditorOperator editor=null;
        Property p=new Property(new PropertySheetOperator(TestNode.NODE_NAME), "Color");
        p.openEditor();
        editor=new ColorCustomEditorOperator("Color");
        editor.setRGBValue(10, 20, 30);
        assertEquals(new java.awt.Color(10, 20, 30), editor.getColorValue());
        java.awt.Color c=new java.awt.Color(40, 50, 60);
        editor.setColorValue(c);
        assertEquals(c, editor.getColorValue());
        editor.ok();
    }
    
    /** Test of org.netbeans.jellytools.properties.editors.FontCustomEditorOperator. */
    public void testFontCustomEditorOperator() {
        FontCustomEditorOperator editor=null;
        PropertySheetOperator pso = new PropertySheetOperator(TestNode.NODE_NAME);
        Property p=new Property(pso, "Font");
        p.openEditor();
        editor=new FontCustomEditorOperator("Font");
        editor.setFontName("Serif");
        assertTrue(editor.getFontName().indexOf("Serif")>=0);
        editor.setFontStyle(editor.STYLE_BOLDITALIC);
        assertEquals(editor.STYLE_BOLDITALIC, editor.getFontStyle());
        editor.setFontSize("14");
        assertEquals("14", editor.getFontSize());
        editor.ok();
        // need to change selection because it gets editable otherwise
        pso.tblSheet().selectCell(0, 0);
    }
    
    /** Test of org.netbeans.jellytools.properties.editors.FileCustomEditorOperator. */
    public void testFileCustomEditorOperator() throws Exception {
        FileCustomEditorOperator editor=null;
        Property p=new Property(new PropertySheetOperator(TestNode.NODE_NAME), "File");
        p.openEditor();
        editor=new FileCustomEditorOperator("File");
        editor.setFileValue(getWorkDir().getAbsolutePath());
        assertEquals(getWorkDir(), editor.getFileValue());
        editor.ok();
        assertEquals(getWorkDir().getAbsolutePath(), p.getValue());
    }
    
    /** Test of org.netbeans.jellytools.properties.editors.ClasspathCustomEditorOperator. */
    public void testClasspathCustomEditorOperator() throws Exception {
        ClasspathCustomEditorOperator editor=null;
        Property p=new Property(new PropertySheetOperator(TestNode.NODE_NAME), "NbClassPath");
        p.openEditor();
        editor=new ClasspathCustomEditorOperator("NbClassPath");
        editor.addJARZIP().close();
        editor.addDirectory(getWorkDir());
        assertEquals(1, editor.getClasspathValue().length);
        assertEquals(getWorkDir().getAbsolutePath().toLowerCase(), editor.getClasspathValue()[0].toLowerCase());
        File parent = getWorkDir().getParentFile();
        editor.addDirectory(parent);
        assertEquals(2, editor.getClasspathValue().length);
        assertEquals(parent.getAbsolutePath().toLowerCase(), editor.getClasspathValue()[1].toLowerCase());
        editor.lstClasspath().selectItem(0);
        editor.moveDown();
        assertEquals(getWorkDir().getAbsolutePath().toLowerCase(), editor.getClasspathValue()[1].toLowerCase());
        editor.lstClasspath().selectItem(1);
        editor.moveUp();
        assertEquals(getWorkDir().getAbsolutePath().toLowerCase(), editor.getClasspathValue()[0].toLowerCase());
        editor.remove(parent.getAbsolutePath());
        assertEquals(1, editor.getClasspathValue().length);
        String s[] = new String[]{ getWorkDir().getAbsolutePath(), parent.getAbsolutePath()};
        editor.setClasspathValue(s);
        String s2[] = editor.getClasspathValue();
        assertEquals(s[0].toLowerCase(), s2[0].toLowerCase());
        assertEquals(s[1].toLowerCase(), s2[1].toLowerCase());
        editor.ok();
        assertTrue(p.getValue().toLowerCase().indexOf(s[0].toLowerCase()+File.pathSeparator+s[1].toLowerCase())>=0);
    }
    
    /** Test of org.netbeans.jellytools.properties.editors.ProcessDescriptorCustomEditorOperator. */
    public void testProcessDescriptorCustomEditorOperator() {
        ProcessDescriptorCustomEditorOperator editor=null;
        Property p=new Property(new PropertySheetOperator(TestNode.NODE_NAME), "NbProcessDescriptor");
        p.openEditor();
        editor=new ProcessDescriptorCustomEditorOperator("NbProcessDescriptor");
        editor.selectProcessExecutable().close();
        editor.setProcess("test process");
        assertEquals("test process", editor.getProcess());
        editor.setArguments("test arguments");
        assertEquals("test arguments", editor.getArguments());
        assertEquals("", editor.getArgumentKey());
        editor.ok();
        assertEquals("test process test arguments", p.getValue());
    }
    
    /** Test of org.netbeans.jellytools.properties.editors.ServiceTypeCustomEditorOperator. */
    public void testServiceTypeCustomEditorOperator() {
        ServiceTypeCustomEditorOperator editor=null;
        Property p=new Property(new PropertySheetOperator(TestNode.NODE_NAME), "Service Type");
        p.openEditor();
        editor=new ServiceTypeCustomEditorOperator("Service Type");
        // "No Indentation"
        String noIndentationLabel = Bundle.getString("org.netbeans.beaninfo.Bundle", "LAB_IndentEngineDefault");  // NOI18N
        editor.setServiceTypeValue(noIndentationLabel);
        assertEquals(noIndentationLabel, editor.getServiceTypeValue());
        editor.ok();
        assertEquals(noIndentationLabel, p.getValue());
    }
    
    /** Tests FilesystemCustomEditorOperator. */
    public void testFilesystemCustomEditorOperator() {
        Property p = new Property(new PropertySheetOperator(TestNode.NODE_NAME), "Filesystem");
        p.openEditor();
        FilesystemCustomEditorOperator editor = new FilesystemCustomEditorOperator("Filesystem");
        editor.addLocalDirectory();
        assertTrue("Add Local Directory radio button not pushed.", editor.rbAddLocalDirectory().isSelected());
        editor.setDirectory("localDirectory");
        assertEquals("Wrong value in Directory text field.", "localDirectory", editor.getDirectory());
        editor.browse();
        String userdir = System.getProperty("netbeans.user");
        org.netbeans.jemmy.operators.JFileChooserOperator fileChooserOper = new org.netbeans.jemmy.operators.JFileChooserOperator();
        fileChooserOper.setSelectedFile(new File(userdir));
        fileChooserOper.approve();
        assertEquals("Wrong value in Directory text field from file chooser.", userdir, editor.getDirectory());
        // test JAR File
        editor.addJARFile();
        assertTrue("Add JAR File radio button not pushed.", editor.rbAddJARFile().isSelected());
        editor.setJARFile("jarFile");
        assertEquals("Wrong value in Directory text field.", "jarFile", editor.getJARFile());
        editor.browse2();
        fileChooserOper = new org.netbeans.jemmy.operators.JFileChooserOperator();
        String nbhome = System.getProperty("netbeans.home");
        String jarPath = nbhome+File.separator+"lib"+File.separator+"core.jar";
        fileChooserOper.setSelectedFile(new File(jarPath));
        fileChooserOper.approve();
        assertEquals("Wrong value in JAR File text field from file chooser.", jarPath, editor.getJARFile());
        editor.ok();
        assertTrue("Editor doesn't save value after OK.", p.getValue().indexOf("core.jar")>-1);
    }

    /** Test of org.netbeans.jellytools.properties.editors.IconCustomEditorOperator. */
    public void testIconCustomEditorOperator() {
        Property p=new Property(new PropertySheetOperator(TestNode.NODE_NAME), "Icon");
        p.openEditor();
        IconCustomEditorOperator editor = new IconCustomEditorOperator("Icon");
        editor.uRL();
        assertTrue("URL radio button not pushed.", editor.rbURL().isSelected());
        editor.noPicture();
        assertTrue("No picture radio button not pushed.", editor.rbNoPicture().isSelected());
        editor.classpath();
        assertTrue("Classpath radio button not pushed.", editor.rbClasspath().isSelected());
        editor.file();
        assertTrue("File radio button not pushed.", editor.rbFile().isSelected());
        editor.selectFile();
        new NbDialogOperator(org.netbeans.jellytools.Bundle.getString("org.openide.explorer.propertysheet.editors.Bundle", "CTL_OpenDialogName")).cancel();
        editor.setName("iconFile");
        assertEquals("Wrong value in Name text field", "iconFile", editor.getName());
        editor.ok();
        assertTrue("Editor doesn't save value after OK.", p.getValue().indexOf("iconFile")>-1);
    }
    
    /** Close property sheet. */
    public void testClose() {
        new PropertySheetOperator(TestNode.NODE_NAME).close();
    }
}
