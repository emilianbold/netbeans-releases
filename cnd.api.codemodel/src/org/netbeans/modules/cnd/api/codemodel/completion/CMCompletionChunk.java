/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.codemodel.completion;

import org.netbeans.modules.cnd.spi.codemodel.completion.CMCompletionChunkImplementation;
import org.netbeans.modules.cnd.spi.codemodel.support.IterableFactory;

/**
 * @see Kind documentation
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public final class CMCompletionChunk {

    /**
     * \brief Retrieve the text associated with a particular chunk within a
     * completion string.
     *
     * @return the text associated with the chunk
     */
    public CharSequence getText() {
        return impl.getText();
    }

    /**
     * \brief Retrieve embedded chunks (if any) associated with a particular
     * chunk within a completion string (it is applicable only for Optional
     * chunk).
     *
     * @return the chunk list associated with the chunk
     */
    public Iterable<CMCompletionChunk> getEmbedded() {
        return fromImpls(impl.getEmbedded());
    }

    /**
     * \brief Determine the kind of a particular chunk within a completion
     * string.
     *
     * @return the kind of the chunk
     */
    public Kind getKind() {
        return impl.getKind();
    }

    /**
     * \brief Describes a kind of single piece of text within a code-completion
     * string.
     *
     * Each "chunk" within a code-completion string (\c CXCompletionString) is
     * either a piece of text with a specific "kind" that describes how that
     * text should be interpreted by the client or is another completion string.
     */
    public enum Kind {

        Invalid(-1),
        /**
         * \brief A code-completion string that describes "optional" text that
         * could be a part of the template (but is not required).
         *
         * The Optional chunk is the only kind of chunk that has a
         * code-completion string for its representation, which is accessible
         * via \c clang_getCompletionChunkCompletionString(). The
         * code-completion string describes an additional part of the template
         * that is completely optional. For example, optional chunks can be used
         * to describe the placeholders for arguments that match up with
         * defaulted function parameters, e.g. given:
         *
         * \code void f(int x, float y = 3.14, double z = 2.71828); \endcode
         *
         * The code-completion string for this function would contain: - a
         * TypedText chunk for "f". - a LeftParen chunk for "(". - a Placeholder
         * chunk for "int x" - an Optional chunk containing the remaining
         * defaulted arguments, e.g., - a Comma chunk for "," - a Placeholder
         * chunk for "float y" - an Optional chunk containing the last defaulted
         * argument: - a Comma chunk for "," - a Placeholder chunk for "double
         * z" - a RightParen chunk for ")"
         *
         * There are many ways to handle Optional chunks. Two simple approaches
         * are: - Completely ignore optional chunks, in which case the template
         * for the function "f" would only include the first parameter ("int
         * x"). - Fully expand all optional chunks, in which case the template
         * for the function "f" would have all of the parameters.
         */
        Optional(0),
        /**
         * \brief Text that a user would be expected to type to get this
         * code-completion result.
         *
         * There will be exactly one "typed text" chunk in a semantic string,
         * which will typically provide the spelling of a keyword or the name of
         * a declaration that could be used at the current code point. Clients
         * are expected to filter the code-completion results based on the text
         * in this chunk.
         */
        TypedText(1),
        /**
         * \brief Text that should be inserted as part of a code-completion
         * result.
         *
         * A "text" chunk represents text that is part of the template to be
         * inserted into user code should this particular code-completion result
         * be selected.
         */
        Text(2),
        /**
         * \brief Placeholder text that should be replaced by the user.
         *
         * A "placeholder" chunk marks a place where the user should insert text
         * into the code-completion template. For example, placeholders might
         * mark the function parameters for a function declaration, to indicate
         * that the user should provide arguments for each of those parameters.
         * The actual text in a placeholder is a suggestion for the text to
         * display before the user replaces the placeholder with real code.
         */
        Placeholder(3),
        /**
         * \brief Informative text that should be displayed but never inserted
         * as part of the template.
         *
         * An "informative" chunk contains annotations that can be displayed to
         * help the user decide whether a particular code-completion result is
         * the right option, but which is not part of the actual template to be
         * inserted by code completion.
         */
        Informative(4),
        /**
         * \brief Text that describes the current parameter when code-completion
         * is referring to function call, message send, or template
         * specialization.
         *
         * A "current parameter" chunk occurs when code-completion is providing
         * information about a parameter corresponding to the argument at the
         * code-completion point. For example, given a function
         *
         * \code int add(int x, int y); \endcode
         *
         * and the source code \c add(, where the code-completion point is after
         * the "(", the code-completion string will contain a "current
         * parameter" chunk for "int x", indicating that the current argument
         * will initialize that parameter. After typing further, to \c add(17,
         * (where the code-completion point is after the ","), the
         * code-completion string will contain a "current paremeter" chunk to
         * "int y".
         */
        CurrentParameter(5),
        /**
         * \brief A left parenthesis ('('), used to initiate a function call or
         * signal the beginning of a function parameter list.
         */
        LeftParen(6),
        /**
         * \brief A right parenthesis (')'), used to finish a function call or
         * signal the end of a function parameter list.
         */
        RightParen(7),
        /**
         * \brief A left bracket ('[').
         */
        LeftBracket(8),
        /**
         * \brief A right bracket (']').
         */
        RightBracket(9),
        /**
         * \brief A left brace ('{').
         */
        LeftBrace(10),
        /**
         * \brief A right brace ('}').
         */
        RightBrace(11),
        /**
         * \brief A left angle bracket ('<').
         */
        LeftAngle(12),
        /**
         * \brief A right angle bracket ('>').
         */
        RightAngle(13),
        /**
         * \brief A comma separator (',').
         */
        Comma(14),
        /**
         * \brief Text that specifies the result type of a given result.
         *
         * This special kind of informative chunk is not meant to be inserted
         * into the text buffer. Rather, it is meant to illustrate the type that
         * an expression using the given completion string would have.
         */
        ResultType(15),
        /**
         * \brief A colon (':').
         */
        Colon(16),
        /**
         * \brief A semicolon (';').
         */
        SemiColon(17),
        /**
         * \brief An '=' sign.
         */
        Equal(18),
        /**
         * Horizontal space (' ').
         */
        HorizontalSpace(19),
        /**
         * Vertical space ('\n'), after which it is generally a good idea to
         * perform indentation.
         */
        VerticalSpace(20);

        //<editor-fold defaultstate="collapsed" desc="hidden">
        public static Kind valueOf(int val) {
            byte curVal = (byte) val;
            for (Kind kind : Kind.values()) {
                if (kind.val == curVal) {
                    return kind;
                }
            }
            assert false : "unsupported value " + val;
            return Invalid;
        }

        private final byte val;

        private Kind(int lang) {
            this.val = (byte) lang;
        }

        public int getValue() {
            return val;
        }
        //</editor-fold>
    }

    //<editor-fold defaultstate="collapsed" desc="hidden">
    private final CMCompletionChunkImplementation impl;

    private CMCompletionChunk(CMCompletionChunkImplementation impl) {
        this.impl = impl;
    }

    /*package*/
    static CMCompletionChunk fromImpl(CMCompletionChunkImplementation impl) {
        // TODO: share instance for the same impl if needed
        return new CMCompletionChunk(impl);
    }

    /*package*/
    static Iterable<CMCompletionChunk> fromImpls(Iterable<CMCompletionChunkImplementation> impls) {
        return IterableFactory.convert(impls, CONV);
    }

    /*package*/
    CMCompletionChunkImplementation getImpl() {
        return impl;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.impl.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CMCompletionChunk) {
            return this.impl.equals(((CMCompletionChunk) obj).impl);
        }
        return false;
    }

    @Override
    public String toString() {
        return "CMCompletionChunk{" + impl + '}'; // NOI18N
    }

    private static final IterableFactory.Converter<CMCompletionChunkImplementation, CMCompletionChunk> CONV
            = new IterableFactory.Converter<CMCompletionChunkImplementation, CMCompletionChunk>() {

                @Override
                public CMCompletionChunk convert(CMCompletionChunkImplementation in) {
                    return fromImpl(in);
                }
            };
    //</editor-fold>
}
