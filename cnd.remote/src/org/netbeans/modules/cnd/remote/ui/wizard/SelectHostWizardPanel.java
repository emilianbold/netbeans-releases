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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.ui.wizard;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsCacheManager;
import org.netbeans.modules.cnd.remote.ui.setup.CreateHostWizardIterator;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Kvashin
 */
public class SelectHostWizardPanel implements
        WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor>,
        ChangeListener {
    
    private final ChangeListener changeListener;
    private final boolean allowLocal;
    private SelectHostVisualPanel component;
    private final CreateHostData createHostData;
    private final ToolsCacheManager cacheManager;
    private final CreateHostWizardPanel1 delegate;
    private final AtomicBoolean setupNewHost;
    private WizardDescriptor wizardDescriptor;

    public SelectHostWizardPanel(boolean allowLocal, ChangeListener changeListener) {
        this.allowLocal = allowLocal;
        this.changeListener = changeListener;
        cacheManager = ToolsCacheManager.createInstance(true);
        createHostData = new CreateHostData(cacheManager);
        delegate = new CreateHostWizardPanel1(createHostData);
        delegate.addChangeListener(this);
        setupNewHost = new AtomicBoolean();
        if (allowLocal) {
            setupNewHost.set(ServerList.getRecords().isEmpty());
        } else {
            setupNewHost.set(ServerList.getRecords().size() <= 1);
        }
    }

    @Override
    public SelectHostVisualPanel getComponent() {
        synchronized (this) {
            if (component == null) {                
                component = new SelectHostVisualPanel(this, allowLocal, delegate.getComponent(), setupNewHost);
            }
        }
        return component;
    }

    public WizardDescriptor.Panel[] getAdditionalPanels() {
        return new WizardDescriptor.Panel[] {
            new CreateHostWizardPanel2(createHostData),
            new CreateHostWizardPanel3(createHostData)
        };
    }

    public boolean isNewHost() {
        return setupNewHost.get();
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void prepareValidation() {
        getComponent().enableControls(false);
    }

    @Override
    public void validate() throws WizardValidationException {
        ExecutionEnvironment execEnv = getComponent().getSelectedHost();
        try {
            if (execEnv != null) {
                ConnectionManager.getInstance().connectTo(execEnv);
            }
        } catch (IOException ex) {
            String message = NbBundle.getMessage(getClass(), "CannotConnectMessage");
            throw new WizardValidationException(getComponent(), message, message);
        } catch (CancellationException ex) {
            // nothing
        } finally {
            getComponent().enableControls(true);
        }
    }

    @Override
    public boolean isValid() {
        if (setupNewHost.get()) {
            return delegate.isValid();
        } else {
            return getComponent().getSelectedHost() != null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // change support
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        delegate.readSettings(settings);
        this.wizardDescriptor = settings;
        getComponent().reset();
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        delegate.storeSettings(settings);
        ExecutionEnvironment env = getComponent().isExistent() ? getComponent().getSelectedHost() : null;
        settings.putProperty("hostUID", (env == null) ? null : ExecutionEnvironmentFactory.toUniqueID(env)); // NOI18N
        settings.putProperty("ToolsCacheManager", createHostData.getCacheManager());
    }

    ExecutionEnvironment getSelectedHost() {
        return getComponent().getSelectedHost();
    }

    void apply() {
        if (isNewHost()) {
            CreateHostWizardIterator.applyHostSetup(cacheManager, createHostData);
        }
    }
}
