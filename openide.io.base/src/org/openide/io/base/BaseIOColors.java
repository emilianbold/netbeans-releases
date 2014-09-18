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
import org.openide.util.Lookup;

/**
 * Settings of colors for normal, error, hyperlink, important hyperlink lines.
 * Change is global for text past and future.
 * <p>
 *   Client usage:
 * </p>
 * <pre>
   // set important hyperlink color to red
   BaseInputOutput io = ...;
   if (BaseIOColors.isSupported(io)) {
       BaseIOColors.setColor(io, BaseIOColors.OutputType.HYPERLINK_IMPORTANT, Color.RED);
   }
 </pre>
 * How to support {@link BaseIOColors} in own {@link BaseIOProvider} implementation:
 * <ul>
 *   <li> Implement some {@link Provider} of BaseIOColors.
 *   <li> Place instance of {@link BaseIOColors} to {@link Lookup} provided by {@link BaseInputOutput}.
 * </ul>
 * @see BaseIOColorLines
 * @see BaseIOColorPrint
 * @author Tomas Holy, Jaroslav Havlin
 */
public final class BaseIOColors {

    private BaseIOColors() {}

    /**
     * output types
     */
    public enum OutputType {
        /** default output */
        OUTPUT,
        /** error output */
        ERROR,
        /** hyperlink */
        HYPERLINK,
        /** important hyperlink */
        HYPERLINK_IMPORTANT,
        /** input text
         * @since 1.39 */
        INPUT,
        /** Info about success. Change is not guaranteed to affect colored
         * output written in the past.
         * @since 1.40 */
        LOG_SUCCESS,
        /** Info about failure. Change is not guaranteed to affect colored
         * output written in the past.
         * @since 1.40 */
        LOG_FAILURE,
        /**Info about warning. Change is not guaranteed to affect colored
         * output written in the past.
         * @since 1.40 */
        LOG_WARNING,
        /** Debugging info. Change is not guaranteed to affect colored
         * output written in the past.
         * @since 1.40 */
        LOG_DEBUG
    }

    /**
     * Gets current color for output.
     * @param io {@link BaseInputOutput} to operate on.
     * @param type Output type to get color for.
     * @return Current color for specified output type or null if not supported.
     */
    public static @CheckForNull BaseColor getColor(
            @NonNull BaseInputOutput io,
            @NonNull OutputType type) {

        Provider ioc = ExtrasHelper.find(io, BaseIOColors.Provider.class);
        return ioc != null ? ioc.getColor(type) : null;
    }

    /**
     * Sets specified color for output.
     *
     * @param io {@link BaseInputOutput} to operate on.
     * @param type Output type to set color for.
     * @param color New color for specified output type.
     */
    public static void setColor(
            @NonNull BaseInputOutput io,
            @NonNull OutputType type,
            @NonNull BaseColor color) {
        Provider ioc = ExtrasHelper.find(io, BaseIOColors.Provider.class);
        if (ioc != null) {
            ioc.setColor(type, color);
        }
    }

    /**
     * Checks whether this feature is supported for provided IO.
     *
     * @param io IO to check on.
     * @return True if supported.
     */
    public static boolean isSupported(@NonNull BaseInputOutput io) {
        return ExtrasHelper.isSupported(io, BaseIOColors.Provider.class);
    }

    /**
     * SPI for implementing support for output coloring.
     *
     * @see BaseIOColors
     */
    public interface Provider {

        /**
         * Gets current color for output.
         *
         * @param type Output type to get color for.
         * @return Current color for specified output.
         */
        @NonNull BaseColor getColor(@NonNull OutputType type);

        /**
         * Sets specified color for output
         *
         * @param type Output type to set color for.
         * @param color New color for specified output type.
         */
        void setColor(@NonNull OutputType type, @NonNull BaseColor color);
    }
}
