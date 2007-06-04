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

package org.netbeans.modules.css.editor;

import org.netbeans.modules.css.actions.CssRuleCreateAction;
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import org.netbeans.modules.css.actions.CheckStyleAction;
import org.netbeans.modules.css.actions.CopyStyleAction;
import org.netbeans.modules.languages.dataobject.LanguagesEditorKit;

/**
 * Editor kit implementation for a document of content type "text/css"
 * 
 * @author Winston Prakash
 * @author Marek Fukala
 * 
 * @version 1.0
 */
public class CssEditorKit  extends LanguagesEditorKit {

    public static final String CSS_MIME_TYPE = "text/x-css"; // NOI18N

    /** Creates a new instance of CssEditorKit */
    public CssEditorKit() {
        super(CSS_MIME_TYPE);
    }

    public Document createDefaultDocument() {
        Document doc = super.createDefaultDocument();
        initDocument(doc); //schliemann hack
        return doc;
    }

    /**
     * Get the content type supported by this editor
     */
    public String getContentType() {
        return CSS_MIME_TYPE;
    }

    protected Action[] createActions() {
        Action[] cssEditorActions = new Action[] {
            new CssRuleCreateAction(),
            new CheckStyleAction(),
            new CopyStyleAction.HTML(),
            new CopyStyleAction.XML()
        };
        return TextAction.augmentList(super.createActions(), cssEditorActions);
    }
    
    public Object clone () {
        return new CssEditorKit ();
    }
}
