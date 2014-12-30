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
package org.netbeans.modules.javascript.bower.file;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

// XXX could be api class
abstract class JsonFile {

    private static final Logger LOGGER = Logger.getLogger(BowerJson.class.getName());

    private static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory() {

        @Override
        public Map createObjectContainer() {
            return new LinkedHashMap();
        }

        @Override
        public List creatArrayContainer() {
            return new ArrayList();
        }

    };

    private final String fileName;
    private final FileObject directory;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final FileChangeListener directoryListener = new DirectoryListener();
    private final FileChangeListener jsonListener = new JsonListener();

    // @GuardedBy("this")
    private File json;
    // @GuardedBy("this")
    private Map<String, Object> content;
    private volatile boolean contentInited = false;


    JsonFile(String fileName, FileObject directory) {
        assert fileName != null;
        assert directory != null;
        assert directory.isFolder() : "Must be folder: " + directory;
        this.fileName = fileName;
        this.directory = directory;
        this.directory.addFileChangeListener(directoryListener);
    }

    abstract List<Pair<String, String[]>> watchedFields();

    public void cleanup() {
        contentInited = false;
        clear(true, false);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        initContent();
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public boolean exists() {
        return getJson().isFile();
    }

    public File getFile() {
        return getJson();
    }

    public String getPath() {
        return getJson().getAbsolutePath();
    }

    /**
     * Refreshes the file (when it was modified externally).
     */
    public void refresh() {
        FileUtil.toFileObject(getJson()).refresh();
    }

    /**
     * Returns <b>shallow</b> copy of the content.
     * <p>
     * <b>WARNING:</b> Do not modify the content directly!
     * @return <b>shallow</b> copy of the data
     */
    @CheckForNull
    public synchronized Map<String, Object> getContent() {
        initContent();
        if (content != null) {
            return new LinkedHashMap<>(content);
        }
        File file = getJson();
        if (!file.isFile()) {
            return null;
        }
        JSONParser parser = new JSONParser();
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(json), StandardCharsets.UTF_8))) {
            content = (Map<String, Object>) parser.parse(reader, CONTAINER_FACTORY);
        } catch (ParseException ex) {
            LOGGER.log(Level.INFO, file.getAbsolutePath(), ex);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, file.getAbsolutePath(), ex);
        }
        if (content == null) {
            return null;
        }
        return new LinkedHashMap<>(content);
    }

    @CheckForNull
    public <T> T getContentValue(Class<T> valueType, String... fieldHierarchy) {
        return getContentValue(getContent(), valueType, fieldHierarchy);
    }

    protected <T> T getContentValue(Map<String, Object> content, Class<T> valueType, String... fieldHierarchy) {
        Map<String, Object> subdata = content;
        if (subdata == null) {
            return null;
        }
        for (int i = 0; i < fieldHierarchy.length; ++i) {
            String field = fieldHierarchy[i];
            if (i == fieldHierarchy.length - 1) {
                Object value = subdata.get(field);
                if (value == null) {
                    return null;
                }
                if (valueType.isAssignableFrom(value.getClass())) {
                    return valueType.cast(value);
                }
                return null;
            }
            subdata = (Map<String, Object>) subdata.get(field);
            if (subdata == null) {
                return null;
            }
        }
        return null;
    }

    private void initContent() {
        if (contentInited) {
            return;
        }
        // read the file so we can listen on changes and fire proper events
        contentInited = true;
        getContent();
    }

    private synchronized File getJson() {
        if (json == null) {
            json = new File(FileUtil.toFile(directory), fileName);
            try {
                FileUtil.addFileChangeListener(jsonListener, json);
                LOGGER.log(Level.FINE, "Started listening to {0}", json);
            } catch (IllegalArgumentException ex) {
                // ignore, already listening
                LOGGER.log(Level.FINE, "Already listening to {0}", json);
            }
        }
        return json;
    }

    void clear(boolean newFile) {
        clear(newFile, true);
    }

    void clear(boolean newFile, boolean fireChanges) {
        Map<String, Object> oldContent;
        Map<String, Object> newContent = null;
        synchronized (this) {
            oldContent = content;
            if (content != null) {
                LOGGER.log(Level.FINE, "Clearing cached content of {0}", json);
                content = null;
            }
            if (newFile) {
                if (json != null) {
                    try {
                        FileUtil.removeFileChangeListener(jsonListener, json);
                        LOGGER.log(Level.FINE, "Stopped listenening to {0}", json);
                    } catch (IllegalArgumentException ex) {
                        // not listeneing yet, ignore
                        LOGGER.log(Level.FINE, "Not listenening yet to {0}", json);
                    }
                    LOGGER.log(Level.FINE, "Clearing cached json path {0}", json);
                }
                json = null;
            }
            if (fireChanges) {
                newContent = getContent();
            }
        }
        if (fireChanges) {
            fireChanges(oldContent, newContent);
        }
    }

    private void fireChanges(@NullAllowed Map<String, Object> oldContent, @NullAllowed Map<String, Object> newContent) {
        for (Pair<String, String[]> watchedField : watchedFields()) {
            String propertyName = watchedField.first();
            String[] field = watchedField.second();
            Object oldValue = getContentValue(oldContent, Object.class, field);
            Object newValue = getContentValue(newContent, Object.class, field);
            if (!Objects.equals(oldValue, newValue)) {
                propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
            }
        }
    }

    //~ Inner classes

    private final class DirectoryListener extends FileChangeAdapter {

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            clear(true);
        }

    }

    private final class JsonListener extends FileChangeAdapter {

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
