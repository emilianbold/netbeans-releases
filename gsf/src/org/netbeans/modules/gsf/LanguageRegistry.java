/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.gsf;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.gsf.api.EmbeddingModel;
import org.netbeans.modules.gsf.api.GsfLanguage;
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.modules.gsf.api.annotations.NonNull;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.IncrementalEmbeddingModel;
import org.netbeans.modules.gsf.api.IncrementalParser;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.gsfret.source.usages.ClassIndexManager;
import org.netbeans.modules.gsfpath.spi.classpath.ClassPathFactory;
import org.netbeans.modules.gsfpath.spi.classpath.ClassPathImplementation;
import org.netbeans.modules.gsfpath.spi.classpath.PathResourceImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * Registry which locates and provides information about languages supported
 * by various plugins.
 *
 * @todo Check to see if a module has had its generator run
 * @todo Do cleanup of older files? Also make sure import wizard does the right thing
 *
 * @author Tor Norbye
 */
public final class LanguageRegistry implements Iterable<Language> {

    private static final Logger LOG = Logger.getLogger(LanguageRegistry.class.getName());
    private static LanguageRegistry instance;

    // Keep in sync with gsf.api/anttask/**/GsfJar.java!
    private static final String STRUCTURE = "structure.instance"; // NOI18N
    private static final String LANGUAGE = "language.instance"; // NOI18N

    private static final String ICON_BASE = "iconBase"; // NOI18N
    private static final String PARSER = "parser.instance"; // NOI18N
    private static final String COMPLETION = "completion.instance"; // NOI18N
    private static final String RENAMER = "renamer.instance"; // NOI18N
    private static final String FORMATTER = "formatter.instance"; // NOI18N
    private static final String BRACKET_COMPLETION = "bracket.instance"; // NOI18N
    private static final String DECLARATION_FINDER = "declarationfinder.instance"; // NOI18N
    private static final String INDEXER = "indexer.instance"; // NOI18N
    private static final String HINTS = "hints.instance"; // NOI18N
    private static final String SEMANTIC = "semantic.instance"; // NOI18N
    private static final String OCCURRENCES = "occurrences.instance"; // NOI18N
    private static final String INDEX_SEARCHER = "index_searcher.instance"; // NOI18N

    /** Location in the system file system where languages are registered */
    private static final String FOLDER = "GsfPlugins"; // NOI18N
    private boolean cacheDirty = true;
    private Map<String, Language> languagesCache;
    private FileChangeListener sfsTracker;
    private Collection<? extends EmbeddingModel> embeddingModels;
    /** Set of applicable langauges for each mimetype */
    private Map<String,List<Language>> applicableLanguages = new HashMap<String,List<Language>>();
    private Map<String,Boolean> possiblyIncremental = new HashMap<String,Boolean>();

    /**
     * Creates a new instance of LanguageRegistry
     */
    private LanguageRegistry() {
    }

    /** For testing only! */
    public void addLanguages(List<Language> newLanguages) {
        if (languagesCache != null) {
            throw new RuntimeException("This is for testing purposes only!!!"); //NOI18N
        }

        cacheDirty = false;
        languagesCache = new HashMap<String, Language>(2 * newLanguages.size());
        for (Language language : newLanguages) {
            String mimeType = language.getMimeType();
            languagesCache.put(mimeType, language);
        }
    }

    public static synchronized LanguageRegistry getInstance() {
        if (instance == null) {
            instance = new LanguageRegistry();
        }

        return instance;
    }

    /**
     * Return a language implementation that corresponds to the given mimeType,
     * or null if no such language is supported
     */
    public Language getLanguageByMimeType(@NonNull String mimeType) {
        final Map<String, Language> map = getLanguages();
        return map.get(mimeType);
    }

    @CheckForNull
    public EmbeddingModel getEmbedding(@NonNull String targetMimeType, @NonNull String sourceMimeType) {
        Collection<? extends EmbeddingModel> models = getEmbeddingModels();
        
        for (EmbeddingModel model : models) {
            if (model.getTargetMimeType().equals(targetMimeType) &&
                model.getSourceMimeTypes().contains(sourceMimeType)) {
                return model;
            }
        }

        return null;
    }

