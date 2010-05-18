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

package org.netbeans.modules.projectimport.j2seimport.ui;

import java.awt.Component;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Convenient class for implementing {@link org.openide.WizardDescriptor.InstantiatingIterator}.
 *
 * @author Radek Matous
 */
public abstract class BasicWizardIterator implements WizardDescriptor.InstantiatingIterator {
    protected BasicWizardIterator.BasicDataModel data;
    private int position = 0;
    private BasicWizardIterator.PrivateWizardPanel[] wizardPanels;
    
    /** Create a new wizard iterator. */
    protected BasicWizardIterator() {
    }
    
    /** @return all panels provided by this {@link org.openide.WizardDescriptor.InstantiatingIterator} */
    protected abstract BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz);
    
    protected abstract String getTitle();    
    
    /** Basic visual panel.*/
    public abstract static class Panel extends BasicVisualPanel {
        
        protected Panel(WizardDescriptor wiz) {
            super(wiz);
        }
        
        /**
         * Returned name is used by a wizard. e.g. on its left side in the
         * <em>step list</em>.
         * @return name of panel
         */
        protected abstract String getPanelName();
        
        /**
         * Gives a chance to store an instance of {@link
         * BasicWizardIterator.BasicDataModel}. It is called when a panel is
         * going to be <em>hidden</em> (e.g. when switching to next/previous
         * panel).
         */
        protected abstract void storeToDataModel();
        
        /**
         * Gives a chance to refresh a panel (usually by reading a state of an
         * instance of {@link BasicWizardIterator.BasicDataModel}. It is called
         * when a panel is going to be <em>displayed</em> (e.g. when switching
         * from next/previous panel).
         */
        protected abstract void readFromDataModel();
        
        protected abstract HelpCtx getHelp();
        
    }
    
    /** DataModel that is passed through individual panels.*/
    public static class BasicDataModel {   
    }
    
    public final void initialize(WizardDescriptor wiz) {
        // mkleint: copied from the NewJavaFileWizardIterator.. there must be something painfully wrong..
        wiz.setTitleFormat(new java.text.MessageFormat("{0}")); // NOI18N
        wiz.setTitle(getTitle()); // NOI18N        
        String[] beforeSteps = null;
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        position = 0;
        BasicWizardIterator.Panel[] panels = createPanels(wiz);
        String[] steps = BasicWizardIterator.createSteps(beforeSteps, panels);
        wizardPanels = new BasicWizardIterator.PrivateWizardPanel[panels.length];
        
        for (int i = 0; i < panels.length; i++) {
            wizardPanels[i] = new BasicWizardIterator.PrivateWizardPanel(panels[i], steps, i);
        }
    }
    
    // mkleint: copied from the NewJavaFileWizardIterator.. there must be something painfully wrong..
    private static String[] createSteps(String[] before, BasicWizardIterator.Panel[] panels) {
        assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getPanelName();
            }
        }
        return res;
    }
    
    public final void uninitialize(WizardDescriptor wiz) {
        wizardPanels = null;
        uninitialize();
    }
    
    public void uninitialize() {
        wizardPanels = null;
    }
    
    public String name() {
        return ((BasicWizardIterator.PrivateWizardPanel)
        current()).getPanel().getPanelName();
    }
    
    public boolean hasNext() {
        return position < (wizardPanels.length - 1);
    }
    
    public boolean hasPrevious() {
        return position > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        position++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        position--;
    }
    
    public WizardDescriptor.Panel current() {
        return wizardPanels[position];
    }
    
    /**
     * Convenience method for accessing Bundle resources from this package.
     */
    protected final String getMessage(String key) {
        return NbBundle.getMessage(getClass(), key);
    }
    
    public final void addChangeListener(ChangeListener  l) {}
    public final void removeChangeListener(ChangeListener l) {}
    

    
    private static final class PrivateWizardPanel extends BasicWizardPanel {
        
        private BasicWizardIterator.Panel panel;
        
        PrivateWizardPanel(BasicWizardIterator.Panel panel, String[] allSteps, int stepIndex) {
            super(panel.getSettings());
            panel.addPropertyChangeListener(this);
            panel.setName(panel.getPanelName()); // NOI18N
            this.panel = panel;
            panel.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(stepIndex)); // NOI18N
            // names of currently used steps
            panel.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, allSteps); // NOI18N
            panel.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
            panel.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
            panel.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
        }
        
        private BasicWizardIterator.Panel getPanel() {
            return panel;
        }
        
        public Component getComponent() {
            return getPanel();
        }
        
        public void storeSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            if (WizardDescriptor.NEXT_OPTION.equals(wiz.getValue()) ||
                    WizardDescriptor.FINISH_OPTION.equals(wiz.getValue())) {
                panel.storeToDataModel();
            }
        }
        
        public void readSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            
            if (WizardDescriptor.NEXT_OPTION.equals(wiz.getValue()) || wiz.getValue() == null) {
                panel.readFromDataModel();
            }
        }
        
        public HelpCtx getHelp() {
            return getPanel().getHelp();
        }
        
    }

    public void setData(BasicWizardIterator.BasicDataModel data) {
        this.data = data;
    }
        
    public BasicWizardIterator.BasicDataModel getData() {
        return data;
    }

    final public Set instantiate() throws IOException {
        return null;
    }    
}

