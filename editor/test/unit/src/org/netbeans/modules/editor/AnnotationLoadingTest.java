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

package org.netbeans.modules.editor;

import java.net.URL;
import junit.framework.*;
import java.awt.Color;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationTypes;
import org.netbeans.modules.editor.options.AnnotationTypeProcessor;
import org.openide.filesystems.FileSystem.Status;
import org.openide.loaders.DataObject;
import org.openide.loaders.Environment;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationLoadingTest extends TestCase {
    
    public AnnotationLoadingTest(String testName) {
        super(testName);
    }

    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    protected void setUp() throws Exception {
        EditorTestLookup.setLookup(
                new URL[] {
            EditorTestConstants.EDITOR_LAYER_URL,
                    getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/resources/annotations-test-layer.xml")
        },
                new Object[] {},
                getClass().getClassLoader()
                );
                
                AnnotationTypes.getTypes().registerLoader(new AnnotationsTest.AnnotationsLoader());
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AnnotationLoadingTest.class);
        
        return suite;
    }

    public void testAnnotationStatusAndColorRead() {
//            <file name="test-annotation-1">
//                <attr name="AnnotationStatus" stringvalue="error" />
//            </file>
        assertEquals(AnnotationType.Severity.STATUS_ERROR, AnnotationTypes.getTypes().getType("test-annotation-1").getSeverity());
        assertEquals(false, AnnotationTypes.getTypes().getType("test-annotation-1").isUseCustomSidebarColor());
        assertEquals(0, AnnotationTypes.getTypes().getType("test-annotation-1").getPriority());
//            <file name="test-annotation-2">
//                <attr name="AnnotationStatus" stringvalue="warning" />
//                <attr name="priority" stringvalue="100" />
//            </file>
        assertEquals(AnnotationType.Severity.STATUS_WARNING, AnnotationTypes.getTypes().getType("test-annotation-2").getSeverity());
        assertEquals(false, AnnotationTypes.getTypes().getType("test-annotation-2").isUseCustomSidebarColor());
        assertEquals(100, AnnotationTypes.getTypes().getType("test-annotation-2").getPriority());
//            <file name="test-annotation-3">
//                <attr name="AnnotationStatus" stringvalue="ok" />
//            </file>
        assertEquals(AnnotationType.Severity.STATUS_OK, AnnotationTypes.getTypes().getType("test-annotation-3").getSeverity());
        assertEquals(false, AnnotationTypes.getTypes().getType("test-annotation-3").isUseCustomSidebarColor());
//            <file name="test-annotation-4">
//                <attr name="AnnotationStatus" stringvalue="error" />
//                <attr name="color" stringvalue="0x000000" />
//            </file>
        assertEquals(AnnotationType.Severity.STATUS_ERROR, AnnotationTypes.getTypes().getType("test-annotation-4").getSeverity());
        assertEquals(true, AnnotationTypes.getTypes().getType("test-annotation-4").isUseCustomSidebarColor());
        assertEquals(new Color(0, 0, 0), AnnotationTypes.getTypes().getType("test-annotation-4").getCustomSidebarColor());
//            <file name="test-annotation-5">
//                <attr name="AnnotationStatus" stringvalue="warning" />
//                <attr name="color" stringvalue="0x000000" />
//            </file>
        assertEquals(AnnotationType.Severity.STATUS_WARNING, AnnotationTypes.getTypes().getType("test-annotation-5").getSeverity());
        assertEquals(true, AnnotationTypes.getTypes().getType("test-annotation-5").isUseCustomSidebarColor());
        assertEquals(new Color(0, 0, 0), AnnotationTypes.getTypes().getType("test-annotation-5").getCustomSidebarColor());
//            <file name="test-annotation-6">
//                <attr name="AnnotationStatus" stringvalue="ok" />
//                <attr name="color" stringvalue="0x000000" />
//            </file>
        assertEquals(AnnotationType.Severity.STATUS_OK, AnnotationTypes.getTypes().getType("test-annotation-6").getSeverity());
        assertEquals(true, AnnotationTypes.getTypes().getType("test-annotation-6").isUseCustomSidebarColor());
        assertEquals(new Color(0, 0, 0), AnnotationTypes.getTypes().getType("test-annotation-6").getCustomSidebarColor());
//            <file name="test-annotation-7">
//                <attr name="AnnotationStatus" stringvalue="ok" />
//                <attr name="color" stringvalue="unparseable" />
//            </file>
        //not possible, throws exception
//        assertEquals(AnnotationType.Severity.STATUS_OK, AnnotationTypes.getTypes().getType("test-annotation-7").getSeverity());
//        assertEquals(false, AnnotationTypes.getTypes().getType("test-annotation-7").isUseCustomSidebarColor());
//            <file name="test-annotation-8">
//                <attr name="AnnotationStatus" stringvalue="unknown" />
//            </file>
        assertEquals(AnnotationType.Severity.STATUS_NONE, AnnotationTypes.getTypes().getType("test-annotation-8").getSeverity());
        assertEquals(false, AnnotationTypes.getTypes().getType("test-annotation-8").isUseCustomSidebarColor());
//            <file name="test-annotation-browseable-1">
//                <attr name="AnnotationStatus" stringvalue="unknown" />
//                <attr name="browseable" boolvalue="true" />
//            </file>
        assertTrue(AnnotationTypes.getTypes().getType("test-annotation-browseable-1").isBrowseable());
//            <file name="test-annotation-browseable-2">
//                <attr name="AnnotationStatus" stringvalue="unknown" />
//                <attr name="browseable" boolvalue="false" />
//            </file>
        assertFalse(AnnotationTypes.getTypes().getType("test-annotation-browseable-2").isBrowseable());
    }
    
//    public void testEquals() {
//        AnnotationStatus testAnnotationStatus = StatusForAnnotationTypeQuery.getDefault().getStatusForAnnotationType("test-annotation-5");
//        
//        assertFalse(testAnnotationStatus.equals(null));
//        assertFalse(testAnnotationStatus.equals("test"));
//        assertFalse(testAnnotationStatus.equals(new AnnotationStatus(Status.STATUS_OK)));
//        assertFalse(testAnnotationStatus.equals(new AnnotationStatus(Status.STATUS_WARNING)));
//        assertFalse(testAnnotationStatus.equals(new AnnotationStatus(Status.STATUS_OK, new Color(0, 0, 0))));
//        
//        assertTrue(testAnnotationStatus.equals(new AnnotationStatus(Status.STATUS_WARNING, new Color(0, 0, 0))));
//    }
    
    public void testNotFoundAnnotation() {
        assertNull(AnnotationTypes.getTypes().getType("non-existent"));
    }
    
}
