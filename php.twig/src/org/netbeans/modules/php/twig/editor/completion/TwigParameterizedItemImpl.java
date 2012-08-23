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
package org.netbeans.modules.php.twig.editor.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.HtmlFormatter;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TwigParameterizedItemImpl implements TwigParameterizedItem {
    private final String name;
    private final List<Parameter> parameters;

    public TwigParameterizedItemImpl(final String name) {
        this(name, Collections.EMPTY_LIST);
    }

    public TwigParameterizedItemImpl(final String name, final List<Parameter> parameters) {
        this.name = name;
        this.parameters = new ArrayList<Parameter>(parameters);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void formatParameters(final HtmlFormatter formatter) {
        formatter.appendText("("); //NOI18N
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            if (i != 0) {
                formatter.appendText(", "); //NOI18N
            }
            parameter.format(formatter);
        }
        formatter.appendText(")"); //NOI18N
    }

    @Override
    public void prepareTemplate(StringBuilder template) {
        template.append(getName());
        template.append("("); //NOI18N
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            if (i != 0) {
                template.append(", "); //NOI18N
            }
            parameter.prepareTemplate(template);
        }
        template.append(")"); //NOI18N
    }

    public static class Parameter {

        public enum Need {
            OPTIONAL,
            MANDATORY;
        }

        private final String name;
        private final Need cardinality;

        public Parameter(final String name) {
            this(name, Need.MANDATORY);
        }

        public Parameter(final String name, final Need cardinality) {
            this.name = name;
            this.cardinality = cardinality;
        }

        public void format(final HtmlFormatter formatter) {
            if (!isMandatory()) {
                formatter.appendText(name);
            } else {
                formatter.emphasis(true);
                formatter.appendText(name);
                formatter.emphasis(false);
            }
        }

        public void prepareTemplate(StringBuilder template) {
            template.append("${").append(name).append("}"); //NOI18N
        }

        private boolean isMandatory() {
            return Need.MANDATORY.equals(cardinality);
        }

    }

}
