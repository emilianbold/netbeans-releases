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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.gsf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.gsf.annotations.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;

/**
 * Registry which locates and provides information about languages supported
 * by various plugins.
 *
 * @author Tor Norbye
 */
public class LanguageRegistry implements Iterable<Language> {

    private static LanguageRegistry instance;
    private static final String DISPLAY_NAME = "displayName";
    private static final String ICON_BASE = "iconBase";
    private static final String EXTENSIONS = "extensions";
    private static final String LANGUAGE = "language.instance";
    private static final String PARSER = "parser.instance";
    private static final String COMPLETION = "completion.instance";
    private static final String RENAMER = "renamer.instance";
    private static final String FORMATTER = "formatter.instance";
    private static final String BRACKET_COMPLETION = "bracket.instance";
    private static final String DECLARATION_FINDER = "declarationfinder.instance";
    private static final String INDEXER = "indexer.instance";
    private static final String PALETTE = "palette.instance";
    private static final String STRUCTURE = "structure.instance";
    private static final String HINTS = "hints.instance";

    /** Location in the system file system where languages are registered */
    private static final String FOLDER = "GsfPlugins";
    private List<Language> languages;
    private boolean languagesInitialized;

    /**
     * Creates a new instance of LanguageRegistry
     */
    public LanguageRegistry() {
        initialize();
    }

    /** For testing only! */
    public void addLanguages(List<Language> newLanguages) {
        if (languages != null && languages.size() > 0) {
            throw new RuntimeException("This is for testing purposes only!!!");
        }

        this.languages = newLanguages;
    }

    public static synchronized LanguageRegistry getInstance() {
        if (instance == null) {
            instance = new LanguageRegistry();
        }

        return instance;
    }

    /**
     * Return a language implementation that corresponds to the given file extension,
     * or null if no such language is supported
     */
    public Language getLanguageByExtension(@NonNull
    String extension) {
        extension = extension.toLowerCase();

        // TODO - create a map if this is slow
        for (Language language : this) {
            String[] extensions = language.getExtensions();

            for (int i = 0; i < extensions.length; i++) {
                if (extension.equals(extensions[i])) {
                    return language;
                }
            }
        }

        return null;
    }

    /**
     * Return a language implementation that corresponds to the given mimeType,
     * or null if no such language is supported
     */
    public Language getLanguageByMimeType(@NonNull
    String mimeType) {
        assert mimeType.equals(mimeType.toLowerCase());

        for (Language language : this) {
            if (language.getMimeType().equals(mimeType)) {
                return language;
            }
        }

        return null;
    }

    /**
     * Return true iff the given mimeType is supported by a registered language plugin
     * @return True iff the given mimeType is supported
     */
    public boolean isSupported(@NonNull
    String mimeType) {
        for (Language language : this) {
            if (mimeType.equals(language.getMimeType())) {
                return true;
            }
        }

        return false;
    }

    public Iterator<Language> iterator() {
        if (languages == null) {
            return new Iterator<Language>() {

                public boolean hasNext() {
                    return false;
                }

                public Language next() {
                    return null;
                }

                public void remove() {
                }
            };
        } else {
            return languages.iterator();
        }
    }

    private synchronized void initialize() {
        if (languages == null) {
            readSfs();

            initializeLanguages();
        }
    }

    synchronized void initializeLanguages() {
        if (languagesInitialized) {
            return;
        }

        languagesInitialized = true;

        if (languages == null) {
            // No registered languages
            return;
        }

        Iterator it = languages.iterator();

        while (it.hasNext()) {
            final Language language = (Language)it.next();

            initializeLanguage(language);

            // I had hoped to lazily initialize editors
            // but the Options panel is eagerly (at startup) caching the
            // set of mime folders that provide NetBeans/Defaults/coloring.xml
            // in the system file system, and only those are listed in the
            // Languages list for syntax editing.
            // One thing I can do here, is ONLY populate the coloring in advance
            // and leave the other portions for later - but coloring is probably
            // the most expensive file to compute anyway. Luckily, this should
            // only have to be done once - it will not be updated on subsequent
            // IDE starts until the user dir is removed.
            // Actually, this causes some real serious problems. DataLoader registration
            // doesn't happen in just one go - and here, the call to initializeLanguageForEditor will
            // be called before all loaders have been registered (I was seeing it with RhtmlDataLoader)
            // but the list will be fixed (because DataLoaderPool calls down to the LoaderPoolNode
            // and copies it list before it's done, and doesn't know to refresh itself).
            //
            // Perhaps I can work around this by adding some specific MIME folder registrations
            // early, but not do full initialization? I specifically need to avoid any calls into
            // the DataObject area - which initializeLanguageForEditor will do when instantiating the
            // registered scanners etc.
            //            SwingUtilities.invokeLater(new Runnable() {
            //                    // Gotta invoke later because if it's done as part of DataLoader initialization,
            //                    // loader registration fails and our files are not recognized
            //                    public void run() {
            //                        initializeLanguageForEditor(language);
            //                    }
            //                });
        }
    }

