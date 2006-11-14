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

import java.util.List;
import junit.framework.TestCase;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.guards.SimpleSection;

/**
 *
 * @author Jan Pokorsky
 */
public class JavaGuardedReaderTest extends TestCase {

    private JavaGuardedSectionsProvider provider;

    private JavaGuardedReader instance;

    private Editor editor;
    
    public JavaGuardedReaderTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        editor = new Editor();
        provider = new JavaGuardedSectionsProvider(editor);
        
        instance = new JavaGuardedReader(provider);
    }

    protected void tearDown() throws Exception {
        provider = null;
        instance = null;
    }

    /**
     * Test of translateToCharBuff method, of class org.netbeans.modules.java.guards.JavaGuardedReader.
     */
    public void testTranslatePlain() {
        System.out.println("read plain");
        
        String expStr =   "\nclass A {\n}\n";
        char[] readBuff = expStr.toCharArray();
        
        char[] result = instance.translateToCharBuff(readBuff);
        List<GuardedSection> sections = instance.getGuardedSections();
        assertEquals(expStr, String.valueOf(result));
        assertTrue("sections not empty", sections.isEmpty());
    }
    
    public void testTranslateLINE() {
        System.out.println("read //" + "GEN-LINE:");
        
        String readStr = "\nclass A {//" + "GEN-LINE:hu\n}\n";
        editor.setStringContent(readStr);
        String expStr =  "\nclass A {  " + "           \n}\n";
        char[] readBuff = readStr.toCharArray();
        
        char[] result = instance.translateToCharBuff(readBuff);
        List<GuardedSection> sections = instance.getGuardedSections();
        
        assertEquals(expStr, String.valueOf(result));
        assertEquals("sections", 1, sections.size());
        
        GuardedSection expSection = sections.get(0);
        assertEquals(SimpleSection.class, expSection.getClass());
        assertEquals("section valid", true, expSection.isValid());
        assertEquals("section name", "hu", expSection.getName());
        assertEquals("begin", 1, expSection.getStartPosition().getOffset());
        assertEquals("end", expStr.indexOf("}") - 1, expSection.getEndPosition().getOffset());
    }
    
    public void testTranslateBEGIN_END() {
        System.out.println("read //" + "GEN-BEGIN_END:");
        
        String readStr = "\nclass A {//" + "GEN-BEGIN:hu\n\n}//" + "GEN-END:hu\n";
        editor.setStringContent(readStr);
        String expStr =  "\nclass A {  " + "            \n\n}  " + "          \n";
        char[] readBuff = readStr.toCharArray();
        
        char[] result = instance.translateToCharBuff(readBuff);
        List<GuardedSection> sections = instance.getGuardedSections();
        
        assertEquals(expStr, String.valueOf(result));
        assertEquals("sections", 1, sections.size());
        
        GuardedSection expSection = sections.get(0);
        assertEquals(SimpleSection.class, expSection.getClass());
        assertEquals("section valid", true, expSection.isValid());
        assertEquals("section name", "hu", expSection.getName());
        assertEquals("begin", 1, expSection.getStartPosition().getOffset());
        assertEquals("end", expStr.length() - 1, expSection.getEndPosition().getOffset());
    }
    
    public void testTranslateFIRST_LAST() {
        System.out.println("read //" + "GEN-FIRST_LAST:");
        
        String readStr = "\nclass A {//" + "GEN-FIRST:hu\n  statement;\n}//" + "GEN-LAST:hu\n";
        editor.setStringContent(readStr);
        String expStr =  "\nclass A {  " + "            \n  statement;\n}  " + "           \n";
        char[] readBuff = readStr.toCharArray();
        
        char[] result = instance.translateToCharBuff(readBuff);
        List<GuardedSection> sections = instance.getGuardedSections();
        
        assertEquals(expStr, String.valueOf(result));
        assertEquals("sections", 1, sections.size());
        
        GuardedSection expSection = sections.get(0);
        assertEquals(InteriorSection.class, expSection.getClass());
        assertEquals("section valid", true, expSection.isValid());
        assertEquals("section name", "hu", expSection.getName());
        assertEquals("begin", 1, expSection.getStartPosition().getOffset());
        assertEquals("end", expStr.length() - 1, expSection.getEndPosition().getOffset());
        InteriorSection iSection = (InteriorSection) expSection;
        assertEquals("body.begin", expStr.indexOf("  statement;"), iSection.getBodyStartPosition().getOffset());
        assertEquals("body.end", expStr.indexOf("\n}"), iSection.getBodyEndPosition().getOffset());
    }
    
    public void testTranslateFIRST_HEADEREND_LAST() {
        System.out.println("read //" + "GEN-FIRST_HEADEREND_LAST:");
        
        String readStr = "\nclass A //" + "GEN-FIRST:hu\n{//" + "GEN-HEADEREND:hu\n  statement;\n}//" + "GEN-LAST:hu\n";
        editor.setStringContent(readStr);
        String expStr =  "\nclass A   " + "            \n{  " + "                \n  statement;\n}  " + "           \n";
        char[] readBuff = readStr.toCharArray();
        
        char[] result = instance.translateToCharBuff(readBuff);
        List<GuardedSection> sections = instance.getGuardedSections();
        
        assertEquals(expStr, String.valueOf(result));
        assertEquals("sections", 1, sections.size());
        
        GuardedSection expSection = sections.get(0);
        assertEquals(InteriorSection.class, expSection.getClass());
        assertEquals("section valid", true, expSection.isValid());
        assertEquals("section name", "hu", expSection.getName());
        assertEquals("begin", 1, expSection.getStartPosition().getOffset());
        assertEquals("end", expStr.length() - 1, expSection.getEndPosition().getOffset());
        InteriorSection iSection = (InteriorSection) expSection;
        assertEquals("body.begin", expStr.indexOf("  statement;"), iSection.getBodyStartPosition().getOffset());
        assertEquals("body.end", expStr.indexOf("\n}"), iSection.getBodyEndPosition().getOffset());
    }
    
}
