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
            undoRedoManager.discardAllEdits ();
            DocumentInterfaceImpl loadingDocumentInterface = new DocumentInterfaceImpl (context, undoRedoManager);
            documentInterfaces.add (new WeakReference<DocumentInterface> (loadingDocumentInterface));
            final DesignDocument loadingDocument = new DesignDocument (loadingDocumentInterface);
            DocumentLoad.load (context, loadingDocument);
            IOSupport.resetCodeResolver (context.getDataObject (), loadingDocument); // HINT - if a new document is created which should update source code then do not call this method 
            loadingDocumentInterface.enable ();
            synchronized (DocumentSerializer.this) {
                document = loadingDocument;
                loaded = true;
                loading = false;
                DocumentSerializer.this.notifyAll ();
            }
            fireDesignDocumentAwareness (loadingDocument);
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

            StackTraceElement[] stackTraceElements = Thread.currentThread ().getStackTrace ();
            System.out.println ("DocumentSerializer invocation:");
            for (StackTraceElement stackTraceElement : stackTraceElements)
                System.out.println (stackTraceElement);

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

    public UndoRedo getUndoRedoManager () {
        synchronized (this) {
            return undoRedoManager;
        }
    }

    public String getProjectType () {
        return DocumentLoad.loadProjectType (context);
    }

    void notifyDataObjectClosed () {
        // TODO - possible race condition - when a data object is closed while a related document is still loading, so the document could be set after that
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
