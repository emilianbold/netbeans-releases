/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.settings.storage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.WeakListeners;

/**
 * The tracker of mime types registered as folders under a common root. This class
 * will listen on a hierarchical structure of folders under a <code>baseFolder</code>
 * and will interpret its subfolders as mime type definitions. For example the
 * following structure of folders will be interpreted as two mime types 'text/x-java'
 * and 'application/pdf'.
 * 
 * <pre>
 *   &lt;baseFolder&gt;/text/x-java
 *   &lt;baseFolder&gt;/application/pdf
 * </pre>
 * 
 * @author Vita Stejskal
 */
public final class MimeTypesTracker {
        
    private static final Logger LOG = Logger.getLogger(MimeTypesTracker.class.getName());

    private static final Pattern REG_NAME_PATTERN = Pattern.compile("^[[\\p{Alnum}][!#$&.+\\-^_]]{1,127}$"); //NOI18N
    
    private static final Set<String> WELL_KNOWN_TYPES = new HashSet<String>(Arrays.asList(
        "application", //NOI18N
        "audio", //NOI18N
        "image", //NOI18N
        "message", //NOI18N
        "model", //NOI18N
        "multipart", //NOI18N
        "text", //NOI18N
        "video" //NOI18N
    ));
    
    private static final String ATTR_MIME_TYPE_FOLDER_MARKER = "org-netbeans-editor-MimeTypeDefinition"; //NOI18N

    /** The property for notifying changes in mime types tracked by this tracker. */
    public static final String PROP_MIME_TYPES = "mime-types"; //NOI18N

    /**
     * Create a new tracker for tracking mime types under the <code>basePath</code>
     * folder.
     * 
     * @param basePath The path on the system <code>FileSystem</code> where the
     *   mime types should be tracked.
     * @param settingsType The type of settings to track mime types for. If not
     *   <code>null</code> the tracker will only list mime types that declare
     *   settings of this type.
     */
    public MimeTypesTracker(String basePath, SettingsType settingsType) {
        this.basePath = basePath;
        this.basePathElements = basePath.split("/"); //NOI18N
        this.locator = settingsType == null ? null : settingsType.getLocator();
        
        rebuild();
        
        // Start listening
        this.listener = new Listener();
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        sfs.addFileChangeListener(WeakListeners.create(FileChangeListener.class,listener, sfs));
    }
    
    /**
     * Gets the root of the mime types hierarchy watched by this tracker.
     * 
     * @return The <code>basePath</code> passed to the constructor.
     */
    public String getBasePath() {
        return basePath;
    }
    
    /**
     * Gets the list of mime types (<code>String</code>s) located under this
     * tracker's <code>basePath</code>.
     * 
     * @return The list of mime types.
     */
    public Set<String> getMimeTypes() {
        synchronized (LOCK) {
            return mimeTypes.keySet();
        }
    }

    /**
     * Gets a display name for a mime type. The display name is read from the
     * localizing bundle associated to the mime type's folder (<code>FileObject</code>).
     * The value of the <code>mimeType</code> parameter will be used as bundle
     * key to read the display name.
     * 
     * @param mimeType The mime type to get the display name for.
     * @return The display (localized) name of the mime type or the <code>mimeType</code>
     *   if the display name can't be found.
     */
    public String getMimeTypeDisplayName(String mimeType) {
        String displayName = mimeTypes.get(mimeType);
        return displayName == null ? mimeType : displayName;
    }
    
