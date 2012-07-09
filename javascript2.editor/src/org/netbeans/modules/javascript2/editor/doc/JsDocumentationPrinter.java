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
package org.netbeans.modules.javascript2.editor.doc;

import java.util.List;
import org.netbeans.modules.javascript2.editor.doc.spi.DocParameter;
import org.netbeans.modules.javascript2.editor.doc.spi.JsComment;
import org.netbeans.modules.javascript2.editor.model.Type;

/**
 * Contains method for printing documentation entries.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocumentationPrinter {

    private static final String WRAPPER_HEADER = "h3"; //NOI18N

    /**
     * Prints documentation for CC doc window.
     * @param jsComment docBlock
     * @return formated documentation
     */
    public static String printDocumentation(JsComment jsComment) {
        StringBuilder sb = new StringBuilder();

        sb.append(printSyntax(jsComment));
        sb.append(printSummary(jsComment));
        sb.append(printParameters(jsComment));
        sb.append(printReturns(jsComment));

        return sb.toString();
    }

    private static String printSyntax(JsComment jsComment) {
        List<String> syntax = jsComment.getSyntax();
        if (!syntax.isEmpty()) {
            StringBuilder sb = new StringBuilder("<p style=\"background-color: #C7C7C7; width: 100%; padding: 5px; margin: 10 5 5 5;\">"); //NOI18N
            for (String descElement : syntax) {
                sb.append(descElement).append("<br>"); //NOI18N
            }
            sb.append("</p>"); //NOI18N
            return sb.toString();
        }
        return ""; //NOI18N
    }

    private static String printSummary(JsComment jsComment) {
        List<String> summary = jsComment.getSummary();
        if (!summary.isEmpty()) {
            StringBuilder sb = new StringBuilder("<p style=\"margin: 5 5 5 5\">"); //NOI18N
            for (String descElement : summary) {
                sb.append(descElement);
            }
            sb.append("</p>"); //NOI18N
            return sb.toString();
        }
        return ""; //NOI18N
    }

    private static String printParameters(JsComment jsComment) {
        List<DocParameter> parameters = jsComment.getParameters();
        if (!parameters.isEmpty()) {
            StringBuilder sb = new StringBuilder("<" + WRAPPER_HEADER + ">Parameters:</" + WRAPPER_HEADER + ">"); //NOI18N
            sb.append("<table style=\"margin-left:10px;\">"); //NOI18N
            for (DocParameter docParam : parameters) {
                sb.append("<tr>"); //NOI18N
                sb.append("<td valign=\"top\" style=\"margin-right:5px;\">").append(getStringFromTypes(docParam.getParamTypes())).append("</td>"); //NOI18N
                sb.append("<td valign=\"top\" style=\"margin-right:5px;\"><b>").append(docParam.getParamName().getName()).append("</b></td>"); //NOI18N
                sb.append("<td>").append(docParam.getParamDescription()).append("</td>"); //NOI18N
                sb.append("</tr>"); //NOI18N
            }
            sb.append("</table>"); //NOI18N
            return sb.toString();
        }
        return ""; //NOI18N
    }

    private static String printReturns(JsComment jsComment) {
        DocParameter returns = jsComment.getReturnType();
        if (returns != null) {
            StringBuilder sb = new StringBuilder("<" + WRAPPER_HEADER + ">Returns:</" + WRAPPER_HEADER + ">"); //NOI18N
            sb.append("<table style=\"margin-left:10px;\">"); //NOI18N
            if (!returns.getParamTypes().isEmpty()) {
                sb.append("<tr>"); //NOI18N
                sb.append("<td valign=\"top\" style=\"margin-right:5px;\"><b>Type:</b></td>"); //NOI18N
                sb.append("<td valign=\"top\">").append(getStringFromTypes(returns.getParamTypes())).append("</td>"); //NOI18N
                sb.append("</tr>"); //NOI18N
            }
            if (!returns.getParamDescription().isEmpty()) {
                sb.append("<tr>"); //NOI18N
                sb.append("<td valign=\"top\" style=\"margin-right:5px;\"><b>Description:</b></td>"); //NOI18N
                sb.append("<td valign=\"top\">").append(returns.getParamDescription()).append("</td>"); //NOI18N
                sb.append("</tr>"); //NOI18N
            }
            sb.append("</table>");
            return sb.toString();
        }
        return "";
    }

    private static String getStringFromTypes(List<? extends Type> types) {
        StringBuilder sb = new StringBuilder();
        String delimiter = ""; //NOI18N
        for (Type type : types) {
            sb.append(delimiter).append(type.getType());
            delimiter = " | "; //NOI18N
        }
        return sb.toString();
    }

}
