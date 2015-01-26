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
package org.netbeans.modules.cnd.api.codemodel;

/**
 * Unified Symbol Resolution.
 * 
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public final class CMUnifiedSymbolResolution {
    private final CharSequence usr;

    private CMUnifiedSymbolResolution(CharSequence usr) {
        // could be null for Invalid cursor
//        assert usr != null;
        this.usr = usr;
    }
    
    public static CMUnifiedSymbolResolution create(CharSequence usr) {
        return new CMUnifiedSymbolResolution(usr);
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (int i = 0; i < usr.length(); i++) {
            h = 31 * h + usr.charAt(i);
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CMUnifiedSymbolResolution other = (CMUnifiedSymbolResolution) obj;
        // Content Equals
        if (usr.length() != other.usr.length()) {
            return false;
        }
        int i = 0;
        int n = usr.length();
        while (n-- != 0) {
            if (usr.charAt(i) != other.usr.charAt(i)) {
                return false;
            }
            i++;
        }
        return true;
    }

    public CharSequence getText() {
        return usr;
    }

    @Override
    public String toString() {
        return usr.toString();
    }
}
