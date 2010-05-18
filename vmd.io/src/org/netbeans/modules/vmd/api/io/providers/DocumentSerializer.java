/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.vmd.api.io.providers;

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.DocumentInterface;
import org.netbeans.modules.vmd.io.DocumentInterfaceImpl;
import org.netbeans.modules.vmd.io.DocumentLoad;
import org.netbeans.modules.vmd.io.DocumentSave;
import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.util.RequestProcessor;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.lang.ref.WeakReference;
import org.netbeans.modules.vmd.api.io.serialization.DocumentErrorHandler;
import org.netbeans.modules.vmd.api.io.serialization.DocumentErrorHandlerSupport;

/**
 * @author David Kaspar
 */
// TODO - versioning + plugable serializers
// TODO - memory leak - listeners has to be weak references.
public final class DocumentSerializer {

    private CopyOnWriteArrayList<DesignDocumentAwareness> listeners;
    private DataObjectContext context;
    private DesignDocument document;
    private UndoRedo.Manager undoRedoManager;
    private ArrayList<WeakReference<DocumentInterface>> documentInterfaces = new ArrayList<WeakReference<DocumentInterface>> ();

    private volatile boolean loaded = false;
    private volatile boolean loading = false;

    private Runnable loader = new Runnable () {
        public void run () {
            DataObjectContext context = DocumentSerializer.this.context;
            if (context == null || context.getProjectType() == null)
                return;

            undoRedoManager.discardAllEdits ();
            DocumentInterfaceImpl loadingDocumentInterface = new DocumentInterfaceImpl (context, undoRedoManager);
            documentInterfaces.add (new WeakReference<DocumentInterface> (loadingDocumentInterface));
            final DesignDocument loadingDocument = new DesignDocument (loadingDocumentInterface);
            DocumentErrorHandler errorHandler = new DocumentErrorHandler();
            DocumentLoad.load (context, loadingDocument, errorHandler);
            if (! errorHandler.getErrors().isEmpty()) {
                DocumentErrorHandlerSupport.showDocumentErrorHandlerDialog(errorHandler, context.getDataObject().getPrimaryFile());
                IOSupport.getCloneableEditorSupport (context.getDataObject()).close();
                return;
            } else if (! errorHandler.getWarnings().isEmpty())
                DocumentErrorHandlerSupport.showDocumentErrorHandlerDialog(errorHandler, context.getDataObject().getPrimaryFile());
            
            IOSupport.resetCodeResolver(context.getDataObject(), loadingDocument); // HINT - if a new document is created which should update source code then do not call this method
            loadingDocumentInterface.enable ();
            synchronized (DocumentSerializer.this) {
                if (DocumentSerializer.this.context == null) // document has been closed during loading, issue #120096
                    return;
                document = loadingDocument;
                loaded = true;
                loading = false;
                DocumentSerializer.this.notifyAll ();
            }

            // TODO - possible race condition with notifyDataObjectClosed method
            fireDesignDocumentAwareness (DocumentSerializer.this.document);
        }
    };

    DocumentSerializer (DataObjectContext context) {
        this.context = context;
        listeners = new CopyOnWriteArrayList<DesignDocumentAwareness> ();
        undoRedoManager = new UndoRedo.Manager ();
    }

    public DesignDocument getDocument () {
        startLoadingDocument ();
        return getActualDocument ();
    }

    public DesignDocument getActualDocument () {
        synchronized (this) {
            return document;
        }
    }

    public boolean isLoadingOrLoaded () {
        synchronized (this) {
            return loading  ||  loaded;
        }
    }

    public void startLoadingDocument () {
        synchronized (this) {
            if (loaded  ||  loading)
                return;
            loading = true;

            RequestProcessor.getDefault ().post (loader);
        }
    }

    public void restartLoadingDocument () {
        synchronized (this) {
            loaded = false;
            startLoadingDocument ();
        }
    }

    public void waitDocumentLoaded () {
        startLoadingDocument ();
        try {
            synchronized (this) {
                if (loaded)
                    return;
                if (loading)
                    wait ();
            }
        } catch (InterruptedException e) {
            ErrorManager.getDefault ().notify (e);
        }
    }

//    public void reloadDocument () {
//        try {
//            synchronized (this) {
//                if (loading)
//                    wait ();
//                loaded = false;
//            }
//        } catch (InterruptedException e) {
//            ErrorManager.getDefault ().notify (e);
//        }
//        waitDocumentLoaded ();
//    }

    public void saveDocument () {
        waitDocumentLoaded ();
        final DesignDocument savingDocument;
        synchronized (this) {
            savingDocument = document;
        }
        savingDocument.getTransactionManager ().readAccess (new Runnable () {
            public void run () {
                DocumentSave.save (context, savingDocument);
            }
        });
    }

    public void addDesignDocumentAwareness (DesignDocumentAwareness listener) {
        listeners.add (listener);
        listener.setDesignDocument (getActualDocument ());
    }

    public void removeDesignDocumentAwareness (DesignDocumentAwareness listener) {
        listeners.remove (listener);
    }

    private void fireDesignDocumentAwareness (DesignDocument newDocument) {
        for (DesignDocumentAwareness listener : listeners)
            listener.setDesignDocument (newDocument);
    }

    public UndoRedo.Manager getUndoRedoManager () {
        synchronized (this) {
            return undoRedoManager;
        }
    }

    public String getProjectType () {
        return DocumentLoad.loadProjectType (context);
    }

    // TODO - possible race condition - when a data object is closed while a related document is still loading, so the document could be set after that
    void notifyDataObjectClosed () {
        synchronized (this) {
            document = null;
            undoRedoManager = null;
            context = null;
        }
        fireDesignDocumentAwareness (null);
        listeners.clear ();
    }

    boolean hasDocumentInterface (DocumentInterface documentInterface) {
        if (documentInterface == null)
            return false;
        for (WeakReference<DocumentInterface> ref : documentInterfaces)
            if (ref.get () == documentInterface)
                return true;
        return false;
    }

}
