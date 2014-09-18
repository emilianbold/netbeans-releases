/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.openide.io.base;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Lookup;

/**
 * Settings of tool tip/icon for IO component (tab).
 * <p>
 * Client usage:
 * <pre>
 *  // settings of IO tab icon, tooltip
 *  BaseInputOutput io = ...;
 *  BaseIOTab.setToolTipText(io, "text");
 * </pre>
 * How to support {@link BaseIOTab} in own {@link BaseIOProvider} implementation:
 * <ul>
 *   <li> Implement {@link Provider}.</li>
 *   <li> Place instance of {@link Provider} to {@link Lookup} provided by {@link BaseInputOutput}.</li>
 * </ul>
 * @author Tomas Holy, Jaroslav Havlin
 */
public final class BaseIOTab {

    private BaseIOTab() {}

    /**
     * Gets current tool tip text for specified IO
     * @param io IO to operate on.
     * @return Current tool tip text or null if not supported.
     */
    public static @CheckForNull String getToolTipText(
            @NonNull BaseInputOutput io) {

        Provider iot = ExtrasHelper.find(io, Provider.class);
        return iot != null ? iot.getToolTipText() : null;
    }

    /**
     * Sets tool tip text to tab corresponding to specified IO
     * @param io IO to operate on.
     * @param text New tool tip text.
     */
    public static void setToolTipText(
            @NonNull BaseInputOutput io,
            @NullAllowed String text) {
        Provider iot = ExtrasHelper.find(io, Provider.class);
        if (iot != null) {
            iot.setToolTipText(text);
        }
    }

    /**
     * Checks whether this feature is supported for provided IO
     * @param io IO to check on.
     * @return True if supported.
     */
    public static boolean isSupported(BaseInputOutput io) {
        return ExtrasHelper.isSupported(io, Provider.class);
    }

    /**
     * SPI for implementation of {@link BaseIOTab} support.
     */
    public interface Provider {

        /**
         * Gets current tool tip text.
         *
         * @return Current tool tip text.
         */
        @CheckForNull String getToolTipText();

        /**
         * Sets tool tip text to tab.
         *
         * @param text New tool tip text.
         */
        void setToolTipText(@NullAllowed String text);
    }
}
