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

package org.netbeans.modules.gsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.gsf.Completable;
import org.netbeans.api.gsf.BracketCompletion;
import org.netbeans.api.gsf.DeclarationFinder;
import org.netbeans.api.gsf.Formatter;
import org.netbeans.api.gsf.InstantRenamer;
import org.netbeans.api.gsf.Indexer;
import org.netbeans.modules.gsf.Language;
import org.netbeans.api.gsf.Parser;
import org.netbeans.api.gsf.GsfLanguage;
import org.netbeans.api.gsf.HintsProvider;
import org.netbeans.api.gsf.StructureScanner;
//import org.netbeans.spi.palette.PaletteController;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Tor Norbye
 */
public class DefaultLanguage implements Language {
    private String displayName;
    private String iconBase;
    private String mime;
    private List<String> extensions;
    private List<Action> actions;
    private GsfLanguage language;
    private Parser parser;
    private Completable completionProvider;
    private InstantRenamer renamer;
    private DeclarationFinder declarationFinder;
    private Formatter formatter;
    private BracketCompletion bracketCompletion;
    private Indexer indexer;
    private StructureScanner structure;
    private HintsProvider hintsProvider;;
    //private PaletteController palette;
    private FileObject parserFile;
    private FileObject languageFile;
    private FileObject completionProviderFile;
    private FileObject renamerFile;
    private FileObject declarationFinderFile;
    private FileObject formatterFile;
    private FileObject bracketCompletionFile;
    private FileObject indexerFile;
    private FileObject structureFile;
    private FileObject hintsProviderFile;
    private FileObject paletteFile;
    
    /** Creates a new instance of DefaultLanguage */
    public DefaultLanguage(String mime) {
        this.mime = mime;
    }

    /** For testing purposes only!*/
    public DefaultLanguage(String displayName, String iconBase, String mime, List<String> extensions, List<Action> actions,
            GsfLanguage gsfLanguage, Parser parser, Completable completionProvider, InstantRenamer renamer,
            DeclarationFinder declarationFinder, Formatter formatter, BracketCompletion bracketCompletion, Indexer indexer,
            StructureScanner structure, /*PaletteController*/Object palette) {
        this.displayName = displayName;
        this.iconBase = iconBase;
        this.mime = mime;
        this.extensions = extensions;
        this.actions = actions;
        this.language = gsfLanguage;
        this.parser = parser;
        this.completionProvider = completionProvider;
        this.renamer = renamer;
        this.declarationFinder = declarationFinder;
        this.formatter = formatter;
        this.bracketCompletion = bracketCompletion;
        this.indexer = indexer;
        this.structure = structure;
//        this.palette = palette;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIconBase() {
        return iconBase;
    }

    public void setIconBase(String iconBase) {
        this.iconBase = iconBase;
    }

    public String getMimeType() {
        return mime;
    }

    public void setMimeType(String mime) {
        this.mime = mime;
    }

    public String[] getExtensions() {
        if (extensions != null) {
            return extensions.toArray(new String[extensions.size()]);
        } else {
            return new String[0];
        }
    }

    public Action[] getEditorActions() {
        if (actions != null) {
            return actions.toArray(new Action[actions.size()]);
        } else {
            return new Action[0];
        }
    }

    public GsfLanguage getGsfLanguage() {
        if (language == null && languageFile != null) {
            // Lazily construct Language
            language = (GsfLanguage)createInstance(languageFile);
            if (language == null) {
                // Don't keep trying
                languageFile = null;
            }
        }
        return language;
    }

    //public void setGsfLanguage(GsfLanguage scanner) {
    //    this.language = language;
    //}

    public void setGsfLanguageFile(FileObject languageFile) {
        this.languageFile = languageFile;
    }
    
    public Parser getParser() {
        if (parser == null && parserFile != null) {
            // Lazily construct Parser
            parser = (Parser)createInstance(parserFile);
            if (parser == null) {
                // Don't keep trying
                parserFile = null;
            }
        }
        return parser;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }
    
    public void setParserFile(FileObject parserFile) {
        this.parserFile = parserFile;
    }
    
    public void addAction(Action action) {
        if (actions == null) {
            actions = new ArrayList<Action>();
        }
        actions.add(action);
    }
    
    public void addExtension(String extension) {
        if (extension == null || extension.length() == 0 || extension.startsWith(".")) {
            throw new IllegalArgumentException("Extension should be a nonzero string not starting with a dot");
        }
        
        if (extensions == null) {
            extensions = new ArrayList<String>();
        }
        
        assert extension.equals(extension.toLowerCase());

        extensions.add(extension);
    }

