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

package org.netbeans.modules.cnd.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * we use own manager to unify work with extensions and support default extension
 * + unfortunately implementation of FileUtil.getMIMETypeExtensions is extremely slow
 * @author Vladimir Voskresensky
 */
public final class MIMEExtensions {
    private final static Preferences preferences = NbPreferences.forModule(MIMEExtensions.class);
    private final static Manager manager = new Manager();
    private final ChangeSupport cs = new ChangeSupport(this);

    // access methods
    public static MIMEExtensions get(String mimeType) {
        return manager.get(mimeType);
    }

    public static List<MIMEExtensions> getCustomizable() {
        return manager.getOrderedExtensions();
    }

    public static boolean isCustomizableExtensions(String mimeType) {
        return get(mimeType) != null;
    }

    public static boolean isRegistered(String mimeType, String ext) {
        if (ext == null || ext.length() == 0) {
            return false;
        }
        // try cache
        MIMEExtensions out = get(mimeType);
        if (out == null) {
            return FileUtil.getMIMETypeExtensions(mimeType).contains(ext);
        } else {
            return out.contains(ext);
        }
    }
    /**
     * Add a listener to changes.
     * @param l a listener to add
     */
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    /**
     * Stop listening to changes.
     * @param l a listener to remove
     */
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    private final String mimeType;
    private final String description;
    private final Set<String> exts;

    private MIMEExtensions(String mimeType, String description) {
        this.mimeType = mimeType;
        this.description = description;
        exts = new TreeSet<String>();
        exts.addAll(FileUtil.getMIMETypeExtensions(mimeType));
    }

    private MIMEExtensions(String mimeType, MIMEExtensions primary) {
        // own
        this.mimeType = mimeType;
        // share
        this.description = primary.description;
        exts = primary.exts;
    }
    /**
     * assign extensions and default one to specified mime type
     * @param newExts extensions associated with mimeType
     * @param defaultExt default extension for mimeType
     * @throws IllegalArgumentException if input list doesn't contain default extension
     */
    public void setExtensions(List<String> newExts, String defaultExt) {
        if (!newExts.contains(defaultExt)) {
            throw new IllegalArgumentException("input list " + newExts + " doesn't contain default element:" + defaultExt); // NOI18N
        }
        Collection<String> old = getValues();
        List<String> toRemove = new ArrayList<String>(old);
        toRemove.removeAll(newExts);
        List<String> toAdd = new ArrayList<String>(newExts);
        toAdd.removeAll(old);
        // TODO: do we need isSystemCaseInsensitive() check?
        for (String ext : toRemove) {
            FileUtil.setMIMEType(ext, null);
        }
        for (String ext : toAdd) {
            FileUtil.setMIMEType(ext, mimeType);
        }
        if (!toRemove.isEmpty() || !toAdd.isEmpty()) {
            exts.clear();
            exts.addAll(newExts);
            cs.fireChange();
        }
        preferences.put(getMIMEType(), defaultExt);
    }

    public String getMIMEType() {
        return mimeType;
    }

    public String getDefaultExtension() {
        String defaultExt = preferences.get(getMIMEType(), "");
        if (defaultExt.length() == 0) {
            Collection<String> vals = getValues();
            return vals.isEmpty() ? "" : vals.iterator().next(); // NOI18N
        } else {
            return defaultExt;
        }
    }

    public String getLocalizedDescription() {
        return description;
    }

    public Collection<String> getValues() {
        return Collections.unmodifiableSet(exts);
    }

    private boolean contains(String ext) {
        return exts.contains(ext);
    }
    
    @Override
    public String toString() {
        return description + "[" + mimeType + ":" + getDefaultExtension() + "]"; // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MIMEExtensions other = (MIMEExtensions) obj;
        return this.mimeType.equals(other.mimeType);
    }

    @Override
    public int hashCode() {
        int hash = this.mimeType.hashCode();
        return hash;
    }

    public void addExtension(String ext) {
        if (!getValues().contains(ext)) {
            exts.add(ext);
            FileUtil.setMIMEType(ext, mimeType);
        }
    }

    public void setDefaultExtension(String defaultExt) {
        addExtension(defaultExt);
        preferences.put(getMIMEType(), defaultExt);
    }

