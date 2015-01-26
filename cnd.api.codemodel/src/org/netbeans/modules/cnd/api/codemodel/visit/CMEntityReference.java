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
import org.netbeans.modules.cnd.api.codemodel.CMUnifiedSymbolResolution;
import org.netbeans.modules.cnd.spi.codemodel.support.CMFactory;
import org.netbeans.modules.cnd.spi.codemodel.support.IterableFactory;
import org.netbeans.modules.cnd.spi.codemodel.trace.CMTraceUtils;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMEntityReferenceImplementation;

/**
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public class CMEntityReference {

    public enum Kind {

        Invalid(0),
        /**
         * \brief The entity is referenced directly in user's code.
         */
        Direct(1),
        /**
         * \brief An implicit reference, e.g. a reference of an ObjC method via
         * the dot syntax.
         */
        Implicit(2);

        //<editor-fold defaultstate="collapsed" desc="hidden">
        public static Kind valueOf(int val) {
            byte langVal = (byte) val;
            for (Kind kind : Kind.values()) {
                if (kind.value == langVal) {
                    return kind;
                }
            }
            assert false : "unsupported kind " + val;
            return Invalid;
        }

        private final byte value;

        private Kind(int lang) {
            this.value = (byte) lang;
        }

        public int getValue() {
            return value;
        }
        //</editor-fold>
    }

    public Kind getKind() {
        return impl.getKind();
    }

    /**
     * \brief Reference cursor.
     */
    public CMCursor getCursor() {
        return CMFactory.CoreAPI.createCursor(impl.getCursor());
    }

    public CMVisitLocation getLocation() {
        return CMVisitLocation.fromImpl(impl.getLocation());
    }

    /**
     * \brief The entity that gets referenced.
     */
    public CMEntity getReferencedEntity() {
        return CMEntity.fromImpl(impl.getReferencedEntity());
    }

    /**
     * \brief Immediate "parent" of the reference. For example:
     *
     * \code Foo *var; \endcode
     *
     * The parent of reference of type 'Foo' is the variable 'var'. For
     * references inside statement bodies of functions/methods, the parentEntity
     * will be the function/method.
     */
    public CMEntity getReferenceParentEntity() {
        return CMEntity.fromImpl(impl.getReferenceParentEntity());
    }

    public CMUnifiedSymbolResolution getReferenceLexicalContainerUSR() {
        return impl.getReferenceLexicalContainerUSR();
    }

    public CharSequence getReferenceLexicalContainerDisplayName() {
        return impl.getReferenceLexicalContainerDisplayName();
    }

    /**
     * \brief Lexical container context of the reference.
     */
    public CMDeclarationContainer getReferenceLexicalContainer() {
        return CMDeclarationContainer.fromImpl(impl.getLexicalContainer());
    }

    //<editor-fold defaultstate="collapsed" desc="hidden">
    private final CMEntityReferenceImplementation impl;

    private CMEntityReference(CMEntityReferenceImplementation impl) {
        this.impl = impl;
    }

    /*package*/
    static CMEntityReference fromImpl(CMEntityReferenceImplementation impl) {
        return new CMEntityReference(impl);
    }

    /*package*/
    static Iterable<CMEntityReference> fromImpls(Iterable<CMEntityReferenceImplementation> impls) {
        return IterableFactory.convert(impls, CONV);
    }

    /*package*/
    CMEntityReferenceImplementation getImpl() {
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
        if (obj instanceof CMEntityReference) {
            return this.impl.equals(((CMEntityReference) obj).impl);
        }
        return false;
    }

    @Override
    public String toString() {
        return CMTraceUtils.toString(this);
    }

    private static final IterableFactory.Converter<CMEntityReferenceImplementation, CMEntityReference> CONV
            = new IterableFactory.Converter<CMEntityReferenceImplementation, CMEntityReference>() {

                @Override
                public CMEntityReference convert(CMEntityReferenceImplementation in) {
                    return fromImpl(in);
                }
            };

    //</editor-fold>
}
