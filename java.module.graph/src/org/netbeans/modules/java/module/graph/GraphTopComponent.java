/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.java.module.graph;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 *
 * @author Tomas Zezula
 */
final class GraphTopComponent extends TopComponent implements MultiViewElement, Runnable {

    @StaticResource
    private static final String ZOOM_IN_ICON = "org/netbeans/modules/java/module/graph/resources/zoomin.gif";   //NOI18N
    @StaticResource
    private static final String ZOOM_OUT_ICON = "org/netbeans/modules/java/module/graph/resources/zoomout.gif";
    private static final RequestProcessor RP = new RequestProcessor(GraphTopComponent.class);

    private final RequestProcessor.Task refreshTask = RP.create(this);
    private final JScrollPane pane = new JScrollPane();
    private final ChangeSupport changeSupport;
    private boolean alreadyShown;
    private boolean needsRefresh;
    private MultiViewElementCallback callback;
    private EditorToolbar toolbar;
    private DependencyGraphScene scene;

    /**
     * Creates new form GraphTopComponent
     */
    GraphTopComponent(@NonNull final Lookup lkp) {
        Parameters.notNull("lkp", lkp); //NOI18N
        changeSupport = new ChangeSupport(
                lkp.lookup(FileObject.class),
                () -> needsRefresh = true);
        associateLookup(lkp);
        initComponents();
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            toolbar = new EditorToolbar();
            toolbar.setFloatable(false);
            toolbar.setRollover(true);

            toolbar.addSeparator();
            Dimension space = new Dimension(3, 0);
            toolbar.add(zoomIn);
            toolbar.addSeparator(space);
            toolbar.add(zoomOut);
//            toolbar.addSeparator(space);
//            toolbar.add(lblFind);
//            toolbar.add(txtFind);
//            toolbar.addSeparator(space);
//            toolbar.add(lblPath);
//            toolbar.add(maxPathSpinner);
//            toolbar.addSeparator(space);
//            toolbar.add(lblScopes);
//            toolbar.add(comScopes);
        }
        return toolbar;
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        pane.setWheelScrollingEnabled(true);
        add(pane, BorderLayout.CENTER);
        alreadyShown = false;
        needsRefresh = true;
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
        if (needsRefresh) {
            refreshModel();
            needsRefresh = false;
            alreadyShown = true;
        }
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
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
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public void run() {
        final FileObject moduleInfo = getLookup().lookup(FileObject.class);
        assert moduleInfo != null;
        final DependencyCalculator calc = new DependencyCalculator(moduleInfo);
        final Collection<? extends ModuleNode> nodes = calc.getNodes();
        final Collection<? extends DependencyEdge> edges = calc.getEdges();
        SwingUtilities.invokeLater(()->displayScene(nodes, edges));
    }

