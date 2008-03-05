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
import org.netbeans.modules.gsf.api.Completable;
import org.netbeans.modules.gsf.api.BracketCompletion;
import org.netbeans.modules.gsf.api.DeclarationFinder;
import org.netbeans.modules.gsf.api.Formatter;
import org.netbeans.modules.gsf.api.InstantRenamer;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.GsfLanguage;
import org.netbeans.modules.gsf.api.HintsProvider;
import org.netbeans.modules.gsf.api.OccurrencesFinder;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
import org.netbeans.modules.gsf.api.StructureScanner;
//import org.netbeans.spi.palette.PaletteController;
import org.netbeans.modules.gsfret.editor.semantic.ColoringManager;
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
    private ColoringManager coloringManager;
    private String displayName;
    private String iconBase;
    private String mime;
    private boolean useCustomEditorKit;
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
    public DefaultLanguage(String mime) {
        this.mime = mime;
    }

    /** For testing purposes only!*/
    public DefaultLanguage(String displayName, String iconBase, String mime, List<String> extensions, List<Action> actions,
            GsfLanguage gsfLanguage, Parser parser, Completable completionProvider, InstantRenamer renamer,
            DeclarationFinder declarationFinder, Formatter formatter, BracketCompletion bracketCompletion, Indexer indexer,
            StructureScanner structure, /*PaletteController*/Object palette, boolean useCustomEditorKit) {
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
        this.useCustomEditorKit = useCustomEditorKit;
    }


    public boolean useCustomEditorKit() {
        return useCustomEditorKit;
    }
    
    public void setUseCustomEditorKit(boolean useCustomEditorKit) {
        this.useCustomEditorKit = useCustomEditorKit;
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

    public void setOccurrencesFinderFile(FileObject occurrencesFile) {
        this.occurrencesFile = occurrencesFile;
    }
    
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

    public void setSemanticAnalyzer(FileObject semanticFile) {
        this.semanticFile = semanticFile;
    }
}