    private void readSfs() {
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        FileObject f = sfs.findResource(FOLDER);

        if (f == null) {
            return;
        }

        // Read languages
        FileObject[] children = f.getChildren();
        languages = new ArrayList<Language>();

        for (int i = 0; i < children.length; i++) {
            FileObject mimePrefixFile = children[i];

            // Read languages
            FileObject[] innerChildren = mimePrefixFile.getChildren();

            for (int j = 0; j < innerChildren.length; j++) {
                FileObject mimeFile = innerChildren[j];

                String mime = mimePrefixFile.getName() + "/" + mimeFile.getName();
                DefaultLanguage language = new DefaultLanguage(mime);
                languages.add(language);

                String displayName = (String)mimeFile.getAttribute(DISPLAY_NAME);

                /*
                public String getLanguageName (String mimeType) {
                if (!mimeTypeToName.containsKey (mimeType)) {
                FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
                FileObject fo = fs.findResource ("Editors/" + mimeType);
                if (fo == null) return "???";
                String bundleName = (String) fo.getAttribute ("SystemFileSystem.localizingBundle");
                String name = mimeType;
                if (bundleName != null)
                try {
                name = NbBundle.getBundle (bundleName).getString (mimeType);
                } catch (MissingResourceException ex) {}
                mimeTypeToName.put (mimeType, name);
                }
                return (String) mimeTypeToName.get (mimeType);
                }
                 */
                if ((displayName != null) && (displayName.length() > 0)) {
                    language.setDisplayName(displayName);
                }

                // Try to obtain icon from (new) IDE location for icons per mime type:
                FileObject loaderMimeFile = sfs.findResource("Loaders/" + mime);

                if (loaderMimeFile != null) {
                    String iconBase = (String)loaderMimeFile.getAttribute(ICON_BASE);

                    if ((iconBase != null) && (iconBase.length() > 0)) {
                        language.setIconBase(iconBase);
                    }
                }

                //Local icon registration in the Languages/ folder
                //String iconBase = (String)mimeFile.getAttribute(ICON_BASE);
                //
                //if ((iconBase != null) && (iconBase.length() > 0)) {
                //    language.setIconBase(iconBase);
                //}
                // Look for extensions, scanners, parsers, etc.
                FileObject extensionsDir = mimeFile.getFileObject(EXTENSIONS, null);

                if ((extensionsDir != null) && extensionsDir.isFolder()) {
                    FileObject[] extensionFiles = extensionsDir.getChildren();

                    for (int k = 0; k < extensionFiles.length; k++) {
                        String extension = extensionFiles[k].getName();
                        language.addExtension(extension);
                    }
                }

                FileObject languageFile = mimeFile.getFileObject(LANGUAGE, null);

                if (languageFile != null) {
                    language.setGsfLanguageFile(languageFile);
                }

                FileObject parserFile = mimeFile.getFileObject(PARSER, null);

                if (parserFile != null) {
                    language.setParserFile(parserFile);
                }

                FileObject completionFile = mimeFile.getFileObject(COMPLETION, null);

                if (completionFile != null) {
                    language.setCompletionProviderFile(completionFile);
                }

                FileObject renamerFile = mimeFile.getFileObject(RENAMER, null);

                if (renamerFile != null) {
                    language.setInstantRenamerFile(renamerFile);
                }

                FileObject formatterFile = mimeFile.getFileObject(FORMATTER, null);

                if (formatterFile != null) {
                    language.setFormatterFile(formatterFile);
                }

                FileObject finderFile = mimeFile.getFileObject(DECLARATION_FINDER, null);

                if (finderFile != null) {
                    language.setDeclarationFinderFile(finderFile);
                }

                FileObject bracketFile = mimeFile.getFileObject(BRACKET_COMPLETION, null);

                if (bracketFile != null) {
                    language.setBracketCompletionFile(bracketFile);
                }

                FileObject indexerFile = mimeFile.getFileObject(INDEXER, null);

                if (indexerFile != null) {
                    language.setIndexerFile(indexerFile);
                }

                FileObject structureFile = mimeFile.getFileObject(STRUCTURE, null);

                if (structureFile != null) {
                    language.setStructureFile(structureFile);
                }

                FileObject hintsFile = mimeFile.getFileObject(HINTS, null);

                if (hintsFile != null) {
                    language.setHintsProviderFile(hintsFile);
                }

                FileObject paletteFile = mimeFile.getFileObject(PALETTE, null);

                if (paletteFile != null) {
                    language.setPaletteFile(paletteFile);
                }
            }
        }
    }

