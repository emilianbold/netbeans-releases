/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.formatter;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Petr Hejl
 */
public final class FormatToken {

    private final Kind kind;

    private final int offset;

    private final CharSequence text;

    private FormatToken next;

    private FormatToken(Kind kind, int offset, CharSequence text) {
        this.kind = kind;
        this.offset = offset;
        this.text = text;
    }

    public static FormatToken forText(int offset, CharSequence text) {
        return new FormatToken(Kind.TEXT, offset, text);
    }

    public static FormatToken forFormat(Kind kind) {
        return new FormatToken(kind, -1, null);
    }

    public static FormatToken forAny(Kind kind, int offset, CharSequence text) {
        return new FormatToken(kind, offset, text);
    }

    @NonNull
    public Kind getKind() {
        return kind;
    }

    @CheckForNull
    public CharSequence getText() {
        return text;
    }

    public int getOffset() {
        return offset;
    }

    @CheckForNull
    public FormatToken next() {
        return next;
    }

    public boolean isVirtual() {
        return offset < 0;
    }

    @Override
    public String toString() {
        return "FormattingToken{" + "kind=" + kind + ", offset=" + offset + ", text=" + text + '}';
    }

    void setNext(FormatToken next) {
        this.next = next;
    }

    public static enum Kind {
        SOURCE_START,
        TEXT,
        WHITESPACE,
        EOL,

        LINE_COMMENT,
        DOC_COMMENT,
        BLOCK_COMMENT,

        INDENTATION_INC,
        INDENTATION_DEC,

        AFTER_STATEMENT,
        AFTER_PROPERTY,
        AFTER_CASE,

        BEFORE_BINARY_OPERATOR,
        AFTER_BINARY_OPERATOR,

        BEFORE_ASSIGNMENT_OPERATOR,
        AFTER_ASSIGNMENT_OPERATOR,

        BEFORE_COMMA,
        AFTER_COMMA
    }

}
