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

import javax.swing.Action;
import org.netbeans.modules.gsf.api.*;
import org.netbeans.modules.gsf.api.Completable;
import org.netbeans.modules.gsf.api.DeclarationFinder;
import org.netbeans.modules.gsf.api.InstantRenamer;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.GsfLanguage;
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.modules.gsf.api.annotations.NonNull;
import org.netbeans.modules.gsf.api.BracketCompletion;
import org.netbeans.modules.gsf.api.Formatter;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.StructureScanner;
//import org.netbeans.spi.palette.PaletteController;
import org.netbeans.modules.gsfret.editor.semantic.ColoringManager;


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
public interface Language {

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
    boolean useCustomEditorKit();

    /** Return the display-name (user visible, and localized) name of this language.
     * It should be brief (one or two words). For example "Java", "C++", "Groovy",
     * "Visual Basic", etc.
     */
    @NonNull
    String getDisplayName();

    /** Return the mime-type of this language. For example text/x-java.
     */
    @NonNull
    String getMimeType();

    /** Return the set of common file extensions used for source files in this
     * type of language. It should not include the dot.
     * For example, for Java it would be { "java" }. For C++ it might
     * be { "cpp", "cc", "c++", "cxx" }. The first item in the array will be
     * considered the "primary" extension that will be used when creating new
     * files etc.
     */
    String[] getExtensions();

    /** Return a scanner (lexical analyzer, tokenizer) for use with this language.
     * @todo Clarify whether clients should cache instances of this or if it will
     *  be called only once and management done by the IDE
     */
    @CheckForNull
    GsfLanguage getGsfLanguage();

    /** Return a parser for use with this language. A parser is optional (in which
     * case getParser() may return null) but in that case a lot of functionality
     * will be disabled for this language.
     * @todo Clarify whether clients should cache instances of this or if it will
     *  be called only once and management done by the IDE
     */
    @CheckForNull
    Parser getParser();

    /** Return Actions that will be provided in the editor context menu for this language.
     */
    Action[] getEditorActions();

    /** Return an icon to be used for files of this language type.
     *  @see org.openide.util.Utilities#loadImage
     */

    //public Image getIcon();

    /** Hmmmm this is a bit rough. The path would have to be relative to some resource...
     *  I guess it would be relative to the specific plugin language class?
     * Example:  "com/foo/bar/javascript.gif"
     * @todo More documentation here, or revise API entirely
     */
    String getIconBase();

    /**
     * Get a code completion handler, if any
     */
    @CheckForNull
    Completable getCompletionProvider();

    /**
     * Get a rename helper, if any, for instant renaming
     */
    @CheckForNull
    InstantRenamer getInstantRenamer();

    /**
     * Get a Declaration finder, if any, for resolving declarations for a given identifier
     */
    @CheckForNull
    DeclarationFinder getDeclarationFinder();
    
    /**
     * Get an Formatter, if any, for helping indent and reformat code
     */
    @CheckForNull
    Formatter getFormatter();
    
    /**
     * Get a BracketCompletion helper, if any, for helping with bracket completion
     */
    @CheckForNull
    BracketCompletion getBracketCompletion();
    
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
    Indexer getIndexer();
    
    /**
     * Get an associated hints provider, if any
     */
    @CheckForNull
    HintsProvider getHintsProvider();

    /**
     * Get a structure scanner which produces navigation/outline contents
     */
    @CheckForNull
    StructureScanner getStructure();
    
    /**
     * Return the coloring manager for this language
     */
    @NonNull
    ColoringManager getColoringManager();
    
    /**
     * Return the semantic analyzer for this language
     */
    @NonNull
    SemanticAnalyzer getSemanticAnalyzer();
    
    /**
     * Return the occurrences finder for this language
     */
    @NonNull
    OccurrencesFinder getOccurrencesFinder();
    
}