    private Collection<? extends EmbeddingModel> getEmbeddingModels() {
        if (embeddingModels == null) {
            embeddingModels = Lookup.getDefault().lookupAll(EmbeddingModel.class);
        }
        
        return embeddingModels;
    }
    
    private Map<String,Map<String,Boolean>> relevantMimes = new HashMap<String,Map<String,Boolean>>();

    /** Return true iff the given file object is relevant for the given mimeType.
     * This is true when the file is of a mime type that we're looking for, or if there
     * is an embedding model mapping available for the given file's mime type targeting
     * the requested mime type.
     */
    public boolean isRelevantFor(FileObject fo, String targetMimeType) {
        final String fileMimeType = fo.getMIMEType();
        if (targetMimeType.equals(fileMimeType)) {
            return true;
        }

        Map<String,Boolean> mimeMap = relevantMimes.get(targetMimeType);
        if (mimeMap == null) {
            mimeMap = new  HashMap<String,Boolean>();
            relevantMimes.put(targetMimeType, mimeMap);
        }

        Boolean result = mimeMap.get(fileMimeType);
        if (result == null) {
            // Check to see if the file is relevant
            result = Boolean.FALSE;

            Collection<? extends EmbeddingModel> models = getEmbeddingModels();
            for (EmbeddingModel model : models) {
                if (model.getTargetMimeType().equals(targetMimeType) && 
                    model.getSourceMimeTypes().contains(fileMimeType)) {
                    result = Boolean.TRUE;
                    break;
                }
            }

            mimeMap.put(fileMimeType, result);
        }
        
        return result.booleanValue();
    }

    @NonNull
    public List<Language> getApplicableLanguages(String mimeType) {
        List<Language> result = applicableLanguages.get(mimeType);
        if (result == null) {
            result = new ArrayList<Language>(5);

            // TODO - cache the answer since this is called a lot (for example during
            // task list scanning)
            Collection<? extends EmbeddingModel> models = getEmbeddingModels();

            final Language origLanguage = getLanguageByMimeType(mimeType);
            if (origLanguage != null) {
                result.add(origLanguage);
            }

            for (EmbeddingModel model : models) {
                if (model.getSourceMimeTypes().contains(mimeType)) {
                    Language language = getLanguageByMimeType(model.getTargetMimeType());
                    if (language != null && !result.contains(language)) {
                        result.add(language);
                    }
                }
            }

            applicableLanguages.put(mimeType, result);
        }
        
        return result;
    }

    /**
     * Return true iff the given mime has incremental support, either as
     * embedding models or parser
     */
    public boolean isIncremental(String mimeType) {
        Boolean b = possiblyIncremental.get(mimeType);
        if (b == null) {
            List<Language> applicable = getApplicableLanguages(mimeType);

            boolean incremental = false;
            for (Language language : applicable) {
                Parser parser = language.getParser(); // Todo - call createParserTask here?
                if (parser instanceof IncrementalParser) {
                    incremental = true;
                }
                if (!language.getMimeType().equals(mimeType)) {
                    EmbeddingModel model = getEmbedding(language.getMimeType(), mimeType);
                    if (model instanceof IncrementalEmbeddingModel) {
                        incremental = true;
                        break;
                    }
                }
            }

            b = incremental ? Boolean.TRUE : Boolean.FALSE;
            possiblyIncremental.put(mimeType, b);
        }

        return b == Boolean.TRUE;
    }
    
