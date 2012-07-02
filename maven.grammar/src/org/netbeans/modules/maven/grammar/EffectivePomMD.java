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

package org.netbeans.modules.maven.grammar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.editor.SideBarFactory;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import static org.netbeans.modules.maven.grammar.Bundle.*;
import org.netbeans.modules.maven.grammar.effpom.AnnotationBarManager;
import org.netbeans.modules.maven.grammar.effpom.LocationAwareMavenXpp3Writer;
import org.netbeans.modules.maven.grammar.effpom.LocationAwareMavenXpp3Writer.Location;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerFactory;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerPanelProvider;
import org.openide.awt.Actions;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

public class EffectivePomMD implements MultiViewDescription, Serializable {
    private static final Logger LOG = Logger.getLogger(EffectivePomMD.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(EffectivePomMD.class);

    private final Lookup lookup;

    private EffectivePomMD(Lookup lookup) {
        this.lookup = lookup;
    }

    @Override public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Messages("TAB_EFF_Pom=Effective POM")
    @Override public String getDisplayName() {
        return TAB_EFF_Pom();
    }

    @Override public Image getIcon() {
        return ImageUtilities.loadImage(POMDataObject.POM_ICON, true);
    }

    @Override public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override public String preferredID() {
        return "effpom"; // XXX could be ArtifactViewer.HINT_* constant
    }

    @Override public MultiViewElement createElement() {
        return new EffPOMView(lookup);
    }

    @ServiceProvider(service=ArtifactViewerPanelProvider.class, position=600)
    public static class Factory implements ArtifactViewerPanelProvider {

        @Override public MultiViewDescription createPanel(Lookup lookup) {
            return new EffectivePomMD(lookup);
        }

    }
    
    @MultiViewElement.Registration(
        displayName="#TAB_Effective",
        iconBase=POMDataObject.POM_ICON,
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID="effectivePom",
        mimeType=Constants.POM_MIME_TYPE,
        position=101
    )
    @Messages("TAB_Effective=Effective")
    public static MultiViewElement forPOM(final Lookup editor) {
        class L extends ProxyLookup implements PropertyChangeListener {
            Project p;
            L() {
                FileObject pom = editor.lookup(FileObject.class);
                if (pom != null) {
                    p = FileOwnerQuery.getOwner(pom);
                    if (p != null) {
                        NbMavenProject nbmp = p.getLookup().lookup(NbMavenProject.class);
                        if (nbmp != null) {
                            nbmp.addPropertyChangeListener(WeakListeners.propertyChange(this, nbmp));
                            reset();
                        } else {
                            LOG.log(Level.WARNING, "not a Maven project: {0}", p);
                        }
                    } else {
                        LOG.log(Level.WARNING, "no owner of {0}", pom);
                    }
                } else {
                    LOG.log(Level.WARNING, "no FileObject in {0}", editor);
                }
            }
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                    reset();
                }
            }
            private void reset() {
                ArtifactViewerFactory avf = Lookup.getDefault().lookup(ArtifactViewerFactory.class);
                if (avf != null) {
                    Lookup l = avf.createLookup(p);
                    if (l != null) {
                        setLookups(l);
                    } else {
                        LOG.log(Level.WARNING, "no artifact lookup for {0}", p);
                    }
                } else {
                    LOG.warning("no ArtifactViewerFactory found");
                }
            }
        }
        return new EffPOMView(new L());
    }    

    private static class EffPOMView implements MultiViewElement, Runnable {

        private final Lookup lookup;
        private final RequestProcessor.Task task = RP.create(this);
        private JToolBar toolbar;
        private JPanel panel;

        EffPOMView(Lookup lookup) {
            this.lookup = lookup;
        }

        @Override public JComponent getVisualRepresentation() {
            if (panel == null) {
                panel = new JPanel(new BorderLayout());
            }
            return panel;
        }

        @Override public JComponent getToolbarRepresentation() {
            // XXX copied from org.netbeans.modules.maven.repository.ui, should be made into shared API
            if (toolbar == null) {
                toolbar = new JToolBar();
                toolbar.setFloatable(false);
                Action[] a = new Action[1];
                Action[] actions = lookup.lookup(a.getClass());
                Dimension space = new Dimension(3, 0);
                toolbar.addSeparator(space);
                for (Action act : actions) {
                    JButton btn = new JButton();
                    Actions.connect(btn, act);
                    toolbar.add(btn);
                    toolbar.addSeparator(space);
                }
            }
            return toolbar;
        }

        @Override public void setMultiViewCallback(MultiViewElementCallback callback) {}

        @Override public CloseOperationState canCloseElement() {
            return CloseOperationState.STATE_OK;
        }

        @Override public Action[] getActions() {
            return new Action[0];
        }

        @Override public Lookup getLookup() {
            return lookup;
        }

        @Override public void componentOpened() {}

        @Override public void componentClosed() {}

        @Messages("LBL_loading_Eff=Loading Effective POM...")
        @Override public void componentShowing() {
            panel.add(new JLabel(LBL_loading_Eff(), SwingConstants.CENTER), BorderLayout.CENTER);
            task.schedule(0);
        }

        @Override public void componentHidden() {}

        @Override public void componentActivated() {}

        @Override public void componentDeactivated() {}

        @Override public UndoRedo getUndoRedo() {
            return UndoRedo.NONE;
        }

        @Override public void run() {
            try {
                Result<MavenProject> result = lookup.lookupResult(MavenProject.class);
                Iterator<? extends MavenProject> it = result.allInstances().iterator();
                MavenProject mp = it.hasNext() ? it.next() : null;
                result.addLookupListener(new LookupListener1(task));
                if (mp == null) {
                    //still loading.
                    return;
                }
                assert mp != null;
                Model model = mp.getModel();
                LocationAwareMavenXpp3Writer writer = new LocationAwareMavenXpp3Writer();
                final StringWriter sw = new StringWriter();
                final List<Location> loc = writer.write(sw, model);
                    EventQueue.invokeLater(new Runnable() {
                        @Override public void run() {
                            Lookup mime = MimeLookup.getLookup("text/x-effective-pom+xml");
                            Collection<? extends SideBarFactory> bars = mime.lookupAll(SideBarFactory.class);
                            NbEditorKit kit = mime.lookup(NbEditorKit.class);
                            NbEditorDocument doc = (NbEditorDocument) kit.createDefaultDocument();
                            JEditorPane pane = new JEditorPane("text/x-effective-pom+xml", null);
                            pane.setDocument(doc);
                            panel.removeAll();
                            panel.add(doc.createEditor(pane), BorderLayout.CENTER);
                            pane.setEditable(false);
                            
                            try {
                                doc.insertString(0, sw.toString(), null);
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            pane.setCaretPosition(0);

                            panel.revalidate();
                            
                            AnnotationBarManager.showAnnotationBar(pane, loc);
                        }
                    });
            } catch (final Exception x) {
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        panel.removeAll();
                        panel.add(new JLabel(LBL_failed_to_load(x.getLocalizedMessage()), SwingConstants.CENTER), BorderLayout.CENTER);
                        panel.revalidate();
                        LOG.log(Level.FINE, "Exception thrown while loading effective POM", x);
                    }
                });
            }
        }

    }
    
    private static class LookupListener1 implements LookupListener {
        private final RequestProcessor.Task task;
        
        public LookupListener1(RequestProcessor.Task task) {
            this.task = task;
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            task.schedule(100);
        }
        
    }

}
