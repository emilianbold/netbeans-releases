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
package org.netbeans.modules.cnd.api.codemodel.visit;

import org.netbeans.modules.cnd.api.codemodel.CMFile;
import org.netbeans.modules.cnd.api.codemodel.CMLocation;
import org.netbeans.modules.cnd.api.codemodel.CMSourceLocation;
import org.netbeans.modules.cnd.spi.codemodel.support.CMFactory;
import org.netbeans.modules.cnd.spi.codemodel.support.IterableFactory;
import org.netbeans.modules.cnd.spi.codemodel.trace.CMTraceUtils;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMVisitLocationImplementation;

/**
 * \brief Source location passed to visitors callbacks.
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public final class CMVisitLocation implements CMLocation {

    /**
     * \brief Retrieve the CXIdxFile, file, line, column, and offset represented
     * by the given visit location.
     *
     * If the location refers into a macro expansion, retrieves the location of
     * the macro expansion and if it refers into a macro argument retrieves the
     * location of the argument.
     *
     * @return file
     */
    @Override
    public CMFile getFile() {
        return CMFactory.CoreAPI.createFile(impl.getFile());
    }

    /**
     * \brief Retrieve the file, line, column, and offset represented by the
     * given visit location.
     *
     * If the location refers into a macro expansion, retrieves the location of
     * the macro expansion and if it refers into a macro argument retrieves the
     * location of the argument.
     *
     * @return line,column,offset
     */
    @Override
    public int getLine() {
        return impl.getLine();
    }

    @Override
    public int getColumn() {
        return impl.getColumn();
    }

    @Override
    public int getOffset()  {
        return impl.getOffset();
    }

    /**
     * \brief Retrieve the CXSourceLocation represented by the given visit
     * location.
     *
     * @return associated source location
     */
    public CMSourceLocation getLocation() {
        return CMFactory.CoreAPI.createSourceLocation(impl.getLocation());
    }
    //<editor-fold defaultstate="collapsed" desc="hidden">
    private final CMVisitLocationImplementation impl;

    private CMVisitLocation(CMVisitLocationImplementation impl) {
        this.impl = impl;
    }

    /*package*/
    static CMVisitLocation fromImpl(CMVisitLocationImplementation impl) {
        return new CMVisitLocation(impl);
    }

    /*package*/
    static Iterable<CMVisitLocation> fromImpls(Iterable<CMVisitLocationImplementation> impls) {
        return IterableFactory.convert(impls, CONV);
    }

    /*package*/
    CMVisitLocationImplementation getImpl() {
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
        if (obj instanceof CMVisitLocation) {
            return this.impl.equals(((CMVisitLocation) obj).impl);
        }
        return false;
    }

    @Override
    public String toString() {
        return CMTraceUtils.toString(this);
    }

    private static final IterableFactory.Converter<CMVisitLocationImplementation, CMVisitLocation> CONV
            = new IterableFactory.Converter<CMVisitLocationImplementation, CMVisitLocation>() {

                @Override
                public CMVisitLocation convert(CMVisitLocationImplementation in) {
                    return fromImpl(in);
                }
            };
    //</editor-fold>
}
