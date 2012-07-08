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
import java.awt.EventQueue;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import static org.netbeans.modules.maven.grammar.Bundle.*;
import org.netbeans.modules.maven.grammar.effpom.AnnotationBarManager;
import org.netbeans.modules.maven.grammar.effpom.LocationAwareMavenXpp3Writer;
import org.netbeans.modules.maven.grammar.effpom.LocationAwareMavenXpp3Writer.Location;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerFactory;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerPanelProvider;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Annotation;
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
import org.openide.util.lookup.Lookups;
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
    
    /**
     * placeholder to put into lookup meaning no lookup returned from base ArtifactViewerFactory
     */
    private static final MavenProject NULL = new MavenProject();
    
    @MultiViewElement.Registration(
        displayName="#TAB_Effective",
        iconBase=POMDataObject.POM_ICON,
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID="effectivePom",
        mimeType=Constants.POM_MIME_TYPE,
        position=101
    )
    @Messages("TAB_Effective=Effective")
    public static MultiViewElement forPOMEditor(final Lookup editor) {
        L look = new L(editor);
        return new EffPOMView(look);
    }    

    
     static class L extends ProxyLookup implements PropertyChangeListener {
            Project p;
            private Lookup editor;
            L(Lookup editor) {
                this.editor = editor;
                setLookups(editor);
                FileObject pom = editor.lookup(FileObject.class);
                if (pom != null) {
                    try {
                        p = ProjectManager.getDefault().findProject(pom.getParent());
                    } catch (IOException ex) {
                        LOG.log(Level.FINE, ex.getMessage(), ex);
                    } catch (IllegalArgumentException ex) {
                        LOG.log(Level.FINE, ex.getMessage(), ex);
                    }
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
                        setLookups(editor, Lookups.singleton(NULL));
                    }
                } else {
                    setLookups(editor, Lookups.singleton(NULL));
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
                assert p != null;
                ArtifactViewerFactory avf = Lookup.getDefault().lookup(ArtifactViewerFactory.class);
                if (avf != null) {
                    //a very weird pattern there, if project's artifact doesn't exist, we return null here..
                    // but later on null MavenProject lookup means Loading.. this we need another state for
                    // non existing MavenProject lookup result
                    Lookup l = avf.createLookup(p);
                    if (l != null) {
                        setLookups(l, editor);
                    } else {
                        LOG.log(Level.WARNING, "no artifact lookup for {0}", p);
                        setLookups(editor, Lookups.singleton(NULL));
                    }
                } else {
                    LOG.warning("no ArtifactViewerFactory found");
                    setLookups(editor, Lookups.singleton(NULL));
                }
            }
        }
    
    private static class EffPOMView implements MultiViewElement, Runnable {

        private final Lookup lookup;
        private final RequestProcessor.Task task = RP.create(this);
        private JToolBar toolbar;
        private JPanel panel;
        boolean firstTimeShown = true;
        private final Result<MavenProject> result;

        EffPOMView(Lookup lookup) {
            this.lookup = lookup;

            result = lookup.lookupResult(MavenProject.class);
            result.allInstances();
            result.addLookupListener(new LookupListener1(task));
            
        }

        @Override public JComponent getVisualRepresentation() {
            if (panel == null) {
                panel = new JPanel(new BorderLayout());
                if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                    panel.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
                }                
            }
            return panel;
        }

        @Override public JComponent getToolbarRepresentation() {
            if (toolbar == null) {
                toolbar = new JToolBar();
                toolbar.setFloatable(false);
                if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                    toolbar.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
                }                
                //TODO
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
            if (firstTimeShown) {
                firstTimeShown = false;
                panel.add(new JLabel(LBL_loading_Eff(), SwingConstants.CENTER), BorderLayout.CENTER);
                task.schedule(0);
            }
        }

        @Override public void componentHidden() {}

        @Override public void componentActivated() {}

        @Override public void componentDeactivated() {}

        @Override public UndoRedo getUndoRedo() {
            return UndoRedo.NONE;
        }

        @Override public void run() {
            try {
                Iterator<? extends MavenProject> it = result.allInstances().iterator();
                MavenProject mp = it.hasNext() ? it.next() : null;
                if (mp == null) {
                    //still loading.
                    return;
                }
                if (mp == NULL) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override public void run() {
                            panel.removeAll();
                            panel.add(new JLabel("No project associated with the project or loading failed. See Source tab for errors", SwingConstants.CENTER), BorderLayout.CENTER);
                            panel.revalidate();
                            LOG.log(Level.FINE, "No MavenProject in base ArtifactViewerFactory lookup. Unloadable project?");
                        }
                    });
                    return;
                }
                assert mp != null;
                Model model = mp.getModel();
                LocationAwareMavenXpp3Writer writer = new LocationAwareMavenXpp3Writer();
                final StringWriter sw = new StringWriter();
                final List<Location> loc = writer.write(sw, model);
                FileObject pom = lookup.lookup(FileObject.class);
                List<ModelProblem> problems = new ArrayList<ModelProblem>();
                final Map<ModelProblem, Location> prblmMap = new HashMap<ModelProblem, Location>();
                if (pom != null) {
                    File file = FileUtil.toFile(pom);
                    problems.addAll(runMavenValidationImpl(file));
                    for (Location lo : loc) {
                        Iterator<ModelProblem> it2 = problems.iterator();
                        while (it2.hasNext()) {
                            ModelProblem modelProblem = it2.next();
                            if (modelProblem.getLineNumber() == lo.loc.getLineNumber() && modelProblem.getModelId().equals(lo.loc.getSource().getModelId())) {
                                prblmMap.put(modelProblem, lo);
                                it2.remove();
                            }
                        }
                    }
                }
                
                
                    EventQueue.invokeLater(new Runnable() {
                        @Override public void run() {
                            Lookup mime = MimeLookup.getLookup("text/x-effective-pom+xml");
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
                            
                            for (final Map.Entry<ModelProblem, Location> ent : prblmMap.entrySet()) {
                                doc.addAnnotation(new Position() {

                                    @Override
                                    public int getOffset() {
                                        return ent.getValue().startOffset;
                                    }
                                }, 1, new Annotation() {

                                    @Override
                                    public String getAnnotationType() {
                                        if (ent.getKey().getSeverity() == ModelProblem.Severity.ERROR || ent.getKey().getSeverity() == ModelProblem.Severity.FATAL) {
                                            return "org-netbeans-spi-editor-hints-parser_annotation_err";
                                        } else {
                                            return "org-netbeans-spi-editor-hints-parser_annotation_warn";
                                        }
                                    }

                                    @Override
                                    public String getShortDescription() {
                                        return ent.getKey().getMessage();
                                    }
                                });
                            }

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
                        Exceptions.printStackTrace(x);
                        firstTimeShown = true;
                    }
                });
            }
        }

    }
    
    //copied from maven.hints Status Provider..
    static List<ModelProblem> runMavenValidationImpl(final File pom) {
        //TODO profiles based on current configuration??
        MavenEmbedder embedder = EmbedderFactory.getProjectEmbedder();
        MavenExecutionRequest meReq = embedder.createMavenExecutionRequest();
        ProjectBuildingRequest req = meReq.getProjectBuildingRequest();
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_1); // currently enables just <reporting> warning
        req.setLocalRepository(embedder.getLocalRepository());
        List<ArtifactRepository> remoteRepos = RepositoryPreferences.getInstance().remoteRepositories(embedder);
        req.setRemoteRepositories(remoteRepos);
        req.setRepositorySession(((DefaultMaven) embedder.lookupComponent(Maven.class)).newRepositorySession(meReq));
        List<ModelProblem> problems;
        try {
            problems = embedder.lookupComponent(ProjectBuilder.class).build(pom, req).getProblems();
        } catch (ProjectBuildingException x) {
            problems = new ArrayList<ModelProblem>();
            List<ProjectBuildingResult> results = x.getResults();
            if (results != null) { //one code point throwing ProjectBuildingException contains results,
                for (ProjectBuildingResult result : results) {
                    problems.addAll(result.getProblems());
                }
            } else {
                // another code point throwing ProjectBuildingException doesn't contain results..
                Throwable cause = x.getCause();
                if (cause instanceof ModelBuildingException) {
                    problems.addAll(((ModelBuildingException) cause).getProblems());
                }
            }
        }
        return problems;
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
