/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.codemodel;

import org.netbeans.modules.cnd.spi.codemodel.CMTokenImplementation;
import org.netbeans.modules.cnd.spi.codemodel.support.IterableFactory;
import org.netbeans.modules.cnd.spi.codemodel.trace.CMTraceUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class CMToken {
    /**
     * \brief Retrieve the token kind.
     *
     * @return token kind
     */
    public CMTokenKind getKind() {
        return impl.getKind();
    }
  
    /**
     * \brief Retrieve a spelling representing the token.
     *
     * @param tu context translation unit
     * @return token spelling
     */
    public CharSequence getSpelling(CMTranslationUnit tu) {
        return impl.getSpelling(tu.getImpl());
    }
  
    /**
     * \brief Retrieve a source location representing the token.
     *
     * @param tu context translation unit
     * @return token location
     */
    public CMSourceLocation getLocation(CMTranslationUnit tu) {
        return CMSourceLocation.fromImpl(impl.getLocation(tu.getImpl()));
    }

    /**
     * \brief Retrieve a source range that covers the given token.
     *
     * @param tu context translation unit
     * @return token range
     */
    public CMSourceRange getExtent(CMTranslationUnit tu) {
        return CMSourceRange.fromImpl(impl.getExtent(tu.getImpl()));
    }
    
    //<editor-fold defaultstate="collapsed" desc="hidden">
    private final CMTokenImplementation impl;

    private CMToken(CMTokenImplementation impl) {
        this.impl = impl;
    }

    /*package*/
    static CMToken fromImpl(CMTokenImplementation impl) {
        // TODO: share instance for the same impl if needed
        return new CMToken(impl);
    }

    /*package*/
    static Iterable<CMToken> fromImpls(Iterable<CMTokenImplementation> impls) {
        return IterableFactory.convert(impls, CONV);
    }

    /*package*/
    CMTokenImplementation getImpl() {
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
        if (obj instanceof CMToken) {
            return this.impl.equals(((CMToken) obj).impl);
        }
        return false;
    }

    @Override
    public String toString() {
        return CMTraceUtils.toString(this);
    }

    private static final IterableFactory.Converter<CMTokenImplementation, CMToken> CONV
            = new IterableFactory.Converter<CMTokenImplementation, CMToken>() {

        @Override
        public CMToken convert(CMTokenImplementation in) {
            return fromImpl(in);
        }
    };
    //</editor-fold>  
}
