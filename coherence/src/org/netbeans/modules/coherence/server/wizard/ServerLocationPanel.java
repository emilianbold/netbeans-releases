/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.server.wizard;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * {@code WizardDescriptor.Panel} container for {@link ServerLocationVisual} panel.
 *
 */
public class ServerLocationPanel implements WizardDescriptor.Panel, ChangeListener {

    private final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();
    private ServerLocationVisual component;
    private WizardDescriptor wizardDescriptor;

    private transient CoherenceWizardIterator instantiatingIterator;

    public ServerLocationPanel(CoherenceWizardIterator instantiatingIterator) {
        this.instantiatingIterator = instantiatingIterator;
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new ServerLocationVisual();
            component.addChangeListener(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void readSettings(Object settings) {
        if (wizardDescriptor == null) {
            wizardDescriptor = (WizardDescriptor) settings;
        }
    }

    @Override
    public void storeSettings(Object settings) {
    }

    @Override
    public boolean isValid() {
        if (!component.valid(wizardDescriptor)) {
            component.fillInCoherenceClasspath(false);
            return false;
        }

        // if the properties are valid, store them
        component.fillInCoherenceClasspath(true);
        instantiatingIterator.setCoherenceLocation(component.getServerLocation());
        instantiatingIterator.setCoherenceClasspath(component.getClasspath());
        instantiatingIterator.setCreateCoherenceLibrary(component.getCreateCoherenceLibrary());
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent(e);
    }

    private void fireChangeEvent(ChangeEvent e) {
        for (ChangeListener listener : listeners) {
            listener.stateChanged(e);
        }
    }

}
