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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.completion.impl.xref;

import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

/**
 * implementation of references resolver
 * @author Vladimir Voskresensky
 */
public class ReferenceResolverImpl extends CsmReferenceResolver {
    
    public ReferenceResolverImpl() {
    }    

    public CsmReference findReference(CsmFile file, int offset) {
        assert file != null;
        BaseDocument doc = getDocument(file);
        if (doc == null) {
            return null;
        }
        CsmReference ref = ReferencesSupport.createReferenceImpl(file, doc, offset);
        return ref;
    }
    
    public CsmReference findReference(CsmFile file, int line, int column) {
        assert file != null;
        BaseDocument doc = getDocument(file);
        if (doc == null) {
            return null;
        }
        int offset = ReferencesSupport.getDocumentOffset(doc, line, column);
        CsmReference ref = ReferencesSupport.createReferenceImpl(file, doc, offset);
        return ref;
    }
    
    public CsmReference findReference(Node activatedNode) {
        EditorCookie cookie = activatedNode.getCookie(EditorCookie.class);
        if (cookie != null) {
            JEditorPane[] panes = CsmUtilities.getOpenedPanesInEQ(cookie);
            if (panes != null && panes.length>0) {
                int offset = panes[0].getCaret().getDot();
                CsmFile file = CsmUtilities.getCsmFile(activatedNode,false);
                StyledDocument doc = null;
                try {
                    doc = cookie.openDocument();
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
                if (file != null && (doc instanceof BaseDocument)) {
                    return ReferencesSupport.createReferenceImpl(file, (BaseDocument)doc, offset);
                }
            }
        }
        return null;
    }
    
    private BaseDocument getDocument(CsmFile file) {
        BaseDocument doc = null;
        try {
            doc = ReferencesSupport.getBaseDocument(file.getAbsolutePath());
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace(System.err);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return doc;
    }
}
