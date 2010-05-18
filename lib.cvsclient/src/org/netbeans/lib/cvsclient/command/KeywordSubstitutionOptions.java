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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.lib.cvsclient.command;

/**
 * @author  Thomas Singer
 */
public final class KeywordSubstitutionOptions {

    public static final KeywordSubstitutionOptions DEFAULT = new KeywordSubstitutionOptions("kv"); //NOI18N
    public static final KeywordSubstitutionOptions DEFAULT_LOCKER = new KeywordSubstitutionOptions("kvl"); //NOI18N
    public static final KeywordSubstitutionOptions ONLY_KEYWORDS = new KeywordSubstitutionOptions("k"); //NOI18N
    public static final KeywordSubstitutionOptions ONLY_VALUES = new KeywordSubstitutionOptions("v"); //NOI18N
    public static final KeywordSubstitutionOptions OLD_VALUES = new KeywordSubstitutionOptions("o"); //NOI18N
    public static final KeywordSubstitutionOptions BINARY = new KeywordSubstitutionOptions("b"); //NOI18N

    public static KeywordSubstitutionOptions findKeywordSubstOption(String keyword) {
        if (BINARY.toString().equals(keyword)) {
            return BINARY;
        }
        if (DEFAULT.toString().equals(keyword)) {
            return DEFAULT;
        }
        if (DEFAULT_LOCKER.toString().equals(keyword)) {
            return DEFAULT_LOCKER;
        }
        if (OLD_VALUES.toString().equals(keyword)) {
            return OLD_VALUES;
        }
        if (ONLY_KEYWORDS.toString().equals(keyword)) {
            return ONLY_KEYWORDS;
        }
        if (ONLY_VALUES.toString().equals(keyword)) {
            return ONLY_VALUES;
        }
        return null;
    }

    private String value;

    private KeywordSubstitutionOptions(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
