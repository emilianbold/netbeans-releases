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
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.actions.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * @author Radek Matous
 */
public class CommandUtils {
    private static final String HTML_MIME_TYPE = "text/html"; // NOI18N

    private final PhpProject project;

    public CommandUtils(PhpProject project) {
        this.project = project;
    }

    /**
     * @return The file objects in the sources folder
     */
    public FileObject[] phpFilesForContext(Lookup context, boolean runAsScript) {
        FileObject dir = runAsScript ? ProjectPropertiesSupport.getSourcesDirectory(getProject()) :
            ProjectPropertiesSupport.getWebRootDirectory(getProject());
        return filter(filesForContext(context), dir);
    }

    public FileObject[] phpFilesForSelectedNodes(boolean runAsScript) {
        FileObject dir = runAsScript ? ProjectPropertiesSupport.getSourcesDirectory(getProject()) :
            ProjectPropertiesSupport.getWebRootDirectory(getProject());
        return filter(Arrays.asList(filesForSelectedNodes()), dir);
    }

    public Collection<? extends FileObject> filesForContext(Lookup context) {
        return context.lookupAll(FileObject.class);
    }

    public static FileObject[] filesForSelectedNodes() {
        Node[] nodes = getSelectedNodes();
        if (nodes == null) {
            return new FileObject[0];
        }
        List<FileObject> list = new ArrayList<FileObject>(nodes.length);
        for (Node node : nodes) {
            FileObject fileObject = node.getLookup().lookup(FileObject.class);

            if (fileObject == null) {
                fileObject = getFileObject(node);
            }

            if (fileObject != null) {
                list.add(fileObject);
            }
        }
        return list.toArray(new FileObject[list.size()]);
    }

    public static Node[] getSelectedNodes() {
        return TopComponent.getRegistry().getCurrentNodes();
    }

    public String getRelativeSrcPath(FileObject fileObject) {
        return getRelativePhpPath(ProjectPropertiesSupport.getSourcesDirectory(getProject()), fileObject);
    }

    public String getRelativeWebRootPath(FileObject fileObject) {
        return getRelativePhpPath(ProjectPropertiesSupport.getWebRootDirectory(getProject()), fileObject);
    }

    private String getRelativePhpPath(FileObject folder, FileObject fileObject) {
        if (fileObject != null) {
            if (FileUtil.isParentOf(folder, fileObject)) {
                return FileUtil.getRelativePath(folder, fileObject);
            } else if (folder.equals(fileObject)) {
                return ""; //NOI18N
            }
        }
        return null;
    }

    private static boolean isUnderSourceRoot(FileObject sourceRoot, FileObject file) {
        return FileUtil.isParentOf(sourceRoot, file) && FileUtil.toFile(file) != null;
    }

    public static boolean isPhpFile(FileObject file) {
        assert file != null;
        return PhpSourcePath.MIME_TYPE.equals(FileUtil.getMIMEType(file, PhpSourcePath.MIME_TYPE));
    }

    public static boolean isPhpOrHtmlFile(FileObject file) {
        assert file != null;
        String mimeType = FileUtil.getMIMEType(file, PhpSourcePath.MIME_TYPE, HTML_MIME_TYPE);
        return PhpSourcePath.MIME_TYPE.equals(mimeType) || HTML_MIME_TYPE.equals(mimeType);
    }

    private static FileObject[] filter(Collection<? extends FileObject> files, FileObject dir) {
        Collection<FileObject> retval = new LinkedHashSet<FileObject>();
        for (FileObject file : files) {
            if (!isUnderSourceRoot(dir, file)) {
                return null;
            }
            retval.add(file);
        }
        return (!retval.isEmpty()) ? retval.toArray(new FileObject[retval.size()]) : null;
    }

    private static FileObject getFileObject(Node node) {
        DataObject dobj = node.getLookup().lookup(DataObject.class);
        return (dobj != null) ? dobj.getPrimaryFile() : null;
    }

    private PhpProject getProject() {
        return project;
    }

    /** Return <code>true</code> if user wants to restart the current debug session. */
    public static boolean warnNoMoreDebugSession() {
        String message = NbBundle.getMessage(CommandUtils.class, "MSG_NoMoreDebugSession");
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(message, NotifyDescriptor.OK_CANCEL_OPTION);
        return DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.OK_OPTION;
    }
}
