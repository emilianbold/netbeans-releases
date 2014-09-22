/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.file;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 * Class representing project's <tt>package.json</tt> file.
 */
public final class PackageJson {

    private static final Logger LOGGER = Logger.getLogger(PackageJson.class.getName());

    public static final String PROP_NAME = "NAME"; // NOI18N
    public static final String PROP_SCRIPTS_START = "SCRIPTS_START"; // NOI18N

    static final String FILENAME = "package.json"; // NOI18N
    // file content
    static final String NAME = "name"; // NOI18N
    static final String SCRIPTS = "scripts"; // NOI18N
    static final String START = "start"; // NOI18N

    private final Project project;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final ChangeListener changeListener = new ChangeListenerImpl();
    private final FileChangeListener fileChangeListener = new FileChangeListenerImpl();

    // @GuardedBy("this")
    private boolean listening = false;
    // @GuardedBy("this")
    private File packageJson;
    // @GuardedBy("this")
    private JSONObject content;
    private volatile boolean contentInited = false;


    public PackageJson(Project project) {
        assert project != null;
        this.project = project;
    }

    public void cleanup() {
        clear(true);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        initContent();
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public boolean exists() {
        return getPackageJson().isFile();
    }

    public String getPath() {
        return getPackageJson().getAbsolutePath();
    }

    @CheckForNull
    public synchronized JSONObject getContent() {
        initContent();
        if (content != null) {
            return new JSONObject(content);
        }
        File file = getPackageJson();
        if (!file.isFile()) {
            return null;
        }
        JSONParser parser = new JSONParser();
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(packageJson), StandardCharsets.UTF_8))) {
            content = (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException ex) {
            LOGGER.log(Level.INFO, file.getAbsolutePath(), ex);
        }
        if (content == null) {
            return null;
        }
        return new JSONObject(content);
    }

    private void initContent() {
        if (contentInited) {
            return;
        }
        // read the file so we can listen on changes and fire proper events
        contentInited = true;
        getContent();
    }

    private synchronized File getPackageJson() {
        if (!listening) {
            listening = true;
            Sources sources = ProjectUtils.getSources(project);
            sources.addChangeListener(WeakListeners.change(changeListener, sources));
        }
        if (packageJson == null) {
            // first sources
            packageJson = findPackageJson(WebClientProjectConstants.SOURCES_TYPE_HTML5);
            if (packageJson == null) {
                // now site root
                packageJson = findPackageJson(WebClientProjectConstants.SOURCES_TYPE_HTML5_SITE_ROOT);
            }
            if (packageJson == null) {
                // now project dir
                FileObject projectDirectory = project.getProjectDirectory();
                File projDir = FileUtil.toFile(projectDirectory);
                assert projDir != null : projectDirectory;
                packageJson = new File(projDir, FILENAME);
            }
            packageJson = FileUtil.normalizeFile(packageJson);
            try {
                FileUtil.addFileChangeListener(fileChangeListener, packageJson);
                LOGGER.log(Level.FINE, "Started listenening to {0}", packageJson);
            } catch (IllegalArgumentException ex) {
                // ignore, already listening
                LOGGER.log(Level.FINE, "Already listenening to {0}", packageJson);
            }
        }
        return packageJson;
    }

    @CheckForNull
    private File findPackageJson(String sourceType) {
        for (SourceGroup sourceGroup : ProjectUtils.getSources(project).getSourceGroups(sourceType)) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            File root = FileUtil.toFile(rootFolder);
            assert root != null : rootFolder;
            return new File(root, FILENAME);
        }
        return null;
    }

    void clear(boolean newFile) {
        JSONObject oldContent;
        JSONObject newContent;
        synchronized (this) {
            oldContent = content;
            if (content != null) {
                LOGGER.log(Level.FINE, "Clearing cached content of {0}", packageJson);
                content = null;
            }
            if (newFile) {
                if (packageJson != null) {
                    try {
                        FileUtil.removeFileChangeListener(fileChangeListener, packageJson);
                        LOGGER.log(Level.FINE, "Stopped listenening to {0}", packageJson);
                    } catch (IllegalArgumentException ex) {
                        // not listeneing yet, ignore
                        LOGGER.log(Level.FINE, "Not listenening yet to {0}", packageJson);
                    }
                    LOGGER.log(Level.FINE, "Clearing cached package.json path {0}", packageJson);
                }
                packageJson = null;
            }
            newContent = getContent();
        }
        fireChanges(oldContent, newContent);
    }

    private void fireChanges(@NullAllowed JSONObject oldContent, @NullAllowed JSONObject newContent) {
        String oldName = getName(oldContent);
        String newName = getName(newContent);
        if (!Objects.equals(oldName, newName)) {
            propertyChangeSupport.firePropertyChange(PROP_NAME, oldName, newName);
        }
        String oldStartScript = getStartScript(oldContent);
        String newStartScript = getStartScript(newContent);
        if (!Objects.equals(oldStartScript, newStartScript)) {
            propertyChangeSupport.firePropertyChange(PROP_SCRIPTS_START, oldStartScript, newStartScript);
        }
    }

    @CheckForNull
    private String getName(@NullAllowed JSONObject data) {
        if (data == null) {
            return null;
        }
        return (String) data.get(NAME);
    }

    @CheckForNull
    private String getStartScript(@NullAllowed JSONObject data) {
        if (data == null) {
            return null;
        }
        JSONObject scripts = (JSONObject) data.get(SCRIPTS);
        if (scripts == null) {
            return null;
        }
        return (String) scripts.get(START);
    }

    //~ Inner classes

    private final class ChangeListenerImpl implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            clear(true);
        }

    }

    private final class FileChangeListenerImpl extends FileChangeAdapter {

        @Override
        public void fileDataCreated(FileEvent fe) {
            clear(false);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            clear(false);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            clear(false);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            clear(true);
        }

    }

}
