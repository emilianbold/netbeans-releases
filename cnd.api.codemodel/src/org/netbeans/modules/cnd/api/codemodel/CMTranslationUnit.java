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

import java.net.URI;
import org.netbeans.modules.cnd.spi.codemodel.CMFileImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMIndexImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMTranslationUnitImplementation;
import org.netbeans.modules.cnd.spi.codemodel.support.IterableFactory;

/**
 * \brief A single translation unit, which resides in an index.
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public final class CMTranslationUnit {

    /**
     * \brief Retrieve a file handle within the given translation unit.
     *
     * @param file URI of the file.
     *
     * @return the file handle for the named file in the translation unit \p tu,
     * or a NULL file handle if the file was not a part of this translation
     * unit.
     */
    public CMFile getFile(URI file) {
        CMFileImplementation fileImpl = impl.getFile(file);
        final CMFile res = fileImpl == null ? null : CMFile.fromImpl(fileImpl);
        // WORKAROUND: getFile returns non-null even if file is not a part of TU, so we do extra check
        if (res != null && getLocation(res, 0).isValid()) {
            return res;
        }
        return null;
    }

    /**
     * @return Get the original translation unit source file path.
     */
    public CharSequence getMainFilePath() {
        return impl.getMainFilePath();
    }
    
    /**
     * \brief Retrieves the source location associated with a given
     * file/line/column in a particular translation unit.
     *
     * @param file
     * @param line
     * @param column
     * @return
     */
    public CMSourceLocation getLocation(CMFile file, int line, int column) {
        return CMSourceLocation.fromImpl(impl.getLocation(file.getImpl(), line, column));
    }

    /**
     * \brief Retrieves the source location associated with a given character
     * offset in a particular translation unit.
     *
     * @param file
     * @param offset
     * @return
     */
    public CMSourceLocation getLocation(CMFile file, int offset) {
        return CMSourceLocation.fromImpl(impl.getLocation(file.getImpl(), offset));
    }

    /**
     * \brief Map a source location to the cursor that describes the entity at
     * that location in the source code.
     *
     * getCursor() maps an arbitrary source location within a translation unit
     * down to the most specific cursor that describes the entity at that
     * location. For example, given an expression \c x + y, invoking
     * clang_getCursor() with a source location pointing to "x" will return the
     * cursor for "x"; similarly for "y". If the cursor points anywhere between
     * "x" or "y" (e.g., on the + or the whitespace around it),
     * clang_getCursor() will return a cursor referring to the "+" expression.
     *
     * @param loc source location
     * @return a cursor representing the entity at the given source location, or
     * a invalid NULL-cursor if no such entity can be found.
     */
    public CMCursor getCursor(CMSourceLocation loc) {
        return CMCursor.fromImpl(impl.getCursor(loc.getImpl()));
    }

    public CMCursor getCursor(CMFile file, int offset) {
        return CMCursor.fromImpl(impl.getCursor(file.getImpl(), offset));
    }

    /**
     * \brief Retrieve the cursor that represents the given translation unit.
     *
     * @return The translation unit cursor can be used to start traversing the various
     * declarations within the given translation unit.
     */
    public CMCursor getRootCursor() {
        return CMCursor.fromImpl(impl.getRootCursor());
    }
    
    /**
     * \brief Retrieve the complete set of diagnostics associated with a
     * translation unit.
     *
     * @return
     */
    public CMDiagnosticSet getDiagnostics() {
        return new CMDiagnosticSet(impl.getDiagnostics());
    }

    /**
     * Retrieve range for the whole file
     * 
     * @param uri
     * @return range
     */
    public CMSourceRange getRangeForFile(URI uri) {
        return CMSourceRange.fromImpl(impl.getRangeForFile(uri));
    }

    public CMIndex getIndex() {
        CMIndexImplementation indexImpl = impl.getIndex();
        return CMIndex.fromImpl(indexImpl);
    }

    //<editor-fold defaultstate="collapsed" desc="hidden">
    private final CMTranslationUnitImplementation impl;

    private CMTranslationUnit(CMTranslationUnitImplementation impl) {
        assert impl != null;
        this.impl = impl;
    }

    /*package*/
    static CMTranslationUnit fromImpl(CMTranslationUnitImplementation impl) {
        // FIXME: it's worth to share TU instances for the same impl
        return new CMTranslationUnit(impl);
    }

    /*package*/
    static Iterable<CMTranslationUnit> fromImpls(Iterable<? extends CMTranslationUnitImplementation> impls) {
        return IterableFactory.convert(impls, CONV);
    }

    /*package*/
    CMTranslationUnitImplementation getImpl() {
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
        if (obj instanceof CMTranslationUnit) {
            return this.impl.equals(((CMTranslationUnit) obj).impl);
        }
        return false;
    }

    @Override
    public String toString() {
        return "CMTranslationUnit{" + impl + '}'; // NOI18N
    }

    private static final IterableFactory.Converter<CMTranslationUnitImplementation, CMTranslationUnit> CONV
            = new IterableFactory.Converter<CMTranslationUnitImplementation, CMTranslationUnit>() {

                @Override
                public CMTranslationUnit convert(CMTranslationUnitImplementation in) {
                    return fromImpl(in);
                }
            };
    //</editor-fold>
}
