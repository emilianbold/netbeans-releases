/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.makefile;

import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.Language;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.LanguagesManager;

/**
 * Helper class responsible parsing of Makefile macros.
 * Referenced from <code>Makefile.nbs</code>.
 *
 * @author Alexey Vladykin
 */
public class MakefileParserHelper {

    private static final String MIME_TYPE = "text/x-make"; // NOI18N
    private static final String MACRO_TOKEN = "macro"; // NOI18N
    private static final String ERROR_TOKEN = "error"; // NOI18N
    private static final String EMPTY_ID = ""; // NOI18N

    /**
     * Parses macro usage like
     * <code>$(wildcard $(addsuffix .d, ${OBJECTFILES}))</code>.
     *
     * @param input  character input with current index set at <code>$</code>
     * @return two-item array, where first item is parsed token
     *      (either <code>macro</code> or <code>error</code>),
     *      and second item is <code>null</code>.
     */
    public static Object[] parseMacro(CharInput input) {
        try {
            Language language = LanguagesManager.get().getLanguage(MIME_TYPE);
            if (readMacro(input)) {
                return new Object[] {
                    ASTToken.create(language, MACRO_TOKEN, EMPTY_ID, 0, 0, null),
                    null};
            } else {
                return new Object[] {
                    ASTToken.create(language, ERROR_TOKEN, EMPTY_ID, 0, 0, null),
                    null};
            }
        } catch (LanguageDefinitionNotFoundException e) {
            // should not happen
            e.printStackTrace();
            return null;
        }
    }

    private static boolean readMacro(CharInput input) {
        if (input.read() != '$') { // NOI18N
            return false;
        }
        while (!input.eof()) {
            switch (input.next()) {
                case '(': // NOI18N
                    return readTo(input, ')'); // NOI18N
                case '{': // NOI18N
                    return readTo(input, '}'); // NOI18N
                case ' ': // NOI18N
                case '\r': // NOI18N
                case '\n': // NOI18N
                    return false;
                default:
                    input.read();
                    return true;
            }
        }
        return false;
    }

    private static boolean readTo(CharInput input, char barrier) {
        while (!input.eof()) {
            switch (input.next()) {
                case '$': // NOI18N
                    if (!readMacro(input)) {
                        return false;
                    }
                    break;
                case '\\': // NOI18N
                    input.read();
                    input.read();
                    break;
                default:
                    if (input.read() == barrier) {
                        return true;
                    }
            }
        }
        return false;
    }

}
