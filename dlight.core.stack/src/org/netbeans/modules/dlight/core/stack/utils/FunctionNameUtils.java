/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.core.stack.utils;

/**
 *
 * @author Alexander Simon
 */
public final class FunctionNameUtils {

    private FunctionNameUtils() {
    }

    /**
     * returns function quilified name
     * @param functionSignature is [type][*|&][space][q-name-namspace::][q-name-class::]name[(prameter-list)]+offset
     * @return [q-name-namspace::][q-name-class::]name
     */
    public static String getFunctionQName(String functionSignature) {
        int start = 0;
        int templateLevel = 0;
        boolean isOperator = false;
        int ssOpenmp = functionSignature.indexOf("_$"); // NOI18N
        if (ssOpenmp >=0 ) {
            int j = functionSignature.indexOf('.');
            if (j > ssOpenmp) {
                functionSignature = functionSignature.substring(0, ssOpenmp)+functionSignature.substring(j+1);
            }
        }
        for (int i = 0; i < functionSignature.length(); i++) {
            char c = functionSignature.charAt(i);
            switch (c) {
                case '<':
                    templateLevel++;
                    break;
                case '>':
                    templateLevel++;
                    break;
                case 'o':
                    if (functionSignature.substring(i).startsWith("operator") && // NOI18N
                            functionSignature.length() > i + 8 &&
                            !Character.isLetter(functionSignature.charAt(i + 8))) {
                        isOperator = true;
                    }
                    break;
                case '+':
                    if (functionSignature.length() > i+1 &&
                        functionSignature.charAt(i + 1) == '0') {
                        return functionSignature.substring(start, i);
                   }
                   break;
                case '.':
                    return functionSignature.substring(start, i);
                case ' ':
                    if (functionSignature.length() > i+1 &&
                        functionSignature.charAt(i + 1) == '#') {
                        return functionSignature.substring(start, i);
                    }
                    if (templateLevel == 0) {
                        if (!isOperator) {
                            start = i + 1;
                        }
                    }
                    break;
                case '*':
                case '&':
                    if (templateLevel == 0) {
                        if (!isOperator) {
                            start = i + 1;
                        }
                    }
                    break;
                case '[':
                    start = i + 1;
                    break;
                case '(':
                case ']':
                    return functionSignature.substring(start, i);
            }
        }
        return functionSignature.substring(start);
    }
}
