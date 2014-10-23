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
import java.util.LinkedList;
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
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;

/**
 * Class representing project's <tt>package.json</tt> file.
 */
public final class PackageJson {

    private static final Logger LOGGER = Logger.getLogger(PackageJson.class.getName());

    public static final String PROP_NAME = "NAME"; // NOI18N
    public static final String PROP_SCRIPTS_START = "SCRIPTS_START"; // NOI18N
    // file content
    public static final String FIELD_NAME = "name"; // NOI18N
    public static final String FIELD_SCRIPTS = "scripts"; // NOI18N
    public static final String FIELD_START = "start"; // NOI18N
    public static final String FIELD_ENGINES = "engines"; // NOI18N
    public static final String FIELD_NODE = "node"; // NOI18N

    static final String FILENAME = "package.json"; // NOI18N

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

    private final File directory;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final FileChangeListener fileChangeListener = new FileChangeListenerImpl();

    // @GuardedBy("this")
    private File packageJson;
    // @GuardedBy("this")
    private Map<String, Object> content;
    private volatile boolean contentInited = false;


    public PackageJson(File directory) {
        assert directory != null;
        assert directory.isDirectory() || !directory.exists() : "Must be directory or cannot exist: " + directory;
        this.directory = FileUtil.normalizeFile(directory);
    }

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
        return getPackageJson().isFile();
    }

    public File getFile() {
        return getPackageJson();
    }

    public String getPath() {
        return getPackageJson().getAbsolutePath();
    }

    /**
     * Returns <b>shallow</b> copy of the content.
     * <p>
     * <b>WARNING:</b> Do not modify the content directly, use {@link #setContent(String, String, String...)} instead!
     * @return <b>shallow</b> copy of the data
     * @see #setContent(String, String, String...)
     */
    @CheckForNull
    public synchronized Map<String, Object> getContent() {
        initContent();
        if (content != null) {
            return new LinkedHashMap<>(content);
        }
        File file = getPackageJson();
        if (!file.isFile()) {
            return null;
        }
        JSONParser parser = new JSONParser();
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(packageJson), StandardCharsets.UTF_8))) {
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

    /**
     * Set new value of the given field.
     * @param fieldHierarchy field (together with its hierarchy) to be changed, e.g. {@link #FIELD_NAME}
     *                       or list of {@link #FIELD_ENGINES}, {@link #FIELD_NODE}
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
        DataObject dataObject = DataObject.find(FileUtil.toFileObject(getPackageJson()));
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
            text = document.getText(0, document.getLength() - 1);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            assert false;
            return;
        }
        String field = null;
        int fieldIndex = -1;
        LinkedList<String> fields = new LinkedList<>(fieldHierarchy);
        while (!fields.isEmpty()) {
            String pureField = fields.pollFirst();
            field = "\"" + JSONValue.escape(pureField) + "\""; // NOI18N
            fieldIndex = text.indexOf(field, fieldIndex == -1 ? 0 : fieldIndex + field.length());
            if (fieldIndex == -1) {
                fields.addFirst(pureField);
                break;
            }
        }
        assert field != null;
        if (fieldIndex == -1) {
            insertNewField(document, fields, value, text, fieldIndex);
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
        if (startIndex == -1) {
            startIndex = text.lastIndexOf('}'); // NOI18N
        } else {
            startIndex = text.indexOf('}'); // NOI18N
        }
        if (startIndex == -1) {
            startIndex = text.length();
        }
        StringBuilder sb = new StringBuilder();
        if (startIndex > 1) {
            sb.append(','); // NOI18N
        }
        boolean first = true;
        for (String field : fieldHierarchy) {
            if (!first) {
                sb.append('{'); // NOI18N
            }
            sb.append('"'); // NOI18N
            sb.append(JSONValue.escape(field));
            sb.append('"'); // NOI18N
            sb.append(':'); // NOI18N
            first = false;
        }
        sb.append(JSONValue.toJSONString(value));
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

    private synchronized File getPackageJson() {
        if (packageJson == null) {
            packageJson = new File(directory, FILENAME);
            try {
                FileUtil.addFileChangeListener(fileChangeListener, packageJson);
                LOGGER.log(Level.FINE, "Started listening to {0}", packageJson);
            } catch (IllegalArgumentException ex) {
                // ignore, already listening
                LOGGER.log(Level.FINE, "Already listening to {0}", packageJson);
            }
        }
        return packageJson;
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
            if (fireChanges) {
                newContent = getContent();
            }
        }
        if (fireChanges) {
            fireChanges(oldContent, newContent);
        }
    }

    private void fireChanges(@NullAllowed Map<String, Object> oldContent, @NullAllowed Map<String, Object> newContent) {
        Object oldName = getName(oldContent);
        Object newName = getName(newContent);
        if (!Objects.equals(oldName, newName)) {
            propertyChangeSupport.firePropertyChange(PROP_NAME, oldName, newName);
        }
        Object oldStartScript = getStartScript(oldContent);
        Object newStartScript = getStartScript(newContent);
        if (!Objects.equals(oldStartScript, newStartScript)) {
            propertyChangeSupport.firePropertyChange(PROP_SCRIPTS_START, oldStartScript, newStartScript);
        }
    }

    @CheckForNull
    private Object getName(@NullAllowed Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        return data.get(FIELD_NAME);
    }

    @CheckForNull
    private Object getStartScript(@NullAllowed Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        Object scripts = data.get(FIELD_SCRIPTS);
        if (!(scripts instanceof Map)) {
            return null;
        }
        return ((Map<String, Object>) scripts).get(FIELD_START);
    }

    //~ Inner classes

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
