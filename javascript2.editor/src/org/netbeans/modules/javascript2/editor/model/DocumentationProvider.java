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
package org.netbeans.modules.javascript2.editor.model;

import com.oracle.nashorn.ir.Node;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public interface DocumentationProvider {

    /**
     * Gets possible return types get from the method.
     * @param node of the javaScript code
     * @return list of potential return types, never {@code null}
     */
    List<Type> getReturnType(Node node);

    /**
     * Gets parameters of the method.
     * @param node of the javaScript code
     * @return list of parameters, never {@code null}
     */
    List<DocParameter> getParameters(Node node);

    /**
     * Gets documentation for given Node.
     * @param node of the javaScript code
     * @return documentation text if any {@code null} otherwise
     */
    String getDocumentation(Node node);

    /**
     * Says whether is code at given code depricated or not.
     * @param node examined node
     * @return {@code true} if the comment says "it's deprecated", {@code false} otherwise
     */
    boolean isDeprecated(Node node);

    /**
     * Gets the set of modifiers attached to given node.
     * @param node examinded node
     * @return {@code Set} of modifiers
     */
    Set<Modifier> getModifiers(Node node);

    /**
     * Possible modifiers of the javaScript element declared by documentation tools.
     */
    public enum Modifier {
        PRIVATE("private"),
        PUBLIC("public"),
        STATIC("static");

        private final String value;

        private Modifier(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        /**
         * Gets {@code Modifier} corresponding to given value.
         * @param value {@code String} value of the {@code Modifier}
         * @return {@code Modifier}
         */
        public static Modifier fromString(String value) {
            for (Modifier modifier : Modifier.values()) {
                if (value.equalsIgnoreCase(modifier.toString())) {
                    return modifier;
                }
            }
            return null;
        }
    }

}
