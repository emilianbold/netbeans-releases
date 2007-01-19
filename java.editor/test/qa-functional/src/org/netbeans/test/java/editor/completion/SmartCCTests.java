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

package org.netbeans.test.java.editor.completion;

import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 *
 * @author jp159440
 */
public class SmartCCTests extends CompletionTestPerformer{
    
   
    public SmartCCTests(String name) {
        super(name);
    }
    
     
    public void testsmartassign() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "Double x = ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Assign.java", 11,CompletionProvider.COMPLETION_SMART_QUERY_TYPE);        
    }
    
    public void testsmartassign2() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "Number x = ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Assign.java", 11,CompletionProvider.COMPLETION_SMART_QUERY_TYPE);        
    }
    
    public void testsmartassign3() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "Number x = new ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Assign.java", 11,CompletionProvider.COMPLETION_SMART_QUERY_TYPE);        
    }
    
    public void testsmartassign4() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "List x = new ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Assign.java", 11,CompletionProvider.COMPLETION_SMART_QUERY_TYPE);        
    }
    
    public void testsmartassign5() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "String x = ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Assign.java", 11,CompletionProvider.COMPLETION_SMART_QUERY_TYPE);        
    }
    
    public void testsmartExtends() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "class A extends ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 32,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartImplements() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "class B implements ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 32,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartThrows() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "public void method() throws ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 32,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartImport() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "import ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 21,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartImportStatic() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "import static java.awt.Color.", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/Types.java", 21,CompletionProvider.COMPLETION_QUERY_TYPE);        
    }
    
    public void testsmartSuperParameter() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "super(", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 31,CompletionProvider.COMPLETION_SMART_QUERY_TYPE);        
    }
    
    public void testsmartInnerClassAsParameter() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "method(", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 41,CompletionProvider.COMPLETION_SMART_QUERY_TYPE);        
    }
    
    public void testsmartInnerClassAsParameter2() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "method( new ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 41,CompletionProvider.COMPLETION_SMART_QUERY_TYPE);        
    }
    
    public void testsmartReturn() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "return ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 42,CompletionProvider.COMPLETION_SMART_QUERY_TYPE);                
    }
    
    public void testsmartReturn2() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "return new ", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 42,CompletionProvider.COMPLETION_SMART_QUERY_TYPE);        
    }
    
    public void testsmartCatch() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "catch (", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/smartcompletion/SmartCC.java", 51,CompletionProvider.COMPLETION_SMART_QUERY_TYPE);        
    }

    
    
}