    @NonNull
    JScrollPane getScrollPane () {
        return pane;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        zoomIn = new javax.swing.JButton();
        zoomOut = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        zoomIn.setIcon(ImageUtilities.loadImageIcon(ZOOM_IN_ICON, true));
        org.openide.awt.Mnemonics.setLocalizedText(zoomIn, org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.zoomIn.text")); // NOI18N
        zoomIn.setFocusable(false);
        zoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        zoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomIn(evt);
            }
        });
        jToolBar1.add(zoomIn);

        zoomOut.setIcon(ImageUtilities.loadImageIcon(ZOOM_OUT_ICON, true));
        org.openide.awt.Mnemonics.setLocalizedText(zoomOut, org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.zoomOut.text")); // NOI18N
        zoomOut.setFocusable(false);
        zoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomOut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        zoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOut(evt);
            }
        });
        jToolBar1.add(zoomOut);

        jPanel1.add(jToolBar1);

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void zoomIn(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomIn
        scene.setMyZoomFactor(scene.getZoomFactor() * 1.2);
        scene.validate();
        scene.repaint();
        if (pane.getHorizontalScrollBar().isVisible() ||
            pane.getVerticalScrollBar().isVisible()) {
            revalidate();
            repaint();
        }
    }//GEN-LAST:event_zoomIn

    private void zoomOut(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOut
        scene.setMyZoomFactor(scene.getZoomFactor() * 0.8);
        scene.validate();
        scene.repaint();
        if (!pane.getHorizontalScrollBar().isVisible() &&
            !pane.getVerticalScrollBar().isVisible()) {
            revalidate();
            repaint();
        }
    }//GEN-LAST:event_zoomOut

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton zoomIn;
    private javax.swing.JButton zoomOut;
    // End of variables declaration//GEN-END:variables

    @NbBundle.Messages({
        "TXT_ComputingDependencies=Computing Dependencies...",
        "TXT_RefreshingDependencies=Refreshing Dependencies..."
    })
    private void refreshModel() {
        setPaneText(alreadyShown?
                Bundle.TXT_RefreshingDependencies() :
                Bundle.TXT_ComputingDependencies());
        enableControls(false);
        refreshTask.schedule(0);
    }

    private void enableControls(final boolean enable) {
        zoomIn.setEnabled(enable);
        zoomOut.setEnabled(enable);
    }

    private void setPaneText(@NonNull final String text)  {
        final JLabel lbl = new JLabel(text);
        lbl.setHorizontalAlignment(JLabel.CENTER);
        lbl.setVerticalAlignment(JLabel.CENTER);
        pane.setViewportView(lbl);
    }

    private void displayScene(
            @NonNull final Collection<? extends ModuleNode> nodes,
            @NonNull final Collection<? extends DependencyEdge> edges) {
        scene = new DependencyGraphScene(this);
        nodes.stream().forEach(scene::addNode);
        edges.stream()
                .forEach((e)->{
                    scene.addEdge(e);
                    scene.setEdgeSource(e, e.getSource());
                    scene.setEdgeTarget(e, e.getTarget());
        });
        JComponent sceneView = scene.getView();
        if (sceneView == null) {
            sceneView = scene.createView();
            // vlv: print
            sceneView.putClientProperty("print.printable", true); // NOI18N
        }
        pane.setViewportView(sceneView);
        scene.setSurroundingScrollPane(pane);
        scene.initialLayout();
        scene.setSelectedObjects(Collections.singleton(scene.getRootNode()));
//        if (scene.getMaxNodeDepth() > 1) {
//            lblPath.setVisible(true);
//            ((SpinnerNumberModel)maxPathSpinner.getModel()).
//                    setMaximum(Integer.valueOf(scene.getMaxNodeDepth()));
//            maxPathSpinner.setEnabled(true);
//            maxPathSpinner.setVisible(true);
//        }
//        depthHighlight();
        enableControls(true);
    }

    private static class EditorToolbar extends org.openide.awt.Toolbar {
        public EditorToolbar() {
            Border b = UIManager.getBorder("Nb.Editor.Toolbar.border"); //NOI18N
            setBorder(b);
            if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            }
        }

        @Override
        public String getUIClassID() {
            if( UIManager.get("Nb.Toolbar.ui") != null ) { //NOI18N
                return "Nb.Toolbar.ui"; //NOI18N
            }
            return super.getUIClassID();
        }

        @Override
        public String getName() {
            return "editorToolbar"; //NOI18N
        }
    }

    private static class ChangeSupport extends FileChangeAdapter implements PropertyChangeListener, DocumentListener {

        private final Runnable resetAction;
        private final FileObject file;
        private final EditorCookie.Observable ec;
        private Document currentDoc;

        ChangeSupport(
                @NonNull final FileObject file,
                @NonNull final Runnable resetAction) {
            Parameters.notNull("file", file);   //NOI18N
            Parameters.notNull("resetAction", resetAction); //NOI18N
            this.resetAction = resetAction;
            this.file = file;
            this.file.addFileChangeListener(WeakListeners.create(FileChangeListener.class, this, this.file));
            EditorCookie.Observable cookie = null;
            try {
                final DataObject dobj = DataObject.find(file);
                cookie = dobj.getLookup().lookup(EditorCookie.Observable.class);
            } catch (DataObjectNotFoundException e) {
                //pass
            }
            this.ec = cookie;
            if (this.ec != null) {
                this.ec.addPropertyChangeListener(WeakListeners.propertyChange(this, this.ec));
                assignDocListener(this.ec);
            }
        }


        @Override
        public void fileDataCreated(FileEvent fe) {
            reset();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            reset();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            reset();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            reset();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
                assignDocListener(this.ec);
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            reset();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            reset();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }

        private void reset() {
            resetAction.run();
        }

        private void assignDocListener(@NonNull final EditorCookie ec) {
            if (currentDoc != null) {
                currentDoc.removeDocumentListener(this);
            }
            currentDoc = ec.getDocument();
            if (currentDoc != null) {
                currentDoc.addDocumentListener(this);
            }
        }
    }
}
