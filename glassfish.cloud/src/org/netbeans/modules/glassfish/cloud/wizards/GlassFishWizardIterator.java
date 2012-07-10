/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.cloud.wizards;

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;

/**
 * GlassFish Cloud Wizard Common Functionality.
 * <p>
 * Adds GlassFish Cloud item into Add &lt;something&gt; wizard.
 * <p/>
 * Implements parts of
 * <code>WizardDescriptor.AsynchronousInstantiatingIterator&lt;WizardDescriptor&gt;</code>
 * interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class GlassFishWizardIterator
        implements
        WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Wizard display name. */
    static final String PROPERTY_WIZARD_DISPLAY_NAME
            = "ServInstWizard_displayName";
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Wizard's descriptor. */
    WizardDescriptor wizard;

    /** Total panels count. */
    private final int panelsCount;

    /**
     * The display name of GlassFish cloud wizard panels retrieved from
     * message bundle.
     * <p/>
     * Child class should initialize individual array elements correctly.
     * Only array itself is initialized in constructor.
     */
    final String[] name;

    /**
     * GlassFish cloud wizard panels.
     * <p/>
     * Child class should initialize individual array elements correctly.
     * Only array itself is initialized in constructor.
     */
    final WizardDescriptor.Panel<WizardDescriptor>[] panel;

    /** Wizard panels internal index. */
    private int panelIndex = 0;

    /** Support for change events listeners. */
    private final ChangeSupport listeners;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings({"unchecked", "rawtypes"})
    public GlassFishWizardIterator(final int panelsCount) {
        this.panelsCount = panelsCount;
        this.panel = new WizardDescriptor.Panel[panelsCount];
        this.name = new String[panelsCount];
        this.listeners = new ChangeSupport(this);
        for (int i = 0; i < panelsCount; i++) {
            this.panel[i] = null;
            this.name[i] = null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Interface Methods                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Initializes this iterator, called from WizardDescriptor's constructor.
     * <p/>
     * Child method should override this method to correctly initialize content
     * of <code>panel</code> and <code>name</code> attributes which are being
     * cleaned by <code>uninitialize</code> method.
     * <p/>
     * @param wizard Wizard's descriptor.
     */
    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    /**
     * Cleans up this iterator, called when the wizard is being closed,
     * no matter what closing option invoked.
     * <p/>
     * @param wizard Wizard's descriptor.
     */
    @Override
    public void uninitialize(WizardDescriptor wizard) {
        for (int i = 0; i < panelsCount; i++) {
            this.panel[i] = null;
            this.name[i] = null;
        }
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panel[panelIndex];
    }

    @Override
    public String name() {
        return name[panelIndex];
    }

    /**
     * Test whether there is a next panel.
     * <p/>
     * @return <code>true</code> if so or <code>false</code> otherwise.
     */
    @Override
    public boolean hasNext() {
        return panelIndex < panelsCount - 1;
    }

    /**
     * Test whether there is a previous panel.
     * <p/>
     * @return <code>true</code> if so or <code>false</code> otherwise.
     */
    @Override
    public boolean hasPrevious() {
        return panelIndex > 0;
    }

    /**
     * Move to the next panel.
     * <p/>
     * Increment internal index pointing to current panel. Need not actually
     * change any GUI itself.
     * <p/>
     * @exception NoSuchElementException if the panel does not exist
     */
    @Override
    public void nextPanel() {
        panelIndex += 1;
    }

    /**
     * Move to the previous panel.
     * <p/>
     * Decrement internal index pointing to current panel. Need not actually
     * change any GUI itself.
     * <p/>
     * @exception NoSuchElementException if the panel does not exist
     */
    @Override
    public void previousPanel() {
        panelIndex -= 1;
    }

    /**
     * Invoked when the target of the listener has changed its state.
     * <p/>
     * @param e a ChangeEvent object
     */
    @Override
    public void addChangeListener(ChangeListener listener) {
        listeners.addChangeListener(listener);
    }

    /**
     * Remove a listener to changes of the panel validity.
     * <p/>
     * @param listener Listener to remove
     */
    @Override
    public void removeChangeListener(ChangeListener listener) {
        listeners.removeChangeListener(listener);
    }
    
}
