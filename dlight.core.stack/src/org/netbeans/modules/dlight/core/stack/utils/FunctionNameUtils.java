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

import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;

/**
 *
 * @author Alexander Simon
 */
public final class FunctionNameUtils {

    private FunctionNameUtils() {
    }

    public static SourceFileInfo getSourceFileInfo(String functionSignature) {
        int indexOf = functionSignature.lastIndexOf("+0x"); //NOI18N
        int indexOfFile = functionSignature.indexOf(":", indexOf); //NOI18N
        if (indexOfFile < 0) {
            return null;
        }
        String _file = functionSignature.substring(indexOfFile + 1);
        int index = _file.indexOf(":"); //NOI18N
        if (index < 0) {
            return new SourceFileInfo(_file, -1);
        }
        String fileName = _file.substring(0, index);
        String line = _file.substring(index + 1);
        int lineNumber = -1;
        try{
            lineNumber = Integer.valueOf(line);
        }catch(NumberFormatException e){
            //if exception is here means something is wrong with the parsing
            return null;
        }
        return new SourceFileInfo(fileName, Integer.valueOf(line), 0);
    }

    public static String getFunctionOffset(String functionSignature) {
        int indexOf = functionSignature.indexOf("+"); //NOI18N
        while (indexOf > 0) {
            if (functionSignature.length() > indexOf + 1
                    && functionSignature.charAt(indexOf + 1) == '0') { //NOI18N
                return functionSignature.substring(indexOf + 1);
            }
            indexOf = functionSignature.indexOf("+", indexOf + 1); //NOI18N
        }
        return null;
    }

    public static String getFunctionModule(final String functionSignature) {
        final int indexOf = functionSignature.indexOf('`');
        if (indexOf < 0) {
            return functionSignature;
        }

        String moduleName = functionSignature.substring(0, indexOf);
        //check if there is a + sign inside
        int indexOfPl = moduleName.indexOf("+"); //NOI18N
        while (indexOfPl > 0) {
            if (moduleName.length() > indexOfPl + 1
                    && moduleName.charAt(indexOfPl + 1) == '0') { //NOI18N
                return moduleName.substring(0, indexOfPl);
            }
            indexOfPl = moduleName.indexOf("+", indexOfPl + 1); //NOI18N
        }
        return moduleName;
    }

    public static String getFunctionModuleOffset(final String functionSignature) {
        final int indexOf = functionSignature.indexOf('`');
        if (indexOf < 0) {
            return null;
        }
        //check if there is a + sign inside

        String module = functionSignature.substring(0, indexOf);
        return getFunctionOffset(module);
    }

    /**
     * returns function qualified name
     * @param functionSignature is [type][*|&][space][q-name-namespace::][q-name-class::]name[(parameter-list)]+offset
     * @return [q-name-namespace::][q-name-class::]name
     */
    public static String getFunctionQName(String functionSignature) {
        int start = 0;
        int templateLevel = 0;
        boolean isOperator = false;
        int ssOpenmp = functionSignature.indexOf("_$"); // NOI18N
        if (ssOpenmp >= 0) {
            int j = functionSignature.indexOf('.');
            if (j > ssOpenmp) {
                functionSignature = functionSignature.substring(0, ssOpenmp) + functionSignature.substring(j + 1);
            }
        }
        //module name is included in the function
        if (functionSignature.indexOf("`") != -1) {//NOI18N
            start = functionSignature.indexOf("`") + 1;//NOI18N
        }
        for (int i = start ; i < functionSignature.length(); i++) {
            char c = functionSignature.charAt(i);
            switch (c) {
                case '<':
                    templateLevel++;
                    break;
                case '>':
                    templateLevel--;
                    break;
                case 'o':
                    if (functionSignature.substring(i).startsWith("operator") && // NOI18N
                            functionSignature.length() > i + 8
                            && !Character.isLetter(functionSignature.charAt(i + 8))) {
                        isOperator = true;
                    }
                    break;
                case '+':
                    if (functionSignature.length() > i + 1
                            && functionSignature.charAt(i + 1) == '0') {
                        return functionSignature.substring(start, i);
                    }
                    break;
                case '.':
                   if (functionSignature.indexOf("`") != -1 && functionSignature.indexOf("`") > i) {//NOI18N
                        break;
                    }
                    return functionSignature.substring(start, i);
                case ' ':
                    if (functionSignature.length() > i + 1
                            && functionSignature.charAt(i + 1) == '#') {
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
                    if (templateLevel == 0) {
                        if (!isOperator) {
                            start = i + 1;
                        }
                    }
                    break;
                case '(':
                    return functionSignature.substring(start, i);
                case ']':
                    if (isOperator && i > 1 && functionSignature.charAt(i-1) == '[') {
                        return functionSignature.substring(start, i+1);
                    }
                    return functionSignature.substring(start, i);
            }
        }
        return functionSignature.substring(start);
    }
    
    public static String getFullFunctionName(String functionSignature){
        return getFunctionModule(functionSignature) + "`" + getFunctionName(functionSignature); // NOI18N
    }

    public static String getFunctionName(String functionSignature) {
        int start = 0;
        int templateLevel = 0;
        boolean isOperator = false;
        int ssOpenmp = functionSignature.indexOf("_$"); // NOI18N
        if (ssOpenmp >= 0) {
            int j = functionSignature.indexOf('.');
            if (j > ssOpenmp) {
                functionSignature = functionSignature.substring(0, ssOpenmp) + functionSignature.substring(j + 1);
            }
        }
        if (functionSignature.indexOf("`") != -1) {//NOI18N
            start = functionSignature.indexOf("`") + 1;//NOI18N
        }
        for (int i = start; i < functionSignature.length(); i++) {
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
                            functionSignature.length() > i + 8
                            && !Character.isLetter(functionSignature.charAt(i + 8))) {
                        isOperator = true;
                    }
                    break;
                case '+':
                    if (functionSignature.length() > i + 1
                            && functionSignature.charAt(i + 1) == '0') {
                        return functionSignature.substring(start, i);
                    }
                    break;
                case '.':
                    if (functionSignature.indexOf("`") != -1 && functionSignature.indexOf("`") > i) {//NOI18N
                        break;
                    }
                    return functionSignature.substring(start, i);
                case ' ':
                    if (functionSignature.length() > i + 1
                            && functionSignature.charAt(i + 1) == '#') {
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
