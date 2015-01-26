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

import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.spi.codemodel.support.CMFactory;
import org.netbeans.modules.cnd.spi.codemodel.support.IterableFactory;
import org.netbeans.modules.cnd.spi.codemodel.trace.CMTraceUtils;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMDeclarationImplementation;

/**
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public final class CMDeclaration {

    public CMEntity getEntity() {
        return CMEntity.fromImpl(impl.getEntity());
    }

    public CMCursor getCursor() {
        return CMFactory.CoreAPI.createCursor(impl.getCursor());
    }

    public CMVisitLocation getLocation() {
        return CMVisitLocation.fromImpl(impl.getLocation());
    }

    public CMDeclarationContainer getSemanticContainer() {
        return CMDeclarationContainer.fromImpl(impl.getSemanticContainer());
    }

//    public CMUnifiedSymbolResolution getLexicalContainerUSR() {
//        return impl.getLexicalContainerUSR();
//    }

    /**
     * \brief Generally same as #semanticContainer but can be different in cases
     * like out-of-line C++ member functions.
     */
    public CMDeclarationContainer getLexicalContainer() {
        return CMDeclarationContainer.fromImpl(impl.getLexicalContainer());
    }

    public CMDeclarationContainer asContainer() {
        return CMDeclarationContainer.fromImpl(impl.asContainer());
    }

    public boolean isRedeclaration() {
        return impl.isRedeclaration();
    }

    public boolean isDefinition() {
        return impl.isDefinition();
    }

    public boolean isContainer() {
        return impl.isContainer();
    }

    /**
     * \brief Whether the declaration exists in code or was created implicitly
     * by the compiler, e.g. implicit objc methods for properties.
     */
    public boolean isImplicit() {
        return impl.isImplicit();
    }

    //<editor-fold defaultstate="collapsed" desc="hidden">
    private final CMDeclarationImplementation impl;

    private CMDeclaration(CMDeclarationImplementation impl) {
        this.impl = impl;
    }

    /*package*/
    static CMDeclaration fromImpl(CMDeclarationImplementation impl) {
        return new CMDeclaration(impl);
    }

    /*package*/
    static Iterable<CMDeclaration> fromImpls(Iterable<CMDeclarationImplementation> impls) {
        return IterableFactory.convert(impls, CONV);
    }

    /*package*/
    CMDeclarationImplementation getImpl() {
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
        if (obj instanceof CMDeclaration) {
            return this.impl.equals(((CMDeclaration) obj).impl);
        }
        return false;
    }

    @Override
    public String toString() {
        return CMTraceUtils.toString(this);
    }

    private static final IterableFactory.Converter<CMDeclarationImplementation, CMDeclaration> CONV
            = new IterableFactory.Converter<CMDeclarationImplementation, CMDeclaration>() {

                @Override
                public CMDeclaration convert(CMDeclarationImplementation in) {
                    return fromImpl(in);
                }
            };
    //</editor-fold>
}
