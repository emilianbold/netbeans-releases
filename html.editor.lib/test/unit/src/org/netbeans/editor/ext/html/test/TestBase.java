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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.ext.html.test;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.html.HTMLKit;

/**
 * Common ancestor for all test classes.
 *
 * @author Andrei Badea
 */
public class TestBase extends NbTestCase {

    static {
        MockServices.setServices(new Class[] {RepositoryImpl.class});
    }

    private static final String PROP_MIME_TYPE = "mimeType"; //NOI18N
    
    public TestBase(String name) {
        super(name);
    }

    protected BaseDocument createDocument() {
        NbEditorDocument doc = new NbEditorDocument(HTMLKit.class);
        doc.putProperty(PROP_MIME_TYPE, BaseKit.getKit(HTMLKit.class).getContentType());
        doc.putProperty(Language.class, HTMLTokenId.language()); //hack for LanguageManager - shoudl be removed
        
        return doc;
    }
    
}
