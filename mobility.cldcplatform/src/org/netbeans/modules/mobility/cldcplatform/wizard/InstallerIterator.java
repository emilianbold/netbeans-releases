/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.mobility.cldcplatform.wizard;

import org.openide.WizardDescriptor;
import org.netbeans.modules.mobility.cldcplatform.*;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.*;
import java.util.*;
import java.io.IOException;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar
 */
public class InstallerIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static InstallerIterator INSTANCE;
    
    ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
    int current;
    private WizardDescriptor.FinishablePanel[] panels;
    private WizardDescriptor wizardDescriptor;
    
    public static synchronized InstallerIterator getDefault() {
        if (INSTANCE == null)
            INSTANCE = new InstallerIterator();
        return INSTANCE;
    }
    
    public void addChangeListener(final ChangeListener changeListener) {
        listeners.add(changeListener);
    }
    
    public void removeChangeListener(final ChangeListener changeListener) {
        listeners.remove(changeListener);
    }
    
    public void fireChanged() {
        final ChangeEvent e = new ChangeEvent(this);
        for ( final ChangeListener l : listeners )
            l.stateChanged(e);
    }
    
    public WizardDescriptor.Panel current() {
        return panels[current];
    }
    
    public String name() {
        return NbBundle.getMessage(InstallerIterator.class, "Title_InstallIterator_Add_Mobile_Platforms"); //NOI18N
    }
    
    public boolean hasNext() {
        return current < panels.length - 1  &&  current().isValid();
    }
    
    public boolean hasPrevious() {
        return current > 0;
    }
    
    public void nextPanel() {
        assert hasNext();
        current ++;
    }
    
    public void previousPanel() {
        assert hasPrevious();
        current --;
    }
    
    public void initialize(final WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
        current = 0;
        panels = new WizardDescriptor.FinishablePanel[] {
            new FindWizardPanel(),
            new DetectWizardPanel(),
        };
        String[] strs = new String[panels.length];
        for (int i = 0; i < strs.length; i++)
            strs[i] = panels[i].getComponent().getName();
        ((JComponent)panels[0].getComponent()).putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, strs); // NOI18N
    }
    
    public void uninitialize(@SuppressWarnings("unused")
	final WizardDescriptor wizardDescriptor) {
        panels = null;
    }
    
    public Set<J2MEPlatform> instantiate() throws IOException {
        final J2MEPlatform[] platforms = (J2MEPlatform[]) wizardDescriptor.getProperty(DetectPanel.PROP_PLATFORMS);
        final HashSet<J2MEPlatform> set = new HashSet<J2MEPlatform>();
        for (int i = 0; i < platforms.length; i++) {
            final J2MEPlatform platform = platforms[i];
            J2MEPlatform.createPlatform(platform);
            set.add(platform);
        }
        return set;
    }
    
}
