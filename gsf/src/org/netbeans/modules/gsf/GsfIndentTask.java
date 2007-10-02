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
package org.netbeans.modules.gsf;

import javax.swing.text.BadLocationException;
import org.netbeans.api.gsf.Formatter;
import org.netbeans.api.gsf.FormattingPreferences;
import org.netbeans.spi.editor.indent.Context;
import org.netbeans.spi.editor.indent.ExtraLock;
import org.netbeans.spi.editor.indent.IndentTask;

public class GsfIndentTask implements IndentTask {

    private Context context;
    private Formatter formatter;
    private FormattingPreferences preferences;
    
    GsfIndentTask(Context context) {
        this.context = context;
    }

    public void reindent() throws BadLocationException {
        Formatter f = getFormatter();
        
        if (f != null) {
            f.reindent(context.document(), context.startOffset(), context.endOffset(), null, preferences);
        }
    }
    
    public ExtraLock indentLock() {
        return null;
    }

    private synchronized Formatter getFormatter() {
        if(formatter == null) {
            String mimeType = context.mimePath();
            Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
            formatter = language.getFormatter();
        }
        return formatter;
    }
}
