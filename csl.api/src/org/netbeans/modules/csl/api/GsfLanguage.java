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

package org.netbeans.modules.csl.api;

import java.util.Collection;
import java.util.Map;
import org.netbeans.modules.csl.api.annotations.CheckForNull;
import org.netbeans.modules.csl.api.annotations.NonNull;
import org.netbeans.api.lexer.Language;
import org.openide.filesystems.FileObject;


/**
 * Lexical information for a given language.
 *
 * @author <a href="mailto:tor.norbye@sun.com">Tor Norbye</a>
 */
public interface GsfLanguage {
    /**
     * <p>Return the prefix used for line comments in this language, or null if this language
     * does not have a line comment. As an example, a Java scanner would return <code>//</code>,
     * a Ruby scanner would return <code>#</code>, a Visual Basic scanner would return <code>'</code>, etc.
     * </p>
     */
    @CheckForNull
    String getLineCommentPrefix();
    
    /**
     * <p>Return true iff the given character is considered to be an identifier character. This
     * is used for example when the user double clicks in the editor to select a "word" or identifier
     * by checking to the left and to the right of the caret position and selecting until a character
     * is not considered an identifier char by the scanner.
     * </p>
     * <p>
     * For a language like Java, just return Character.isJavaIdentifierPart(). For something like
     * Ruby, we also want to include "@" and "$" such that double clicking on a global variable
     * for example will include the global prefix "$".
     */
    boolean isIdentifierChar(char c);
    
    /**
     * <p>Return the Lexer Language associated with this scanner
     *</p>
     */
    @NonNull
    Language getLexerLanguage();
    
    /**
     * Return a set of file object folders for core libraries for this language that should be added
     * to the indexing and querying paths.
     */
    @NonNull
    Collection<FileObject> getCoreLibraries();

    /**
     * Display name for this language. This name should be localized since it can be shown to
     * the user (currently, it shows up in the Tasklist filter for example).
     */
    @NonNull
    String getDisplayName();
    
    /**
     * Return a preferred file extension for this language (if any -- may be null).
     * The extension should NOT include the separating dot. For example, for Java the preferred
     * file extension is "java", not ".java". 
     * 
     * Note also that registering a preferred extension will NOT automatically cause GSF to
     * identify files of that extension as belonging to GSF (or this language's mime type).
     * You still need a MIME resolver for that. This method is primarily used with some
     * older mechanisms in NetBeans (such as template creation) which is still file extension
     * oriented.
     */
    @CheckForNull
    String getPreferredExtension();
    
    /**
     * Get source types for files corresponding to this language. This is used by for example
     * the tasklist to locate source files in a project by using the 
     *   ProjectUtils.getSources(p).getSourceGroups(x)
     * mechanism.
     * <p>
     * The map corresponds to the project name mapped to the source group. If no mapping exists
     * for a target project type the infrastructure will use the generic source type.
     * <p>
     * As with #acceptQueryPath, this is just a temporary measure (i.e. NetBeans 6.1) to deal with shortcomings
     * in the path and project integration for GSF.
     */
    @NonNull
    Map<String,String> getSourceGroupNames();
}
