/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.navigation.hierarchy;

import com.sun.source.util.TreePath;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.java.navigation.JavadocTopComponent;
import org.netbeans.modules.java.navigation.base.Pair;
import org.netbeans.modules.java.navigation.base.TapPanel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd = "-//org.netbeans.modules.java.navigation.hierarchy//Hierarchy//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "HierarchyTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "rightSlidingSide", openAtStartup = false)
@ActionID(category = "Window", id = "org.netbeans.modules.java.navigation.hierarchy.HierarchyTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@Messages({
    "CTL_HierarchyTopComponent=Hierarchy",
    "HINT_HierarchyTopComponent=This is a Hierarchy window"
})
public final class HierarchyTopComponent extends TopComponent implements ExplorerManager.Provider, ActionListener {

    private static final Logger LOG = Logger.getLogger(HierarchyTopComponent.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(HierarchyTopComponent.class);
    @StaticResource
    private static final String REFRESH_ICON = "org/netbeans/modules/java/navigation/resources/hierarchy_refresh.png";
    @StaticResource
    private static final String JDOC_ICON = "org/netbeans/modules/java/navigation/resources/javadoc_open.png";
    private static HierarchyTopComponent instance;

    private final ExplorerManager explorerManager;
    private final Box upperToolBar;
    private final BeanTreeView btw;
    private final TapPanel lowerToolBar;
    private final JComboBox viewTypeCombo;
    private final JComboBox historyCombo;
    private final JButton refreshButton;
    private final JButton jdocButton;

    public HierarchyTopComponent() {
        explorerManager = new ExplorerManager();
        initComponents();
        setName(Bundle.CTL_HierarchyTopComponent());
        setToolTipText(Bundle.HINT_HierarchyTopComponent());
        upperToolBar = new Box(BoxLayout.X_AXIS);
        viewTypeCombo = new JComboBox(new DefaultComboBoxModel(ViewType.values()));
        historyCombo = new JComboBox(HierarchyHistoryUI.createModel());
        historyCombo.setRenderer(HierarchyHistoryUI.createRenderer());
        historyCombo.addActionListener(this);
        refreshButton = new JButton(ImageUtilities.loadImageIcon(REFRESH_ICON, true));
        refreshButton.addActionListener(this);
        jdocButton = new JButton(ImageUtilities.loadImageIcon(JDOC_ICON, true));
        jdocButton.addActionListener(this);
        upperToolBar.add(viewTypeCombo);
        upperToolBar.add(historyCombo);
        upperToolBar.add(refreshButton);
        upperToolBar.add(jdocButton);
        add(upperToolBar, BorderLayout.NORTH);
        btw = createBeanTreeView();
        add(btw,BorderLayout.CENTER);
        lowerToolBar = new TapPanel();
        add(lowerToolBar, BorderLayout.SOUTH);

    }

    public void setContext (@NonNull final JavaSource context) {
        showBusy();
        final Collection<FileObject> fos = context.getFileObjects();
        assert fos.size() == 1;
        final FileObject fo = fos.iterator().next();
        JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
        final Callable<Pair<URI,ElementHandle<TypeElement>>> resolver;
        if (lastFocusedComponent != null && fo.equals(getFileObject(Utilities.getDocument(lastFocusedComponent)))) {
            resolver = new EditorResolver(context, fo, lastFocusedComponent.getCaret().getDot());
        } else {
            resolver = new FileResolver(context, fo);
        }
        schedule(resolver);

    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        if (refreshButton == e.getSource()) {
            final JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
            final JavaSource js = JavaSource.forDocument(Utilities.getDocument(lastFocusedComponent));
            if (js != null) {
                setContext(js);
            }
        } else if (jdocButton == e.getSource()) {
            final TopComponent win = JavadocTopComponent.findInstance();
            win.open();
        } else if (historyCombo == e.getSource()) {
            final Object selItem = historyCombo.getSelectedItem();
            if (selItem instanceof Pair) {
                final Pair<URI,ElementHandle<TypeElement>> pair = (Pair<URI,ElementHandle<TypeElement>>)selItem;
                schedule(new Callable<Pair<URI, ElementHandle<TypeElement>>>() {
                    @Override
                    public Pair<URI, ElementHandle<TypeElement>> call() throws Exception {
                        return pair;
                    }
                });
            }
            
        } else if (viewTypeCombo == e.getSource()) {
            
        }
    }


    @CheckForNull
    private static FileObject getFileObject(@NullAllowed final Document doc) {
        if (doc == null) {
            return null;
        }
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof FileObject) {
            return (FileObject)sdp;
        }
        if (sdp instanceof DataObject) {
            return ((DataObject)sdp).getPrimaryFile();
        }
        return null;
    }

