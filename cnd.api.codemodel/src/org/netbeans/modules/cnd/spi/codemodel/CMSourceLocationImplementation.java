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
package org.netbeans.modules.cnd.spi.codemodel;

/**
 * Identifies a specific source location within a translation unit.
 *
 * @author Vladimir Voskresensky
 */
public interface CMSourceLocationImplementation {

    /**
     * \brief Returns true if \p range is invalid.
     */
    public boolean isValid();

    /**
     * \brief Returns true if the given source location is in a system header.
     */
    public boolean isInSystemHeader();

    /**
     * \brief Retrieve the file, line, column, and offset represented by the
     * given source location.
     *
     * If the location refers into a macro expansion, return where the macro was
     * expanded or where the macro argument was written, if the location points
     * at a macro argument.
     */
    public CMFileImplementation getFile();
    public int getLine();
    public int getColumn();
    public int getOffset();

    /**
     * If the location refers into a macro expansion, retrieves the location of
     * the macro expansion, otherwise must return itself.
     */
    public CMSourceLocationImplementation getExpansionLocation();

    /**
     * If the location refers into a macro instantiation, return where the
     * location was originally spelled in the source file, otherwise must return
     * itself.
     */
    public CMSourceLocationImplementation getSpellingLocation();

    /**
     * \brief Retrieve the file, line, column, and offset represented by the
     * given source location, as specified in a # line directive.
     *
     * Example: given the following source code in a file somefile.c
     *
     * \code #123 "dummy.c" 1
     *
     * static int func(void) { return 0; } \endcode
     *
     * the location information returned by this function would be
     *
     * File: dummy.c Line: 124 Column: 12
     *
     * whereas getExpansionLocation would have returned
     *
     * File: somefile.c Line: 3 Column: 12
     *
     */
    public PresumedImplementation getPresumedLocation();

    interface PresumedImplementation {

        /**
         * \brief Return true if this object is invalid or uninitialized.
         *
         * This occurs when created with invalid source locations or when
         * walking off the top of a \#include stack.
         */
        public boolean isValid();

        /**
         * \brief Return the presumed filename of this location. This can be
         * affected by \#line etc.
         *
         * Note that filenames returned will be for "virtual" files, which don't
         * necessarily exist on the machine running clang - e.g. when parsing
         * preprocessed output obtained from a different environment. If a
         * non-NULL value is passed in, remember to dispose of the returned
         * value using \c clang_disposeString() once you've finished with it.
         * For an invalid source location, an empty string is returned.
         *
         * @return file name.
         */
        public CharSequence getFileName();

        /**
         * \brief Return the presumed line number of this location. This can be
         * affected by \#line etc.
         *
         * @return line. For an invalid source location, zero is returned.
         */
        public int getLine();

        /**
         * \brief Return the presumed column of this location. This can be
         * affected by \#line etc.
         *
         * @return column. For an invalid source location, zero is returned.
         */
        public int getColumn();

        @Override
        boolean equals(Object other);

        @Override
        int hashCode();
    }

    /**
     * \brief Determine whether other source location, which must refer into the
     * same translation unit, refer to exactly the same point in the source
     * code.
     *
     * @return true if the source locations refer to the same location, false if
     * they refer to different locations.
     */
    @Override
    boolean equals(Object/*CMSourceLocationImplementation*/ other);

    @Override
    int hashCode();
}
