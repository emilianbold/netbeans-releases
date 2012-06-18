/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.grailsproject;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.groovy.editor.api.NbUtilities;
import org.netbeans.modules.groovy.grailsproject.actions.GotoControllerAction;
import org.netbeans.modules.groovy.grailsproject.actions.GotoDomainClassAction;
import org.netbeans.modules.groovy.grailsproject.actions.GotoViewAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author schmidtm
 */
public class NavigationSupport {

    private static final String GSP_MIME_TYPE = "text/x-gsp"; // NOI18N

    private static final Logger LOG = Logger.getLogger(NavigationSupport.class.getName());

    public NavigationSupport() {
        super();
    }

    public static boolean isActionEnabled(AbstractAction caller) {

        JTextComponent component = Utilities.getFocusedComponent();

        String fileName = getTargetFilename(caller, component);

        if (fileName != null && (new File(fileName)).canRead()) {
            return true;
        }

        return false;
    }

    private static DataObject getDataObjectFromComponent(JTextComponent sourceComponent) {
        if (sourceComponent == null) {
            LOG.log(Level.FINEST, "JTextComponent == null"); // NOI18N
            return null;
        }

        Document doc = sourceComponent.getDocument();

        if (doc == null) {
            LOG.log(Level.FINEST, "Document == null"); // NOI18N
            return null;
        }

        return NbEditorUtilities.getDataObject(doc);

    }

    private static GrailsProject getOwningProject(FileObject fo) {
        Project prj = FileOwnerQuery.getOwner(fo);

        if (prj instanceof GrailsProject) {
            return (GrailsProject) prj;
        }

        return null;
    }

    private static String getTargetFilename(AbstractAction caller, JTextComponent sourceComponent) {
        LOG.log(Level.FINEST, "openArtifact()"); // NOI18N


        /* 1. Are we are dealing with a Grails Project?
         * 2. Are we called up from a groovy ducument?
         * 3. Is the target where it should be?
         */

        DataObject dob = getDataObjectFromComponent(sourceComponent);

        if (dob == null) {
            LOG.log(Level.FINEST, "DataObject == null"); // NOI18N
            return null;
        }

        String sourceName = dob.getName();
        FileObject fo = dob.getPrimaryFile();

        if (fo == null) {
            LOG.log(Level.FINEST, "FileObject == null"); // NOI18N
            return null;
        }

        LOG.log(Level.FINEST, "Source Name : {0}", sourceName); // NOI18N

        GrailsProject prj = getOwningProject(fo);

        if (prj == null) {
            LOG.log(Level.FINEST, "Not a grails-project"); // NOI18N
            return null;
        }

        String mimetype = fo.getMIMEType();

        if (!(mimetype.equals(GroovyTokenId.GROOVY_MIME_TYPE) || mimetype.equals(GSP_MIME_TYPE))) {
            LOG.log(Level.FINEST, "Not a groovy mimetype : {0}", mimetype); // NOI18N
            return null;
        }

        ActionType target = actionToType(caller);

        // if we are dealing with a view (*.gsp), then the filename is the directory with first
        // character uppercase

        String targetName;

        if (mimetype.equals(GSP_MIME_TYPE)) {
            String parentName = fo.getParent().getName();
            targetName = parentName.substring(0, 1).toUpperCase() + parentName.substring(1);
        } else {
            if (ActionType.VIEW == target) {
                targetName = sourceName;
            } else {
                targetName = findPackagePath(fo) + File.separator + sourceName;
            }
        }

        String ret = getTargetPath(target, prj, targetName);
        FileObject artifactFile = FileUtil.toFileObject(FileUtil.normalizeFile(new File(ret)));
        // do not navigate to itself
        if (fo.equals(artifactFile)) {
            return null;
        }

        return ret;
    }

    static String findPackagePath(FileObject fo) {
        FileObject pkgFO = fo.getParent();
        String pkgName = pkgFO.getName();
        if (!"controllers".equals(pkgName) &&    //NOI18N
            !"domain".equals(pkgName) &&         //NOI18N
            !"views".equals(pkgName)) {          //NOI18N

            String parentPath = findPackagePath(pkgFO);
            if ("".equals(parentPath)) {
                return pkgName; // We don't want to add separator at the beginning
            } else {
                return parentPath + File.separator + pkgName;
            }
        } else {
            return ""; //NOI18N
        }
    }

    public static void openArtifact(AbstractAction caller, JTextComponent sourceComponent) {
        String fileName = getTargetFilename(caller, sourceComponent);

        FileObject artifactFile = FileUtil.toFileObject(FileUtil.normalizeFile(new File(fileName)));

        if (artifactFile != null && artifactFile.isValid()) {
            LOG.log(Level.FINEST, "Open File : {0}", FileUtil.getFileDisplayName(artifactFile)); // NOI18N
            NbUtilities.open(artifactFile, 1, "");
        } else {
            LOG.log(Level.FINEST, "File is either null or invalid : {0}", fileName); // NOI18N
        }

    }

    private static String getTargetPath(ActionType type, GrailsProject prj, String filename) {
        String GRAILS_APP_DIR = "grails-app"; // NOI18N

        String BASE_DIR = FileUtil.getFileDisplayName(prj.getProjectDirectory()) + File.separator + GRAILS_APP_DIR + File.separator;

        // this needs to be done if we are moving from controller to view or domain.
        if (filename.endsWith("Controller")) {
            filename = filename.replaceAll("Controller$", "");
        }

        switch (type) {
            case CONTROLLER:
                return BASE_DIR + "controllers" + File.separator + filename + "Controller.groovy"; //NOI18N
            case DOMAIN:
                return BASE_DIR + "domain" + File.separator + filename + ".groovy"; //NOI18N
            case VIEW:
                if (filename.length() > 1) {
                    char first = filename.charAt(0);
                    filename = Character.toLowerCase(first) + filename.substring(1);
                } else {
                    filename = filename.toLowerCase();
                }
                return BASE_DIR + "views" + File.separator + filename + File.separator + "show.gsp"; //NOI18N
        }

        return "";
    }

    private enum ActionType {
        DOMAIN, VIEW, CONTROLLER, NONE;
    }

    private static ActionType actionToType(AbstractAction caller) {
        if (caller instanceof GotoDomainClassAction) {
            return ActionType.DOMAIN;
        } else if (caller instanceof GotoViewAction) {
            return ActionType.VIEW;
        } else if (caller instanceof GotoControllerAction) {
            return ActionType.CONTROLLER;
        }
        return ActionType.NONE;
    }
}
