/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import org.netbeans.modules.cnd.source.spi.CndCookieProvider;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node.Cookie;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Alexey Vladykin
 */
@ServiceProvider(service = CndCookieProvider.class)
public final class CppEditorSupportProvider extends CndCookieProvider {

    @Override
    public void addCookies(DataObject dao, CookieSet cookies) {
        MultiDataObject mdao = (MultiDataObject) dao;
        if (!MIMENames.isBinary(dao.getPrimaryFile().getMIMEType())){
            cookies.add(CppEditorSupport.class, new CppEditorSupportFactory(mdao, cookies));
        }
    }

    private static final class CppEditorSupportFactory implements CookieSet.Factory {

        private final MultiDataObject mdao;
        private final CookieSet cookies;
        private Reference<CppEditorSupport> cppEditorSupport;

        public CppEditorSupportFactory(MultiDataObject mdao, CookieSet cookies) {
            this.mdao = mdao;
            this.cookies = cookies;
        }

        @Override
        public <T extends Cookie> T createCookie(Class<T> klass) {
            return klass.cast(createCppEditorSupport());
        }

        private synchronized CppEditorSupport createCppEditorSupport() {
            CppEditorSupport support = (cppEditorSupport == null) ? null : cppEditorSupport.get();
            if (support == null) {
                support = new CppEditorSupport(mdao, cookies);
                cppEditorSupport = new SoftReference<CppEditorSupport>(support);
            }
            return support;
        }
    }
}
