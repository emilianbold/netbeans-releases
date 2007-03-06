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
package org.netbeans.jellytools.properties;

import java.io.File;
import javax.swing.JDialog;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.editors.FontCustomEditorOperator;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JDialogOperator;

/** Tests of all custom properties which extend org.netbeans.jellytools.properties.Property
 * and reside in package org.netbeans.jellytools.properties.
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class CustomPropertiesTest extends org.netbeans.jellytools.JellyTestCase {

    /** Node with all customizable properties */
    private static TestNode testNode;

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public CustomPropertiesTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new org.netbeans.junit.NbTestSuite();
        suite.addTest(new CustomPropertiesTest("testStringProperty"));
        suite.addTest(new CustomPropertiesTest("testStringArrayProperty"));
        suite.addTest(new CustomPropertiesTest("testPointProperty"));
        suite.addTest(new CustomPropertiesTest("testDimensionProperty"));
        suite.addTest(new CustomPropertiesTest("testRectangleProperty"));
        suite.addTest(new CustomPropertiesTest("testColorProperty"));
        suite.addTest(new CustomPropertiesTest("testFontProperty"));
        suite.addTest(new CustomPropertiesTest("testFileProperty"));
        suite.addTest(new CustomPropertiesTest("testClasspathProperty"));
        suite.addTest(new CustomPropertiesTest("testProcessDescriptorProperty"));
        suite.addTest(new CustomPropertiesTest("testServiceTypeProperty"));
        suite.addTest(new CustomPropertiesTest("testClose"));
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
    
    /** Test org.netbeans.jellytools.properties.StringProperty. */
    public void testStringProperty() {
        StringProperty p = new StringProperty(new PropertySheetOperator(TestNode.NODE_NAME), "String");  // NOI18N
        p.setStringValue("test value");  // NOI18N
        assertEquals("test value", p.getStringValue());  // NOI18N
    }
    
    /** Test org.netbeans.jellytools.properties.StringArrayProperty. */
    public void testStringArrayProperty() {
        StringArrayProperty p=new StringArrayProperty(new PropertySheetOperator(TestNode.NODE_NAME), "String []");  // NOI18N
        String s[]=new String[]{"aa", "bb"};  // NOI18N
        p.setStringArrayValue(s);
        String s2[]=p.getStringArrayValue();
        assertEquals(s[0], s2[0]);
        assertEquals(s[1], s2[1]);
    }
    
    /** Test org.netbeans.jellytools.properties.PointProperty. */
    public void testPointProperty() {
        PointProperty p = new PointProperty(new PropertySheetOperator(TestNode.NODE_NAME), "Point");  // NOI18N
        p.setPointValue("10", "20");  // NOI18N
        String s[] = p.getPointValue();
        assertEquals(s[0], "10");  // NOI18N
        assertEquals(s[1], "20");  // NOI18N
    }
    
    /** Test org.netbeans.jellytools.properties.RectangleProperty. */
    public void testRectangleProperty() {
        RectangleProperty p = new RectangleProperty(new PropertySheetOperator(TestNode.NODE_NAME), "Rectangle");  // NOI18N
        p.setRectangleValue("10", "20", "30", "40");  // NOI18N
        String s[] = p.getRectangleValue();
        assertEquals(s[0], "10");  // NOI18N
        assertEquals(s[1], "20");  // NOI18N
        assertEquals(s[2], "30");  // NOI18N
        assertEquals(s[3], "40");  // NOI18N
    }
    
    /** Test org.netbeans.jellytools.properties.DimensionProperty. */
    public void testDimensionProperty() {
        DimensionProperty p = new DimensionProperty(new PropertySheetOperator(TestNode.NODE_NAME), "Dimension");  // NOI18N
        p.setDimensionValue("10", "20");  // NOI18N
        String s[] = p.getDimensionValue();
        assertEquals(s[0], "10");  // NOI18N
        assertEquals(s[1], "20");  // NOI18N
    }
    
    /** Test org.netbeans.jellytools.properties.ColorProperty. */
    public void testColorProperty() {
        ColorProperty p = new ColorProperty(new PropertySheetOperator(TestNode.NODE_NAME), "Color");  // NOI18N
        p.setRGBValue(10, 20, 30);
        java.awt.Color c = new java.awt.Color(10, 20, 30);
        assertEquals(c, p.getColorValue());
        c = new java.awt.Color(40, 50, 60);
        p.setColorValue(c);
        assertEquals(c, p.getColorValue());
    }
    
    /** Test org.netbeans.jellytools.properties.FontProperty. */
    public void testFontProperty() {
        PropertySheetOperator pso = new PropertySheetOperator(TestNode.NODE_NAME);
        FontProperty p = new FontProperty(pso, "Font");  // NOI18N
        try {
            p.setFontValue("Serif", p.STYLE_BOLDITALIC, "14");  // NOI18N
        } catch (TimeoutExpiredException e) {
            // sometimes it fails on Solaris
            log("jemmy.log", "ERROR: "+e.getMessage());
            JDialog fontDialog = JDialogOperator.findJDialog(p.getName(), false, false);
            if(fontDialog != null) {
                log("jemmy.log", "   Closing Font dialog.");
                new NbDialogOperator(fontDialog).close();
            }
            log("jemmy.log", "   Trying to set font once more");
            p.setFontValue("Serif", p.STYLE_BOLDITALIC, "14");  // NOI18N
        }
        // need to change selection because it gets editable otherwise
        pso.tblSheet().selectCell(0, 0);
        String s[] = p.getFontValue();
        // need to change selection because it gets editable otherwise
        pso.tblSheet().selectCell(0, 0);
        assertTrue(s[0].indexOf("Serif")>=0);  // NOI18N
        assertEquals(p.STYLE_BOLDITALIC, s[1]);
        assertEquals("14", s[2]);  // NOI18N
    }
    
    /** Test org.netbeans.jellytools.properties.FileProperty. */
    public void testFileProperty() throws Exception {
        FileProperty p = new FileProperty(new PropertySheetOperator(TestNode.NODE_NAME), "File");  // NOI18N
        p.setFileValue(getWorkDir());
        assertEquals(getWorkDir(), p.getFileValue());
        log("init file");
        p.setFileValue(new File(getWorkDir(), getName()+".log").getAbsolutePath());
        assertEquals(new File(getWorkDir(), getName()+".log"), p.getFileValue());
    }
    
    /** Test org.netbeans.jellytools.properties.ClasspathProperty. */
    public void testClasspathProperty() throws Exception {
        ClasspathProperty p = new ClasspathProperty(new PropertySheetOperator(TestNode.NODE_NAME), "NbClassPath");  // NOI18N
        String s[] = new String[]{getWorkDir().getAbsolutePath(), getWorkDir().getParentFile().getAbsolutePath()};
        p.setClasspathValue(s);
        String s2[] = p.getClasspathValue();
        assertEquals(s[0].toLowerCase(), s2[0].toLowerCase());
        assertEquals(s[1].toLowerCase(), s2[1].toLowerCase());
    }
    
    /** Test org.netbeans.jellytools.properties.ProcessDescriptorProperty. */
    public void testProcessDescriptorProperty() {
        ProcessDescriptorProperty p = new ProcessDescriptorProperty(new PropertySheetOperator(TestNode.NODE_NAME), "NbProcessDescriptor");  // NOI18N
        p.setProcessDescriptorValue("test process", "test arguments");  // NOI18N
        String s[] = p.getProcessDescriptorValue();
        assertEquals("test process", s[0]);  // NOI18N
        assertEquals("test arguments", s[1]);  // NOI18N
    }
    
    /** Test org.netbeans.jellytools.properties.ServiceTypeProperty. */
    public void testServiceTypeProperty() {
        ServiceTypeProperty p = new ServiceTypeProperty(new PropertySheetOperator(TestNode.NODE_NAME), "Service Type");  // NOI18N
        // "No Indentation"
        String noIndentationLabel = Bundle.getString("org.netbeans.beaninfo.Bundle", "LAB_IndentEngineDefault");  // NOI18N
        p.setServiceTypeValue(noIndentationLabel);
        assertEquals(noIndentationLabel, p.getServiceTypeValue());
    }
    
    /** Close property sheet. */
    public void testClose() {
        new PropertySheetOperator(TestNode.NODE_NAME).close();
    }
}
