/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.indent.spi;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
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
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Provides access to formatting settings for a document or file. The formatting
 * settings can either be stored globally in the IDE or they can be stored in a
 * project owning the document. The settings are provided in form of a
 * <code>java.util.prefs.Prefernces</code> instance.
 *
 * <p><b>Typical usecase</b>: This class is typically called from an implementation
 * of {@link IndentTask} or {@link ReformatTask}, which needs to know formatting
 * setting in order to do its job. The implementation is given a context object
 * with a <code>javax.swing.text.Document</code> instance where the formatting is
 * taking place. The implementation should call {@link #get(javax.swing.text.Document)}
 * and {@link #getPreferences()} in order to get <code>Preferences</code> with
 * formatting settings.
 * 
 * <p>The infrastructure will take care of providing the right <code>Preferences</code>
 * instance from either <code>MimeLookup</code> or a project depending on the formatted
 * document and user's choice. It is important <b>not</b> to cache the <code>Preferences</code>
 * instance, because a different instance may be provided in the future if a user
 * changes his mind in using global or per-project formatting settings.
 *
 * @author Vita Stejskal
 * @since 1.9
 */
public final class CodeStylePreferences {

    /**
     * Gets <code>CodeStylePreferences</code> for a document. This is the prefered
     * method to use. Whenever you have both <code>Document</code> and its
     * <code>FileObject</code> always use this method and provide the <code>Document</code>
     * instance.
     *
     * @param doc The document to get <code>CodeStylePreferences</code> for,
     *   can be <code>null</code>. If <code>null</code>, the method will return
     *   global preferences for 'all languages'.
     *   
     * @return The <code>CodeStylePreferences</code>, never <code>null</code>.
     */
    public static CodeStylePreferences get(Document doc) {
        return get(doc, doc != null ? (String) doc.getProperty("mimeType") : null); //NOI18N
    }

    /**
     * Gets <code>CodeStylePreferences</code> for a document and an embedding mimeType.
     * This is the prefered method to use. Whenever you have both <code>Document</code> and its
     * <code>FileObject</code> always use this method and provide the <code>Document</code>
     * instance.
     *
     * @param doc The document to get <code>CodeStylePreferences</code> for,
     *   can be <code>null</code>. If <code>null</code>, the method will return
     *   global preferences for 'all languages'.
     *
     * @return The <code>CodeStylePreferences</code>, never <code>null</code>.
     */
    public static CodeStylePreferences get(Document doc, String mimeType) {
        if (doc != null) {
            return getPreferences(doc, mimeType);
        } else {
            return getPreferences(null, null);
        }
    }

    /**
     * Gets <code>CodeStylePreferences</code> for a file. If you also have a
     * <code>Document</code> instance you should use the {@link #get(javax.swing.text.Document)}
     * method.
     *
     * @param file The file to get <code>CodeStylePreferences</code> for,
     *   can be <code>null</code>. If <code>null</code>, the method will return
     *   global preferences for 'all languages'.
     *   
     * @return The <code>CodeStylePreferences</code>, never <code>null</code>.
     */
    public static CodeStylePreferences get(FileObject file) {
        return get(file, file != null ? file.getMIMEType() : null);
    }
    
    /**
     * Gets <code>CodeStylePreferences</code> for a file. If you also have a
     * <code>Document</code> instance you should use the {@link #get(javax.swing.text.Document)}
     * method.
     *
     * @param file The file to get <code>CodeStylePreferences</code> for,
     *   can be <code>null</code>. If <code>null</code>, the method will return
     *   global preferences for 'all languages'.
     *
     * @return The <code>CodeStylePreferences</code>, never <code>null</code>.
     */
    public static CodeStylePreferences get(FileObject file, String mimeType) {
        if (file != null) {
            return getPreferences(file, mimeType); //NOI18N
        } else {
            return getPreferences(null, null);
        }
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
    // private implementation
    // ----------------------------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(CodeStylePreferences.class.getName());
    
    private static final String NODE_CODE_STYLE = "CodeStyle"; //NOI18N
    private static final String PROP_USED_PROFILE = "usedProfile"; // NOI18N
    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    private static final String PROJECT_PROFILE = "project"; // NOI18N

    private static final Map<Object, Map<String, CodeStylePreferences>> cache = new WeakHashMap<Object, Map<String, CodeStylePreferences>>();
    
    private final String mimeType;
    private final Reference<Document> refDoc;
    private final String filePath;
    
    private final Preferences projectRoot;
    private final Preferences globalPrefs;
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
    
    private static CodeStylePreferences getPreferences(final Object obj, final String mimeType) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<CodeStylePreferences>() {
            public CodeStylePreferences run() {
                synchronized (cache) {
                    Map<String, CodeStylePreferences> csps = cache.get(obj);
                    CodeStylePreferences csp = csps != null ? csps.get(mimeType) : null;
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
                                doc == null ? null : new CleaningWeakReference(doc),
                                file == null ? "no file" : file.getPath()); //NOI18N
                        if (csps == null) {
                            csps = new HashMap<String, CodeStylePreferences>();
                            cache.put(obj, csps);
                        }
                        csps.put(mimeType, csp);
                    }

                    return csp;
                }
            }
        });
    }

    private static final class CleaningWeakReference extends WeakReference<Document> implements Runnable {

        public CleaningWeakReference(Document referent) {
            super(referent, Utilities.activeReferenceQueue());
        }

        public void run() {
            synchronized (cache) {
                //expunge stale entries from the cache:
                cache.size();
            }
        }
        
    }

    private CodeStylePreferences(Preferences projectRoot, String mimeType, Reference<Document> refDoc, String filePath) {
        this.projectRoot = projectRoot;
        this.mimeType = mimeType;
        this.refDoc = refDoc;
        this.filePath = filePath; // just for logging

        this.globalPrefs = MimeLookup.getLookup(mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType)).lookup(Preferences.class);
        this.projectPrefs = null;
        this.useProject = false;

        ProjectManager.mutex().postReadRequest(new Runnable() {
            public void run() {
                synchronized (CodeStylePreferences.this) {
                    if (CodeStylePreferences.this.projectRoot != null) {
                        Preferences allLangCodeStyle = CodeStylePreferences.this.projectRoot.node(NODE_CODE_STYLE);
                        Preferences p = allLangCodeStyle.node(PROJECT_PROFILE);

                        // determine if we are using code style preferences from the project
                        String usedProfile = allLangCodeStyle.get(PROP_USED_PROFILE, DEFAULT_PROFILE);
                        useProject = PROJECT_PROFILE.equals(usedProfile);
                        projectPrefs = CodeStylePreferences.this.mimeType == null ?
                            p :
                            new ProxyPreferences(CodeStylePreferences.this.projectRoot.node(CodeStylePreferences.this.mimeType).node(NODE_CODE_STYLE).node(PROJECT_PROFILE), p);

                        // listen on changes
                        allLangCodeStyle.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, switchTrakcer, allLangCodeStyle));
                    } else {
                        useProject = false;
                        projectPrefs = null;
                    }
                }
            }
        });

        LOG.fine("file '" + filePath + "' (" + mimeType + ") is using " + (useProject ? "project" : "global") + " Preferences; doc=" + s2s(refDoc == null ? null : refDoc.get())); //NOI18N
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
        if (doc != null) {
            Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
            if (sdp instanceof DataObject) {
                return ((DataObject) sdp).getPrimaryFile();
            } else if (sdp instanceof FileObject) {
                return (FileObject) sdp;
            }
        }
        return null;
    }

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o));
    }
}