    // XXX This is crying out for generics!
    private Object createInstance(FileObject file) {
        assert file.getExt().equals("instance"); // NOI18N
        // Construct the service lazily using the instance cookie on the provided data object
        try {
            DataObject dobj = DataObject.find(file);
            InstanceCookie ic = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
            return ic.instanceCreate();
        } catch (ClassNotFoundException e) {
            ErrorManager.getDefault().notify(e);
        } catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(e);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }
    
    public String toString() {
        return mime + ":" + displayName;
    }

    public Completable getCompletionProvider() {
        if (completionProvider == null && completionProviderFile != null) {
            // Lazily construct completion provider
            completionProvider = (Completable)createInstance(completionProviderFile);
            if (completionProvider == null) {
                // Don't keep trying
                completionProviderFile = null;
            }
        }
        return completionProvider;
    }

    public void setCompletionProvider(Completable completionProvider) {
        this.completionProvider = completionProvider;
    }
    
    public void setCompletionProviderFile(FileObject completionProviderFile) {
        this.completionProviderFile = completionProviderFile;
    }

    public InstantRenamer getInstantRenamer() {
        if (renamer == null && renamerFile != null) {
            renamer = (InstantRenamer)createInstance(renamerFile);
            if (renamer == null) {
                // Don't keep trying
                renamerFile = null;
            }
        }
        return renamer;
    }

    public void setInstantRenamerFile(FileObject renamerFile) {
        this.renamerFile = renamerFile;
    }

    public DeclarationFinder getDeclarationFinder() {
        if (declarationFinder == null && declarationFinderFile != null) {
            declarationFinder = (DeclarationFinder)createInstance(declarationFinderFile);
            if (declarationFinder == null) {
                // Don't keep trying
                declarationFinderFile = null;
            }
        }
        return declarationFinder;
    }

    public void setDeclarationFinderFile(FileObject declarationFinderFile) {
        this.declarationFinderFile = declarationFinderFile;
    }

    public Formatter getFormatter() {
        if (formatter == null && formatterFile != null) {
            formatter = (Formatter)createInstance(formatterFile);
            if (formatter == null) {
                // Don't keep trying
                formatterFile = null;
            }
        }
        return formatter;
    }

    public void setFormatterFile(FileObject formatterFile) {
        this.formatterFile = formatterFile;
    }
    
    public BracketCompletion getBracketCompletion() {
        if (bracketCompletion == null && bracketCompletionFile != null) {
            bracketCompletion = (BracketCompletion)createInstance(bracketCompletionFile);
            if (bracketCompletion == null) {
                // Don't keep trying
                bracketCompletionFile = null;
            }
        }
        return bracketCompletion;
    }

    public void setBracketCompletionFile(FileObject bracketCompletionFile) {
        this.bracketCompletionFile = bracketCompletionFile;
    }

    public Indexer getIndexer() {
        if (indexer == null && indexerFile != null) {
            indexer = (Indexer)createInstance(indexerFile);
            if (indexer == null) {
                // Don't keep trying
                indexerFile = null;
            }
        }
        return indexer;
    }

    public void setIndexerFile(FileObject indexerFile) {
        this.indexerFile = indexerFile;
    }

    public StructureScanner getStructure() {
        if (structure == null && structureFile != null) {
            structure = (StructureScanner)createInstance(structureFile);
            if (structure == null) {
                // Don't keep trying
                structureFile = null;
            }
        }
        return structure;
    }

    public void setStructureFile(FileObject structureFile) {
        this.structureFile = structureFile;
    }

    public HintsProvider getHintsProvider() {
        if (hintsProvider == null && hintsProviderFile != null) {
            hintsProvider = (HintsProvider)createInstance(hintsProviderFile);
            if (hintsProvider == null) {
                // Don't keep trying
                hintsProviderFile = null;
            }
        }
        return hintsProvider;
    }

    public void setHintsProviderFile(FileObject hintsProviderFile) {
        this.hintsProviderFile = hintsProviderFile;
    }
    
//    public PaletteController getPalette() {
//        if (palette == null && paletteFile != null) {
//            palette = (PaletteController)createInstance(paletteFile);
//            if (palette == null) {
//                // Don't keep trying
//                paletteFile = null;
//            }
//        }
//        return palette;
//    }

    public void setPaletteFile(FileObject paletteFile) {
        this.paletteFile = paletteFile;
    }
}
