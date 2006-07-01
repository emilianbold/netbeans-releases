/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * UIBootstrapValue.java
 *
 * Created on March 29, 2004, 4:17 PM
 */

package org.netbeans.swing.plaf.util;

import javax.swing.*;

/**
 * Value that can be placed into UIDefaults, which will put additional items into
 * UIDefaults when its value is requested.
 * <p>
 * The idea is to avoid putting a lot of things that may not be used into
 * UIDefaults on startup.  So, for example, the first time a tab control's UI
 * is requested, this value will return the string from which the UI can be
 * fetched - but first it will put the assorted keys and values that that UI
 * will need into UIDefaults.
 * <p>
 * Since multiple UIs may require the same things in UIDefaults, there is the
 * method createShared(), which will create another instance (really an inner
 * class instance) that shares the code and key/value pairs.  Whichever is
 * asked for first will initialize the keys and values required.  So the usage
 * pattern is something like:
 * <pre>
 * Object someKeysAndValues = new Object[] {"fooColor", Color.BLUE, "barColor", Color.RED};
 * UIBootstrapValue bv = new UIBootstrapValue ("com.foo.FnordUIClass", someKeysAndValues);
 * Object next = bv.createShared ("com.foo.ThiptUIClass");
 * UIManager.put ("FnordUI", bv);
 * UIManager.put ("ThiptUI", next);
 * </pre>
 *
 * @author  Tim Boudreau
 */
public class UIBootstrapValue implements UIDefaults.LazyValue {
    private boolean installed = false;
    private final String uiClassName;
    protected Object[] defaults;
    /** Creates a new instance of UIBootstrapValue */
    public UIBootstrapValue(String uiClassName, Object[] defaults) {
        this.defaults = defaults;
        this.uiClassName = uiClassName;
    }
    
    /** Create the value that UIDefaults will return.  If the keys and values
     * the UI class we're representing requires have not yet been installed,
     * this will install them.
     */
    public Object createValue(UIDefaults uidefaults) {
        if (!installed) {
            installKeysAndValues(uidefaults);
        }
        return uiClassName;
    }
    
    /** Install the defaults the UI we're representing will need to function */
    private void installKeysAndValues(UIDefaults ui) {
        ui.putDefaults (getKeysAndValues());
        installed = true;
    }

    public Object[] getKeysAndValues() {
        return defaults;
    }

    public void uninstall() {
        if (defaults == null) {
            return;
        }
        for (int i=0; i < defaults.length; i+=2) {
            UIManager.put (defaults[i], null);
        }
        //null defaults so a Meta instance won't cause us to do work twice
        defaults = null;
    }

    public String toString() {
        return getClass() + " for " + uiClassName; //NOI18N
    }

    /** Create another entry value to put in UIDefaults, which will also
     * trigger installing the keys and values, to ensure that they are only
     * added once, by whichever entry is asked for the value first. */
    public UIDefaults.LazyValue createShared (String uiClassName) {
        return new Meta (uiClassName);
    }
    
    private class Meta implements UIDefaults.LazyValue {
        private String name;
        public Meta (String uiClassName) {
            this.name = uiClassName;
        }
        
        public Object createValue(javax.swing.UIDefaults uidefaults) {
            if (!installed) {
                installKeysAndValues(uidefaults);
            }
            return name;
        }

        public String toString() {
            return "Meta-" + super.toString() + " for " + uiClassName; //NOI18N
        }
    }

    public abstract static class Lazy extends UIBootstrapValue implements UIDefaults.LazyValue {
        public Lazy (String uiClassName) {
            super (uiClassName, null);
        }

        public Object[] getKeysAndValues() {
            if (defaults == null) {
                defaults = createKeysAndValues();
            }
            return defaults;
        }

        public abstract Object[] createKeysAndValues();

    }
}
