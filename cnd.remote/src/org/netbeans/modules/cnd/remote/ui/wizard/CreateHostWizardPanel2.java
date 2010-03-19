/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.remote.ui.wizard;

import java.util.concurrent.Future;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/*package*/ final class CreateHostWizardPanel2 implements WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor>, WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private CreateHostVisualPanel2 component;
    private ExecutionEnvironment lastValidatedHost;
    private final CreateHostData data;
    private Future<Boolean> validationTask;
    private WizardDescriptor settings;

    public CreateHostWizardPanel2(CreateHostData data) {
        this.data = data;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public CreateHostVisualPanel2 getComponent() {
        if (component == null) {
            component = new CreateHostVisualPanel2(data, this);
        }
        return component;
    }

    @Override
    public void prepareValidation() {
        component.enableControls(false);
    }

    @Override
    public void validate() throws WizardValidationException {
        ExecutionEnvironment host = component.getHost();

        if (host == null || !host.equals(lastValidatedHost)) {
            validationTask = component.validateHost();

            try {
                validationTask.get();
            } catch (Exception ex) {
                // just skip it 
                // component.getHost() == null will indicate that validation
                // failed
            } finally {
                validationTask = null;
            }
        }

        component.enableControls(true);

        if (component.getHost() == null) {
            String errMsg = NbBundle.getMessage(getClass(), "MSG_Failure");
            throw new WizardValidationException(component, errMsg, errMsg);
        }

        lastValidatedHost = host;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("NewRemoteDevelopmentHostWizardP2");
    }

    @Override
    public boolean isValid() {
        boolean res = component.canValidateHost();
        if (res) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N
        } else {
            String message = NbBundle.getMessage(getClass(), "MSG_HostAlreadyAdded"); // NOI18N
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
        }
        return res;
    }
    ////////////////////////////////////////////////////////////////////////////
    // change support
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    // 
    // This method (removeChangeListener) is called when we go away from
    // the panel.
    // If it happens when we are in the host validation phase and to step back,
    // then both buttons (prev, next) will be disabled (validationRuns == true)
    // See WizardDescriptor:893
    //         boolean valid = p.isValid () && !validationRuns;
    //         nextButton.setEnabled (next && valid);
    //
    // So here we should interrupt the validation process.
    //
    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);

        if (validationTask != null && !validationTask.isDone()) {
            try {
                validationTask.cancel(true);
            } finally {
                validationTask = null;
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    ////////////////////////////////////////////////////////////////////////////
    // settings
    @Override
    public void readSettings(WizardDescriptor settings) {
        this.settings = settings;
        getComponent().init();
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        data.setExecutionEnvironment(getComponent().getHost());
        data.setRunOnFinish(getComponent().getRunOnFinish());
    }
}
