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
package org.netbeans.modules.gsf;

import javax.swing.text.BadLocationException;
import org.netbeans.api.gsf.Formatter;
import org.netbeans.api.gsf.FormattingPreferences;
import org.netbeans.spi.editor.indent.Context;
import org.netbeans.spi.editor.indent.ExtraLock;
import org.netbeans.spi.editor.indent.ReformatTask;

public class GsfReformatTask implements ReformatTask {

    private Context context;
    private Formatter formatter;
    private FormattingPreferences preferences;
    
    GsfReformatTask(Context context) {
        this.context = context;
    }

    private synchronized Formatter getFormatter() {
        if(formatter == null) {
            String mimeType = context.mimePath();
            Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
            formatter = language.getFormatter();
        }
        return formatter;
    }

    public void reformat() throws BadLocationException {
        Formatter f = getFormatter();
        
        if (f != null) {
            f.reformat(context.document(), context.startOffset(), context.endOffset(), null, preferences);
        }
    }

    public ExtraLock reformatLock() {
        return null;
    }
        
}