    private ClassPath libraryPath;
    private List<URL> urls;

    
    public List<URL> getLibraryUrls() {
        if (urls == null) {
            urls = new ArrayList<URL>();
            for (Language language : this) {
                GsfLanguage gsfLanguage = language.getGsfLanguage();
                if (gsfLanguage != null) {
                    for (FileObject fo : gsfLanguage.getCoreLibraries()) {
                        try {
                            if (fo == null) {
                                continue;
                            }
                            URL url = FileUtil.toFile(fo).toURI().toURL();
                            urls.add(url);
                            ClassIndexManager.get(language).addBootRoot(url);
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
        
        return urls;
    }

    public List<FileObject> getLibraryFos() {
        List<FileObject> fos = new ArrayList<FileObject>();
        for (Language language : this) {
            GsfLanguage gsfLanguage = language.getGsfLanguage();
            if (gsfLanguage != null) {
                for (FileObject fo : gsfLanguage.getCoreLibraries()) {
                    fos.add(fo);
                }
            }
        }
        
        return fos;
    }
    
    public ClassPath getLibraryPaths() {
        if (libraryPath == null) {
            List<URL> urlList = getLibraryUrls();
            final URL[] urlArray = urlList.toArray(new URL[urlList.size()]);
            
            libraryPath = ClassPathFactory.createClassPath(new ClassPathImplementation() {

                public List<? extends PathResourceImplementation> getResources() {
                    return Collections.<PathResourceImplementation>singletonList(new PathResourceImplementation() {
                        public URL[] getRoots() {
                            return urlArray;
                        }

                        public ClassPathImplementation getContent() {
                            return null;
                        }

                        public void addPropertyChangeListener(PropertyChangeListener listener) {
                        }

                        public void removePropertyChangeListener(PropertyChangeListener listener) {
                        }
                    });
                }

                public void addPropertyChangeListener(PropertyChangeListener listener) {
                }

                public void removePropertyChangeListener(PropertyChangeListener listener) {
                }
                
            });
        }
        
        return libraryPath;
    }
    
    private void addLanguages(List<Language> result, TokenSequence ts, int offset) {
        ts.move(offset);
        if (ts.moveNext() || ts.movePrevious()) {
            TokenSequence ets = ts.embedded();
            if (ets != null) {
                addLanguages(result, ets, offset); // Recurse
            }
            String mimeType = ts.language().mimeType();
            Language language = getLanguageByMimeType(mimeType);

            if (language != null) {
                result.add(language);
            }
        }
    }
    
    public List<Language> getEmbeddedLanguages(BaseDocument doc, int offset) {
        List<Language> result = new ArrayList<Language>();

        doc.readLock(); // Read-lock due to Token hierarchy use
        try {
            // TODO - I should only do this for languages which CAN have it
            /*
     at org.netbeans.lib.lexer.inc.IncTokenList.reinit(IncTokenList.java:113)
        at org.netbeans.lib.lexer.TokenHierarchyOperation.setActiveImpl(TokenHierarchyOperation.java:257)
        at org.netbeans.lib.lexer.TokenHierarchyOperation.isActiveImpl(TokenHierarchyOperation.java:308)
        at org.netbeans.lib.lexer.TokenHierarchyOperation.tokenSequence(TokenHierarchyOperation.java:344)
        at org.netbeans.lib.lexer.TokenHierarchyOperation.tokenSequence(TokenHierarchyOperation.java:338)
        at org.netbeans.api.lexer.TokenHierarchy.tokenSequence(TokenHierarchy.java:183)
        at org.netbeans.modules.gsf.LanguageRegistry.getEmbeddedLanguages(LanguageRegistry.java:336)
        at org.netbeans.modules.gsfret.hints.infrastructure.SuggestionsTask.getHintsProviderLanguage(SuggestionsTask.java:70)
        at org.netbeans.modules.gsfret.hints.infrastructure.SuggestionsTask.run(SuggestionsTask.java:94)
        at org.netbeans.modules.gsfret.hints.infrastructure.SuggestionsTask.run(SuggestionsTask.java:63)
[catch] at org.netbeans.napi.gsfret.source.Source$CompilationJob.run(Source.java:1272)             * 
             */
            TokenSequence ts = TokenHierarchy.get(doc).tokenSequence();
            if (ts != null) {
                addLanguages(result, ts, offset);
            }
        } finally {
            doc.readUnlock();
        }

        String mimeType = (String) doc.getProperty("mimeType"); // NOI18N
        if (mimeType != null) {
            Language language = getLanguageByMimeType(mimeType);
            if (language != null && (result.size() == 0 || result.get(result.size()-1) != language))  {
                result.add(language);
            }
        }
        
        return result;
    }

    /**
     * Return true iff the given mimeType is supported by a registered language plugin
     * @return True iff the given mimeType is supported
     */
    public boolean isSupported(@NonNull String mimeType) {
        if (mimeType == null) {
            return false;
        }
        
        return getLanguageByMimeType(mimeType) != null;
    }
    
    //private void listCustomEditorKits() {
    //    for (Language language : this) {
    //        if (language.useCustomEditorKit()) {
    //            System.out.println(language.getDisplayName());
    //        }
    //    }
    //}
    
    public String getLanguagesDisplayName() {
        StringBuilder sb = new StringBuilder();
        for (Language language : this) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(language.getDisplayName());
        }
        
        return sb.toString();
    }

    public Iterator<Language> iterator() {
        Map<String, Language> map = getLanguages();
        return map.values().iterator();
    }

    private synchronized Map<String, Language> getLanguages() {
        if (cacheDirty) {
            cacheDirty = false;
            FileSystem sfs;
            try {
                sfs = FileUtil.getConfigRoot().getFileSystem();
            } catch (FileStateInvalidException ex) {
                // ignore
                sfs = null;
            }
            languagesCache = readSfs(sfs, languagesCache);
            
            if (sfsTracker == null) {
                // First time we run do the cleanup
                userdirCleanup();

                // start listening on SystemFileSystem
                sfsTracker = new FsTracker(sfs);
            } else {
                GsfDataLoader.getLoader(GsfDataLoader.class).initExtensions();
            }
        }
        return languagesCache;
    }
    
    private static boolean isValidType(FileObject typeFile) {
        if (!typeFile.isFolder()) {
            return false;
        }

        String typeName = typeFile.getNameExt();
        return MimePath.validate(typeName, null);
    }

    private static boolean isValidSubtype(FileObject subtypeFile) {
        if (!subtypeFile.isFolder()) {
            return false;
        }

        String typeName = subtypeFile.getNameExt();
        return MimePath.validate(null, typeName) && !typeName.equals("base"); //NOI18N
    }
    
    private static Map<String, Language> readSfs(FileSystem sfs, Map<String, Language> existingMap) {
        FileObject registryFolder = sfs == null ? null : sfs.findResource(FOLDER);

        if (registryFolder == null) {
            LOG.info("No " + FOLDER + " folder"); //NOI18N
            return Collections.<String, Language>emptyMap();
        }

        // Read languages
        LOG.fine("Reading " + FOLDER + " registry..."); //NOI18N
        Map<String, Language> newMap = new HashMap<String, Language>();

        // Go through mimetype types
        FileObject[] types = registryFolder.getChildren();
        for (int i = 0; i < types.length; i++) {
            if (!isValidType(types[i])) {
                continue;
            }

            // Go through mimetype subtypes
            FileObject[] subtypes = types[i].getChildren();
            for (int j = 0; j < subtypes.length; j++) {
                if (!isValidSubtype(subtypes[j])) {
                    continue;
                }

                String mimeType = types[i].getNameExt() + "/" + subtypes[j].getNameExt(); // NOI18N
                Language existingLanguage = existingMap != null ? existingMap.get(mimeType) : null;
                if (existingLanguage != null) {
                    LOG.fine("Reusing existing Language for '" + mimeType + "': " + existingLanguage); //NOI18N
                    newMap.put(mimeType, existingLanguage);
                    continue;
                }

                Integer attr = (Integer) subtypes[j].getAttribute("genver"); //NOI18N
                if (attr == null) {
                    LOG.log(Level.SEVERE, "Language " + mimeType + " has not been preprocessed during jar module creation"); //NOI18N
                }

                Language language = new Language(mimeType);
                newMap.put(mimeType, language);
                LOG.fine("Adding new Language for '" + mimeType + "': " + language); //NOI18N

                Boolean useCustomEditorKit = (Boolean)subtypes[j].getAttribute("useCustomEditorKit"); // NOI18N
                if (useCustomEditorKit != null && useCustomEditorKit.booleanValue()) {
                    language.setUseCustomEditorKit(true);
                    LOG.fine("Language for '" + mimeType + "' is using custom editor kit."); //NOI18N
                }

                // Try to obtain icon from (new) IDE location for icons per mime type:
                FileObject loaderMimeFile = FileUtil.getConfigFile("Loaders/" + mimeType); // NOI18N
                if (loaderMimeFile != null) {
                    String iconBase = (String)loaderMimeFile.getAttribute(ICON_BASE);

                    if ((iconBase != null) && (iconBase.length() > 0)) {
                        language.setIconBase(iconBase);
                    }
                }

                boolean foundConfig = false;
                for (FileObject fo : subtypes[j].getChildren()) {
                    String name = fo.getNameExt();
                    LOG.fine("Language for '" + mimeType + "' registers: " + name); //NOI18N

                    if (LANGUAGE.equals(name)) {
                        foundConfig = true;
                        language.setGsfLanguageFile(fo);
                    } else if (HINTS.equals(name)) {
                        language.setHintsProviderFile(fo);
                    } else if (STRUCTURE.equals(name)) {
                        language.setStructureFile(fo);
                    } else if (PARSER.equals(name)) {
                        language.setParserFile(fo);
                    } else if (COMPLETION.equals(name)) {
                        language.setCompletionProviderFile(fo);
                    } else if (RENAMER.equals(name)) {
                        language.setInstantRenamerFile(fo);
                    } else if (FORMATTER.equals(name)) {
                        language.setFormatterFile(fo);
                    } else if (DECLARATION_FINDER.equals(name)) {
                        language.setDeclarationFinderFile(fo);
                    } else if (BRACKET_COMPLETION.equals(name)) {
                        language.setBracketCompletionFile(fo);
                    } else if (INDEXER.equals(name)) {
                        language.setIndexerFile(fo);
                    //} else if (PALETTE.equals(name)) {
                    //    language.setPaletteFile(fo);
                    } else if (SEMANTIC.equals(name)) {
                        language.setSemanticAnalyzer(fo);
                    } else if (OCCURRENCES.equals(name)) {
                        language.setOccurrencesFinderFile(fo);
                    } else if (INDEX_SEARCHER.equals(name)) {
                        language.setIndexSearcher(fo);
                    }
                }

                if (!foundConfig && warnedPaths.add(subtypes[j].getPath())) {
                    LOG.log(Level.WARNING, "No GsfLanguage instance registered in {0}", subtypes[j].getPath());
                }
            }
        }

        LOG.fine("-- Finished reading " + FOLDER + " registry!"); //NOI18N
        return newMap;
    }

    private static final Set<String> warnedPaths = Collections.synchronizedSet(new HashSet<String>());

    private void userdirCleanup() {
        // Don't do this check in release builds, it's for dev builds only.
        // We don't migrate userdir settings related to these services from
        // dev build to dev build.
        boolean assertionsEnabled = false;
        assert assertionsEnabled = true;
        if (!assertionsEnabled) {
            return;
        }

        String userDir = System.getProperty("netbeans.user"); // NOI18N
        if (userDir == null) {
            return;
        }


        FileObject config = FileUtil.toFileObject(new File(userDir, "config")); // NOI18N

        if (config == null) {
            return;
        }

        FileObject navFo = config.getFileObject("Navigator/Panels/text/javascript/org-netbeans-modules-gsfret-navigation-ClassMemberPanel.instance"); // NOI18N
        if (navFo == null) {
            // We've already done the cleanup.
            // (text/javascript is in all configurations of the IDE, so if
            // the text/javascript folder is gone, so are all the others
            return;
        }

        try {
            FileObject navigator = config.getFileObject("Navigator"); // NOI18N
            if (navigator != null) {
                FileObject panels = navigator.getFileObject("Panels"); // NOI18N
                if (panels != null) {
                    for (FileObject outerMime : panels.getChildren()) {
                        for (FileObject innerMime : outerMime.getChildren()) {
                            FileObject panel = innerMime.getFileObject("org-netbeans-modules-gsfret-navigation-ClassMemberPanel.instance"); // NOI18N
                            if (panel != null) {
                                panel.delete();
                                if (innerMime.getChildren().length == 0) {
                                    innerMime.delete();
                                }
                            }
                            if (outerMime.getChildren().length == 0) {
                                outerMime.delete();
                            }
                        }
                    }
                    if (panels.getChildren().length == 0) {
                        panels.delete();
                        if (navigator.getChildren().length == 0) {
                            navigator.delete();
                        }
                    }
                }
            }


            // Delete editors stuff
            FileObject editors = config.getFileObject("Editors"); // NOI18N
            if (editors != null) {
                for (FileObject outerMime : editors.getChildren()) {
                    for (FileObject innerMime : outerMime.getChildren()) {
                        String mimeType = outerMime.getName() + "/" + innerMime.getName();

                        FileObject root = innerMime;

                        // Clean up the settings files
                        FileObject settings = root.getFileObject("Settings.settings"); // NOI18N
                        if (settings != null) {
                            settings.delete();
                        }

                        // init code folding bar
                        FileObject fo = root.getFileObject("SideBar/org-netbeans-modules-editor-gsfret-GsfCodeFoldingSideBarFactory.instance");
                        if (fo != null) {
                            fo.delete();
                        }
                        fo = root.getFileObject("SideBar");
                        if (fo != null && fo.getChildren().length == 0) {
                            fo.delete();
                        }
                        fo = root.getFileObject("FoldManager/org-netbeans-modules-gsfret-editor-fold-GsfFoldManagerFactory.instance");
                        if (fo != null) {
                            fo.delete();
                        }
                        fo = root.getFileObject("FoldManager");
                        if (fo != null && fo.getChildren().length == 0) {
                            fo.delete();
                        }

                        // YAML cleanup: Was a Schliemann editor in 6.0/6.1/6.5dev so may have to delete its persistent system files
                        if (mimeType.equals("text/x-yaml") || mimeType.equals("text/x-json")) { // NOI18N
                            FileObject f = root.getFileObject("Popup/generate-fold-popup"); // NOI18N
                            if (f != null) {
                                f.delete();
                                f = root.getFileObject("ToolTips/org-netbeans-modules-languages-features-ToolTipAnnotation.instance"); // NOI18N
                                if (f != null) {
                                    f.delete();
                                }
                                f = root.getFileObject("Popup/org-netbeans-modules-languages-features-GoToDeclarationAction.instance"); // NOI18N
                                if (f != null) {
                                    f.delete();
                                }
                                f = root.getFileObject("UpToDateStatusProvider/org-netbeans-modules-languages-features-UpToDateStatusProviderFactoryImpl.instance"); // NOI18N
                                if (f != null) {
                                    f.delete();
                                }
                                f = root.getFileObject("run_script.instance"); // NOI18N
                                if (f != null) {
                                    f.delete();
                                }
                            }
                        }

                        // Delete old names present up to and including beta2
                        FileObject oldSidebar = root.getFileObject("SideBar/org-netbeans-modules-editor-retouche-GsfCodeFoldingSideBarFactory.instance");

                        if (oldSidebar != null) {
                            oldSidebar.delete();
                            oldSidebar = root.getFileObject("FoldManager/org-netbeans-modules-retouche-editor-fold-GsfFoldManagerFactory.instance");
                            if (oldSidebar != null) {
                                oldSidebar.delete();
                            }
                        }

                        // init hyperlink provider
                        FileObject hyperlinkProvider = root.getFileObject("HyperlinkProviders/GsfHyperlinkProvider.instance");
                        if (hyperlinkProvider != null) {
                            hyperlinkProvider.delete();
                        }
                        fo = root.getFileObject("HyperlinkProviders");
                        if (fo != null && fo.getChildren().length == 0) {
                            fo.delete();
                        }

                        // Context menu
                        FileObject popup = root.getFileObject("Popup");

                        if (popup != null) {
                            // I can't just do popup!=null to see if I need to dynamically add gsf
                            // menu items because modules may have registered additional Popup
                            // items, so the layer will contain Popup already
                            FileObject ref = popup.getFileObject("in-place-refactoring");
                            if (ref != null) {
                                ref.delete();
                            }

                            FileObject gotoF = popup.getFileObject("goto");
                            if (gotoF != null) {
                                fo = gotoF.getFileObject("goto-declaration");
                                if (fo != null) {
                                    fo.delete();
                                }
                                fo = gotoF.getFileObject("goto");
                                if (fo != null) {
                                    fo.delete();
                                }
                                if (gotoF.getChildren().length == 0) {
                                    gotoF.delete();
                                }
                            }
                            fo = popup.getFileObject("SeparatorBeforeCut.instance");
                            if (fo != null) {
                                fo.delete();
                            }
                            fo = popup.getFileObject("format");
                            if (fo != null) {
                                fo.delete();
                            }
                            fo = popup.getFileObject("SeparatorAfterFormat.instance");
                            if (fo != null) {
                                fo.delete();
                            }
                            fo = popup.getFileObject("pretty-print");
                            if (fo != null) {
                                fo.delete();
                            }
                            fo = popup.getFileObject("generate-goto-popup");
                            if (fo != null) {
                                fo.delete();
                            }
                            if (popup.getChildren().length == 0) {
                                popup.delete();
                            }
                        }

                        // Service to show if file is compileable or not
                        fo = root.getFileObject("UpToDateStatusProvider/org-netbeans-modules-gsfret-hints-GsfUpToDateStateProviderFactory.instance");
                        if (fo != null) {
                            fo.delete();
                        }
                        fo = root.getFileObject("UpToDateStatusProvider/org-netbeans-modules-retouche-hints-GsfUpToDateStateProviderFactory.instance");
                        if (fo != null) {
                            fo.delete();
                        }

                        // I'm not sure what this is used for - perhaps to turn orange when there are unused imports etc.
                        fo = root.getFileObject("UpToDateStatusProvider/org-netbeans-modules-gsfret-editor-semantic-OccurrencesMarkProviderCreator.instance");
                        if (fo != null) {
                            fo.delete();
                        }
                        fo = root.getFileObject("UpToDateStatusProvider/org-netbeans-modules-retouche-editor-semantic-OccurrencesMarkProviderCreator.instance");
                        if (fo != null) {
                            fo.delete();
                        }
                        fo = root.getFileObject("UpToDateStatusProvider");
                        if (fo != null && fo.getChildren().length == 0) {
                            fo.delete();
                        }

                        // Highlighting layers
                        fo = root.getFileObject("org-netbeans-modules-gsfret-editor-semantic-HighlightsLayerFactoryImpl.instance");
                        if (fo != null) {
                            fo.delete();
                        }

                        // Code completion
                        String completionProviders = "CompletionProviders";
                        FileObject completion = root.getFileObject(completionProviders);

                        if (completion != null) {
                            String templates = "org-netbeans-lib-editor-codetemplates-CodeTemplateCompletionProvider.instance";
                            FileObject templeteProvider = root.getFileObject(completionProviders + "/" + templates);
                            if (templeteProvider != null) {
                                templeteProvider.delete();
                            }
                            String provider = "org-netbeans-modules-gsfret-editor-completion-GsfCompletionProvider.instance";
                            FileObject completionProvider = root.getFileObject(completionProviders + "/" + provider);
                            if (completionProvider != null) {
                                completionProvider.delete();
                            }

                            FileObject old = completion.getFileObject("org-netbeans-modules-retouche-editor-completion-GsfCompletionProvider.instance");
                            if (old != null) {
                                old.delete();
                            }
                            if (completion.getChildren().length == 0) {
                                completion.delete();
                            }
                        }

                        // Editor toolbar: commenting and uncommenting actions
                        fo = root.getFileObject("Toolbars/Default/comment");
                        if (fo != null) {
                            fo.delete();
                        }
                        fo = root.getFileObject("Toolbars/Default/uncomment");
                        if (fo != null) {
                            fo.delete();
                        }
                        FileObject sep = root.getFileObject("Toolbars/Default/Separator-before-comment.instance");
                        if (sep != null) {
                            sep.delete();
                        }
                        fo = root.getFileObject("Toolbars/Default");
                        if (fo != null && fo.getChildren().length == 0) {
                            fo.delete();
                            fo = root.getFileObject("Toolbars");
                            if (fo != null && fo.getChildren().length == 0) {
                                fo.delete();
                            }
                        }

                        // init code templates
                        fo = root.getFileObject("CodeTemplateProcessorFactories/org-netbeans-modules-gsfret-editor-codetemplates-GsfCodeTemplateProcessor$Factory.instance");
                        if (fo != null) {
                            fo.delete();
                        }
                        FileObject old = root.getFileObject("CodeTemplateProcessorFactories/org-netbeans-modules-retouche-editor-codetemplates-GsfCodeTemplateProcessor$Factory.instance");
                        if (old != null) {
                            old.delete();
                        }
                        fo = root.getFileObject("CodeTemplateProcessorFactories");
                        if (fo != null && fo.getChildren().length == 0) {
                            fo.delete();
                        }

                        // init code templates filters
                        fo = root.getFileObject("CodeTemplateFilterFactories/org-netbeans-modules-gsfret-editor-codetemplates-GsfCodeTemplateFilter$Factory.instance");
                        if (fo != null) {
                            fo.delete();
                        }
                        old = root.getFileObject("CodeTemplateFilterFactories/org-netbeans-modules-retouche-editor-codetemplates-GsfCodeTemplateFilter$Factory.instance");
                        if (old != null) {
                            old.delete();
                        }
                        fo = root.getFileObject("CodeTemplateFilterFactories");
                        if (fo != null && fo.getChildren().length == 0) {
                            fo.delete();
                        }

                        if (innerMime.getChildren().length == 0) {
                            innerMime.delete();
                        }
                    }

                    if (outerMime.getChildren().length == 0) {
                        outerMime.delete();
                    }
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    private final class FsTracker implements FileChangeListener, Runnable {

        private final FileSystem fs;
        private RequestProcessor.Task slidingTask = RequestProcessor.getDefault().create(this);

        public FsTracker(FileSystem fs) {
            this.fs = fs;
            if (this.fs != null) {
                this.fs.addFileChangeListener(FileUtil.weakFileChangeListener(this, this.fs));
            }
        }

        public void fileFolderCreated(FileEvent fe) {
            process(fe);
        }

        public void fileDataCreated(FileEvent fe) {
            process(fe);
        }

        public void fileChanged(FileEvent fe) {
            process(fe);
        }

        public void fileDeleted(FileEvent fe) {
            process(fe);
        }

        public void fileRenamed(FileRenameEvent fe) {
            process(fe);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            process(fe);
        }

        private void process(FileEvent fe) {
            if (fe.getFile().getPath().startsWith(FOLDER)) {
                synchronized (LanguageRegistry.this) {
                    cacheDirty = true;
                    slidingTask.schedule(100);
                }
            }
        }

        public void run() {
            synchronized (LanguageRegistry.this) {
                if (cacheDirty) {
                    cacheDirty = false;
                    languagesCache = readSfs(fs, languagesCache);
                    GsfDataLoader.getLoader(GsfDataLoader.class).initExtensions();
                }
            }
        }
    } // End of SfsTracker class

}
