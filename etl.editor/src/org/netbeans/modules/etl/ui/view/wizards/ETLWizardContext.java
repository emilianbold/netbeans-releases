/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.etl.ui.view.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.openide.WizardDescriptor;

/**
 * Provides a context for reading and storing intermediate values,
 * properties, etc., among components in a wizard.
 * 
 */
public class ETLWizardContext extends HashMap {
    /** Default reserved key to reference wizard descriptor. */
    public static final String WIZARD_DESCRIPTOR = "wizDesc";

    /** List of current reserved keys for this context. */
    protected List reservedKeys;

    /** Creates a new instance of ETLWizardContext */
    public ETLWizardContext() {
    }

    /**
     * Creates a new instance of ETLWizardContext containing the given WizardDescriptor as
     * a context object.
     * 
     * @param desc WizardDescriptor to be added to the context.
     */
    public ETLWizardContext(WizardDescriptor desc) {
        setWizardDescriptor(desc);
    }

    /**
     * Sets the property associated with the given key. Supplying a null value for key, or
     * attempting to clear a property associated with a reserved key, results in an
     * IllegalArgumentException.
     * 
     * @param key key of property to be cleared
     */
    public void clearProperty(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Must supply non-null ref for key.");
        }

        if (isReservedKey(key)) {
            throw new IllegalArgumentException("Cannot use clear property using reserved key: " + key.trim()
                + "; use appropriate clear method instead.");
        }

        this.remove(key);
    }

    /**
     * Clears the current wizard descriptor instance, if any.
     */
    public void clearWizardDescriptor() {
        this.remove(WIZARD_DESCRIPTOR);
    }

    /**
     * Gets the property, if any, associated with the given key.
     * 
     * @param key key of property to get
     * @return associated property, or null if none exists
     */
    public Object getProperty(String key) {
        return this.get(key);
    }

    /**
     * Gets List of current reserved keys for this context.
     * 
     * @return List of reserved keys
     */
    public List getReservedKeys() {
        createReservedKeys();
        return Collections.unmodifiableList(reservedKeys);
    }

    /**
     * Indicates the wizard option last selected by the user, provided a wizard descriptor
     * has been set in this context. If no wizard descriptor is set, throws
     * java.lang.IllegalStateException.
     * 
     * @return Object representing selected wizard option.
     * @see org.openide.WizardDescriptor#PREVIOUS_OPTION
     * @see org.openide.WizardDescriptor#NEXT_OPTION
     * @see org.openide.WizardDescriptor#FINISH_OPTION
     * @see org.openide.WizardDescriptor#CANCEL_OPTION
     * @see org.openide.WizardDescriptor#CLOSED_OPTION
     */
    public Object getSelectedOption() {
        WizardDescriptor desc = getWizardDescriptor();
        return (desc != null) ? desc.getValue() : null;
    }

    /**
     * Gets wizard descriptor, if any, from this context.
     * 
     * @return WizardDescriptor instance, or null if not found.
     */
    public WizardDescriptor getWizardDescriptor() {
        Object o = this.get(WIZARD_DESCRIPTOR);
        return (o instanceof WizardDescriptor) ? (WizardDescriptor) o : null;
    }

    /**
     * Indicates whether the given string is a reserved key;
     * 
     * @param key String to be tested
     * @return true if key is reserved; false otherwise
     */
    public boolean isReservedKey(String key) {
        return getReservedKeys().contains(key);
    }

    /**
     * Sets the property associated with the given key. Null values for either argument
     * results in an IllegalArgumentException.
     * 
     * @param key key of property to be associated
     * @param value property to be associated
     */
    public void setProperty(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Must supply non-null ref for key.");
        }

        if (isReservedKey(key)) {
            throw new IllegalArgumentException("Cannot use set property using reserved key: " + key.trim() + "; use appropriate setter instead.");
        }

        this.put(key, value);
    }

    /**
     * Sets wizard descriptor in this context to the given instance.
     * 
     * @param desc WizardDescriptor instance to be set
     */
    public void setWizardDescriptor(WizardDescriptor desc) {
        if (desc == null) {
            throw new IllegalArgumentException("Must supply non-null ref for desc.");
        }

        this.put(WIZARD_DESCRIPTOR, desc);
    }

    /**
     * Creates list of reserved keys associated with this context instance.
     */
    protected void createReservedKeys() {
        if (reservedKeys == null) {
            reservedKeys = new ArrayList();
        } else {
            reservedKeys.clear();
        }

        reservedKeys.add(WIZARD_DESCRIPTOR);
    }
}

