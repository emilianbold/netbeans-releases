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
package org.netbeans.modules.java.hints.errors;

import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;

/**
 *
 * @author Jan Lahoda
 */
public class ImportClassTest extends HintsTestBase {
    
    /** Creates a new instance of ImportClassEnablerTest */
    public ImportClassTest(String name) {
        super(name);
    }
    
    //    public void testImportHint() throws Exception {
    //        performTest("ImportTest", "java.util.List", 22, 13);
    //    }
    //
    //    public void testImportHint2() throws Exception {
    //        performTest("ImportTest2", "java.util.List", 18, 13);
    //    }
    
    public void testImportHint3() throws Exception {
        performTest("ImportTest3", "java.util.ArrayList", 9, 13);
    }
    
    public void testImportHint4() throws Exception {
        performTest("ImportTest4", "java.util.Collections", 7, 13);
    }
    
    public void testImportHint5() throws Exception {
        performTest("ImportTest5", "java.util.Map", 7, 13);
    }
    
    public void testImportHint6() throws Exception {
        performTest("ImportTest6", "java.util.Collections", 7, 13);
    }
    
    public void testImportHintDoNotPropose() throws Exception {
        performTestDoNotPerform("ImportHintDoNotPropose", 10, 24);
        performTestDoNotPerform("ImportHintDoNotPropose", 11, 24);
    }
    
    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/ImportClassEnablerTest/";
    }
    
}
