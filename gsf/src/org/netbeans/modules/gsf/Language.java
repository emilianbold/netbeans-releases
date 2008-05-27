/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.gsf.api.*;
import org.netbeans.modules.gsf.api.CodeCompletionHandler;
import org.netbeans.modules.gsf.api.DeclarationFinder;
import org.netbeans.modules.gsf.api.InstantRenamer;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.GsfLanguage;
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.modules.gsf.api.annotations.NonNull;
import org.netbeans.modules.gsf.api.KeystrokeHandler;
import org.netbeans.modules.gsf.api.Formatter;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.StructureScanner;
//import org.netbeans.spi.palette.PaletteController;
import org.netbeans.modules.gsfret.editor.semantic.ColoringManager;
import org.netbeans.modules.gsfret.hints.infrastructure.GsfHintsManager;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;


/**
 * @todo Should languages get to declared "priorities"? In case there are
 *    overlaps in extensions that is.
 * @todo Can I devise a way where one language can "extend" another?
 *    For example, the Jackpot Rule language should simply be the Java language
 *    with a couple of simple changes.
 * @todo Add LanguageVersion list property. For example, for Java, they could be
 *    JDK 1.4, 5.0, 6.0. This would be exposed as a property somewhere (perhaps
 *    on a project basis) and would be used by plugins to drive parser specific
 *    info.  Similarly for JavaScript I have multiple language versions - 1.0 through 1.6
 *    in the case of Rhino (corresponding to different JavaScript/EcmaScript versions).
 *
 * @author <a href="mailto:tor.norbye@sun.com">Tor Norbye</a>
 */
public final class Language {
    private ColoringManager coloringManager;
    private String iconBase;
    private String mime;
    private boolean useCustomEditorKit;
    private List<Action> actions;
    private GsfLanguage language;
    private Parser parser;
    private CodeCompletionHandler completionProvider;
    private InstantRenamer renamer;
    private DeclarationFinder declarationFinder;
    private Formatter formatter;
    private KeystrokeHandler bracketCompletion;
    private Indexer indexer;
    private StructureScanner structure;
    private HintsProvider hintsProvider;
    private GsfHintsManager hintsManager;
    //private PaletteController palette;
    private OccurrencesFinder occurrences;
    private SemanticAnalyzer semantic;
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
    private FileObject semanticFile;
    private FileObject occurrencesFile;
    
    
    /** Creates a new instance of DefaultLanguage */
    public Language(String mime) {
        this.mime = mime;
    }

    /** For testing purposes only!*/
    public Language(String iconBase, String mime, List<Action> actions,
            GsfLanguage gsfLanguage, Parser parser, CodeCompletionHandler completionProvider, InstantRenamer renamer,
            DeclarationFinder declarationFinder, Formatter formatter, KeystrokeHandler bracketCompletion, Indexer indexer,
            StructureScanner structure, /*PaletteController*/Object palette, boolean useCustomEditorKit) {
        this.iconBase = iconBase;
        this.mime = mime;
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
        this.useCustomEditorKit = useCustomEditorKit;
    }



    /** 
     * HACK: Some language supports may want to use their own editor kit
     * implementation (such as Schliemann) for some services. By returning
     * true here (which can be done by registering "useCustomEditorKit" on the
     * GsfPlugin folder for the mime type) GSF will not register its own editing
     * services for this mime type.
     * <p>
     * If you set this flag, you may need to register additional services on your
     * own. For example, if you still want GSF "Go To Declaration" functionality,
     * you need to register the GsfHyperlinkProvider.
     * The ruby/rhtml/ module provides an example of this.
     * <p>
     * NOTE: Code folding doesn't work until you enable code folding for your
     * editor kit; see GsfEditorKitFactory's reference to CODE_FOLDING_ENABLE for
     * an example.
     */
    public boolean useCustomEditorKit() {
        return useCustomEditorKit;
    }
    
    void setUseCustomEditorKit(boolean useCustomEditorKit) {
        this.useCustomEditorKit = useCustomEditorKit;
    }
    
    /** Return the display-name (user visible, and localized) name of this language.
     * It should be brief (one or two words). For example "Java", "C++", "Groovy",
     * "Visual Basic", etc.
     */
    @NonNull
    public String getDisplayName() {
        return getGsfLanguage().getDisplayName();
    }

    /** Return an icon to be used for files of this language type.
     *  @see org.openide.util.Utilities#loadImage
     */

    //public Image getIcon();

    /** Hmmmm this is a bit rough. The path would have to be relative to some resource...
     *  I guess it would be relative to the specific plugin language class?
     * Example:  "com/foo/bar/javascript.gif"
     * @todo More documentation here, or revise API entirely
     */
    public String getIconBase() {
        return iconBase;
    }

    void setIconBase(String iconBase) {
        this.iconBase = iconBase;
    }

    /** Return the mime-type of this language. For example text/x-java.
     */
    @NonNull
    public String getMimeType() {
        return mime;
    }

    void setMimeType(String mime) {
        this.mime = mime;
    }

    /** Return Actions that will be provided in the editor context menu for this language.
     */
    public Action[] getEditorActions() {
        if (actions != null) {
            return actions.toArray(new Action[actions.size()]);
        } else {
            return new Action[0];
        }
    }

    /** Return a language configuration object for this language.
     */
    @NonNull
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

    //void setGsfLanguage(GsfLanguage scanner) {
    //    this.language = language;
    //}

    void setGsfLanguageFile(FileObject languageFile) {
        this.languageFile = languageFile;
    }
    
    /** Return a parser for use with this language. A parser is optional (in which
     * case getParser() may return null) but in that case a lot of functionality
     * will be disabled for this language.
     * @todo Clarify whether clients should cache instances of this or if it will
     *  be called only once and management done by the IDE
     */
    @CheckForNull
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

