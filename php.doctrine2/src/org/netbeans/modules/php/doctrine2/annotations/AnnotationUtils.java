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
package org.netbeans.modules.php.doctrine2.annotations;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.csl.api.OffsetRange;
import org.openide.util.Parameters;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class AnnotationUtils {

    private static final Pattern PARAM_TYPE_PATTERN = Pattern.compile("=\\s*\\\"\\s*([\\w\\\\]+)\\s*\\\""); //NOI18N

    private static final Pattern INLINE_TYPE_PATTERN = Pattern.compile("@([\\w\\\\]+)"); //NOI18N

    private AnnotationUtils() {
    }

    public static boolean isTypeAnnotation(final String lineToCheck, final String annotationName) {
        Parameters.notNull("lineToCheck", lineToCheck); //NOI18N
        Parameters.notNull("annotationName", annotationName); //NOI18N
        return lineToCheck.toLowerCase().matches("\\\\?(\\w+\\\\)*" + annotationName.toLowerCase() + "\\s*"); //NOI18N
    }

    public static Map<OffsetRange, String> extractTypesFromParameters(final String line) {
        Parameters.notNull("line", line); //NOI18N
        final Map<OffsetRange, String> result = new HashMap<OffsetRange, String>();
        final Matcher matcher = PARAM_TYPE_PATTERN.matcher(line);
        while (matcher.find()) {
            result.put(new OffsetRange(matcher.start(1), matcher.end(1)), matcher.group(1));
        }
        return result;
    }

    public static Map<OffsetRange, String> extractInlineTypes(final String line) {
        Parameters.notNull("line", line); //NOI18N
        final Map<OffsetRange, String> result = new HashMap<OffsetRange, String>();
        final Matcher matcher = INLINE_TYPE_PATTERN.matcher(line);
        while (matcher.find()) {
            result.put(new OffsetRange(matcher.start(1), matcher.end(1)), matcher.group(1));
        }
        return result;
    }

}
