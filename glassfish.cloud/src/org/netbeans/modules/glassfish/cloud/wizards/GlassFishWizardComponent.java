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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author kratz
 */
public abstract class GlassFishWizardComponent
        extends javax.swing.JPanel {

    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Validation result.
     * <p/>
     * Validation result contains information if validated value is valid
     * or not and also error message related to failed validation.
     */
    class ValidationResult {

        /** Validation result. */
        private final boolean valid;

        /** Error message is set to non <code>null</code> value when validation
         *  has failed. */
        private final String errorMessage;
        
        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Constructs an instance of validation result object with all values
         * initialized as provided arguments.
         * <p/>
         * @param valid        Validation result.
         * @param errorMessage Error message is set to non <code>null</code>
         *                     value when validation has failed.
         */
        ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        ////////////////////////////////////////////////////////////////////////
        // Getters                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Get validation result.
         * <p/>
         * @return Validation result.
         */
        boolean isValid() {
            return valid;
        }

        /**
         * Get error message related to validation failure.
         * <p/>
         * @return Error message related to validation failure
         *         or <code>null</code> when validation result
         *         is <code>true</code>.
         */
        String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Event listener to validate component field on the fly.
     */
    abstract class ComponentFieldListener implements DocumentListener {
        
        ////////////////////////////////////////////////////////////////////////
        // Abstract methods                                                   //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Process received notification from all notification types.
         */
        abstract void processEvent();

        ////////////////////////////////////////////////////////////////////////
        // Implemented Interface Methods                                      //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Gives notification that there was an insert into component field.
         * <p/>
         * @param event Change event object.
         */
        @Override
        public void insertUpdate(DocumentEvent e) {
            processEvent();
        }

        /**
         * Gives notification that a portion of component field has been removed.
         * <p/>
         * @param event Change event object.
         */
        @Override
        public void removeUpdate(DocumentEvent e) {
            processEvent();
        }

        /**
         * Gives notification that an attribute or set of attributes changed.
         * <p/>
         * @param event Change event object.
         */
        @Override
        public void changedUpdate(DocumentEvent e) {
            processEvent();
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Listener object to listen for change events. */
    private ChangeListener changeListener;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of common wizard component form.
     */
    public GlassFishWizardComponent() {
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Set listener object to listen for change events.
     * <p/>
     * @param changeListener Listener object to listen for change events.
     */
    public void setChangeListener(ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    /**
     * Update change events listener to notify him about state change.
     * <p/>
     * This method should be called in child class where field change event
     * listeners are implemented.
     * <p/>
     * @param result Field validation result.
     */
    void update(ValidationResult result) {
        if (changeListener != null) {
            changeListener.stateChanged(new ChangeEvent((result)));
        }
    }

}
