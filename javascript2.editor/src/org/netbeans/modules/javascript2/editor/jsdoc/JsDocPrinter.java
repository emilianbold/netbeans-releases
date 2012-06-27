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
package org.netbeans.modules.javascript2.editor.jsdoc;

import java.util.List;
import org.netbeans.modules.javascript2.editor.jsdoc.model.DescriptionElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.JsDocElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.NamedParameterElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.ParameterElement;
import org.netbeans.modules.javascript2.editor.model.Type;

/**
 * Contains method for printing documentation entries.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocPrinter {

//    private static final String WRAPPER_ELEMENT = "b";
    private static final String WRAPPER_HEADER = "h3";

    /**
     * Prints documentation of CC doc window.
     * @param jsDocBlock docBlock
     * @return formated documentation
     */
    public static String printDocumentation(JsDocBlock jsDocBlock) {
        StringBuilder sb = new StringBuilder();

//        sb.append(printElementName(jsDocBlock));
        sb.append(printSyntax(jsDocBlock));
        sb.append(printSummary(jsDocBlock));
        sb.append(printParameters(jsDocBlock));
        sb.append(printReturns(jsDocBlock));

        return sb.toString();
    }

//    private static String printElementName(JsDocBlock jsDocBlock) {
//        jsBuilder.appendComment("<" + WRAPPER_ELEMENT + ">" + jsDocBlock.getName() + "</" + WRAPPER_ELEMENT + ">");
//    }

    private static String printSyntax(JsDocBlock jsDocBlock) {
        List<? extends JsDocElement> syntax = jsDocBlock.getTagsForType(JsDocElement.Type.SYNTAX);
        if (!syntax.isEmpty()) {
            StringBuilder sb = new StringBuilder("<p style=\"background-color: #E1FEFF; width: 100%; padding: 5px; margin: 5 5 0 5; border: 2px black dashed;\">");
            for (JsDocElement jsDocElement : syntax) {
                sb.append(((DescriptionElement) jsDocElement).getDescription());
            }
            sb.append("</p>");
            return sb.toString();
        }
        return "";
    }

    private static String printSummary(JsDocBlock jsDocBlock) {
        List<? extends JsDocElement> summary = jsDocBlock.getTagsForTypes(
                new JsDocElement.Type[]{JsDocElement.Type.DESCRIPTION, JsDocElement.Type.CONTEXT_SENSITIVE});
        if (!summary.isEmpty()) {
            StringBuilder sb = new StringBuilder("<p>");
            for (JsDocElement jsDocElement : summary) {
                sb.append(((DescriptionElement) jsDocElement).getDescription());
            }
            sb.append("</p>");
            return sb.toString();
        }
        return "";
    }

    private static String printParameters(JsDocBlock jsDocBlock) {
        List<? extends JsDocElement> parameters = jsDocBlock.getTagsForTypes(
                new JsDocElement.Type[]{JsDocElement.Type.PARAM, JsDocElement.Type.ARGUMENT});
        if (!parameters.isEmpty()) {
            StringBuilder sb = new StringBuilder("<" + WRAPPER_HEADER + ">Parameters:</" + WRAPPER_HEADER + ">");
            sb.append("<table style=\"margin-left:10px;\">");
            for (JsDocElement jsDocElement : parameters) {
                NamedParameterElement namedParam = (NamedParameterElement) jsDocElement;
                sb.append("<tr>");
                sb.append("<td valign=\"top\"><i>").append(getStringFromTypes(namedParam.getParamTypes())).append("</i></td>");
                sb.append("<td valign=\"top\"><b>").append(namedParam.getParamName().getName()).append("</b></td>");
                sb.append("<td>").append(namedParam.getParamDescription()).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            return sb.toString();
        }
        return "";
    }

    private static String printReturns(JsDocBlock jsDocBlock) {
        List<? extends JsDocElement> parameters = jsDocBlock.getTagsForTypes(
                new JsDocElement.Type[]{JsDocElement.Type.RETURN, JsDocElement.Type.RETURNS});
        if (!parameters.isEmpty()) {
            StringBuilder sb = new StringBuilder("<" + WRAPPER_HEADER + ">Returns:</" + WRAPPER_HEADER + ">");
            sb.append("<table style=\"margin-left:10px;\">");
            for (JsDocElement jsDocElement : parameters) {
                ParameterElement param = (ParameterElement) jsDocElement;
                sb.append("<tr>");
                sb.append("<td valign=\"top\"><i>").append(getStringFromTypes(param.getParamTypes())).append("</i></td>");
                sb.append("<td>").append(param.getParamDescription()).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            return sb.toString();
        }
        return "";
    }

    private static String getStringFromTypes(List<Type> types) {
        StringBuilder sb = new StringBuilder();
        String delimiter = ""; //NOI18N
        for (Type type : types) {
            sb.append(delimiter).append(type.getType());
            delimiter = "|"; //NOI18N
        }
        return sb.toString();
    }

}
