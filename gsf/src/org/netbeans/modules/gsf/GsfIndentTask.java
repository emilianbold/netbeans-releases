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
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.gsf.Formatter;
import org.netbeans.api.gsf.FormattingPreferences;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.editor.SimpleIndentEngine;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.spi.editor.indent.Context;
import org.netbeans.spi.editor.indent.ExtraLock;
import org.netbeans.spi.editor.indent.IndentTask;
import org.openide.text.IndentEngine;
import org.openide.util.Lookup;

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
            
            if (formatter == null) {
                return null;
            }

            Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimeType));
            BaseOptions options = lookup.lookup(BaseOptions.class);
            int indentSize = formatter.indentSize();
            if (options != null) {
                IndentEngine indentEngine = options.getIndentEngine ();
                if (indentEngine instanceof SimpleIndentEngine) {
                    indentSize = ((SimpleIndentEngine)indentEngine).getSpacesPerTab();
                } else {
                    Object o = options.getSettingValue(SettingsNames.SPACES_PER_TAB);
                    if (o instanceof Number) {
                        indentSize = ((Number)o).intValue();
                    }
                }
            }
            
            int hangingIndentSize = formatter.hangingIndentSize();
            preferences = new GsfFormattingPreferences(indentSize, hangingIndentSize);
        }
        return formatter;
    }
        
}
