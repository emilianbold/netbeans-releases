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
package org.netbeans.modules.php.zend2.util;

import java.io.File;
import java.util.regex.Pattern;

public final class Zend2Utils {

    private static final String FILE_VIEW_EXTENSION = ".phtml"; // NOI18N
    private static final String VIEW_DIRECTORY = "view"; // NOI18N

    private static final String DASH = "-"; // NOI18N


    private Zend2Utils() {
    }

    public static boolean isView(File file) {
        if (file == null) {
            return false;
        }
        if (!file.isFile() || !file.getName().endsWith(FILE_VIEW_EXTENSION)) {
            return false;
        }
        File parent = file.getParentFile(); // controller
        if (parent == null) {
            return false;
        }
        parent = parent.getParentFile(); // module
        if (parent == null) {
            return false;
        }
        parent = parent.getParentFile(); // view
        return VIEW_DIRECTORY.equals(parent.getName());
    }

    // AllJobs -> all-jobs
    static String dashize(String input) {
        StringBuilder sb = new StringBuilder(2 * input.length());
        for (int i = 0; i < input.length(); ++i) {
            char ch = input.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i != 0) {
                    sb.append(DASH);
                }
                sb.append(Character.toLowerCase(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    // all-jobs -> AllJobs or allJobs
    static String undashize(String input, boolean firstLowerCase) {
        StringBuilder sb = new StringBuilder(input.length());
        boolean first = firstLowerCase;
        for (String part : input.split(Pattern.quote(DASH))) {
            if (first) {
                first = false;
                sb.append(part);
            } else {
                sb.append(part.substring(0, 1).toUpperCase());
                sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

}
