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
import org.netbeans.modules.cnd.api.codemodel.visit.CMObject;
import org.netbeans.modules.cnd.spi.codemodel.CMFileImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMFileImplementation.StatInfoImplementation;

/**
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public final class CMFile extends CMObject {

    /**
     * \brief Retrieve the URI of the given file.
     *
     * @return
     */
    public URI getURI() {
        return impl.getURI();
    }

    /**
     * \brief Retrieve the complete file and path name of the given file.
     *
     * @return
     */
    public CharSequence getFilePath() {
        return impl.getFilePath();
    }

    /**
     * \brief Retrieve the complete file name of the given file.
     *
     * @return
     */
    public CharSequence getName() {
        return impl.getName();
    }

    /**
     * \brief Retrieve the last modification time of the given file.
     *
     * @return
     */
    public long getLastModified() {
        return impl.getLastModified();
    }

    /**
     * \brief Uniquely identifies physical CMFile, that refers to the same
     * underlying file.
     *
     * @return
     */
    public StatInfo getStatInfo() {
        return StatInfo.fromImpl(impl.getStatInfo());
    }

    /**
     * \brief Uniquely identifies a CMFile, that refers to the same underlying
     * file.
     */
    public static final class StatInfo {

        public long getInode() {
            return impl.getInode();
        }

        public long getDevice() {
            return impl.getDevice();
        }

        //<editor-fold defaultstate="collapsed" desc="hidden">
        private final StatInfoImplementation impl;

        private StatInfo(StatInfoImplementation impl) {
            assert impl != null;
            this.impl = impl;
        }

        /*package*/
        static StatInfo fromImpl(StatInfoImplementation impl) {
            return new StatInfo(impl);
        }

        /*package*/
        StatInfoImplementation getImpl() {
            return impl;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + this.impl.hashCode();
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
            if (obj instanceof StatInfo) {
                return this.impl.equals(((StatInfo) obj).impl);
            }
            return false;
        }

        @Override
        public String toString() {
            return "StatInfo{" + impl + '}'; // NOI18N
        }
        //</editor-fold>
    }

    //<editor-fold defaultstate="collapsed" desc="private">
    private final CMFileImplementation impl;

    private CMFile(CMFileImplementation impl) {
        assert impl != null;
        this.impl = impl;
    }

    /*package*/
    static CMFile fromImpl(CMFileImplementation impl) {
        // FIXME: it's worth to share File instances for the same impl
        return new CMFile(impl);
    }

    /*package*/
    CMFileImplementation getImpl() {
        return impl;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + this.impl.hashCode();
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
        if (obj instanceof CMFile) {
            return this.impl.equals(((CMFile) obj).impl);
        }
        return false;
    }

    //</editor-fold>
}
