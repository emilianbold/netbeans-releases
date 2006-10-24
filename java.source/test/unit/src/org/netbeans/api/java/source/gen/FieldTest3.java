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
package org.netbeans.api.java.source.gen;

import java.io.IOException;
import java.util.List;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileStateInvalidException;

/**
 * Tests more transaction on file with fields. Tests fields generating and
 * also update.
 *
 * @author  Pavel Flaska
 */
public class FieldTest3 extends GeneratorTest {

    /** Creates a new instance of FieldTest3 */
    public FieldTest3(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(FieldTest3.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "FieldTest3.java");
    }

    /**
     * Tests inital value for fields in field group.
     */
    public void testGroupInitValues() throws IOException {
        System.err.println("testGroupInitValues");
/*            FieldGroup group = (FieldGroup) clazz.getContents().get(2);
            List fields = group.getFields();
            ((Field) fields.get(0)).setInitialValueText("\"prvni\"");
            ((Field) fields.get(2)).setInitialValueText("\"treti\"");*/
        assertFiles("testGroupInitValues_FieldTest3.pass");
    }
    
    /**
     * Tests group separation
     */
    public void testGroupSeparation() throws IOException {
        System.err.println("testGroupSeparation");
  /*          Field second = (Field) clazz.getFeatures().remove(3);
            Field third = (Field) clazz.getFeatures().remove(3);
            clazz.getFeatures().add(second);
            clazz.getFeatures().add(third);*/
        assertFiles("testGroupSeparation_FieldTest3.pass");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/FieldTest3/";
    }
}
