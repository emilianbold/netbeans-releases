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

package java_smart_bracket;

import java.awt.event.KeyEvent;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import lib.JavaEditorTestCase;
import org.netbeans.jemmy.operators.JEditorPaneOperator;

/**
 * Test behavior of smart brackets feature.
 *
 * @author Miloslav Metelka
 */
public class JavaSmartBracketTest extends JavaEditorTestCase {

    public JavaSmartBracketTest(String testMethodName) {
        super(testMethodName);
    }
    
    public void testJavaSmartBracketAfterLBrace(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
        
            EditorOperator editor = getDefaultSampleEditorOperator();

            // 1. move to adequate place 
            editor.setCaretPosition(6, 20);

            // 2. hit Enter 
            JEditorPaneOperator txtOper = editor.txtEditorPane();
            txtOper.pushKey(KeyEvent.VK_ENTER);

            // Compare document content to golden file
            compareReferenceFiles(txtOper.getDocument());

        } finally {
            closeFileWithDiscard();
        }
    }

    /** Testing problem of issue #68992 */
    public void testJavaSmartBracketAfterLBraceAndSpace(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
        
            EditorOperator editor = getDefaultSampleEditorOperator();

            // 1. move to adequate place 
            editor.setCaretPosition(6, 22);

            // 2. hit Enter 
            JEditorPaneOperator txtOper = editor.txtEditorPane();
            txtOper.pushKey(KeyEvent.VK_ENTER);

            // Compare document content to golden file
            compareReferenceFiles(txtOper.getDocument());

        } finally {
            closeFileWithDiscard();
        }
    }
    
    public void testJavaSmartBracketAfterLBraceAndComment(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();

            // 1. move to adequate place
            editor.setCaretPosition(6, 36);

            // 2. hit Enter
            JEditorPaneOperator txtOper = editor.txtEditorPane();
            txtOper.pushKey(KeyEvent.VK_ENTER);

            // Compare document content to golden file
            compareReferenceFiles(txtOper.getDocument());

        } finally {
            closeFileWithDiscard();
        }
    }

}
