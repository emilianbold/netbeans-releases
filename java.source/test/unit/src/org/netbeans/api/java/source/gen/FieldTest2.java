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

import com.sun.source.tree.VariableTree;
import java.io.IOException;
import junit.textui.TestRunner;
import org.netbeans.jackpot.transform.Transformer;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author  Pavel Flaska
 */
public class FieldTest2 extends GeneratorTest {

    /** Creates a new instance of FieldTest2 */
    public FieldTest2(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(FieldTest2.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "FieldTest2.java");
    }

    /**
     * Test move field from field group to the single field declaration.
     */
    public void testMoveField() throws IOException {
        System.err.println("testMoveField");
        process(
            new Transformer<Void, Object>() {
                public Void visitVariable(VariableTree node, Object p) {
                    if ("nerudova".contentEquals(node.getName())) {
                        System.err.println(node);
                    }
                    /*
                Field nerudova = null;
                for (Iterator fIt = clazz.getContents().iterator(); fIt.hasNext(); ) {
                    Object feature = fIt.next();
                    if (feature instanceof FieldGroup) {
                        FieldGroup group = (FieldGroup) feature;
                        nerudova = (Field) group.getFields().remove(2);
                        nerudova.setTypeName((TypeReference) ((TypeReferenceImpl) group.getTypeName()).duplicate());
                        nerudova.setModifiers(Modifier.PRIVATE);
                    }
                }
                clazz.getFeatures().add(nerudova);*/
                    return null;
                }
            }
        );
        assertFiles("FieldTest2.pass");
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
        return "org/netbeans/jmi/javamodel/codegen/FieldTest2/";
    }
}
