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
package org.netbeans.modules.php.editor.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids for PHP Documentor Comments
 *
 * @author Petr Pisl
 */
public enum PHPDocCommentTokenId implements TokenId {
    PHPDOC_COMMENT(null, "comment"), //NOI18N
    PHPDOC_NAME("@name", "phpdockeyword"), //NOI18N
    PHPDOC_DESC("@desc", "phpdockeyword"), //NOI18N
    PHPDOC_TODO("@todo", "phpdockeyword"), //NOI18N
    PHPDOC_LINK("@link", "phpdockeyword"), //NOI18N
    PHPDOC_EXAMPLE("@example", "phpdockeyword"), //NOI18N
    PHPDOC_LICENSE("@license", "phpdockeyword"), //NOI18N
    PHPDOC_PACKAGE("@package", "phpdockeyword"), //NOI18N
    PHPDOC_VERSION("@version", "phpdockeyword"), //NOI18N
    PHPDOC_ABSTRACT("@abstract", "phpdockeyword"), //NOI18N
    PHPDOC_INTERNAL("@internal", "phpdockeyword"), //NOI18N
    PHPDOC_TUTORIAL("@tutorial", "phpdockeyword"), //NOI18N
    PHPDOC_METHOD("@method", "phpdockeyword"), //NOI18N
    PHPDOC_PROPERTY("@property", "phpdockeyword"), //NOI18N
    PHPDOC_PROPERTY_READ("@property-write", "phpdockeyword"), //NOI18N
    PHPDOC_PROPERTY_WRITE("@property-read", "phpdockeyword"), //NOI18N
    PHPDOC_USES("@uses", "phpdockeyword"), //NOI18N
    PHPDOC_CATEGORY("@category", "phpdockeyword"), //NOI18N
    PHPDOC_FINAL("@final", "phpdockeyword"), //NOI18N
    PHPDOC_SINCE("@since", "phpdockeyword"), //NOI18N
    PHPDOC_PARAM("@param", "phpdockeyword"), //NOI18N
    PHPDOC_MAGIC("@magic", "phpdockeyword"), //NOI18N
    PHPDOC_RETURN("@return", "phpdockeyword"), //NOI18N
    PHPDOC_AUTHOR("@author", "phpdockeyword"), //NOI18N
    PHPDOC_ACCESS("@access", "phpdockeyword"), //NOI18N
    PHPDOC_IGNORE("@ingnore", "phpdockeyword"), //NOI18N
    PHPDOC_THROWS("@throws", "phpdockeyword"), //NOI18N
    PHPDOC_STATIC("@static", "phpdockeyword"), //NOI18N
    PHPDOC_GLOBAL("@global", "phpdockeyword"), //NOI18N
    PHPDOC_SUBPACKAGE("@subpackage", "phpdockeyword"), //NOI18N
    PHPDOC_FILESOURCE("@filesource", "phpdockeyword"), //NOI18N
    PHPDOC_EXCEPTION("@exception", "phpdockeyword"), //NOI18N
    PHPDOC_COPYRIGHT("@copyright", "phpdockeyword"), //NOI18N
    PHPDOC_STATICVAR("@static", "phpdockeyword"), //NOI18N
    PHPDOC_VAR("@var", "phpdockeyword"), //NOI18N
    PHPDOC_SEE("@see", "phpdockeyword"), //NOI18N
    PHPDOC_DEPRECATED("@deprecated", "phpdockeyword"); //NOI18N

    private final String fixedText;
    private final String primaryCategory;

    PHPDocCommentTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }
    
    public String primaryCategory() {
        return primaryCategory;
    }
    
    public String fixedText() {
        return fixedText;
    }

    private static final Language<PHPDocCommentTokenId> language =
        new LanguageHierarchy<PHPDocCommentTokenId>() {
                @Override
                protected Collection<PHPDocCommentTokenId> createTokenIds() {
                    return EnumSet.allOf(PHPDocCommentTokenId.class);
                }

                @Override
                protected Map<String, Collection<PHPDocCommentTokenId>> createTokenCategories() {
                    return null; // no extra categories
                }

                @Override
                protected Lexer<PHPDocCommentTokenId> createLexer(
                    LexerRestartInfo<PHPDocCommentTokenId> info) {
                    return new PHPDocCommentLexer(info);
                }

                @Override
                public String mimeType() {
                    return "text/x-php-doccomment";
                }
            }.language();

    public static Language<PHPDocCommentTokenId> language() {
        return language;
    }
}
