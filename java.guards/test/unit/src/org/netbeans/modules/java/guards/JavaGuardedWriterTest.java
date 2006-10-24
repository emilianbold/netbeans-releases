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

package org.netbeans.modules.java.guards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import junit.framework.TestCase;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.guards.SimpleSection;


/**
 *
 * @author Jan Pokorsky
 */
public class JavaGuardedWriterTest extends TestCase {
    
    public JavaGuardedWriterTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    //    /**
    //     * Test of setGuardedSection method, of class org.netbeans.modules.java.guards.JavaGuardedWriter.
    //     */
    //    public void testSetGuardedSection() {
    //        System.out.println("setGuardedSection");
    //
    //        List<GuardedSection> sections = null;
    //        JavaGuardedWriter instance = new JavaGuardedWriter();
    //
    //        instance.setGuardedSection(sections);
    //
    //        // TODO review the generated test code and remove the default call to fail.
    //        fail("The test case is a prototype.");
    //    }
    
    /**
     * Test of translate method, of class org.netbeans.modules.java.guards.JavaGuardedWriter.
     */
    public void testTranslatePlain() {
        System.out.println("write plain");
        
        char[] writeBuff = "\nclass A {\n}\n".toCharArray();
        JavaGuardedWriter instance = new JavaGuardedWriter();
        List<GuardedSection> sections = Collections.<GuardedSection>emptyList();
        
        char[] expResult = writeBuff;
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(String.valueOf(expResult), String.valueOf(result));
    }
    
    public void testTranslateLINE() throws BadLocationException {
        System.out.println("write//" + "GEN-LINE:");
        
        String expStr =   "\nclass A {//" + "GEN-LINE:hu\n}\n";
        String writeStr = "\nclass A {             \n}\n";
        char[] writeBuff = writeStr.toCharArray();
        char[] expResult = expStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        int index = expStr.indexOf('\n', 2);
        sections.add(provider.createSimpleSection("hu", 1, index));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
        
    }
    
    public void testCreateSections() throws Exception {
        Editor editor = new Editor();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(editor);
        Reader r = provider.createGuardedReader(new ByteArrayInputStream(new byte[0]), null);
        while (r.read() > 0);
        r.close();
        GuardedSectionManager manager = GuardedSectionManager.getInstance(editor.getDocument());
        assertNotNull(manager);
        StyledDocument doc = editor.getDocument();
        
        SimpleSection ss = manager.createSimpleSection(doc.createPosition(0), "simpletest");
        System.out.println("simplepos1: " + ss.getStartPosition().getOffset() + ", " + ss.getEndPosition().getOffset());
        ss.setText("  simpletext;");
        System.out.println("simplepos2: " + ss.getStartPosition().getOffset() + ", " + ss.getEndPosition().getOffset());
        
        InteriorSection is = manager.createInteriorSection(doc.createPosition(ss.getEndPosition().getOffset() + 1), "intertest");
        System.out.println("interpos 1: " + is.getStartPosition().getOffset() + ", " + is.getEndPosition().getOffset());
        is.setHeader("public void addListener() {");
        is.setBody("tady pis;");
        is.setFooter("}");
        System.out.println("interpos 2: " + is.getStartPosition().getOffset() + ", " + is.getEndPosition().getOffset());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer w = provider.createGuardedWriter(baos, null);
        w.write(doc.getText(0, doc.getLength()));
        w.close();
        System.out.println("output: '" + baos.toString() + "'");
    }
    
}
