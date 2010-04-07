/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.search.api;

import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.20
 */
public final class SearchPattern {

    public SearchPattern(String text, SearchMatch match, boolean caseSensitive) {
//out();
//out("===    INIT: '" + text + "' " + match);
        myText = text;
        mySearchMatch = match;
        myCaseSensitive = caseSensitive;

        if (mySearchMatch == SearchMatch.PATTERN) {
            myText = replace(myText);
//out("===    repl: '" + myText + "'");
        }
        if (mySearchMatch == SearchMatch.PATTERN || mySearchMatch == SearchMatch.REGULAR_EXPRESSION) {
            int flags = 0;

            if (!myCaseSensitive) {
                flags |= java.util.regex.Pattern.CASE_INSENSITIVE;
            }
            myPattern = java.util.regex.Pattern.compile(myText, flags);
        }
    }

    public boolean accepts(String value) {
//out("=== ACCEPTS: '" + value + "'");
        if (value == null) {
            return myText == null;
        }
        if (myText == null) {
            return value == null;
        }
        if (mySearchMatch == SearchMatch.PATTERN || mySearchMatch == SearchMatch.REGULAR_EXPRESSION) {
//out("=== return: '" + regexp(value));
            return regexp(value);
        }
        return text(value);
    }

    private boolean regexp(String value) {
        return myPattern.matcher(value).find();
    }

    private boolean text(String value) {
        if (myCaseSensitive) {
            return myText.equals(value);
        }
        return myText.equalsIgnoreCase(value);
    }

    private String replace(String value) {
        StringBuilder builder = new StringBuilder("^"); // NOI18N

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);

            if ("$^.+(){}[]|\\".indexOf(ch) != -1) { // NOI18N
                builder.append("\\" + ch); // NOI18N
            } else if (ch == '?') {
                builder.append(".");
            } else if (ch == '*') {
                builder.append(".*"); // NOI18N
            } else {
                builder.append(ch);
            }
        }
        builder.append('$');

        return builder.toString();
    }

    private String myText;
    private boolean myCaseSensitive;
    private SearchMatch mySearchMatch;
    private java.util.regex.Pattern myPattern;
}
