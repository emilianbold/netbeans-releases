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

import java.io.IOException;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Lookup;

/**
 * Text printing with listeners, tags, or custom extension types.
 *
 * <p>
 * Client usage:
 * </p>
 * <pre>
  BaseInputOutput io = ...;
  BaseOutputListener l = ...;
  BaseOutputTag t = ...;

  // check only one extension type
  if (BaseIOHyperlink.isSupported(io, BaseOutputListener.class) {
    BaseIOHyperlink.println(io, "Link", false, l);
  }

  // to print a single line with several extensions
  if (BaseIOHyperlink.isSupported(io)) { //check extensions supported at all
    List&ltObject&gt exts = new LinkedList&lt;Object&gt;();
    if (BaseIOHyperlink.isSupported(io, BaseOutputListener.class)) {
      exts.add(new MyOutputListener());
    }
    if (BaseIOHyperlink.isSupported(io, BaseOutputTag.class)) {
      exts.add(new MyOutputTag());
    }
    BaseIOHyperlink.println(io, "Link 2", false, exts.toArray(new Object[exts.size()]));
  }

 </pre>
 * How to support {@link BaseIOHyperlink} in own {@link BaseIOProvider} implementation:
 * <ul>
 *   <li> Implement some {@link Provider} of BaseIOHyperlink.</li>
 *   <li> Place instance of {@link Provider} to {@link Lookup} provided by {@link BaseInputOutput}.</li>
 * </ul>
 *
 * @author Jaroslav Havlin
 */
public final class BaseIOHyperlink {

    private BaseIOHyperlink() {}

    public enum Type {

        OUT, ERR;
    }

    /**
     * Check whether BaseIOHyperlink extension is supported by the I/O.
     *
     * @param io The I/O to check for support of BaseIOHyperlink.
     * @return True if BaseIOHyperlink is supported by the I/O, false otherwise.
     */
    public static boolean isSupported(@NonNull BaseInputOutput io) {
        return ExtrasHelper.isSupported(io, BaseIOHyperlink.Provider.class);
    }

    /**
     * Print a line which will be displayed as a hyperlink, calling the action
     * specified in the array object if it is clicked.
     *
     * @param io I/O to print into.
     * @param type Type of output, standard or error.
     * @param str a string to print to the tab
     * @param important mark the line as important. Makes the UI respond
     * appropriately, eg. stop the automatic scrolling or highlight the
     * hyperlink.
     * @param linkInfo array containing extending info for the text, e.g.
     * an output listener and/or an output tag.
     * @throws IOException if the string could not be printed
     * @throws IllegalArgumentException if the I/O does not support this feature.
     */
    public static void println(
            @NonNull BaseInputOutput io,
            @NonNull Type type,
            @NonNull String str,
            boolean important,
            @NonNull BaseIOLinkInfo... linkInfo) throws IOException {
        ExtrasHelper.getExtras(io, BaseIOHyperlink.Provider.class).println(
                type, str, important, linkInfo);
    }

    /**
     * Print a line which will be displayed as a hyperlink, calling the action
     * specifided in the {@link Lookup} object if it is clicked, if the caret
     * enters it, or if the enter key is pressed over it.
     *
     * @param io I/O to print into.
     * @param type Type of output, standard or error.
     * @param str a string to print to the tab
     * @param linkInfo array containing extending info for the text, e.g.
     * an output listener and/or an output tag.
     * @throws IOException if the string could not be printed
     * @throws IllegalArgumentException if the I/O does not support this feature.
     */
    public static void println(
            @NonNull BaseInputOutput io,
            @NonNull Type type,
            @NonNull String str,
            @NonNull BaseIOLinkInfo... linkInfo) throws IOException {
        println(io, type, str, false, linkInfo);
    }

    /**
     * Check whether BaseIOHyperlink is supported by the I/O and that its lines
     * can be extended with link info of class {@code cls}.
     *
     * @param io I/O object to get extending types from.
     * @param cls The link info class that we want to check the support for.
     * @return True if the I/O supports {@link BaseIOHyperlink} and if it can be
     * extended with instances of class {@code cls}.
     */
    public static boolean isSupported(
            @NonNull BaseInputOutput io,
            @NonNull Class<? extends BaseIOLinkInfo> cls) {
        Provider p = ExtrasHelper.find(io, Provider.class);
        return p == null
                ? false
                : p.isSupported(cls);
    }

    /**
     * SPI for implementing support for {@link BaseIOHyperlink}.
     */
    public interface Provider {

        /**
         * Check whether passed class or interface is supported by this
         * implementation of BaseOutputWriter and can be put to the array passed
         * to
         * {@link #println(BaseIOHyperlink.Type, String, boolean, BaseIOLinkInfo...)}.
         *
         * @param cls Class or interface to check
         * @return True if class or interface {@code cls} is supported.
         */
        boolean isSupported(@NonNull Class<? extends BaseIOLinkInfo> cls);

        /**
         * Print a line which will be displayed as a hyperlink, calling the
         * action specifided in the array object if it is clicked.
         *
         * @param type Type of the output, {@link Type#OUT} for standard output,
         * {@link Type#ERR} for error output.
         * @param s a string to print to the tab
         * @param linkInfo array containing extending info for the text,
         * e.g. an output listener and/or an output tag.
         * @param important mark the line as important. Makes the UI respond
         * appropriately, eg. stop the automatic scrolling or highlight the
         * hyperlink.
         * @throws IOException if the string could not be printed.
         */
        void println(@NonNull Type type, @NonNull String s, boolean important,
                @NonNull BaseIOLinkInfo... linkInfo) throws IOException;
    }
}
