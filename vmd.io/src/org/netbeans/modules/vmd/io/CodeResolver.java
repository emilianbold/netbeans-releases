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
package org.netbeans.modules.vmd.io;

import org.netbeans.modules.vmd.api.io.*;
import org.netbeans.modules.vmd.api.io.providers.DocumentSerializer;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.openide.util.Lookup;

/**
 * @author David Kaspar
 */
public class CodeResolver implements DesignDocumentAwareness {

    private static final Lookup.Result<CodeGenerator> result = Lookup.getDefault ().lookupResult (CodeGenerator.class);

    private DataObjectContext context;
    private DocumentSerializer serializer;
    private volatile DesignDocument document;
    private volatile long documentState = Long.MIN_VALUE;
    private volatile DataEditorView.Kind viewKind;

    public CodeResolver (DataObjectContext context, DocumentSerializer serializer) {
        this.context = context;
        this.serializer = serializer;
        this.serializer.addDesignDocumentAwareness (this);
    }

    public void setDesignDocument (final DesignDocument designDocument) {
        DataEditorView activeView = ActiveViewSupport.getDefault ().getActiveView ();
        if (activeView == null)
            return;
        if (activeView.getContext () != context)
            return;
        update (designDocument, activeView.getKind ());
    }

    public void viewActivated (DataEditorView view) {
        update (serializer.getDocument (), view.getKind ());
    }

    public void forceUpdateCode () {
        update (serializer.getDocument (), null);
    }

    private void update (final DesignDocument document, final DataEditorView.Kind kind) {
        if (document == null)
            return;
        if (kind != null  &&  kind.equals (DataEditorView.Kind.NONE))
            return;

        synchronized (this) {
            if (kind != null  &&  kind.equals (viewKind))
                return;

            boolean modelModified = CodeResolver.this.document != document || CodeResolver.this.documentState != document.getListenerManager ().getDocumentState ();
            boolean switchedFromModelToCode = kind != null && kind.equals (DataEditorView.Kind.CODE) && viewKind != null && viewKind.equals (DataEditorView.Kind.MODEL);
            final boolean switchedFromCodeToModel = kind != null && kind.equals (DataEditorView.Kind.MODEL) && viewKind != null && viewKind.equals (DataEditorView.Kind.CODE);
            final boolean regenerateSourceCode = modelModified  &&  switchedFromModelToCode;

            final long eventID = document.getTransactionManager ().writeAccess (new Runnable() {
                public void run () {
                    if (regenerateSourceCode)
                        for (CodeGenerator generator : result.allInstances ())
                            generator.validateModelForCodeGeneration (context, document);

                    if (regenerateSourceCode  ||  switchedFromCodeToModel)
                        for (CodeGenerator generator : result.allInstances ())
                            generator.updateModelFromCode (context, document);
                }
            });

            CodeResolver.this.document = document;
            if (regenerateSourceCode)
                CodeResolver.this.documentState = eventID;
            if (kind != null)
                CodeResolver.this.viewKind = kind;

            if (regenerateSourceCode)
                document.getTransactionManager ().readAccess (new Runnable() {
                    public void run () {
                        for (CodeGenerator generator : result.allInstances ())
                            generator.updateCodeFromModel (context, document);
                    }
                });
        }
    }

    public void notifyDataObjectClosed () {
        serializer.removeDesignDocumentAwareness (this);
        serializer = null;
        context = null;
    }

}
