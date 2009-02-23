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
package org.netbeans.modules.css.editor.indent;

import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.editor.CSSIndenter;
import org.netbeans.modules.editor.indent.spi.IndentTask.FormattingContext;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;

public class CssIndentTask implements IndentTask.ContextAwareIndentTask {

    private Context context;
    private CSSIndenter indenter;
    
    CssIndentTask(Context context) {
        this.context = context;
        indenter = new CSSIndenter(context);
    }

    public void reindent() throws BadLocationException {
        //new JSPLexerFormatter().process(context);
        indenter.reindent();
    }
    
    public ExtraLock indentLock() {
        return null;
    }

    public void beforeReindent(List<FormattingContext> contexts) {
        indenter.beforeReindent(contexts);
    }

    public FormattingContext createFormattingContext() {
        return indenter.createFormattingContext();
    }
}
