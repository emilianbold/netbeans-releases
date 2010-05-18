/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.fake.frameworks;

import java.io.File;
import java.util.Map;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 * Provider for fake web frameworks.
 * @author Tomas Mysik
 */
public final class FakeWebFrameworkProvider {

    private FakeWebFrameworkProvider() {
    }

    public  static  WebFrameworkProvider create(Map<Object, Object> attrs) {
        final String name = (String) attrs.get("name"); // NOI18N
        final String codeNameBase = (String) attrs.get("codeNameBase"); // NOI18N
        final String frameworkProviderClassName = (String) attrs.get("frameworkProviderClassName"); // NOI18N
        assert name != null;
        assert codeNameBase != null;
        assert frameworkProviderClassName != null;

        boolean modulePresent = false;
        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (codeNameBase.equals(mi.getCodeNameBase())) {
                if (mi.isEnabled()) {
                    return null;
                }
                modulePresent = true;
                break;
            }
        }

        return new FakeWebFrameworkProviderImpl(name, codeNameBase, frameworkProviderClassName, modulePresent);
    }

    static final class FakeWebFrameworkProviderImpl extends WebFrameworkProvider {
        private final String name;
        private final String codeNameBase;
        private final String frameworkProviderClassName;
        private final boolean modulePresent;
        private volatile WebFrameworkProvider delegate;

        FakeWebFrameworkProviderImpl(String name, String codeNameBase, String frameworkProviderClassName, boolean modulePresent) {
            super(name, null);

            assert name != null;
            assert codeNameBase != null;
            assert frameworkProviderClassName != null;

            this.name = name;
            this.codeNameBase = codeNameBase;
            this.frameworkProviderClassName = frameworkProviderClassName;
            this.modulePresent = modulePresent;
        }

        @Override
        public WebModuleExtender createWebModuleExtender(WebModule wm, ExtenderController controller) {
            if (delegate != null) {
                return delegate.createWebModuleExtender(wm, controller);
            }
            return new FakeWebModuleExtender(this, name, codeNameBase, wm, controller);
        }

        @Override
        public String getName() {
            if (delegate != null) {
                return delegate.getName();
            }
            return name;
        }

        public String getFrameworkProviderClassName() {
            return frameworkProviderClassName;
        }

        @Override
        public boolean isInWebModule(WebModule wm) {
            if (delegate != null) {
                return delegate.isInWebModule(wm);
            }
            return false;
        }

        @Override
        public File[] getConfigurationFiles(WebModule wm) {
            if (delegate != null) {
                return delegate.getConfigurationFiles(wm);
            }
            return new File[0];
        }

        public WebFrameworkProvider getDelegate() {
            return delegate;
        }

        public void setDelegate(WebFrameworkProvider delegate) {
            assert delegate != null;
            this.delegate = delegate;
        }

        public boolean isModulePresent() {
            return modulePresent;
        }
    }
}
