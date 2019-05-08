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

package org.netbeans.modules.cnd.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public final class CndCollectionUtils {


    private CndCollectionUtils() {
    }
    
    public static int hashCode(Collection<?> col) {
        int hash = 0;
        if (col != null) {
            hash = 5;
            for (Object o : col) {
                hash = 67 * hash + Objects.hashCode(o);
            }
        }
        return hash;
    }

    public static int hashCode(Map<?, ?> thisCol) {
        int hash = 5;
        for (Object p : thisCol.keySet()) {
            hash ^= Objects.hashCode(p);
        }
        return hash;
    }
    
    public static boolean equals(Map<?, ?> thisCol, Map<?, ?> otherCol) {
        if (thisCol == otherCol) {
            return true;
        }
        if ((thisCol == null) != (otherCol == null)) {
            return false;
        }
        if (thisCol != null && otherCol != null) {
            if (thisCol.size() != otherCol.size()) {
                return false;
            }
            for (Map.Entry<?, ?> entry : thisCol.entrySet()) {
                Object otherVal = otherCol.get(entry.getKey());
                if (!Objects.equals(entry.getValue(), otherVal)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean equals(List<?> thisCol, List<?> otherCol) {
        if (thisCol == otherCol) {
            return true;
        }
        if ((thisCol == null) != (otherCol == null)) {
            return false;
        }
        if (thisCol != null && otherCol != null) {
            if (thisCol.size() != otherCol.size()) {
                return false;
            }
            for (int i = 0; i < thisCol.size(); i++) {
                if (!Objects.equals(thisCol.get(i), otherCol.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean equals(Iterable<?> thisCol, Iterable<?> otherCol) {
        if (thisCol == otherCol) {
            return true;
        }
        if ((thisCol == null) != (otherCol == null)) {
            return false;
        }
        if (thisCol != null && otherCol != null) {
            Iterator<?> itThis = thisCol.iterator();
            Iterator<?> itOther = otherCol.iterator();
            while (itThis.hasNext()) {
                if (!itOther.hasNext()) {
                    return false;
                }
                if (!Objects.equals(itThis.next(), itOther.next())) {
                    return false;
                }
            }
            if (itOther.hasNext()) {
                return false;
            }
        }
        return true;
    }
}
