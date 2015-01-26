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

import org.netbeans.modules.cnd.spi.codemodel.CMSourceLocationImplementation;
import org.netbeans.modules.cnd.spi.codemodel.trace.CMTraceUtils;

/**
 *
 * \brief Identifies a specific source location within a translation unit.
 *
 * Use getExpansionLocation() or getSpellingLocation() to map a source location
 * to a particular file, line, and column.
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public final class CMSourceLocation implements CMLocation {

    public boolean isValid() {
        return impl.isValid();
    }

    /**
     * \brief Retrieve the file, line, column, and offset represented by the
     * given source location.
     *
     * If the location refers into a macro expansion, return where the macro was
     * expanded or where the macro argument was written, if the location points
     * at a macro argument.
     * @return 
     */
    @Override
    public CMFile getFile() {
        if (impl.getFile() == null) {
            return null;
        }
        return CMFile.fromImpl(impl.getFile());
    }

    @Override
    public int getLine() {
        return impl.getLine();
    }

    @Override
    public int getColumn() {
        return impl.getColumn();
    }

    @Override
    public int getOffset() {
        return impl.getOffset();
    }

    /**
     * If the location refers into a macro expansion, retrieves the location of
     * the macro expansion, otherwise returns this.
     * @return 
     */
    public CMSourceLocation getExpansionLocation() {
        CMSourceLocationImplementation other = impl.getExpansionLocation();
        if (impl.equals(other)) {
            return this;
        } else {
            return new CMSourceLocation(other);
        }
    }

    /**
     * If the location refers into a macro instantiation, return where the
     * location was originally spelled in the source file, otherwise returns
     * this.
     * @return 
     */
    public CMSourceLocation getSpellingLocation() {
        CMSourceLocationImplementation other = impl.getExpansionLocation();
        if (impl.equals(other)) {
            return this;
        } else {
            return new CMSourceLocation(other);
        }
    }

    /**
     * \brief Returns true if the given source location is in a system header.
     * @return 
     */
    public boolean isInSystemHeader() {
        return impl.isInSystemHeader();
    }

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
     * @return 
     */
    public Presumed getPresumedLocation() {
        return new Presumed(impl.getPresumedLocation());
    }

    /**
     * \brief Represents an unpacked "presumed" location which can be presented
     * to the user. A 'presumed' location can be modified by \#line and GNU line
     * marker directives and is always the expansion point of a normal location.
     */
    public static final class Presumed {

        /**
         * \brief Return true if this object is invalid or uninitialized.
         *
         * This occurs when created with invalid source locations or when
         * walking off the top of a \#include stack.
         * @return 
         */
        public boolean isValid() {
            return impl.isValid();
        }

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
        public CharSequence getFileName() {
            return impl.getFileName();
        }

        /**
         * \brief Return the presumed line number of this location. This can be
         * affected by \#line etc.
         *
         * @return line. For an invalid source location, zero is returned.
         */
        public int getLine() {
            return impl.getLine();
        }

        /**
         * \brief Return the presumed column of this location. This can be
         * affected by \#line etc.
         *
         * @return column. For an invalid source location, zero is returned.
         */
        public int getColumn() {
            return impl.getColumn();
        }

        //<editor-fold defaultstate="collapsed" desc="hidden">
        private final CMSourceLocationImplementation.PresumedImplementation impl;

        /*package*/
        Presumed(CMSourceLocationImplementation.PresumedImplementation impl) {
            this.impl = impl;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 23 * hash + this.impl.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (obj instanceof Presumed) {
                return this.impl.equals(((Presumed) obj).impl);
            }
            return false;
        }

        @Override
        public String toString() {
            return "CMSourceLocation.Presumed{" + impl + '}'; // NOI18N
        }
        //</editor-fold>
    }
    //<editor-fold defaultstate="collapsed" desc="hidden">
    private final CMSourceLocationImplementation impl;

    private CMSourceLocation(CMSourceLocationImplementation impl) {
        this.impl = impl;
    }

    static CMSourceLocation fromImpl(CMSourceLocationImplementation impl) {
        return new CMSourceLocation(impl);
    }
    
    CMSourceLocationImplementation getImpl() {
        return impl;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.impl.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof CMSourceLocation) {
            return this.impl.equals(((CMSourceLocation) obj).impl);
        }
        return false;
    }

    @Override
    public String toString() {
        return CMTraceUtils.toString(this);
    }
    //</editor-fold>
}
