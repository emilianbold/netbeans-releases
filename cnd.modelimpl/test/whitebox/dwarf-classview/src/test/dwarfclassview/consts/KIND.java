/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package test.dwarfclassview.consts;

import java.util.HashMap;

public enum KIND {
    PROJECT     ("project"), // NOI18N
    OPERATOR    ("operator"), // NOI18N
    FIELD       ("field"), // NOI18N
    VARIABLE    ("variable"), // NOI18N
    CLASS       ("class"), // NOI18N
    NAMESPACE   ("namespace"), // NOI18N
    MEMBER      ("member"), // NOI18N
    TYPEDEF     ("typedef"), // NOI18N
    ENUM        ("enum"), // NOI18N
    UNION       ("union"), // NOI18N
    CONSTRUCTOR ("constructor"), // NOI18N
    DESTRUCTOR  ("destructor"), // NOI18N
    METHOD      ("method"), // NOI18N
    FUNCTION    ("function"), // NOI18N
    ENUMITEM    ("enum_item"), // NOI18N
    UNHANDLED_KIND("UNHANDLED_KIND"); // NOI18N
    
    private static final HashMap<String, KIND> hashmap = new HashMap<String, KIND>();
    private final String value;
    
    static {
        for (KIND elem : KIND.values()) {
            hashmap.put(elem.value, elem);
        }
    }
    
    KIND(String value) {
        this.value = value;
    }
    
    public static KIND get(String val) {
        return hashmap.get(val);
    }
    
    public String value() {
        return value;
    }    
}
