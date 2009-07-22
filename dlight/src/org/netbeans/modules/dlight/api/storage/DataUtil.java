/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.api.storage;

/**
 * Some functions useful when dealing with {@link DataRow}s.
 *
 * @author Alexey Vladykin
 */
public final class DataUtil {

    private DataUtil() {
    }

    /**
     * Shortcut for {@link #toInt(java.lang.Object, int)}
     * with <code>defaultValue = 0</code>.
     * 
     * @param obj  converted object
     * @return conversion result
     */
    public static int toInt(Object obj) {
        return toInt(obj, 0);
    }

    /**
     * Tries to convert given object to int.
     * <code>Number</code> subclasses are converted using <code>intValue()</code> method.
     * <code>String</code>s are parsed with <code>Integer.parseInt()</code>.
     * In caes string is malformed, or object is of any other class,
     * or <code>null</code> is given, default value is returned.
     *
     * @param obj  converted object
     * @param defaultValue  what to return if conversion fails
     * @return coversion result
     */
    public static int toInt(Object obj, int defaultValue) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException ex) {
            }
        }
        return 0;
    }
}
