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

package org.netbeans.modules.lexer.editorbridge;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.plaf.TextUI;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.plain.PlainSyntax;
import org.netbeans.modules.editor.NbEditorKit;
import org.openide.filesystems.FileObject;

public class LexerEditorKit extends NbEditorKit {

    public static final String tokenListDebugAction = "token-list-debug";
    
    public static final String UNKNOWN_MIME_TYPE = "text/x-unknown";
    
    public static LexerEditorKit create(FileObject fo) {
        String mimeType = UNKNOWN_MIME_TYPE;
        // Get mime-type from fo's name
        fo = fo.getParent(); // get parent folder
        String path = fo.getPath();
        // Strip initial "Editors/"
        if (path.startsWith("Editors/")) {
            mimeType = path.substring("Editors/".length());
        }
        return new LexerEditorKit(mimeType);
    }
    
    private String mimeType;
    
    public LexerEditorKit(String mimeType) {
        this.mimeType = mimeType;
    }
    
    /**
     * @deprecated LexerEditorKit should not be used in longterm; it should be eliminated during 6.0 development
     */
    public LexerEditorKit() {
        // Compatibility constructor - should not 
    }
    
    public String getContentType() {
        return mimeType;
    }

    public void install(JEditorPane pane) {
        super.install(pane);

        TextUI ui = pane.getUI();
        if (ui instanceof BaseTextUI) {
            ((BaseTextUI)ui).getEditorUI().addLayer(new LexerLayer(pane), LexerLayer.VISIBILITY);
        }
    }

    public Syntax createSyntax(Document doc) {
        return new PlainSyntax();
    }

    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new ExtSyntaxSupport(doc);
    }

    protected Action[] createActions() {
        Action[] calcActions = new Action[] {
            new TokenListDebugAction(),
        };
        return TextAction.augmentList(super.createActions(), calcActions);
    }

    static class TokenListDebugAction extends BaseAction {
        
        public TokenListDebugAction() {
            super(tokenListDebugAction, 0);
        }

        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            TokenHierarchy hi = TokenHierarchy.get(target.getDocument());
            if (hi != null) {
                /*DEBUG*/System.err.println("Token list:\n" + hi.tokenSequence());
            } else {
                /*DEBUG*/System.err.println("Token hierarchy is null.");
            }
        }
    }

    /**
     * Possibly change the original coloring name.
     */
    protected String updateColoringName(String coloringName) {
        return coloringName;
    }

}

