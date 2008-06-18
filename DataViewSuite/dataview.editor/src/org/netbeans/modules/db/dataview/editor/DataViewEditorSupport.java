/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.dataview.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import javax.swing.JPanel;
import javax.swing.text.StyledDocument;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Ahimanikya Satapathy
 */
public class DataViewEditorSupport extends DataEditorSupport implements OpenCookie,
        EditCookie, EditorCookie.Observable, LineCookie, CloseCookie, PrintCookie {

    public static final String EDITOR_CONTAINER = "dataViewEditorContainer"; // NOI18N

    public DataViewEditorSupport(DataViewDataObject sobj) {
        super(sobj, new DVEditorEnv(sobj));
        setMIMEType("text/x-sql");
    }

    public DVEditorEnv getEnv() {
        return (DVEditorEnv) env;
    }

    @Override
    protected Pane createPane() {
        multiviewTC = DataViewMultiViewFactory.createMultiView((DataViewDataObject) getDataObject());
        multiviewTC.setName(getDataObject().getPrimaryFile().getNameExt());

        Mode editorMode = WindowManager.getDefault().findMode(DataViewEditorSupport.EDITOR_MODE);
        if (editorMode != null) {
            editorMode.dockInto(multiviewTC);
        }
        return (Pane) multiviewTC;
    }

    /**
     * This is called by the multiview elements whenever they are created
     * (and given a observer knowing their multiview TopComponent). It is
     * important during deserialization and clonig the multiview - i.e. during
     * the operations we have no control over. But anytime a multiview is
     * created, this method gets called.
     *
     * @param  topComp  TopComponent to which we are associated.
     */
    public void setTopComponent(TopComponent mvtc) {
        this.multiviewTC = mvtc;

        // Force the title to update so the * left over from when the
        // modified data object was discarded is removed from the title.
        // It is okay for this to be invoked multiple times.
        if (!getEnv().getDVDataObject().isModified()) {
            // Update later to avoid a loop.
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    updateTitles();
                }
            });
        }
    }

    public static boolean isLastView(TopComponent tc) {

        if (!(tc instanceof CloneableTopComponent)) {
            return false;
        }
        boolean oneOrLess = true;
        Enumeration en = ((CloneableTopComponent) tc).getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements()) {
                oneOrLess = false;
            }
        }

        return oneOrLess;
    }
    
    @Override
    protected void updateTitles() {
        // This method is invoked by DataEditorSupport.DataNodeListener
        // whenever the DataNode displayName property is changed. It is
        // also called when the CloneableEditorSupport is (un)modified.
        // Let the superclass handle the CloneableEditor instances.
        super.updateTitles();

        // We need to get the title updated on the MultiViewTopComponent.
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (multiviewTC != null) {
                    multiviewTC.setHtmlDisplayName(messageHtmlName());
                    String name = messageName();
                    multiviewTC.setDisplayName(name);
                    multiviewTC.setName(name);
                    multiviewTC.setToolTipText(messageToolTip());
                }
            }
        });
    }

    /**
     * Env class extends SchemaEditorSupport.Env.
     * overrides findSchemaEditorSupport
     *
     */
    protected static class DVEditorEnv extends DataEditorSupport.Env {

        static final long serialVersionUID = 1099957785497677206L;

        public DVEditorEnv(DataViewDataObject obj) {
            super(obj);
        }

        public CloneableEditorSupport findTextEditorSupport() {
            return getDVDataObject().getDataViewEditorSupport();
        }

        public DataViewDataObject getDVDataObject() {
            return (DataViewDataObject) getDataObject();
        }

        @Override
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        @Override
        protected FileLock takeLock() throws IOException {
            return null;//getDataObject().getPrimaryFile().lock();

        }
    }

    public boolean silentClose() {
        return super.close(false);
    }

    /**
     * Implementation of CloseOperationHandler for multiview. Ensures both
     * column view and xml editor are correctly closed, data saved, etc. Holds
     * a reference to Schema DataObject only - to be serializable with the
     * multiview TopComponent without problems.
     */
    public static class CloseHandler implements CloseOperationHandler, Serializable {

        private CloseHandler() {
            super();
        }

        public CloseHandler(DataViewDataObject schemaDO) {
            dataObject = schemaDO;
        }

        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            DataViewEditorSupport dvEditor = dataObject == null ? null
                    : (DataViewEditorSupport) dataObject.getCookie(DataViewEditorSupport.class);
            if (dvEditor == null) {
                return true;
            }
            // This handles saving the document.
            boolean close = dvEditor.canClose();
            if (close) {
                if (dataObject.isValid()) {
                    // In case user discarded edits, need to reload.
                    if (dataObject.isModified()) {
                        // In case user discarded edits, need to reload.
                        dvEditor.reloadDocument().waitFinished();
                    }
                    // Need to properly close the support, too.
                    dvEditor.notifyClosed();
                }
                dataObject.setModified(false);
            }
            return close;
        }
        private static final long serialVersionUID = -3838395157610633251L;
        private DataViewDataObject dataObject;
    }
    private TopComponent multiviewTC;

    @Override
    public void saveDocument() throws IOException {
        super.saveDocument();
        getDataObject().setModified(false);
    }

    @Override
    protected void notifyClosed() {
        super.notifyClosed();
    }

    @Override
    protected void initializeCloneableEditor(CloneableEditor editor) {
        super.initializeCloneableEditor(editor);
        // Update later to avoid an infinite loop.
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                updateTitles();
            }
        });
    }

    @Override
    public StyledDocument openDocument() throws IOException {
        return super.openDocument();
    }
    
    @Override
    protected Component wrapEditorComponent(Component editor) {
        JPanel container = new JPanel(new BorderLayout());
        container.setName(EDITOR_CONTAINER); // NOI18N
        container.add(editor, BorderLayout.CENTER);
        return container;
    }

    public void setDatabaseConnection(DatabaseConnection arg0) {

    }

    public void execute() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
