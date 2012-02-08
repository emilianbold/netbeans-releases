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
package org.netbeans.modules.javascript2.editor.doclets;

/**
 * Represents elements of jsDoc in the version 2.x. It can be i.e.:
 * @final, @private, @author Jackie Chan, @augments OtherClass etc.
 * 
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocElement {

    private final Type type;
    private final String description;

    /**
     * Creates new {@code JsDocElement}.
     * @param type {@code JsDocElement} type
     * @param description rest of {@code JsDocElement}
     */
    public JsDocElement(Type type, String description) {
        this.type = type;
        this.description = description;
    }

    /**
     * Represents jsDoc element type.
     */
    public enum Type {
        AUGMENTS("augments"),
        AUTHOR("author"),
        ARGUMENT("argument"),
        BORROWS("borrows"),
        CLASS("class"),
        CONSTANT("constant"),
        CONSTRUCTOR("constructor"),
        CONSTRUCTS("constructs"),
        DEFAULT("default"),
        DEPRECATED("deprecated"),
        DESCRIPTION("description"),
        EVENT("event"),
        EXAMPLE("example"),
        EXTENDS("extends"),
        FIELD("field"),
        FILE_OVERVIEW("fileOverview"),
        FUNCTION("function"),
        IGNORE("ignore"),
        INNER("inner"),
        LENDS("lends"),
        LINK("link"),
        MEMBER_OF("memberOf"),
        NAME("name"),
        NAMESPACE("namespace"),
        PARAM("param"),
        PRIVATE("private"),
        PROPERTY("property"),
        PUBLIC("public"),
        REQUIRES("requires"),
        RETURNS("returns"),
        SEE("see"),
        SINCE("since"),
        STATIC("static"),
        THROWS("throws"),
        TYPE("type"),
        VERSION("version");

        private final String value;

        private Type(String textValue) {
            this.value = textValue;
        }

        @Override
        public String toString() {
            return value;
        }
    }
    
}