    private static class Manager {
        private final Map<String, MIMEExtensions> mime2ext = new LinkedHashMap<String, MIMEExtensions>(5);
        private final FileObject configFolder;
        private final FileChangeListener listener;

        private Manager() {
            configFolder = FileUtil.getConfigFile("CND/Extensions"); // NOI18N
            if (configFolder != null) {
                listener = new L();
                configFolder.addFileChangeListener(FileUtil.weakFileChangeListener(listener, configFolder));
                initialize(configFolder);
            } else {
                listener = null;
            }
        }

        public MIMEExtensions get(String mimeType) {
            return mime2ext.get(mimeType);
        }

        public List<MIMEExtensions> getOrderedExtensions() {
            Map<String, MIMEExtensions> out = new LinkedHashMap<String, MIMEExtensions>(mime2ext);
            out.remove(MIMENames.SHELL_MIME_TYPE);
            out.remove(MIMENames.C_HEADER_MIME_TYPE);
            return new ArrayList<MIMEExtensions>(out.values());
        }
        
        private void initialize(FileObject configFolder) {
            mime2ext.clear();
            if (configFolder != null) {
                for (FileObject fo : FileUtil.getOrder(Arrays.asList(configFolder.getChildren()), false)) {
                    MIMEExtensions data = create(fo);
                    if (!mime2ext.containsKey(data.getMIMEType())) {
                        mime2ext.put(data.getMIMEType(), data);
                        if (MIMENames.HEADER_MIME_TYPE.equals(data.getMIMEType())) {
                            MIMEExtensions cHeader = new MIMEExtensions(MIMENames.C_HEADER_MIME_TYPE, data);
                            // check if newly created or already has custom value in prefs
                            String defExt = preferences.get(MIMENames.C_HEADER_MIME_TYPE, ""); // NOI18N
                            if (defExt.length() == 0) {
                                // for newly created use normal headers extension
                                cHeader.setDefaultExtension(data.getDefaultExtension());
                            }
                            mime2ext.put(MIMENames.C_HEADER_MIME_TYPE, cHeader);
                        }
                    }
                }
                // also cache shell files
                MIMEExtensions shell = new MIMEExtensions(MIMENames.SHELL_MIME_TYPE, ""); // NOI18N
                mime2ext.put(MIMENames.SHELL_MIME_TYPE, shell);
            }
        }

        private MIMEExtensions create(FileObject configFile) throws MissingResourceException {
            Object attr = configFile.getAttribute("mimeType"); // NOI18N
            if (!(attr instanceof String)) {
                throw new MissingResourceException(configFile.getPath(), configFile.getClass().getName(), "no stringvalue attribute \"mimeType\""); // NOI18N
            }
            String mimeType = (String) attr;
            attr = configFile.getAttribute("SystemFileSystem.localizingBundle"); // NOI18N
            if (!(attr instanceof String)) {
                throw new MissingResourceException(configFile.getPath(), configFile.getClass().getName(), "no stringvalue attribute \"SystemFileSystem.localizingBundle\""); // NOI18N
            }
            ResourceBundle rb = NbBundle.getBundle((String) attr);
            String localizedName = rb.getString(configFile.getPath());
            attr = configFile.getAttribute("default"); // NOI18N
            if (attr != null && !(attr instanceof String)) {
                throw new MissingResourceException(configFile.getPath(), configFile.getClass().getName(), "no stringvalue attribute \"default\""); // NOI18N
            }
            String defaultExt = (String) (attr == null ? "" : attr); // NOI18N
            MIMEExtensions out = new MIMEExtensions(mimeType, localizedName);
            // default extension could be in preferences
            defaultExt = preferences.get(mimeType, defaultExt);
            out.setDefaultExtension(defaultExt);
            return out;
        }
        // file change listener

        private final class L implements FileChangeListener {

            private L() {
            }

            public void fileFolderCreated(FileEvent fe) {
                initialize(configFolder);
            }

            public void fileDataCreated(FileEvent fe) {
                initialize(configFolder);
            }

            public void fileChanged(FileEvent fe) {
                initialize(configFolder);
            }

            public void fileDeleted(FileEvent fe) {
                initialize(configFolder);
            }

            public void fileRenamed(FileRenameEvent fe) {
                initialize(configFolder);
            }

            public void fileAttributeChanged(FileAttributeEvent fe) {
                initialize(configFolder);
            }
        }
    }
}
