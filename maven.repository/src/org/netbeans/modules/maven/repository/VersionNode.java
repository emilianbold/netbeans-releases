/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.repository;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.maven.artifact.Artifact;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryUtil;
import org.netbeans.modules.maven.api.CommonArtifactActions;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.indexer.api.ui.ArtifactViewer;
import org.netbeans.modules.maven.repository.dependency.AddAsDependencyAction;
import org.openide.actions.CopyAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author mkleint
 * @author Anuradha
 */
public class VersionNode extends AbstractNode {
    private static final String JAVADOC_BADGE_ICON = "org/netbeans/modules/maven/repository/DependencyJavadocIncluded.png"; //NOI18N
    private static final String SOURCE_BADGE_ICON = "org/netbeans/modules/maven/repository/DependencySrcIncluded.png"; //NOI18N

    private NBVersionInfo record;
    private boolean hasJavadoc;
    private boolean hasSources;
    private RepositoryInfo info;

    private static String toolTipJavadoc = "<img src=\"" + VersionNode.class.getClassLoader().getResource(JAVADOC_BADGE_ICON) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(VersionNode.class, "ICON_JavadocBadge");//NOI18N
    private static String toolTipSource = "<img src=\"" + VersionNode.class.getClassLoader().getResource(SOURCE_BADGE_ICON) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(VersionNode.class, "ICON_SourceBadge");//NOI18N

    public static Children createChildren(RepositoryInfo info, NBVersionInfo record) {
        if (info.isLocal() && !"pom".equals(record.getType())) { //NOI18N
            try {
                Artifact art = RepositoryUtil.createArtifact(record);
                FileObject fo = FileUtil.toFileObject(FileUtilities.convertArtifactToLocalRepositoryFile(art));
                if (fo != null) {
                    DataObject dobj = DataObject.find(fo);
                    return new FilterNode.Children(dobj.getNodeDelegate().cloneNode());
                }
            } catch (DataObjectNotFoundException e) {
            }
        }
        return Children.LEAF;
    }

    /** Creates a new instance of VersionNode */
    public VersionNode(RepositoryInfo info, NBVersionInfo versionInfo, boolean javadoc, boolean source, boolean dispNameShort) {
        super(createChildren(info,versionInfo));
        this.info = info;
        hasJavadoc = javadoc;
        hasSources = source;
        this.record = versionInfo;
        if (dispNameShort) {
            setName(versionInfo.getVersion());
            setDisplayName(versionInfo.getVersion() + " [ " + (versionInfo.getType() != null ? versionInfo.getType() : "jar") //NOI18N
                    + (versionInfo.getClassifier() != null ? ("," + versionInfo.getClassifier()) : "") + " ]");//NOI18N
        } else {
            setName(versionInfo.getGroupId() + ":" + versionInfo.getArtifactId() + ":" + versionInfo.getVersion()); //NOI18N
        }
        setIconBaseWithExtension("org/netbeans/modules/maven/repository/DependencyJar.gif"); //NOI18N
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        Transferable deflt = super.clipboardCopy();
        ExTransferable enriched = ExTransferable.create(deflt);
        ExTransferable.Single ex = new ExTransferable.Single(DataFlavor.stringFlavor) {
            protected Object getData() {
                return "<dependency>\n" + //NOI18N
                        "  <groupId>" + record.getGroupId() + "</groupId>\n" + //NOI18N
                        "  <artifactId>" + record.getArtifactId() + "</artifactId>\n" + //NOI18N
                        "  <version>" + record.getVersion() + "</version>\n" + //NOI18N
                        "</dependency>"; //NOI18N
            }
        };
        enriched.put(ex);
        return enriched;
    }

    @Override
    public Action[] getActions(boolean context) {
        Artifact artifact = RepositoryUtil.createArtifact(record);
        Action[] retValue;
        if(info.isRemoteDownloadable()){
             retValue = new Action[]{
            new ShowArtifactAction(record),
            new AddAsDependencyAction(artifact),
            CommonArtifactActions.createFindUsages(artifact),
            null,
            CopyAction.get(CopyAction.class),
        };
        
        }else{
        

        retValue = new Action[]{
            new ShowArtifactAction(record),
            new AddAsDependencyAction(artifact),
            CommonArtifactActions.createFindUsages(artifact),
            CommonArtifactActions.createViewJavadocAction(artifact),
            null,
            CopyAction.get(CopyAction.class),
        };
        }
        return retValue;
    }

    @Override
    public Action getPreferredAction() {
        return new ShowArtifactAction(record);
    }

    @Override
    public Image getIcon(int param) {
        java.awt.Image retValue = super.getIcon(param);
        if (hasJavadoc) {
            Image ann = ImageUtilities.loadImage(JAVADOC_BADGE_ICON); //NOI18N
            ann = ImageUtilities.addToolTipToImage(ann, toolTipJavadoc);
            retValue = ImageUtilities.mergeImages(retValue, ann, 12, 0);//NOI18N
        }
        if (hasSources) {
            Image ann = ImageUtilities.loadImage(SOURCE_BADGE_ICON); //NOI18N
            ann = ImageUtilities.addToolTipToImage(ann, toolTipSource);
            retValue = ImageUtilities.mergeImages(retValue, ann, 12, 8);//NOI18N
        }
        return retValue;

    }

    @Override
    public Image getOpenedIcon(int param) {
        return getIcon(param);
    }

    @Override
    public String getShortDescription() {
        StringBuffer buffer = new StringBuffer();
        if (record != null) {
            buffer.append("<html>").append(NbBundle.getMessage(VersionNode.class, "TXT_GroupId")).append("<b>").append(record.getGroupId()).append("</b><p>");//NOI18N
            buffer.append(NbBundle.getMessage(VersionNode.class, "TXT_ArtifactId")).append("<b>").append(record.getArtifactId()).append("</b><p>");//NOI18N
            buffer.append(NbBundle.getMessage(VersionNode.class, "TXT_Version")).append("<b>").append(record.getVersion().toString()).append("</b><p>");//NOI18N
            buffer.append(NbBundle.getMessage(VersionNode.class, "TXT_Packaging")).append("<b>").append(record.getPackaging()).append("</b><p>");//NOI18N
            buffer.append(NbBundle.getMessage(VersionNode.class, "TXT_Name")).append(record.getProjectName()).append("<p>");//NOI18N
//            buffer.append(NbBundle.getMessage(VersionNode.class, "TXT_HasJavadoc")).append(hasJavadoc ? NbBundle.getMessage(VersionNode.class, "TXT_true") : NbBundle.getMessage(VersionNode.class, "TXT_false")).append("<p>");//NOI18N
//            buffer.append(NbBundle.getMessage(VersionNode.class, "TXT_HasSources")).append(hasSources ? NbBundle.getMessage(VersionNode.class, "TXT_true") : NbBundle.getMessage(VersionNode.class, "TXT_false"));//NOI18N
//            buffer.append("</html>");//NOI18N
        }
        return buffer.toString();
    }

    private static class ShowArtifactAction extends AbstractAction {
        private NBVersionInfo info;
        ShowArtifactAction(NBVersionInfo info) {
            this.info = info;
            putValue(NAME, NbBundle.getMessage(VersionNode.class, "ACT_View_Details"));
        }

        public void actionPerformed(ActionEvent e) {
            ArtifactViewer.showArtifactViewer(info);
        }
    }
}
