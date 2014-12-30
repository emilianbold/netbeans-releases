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

import java.awt.EventQueue;
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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
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

    /**
     * Set new value of the given field.
     * @param fieldHierarchy field (together with its hierarchy) to be changed
     * @param value new value of any type, e.g. new project name
     * @throws IOException if any error occurs
     */
    public synchronized void setContent(final List<String> fieldHierarchy, final Object value) throws IOException {
        assert fieldHierarchy != null;
        assert !fieldHierarchy.isEmpty();
        assert value != null;
        assert !EventQueue.isDispatchThread();
        assert exists();
        initContent();
        DataObject dataObject = DataObject.find(FileUtil.toFileObject(getJson()));
        EditorCookie editorCookie = dataObject.getLookup().lookup(EditorCookie.class);
        assert editorCookie != null : "No EditorCookie for " + dataObject;
        boolean modified = editorCookie.isModified();
        StyledDocument document = editorCookie.getDocument();
        if (document == null) {
            document = editorCookie.openDocument();
        }
        assert document != null;
        final StyledDocument documentRef = document;
        NbDocument.runAtomic(document, new Runnable() {
            @Override
            public void run() {
                setContent(documentRef, fieldHierarchy, value);
            }
        });
        if (!modified) {
            editorCookie.saveDocument();
        }
        clear(false);
    }

    void setContent(Document document, List<String> fieldHierarchy, Object value) {
        String text;
        try {
            text = document.getText(0, document.getLength());
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            assert false;
            return;
        }
        List<String> fields = new ArrayList<>(fieldHierarchy);
        int fieldIndex = -1;
        int closestFieldIndex = -1;
        int level = -1;
        int searchInLevel = 0;
        String field = null;
        for (int i = 0; i < text.length(); i++) {
            if (field == null) {
                field = "\"" + JSONValue.escape(fields.get(searchInLevel)) + "\""; // NOI18N
            }
            char ch = text.charAt(i);
            if (ch == '{') {
                level++;
                continue;
            } else if (ch == '}') {
                level--;
                continue;
            } else if (Character.isWhitespace(ch)) {
                continue;
            }
            if (level != searchInLevel) {
                continue;
            }
            if (ch == '"'
                    && text.substring(i).startsWith(field)) {
                // match
                closestFieldIndex = i;
                searchInLevel++;
                if (searchInLevel >= fields.size()) {
                    fieldIndex = i;
                    break;
                }
                i += field.length();
                field = null;
            }
        }
        assert field != null;
        if (fieldIndex == -1) {
            // remove found fields
            while (searchInLevel > 0) {
                fields.remove(0);
                searchInLevel--;
            }
            // insert missing fields
            insertNewField(document, fields, value, text, closestFieldIndex);
            return;
        }
        int colonIndex = -1;
        for (int i = fieldIndex + field.length(); i < text.length(); ++i) {
            char ch = text.charAt(i);
            switch (ch) {
                case ' ':
                    // noop
                    break;
                case ':':
                    colonIndex = i;
                    break;
                default:
                    // unexpected
                    return;
            }
            if (colonIndex != -1) {
                break;
            }
        }
        if (colonIndex == -1) {
            return;
        }
        int valueStartIndex = -1;
        for (int i = colonIndex + 1; i < text.length(); ++i) {
            char ch = text.charAt(i);
            if (!Character.isWhitespace(ch)) {
                valueStartIndex = i;
                break;
            }
        }
        if (valueStartIndex == -1) {
            return;
        }
        char valueFirstChar = text.charAt(valueStartIndex);
        int valueEndIndex = -1;
        for (int i = valueStartIndex + 1; i < text.length(); ++i) {
            char ch = text.charAt(i);
            if (valueFirstChar == '"') {
                if (ch == '"') {
                    valueEndIndex = i + 1;
                }
            } else if (Character.isDigit(ch)
                    || ch == '.') {
                // number
                continue;
            } else {
                valueEndIndex = i;
            }
            if (valueEndIndex != -1) {
                break;
            }
        }
        if (valueEndIndex == -1) {
            return;
        }
        insertValue(document, valueStartIndex, valueEndIndex, JSONValue.toJSONString(value), !(value instanceof String) && !(value instanceof Number));
    }

    private void insertNewField(Document document, List<String> fieldHierarchy, Object value, String text, int index) {
        int startIndex = index;
        boolean commaBefore;
        if (startIndex == -1) {
            startIndex = text.lastIndexOf('}'); // NOI18N
            if (startIndex != -1) {
                for (;;) {
                    char ch = text.charAt(--startIndex);
                    if (!Character.isWhitespace(ch)) {
                        startIndex++;
                        break;
                    }
                }
            }
            commaBefore = true;
        } else {
            startIndex = text.indexOf('{', startIndex); // NOI18N
            if (startIndex != -1) {
                startIndex++;
            }
            commaBefore = false;
        }
        if (startIndex == -1) {
            startIndex = text.length();
        }
        StringBuilder sb = new StringBuilder();
        if (commaBefore) {
            sb.append(','); // NOI18N
            sb.append('\n'); // NOI18N
        }
        int braces = -1;
        for (String field : fieldHierarchy) {
            if (braces > -1) {
                sb.append('{'); // NOI18N
                sb.append('\n'); // NOI18N
            }
            sb.append('"'); // NOI18N
            sb.append(JSONValue.escape(field));
            sb.append('"'); // NOI18N
            sb.append(':'); // NOI18N
            braces++;
        }
        sb.append(JSONValue.toJSONString(value));
        for (int i = 0; i < braces; i++) {
            sb.append('}'); // NOI18N
            sb.append('\n'); // NOI18N
        }
        if (!commaBefore) {
            sb.append(','); // NOI18N
        }
        insertValue(document, startIndex, -1, sb.toString(), true);
    }

    private void insertValue(Document document, int valueStartIndex, int valueEndIndex, String value, boolean format) {
        try {
            if (valueEndIndex != -1) {
                document.remove(valueStartIndex, valueEndIndex - valueStartIndex);
            }
            document.insertString(valueStartIndex, value, null);
            if (format) {
                reformat(document, valueStartIndex, valueStartIndex + value.length());
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            assert false;
        }
    }

    private void reformat(Document document, int startOffset, int endOffset) throws BadLocationException {
        Reformat reformat = Reformat.get(document);
        reformat.lock();
        try {
            reformat.reformat(startOffset, endOffset);
        } finally {
            reformat.unlock();
        }
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
