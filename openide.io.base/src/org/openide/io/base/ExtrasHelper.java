/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.openide.io.base;

/**
 * Helper class for finding extensions in I/O's lookup.
 *
 * @author jhavlin
 */
class ExtrasHelper {

    private ExtrasHelper() {}

    /**
     * Check whether class {@code extras} is present in the I/O's lookup.
     *
     * @param io I/O to check
     * @param extras Extension class that should be supported.
     *
     * @return True if the I/O supports the extension class, false otherwise.
     */
    static boolean isSupported(BaseInputOutput io, Class<?> extras) {
        Object o = io.getLookup().lookup(extras);
        return o != null;
    }

    /**
     * Get extension of given type, or throw an exception if it is not present.
     *
     * @param <T> Type of the extension.
     * @param io I/O to get the extension for.
     * @param cls Extension class.
     *
     * @return The extension object of given type.
     * @throws IllegalArgumentException If the I/O does not support this
     * extension type.
     */
    static <T> T getExtras(BaseInputOutput io, Class<T> cls) {
        T extras = io.getLookup().lookup(cls);
        if (extras == null) {
            throw new IllegalArgumentException("BaseInputOutput " + io
                    + " does not support " + cls);
        } else {
            return extras;
        }
    }

    /**
     * Get extension of given type, or return null if it is not present.
     *
     * @param <T> Type of the extension.
     * @param io I/O to get the extension for.
     * @param cls Extension class.
     *
     * @return The extension object of given type, or null if it is not
     * supported.
     */
    static <T> T find(BaseInputOutput io, Class<T> cls) {
        return io.getLookup().lookup(cls);
    }
}
