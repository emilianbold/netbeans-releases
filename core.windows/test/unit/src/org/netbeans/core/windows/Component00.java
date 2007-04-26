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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows;

import org.openide.ErrorManager;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * Test component for PersistenceHandlerTest.
 *
 * @author  Marek Slama
 *
 */
public class Component00 extends TopComponent {

    static final long serialVersionUID = 6021472310161712674L;

    private static Component00 component = null;
    
    private static final String TC_ID = "component00";
    
    /** 
     * Used to detect if TC instance was created either using deserialization
     * or by getDefault.
     */
    private static boolean deserialized = false;
    
    private Component00 () {
    }

    protected String preferredID () {
        return TC_ID;
    }
    
    /* Singleton accessor. As Component00 is persistent singleton this
     * accessor makes sure that Component00 is deserialized by window system.
     * Uses known unique TopComponent ID "component00" to get Component00 instance
     * from window system. "component00" is name of settings file defined in module layer.
     */
    public static synchronized Component00 findDefault() {
        if (component == null) {
            //If settings file is correctly defined call of WindowManager.findTopComponent() will
            //call TestComponent00.getDefault() and it will set static field component.
            TopComponent tc = WindowManager.getDefault().findTopComponent(TC_ID);
            if (tc != null) {
                if (!(tc instanceof Component00)) {
                    //This should not happen. Possible only if some other module
                    //defines different settings file with the same name but different class.
                    //Incorrect settings file?
                    IllegalStateException exc = new IllegalStateException
                    ("Incorrect settings file. Unexpected class returned." // NOI18N
                    + " Expected:" + Component00.class.getName() // NOI18N
                    + " Returned:" + tc.getClass().getName()); // NOI18N
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                    //Fallback to accessor reserved for window system.
                    Component00.getDefault();
                }
            } else {
                //This should not happen when settings file is correctly defined in module layer.
                //TestComponent00 cannot be deserialized
                //Fallback to accessor reserved for window system.
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "Cannot deserialize TopComponent for tcID:'" + TC_ID + "'"); // NOI18N
                Component00.getDefault();
            }
        }
        return component;
    }
    
    /* Singleton accessor reserved for window system ONLY. Used by window system to create
     * TestComponent00 instance from settings file when method is given. Use <code>findDefault</code>
     * to get correctly deserialized instance of TestComponent00. */
    public static synchronized Component00 getDefault() {
        if (component == null) {
            component = new Component00();
        }
        deserialized = true;
        return component;
    }
    
    /** Overriden to explicitely set persistence type of TestComponent00
     * to PERSISTENCE_ALWAYS */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    /** Resolve to singleton instance */
    public Object readResolve() throws java.io.ObjectStreamException {
        return Component00.getDefault();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        deserialized = true;
    }

    static void clearRef () {
        component = null;
    }
    
    public static boolean wasDeserialized () {
        return deserialized;
    }
    
}
