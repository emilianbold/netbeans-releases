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
package org.netbeans.modules.javascript2.editor.doclets.model;

import org.netbeans.modules.javascript2.editor.doclets.model.el.Description;
import org.netbeans.modules.javascript2.editor.doclets.model.el.Name;
import org.netbeans.modules.javascript2.editor.doclets.model.el.NamePath;
import org.netbeans.modules.javascript2.editor.doclets.model.el.Type;

/**
 * Contains helper classes for work with jsDoc model.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocElementUtils {

    /**
     * Creates element of correct type for given type and remaining element text.
     * @param type element type
     * @param elementText element text - without the initial type
     * @return created {@code JsDocElement)
     */
    public static JsDocElement createElementForType(JsDocElement.Type type, String elementText) {
        String trimmed = elementText.trim();
        switch (type.getCategory()) {
            case ASSIGN:
                String[] values = trimmed.split("(\\s)*as(\\s)*"); //NOI18N
                return new AssignElement(
                        type,
                        (values.length > 0) ? new NamePath(values[0].trim()) : null,
                        (values.length > 2) ? new NamePath(values[2].trim()) : null);
            case DECLARATION:
                return new DeclarationElement(type, new Type(trimmed));
            case DESCRIPTION:
                return new DescriptionElement(type, new Description(trimmed));
            case LINK:
                return new LinkElement(type, new NamePath(trimmed));
            case NAMED_PARAMETER:
                return createParameterElement(type, trimmed, true);
            case SIMPLE:
                return new SimpleElement(type);
            case UNNAMED_PARAMETER:
                return createParameterElement(type, trimmed, false);
            default:
                // unknown jsDoc element type
                return new DescriptionElement(type, new Description(trimmed));
        }
    }

    private static ParameterElement createParameterElement(JsDocElement.Type elementType,
            String elementText, boolean named) {
        String type = "", name = "", desc = ""; //NOI18N
        int process = 0;
        String[] parts = elementText.split("(\\s)*"); //NOI18N

        if (parts.length > process) {
            // get type value if any
            if (parts[0].startsWith("{")) { //NOI18N
                int rparIndex = parts[0].indexOf("}"); //NOI18N
                if (rparIndex == -1) {
                    type = parts[0].trim();
                } else {
                    type = parts[0].substring(1, rparIndex);
                }
                process++;
            }

            // get name value (mandatory part)
            if (parts.length > process && named) {
                name = parts[process].trim();
                process++;
            }

            // get description
            StringBuilder sb = new StringBuilder();
            while (process < parts.length) {
                sb.append(parts[process]).append(" "); //NOI18N
                process++;
            }
            desc = sb.toString().trim();
        }

        if (named) {
            return new NamedParameterElement(elementType, new Name(name), new Type(type), new Description(desc));
        } else {
            return new UnnamedParameterElement(elementType, new Type(type), new Description(desc));
        }
    }

}
