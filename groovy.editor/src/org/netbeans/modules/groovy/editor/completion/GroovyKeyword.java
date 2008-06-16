/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.editor.completion;

/**
 *
 * @author schmidtm
 */
public enum GroovyKeyword {
    // Java keywords:
    KEYWORD_assert("assert", false, false, false),
    KEYWORD_break("break", false, false, false),
    KEYWORD_case("case", false, false, false),
    KEYWORD_catch("catch", false, false, false),
    KEYWORD_class("class", false, true, false),
    KEYWORD_continue("continue", false, false, false),
    KEYWORD_default("default", false, false, false),
    KEYWORD_do("do", false, false, false),
    KEYWORD_else("else", false, false, false),
    KEYWORD_extends("extends", false, true, false),
    KEYWORD_finally("finally", false, false, false),
    KEYWORD_for("for", false, false, false),
    KEYWORD_if("if", false, false, false),
    KEYWORD_implements("implements", false, true, false),
    KEYWORD_import("import", false, true, false),
    KEYWORD_instanceof("instanceof", false, false, false),
    KEYWORD_interface("interface", false, true, false),
    KEYWORD_new("new", false, false, false),
    KEYWORD_package("package", false, true, false),
    KEYWORD_return("return", false, false, false),
    KEYWORD_switch("switch", false, false, false),
    KEYWORD_throw("throw", false, false, false),
    KEYWORD_throws("throws", false, false, false),
    KEYWORD_try("try", false, false, false),
    KEYWORD_while("while", false, false, false),
    
    // Uniq Groovy keywords:
    
    KEYWORD_as("as", true, true, false),
    KEYWORD_def("def", true, false, true),
    KEYWORD_in("in", true, true, true),
    KEYWORD_property("property", true, true, true),
    KEYWORD_undefined("undefined", false, false, false);
    
    String name;
    boolean isGroovy;
    boolean outsideClasses;
    boolean insideClasses;

    GroovyKeyword(String name, boolean isGroovy, boolean outsideClasses, boolean insideClasses) {
        this.name = name;
        this.isGroovy = isGroovy;
        this.outsideClasses = outsideClasses;
        this.insideClasses = insideClasses;
    }
}
