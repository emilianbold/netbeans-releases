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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.api.Reformat;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class FormattingUtils {
    private static final Logger logger = Logger.getLogger(FormattingUtils.class.getName());
    
    public static void reformat(BaseDocument doc, int startPos, int endPos){
        Reformat reformat = Reformat.get(doc);
        reformat.lock();
      
        try {
            doc.atomicLock();
            try {
                reformat.reformat(startPos, endPos);
            } catch (BadLocationException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            } finally {
                doc.atomicUnlock();
            }
        } finally {
            reformat.unlock();
        }
    }
    
    public static void indentNewLine(BaseDocument doc, int caretPos){
        Indent indent = Indent.get(doc);
        indent.lock();

        try {
            doc.atomicLock();
            try {
                indent.reindent(caretPos);
            } catch (BadLocationException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            } finally {
                doc.atomicUnlock();
            }
        } finally {
            indent.unlock();
        }
    }
}