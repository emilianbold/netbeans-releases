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
package org.netbeans.modules.javascript2.editor.jsdoc.model;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.javascript2.editor.model.Type;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.javascript2.editor.model.impl.IdentifierImpl;
import org.netbeans.modules.javascript2.editor.model.impl.TypeUsageImpl;

/**
 * Contains helper classes for work with jsDoc model.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocElementUtils {

    private static final String LOWERCASED_NUMBER = Type.NUMBER.toLowerCase();
    private static final String LOWERCASED_STRING = Type.STRING.toLowerCase();
    private static final String LOWERCASED_BOOLEAN = Type.BOOLEAN.toLowerCase();

    /** Limit for number of spaces inside the type declaration. */
    public static final int LIMIT_SPACES_IN_TYPE = 10;

    /**
     * Creates element of correct type for given type and remaining element text.
     * @param type element type
     * @param tagDescription tag description text - without the initial type and first spaces, can be empty but never {@code null}
     * @param descBeginOffset type description text start offset
     * @return created {@code JsDocElement)
     */
    public static JsDocElement createElementForType(JsDocElementType type, String tagDescription, int descStartOffset) {
        switch (type.getCategory()) {
            case ASSIGN:
                String[] values = tagDescription.split("(\\s)*as(\\s)*"); //NOI18N
                return AssignElement.create(
                        type,
                        (values.length > 0) ? new NamePath(values[0].trim()) : null,
                        (values.length > 1) ? new NamePath(values[1].trim()) : null);
            case DECLARATION:
                return createDeclarationElement(type, tagDescription, descStartOffset);
            case DESCRIPTION:
                return DescriptionElement.create(type, tagDescription);
            case LINK:
                return LinkElement.create(type, new NamePath(tagDescription));
            case NAMED_PARAMETER:
                return createParameterElement(type, tagDescription, descStartOffset);
            case SIMPLE:
                return SimpleElement.create(type);
            case UNNAMED_PARAMETER:
                return createParameterElement(type, tagDescription, descStartOffset);
            default:
                // unknown jsDoc element type
                return DescriptionElement.create(type, tagDescription);
        }
    }

    /**
     * Gets list of {@link Type}s parsed from given string.
     * @param textToParse string to be parsed for types
     * @param offset offset of the textToParse in the file
     * @return list of {@code Type}s
     */
    public static List<Type> parseTypes(String textToParse, int offset) {
        List<Type> types = new LinkedList<Type>();
        String[] typesArray = textToParse.split("[|]"); //NOI18N
        for (String string : typesArray) {
            if (!string.trim().isEmpty()) {
                types.add(createTypeUsage(string, offset + textToParse.indexOf(string)));
            }
        }
        return types;
    }

    private static DeclarationElement createDeclarationElement(JsDocElementType elementType, String elementText, int descStartOffset) {
        String type = elementText;
        int typeOffset = descStartOffset + (elementText.indexOf("{") == -1 ? 0 : elementText.indexOf("{") + 1); //NOI18N
        if (typeOffset > 0 && elementText.endsWith("}")) { //NOI18N
            type = type.substring(1, type.length() - 1);
        }
        return DeclarationElement.create(elementType, createTypeUsage(type, typeOffset));
    }

    private static TypeUsage createTypeUsage(String type, int offset) {
        // see issue #233176
        if (LOWERCASED_STRING.equals(type)) {
            return new TypeUsageImpl(Type.STRING, offset);
        } else if (LOWERCASED_NUMBER.equals(type)) {
            return new TypeUsageImpl(Type.NUMBER, offset);
        } else if (LOWERCASED_BOOLEAN.equals(type)) {
            return new TypeUsageImpl(Type.BOOLEAN, offset);
        } else {
            return new TypeUsageImpl(type, offset);
        }
    }

    private static ParameterElement createParameterElement(JsDocElementType elementType,
            String elementText, int descStartOffset) {
        int typeOffset = -1, nameOffset = -1;
        String types = "", desc = ""; //NOI18N
        StringBuilder name = new StringBuilder();
        int process = 0;
        String[] parts = elementText.split("[\\s]+"); //NOI18N

        if (parts.length > process) {
            // get type value if any
            if (parts[0].startsWith("{")) { //NOI18N
                typeOffset = descStartOffset + 1;
                int rparIndex = parts[0].indexOf("}"); //NOI18N
                if (rparIndex == -1) {
                    // issue #224205 - search for ending } curly braces, spaces in its type
                    int actualProcessed = process;
                    StringBuilder typesSB = new StringBuilder(parts[0].substring(1));
                    while ((actualProcessed < parts.length - 1)  && (actualProcessed < LIMIT_SPACES_IN_TYPE)) {
                       actualProcessed++;
                       rparIndex = parts[actualProcessed].indexOf("}"); //NOI18N
                       if (rparIndex != -1) {
                           typesSB.append(parts[actualProcessed].substring(0, rparIndex));
                           process = actualProcessed;
                           types = typesSB.toString();
                           break;
                       } else {
                           typesSB.append(parts[actualProcessed]);
                       }
                    }
                    if (types.isEmpty()) {
                        types = parts[0].trim();
                    }
                } else {
                    types = parts[0].substring(1, rparIndex);
                }
                process++;
            }

            // get name value (mandatory part)
            if (parts.length > process && elementType.getCategory() == JsDocElement.Category.NAMED_PARAMETER) {
                nameOffset = descStartOffset + elementText.indexOf(parts[process], types.length());
                name.append(parts[process].trim());
                process++;
                if (name.toString().contains("\"") || name.toString().contains("'")) { //NOI18N
                    process = buildNameForString(name, process, parts);
                }
            }

            // get description
            StringBuilder sb = new StringBuilder();
            while (process < parts.length) {
                sb.append(parts[process]).append(" "); //NOI18N
                process++;
            }
            desc = sb.toString().trim();
        }

        if (elementType.getCategory() == JsDocElement.Category.NAMED_PARAMETER) {
            return NamedParameterElement.createWithNameDiagnostics(elementType,
                    new IdentifierImpl(name.toString(), nameOffset), parseTypes(types, typeOffset), desc);
        } else {
            return UnnamedParameterElement.create(elementType, parseTypes(types, typeOffset), desc);
        }
    }

    private static int buildNameForString(StringBuilder name, int currentOffset, String[] parts) {
        // TODO - better would be to solve that using lexer
        String nameString = name.toString();
        if ((nameString.indexOf("\"") != -1 && (nameString.indexOf("\"") == nameString.lastIndexOf("\""))) //NOI18N
                || (nameString.indexOf("'") != -1 && nameString.indexOf("'") == nameString.lastIndexOf("'"))) { //NOI18N
            // string with spaces
            boolean endOfString = false;
            while (currentOffset < parts.length && !endOfString) {
                name.append(" ").append(parts[currentOffset]); //NOI18N
                if (parts[currentOffset].contains("\"") || parts[currentOffset].contains("'")) { //NOI18H
                    endOfString = true;
                }
                currentOffset++;
            }
        }
        return currentOffset;
    }

}