    /**
     * Adds a listener that will be receiving <code>PROP_MIME_TYPES</code> notifcations.
     * 
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    /**
     * Removes a previously added listener.
     * 
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    // ------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------

    private final String LOCK = new String("MimeTypesTracker.LOCK"); //NOI18N
    
    private final String basePath;
    private final String [] basePathElements;
    private final SettingsType.Locator locator;
    
    private FileObject folder;
    private boolean isBaseFolder;
    
    private Map<String, String> mimeTypes = Collections.<String, String>emptyMap();

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final FileChangeListener listener;
    
    private void rebuild() {
        PropertyChangeEvent event = null;
        
        synchronized (LOCK) {
            Object [] ret = findTarget(basePathElements);
            FileObject f = (FileObject) ret[0];
            boolean isBase = ((Boolean) ret[1]).booleanValue();

            // The base folder or some folder up in the hierarchy has been created/deleted
            if (f != folder) {
                // Set the current folder and its is-target-flag
                folder = f;
                isBaseFolder = isBase;

                LOG.finest("folder = '" + folder.getPath() + "'"); //NOI18N
                LOG.finest("isBaseFolder = '" + isBaseFolder + "'"); //NOI18N
            }

            if (isBaseFolder) {
                // Clear the cache
                Map<String, String> newMimeTypes = new HashMap<String, String>();

                // Go through mime type types
                FileObject [] types = folder.getChildren();
                for(int i = 0; i < types.length; i++) {
                    if (!isValidType(types[i])) {
                        continue;
                    }

                    // Go through mime type subtypes
                    FileObject [] subTypes = types[i].getChildren();
                    for(int j = 0; j < subTypes.length; j++) {
                        if (!isValidSubtype(subTypes[j])) {
                            continue;
                        }

                        String mimeType = types[i].getNameExt() + "/" + subTypes[j].getNameExt(); //NOI18N

                        boolean add;
                        if (locator != null) {
                            Map<String, List<Object []>> scan = new HashMap<String, List<Object []>>();
                            locator.scan(folder, mimeType, null, false, true, true, scan);
                            add = !scan.isEmpty();
                        } else {
                            add = true;
                        }
                        
                        if (add) {
                            // First try the standard way for filesystem annotations
                            String displayName = Utils.getLocalizedName(subTypes[j], null);

                            // Then try the crap way introduced with Tools-Options
                            if (displayName == null) {
                                displayName = Utils.getLocalizedName(subTypes[j], mimeType, mimeType);
                            }
                            newMimeTypes.put(mimeType, displayName);
                        }
                    }
                }

                newMimeTypes = Collections.unmodifiableMap(newMimeTypes);
                if (!mimeTypes.equals(newMimeTypes)) {
                    event = new PropertyChangeEvent(this, PROP_MIME_TYPES, mimeTypes, newMimeTypes);
                    mimeTypes = newMimeTypes;
                }
            }
        }
        
        if (event != null) {
            pcs.firePropertyChange(event);
        }
    }

    private static boolean isValidType(FileObject typeFile) {
        if (!typeFile.isFolder()) {
            return false;
        }

        String typeName = typeFile.getNameExt();

        if (!isValidRegName(typeName)) {
            return false;
        }
        
        if (WELL_KNOWN_TYPES.contains(typeName)) {
            return true;
        }

        // XXX: undocumented backdoor
        Object marker = typeFile.getAttribute(ATTR_MIME_TYPE_FOLDER_MARKER);
        if ((marker instanceof Boolean) && ((Boolean) marker).booleanValue()) {
            return true;
        }
        
        return false;
    }

    private static boolean isValidSubtype(FileObject subtypeFile) {
        if (!subtypeFile.isFolder()) {
            return false;
        }

        String typeName = subtypeFile.getNameExt();
        return isValidRegName(typeName) && !typeName.equals("base"); //NOI18N
    }        
    
    private static boolean isValidRegName(String name) {
        Matcher m = REG_NAME_PATTERN.matcher(name);
        return m.matches();
    }

    private static Object [] findTarget(String [] path) {
        FileObject target = Repository.getDefault().getDefaultFileSystem().getRoot();
        boolean isTarget = 0 == path.length;
        
        for (int i = 0; i < path.length; i++) {
            FileObject f = target.getFileObject(path[i]);

            if (f == null || !f.isFolder() || !f.isValid() || f.isVirtual()) {
                break;
            } else {
                target = f;
                isTarget = i + 1 == path.length;
            }
        }
        
        return new Object [] { target, Boolean.valueOf(isTarget) };
    }

    private final class Listener extends FileChangeAdapter {
        
        public Listener() {
        }
        
        public void fileFolderCreated(FileEvent fe) {
            notifyRebuild(fe.getFile());
        }

        public void fileDeleted(FileEvent fe) {
            notifyRebuild(fe.getFile());
        }

        public void fileRenamed(FileRenameEvent fe) {
            notifyRebuild(fe.getFile());
        }
        
        private void notifyRebuild(FileObject f) {
            String path = f.getPath();
            if (path.startsWith(basePath)) {
                rebuild();
            }
        }
    } // End of Listener class
}
