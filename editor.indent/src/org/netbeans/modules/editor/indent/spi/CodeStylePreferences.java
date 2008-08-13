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

package org.netbeans.modules.editor.indent.spi;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.indent.ProxyPreferences;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 * @since 1.9
 */
public final class CodeStylePreferences {

    public static CodeStylePreferences get(Document doc) {
        return get(doc, (String) doc.getProperty("mimeType")); //NOI18N
    }

    public static CodeStylePreferences get(FileObject file) {
        return get(file, file.getMIMEType()); //NOI18N
    }
    
    public Preferences getPreferences() {
        synchronized (this) {
            // This is here solely for the purpose of previewing changes in formatting settings
            // in Tools-Options. This is NOT, repeat NOT, to be used by anybody else!
            // The name of this property is also hardcoded in options.editor/.../IndentationPanel.java
            Document doc = refDoc == null ? null : refDoc.get();
            Object o = doc == null ? null : doc.getProperty("Tools-Options->Editor->Formatting->Preview - Preferences"); //NOI18N
            if (o instanceof Preferences) {
                return (Preferences) o;
            } else {
                Preferences prefs = useProject ? projectPrefs : globalPrefs;
                // to support tests that don't use editor.mimelookup.impl
                return prefs == null ? AbstractPreferences.systemRoot() : prefs;
            }
        }
    }
    
    // ----------------------------------------------------------------------
    // PreferenceChangeListener implementation
    // ----------------------------------------------------------------------

    public void run() {
        // this runs under ProjectManager.mutex().writeAccess, see #138528
        synchronized (this) {
            this.projectRoot = findProjectPreferences(doc);
            if (projectRoot != null) {
                // determine if we are using code style preferences from the project
                String usedProfile = projectRoot.get(PROP_USED_PROFILE, DEFAULT_PROFILE);
                this.useProject = PROJECT_PROFILE.equals(usedProfile);
                this.projectPrefs = mimeType == null ?
                    projectRoot.node(PROJECT_PROFILE) : projectRoot.node(PROJECT_PROFILE).node(mimeType);

                // listen on changes
                projectRoot.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, this.projectRoot));
            } else {
                useProject = false;
                projectPrefs = null;
            }
            
            logInfo();
        }
    }
    
    // ----------------------------------------------------------------------
    // private implementation
    // ----------------------------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(CodeStylePreferences.class.getName());
    
    private static final String NODE_CODE_STYLE = "CodeStyle"; //NOI18N
    private static final String PROP_USED_PROFILE = "usedProfile"; // NOI18N
    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    private static final String PROJECT_PROFILE = "project"; // NOI18N

    private static final Map<Object, CodeStylePreferences> cache = new WeakHashMap<Object, CodeStylePreferences>();
    
    private final String mimeType;
    private final Reference<Document> refDoc;
    private final String filePath;
    
    private final Document doc;
    private final Preferences globalPrefs;
    
    private Preferences projectRoot;
    private Preferences projectPrefs;
    private boolean useProject;

    private final PreferenceChangeListener switchTrakcer = new PreferenceChangeListener() {
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (evt.getKey() == null || PROP_USED_PROFILE.equals(evt.getKey())) {
                synchronized (this) {
                    useProject = PROJECT_PROFILE.equals(evt.getNewValue());
                    LOG.fine("file '" + filePath + "' (" + mimeType + ") is using " + (useProject ? "project" : "global") + " Preferences"); //NOI18N
                }
            }
        }
    };
    
    private static CodeStylePreferences get(Object obj, String mimeType) {
        synchronized (cache) {
            CodeStylePreferences csp = cache.get(obj);
            if (csp == null) {
                Document doc;
                FileObject file;

                if (obj instanceof FileObject) {
                    doc = null;
                    file = (FileObject) obj;
                } else {
                    doc = (Document) obj;
                    file = findFileObject(doc);
                }

                csp = new CodeStylePreferences(
                        findProjectPreferences(file), 
                        mimeType, 
                        doc == null ? null : new WeakReference<Document>(doc),
                        file == null ? "no file" : file.getPath()); //NOI18N
                cache.put(obj, csp);
            }

            return csp;
        }
    }

    private CodeStylePreferences(Preferences projectRoot, String mimeType, Reference<Document> refDoc, String filePath) {
        this.projectRoot = projectRoot;
        this.mimeType = mimeType;
        this.refDoc = refDoc;
        this.filePath = filePath; // just for logging
        
        if (projectRoot != null) {
            Preferences allLangCodeStyle = projectRoot.node(NODE_CODE_STYLE);
            Preferences p = allLangCodeStyle.node(PROJECT_PROFILE);

            // determine if we are using code style preferences from the project
            String usedProfile = allLangCodeStyle.get(PROP_USED_PROFILE, DEFAULT_PROFILE);
            this.useProject = PROJECT_PROFILE.equals(usedProfile);
            this.projectPrefs = mimeType == null ? 
                p :
                new ProxyPreferences(projectRoot.node(mimeType).node(NODE_CODE_STYLE).node(PROJECT_PROFILE), p);
            
            // listen on changes
            allLangCodeStyle.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, switchTrakcer, allLangCodeStyle));
        } else {
            useProject = false;
            projectPrefs = null;
        }
        
        this.globalPrefs = MimeLookup.getLookup(mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType)).lookup(Preferences.class);

        LOG.fine("file '" + filePath + "' (" + mimeType + ") is using " + (useProject ? "project" : "global") + " Preferences; doc=" + s2s(refDoc.get())); //NOI18N
    }
    
    private static final Preferences findProjectPreferences(FileObject file) {
        if (file != null) {
            Project p = FileOwnerQuery.getOwner(file);
            if (p != null) {
                return ProjectUtils.getPreferences(p, IndentUtils.class, true);
            }
        }
        return null;
    }

    private static final FileObject findFileObject(Document doc) {
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof DataObject) {
            return ((DataObject) sdp).getPrimaryFile();
        } else if (sdp instanceof FileObject) {
            return (FileObject) sdp;
        } else {
            return null;
        }
    }

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o));
    }
}
