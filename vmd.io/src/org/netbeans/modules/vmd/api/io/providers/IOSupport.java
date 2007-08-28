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

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.DocumentInterface;
import org.netbeans.modules.vmd.io.CodeResolver;
import org.netbeans.modules.vmd.io.DataObjectContextImpl;
import org.netbeans.modules.vmd.io.DocumentLoad;
import org.netbeans.modules.vmd.io.editor.EditorViewDescription;
import org.netbeans.modules.vmd.io.editor.EditorViewFactorySupport;
import org.netbeans.modules.vmd.io.editor.EditorViewElement;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import org.netbeans.modules.vmd.api.io.serialization.DocumentErrorHandler;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;


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
    private static final WeakHashMap<DataObject, DataObjectContext> contexts = new WeakHashMap<DataObject, DataObjectContext>();
    private static final WeakHashMap<DataObject, DocumentSerializer> serializers = new WeakHashMap<DataObject, DocumentSerializer>();
    private static final WeakHashMap<DataObject, CodeResolver> resolvers = new WeakHashMap<DataObject, CodeResolver>();
    private static final WeakHashMap<DataObject, Boolean> documentUpdating = new WeakHashMap<DataObject, Boolean>();

    /**
     * Returns a data object context representing specified data object.
     * @param dataObject the data object
     * @return the data object context
     */
    public static synchronized DataObjectContext getDataObjectContext(DataObject dataObject) {
        DataObjectContext context = contexts.get(dataObject);
        if (context == null) {
            getDataObjectInteface(dataObject);
            context = new DataObjectContextImpl(dataObject);
            contexts.put(dataObject, context);
        }
        return context;
    }

    /**
     * Returns a document serializer related to specified data object.
     * @param dataObject the data object
     * @return the related document serializer
     */
    public static synchronized DocumentSerializer getDocumentSerializer(DataObject dataObject) {
        DocumentSerializer serializer = serializers.get(dataObject);
        if (serializer == null) {
            DataObjectContext context = getDataObjectContext(dataObject);
            serializer = new DocumentSerializer(context);
            serializers.put(dataObject, serializer);
            resolvers.put(dataObject, new CodeResolver(context, serializer));
        }
        return serializer;
    }

    /**
     * Force update of code. This is usually invoked immediately after a document is loaded and immediately a document is saved
     * to keep the generated code synchronized with related design document.
     * @param dataObject the data object
     */
    public static void forceUpdateCode(DataObject dataObject) {
        CodeResolver resolver = resolvers.get(dataObject);
        if (resolver != null) {
            resolver.forceUpdateCode();
        }
    }

    /**
     * Call this method to free all objects related to the data object that very assigned by the class.
     * @param dataObject the data object
     */
    public static synchronized void notifyDataObjectClosed(DataObject dataObject) {
        documentUpdating.remove(dataObject);
        CodeResolver resolver = resolvers.remove(dataObject);
        if (resolver != null) {
            resolver.notifyDataObjectClosed();
        }
        DocumentSerializer serializer = serializers.remove(dataObject);
        if (serializer != null) {
            serializer.notifyDataObjectClosed();
        }
        contexts.remove(dataObject);
    }

    /**
     * Returns data object interface for specified data object.
     * DataObject must implement DataObjectInterface.
     * @param dataObject the data object
     * @return the data object interface
     */
    public static DataObjectInterface getDataObjectInteface(DataObject dataObject) {
        if (!(dataObject instanceof DataObjectInterface)) {
            throw Debug.illegalArgument("DataObject does not implement DataObjectInterface", dataObject); // NOI18N
        }
        return (DataObjectInterface) dataObject;
    }

    /**
     * Returns a cloneable editor lookup for specified data object.
     * The CloneableEditorSupport has to be in data object lookup.
     * @param dataObject the data object
     * @return the cloneable editor support
     */
    public static CloneableEditorSupport getCloneableEditorSupport(DataObject dataObject) {
        CloneableEditorSupport editorSupport = dataObject.getLookup().lookup(CloneableEditorSupport.class);
        if (editorSupport == null) {
            throw Debug.illegalArgument("Missing CloneableEditorSupport in DataObject lookup", dataObject); // NOI18N
        }
        return editorSupport;
    }

    /**
     * Returns the design file of specified data object context.
     * @param context the data object context
     * @return the design file object
     */
    public static FileObject getDesignFile(DataObjectContext context) {
        return getDataObjectInteface(context.getDataObject()).getDesignFile();
    }

    /**
     * Creates an array of multi view descriptions for editor support for a specified context.
     * @param context the data object context
     * @return the arra of multi view descriptions
     */
    public static MultiViewDescription[] createEditorSupportPane(DataObjectContext context) {
        Collection<DataEditorView> views = EditorViewFactorySupport.createEditorViews(context);
        ArrayList<EditorViewDescription> descriptions = new ArrayList<EditorViewDescription>();
        for (DataEditorView view : views) {
            descriptions.add(new EditorViewDescription(context, view));
        }
        return descriptions.toArray(new MultiViewDescription[descriptions.size()]);
    }

    /**
     * Returns data editor view instance assigned to a multi view description.
     * @param description the description
     * @return the data editor view
     */
    public static DataEditorView getDataEditorView(MultiViewDescription description) {
        return description instanceof EditorViewDescription ? ((EditorViewDescription) description).getView() : null;
    }

    /**
     * Returns a data object context for specified document
     * @param document the design document
     * @return the data object context
     */
    // TODO - should be hidden - used by ProjectUtils.getDataObjectContextForDocument method only
    public static synchronized DataObjectContext getDataObjectContextForDocumentInterface(DesignDocument document) {
        assert Debug.isFriend(ProjectUtils.class, "getDataObjectContextForDocument"); // NOI18N
        DocumentInterface documentInterface = document.getDocumentInterface();
        for (DataObject dataObject : serializers.keySet()) {
            if (dataObject == null) {
                continue;
            }
            DocumentSerializer documentSerializer = getDocumentSerializer(dataObject); // TODO - use direct access to serializers field
            if (documentSerializer.hasDocumentInterface(documentInterface)) {
                return getDataObjectContext(dataObject);
            }
        }
        return null;
    }

    // TODO - should be hidden - used by EditorViewElement.componentActivated method only
    public static void notifyDataEditorViewActivated(DataEditorView activatedView) {
        assert Debug.isFriend(EditorViewElement.class, "componentActivated"); // NOI18N
        if (activatedView == null) {
            return;
        }
        CodeResolver resolver = resolvers.get(activatedView.getContext().getDataObject());
        resolver.viewActivated(activatedView);
    }

    /**
     * Loads the document and resolves the project type of the document.
     * @param context the context
     * @return the project type
     */
    public static String resolveProjectType(DataObjectContext context) {
        return DocumentLoad.loadProjectType(context);
    }

    static void resetCodeResolver(DataObject dataObject, DesignDocument document) {
        CodeResolver resolver = resolvers.get(dataObject);
        resolver.resetModelModifiedStatus(document);
    }

    /**
     * Returns whether a document updating is enabled.
     * @param dataObject the data object
     * @return true, if enabled
     */
    public static boolean isDocumentUpdatingEnabled(DataObject dataObject) {
        Boolean enabled = documentUpdating.get(dataObject);
        return enabled != null && enabled;
    }

    /**
     * Sets whether a document updating is enabled.
     * @param dataObject the data object
     * @param enabled if true, then enabled
     */
    public static void setDocumentUpdatingEnabled(DataObject dataObject, boolean enabled) {
        documentUpdating.put(dataObject, enabled);
    }

    /**
     * Visuals content of DocumentErrorHandler. All messages (wrning and errors) are displayed in 
     * informational dialog window.
     * @param errorHandler 
     * @param fileName
     */
    public static void showDocumentErrorHandlerDialog(final DocumentErrorHandler errorHandler, final String fileName) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (errorHandler.getErrors().isEmpty() && errorHandler.getWarnings().isEmpty()) {
                    return;
                }
                StringBuffer warnings = new StringBuffer();
                if (!errorHandler.getWarnings().isEmpty()) {
                    warnings.append("<HTML><FONT COLOR=BLUE> <STRONG>" + NbBundle.getMessage(IOSupport.class, "LBL_DialogWarning") + "</STRONG></FONT><UL>"); //NOI18N
                    for (String warning : errorHandler.getWarnings()) {
                        warnings.append("<LI>"); //NOI18N
                        warnings.append(warning);
                        warnings.append("</LI>"); //NOI18N
                    }
                    warnings.append("</UL>"); //NOI18N
                }
                StringBuffer errors = new StringBuffer();
                if (!errorHandler.getErrors().isEmpty()) {
                    errors.append("<HTML><FONT COLOR=RED> <STRONG>" + NbBundle.getMessage(IOSupport.class, "LBL_DialogError") + "</STRONG></FONT><UL>"); //NOI18N
                    for (String error : errorHandler.getErrors()) {
                        errors.append("<LI>"); //NOI18N
                        errors.append(error);
                        errors.append("</LI>"); //NOI18N
                    }
                    errors.append("</UL>"); //NOI18N
                }
                DialogDescriptor descriptor = new DialogDescriptor(errors.toString() + warnings.toString(), NbBundle.getMessage(IOSupport.class, "LBL_DialogTitle") + " " + fileName); //NOI18N
                DialogDisplayer.getDefault().notify(descriptor);
            }
        });
    }
}