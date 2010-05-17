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

import java.util.Collections;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * Provider for fake web module extenders.
 * @author Tomas Mysik
 */
public class FakeWebModuleExtender extends WebModuleExtender implements ChangeListener {
    private final FakeWebFrameworkProvider.FakeWebFrameworkProviderImpl fakeProvider;
    private final String name;
    private final String codeNameBase;
    private final WebModule wm;
    private final ExtenderController controller;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private JComponent component;
    private volatile WebModuleExtender delegate;

    FakeWebModuleExtender(FakeWebFrameworkProvider.FakeWebFrameworkProviderImpl fakeProvider, final String name, final String codeNameBase,
            WebModule wm, ExtenderController controller) {
        assert fakeProvider != null;
        assert name != null;
        assert codeNameBase != null;

        this.fakeProvider = fakeProvider;
        this.name = name;
        this.codeNameBase = codeNameBase;
        this.wm = wm;
        this.controller = controller;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    /**
     * We have to return the same instance for all the method call, see {@link WebModuleExtender#getComponent()}.
     * @return
     */
    @Override
    public JComponent getComponent() {
        if (component != null) {
            return component;
        }
        component = new FakeWebFrameworkConfigurationPanel(this, name, codeNameBase);
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        if (getDelegate() != null) {
            return getDelegate().getHelp();
        }
        return null;
    }

    @Override
    public void update() {
        if (getDelegate() != null) {
            getDelegate().update();
        }
    }

    @Override
    public boolean isValid() {
        if (getDelegate() != null) {
            return getDelegate().isValid();
        }
        return false;
    }

    @Override
    public Set<FileObject> extend(WebModule webModule) {
        if (getDelegate() != null) {
            return getDelegate().extend(webModule);
        }
        return Collections.<FileObject>emptySet();
    }

    void setWebFrameworkProvider(WebFrameworkProvider webFrameworkProvider) {
        fakeProvider.setDelegate(webFrameworkProvider);
    }

    public WebModuleExtender getDelegate() {
        if (delegate != null) {
            return delegate;
        }
        WebFrameworkProvider provider = fakeProvider.getDelegate();
        if (provider != null) {
            delegate = provider.createWebModuleExtender(wm, controller);
            delegate.addChangeListener(this);
        }
        return delegate;
    }

    public String getFrameworkProviderClassName() {
        return fakeProvider.getFrameworkProviderClassName();
    }

    public boolean isModulePresent() {
        return fakeProvider.isModulePresent();
    }

    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }
}
