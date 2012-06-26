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

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public interface JsDocElement {

    /**
     * Gets jsDoc element type.
     * @return jsDoc element type
     */
    public Type getType();

    /**
     * Represents jsDoc element type.
     */
    public enum Type {
        // special context sensitive type
        CONTEXT_SENSITIVE("contextSensitive", Category.DESCRIPTION), //NOI18N

        // unknow type
        UNKNOWN("unknown", Category.UNKNOWN), //NOI18N

        // common jsDoc tags
        ARGUMENT("@argument", Category.NAMED_PARAMETER), //NOI18N
        AUGMENTS("@augments", Category.DECLARATION), //NOI18N
        AUTHOR("@author", Category.DESCRIPTION), //NOI18N
        BORROWS("@borrows", Category.ASSIGN), //NOI18N
        CLASS("@class", Category.DESCRIPTION), //NOI18N
        CONSTANT("@constant", Category.SIMPLE), //NOI18N
        CONSTRUCTOR("@constructor", Category.SIMPLE), //NOI18N
        CONSTRUCTS("@constructs", Category.SIMPLE), //NOI18N
        DEFAULT("@default", Category.DESCRIPTION), //NOI18N
        DEPRECATED("@deprecated", Category.DESCRIPTION), //NOI18N
        DESCRIPTION("@description", Category.DESCRIPTION), //NOI18N
        EVENT("@event", Category.SIMPLE), //NOI18N
        EXAMPLE("@example", Category.DESCRIPTION), //NOI18N
        EXTENDS("@extends", Category.DECLARATION), //NOI18N
        FIELD("@field", Category.SIMPLE), //NOI18N
        FILE_OVERVIEW("@fileOverview", Category.DESCRIPTION), //NOI18N
        FUNCTION("@function", Category.SIMPLE), //NOI18N
        IGNORE("@ignore", Category.SIMPLE), //NOI18N
        INNER("@inner", Category.SIMPLE), //NOI18N
        LENDS("@lends", Category.LINK), //NOI18N
        LINK("@link", Category.DESCRIPTION), //NOI18N
        MEMBER_OF("@memberOf", Category.LINK), //NOI18N
        NAME("@name", Category.LINK), //NOI18N
        NAMESPACE("@namespace", Category.DESCRIPTION), //NOI18N
        PARAM("@param", Category.NAMED_PARAMETER), //NOI18N
        PRIVATE("@private", Category.SIMPLE), //NOI18N
        PROPERTY("@property", Category.NAMED_PARAMETER), //NOI18N
        PUBLIC("@public", Category.SIMPLE), //NOI18N
        REQUIRES("@requires", Category.DESCRIPTION), //NOI18N
        RETURN("@return", Category.UNNAMED_PARAMETER), //NOI18N
        RETURNS("@returns", Category.UNNAMED_PARAMETER), //NOI18N
        SEE("@see", Category.DESCRIPTION), //NOI18N
        SINCE("@since", Category.DESCRIPTION), //NOI18N
        STATIC("@static", Category.SIMPLE), //NOI18N
        SYNTAX("@syntax", Category.DESCRIPTION), //NOI18N
        THROWS("@throws", Category.UNNAMED_PARAMETER), //NOI18N
        TYPE("@type", Category.DECLARATION), //NOI18N
        VERSION("@version", Category.DESCRIPTION); //NOI18N

        private final String value;
        private final Category category;

        private Type(String textValue, Category category) {
            this.value = textValue;
            this.category = category;
        }

        @Override
        public String toString() {
            return value;
        }

        /**
         * Gets the category of the jsDoc element.
         * @return category
         */
        public Category getCategory() {
            return category;
        }

        /**
         * Gets {@code Type} corresponding to given value.
         * @param value {@code String} value of the {@code Type}
         * @return {@code Type}
         */
        public static Type fromString(String value) {
            if (value != null) {
                for (Type type : Type.values()) {
                    if (value.equalsIgnoreCase(type.toString())) {
                        return type;
                    }
                }
            }
            return UNKNOWN;
        }

    }

    /**
     * Contains information about element kind.
     */
    public enum Category {
        ASSIGN,
        DECLARATION,
        DESCRIPTION,
        LINK,
        NAMED_PARAMETER,
        SIMPLE,
        UNKNOWN,
        UNNAMED_PARAMETER
    }
}
