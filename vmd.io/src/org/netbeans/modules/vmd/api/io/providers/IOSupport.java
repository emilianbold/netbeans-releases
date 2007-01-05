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

import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.io.DataObjectContextImpl;
import org.netbeans.modules.vmd.io.CodeResolver;
import org.netbeans.modules.vmd.io.editor.EditorViewDescription;
import org.netbeans.modules.vmd.io.editor.EditorViewFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;

import java.util.*;

/**
 * Custom DataObject used by the IOSupport class must:
 * <ul>
 * <li>implement DataObjectInterface
 * <li>contain CloneableEditorSupport in data object lookup
 * <li>call notifyDataObjectClosed to notify about editor closing and therefore freeing related structure
 * <li>optionally call forceUpdateCode to force editor synchronization with design document
 * </ul>
 *
 * @author David Kaspar
 */
public final class IOSupport {

    private IOSupport() {
    }

    private static final WeakHashMap<DataObject, DataObjectContext> contexts = new WeakHashMap<DataObject, DataObjectContext> ();
    private static final WeakHashMap<DataObject, DocumentSerializer> serializers = new WeakHashMap<DataObject, DocumentSerializer> ();
    private static final WeakHashMap<DataObject, CodeResolver> resolvers = new WeakHashMap<DataObject, CodeResolver> ();

    /**
     * Returns a data object context representing specified data object.
     * @param dataObject the data object
     * @return the data object context
     */
    public synchronized static DataObjectContext getDataObjectContext (DataObject dataObject) {
        DataObjectContext context = contexts.get (dataObject);
        if (context == null) {
            getDataObjectInteface (dataObject);
            context = new DataObjectContextImpl (dataObject);
            contexts.put (dataObject, context);
        }
        return context;
    }

    /**
     * Returns a document serializer related to specified data object.
     * @param dataObject the data object
     * @return the related document serializer
     */
    public synchronized static DocumentSerializer getDocumentSerializer (DataObject dataObject) {
        DocumentSerializer serializer = serializers.get (dataObject);
        if (serializer == null) {
            DataObjectContext context = getDataObjectContext (dataObject);
            serializer = new DocumentSerializer (context);
            serializers.put (dataObject, serializer);
            resolvers.put (dataObject, new CodeResolver (context, serializer));
        }
        return serializer;
    }

    /**
     * Force update of code. This is usually invoked immediately after a document is loaded and immediately a document is saved
     * to keep the generated code synchronized with related design document.
     * @param dataObject the data object
     */
    public static void forceUpdateCode (DataObject dataObject) {
        CodeResolver resolver = resolvers.get (dataObject);
        if (resolver != null)
            resolver.forceUpdateCode ();
    }

    /**
     * Call this method to free all objects related to the data object that very assigned by the class.
     * @param dataObject
     */
    public synchronized static void notifyDataObjectClosed (DataObject dataObject) {
        CodeResolver resolver = resolvers.remove (dataObject);
        if (resolver != null)
            resolver.notifyDataObjectClosed ();
        DocumentSerializer serializer = serializers.remove (dataObject);
        if (serializer != null)
            serializer.notifyDataObjectClosed ();
        contexts.remove (dataObject);
    }

    /**
     * Returns data object interface for specified data object.
     * DataObject must implement DataObjectInterface.
     * @param dataObject the data object
     * @return the data object interface
     */
    public static DataObjectInterface getDataObjectInteface (DataObject dataObject) {
        if (! (dataObject instanceof DataObjectInterface))
            throw Debug.illegalArgument ("DataObject does not implement DataObjectInterface", dataObject);
        return (DataObjectInterface) dataObject;
    }

    /**
     * Returns a cloneable editor lookup for specified data object.
     * The CloneableEditorSupport has to be in data object lookup.
     * @param dataObject the data object
     * @return the cloneable editor support
     */
    public static CloneableEditorSupport getCloneableEditorSupport (DataObject dataObject) {
        CloneableEditorSupport editorSupport = dataObject.getLookup ().lookup (CloneableEditorSupport.class);
        if (editorSupport == null)
            throw Debug.illegalArgument ("Missing CloneableEditorSupport in DataObject lookup", dataObject);
        return editorSupport;
    }

    /**
     * Returns the design file of specified data object context.
     * @param context the data object context
     * @return the design file object
     */
    public static FileObject getDesignFile (DataObjectContext context) {
        return getDataObjectInteface (context.getDataObject ()).getDesignFile ();
    }

    /**
     * Creates pane for editor support.
     * @param context the data object context
     * @param showingType the showing type; could be null
     * @param closeHandler
     */
    public static CloneableEditorSupport.Pane createEditorSupportPane (DataObjectContext context, ShowingType showingType, CloseOperationHandler closeHandler) {
        Collection<DataEditorView> views = EditorViewFactorySupport.createEditorViews (context);
        DataEditorView defaultView;
        if (showingType != ShowingType.EDIT) {
            defaultView = Collections.max (views, new Comparator<DataEditorView>() {
                public int compare (DataEditorView o1, DataEditorView o2) {
                    return o1.getOpenPriority () - o2.getOpenPriority ();
                }
            });
        } else {
            defaultView = Collections.max (views, new Comparator<DataEditorView>() {
                public int compare (DataEditorView o1, DataEditorView o2) {
                    return o1.getEditPriority () - o2.getEditPriority ();
                }
            });
        }
        ArrayList<EditorViewDescription> descriptions = new ArrayList<EditorViewDescription> ();
        EditorViewDescription defaultDescription = null;
        for (DataEditorView view : views) {
            EditorViewDescription description = new EditorViewDescription (context, view);
            if (view == defaultView)
                defaultDescription = description;
            descriptions.add (description);
        }
        return (CloneableEditorSupport.Pane) MultiViewFactory.createCloneableMultiView (descriptions.toArray (new MultiViewDescription[descriptions.size ()]), defaultDescription, closeHandler);

    }

    /**
     * Returns a data object context for specified document
     * @param document the design document
     * @return the data object context
     */
    // TODO - should be hidden - used by ProjectUtils.getDataObjectContextForDocument method only
    public synchronized static DataObjectContext getDataObjectForDocument (DesignDocument document) {
        for (DataObject dataObject : serializers.keySet ()) {
            if (dataObject == null)
                continue;
            DocumentSerializer documentSerializer = getDocumentSerializer (dataObject); // TODO - use direct access to serializers field
            if (document == documentSerializer.getActualDocument ())
                return getDataObjectContext (dataObject);
        }
        return null;
    }

    // TODO - should be hidden - used by EditorViewElement.componentActivated method only
    public static void notifyDataEditorViewActivated (DataEditorView activatedView) {
        if (activatedView == null)
            return;
        CodeResolver resolver = resolvers.get (activatedView.getContext ().getDataObject ());
        resolver.viewActivated (activatedView);
    }

    /**
     * Describes which showing type (opening/editing) is invoked by user.
     */
    public enum ShowingType {

        OPEN, EDIT

    }

}
