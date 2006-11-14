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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import junit.framework.TestCase;
import org.netbeans.api.editor.guards.GuardedSection;

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
        System.out.println("write //" + "GEN-LINE:");
        
        String writeStr = "\nclass A {" +              "\n}\n";
        String expStr =   "\nclass A {//" + "GEN-LINE:hu\n}\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createSimpleSection("hu", 1, writeStr.indexOf("\n}")));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }
    
    public void testTranslateLINEWithSpaces() throws BadLocationException {
        System.out.println("write with spaces //" + "GEN-LINE:");
        
        String writeStr = "\nclass A {  " + "           \n}\n";
        String expStr =   "\nclass A {//" + "GEN-LINE:hu\n}\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createSimpleSection("hu", 1, writeStr.indexOf("\n}")));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }
    
    public void testTranslateBEGIN_END() throws BadLocationException {
        System.out.println("write //" + "GEN-BEGIN_END:");
        
        String writeStr = "\nclass A {" +               "\n\n}" +             "\n";
        String expStr =   "\nclass A {//" + "GEN-BEGIN:hu\n\n}//" + "GEN-END:hu\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createSimpleSection("hu", 1, writeStr.length() - 1));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }
    
    public void testTranslateBEGIN_ENDWithSpaces() throws BadLocationException {
        System.out.println("write with spaces //" + "GEN-BEGIN_END:");
        
        String writeStr = "\nclass A {  " + "            \n\n}  " + "          \n";
        String expStr =   "\nclass A {//" + "GEN-BEGIN:hu\n\n}//" + "GEN-END:hu\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createSimpleSection("hu", 1, writeStr.length() - 1));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }
    
    public void testTranslateFIRST_LAST() throws BadLocationException {
        System.out.println("write //" + "GEN-FIRST_LAST:");
        
        String writeStr = "\nclass A {  " +             "\n  statement;\n}" +              "\n";
        String expStr =   "\nclass A {//" + "GEN-FIRST:hu\n  statement;\n}//" + "GEN-LAST:hu\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createInteriorSection("hu",
                1, writeStr.indexOf("\n  statement;"),
                writeStr.indexOf("}"), writeStr.length() - 1));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }
    
    public void testTranslateFIRST_LASTWithSpaces() throws BadLocationException {
        System.out.println("write with spaces //" + "GEN-FIRST_LAST:");
        
        String writeStr = "\nclass A {  " + "            \n  statement;\n}  " + "           \n";
        String expStr =   "\nclass A {//" + "GEN-FIRST:hu\n  statement;\n}//" + "GEN-LAST:hu\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createInteriorSection("hu",
                1, writeStr.indexOf("\n  statement;"),
                writeStr.indexOf("}"), writeStr.length() - 1));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }
    
    public void testTranslateFIRST_HEADEREND_LAST() throws BadLocationException {
        System.out.println("write //" + "GEN-FIRST_HEADEREND_LAST:");
        
        String writeStr = "\nclass A  " + "            \n{  " + "                \n  statement;\n}  " + "           \n";
        String expStr =   "\nclass A//" + "GEN-FIRST:hu\n{//" + "GEN-HEADEREND:hu\n  statement;\n}//" + "GEN-LAST:hu\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createInteriorSection("hu",
                1, writeStr.indexOf("\n  statement;"),
                writeStr.indexOf("}"), writeStr.length() - 1));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }
    
    public void testTranslateFIRST_HEADEREND_LASTWithSpaces() throws BadLocationException {
        System.out.println("write with spaces //" + "GEN-FIRST_HEADEREND_LAST:");
        
        String writeStr = "\nclass A" +               "\n{" +                   "\n  statement;\n}" +              "\n";
        String expStr =   "\nclass A//" + "GEN-FIRST:hu\n{//" + "GEN-HEADEREND:hu\n  statement;\n}//" + "GEN-LAST:hu\n";
        char[] writeBuff = writeStr.toCharArray();
        
        JavaGuardedWriter instance = new JavaGuardedWriter();
        JavaGuardedSectionsProvider provider = new JavaGuardedSectionsProvider(new Editor());
        List<GuardedSection> sections = new ArrayList<GuardedSection>();
        sections.add(provider.createInteriorSection("hu",
                1, writeStr.indexOf("\n  statement;"),
                writeStr.indexOf("}"), writeStr.length() - 1));
        
        instance.setGuardedSection(sections);
        char[] result = instance.translate(writeBuff);
        assertEquals(expStr, String.valueOf(result));
    }
    
}
