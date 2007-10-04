/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.core.syntax.indent;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.web.core.syntax.JSPKit;
import org.netbeans.modules.web.core.syntax.formatting.JSPLexerFormatter;
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
    private JSPLexerFormatter formatter = null;
    
    JspIndentTask(Context context) {
        this.context = context;
    }

    public void reindent() throws BadLocationException {
        getFormatter().reformat((BaseDocument)context.document(), context.startOffset(), context.endOffset(), false);
    }
    
    public ExtraLock indentLock() {
        return null;
    }

    private synchronized JSPLexerFormatter getFormatter() {
        if(formatter == null) {
            formatter = new JSPLexerFormatter(JSPKit.class);
        }
        return formatter;
    }
        
}
