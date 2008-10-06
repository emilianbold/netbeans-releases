/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.beans.beaninfo;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.beans.beaninfo.GenerateBeanInfoAction.BeanInfoWorker;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jan Pokorsky
 */
final class BIEditorSupport extends DataEditorSupport
        implements OpenCookie, EditCookie, EditorCookie, PrintCookie, EditorCookie.Observable {
    
    private static final String MV_JAVA_ID = "java"; // NOI18N
    private static final String MV_BEANINFO_ID = "beaninfo"; // NOI18N
    private BIGES guardedEditor;
    private GuardedSectionsProvider guardedProvider;
    private GenerateBeanInfoAction.BeanInfoWorker worker;
    
    /**
     * The embracing multiview TopComponent (holds the form designer and
     * java editor) - we remeber the last active TopComponent (not all clones)
     */
    private CloneableTopComponent multiviewTC;
    private TopComponentsListener topComponentsListener;

    public BIEditorSupport(DataObject obj, CookieSet cookieSet) {
        super(obj, new Environment(obj, cookieSet));
        setMIMEType("text/x-java"); // NOI18N
    }
    
    public GuardedSectionManager getGuardedSectionManager() {
        try {
            StyledDocument doc = openDocument();
            return GuardedSectionManager.getInstance(doc);
        } catch (IOException ex) {
            throw (IllegalStateException) new IllegalStateException("cannot open document").initCause(ex); // NOI18N
        }
    }
    
    @Override
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit)
            throws IOException, BadLocationException {
        
        if (guardedEditor == null) {
            guardedEditor = new BIGES();
            GuardedSectionsFactory gFactory = GuardedSectionsFactory.find(((DataEditorSupport.Env) env).getMimeType());
            if (gFactory != null) {
                guardedProvider = gFactory.create(guardedEditor);
            }
        }
        
        if (guardedProvider != null) {
            guardedEditor.doc = doc;
            Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
            Reader reader = guardedProvider.createGuardedReader(stream, c);
            try {
                kit.read(reader, doc, 0);
            } finally {
                reader.close();
            }
        } else {
            super.loadFromStreamToKit(doc, stream, kit);
        }
    }

    @Override
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream)
            throws IOException, BadLocationException {
        
        if (guardedProvider != null) {
            Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
            Writer writer = guardedProvider.createGuardedWriter(stream, c);
            try {
                kit.write(writer, doc, 0, doc.getLength());
            } finally {
                writer.close();
            }
        } else {
            super.saveFromKitToStream(doc, kit, stream);
        }
    }

    @Override
    public void saveDocument() throws IOException {
        if (worker != null && worker.isModelModified()) {
            worker.generateSources();
            worker.waitFinished();
        }
        super.saveDocument();
    }
    
    @Override
    protected boolean notifyModified() {
        if (!super.notifyModified())
            return false;
        ((Environment)this.env).addSaveCookie();
        updateMVTCName();
        return true;
    }


    @Override
    protected void notifyUnmodified() {
        super.notifyUnmodified();
        ((Environment)this.env).removeSaveCookie();
        updateMVTCName();
    }

    @Override
    protected void notifyClosed() {
        super.notifyClosed();
        worker = null;
        if (topComponentsListener != null) {
            TopComponent.getRegistry().removePropertyChangeListener(topComponentsListener);
            topComponentsListener = null;
        }
    }

    @Override
    protected Pane createPane() {
        DataObject dobj = getDataObject();
        if (dobj == null || !dobj.isValid()) {
            return super.createPane();
        }
        MultiViewDescription[] descs = {
            new JavaView(dobj),
            new BeanInfoView(dobj),
        };
        return (Pane) MultiViewFactory.createCloneableMultiView(
                descs, descs[0], new CloseHandler(dobj));
    }
    
    /** This is called by the multiview elements whenever they are created
     * (and given a observer knowing their multiview TopComponent). It is
     * important during deserialization and clonig the multiview - i.e. during
     * the operations we have no control over. But anytime a multiview is
     * created, this method gets called.
     */
    private void setTopComponent(TopComponent topComp) {
        multiviewTC = (CloneableTopComponent)topComp;
        updateMVTCName();

        if (topComponentsListener == null) {
            topComponentsListener = new TopComponentsListener();
            TopComponent.getRegistry().addPropertyChangeListener(topComponentsListener);
        }
    }
    
    private void updateMVTCName() {
        Runnable task = new Runnable() {
            public void run() {
                updateMVTCNameInAwt();
            }
        };
        
        if (EventQueue.isDispatchThread()) {
            task.run();
        } else {
            EventQueue.invokeLater(task);
        }
    }
    
    private void updateMVTCNameInAwt() {
        CloneableTopComponent topComp = multiviewTC;
        if (topComp != null) {
            String htmlname = messageHtmlName();
            String name = messageName();
            String tip = messageToolTip();
            for (CloneableTopComponent o : NbCollections.
                    iterable(topComp.getReference().getComponents())) {
                
                topComp.setHtmlDisplayName(htmlname);
                topComp.setDisplayName(name);
                topComp.setName(name);
                topComp.setToolTipText(tip);
            }
        }
    }
    
    static boolean isLastView(TopComponent tc) {
        if (!(tc instanceof CloneableTopComponent))
            return false;
        
        boolean oneOrLess = true;
        Enumeration en = ((CloneableTopComponent)tc).getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements())
                oneOrLess = false;
        }
        return oneOrLess;
    }
    
    static BIEditorSupport findEditor(DataObject dobj) {
        return dobj.getLookup().lookup(BIEditorSupport.class);
    }
    
    private static final class CloseHandler implements CloseOperationHandler, Serializable {

        private static final long serialVersionUID = 1L;
        private final DataObject dataObject;

        public CloseHandler(DataObject dataObject) {
            this.dataObject = dataObject;
        }
        
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            BIEditorSupport editor = findEditor(dataObject);
            return editor != null ? editor.canClose() : true;
        }
        
    }
    
    private static final class JavaView implements MultiViewDescription, Serializable {
        
        private static final long serialVersionUID = 1L;
        private static final String FIELD_DATAOBJECT = "dataObject"; // NOI18N
        private DataObject dataObject;

        public JavaView(DataObject dataObject) {
            this.dataObject = dataObject;
        }

        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ONLY_OPENED;
        }

        public String getDisplayName() {
            return NbBundle.getMessage(JavaView.class, "LAB_JavaSourceView");
        }

        public Image getIcon() {
            return dataObject.isValid()
                    ? dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16)
                    : ImageUtilities.loadImage("org/netbeans/modules/beans/resources/warning.gif"); // NOI18N
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String preferredID() {
            return MV_JAVA_ID;
        }

        public MultiViewElement createElement() {
            BIEditorSupport support = findEditor(dataObject);
            JavaElement javaElement = new JavaElement(support);
            // #133931: another multiview trap
            Node[] nodes = javaElement.getActivatedNodes();
            if ((nodes == null) || (nodes.length == 0)) {
                javaElement.setActivatedNodes(new Node[] {dataObject.getNodeDelegate()});
            }
            return javaElement;
        }
        
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.putFields().put(FIELD_DATAOBJECT, dataObject);
            out.writeFields();
        }
        
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            this.dataObject = (DataObject) in.readFields().get(FIELD_DATAOBJECT, in);
        }
    }
    
    private static final class BeanInfoView implements MultiViewDescription, Serializable {
        
        private static final long serialVersionUID = 1L;
        private static final String FIELD_DATAOBJECT = "dataObject"; // NOI18N
        private DataObject dataObject;

        public BeanInfoView(DataObject dataObject) {
            this.dataObject = dataObject;
        }

        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ONLY_OPENED;
        }

        public String getDisplayName() {
            return NbBundle.getMessage(BeanInfoView.class, "LAB_BeanInfoEditorView");
        }

        public Image getIcon() {
            return dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String preferredID() {
            return MV_BEANINFO_ID;
        }

        public MultiViewElement createElement() {
            return new BeanInfoElement(dataObject);
        }
        
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.putFields().put(FIELD_DATAOBJECT, dataObject);
            out.writeFields();
        }
        
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            this.dataObject = (DataObject) in.readFields().get(FIELD_DATAOBJECT, in);
        }
        
    }
    
    private static final class JavaElement extends CloneableEditor implements MultiViewElement, Externalizable {
        
        private static final long serialVersionUID = 1L;
        private MultiViewElementCallback callback;

        public JavaElement(CloneableEditorSupport support) {
            super(support);
        }

        /**
         * serialization stuff; do not use
         */
        private JavaElement() {
        }

        public JComponent getVisualRepresentation() {
            return this;
        }

        public JComponent getToolbarRepresentation() {
            JComponent toolbar = null;
            JEditorPane jepane = getEditorPane();
            if (jepane != null) {
                Document doc = jepane.getDocument();
                if (doc instanceof NbDocument.CustomToolbar) {
                    toolbar = ((NbDocument.CustomToolbar)doc).createToolbar(jepane);
                }
            }
            return toolbar;
        }

        public void setMultiViewCallback(MultiViewElementCallback callback) {
            this.callback = callback;
            BIEditorSupport editor = (BIEditorSupport) cloneableEditorSupport();
            editor.setTopComponent(callback.getTopComponent());
        }

        public CloseOperationState canCloseElement() {
            return isLastView(callback.getTopComponent())
                    ? MultiViewFactory.createUnsafeCloseState(
                            MV_JAVA_ID,
                            MultiViewFactory.NOOP_CLOSE_ACTION,
                            MultiViewFactory.NOOP_CLOSE_ACTION)
                    : CloseOperationState.STATE_OK;
        }

        @Override
        public void componentActivated() {
            super.componentActivated();
            BIEditorSupport editor = (BIEditorSupport) cloneableEditorSupport();
            if (editor.worker != null && editor.worker.isModelModified()) {
                editor.worker.generateSources();
            }
        }

        @Override
        public void componentDeactivated() {
            super.componentDeactivated();
        }

        @Override
        public void componentHidden() {
            super.componentHidden();
        }

        @Override
        public void componentShowing() {
            super.componentShowing();
        }

        @Override
        public void componentClosed() {
            // XXX copied from form module see issue 55818
            super.canClose(null, true);
            super.componentClosed();
        }
        
        @Override
        protected boolean closeLast() {
            return true;
        }

        @Override
        public void componentOpened() {
            super.componentOpened();
        }

        @Override
        public void updateName() {
            super.updateName();
            if (callback != null) {
                callback.updateTitle(getDisplayName());
            }
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
        }
        
    }
    
    private static final class BeanInfoElement extends CloneableTopComponent implements MultiViewElement, Externalizable {
        
        private static final long serialVersionUID = 1L;
        private MultiViewElementCallback callback;
        private DataObject dataObject;
        private boolean isInitialized = false;
        private final JPanel emptyToolbar = new JPanel();
        private BiPanel biPanel;
        
        public BeanInfoElement(DataObject dataObject) {
            this.dataObject = dataObject;
        }

        /**
         * serialization stuff; do not use
         */
        private BeanInfoElement() {
        }

        public JComponent getVisualRepresentation() {
            return this;
        }

        public JComponent getToolbarRepresentation() {
            return emptyToolbar;
        }

        public void setMultiViewCallback(MultiViewElementCallback callback) {
            this.callback = callback;
            BIEditorSupport editor = findEditor(dataObject);
            editor.setTopComponent(callback.getTopComponent());
        }

        public CloseOperationState canCloseElement() {
            return isLastView(callback.getTopComponent())
                    ? MultiViewFactory.createUnsafeCloseState(
                            MV_JAVA_ID,
                            MultiViewFactory.NOOP_CLOSE_ACTION,
                            MultiViewFactory.NOOP_CLOSE_ACTION)
                    : CloseOperationState.STATE_OK;
        }

        @Override
        public void componentActivated() {
            super.componentActivated();
        }

        @Override
        public void componentDeactivated() {
            super.componentDeactivated();
        }

        @Override
        public void componentHidden() {
            super.componentHidden();
        }

        @Override
        public void componentShowing() {
            super.componentShowing();
            initialize();
        }

        @Override
        public void componentClosed() {
            super.componentClosed();
        }

        @Override
        public void componentOpened() {
            super.componentOpened();
        }
        
        private void initialize() {
            if (!isInitialized) {
                setLayout(new BorderLayout());
                biPanel = new BiPanel();
                add(biPanel, BorderLayout.CENTER);
                isInitialized = true;
            } else {
                biPanel.setContext(new BiNode.Wait());
            }
            
            FileObject biFile = dataObject.getPrimaryFile();
            String name = biFile.getName();
            name = name.substring(0, name.length() - "BeanInfo".length()); // NOI18N
            FileObject javaFile = biFile.getParent().getFileObject(name, biFile.getExt());
            BIEditorSupport editor = findEditor(dataObject);
            if (javaFile != null) {
                final BeanInfoWorker beanInfoWorker = new GenerateBeanInfoAction.BeanInfoWorker(javaFile, biPanel);
                editor.worker = beanInfoWorker;
                beanInfoWorker.analyzePatterns().addTaskListener(new TaskListener() {

                    public void taskFinished(Task task) {
                        beanInfoWorker.updateUI();
                    }
                });
            } else {
                // notify missing source file
                biPanel.setContext(BiNode.createNoSourceNode(biFile));
            }
        }

        @Override
        public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
            super.readExternal(oi);
            dataObject = (DataObject) oi.readObject();
//            initialize();
        }

        @Override
        public void writeExternal(ObjectOutput oo) throws IOException {
            super.writeExternal(oo);
            oo.writeObject(dataObject);
        }
        
    }

    private static final class BIGES implements GuardedEditorSupport {
        
        StyledDocument doc = null;
        
        public StyledDocument getDocument() {
            return BIGES.this.doc;
        }
    }
    
    private static final class Environment extends DataEditorSupport.Env {

        private static final long serialVersionUID = -1;

        private final transient CookieSet cookieSet;
        private transient SaveSupport saveCookie = null;

        private final class SaveSupport implements SaveCookie {
            public void save() throws java.io.IOException {
                DataObject dobj = getDataObject();
                ((DataEditorSupport) findCloneableOpenSupport()).saveDocument();
                dobj.setModified(false);
            }
        }

        public Environment(DataObject obj, CookieSet cookieSet) {
            super(obj);
            this.cookieSet = cookieSet;
        }

        protected FileObject getFile() {
            return this.getDataObject().getPrimaryFile();
        }

        protected FileLock takeLock() throws java.io.IOException {
            return ((MultiDataObject)this.getDataObject()).getPrimaryEntry().takeLock();
        }

        public @Override CloneableOpenSupport findCloneableOpenSupport() {
            return findEditor(this.getDataObject());
        }


        public void addSaveCookie() {
            DataObject javaData = this.getDataObject();
            if (javaData.getCookie(SaveCookie.class) == null) {
                if (this.saveCookie == null)
                    this.saveCookie = new SaveSupport();
                this.cookieSet.add(this.saveCookie);
                javaData.setModified(true);
            }
        }

        public void removeSaveCookie() {
            DataObject javaData = this.getDataObject();
            if (javaData.getCookie(SaveCookie.class) != null) {
                this.cookieSet.remove(this.saveCookie);
                javaData.setModified(false);
            }
        }
    }
    
    private class TopComponentsListener implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())) {
                // Check closed top components
                @SuppressWarnings("unchecked")
                Set<TopComponent> closed = (Set<TopComponent>) evt.getOldValue();
                closed.removeAll((Set) evt.getNewValue());
                for (TopComponent o : closed) {
                    if (o instanceof CloneableTopComponent) {
                        final CloneableTopComponent topComponent = (CloneableTopComponent) o;
                        Enumeration en = topComponent.getReference().getComponents();
                        if (multiviewTC == topComponent) {
                            if (en.hasMoreElements()) {
                                // Remember next cloned top component
                                multiviewTC = (CloneableTopComponent) en.nextElement();
                            } else {
                                // All cloned top components are closed
                                notifyClosed();
                            }
                        }
                    }
                }
            }
        }
        
    }

}
