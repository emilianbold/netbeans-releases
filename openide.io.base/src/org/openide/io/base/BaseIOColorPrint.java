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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.openide.io.base;

import java.io.IOException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Lookup;

/**
 * Text printing with custom color.
 * <p>
 * Client usage:
 * </p>
 * <pre>
  BaseInputOutput io = ...;
  BaseOutputListener l = ...;
  BaseOutputListener l2 = ...;
  if (BaseIOColorPrint.isSupported(io)) {
    BaseIOColorPrint.print(io, "Green text", Color.GREEN);
    BaseIOColorPrint.print(io, " orange hyperlink ", false, Color.ORANGE, l);
    BaseIOColorPrint.print(io, " green hyperlink\n", false, Color.GREEN, l2);
  }
 </pre>
 * How to support {@link BaseIOColorPrint} in own {@link BaseIOProvider} implementation:
 * <ul>
 *   <li> Implement some {@link Provider} of BaseIOColorPrint.</li>
 *   <li> Place instance of {@link Provider} to {@link Lookup} provided by {@link BaseInputOutput}.</li>
 * </ul>
 * @see BaseIOColors
 * @see BaseIOColorLines
 * @author Tomas Holy, Jaroslav Havlin
 */
public final class BaseIOColorPrint {

    private BaseIOColorPrint() {}

    /**
     * Prints text with selected color
     * @param io IO to print to.
     * @param text A string to print to the tab.
     * @param color A color for the text (null allowed). If null is passed
     * default color (see {@link BaseIOColors}) is used.
     * @throws java.io.IOException if printint to the output fails.
     * @throws IllegalArgumentException if the I/O does not support this
     * feature.
     */
    public static void print(
            @NonNull BaseInputOutput io,
            @NonNull CharSequence text,
            @NullAllowed BaseColor color) throws IOException {
        print(io, text, false, color);
    }

    /**
     * Prints text with selected color and add listener for it
     * @param io IO to print to.
     * @param text A string to print to the tab.
     * @param important  Mark the line as important.
     *        Makes the UI respond appropriately, eg. stop the automatic scrolling
     *        or highlight the hyperlink.
     * @param color A color for the text (null allowed). If null is passed
     * default color (see {@link BaseIOColors}) is used.
     * @param extendedInfo Output listeners, output tags or similar objects, see
     * {@link BaseIOHyperlink}.
     * @throws java.io.IOException if writing to the output fails.
     * @throws IllegalArgumentException if the I/O does not support this
     * feature.
     */
    public static void print(
            @NonNull BaseInputOutput io,
            @NonNull CharSequence text,
            boolean important,
            @NullAllowed BaseColor color,
            @NonNull BaseIOLinkInfo... extendedInfo)
            throws IOException {

        ExtrasHelper.getExtras(io, BaseIOColorPrint.Provider.class).print(
                text, important, color, extendedInfo);
    }


    /**
     * Checks whether this feature is supported for provided IO
     * @param io IO to check on
     * @return true if supported
     */
    public static boolean isSupported(@NonNull BaseInputOutput io) {
        return ExtrasHelper.isSupported(io, BaseIOColorPrint.Provider.class);
    }

    public interface Provider {

        /**
         * Prints text with selected color and optionaly add listener for it
         *
         * @param text A string to print to the tab.
         * @param important Mark the line as important. Makes the UI
         * respond appropriately, eg. stop the automatic scrolling or highlight
         * the hyperlink.
         * @param color A color for the text (null allowed). If null is passed
         * default color (see {@link BaseIOColors}) is used.
         * @param extendedInfo Output listener, output tag or similar objects,
         * see {@link BaseIOHyperlink}.
         * @throws java.io.IOException if writing to the output fails.
         */
        void print(
                @NonNull CharSequence text,
                boolean important,
                @NullAllowed BaseColor color,
                @NonNull BaseIOLinkInfo... extendedInfo) throws IOException;
    }
}
