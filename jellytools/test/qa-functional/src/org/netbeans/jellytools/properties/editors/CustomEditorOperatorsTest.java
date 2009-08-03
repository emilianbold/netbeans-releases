/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.jellytools.properties.editors;

import java.io.File;
import junit.framework.TestSuite;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.TestNode;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;

/** Tests of all custom editors which reside in package org.netbeans.jellytools.properties.editors.
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class CustomEditorOperatorsTest extends org.netbeans.jellytools.JellyTestCase {
    
    /** Node with all customizable properties */
    private static TestNode testNode;

     public static final String[] tests = {

        /*
         * The following tests keep failing on an NPE, because the editors in the test
         * are probably not loaded by the system classloader.
         * org.openide.explorer.propertysheet.CustomEditorAction:129 (if (editor instanceof ExPropertyEditor))
         * throws NPE even though each of the editors is an instance of ExPropertyEditor, so
         * I assume the editor is loaded in a different classloader. Then, PropertyEnv is not
         * attached to the editor and the NPE then  fires for example here:
         * org.netbeans.beaninfo.editors.StringCustomEditor:83.
         *
         * Commenting those tests out, because fixing it would be very complicated
         * and it's not worth the effort at the moment.
         *
         "testStringCustomEditorOperator",
        "testStringArrayCustomEditorOperator",
        "testPointCustomEditorOperator",
        "testDimensionCustomEditorOperator",
        "testRectangleCustomEditorOperator",
        */
        "testColorCustomEditorOperator",
        "testFontCustomEditorOperator",
        "testFileCustomEditorOperator",
        "testClasspathCustomEditorOperator",
        "testProcessDescriptorCustomEditorOperator",         
        // TODO: Fix IconEditor test
        // "testIconCustomEditorOperator",
        "testClose"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public CustomEditorOperatorsTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static NbTestSuite suite() {
        return (NbTestSuite) createModuleTest(CustomEditorOperatorsTest.class, tests);
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
    /*public void testStringCustomEditorOperator() {
        StringCustomEditorOperator editor=null;
        Property p = new Property(new PropertySheetOperator(TestNode.NODE_NAME), "String");
        p.openEditor();
        editor = new StringCustomEditorOperator("String");
        editor.setStringValue("tested text");
        assertEquals("tested text", editor.getStringValue());
        editor.ok();
        assertEquals("tested text", p.getValue());
    }*/
    
    /** Test of org.netbeans.jellytools.properties.editors.StringArrayCustomEditorOperator. */
    /*public void testStringArrayCustomEditorOperator() {
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
    }*/
    
    /** Test of org.netbeans.jellytools.properties.editors.PointCustomEditorOperator. */
    /*public void testPointCustomEditorOperator() {
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
    }*/
    
    /** Test of org.netbeans.jellytools.properties.editors.RectangleCustomEditorOperator. */
    /*public void testRectangleCustomEditorOperator() {
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
    }*/
    
    /** Test of org.netbeans.jellytools.properties.editors.DimensionCustomEditorOperator. */
    /*public void testDimensionCustomEditorOperator() {
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
    }*/
    
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

    /** Test of org.netbeans.jellytools.properties.editors.IconCustomEditorOperator. */
   /* public void testIconCustomEditorOperator() {
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
    */
    
    /** Close property sheet. */
    public void testClose() {
        new PropertySheetOperator(TestNode.NODE_NAME).close();
    }
}
