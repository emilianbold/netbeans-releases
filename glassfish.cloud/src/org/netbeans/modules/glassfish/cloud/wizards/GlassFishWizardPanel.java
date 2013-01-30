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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.Utilities;

/**
 * GlassFish Cloud Wizard Panel Common Functionality.
 * <p/>
 * Implements parts of
 * <code>WizardDescriptor.AsynchronousValidatingPanel&lt;WizardDescriptor&gt;</code>
 * and <code>ChangeListener</code> interfaces.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class GlassFishWizardPanel
        implements
        WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor>,
        ChangeListener {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Wizard descriptor object. */
    WizardDescriptor wizardDescriptor;

    /** Support for change events listeners. */
    private ChangeSupport listeners;

    /** Asynchronous error message. */
    String asynchError;

    /** Panel component containing CPAS attributes.
     *  Child class must initialize this attribute with panel component
     *  in <code>getComponent</code> method. */
    GlassFishWizardComponent component;

    /** Wizard step names. */
    final String[] names;
    
    /** Index of wizard step name to use as panel header. */
    final int nameIndex;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of CPAS panel.
     * <p/>
     * @param names     Wizard steps names to be added into steps tab.
     * @param nameIndex Index of wizard step name to be displayed
     *                  as component name.
     */
    public GlassFishWizardPanel(String[] names, int nameIndex) {
        listeners = new ChangeSupport(this);
        this.nameIndex = nameIndex;
        this.names = names;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Interface Methods                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Called synchronously from UI thread when Next or Finish buttons clicked.
     * <p/>
     * It allows to lock user input to assure official data for background
     * validation.
     * Pointer to <code>GlassFishWizardComponent</code> instance must be
     * initialized in child class in <code>getComponent</code> method.
     */
    @Override
    public void prepareValidation() {
        // Lock form before validation.
        getComponent().setCursor(Utilities.createProgressCursor(getComponent()));
        component.disableModifications();
    }

    /**
     * Test whether the panel is finished and it is safe to proceed to the next
     * one.
     * <p/>
     * If the panel is valid, the "Next" (or "Finish") button will be
     * enabled.
     * <p/>
     * @retrn <code>true</code> if the user has entered satisfactory information
     *        or <code>false</code> otherwise.
     */
    @Override
    public boolean isValid() {
        if (component == null || wizardDescriptor == null || asynchError != null) {
            return false;
        }
        return component.valid();
    }

    /**
     * Provides the wizard panel with the current data.
     * <p/>
     * Either the default data or already-modified settings, if the user used
     * the previous and/or next buttons. This method can be called multiple
     * times on one instance of wizard panel.
     * <p/>
     * The settings object is originally supplied to
     * {@link
     * WizardDescriptor#WizardDescriptor(WizardDescriptor.Iterator,Object)}.
     * <P/>
     * @param settings the object representing wizard panel state.
     */
    @Override
    public void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
        asynchError = null;
    }

    /**
     * Add a listener to changes of the panel validity.
     * <p/>
     * @param listener Listener to add.
     * @see #isValid
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

    /**
     * Invoked when the target of the listener has changed its state.
     * <p/>
     * @param e a ChangeEvent object
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        listeners.fireChange();
        asynchError = null;        
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize common properties of newly created panel component.
     * <p/>
     * This is helper method to implement <code>getComponent</code> method
     * in child classes.
     * <p/>
     * Component must be initialized before calling this method and also both
     * <code>names</code> and <code>nameIndex</code> attributes must be
     * initialized properly.
     */
    void initComponent() {
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, names);
        component.putClientProperty(
                WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,
                Integer.valueOf(nameIndex));
        component.setName(names[nameIndex]);
    }

    /**
     * Set error message to be displayed at the bottom of the wizard.
     * <p/>
     * @param message Error message to be displayed.
     */
    public void setErrorMessage(String message) {
        wizardDescriptor.putProperty(
                WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }
    
    /**
     * Set warning message to be displayed at the bottom of the wizard.
     * <p/>
     * @param message Warning message to be displayed.
     */
    public void setWarningMessage(String message) {
        wizardDescriptor.putProperty(
                WizardDescriptor.PROP_WARNING_MESSAGE, message);
    }

}
