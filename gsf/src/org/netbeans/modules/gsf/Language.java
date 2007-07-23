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

import javax.swing.Action;
import org.netbeans.api.gsf.*;
import org.netbeans.api.gsf.Completable;
import org.netbeans.api.gsf.DeclarationFinder;
import org.netbeans.api.gsf.InstantRenamer;
import org.netbeans.api.gsf.Parser;
import org.netbeans.api.gsf.GsfLanguage;
import org.netbeans.api.gsf.annotations.CheckForNull;
import org.netbeans.api.gsf.annotations.NonNull;
import org.netbeans.api.gsf.BracketCompletion;
import org.netbeans.api.gsf.Formatter;
import org.netbeans.api.gsf.Indexer;
import org.netbeans.api.gsf.StructureScanner;
//import org.netbeans.spi.palette.PaletteController;


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
 * @todo Add a "Line Comment Prefix" property for languages (e.g. "//" for Java, "#" for ksh,
 *    etc. which can be used to drive the availability and implementation of the Comment
 *    (Shift + Meta + T) feature (and uncomment, Shift + Meta + D).
 *
 * @author <a href="mailto:tor.norbye@sun.com">Tor Norbye</a>
 */
public interface Language {
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
}