    /**
     * Based on code from Schliemann
     *
     * @author Jan Jancura
     */
    private void initializeLanguage(Language language) {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();

        String oldNavFileName = "Navigator/Panels/" + language.getMimeType() + "/org-netbeans-modules-retouche-navigation-GsfStructurePanel.instance";

        // Delete the old navigator description - I have moved the class name
        FileObject fo = fs.findResource(oldNavFileName);

        if (fo != null) {
            try {
                fo.delete();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        String navFileName = "Navigator/Panels/" + language.getMimeType() + "/org-netbeans-modules-retouche-navigation-ClassMemberPanel.instance";

        fo = fs.findResource(navFileName);

        if (fo == null) {
            try {
                FileUtil.createData(fs.getRoot(), navFileName);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Delayed initialization of editor settings for a language, until the editor
     * requests the info via mime lookup.
     *
     * @todo Ensure that the Options dialog also uses Mime lookup such that this
     *  is initialized in time.
     *
     * Based on code from Schliemann
     *
     * @author Jan Jancura
     */
    void initializeLanguageForEditor(Language l) {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        final FileObject root = fs.findResource("Editors/" + l.getMimeType()); // NOI18N
        if (root.getFileObject("Settings.settings") == null) {
            // NOI18N
            try {
                fs.runAtomicAction(new AtomicAction() {

                    public void run() {
                        try {
                            InputStream is = getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/gsf/GsfOptions.settings"); // NOI18N
                            try {
                                FileObject fo = root.createData("Settings.settings"); // NOI18N
                                OutputStream os = fo.getOutputStream();

                                try {
                                    FileUtil.copy(is, os);
                                } finally {
                                    os.close();
                                }
                            } finally {
                                is.close();
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // init code folding bar
        if ((root.getFileObject("SideBar/org-netbeans-modules-editor-retouche-GsfCodeFoldingSideBarFactory.instance") == null) && (l.getParser() != null)) {
            // XXX Don't construct a new parser just to see this!
            try {
                //FileUtil.createData (root, "FoldManager/org-netbeans-editor-CustomFoldManager$Factory.instance");
                FileUtil.createData(root, "FoldManager/org-netbeans-modules-retouche-editor-fold-GsfFoldManagerFactory.instance");
                FileUtil.createData(root, "SideBar/org-netbeans-modules-editor-retouche-GsfCodeFoldingSideBarFactory.instance").setAttribute("position", 1200);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // init hyperlink provider
        if (root.getFileObject("HyperlinkProviders/GsfHyperlinkProvider.instance") == null) {
            try {
                FileObject fo = FileUtil.createData(root, "HyperlinkProviders/GsfHyperlinkProvider.instance");
                fo.setAttribute("instanceClass", "org.netbeans.modules.retouche.editor.hyperlink.GsfHyperlinkProvider");
                fo.setAttribute("instanceOf", "org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // Context menu
        FileObject popup = root.getFileObject("Popup");
        if (popup == null) {
            try {
                popup = root.createFolder("Popup");
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // I can't just do popup!=null to see if I need to dynamically add gsf
        // menu items because modules may have registered additional Popup
        // items, so the layer will contain Popup already
        FileObject ref = popup.getFileObject("in-place-refactoring");

        if (ref == null) {
            try {
                popup.createData("in-place-refactoring").setAttribute("position", 680);
                //popup.createData("generate-goto-popup");
                FileObject gotoF = popup.getFileObject("goto");
                if (gotoF == null) {
                    gotoF = popup.createFolder("goto");
                    gotoF.setAttribute("position", 500);
                }
                gotoF.setAttribute("SystemFileSystem.localizingBundle", "org.netbeans.modules.gsf.Bundle");
                gotoF.createData("goto-declaration").setAttribute("position", 500);
                gotoF.createData("goto").setAttribute("position", 600); // Goto by linenumber
                // What about goto-source etc?
                // TODO: Goto Type (integrate with Java's GotoType)
                // Temporary - userdir upgrade
                if (popup.getFileObject("SeparatorBeforeCut.instance") == null) {
                    FileObject sep = popup.createData("SeparatorBeforeCut.instance");
                    sep.setAttribute("instanceClass", "javax.swing.JSeparator");
                    // Should be before org-netbeans-modules-editor-NbSelectInPopupAction.instance & org-openide-actions-CutAction.instance:
                    sep.setAttribute("position", 1200);
                }
                // Temporary - userdir upgrade
                if (popup.getFileObject("format") == null) {
                    popup.createData("format").setAttribute("position", 750);
                }
                FileObject sep2 = popup.createData("SeparatorAfterFormat.instance");
                sep2.setAttribute("instanceClass", "javax.swing.JSeparator");
                // Should be between org-openide-actions-PasteAction.instance and format
                sep2.setAttribute("position", 780);
                // Temporary - userdir upgrade
                // Obsolete - nuke
                if (popup.getFileObject("pretty-print") != null) {
                    popup.getFileObject("pretty-print").delete();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            if (popup.getFileObject("generate-goto-popup") != null) {
                FileObject f = popup.getFileObject("generate-goto-popup");

                try {
                    f.delete();
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }

            // Temporary userdir upgrade
            if (root.getFileObject("Popup/format") == null) {
                try {
                    popup.createData("format");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            // Obsolete - nuke
            if (root.getFileObject("Popup/pretty-print") != null) {
                try {
                    FileObject d = root.getFileObject("Popup/pretty-print");
                    d.delete();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        // Service to show if file is compileable or not
        if (root.getFileObject("UpToDateStatusProvider/org-netbeans-modules-retouche-hints-GsfUpToDateStateProviderFactory.instance") == null) {
            try {
                FileUtil.createData(root, "UpToDateStatusProvider/org-netbeans-modules-retouche-hints-GsfUpToDateStateProviderFactory.instance");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // I'm not sure what this is used for - perhaps to turn orange when there are unused imports etc.
        if (root.getFileObject("UpToDateStatusProvider/org-netbeans-modules-retouche-editor-semantic-OccurrencesMarkProviderCreator.instance") == null) {
            try {
                FileUtil.createData(root, "UpToDateStatusProvider/org-netbeans-modules-retouche-editor-semantic-OccurrencesMarkProviderCreator.instance");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        /* XXX breaks ValidateLayerConsistencyTest.testInstantiateAllInstances: Editors/text/x-ruby/org-netbeans-modules-retouche-hints-GsfHintsProvider.instance thrown exception java.lang.ClassNotFoundException: Cannot instantiate org.netbeans.modules.retouche.hints.GsfHintsProvider
        // Editor hints -- this may not be necessary - might already be done from the java source tasks factory...
        String hintsFilename =
        "Editors/" + l.getMimeType() +
        "/org-netbeans-modules-retouche-hints-GsfHintsProvider.instance";
        if (fs.findResource(hintsFilename) == null) {
        try {
        FileObject fo = FileUtil.createData(fs.getRoot(), hintsFilename);
        fo.setAttribute("instanceOf", "org.netbeans.modules.editor.hints.spi.HintsProvider");
        } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
        }
        }
         */

        // Code completion
        String completionProviders = "CompletionProviders";
        FileObject completion = root.getFileObject(completionProviders);

        if (completion == null) {
            try {
                completion = root.createFolder(completionProviders);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (completion != null) {
            String templates = "org-netbeans-lib-editor-codetemplates-CodeTemplateCompletionProvider.instance";
            FileObject templeteProvider = root.getFileObject(completionProviders + "/" + templates);
            if (templeteProvider == null) {
                try {
                    completion.createData(templates);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            String provider = "org-netbeans-modules-retouche-editor-completion-GsfCompletionProvider.instance";
            FileObject completionProvider = root.getFileObject(completionProviders + "/" + provider);
            if (completionProvider == null) {
                try {
                    completion.createData(provider);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        // Editor toolbar: commenting and uncommenting actions
        if (root.getFileObject("Toolbars/Default/comment") == null) {
            if (!((l.getGsfLanguage() == null) || (l.getGsfLanguage().getLineCommentPrefix() == null))) {
                try {
                    FileObject sep = FileUtil.createData(root, "Toolbars/Default/Separator-before-comment.instance");
                    sep.setAttribute("instanceClass", "javax.swing.JSeparator");
                    sep.setAttribute("position", 30000);

                    FileUtil.createData(root, "Toolbars/Default/comment").setAttribute("position", 30100);
                    FileUtil.createData(root, "Toolbars/Default/uncomment").setAttribute("position", 30200);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        // init code templates
        if (root.getFileObject("CodeTemplateProcessorFactories") == null) {
            try {
                FileObject fo = FileUtil.createData(root, "CodeTemplateProcessorFactories/org-netbeans-modules-retouche-editor-codetemplates-GsfCodeTemplateProcessor$Factory.instance");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        // init code templates filters
        if (root.getFileObject("CodeTemplateFilterFactories") == null) {
            try {
                FileObject fo = FileUtil.createData(root, "CodeTemplateFilterFactories/org-netbeans-modules-retouche-editor-codetemplates-GsfCodeTemplateFilter$Factory.instance");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // Temporarily disabled; each language does it instead
        //initializeColoring(l);
    }

    //    void initializeColoring(Language l) {
    //        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
    //        final FileObject root = fs.findResource("Editors/" + l.getMimeType());
    //
    //        // Initialize Coloring
    //        if (l.getGsfLanguage() != null) {
    //            List<?extends TokenId> types = l.getGsfLanguage().getRelevantTokenTypes();
    //
    //            if ((types != null) && (types.size() > 0)) {
    //                //String prefix = "gls-";
    //                String prefix = "";
    //
    //                // Default categories
    //                Collection defaults =
    //                    EditorSettings.getDefault().getDefaultFontColorDefaults("NetBeans");
    //                Map defaultsMap = new HashMap();
    //                Iterator it = defaults.iterator(); // check if IDE Defaults module is installed
    //
    //                while (it.hasNext()) {
    //                    AttributeSet as = (AttributeSet)it.next();
    //                    defaultsMap.put(as.getAttribute(StyleConstants.NameAttribute), as);
    //                }
    //
    //                // current colors
    //                FontColorSettingsFactory fcsf =
    //                    EditorSettings.getDefault()
    //                                  .getFontColorSettings(new String[] { l.getMimeType() });
    //                Collection colors = fcsf.getAllFontColors("NetBeans"); // NOI18N
    //                Map colorsMap = new HashMap();
    //                it = colors.iterator();
    //
    //                while (it.hasNext()) {
    //                    AttributeSet as = (AttributeSet)it.next();
    //                    colorsMap.put(as.getAttribute(StyleConstants.NameAttribute), as);
    //                }
    //
    //                for (TokenId id : types) {
    //                    TokenType type = l.getGsfLanguage().getTokenType(id);
    //
    //                    if (type == null) {
    //                        type = DefaultTokenType.getTokenType(id);
    //                    }
    //
    //                    if (type == null) {
    //                        continue;
    //                    }
    //
    //                    //String colorName = type.getName();
    //                    String colorName = id.name();
    //                    String category = type.getCategory();
    //                    SimpleAttributeSet as = new SimpleAttributeSet();
    //                    as.addAttribute(StyleConstants.NameAttribute, colorName);
    //
    //                    if (colorName != null) {
    //                        addColor(colorName, category, as, l, colorsMap, defaultsMap, prefix,
    //                            type.getDisplayName(), type.getColor(), type.getBackgroundColor(),
    //                            type.getFontType());
    //                    } else {
    //                        System.err.println("skipping null colorName for " + type);
    //                    }
    //                }
    //
    //                fcsf.setAllFontColorsDefaults("NetBeans", colorsMap.values());
    //                fcsf.setAllFontColors("NetBeans", colorsMap.values());
    //            }
    //        }
    //    }
    //
    //    /**
    //     * Based on code from Schliemann
    //     *
    //     * @author Jan Jancura
    //     * @author Tor Norbye
    //     */
    //    private void addColor(String colorName, String category, SimpleAttributeSet sas, Language l,
    //        Map colorsMap, Map defaultsMap, String prefix, String displayName, Color fg, Color bg,
    //        int fontMode) {
    //        String color = colorName;
    //
    //        String pcolor = prefix + color;
    //
    //        if (sas == null) {
    //            sas = new SimpleAttributeSet();
    //        }
    //
    //        sas.addAttribute(StyleConstants.NameAttribute, pcolor);
    //        sas.addAttribute(EditorStyleConstants.DisplayName, displayName);
    //
    //        if (defaultsMap.containsKey(category)) {
    //            sas.addAttribute(EditorStyleConstants.Default, category);
    //        } else {
    //            if (fg != null) {
    //                sas.addAttribute(StyleConstants.Foreground, fg);
    //            }
    //
    //            if (bg != null) {
    //                sas.addAttribute(StyleConstants.Background, bg);
    //            }
    //
    //            if ((fontMode & Font.BOLD) != 0) {
    //                sas.addAttribute(StyleConstants.Bold, Boolean.TRUE);
    //            }
    //
    //            if ((fontMode & Font.ITALIC) != 0) {
    //                sas.addAttribute(StyleConstants.Italic, Boolean.TRUE);
    //            }
    //        }
    //
    //        colorsMap.put(pcolor, sas);
    //    }
}
