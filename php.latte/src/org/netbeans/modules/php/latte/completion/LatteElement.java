/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.latte.completion;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public interface LatteElement extends ElementHandle {

    public String getTemplate();

    public void formatParameters(HtmlFormatter formatter);

    public static class Factory {

        public static LatteElement create(String name) {
            return new LatteElementWithoutParams(name);
        }

        public static LatteElement create(String name, List<Parameter> parameters) {
            return new LatteElementWithParams(name, parameters);
        }

        public static LatteElement create(String name, String customTemplate) {
            return new LatteElementWithCustomTemplate(name, customTemplate);
        }

    }

    abstract static class BaseLatteElementItem implements LatteElement {

        private final String name;

        public BaseLatteElementItem(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public FileObject getFileObject() {
            return null;
        }

        @Override
        public String getMimeType() {
            return "";
        }

        @Override
        public String getIn() {
            return "";
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.OTHER;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.EMPTY_SET;
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }

    }

    static class LatteElementWithoutParams extends BaseLatteElementItem {

        public LatteElementWithoutParams(String name) {
            super(name);
        }

        @Override
        public String getTemplate() {
            return getName();
        }

        @Override
        public void formatParameters(HtmlFormatter formatter) {
        }

    }

    static class LatteElementWithParams extends BaseLatteElementItem {
        private final List<Parameter> parameters;

        public LatteElementWithParams(String name, List<Parameter> parameters) {
            super(name);
            this.parameters = parameters;
        }

        @Override
        public void formatParameters(final HtmlFormatter formatter) {
            formatter.appendText(":"); //NOI18N
            for (int i = 0; i < parameters.size(); i++) {
                Parameter parameter = parameters.get(i);
                if (i != 0) {
                    formatter.appendText(":"); //NOI18N
                }
                parameter.format(formatter);
            }
        }

        @Override
        public String getTemplate() {
            StringBuilder template = new StringBuilder();
            template.append(getName());
            template.append(":"); //NOI18N
            for (int i = 0; i < parameters.size(); i++) {
                Parameter parameter = parameters.get(i);
                if (i != 0) {
                    template.append(":"); //NOI18N
                }
                parameter.prepareTemplate(template);
            }
            return template.toString();
        }
    }

    public static class Parameter {
        private final String name;
        private final String defaultValue;

        public Parameter(String name, String defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public Parameter(String name) {
            this(name, null);
        }

        public void format(HtmlFormatter formatter) {
            if (!isMandatory()) {
                formatter.appendText(name);
            } else {
                formatter.emphasis(true);
                formatter.appendText(name);
                formatter.appendText(" = "); //NOI18N
                formatter.appendText(defaultValue);
                formatter.emphasis(false);
            }
        }

        public void prepareTemplate(StringBuilder template) {
            if (isMandatory()) {
                template.append("${").append(name).append("}"); //NOI18N
            } else {
                template.append("${").append(defaultValue).append("}"); //NOI18N
            }
        }

        private boolean isMandatory() {
            return defaultValue != null;
        }
    }

    static class LatteElementWithCustomTemplate extends BaseLatteElementItem {

        private final String customTemplate;

        public LatteElementWithCustomTemplate(String name, String customTemplate) {
            super(name);
            this.customTemplate = customTemplate;
        }

        @Override
        public String getTemplate() {
            return customTemplate;
        }

        @Override
        public void formatParameters(HtmlFormatter formatter) {
        }
    }

}
