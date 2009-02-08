/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
public class CreateLibraryAction extends AbstractAction implements LookupListener {
    private Lookup lookup;
    private Lookup.Result<MavenProject> result;

    public CreateLibraryAction(Lookup lkp) {
        this.lookup = lkp;
        putValue(NAME, NbBundle.getMessage(CreateLibraryAction.class, "ACT_Library"));
        //TODO proper icon
        putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/maven/actions/libraries.gif", true))); //NOI18N
        putValue("iconBase", "org/netbeans/modules/maven/actions/libraries.gif"); //NOI18N
        result = lookup.lookupResult(MavenProject.class);
        setEnabled(result.allInstances().size() > 0);
        result.addLookupListener(this);

    }

    public void actionPerformed(ActionEvent e) {
        Iterator<? extends MavenProject> prj = result.allInstances().iterator();
        if (!prj.hasNext()) {
            return;
        }
        final MavenProject project = prj.next();
        final CreateLibraryPanel pnl = new CreateLibraryPanel(project);
        DialogDescriptor dd = new DialogDescriptor(pnl,  NbBundle.getMessage(CreateLibraryPanel.class, "LBL_CreateLibrary"));
        if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    createLibrary(pnl.getLibraryManager(), pnl.getLibraryName(), pnl.getIncludeArtifacts(), pnl.isAllSourceAndJavadoc(), project);
                }
            });
        }
    }

    public void resultChanged(LookupEvent ev) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setEnabled(result.allInstances().size() > 0);
            }
        });
    }

    private void createLibrary(LibraryManager libraryManager, String libraryName, List<Artifact> includeArtifacts, boolean allSourceAndJavadoc, MavenProject project) {
        ProgressHandle handle = ProgressHandleFactory.createHandle("Create Library");
        int count = includeArtifacts.size() * (allSourceAndJavadoc ? 3 : 1) + 5;
        handle.start(count);
        try {
            MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
            int index = 1;
            List<Artifact> failed = new ArrayList<Artifact>();
            List<URI> classpathVolume = new ArrayList<URI>();
            List<URI> javadocVolume = new ArrayList<URI>();
            List<URI> sourceVolume = new ArrayList<URI>();
            Map<String, List<URI>> volumes = new HashMap<String, List<URI>>();
            volumes.put("classpath", classpathVolume); //NOI18N
            if (allSourceAndJavadoc) {
                volumes.put("javadoc", javadocVolume); //NOI18N
                volumes.put("src", sourceVolume); //NOI18N
            }
            for (Artifact a : includeArtifacts) {
                handle.progress("Downloading " + a.getId(), index);
                try {
                    online.resolve(a, project.getRemoteArtifactRepositories(), online.getLocalRepository());
                    classpathVolume.add(a.getFile().toURI());
                    try {
                        if (allSourceAndJavadoc) {
                            handle.progress("Downloading javadoc " + a.getId(), index + 1);
                            Artifact javadoc = online.createArtifactWithClassifier(
                                    a.getGroupId(),
                                    a.getArtifactId(),
                                    a.getVersion(),
                                    a.getType(),
                                    "javadoc"); //NOI18N
                            online.resolve(javadoc, project.getRemoteArtifactRepositories(), online.getLocalRepository());
                            javadocVolume.add(javadoc.getFile().toURI());

                            handle.progress("Downloading sources " + a.getId(), index + 2);
                            Artifact sources = online.createArtifactWithClassifier(
                                    a.getGroupId(),
                                    a.getArtifactId(),
                                    a.getVersion(),
                                    a.getType(),
                                    "sources"); //NOI18N
                            online.resolve(sources, project.getRemoteArtifactRepositories(), online.getLocalRepository());
                            sourceVolume.add(sources.getFile().toURI());
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(CreateLibraryAction.class.getName()).log(Level.FINE, "Failed to download artifact", ex);
                    }

                } catch (Exception ex) {
                    failed.add(a);
                    Logger.getLogger(CreateLibraryAction.class.getName()).log(Level.FINE, "Failed to download artifact", ex);
                }
                index = index + (allSourceAndJavadoc ? 3 : 1);
            }
            try {
                libraryManager.createURILibrary("j2se", libraryName, volumes); //NOI18N
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } finally {
            handle.finish();
        }
    }

}
