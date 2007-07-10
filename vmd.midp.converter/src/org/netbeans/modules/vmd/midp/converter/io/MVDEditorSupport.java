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
 *
 */
package org.netbeans.modules.vmd.midp.converter.io;

import org.netbeans.modules.mobility.editor.pub.J2MEDataObject;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.text.CloneableEditorSupport;

import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import java.io.*;

/**
 * @author David Kaspar
 */
public final class MVDEditorSupport extends J2MEDataObject.J2MEEditorSupport implements EditorCookie.Observable, OpenCookie, EditCookie, PrintCookie {

    private GuardsEditor guardsEditor;
    private GuardedSectionsProvider sections;

    public MVDEditorSupport (MVDDataObject dataObject) {
        super(dataObject);
    }

    @Override
    protected void loadFromStreamToKitHook (StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        if (sections == null) {
            guardsEditor = new GuardsEditor (doc);
            String mimeType = ((CloneableEditorSupport.Env) this.env).getMimeType ();
            GuardedSectionsFactory factory = GuardedSectionsFactory.find (mimeType);
            sections = factory.create (guardsEditor);
        } else {
            guardsEditor.setDocument (doc);
        }

        if (sections != null) {
            Reader reader = sections.createGuardedReader (stream, getEncoding ());
            try {
                kit.read (reader, doc, 0);
            } finally {
                reader.close ();
            }
        } else {
            super.loadFromStreamToKitHook (doc, stream, kit);
        }
    }

    @Override
    protected void saveFromKitToStreamHook (StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        if (sections != null) {
            Writer w = sections.createGuardedWriter (stream, getEncoding ());
            try {
                kit.write (w, doc, 0, doc.getLength ());
            } finally {
                w.close ();
            }
        } else {
            super.saveFromKitToStream (doc, kit, stream);
        }
    }

    private class GuardsEditor implements GuardedEditorSupport {

        private StyledDocument document;

        public GuardsEditor(StyledDocument document) {
            this.document = document;
        }

        public StyledDocument getDocument() {
            return document;
        }

        public void setDocument(StyledDocument document) {
            this.document = document;
        }

    }

}