    @NonNull
    private static BeanTreeView createBeanTreeView() {
        return new BeanTreeView();
    }

    private void showBusy() {
        explorerManager.setRootContext(WaitNode.INSTANCE);
    }

    private void schedule(@NonNull final Callable<Pair<URI,ElementHandle<TypeElement>>> resolver) {
        assert resolver != null;
        final RunnableFuture<Pair<URI,ElementHandle<TypeElement>>> becomesType = new FutureTask<Pair<URI,ElementHandle<TypeElement>>>(resolver);
        RP.execute(becomesType);
        final Runnable refreshTask = new RefreshTask(becomesType);
        RP.execute(refreshTask);
    }

    public static synchronized HierarchyTopComponent findDefault() {

        HierarchyTopComponent component = instance;

        if (component == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent("HierarchyTopComponent"); //NOI18N
            if (tc instanceof HierarchyTopComponent) {
                component = instance = (HierarchyTopComponent) tc;
            } else {
                component = instance = new HierarchyTopComponent();
            }
        }
        return component;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @NbBundle.Messages({
        "LBL_SuperTypeView=Supertype View",
        "LBL_SubTypeView=Subtype View"})
    private static enum ViewType {
                       
        SUPER_TYPE(Bundle.LBL_SuperTypeView()),
        SUB_TYPE(Bundle.LBL_SubTypeView());

        private final String displayName;

        private ViewType(@NonNull final String displayName) {
            assert displayName != null;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static final class FileResolver implements Callable<Pair<URI,ElementHandle<TypeElement>>>{

        private final JavaSource js;
        private final FileObject fo;

        public FileResolver(
                @NonNull final JavaSource js,
                @NonNull final FileObject fo) {
            assert js != null;
            assert fo != null;
            this.js = js;
            this.fo = fo;
        }

        @Override
        public Pair<URI,ElementHandle<TypeElement>> call() throws Exception {
            final List<ElementHandle<TypeElement>> ret = new ArrayList<ElementHandle<TypeElement>>(1);
            ret.add(null);
            js.runUserActionTask(
                    new Task<CompilationController>(){
                        @Override
                        public void run(CompilationController cc) throws Exception {
                            cc.toPhase (Phase.ELEMENTS_RESOLVED);
                            ret.set(0,findMainElement(cc,fo.getName()));
                        }
                    },
                    true);
            final ElementHandle<TypeElement> handle = ret.get(0);
            if (handle == null) {
                return null;
            }
            final FileObject file = SourceUtils.getFile(handle, js.getClasspathInfo());
            if (file == null) {
                return null;
            }
            return Pair.<URI,ElementHandle<TypeElement>>of(file.toURI(),handle);
        }

        @CheckForNull
        static ElementHandle<TypeElement> findMainElement(
                @NonNull final CompilationController cc,
                @NonNull final String fileName) {
            final List<? extends TypeElement> topLevels = cc.getTopLevelElements();
            if (topLevels.isEmpty()) {
                return null;
            }
            TypeElement candidate = topLevels.get(0);
            for (int i = 1; i< topLevels.size(); i++) {
                if (fileName.contentEquals(topLevels.get(i).getSimpleName())) {
                    candidate = topLevels.get(i);
                    break;
                }
            }
            return ElementHandle.create(candidate);
        }
    }

    private static final class EditorResolver implements Callable<Pair<URI,ElementHandle<TypeElement>>> {

        private final JavaSource js;
        private final FileObject fo;
        private final int dot;

        public EditorResolver(
                @NonNull final JavaSource js,
                @NonNull final FileObject fo,
                final int dot) {
            assert js != null;
            assert fo != null;
            this.js = js;
            this.fo = fo;
            this.dot = dot;
        }

        @Override
        public Pair<URI,ElementHandle<TypeElement>> call() throws Exception {
            final List<ElementHandle<TypeElement>> ret = new ArrayList<ElementHandle<TypeElement>>();
            ret.add(null);
            js.runUserActionTask(
                    new Task<CompilationController>(){
                        @Override
                        public void run(CompilationController cc) throws Exception {
                            cc.toPhase (Phase.RESOLVED);
                            Document document = cc.getDocument ();
                            if (document != null) {
                                // Find the TreePath for the caret position
                                final TreePath tp = cc.getTreeUtilities ().pathFor(dot);
                                // Get Element
                                Element element = cc.getTrees().getElement(tp);
                                if (element instanceof TypeElement) {
                                    ret.set(0, ElementHandle.create((TypeElement) element));
                                } else if (element instanceof VariableElement) {
                                    TypeMirror typeMirror = ((VariableElement) element).asType();
                                    if (typeMirror.getKind() == TypeKind.DECLARED) {
                                        element = ((DeclaredType) typeMirror).asElement();
                                        if (element != null) {
                                            ret.set(0, ElementHandle.create((TypeElement) element));
                                        }
                                    }
                                } else if (element instanceof ExecutableElement) {
                                    if (element.getKind() == ElementKind.METHOD) {
                                        TypeMirror typeMirror = ((ExecutableElement) element).getReturnType();
                                        if (typeMirror.getKind() == TypeKind.DECLARED) {
                                            element = ((DeclaredType) typeMirror).asElement();
                                            if (element != null) {
                                                ret.set(0, ElementHandle.create((TypeElement) element));
                                            }
                                        }
                                    } else if (element.getKind() == ElementKind.CONSTRUCTOR) {
                                        element = element.getEnclosingElement();
                                        if (element != null) {
                                            ret.set(0, ElementHandle.create((TypeElement) element));
                                        }
                                    }
                                } else {
                                    ret.set(0,FileResolver.findMainElement(cc, fo.getName()));
                                }
                            }
                        }
                    },
                    true);
            final ElementHandle<TypeElement> handle = ret.get(0);
            if (handle == null) {
                return null;
            }
            final FileObject file = SourceUtils.getFile(handle, js.getClasspathInfo());
            if (file == null) {
                return null;
            }
            return Pair.<URI,ElementHandle<TypeElement>>of(file.toURI(),handle);
        }
    }


    private final class RefreshTask implements Runnable {

        private final Future<Pair<URI,ElementHandle<TypeElement>>> toShow;

        RefreshTask(@NonNull final Future<Pair<URI,ElementHandle<TypeElement>>> toShow) {
            assert toShow != null;
            this.toShow = toShow;
        }

        @Override
        public void run() {
            try {
                final Pair<URI,ElementHandle<TypeElement>> pair = toShow.get();
                LOG.log(Level.FINE, "Showing hierarchy for: {0}", pair.second.getQualifiedName());
                HierarchyHistory.getInstance().addToHistory(pair);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        historyCombo.getModel().setSelectedItem(pair);
                        explorerManager.setRootContext(new AbstractNode(Children.LEAF));
                    }
                });

            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    private static class WaitNode extends AbstractNode {

        static final WaitNode INSTANCE = new WaitNode();

        @StaticResource
        private static final String ICON = "org/netbeans/modules/java/navigation/resources/wait.gif";   //NOI18N

        @NbBundle.Messages({
            "LBL_PleaseWait=Please Wait..."
        })
        private WaitNode() {
            super(Children.LEAF);
            setIconBaseWithExtension(ICON);
            setDisplayName(Bundle.LBL_PleaseWait());
        }
    }
}
