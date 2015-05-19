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
import org.netbeans.modules.cnd.spi.codemodel.visit.CMEntityImplementation;

/**
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public final class CMEntity extends CMObject {

    public enum Kind {

        Invalid(-1),
        Unexposed(0),
        Typedef(1),
        Function(2),
        Variable(3),
        Field(4),
        EnumConstant(5),
        ObjCClass(6),
        ObjCProtocol(7),
        ObjCCategory(8),
        ObjCInstanceMethod(9),
        ObjCClassMethod(10),
        ObjCProperty(11),
        ObjCIvar(12),
        Enum(13),
        Struct(14),
        Union(15),
        CXXClass(16),
        CXXNamespace(17),
        CXXNamespaceAlias(18),
        CXXStaticVariable(19),
        CXXStaticMethod(20),
        CXXInstanceMethod(21),
        CXXConstructor(22),
        CXXDestructor(23),
        CXXConversionFunction(24),
        CXXTypeAlias(25),
        CXXInterface(26);

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

    /**
     * \brief Extra C++ template information for an entity. This can apply to
     * kinds: Function CXXClass CXXStaticMethod CXXInstanceMethod CXXConstructor
     * CXXConversionFunction CXXTypeAlias
     */
    public enum CXXTemplateKind {

        Invalid(-1),
        NonTemplate(0),
        Template(1),
        TemplatePartialSpecialization(2),
        TemplateSpecialization(3);

        //<editor-fold defaultstate="collapsed" desc="hidden">
        public static CXXTemplateKind valueOf(int val) {
            byte langVal = (byte) val;
            for (CXXTemplateKind kind : CXXTemplateKind.values()) {
                if (kind.value == langVal) {
                    return kind;
                }
            }
            assert false : "unsupported kind " + val;
            return Invalid;
        }

        private final byte value;

        private CXXTemplateKind(int lang) {
            this.value = (byte) lang;
        }

        public int getValue() {
            return value;
        }
        //</editor-fold>
    }

    /// \brief A C++ access specifier (public, private, protected), plus the
    /// special value "none" which means different things in different contexts.
    public enum AccessSpecifierKind {

        Public(0),
        Protected(1),
        Private(2),
        None(3);
        //<editor-fold defaultstate="collapsed" desc="hidden">

        public static AccessSpecifierKind valueOf(int val) {
            byte langVal = (byte) val;
            for (AccessSpecifierKind kind : AccessSpecifierKind.values()) {
                if (kind.value == langVal) {
                    return kind;
                }
            }
            assert false : "unsupported kind " + val;
            return None;
        }

        private final byte value;

        private AccessSpecifierKind(int lang) {
            this.value = (byte) lang;
        }

        public int getValue() {
            return value;
        }
        //</editor-fold>
    };

    /// \brief Storage classes.
    public enum StorageClassKind {
        // These are legal on both functions and variables.

        None(0),
        Extern(1),
        Static(2),
        PrivateExtern(3),
        // These are only legal on variables.
        OpenCLWorkGroupLocal(4),
        Auto(5),
        Register(6);

        /// \brief Checks whether the given storage class is legal for functions.
        boolean isLegalForFunction() {
            return value <= PrivateExtern.value;
        }

        /// \brief Checks whether the given storage class is legal for variables.
        boolean isLegalForVariable() {
            return true;
        }
        
        //<editor-fold defaultstate="collapsed" desc="hidden">

        public static StorageClassKind valueOf(int val) {
            byte langVal = (byte) val;
            for (StorageClassKind kind : StorageClassKind.values()) {
                if (kind.value == langVal) {
                    return kind;
                }
            }
            assert false : "unsupported kind " + val;
            return None;
        }

        private final byte value;

        private StorageClassKind(int lang) {
            this.value = (byte) lang;
        }

        public int getValue() {
            return value;
        }
        //</editor-fold>
    };

    public Kind getKind() {
        return impl.getKind();
    }

    public CXXTemplateKind getTemplateKind() {
        return impl.getTemplateKind();
    }

    public AccessSpecifierKind getAccessSpecifierKind() {
        return impl.getAccessSpecifierKind();
    }

    public StorageClassKind getStorageClassKind() {
        return impl.getStorageClassKind();
    }

    public boolean isVirtual() {
        return impl.isVirtual();
    }
    
    public CharSequence getName() {
        return impl.getName();
    }

    public CMUnifiedSymbolResolution getUSR() {
        return impl.getUSR();
    }

//    /**
//     * canonical declaration cursor.
//     *
//     * @return cursor for canonical declaration
//     */
//    public CMCursor getCanonical() {
//        return CMFactory.CoreAPI.createCursor(impl.getCanonical());
//    }
    //<editor-fold defaultstate="collapsed" desc="hidden">
    private final CMEntityImplementation impl;

    private CMEntity(CMEntityImplementation impl) {
        this.impl = impl;
    }

    /*package*/
    static CMEntity fromImpl(CMEntityImplementation impl) {
        return new CMEntity(impl);
    }

    /*package*/
    static Iterable<CMEntity> fromImpls(Iterable<CMEntityImplementation> impls) {
        return IterableFactory.convert(impls, CONV);
    }

    /*package*/
    CMEntityImplementation getImpl() {
        return impl;
    }
   /**
     * canonical declaration cursor.
     *
     * @return cursor for canonical declaration
     */
    public CMCursor getCanonical() {
        return CMFactory.CoreAPI.createCursor(impl.getCanonical());
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
        if (obj instanceof CMEntity) {
            return this.impl.equals(((CMEntity) obj).impl);
        }
        return false;
    }

    @Override
    public String toString() {
        return CMTraceUtils.toString(this);
    }

    private static final IterableFactory.Converter<CMEntityImplementation, CMEntity> CONV
            = new IterableFactory.Converter<CMEntityImplementation, CMEntity>() {

                @Override
                public CMEntity convert(CMEntityImplementation in) {
                    return fromImpl(in);
                }
            };
    //</editor-fold>
}
