/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.editor.makefile;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.FormatWriter;

/**
 *
 * @author gordonp
 */
public class MakefileFormatter extends ExtFormatter {
    
    /** Creates a new instance of MakefileFormatter */
    public MakefileFormatter(Class kitClass) {
        super(kitClass);
    }
    
    protected boolean acceptSyntax(Syntax syntax) {
        return (syntax instanceof MakefileSyntax);
    }

    public boolean expandTabs() { 
        return false;
    }

    /**
     * Force indentation to always be a tab. We shouldn't need this method except
     * that IZ 88167 ignores our indent rules and indents 4 spaces. This forces
     * indent to always be a tab. We're really overriding Formatter.getIndentString().
     */
    public String getIndentString(BaseDocument doc, int indent) {
        JTextComponent tc = Utilities.getFocusedComponent();
        int dot = tc.getCaret().getDot();
        try {
            int start = Utilities.getRowStart(doc, dot);
            if (dot == start) {
                return "\t"; // NOI18N
            }
        } catch (BadLocationException ex) {
        }
        return super.getIndentString(doc, indent);
    }
    
    protected void initFormatLayers() {
        addFormatLayer(new MakefileLayer());
    }
    
    public FormatSupport createFormatSupport(FormatWriter fw) {
        return new MakefileFormatSupport(fw);
    }
    
    public class MakefileLayer extends AbstractFormatLayer {
        
        public MakefileLayer() {
            super("Makefile-layer"); // NOI18N
        }
    
        public FormatSupport createFormatSupport(FormatWriter fw) {
            return new MakefileFormatSupport(fw);
        }
        
        public void format(FormatWriter fw) {
            MakefileFormatSupport mfs = (MakefileFormatSupport) createFormatSupport(fw);
            FormatTokenPosition pos = mfs.getFormatStartPosition();
            TokenItem ti = mfs.getLastToken();
            
            if (mfs.isIndentOnly()) {
                mfs.indentLine(pos);
            }
        }
        
        protected boolean expandTabs() {
            return false;
        }
    }
}
