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
package org.netbeans.modules.web.core.syntax.indent;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.web.core.syntax.JSPKit;
import org.netbeans.modules.web.core.syntax.formatting.JspFormatter;
import org.netbeans.spi.editor.indent.Context;
import org.netbeans.spi.editor.indent.ExtraLock;
import org.netbeans.spi.editor.indent.IndentTask;

/**
 * Implementation of IndentTask for text/x-jsp mimetype.
 *
 * @author Marek Fukala
 */
                                        

public class JspIndentTask implements IndentTask {

    private Context context;
    private JspFormatter formatter = null;
    
    JspIndentTask(Context context) {
        this.context = context;
    }

    public void reindent() throws BadLocationException {
        getFormatter().reformat((BaseDocument)context.document(), context.startOffset(), context.endOffset(), false);
    }
    
    public ExtraLock indentLock() {
        return null;
    }

    private synchronized JspFormatter getFormatter() {
        if(formatter == null) {
            formatter = new JspFormatter(JSPKit.class);
        }
        return formatter;
    }
        
}