    void setParser(Parser parser) {
        this.parser = parser;
    }
    
    void setParserFile(FileObject parserFile) {
        this.parserFile = parserFile;
    }
    
    public void addAction(Action action) {
        if (actions == null) {
            actions = new ArrayList<Action>();
        }
        actions.add(action);
    }
    
    // XXX This is crying out for generics!
    private Object createInstance(FileObject file) {
        assert file.getExt().equals("instance"); // NOI18N
        // Construct the service lazily using the instance cookie on the provided data object
        try {
            DataObject dobj = DataObject.find(file);
            InstanceCookie ic = dobj.getCookie(InstanceCookie.class);
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
    
    @Override
    public String toString() {
        return mime + ":" + getDisplayName();
    }

    /**
     * Get a code completion handler, if any
     */
    @CheckForNull
    public CodeCompletionHandler getCompletionProvider() {
        if (completionProvider == null && completionProviderFile != null) {
            // Lazily construct completion provider
            completionProvider = (CodeCompletionHandler)createInstance(completionProviderFile);
            if (completionProvider == null) {
                // Don't keep trying
                completionProviderFile = null;
            }
        }
        return completionProvider;
    }

    void setCompletionProvider(CodeCompletionHandler completionProvider) {
        this.completionProvider = completionProvider;
    }
    
    void setCompletionProviderFile(FileObject completionProviderFile) {
        this.completionProviderFile = completionProviderFile;
    }

    /**
     * Get a rename helper, if any, for instant renaming
     */
    @CheckForNull
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

    void setInstantRenamerFile(FileObject renamerFile) {
        this.renamerFile = renamerFile;
    }

    /**
     * Get a Declaration finder, if any, for resolving declarations for a given identifier
     */
    @CheckForNull
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

    void setDeclarationFinderFile(FileObject declarationFinderFile) {
        this.declarationFinderFile = declarationFinderFile;
    }

    /**
     * Get an Formatter, if any, for helping indent and reformat code
     */
    @CheckForNull
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

    void setFormatterFile(FileObject formatterFile) {
        this.formatterFile = formatterFile;
    }
    
    /**
     * Get a KeystrokeHandler helper, if any, for helping with bracket completion
     */
    @CheckForNull
    public KeystrokeHandler getBracketCompletion() {
        if (bracketCompletion == null && bracketCompletionFile != null) {
            bracketCompletion = (KeystrokeHandler)createInstance(bracketCompletionFile);
            if (bracketCompletion == null) {
                // Don't keep trying
                bracketCompletionFile = null;
            }
        }
        return bracketCompletion;
    }

    void setBracketCompletionFile(FileObject bracketCompletionFile) {
        this.bracketCompletionFile = bracketCompletionFile;
    }

    /**
     * Get an associated palette controller, if any
     */
/*
    @CheckForNull
    PaletteController getPalette();
*/

    /**
     * Get an associated indexer, if any
     */
    @CheckForNull
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

    void setIndexerFile(FileObject indexerFile) {
        this.indexerFile = indexerFile;
    }

    /**
     * Get a structure scanner which produces navigation/outline contents
     */
    @CheckForNull
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

    void setStructureFile(FileObject structureFile) {
        this.structureFile = structureFile;
    }

    /**
     * Get an associated hints provider, if any
     */
    @CheckForNull
    public HintsProvider getHintsProvider() {
        if (hintsProvider == null && hintsProviderFile != null) {
            hintsProvider = (HintsProvider)createInstance(hintsProviderFile);
            if (hintsProvider == null) {
                // Don't keep trying
                hintsProviderFile = null;
            } else {
                hintsManager = new GsfHintsManager(getMimeType(), hintsProvider, this);
            }
        }
        return hintsProvider;
    }
    
    @NonNull
    public GsfHintsManager getHintsManager() {
        assert hintsProvider != null; // Should never call this method before getHintsProvider has been initialized!
        return hintsManager;
    }

    void setHintsProviderFile(FileObject hintsProviderFile) {
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

    void setPaletteFile(FileObject paletteFile) {
        this.paletteFile = paletteFile;
    }

    /**
     * Return the coloring manager for this language
     */
    @NonNull
    public ColoringManager getColoringManager() {
        if (coloringManager == null) {
            coloringManager = new ColoringManager(mime);
        }

        return coloringManager;
    }
    
    public boolean hasStructureScanner() {
        return this.structureFile != null;
    }
    
    public boolean hasFormatter() {
        return this.formatterFile != null;
    }

    public boolean hasHints() {
        return this.hintsProviderFile != null;
    }
    
    /**
     * Return the occurrences finder for this language
     */
    @NonNull
    public OccurrencesFinder getOccurrencesFinder() {
        if (occurrences == null && occurrencesFile != null) {
            occurrences = (OccurrencesFinder)createInstance(occurrencesFile);
            if (occurrences == null) {
                // Don't keep trying
                occurrencesFile = null;
            }
        }
        return occurrences;
    }

    void setOccurrencesFinderFile(FileObject occurrencesFile) {
        this.occurrencesFile = occurrencesFile;
    }
    
    /**
     * Return the semantic analyzer for this language
     */
    @NonNull
    public SemanticAnalyzer getSemanticAnalyzer() {
        if (semantic == null && semanticFile != null) {
            semantic = (SemanticAnalyzer)createInstance(semanticFile);
            if (semantic == null) {
                // Don't keep trying
                semanticFile = null;
            }
        }
        return semantic;
    }

    void setSemanticAnalyzer(FileObject semanticFile) {
        this.semanticFile = semanticFile;
    }
}
