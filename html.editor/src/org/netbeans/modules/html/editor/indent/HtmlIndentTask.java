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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.html.editor.indent;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.HTMLFormatter;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.spi.editor.indent.Context;
import org.netbeans.spi.editor.indent.ExtraLock;
import org.netbeans.spi.editor.indent.IndentTask;

/**
 * Implementation of IndentTask for text/html mimetype.
 *
 * @author Marek Fukala
 */
                                        

public class HtmlIndentTask implements IndentTask {

    private Context context;
    //private HTMLLexerFormatter formatter = null;
    private static HTMLFormatter formatter = null;
    
    HtmlIndentTask(Context context) {
        this.context = context;
    }

    public void reindent() throws BadLocationException {
        getFormatter().reformat((BaseDocument)context.document(), context.startOffset(), context.endOffset(), true);
    }
    
    public ExtraLock indentLock() {
        return null;
    }

    //private synchronized HTMLLexerFormatter getFormatter() {
    private synchronized HTMLFormatter getFormatter() {
        if(formatter == null) {
            //formatter = new HTMLLexerFormatter(HTMLKit.class);
            formatter = new HTMLFormatter(HTMLKit.class);
        }
        return formatter;
    }
        
}
