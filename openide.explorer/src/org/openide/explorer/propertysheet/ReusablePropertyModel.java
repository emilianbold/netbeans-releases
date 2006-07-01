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
 * ReusablePropertyModel.java
 *
 * Created on February 6, 2003, 5:12 PM
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;

import java.beans.PropertyEditor;

import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;


/** A reconfigurable property model for use by the rendering
 *  infrastructure, to avoid allocating memory while painting.
 *  Contains two static fields, PROPERTY and NODE which
 *  set the node and property this model acts as an interface to.<P>
 *  This class is <i>not thread safe</i>.  It assumes that it will
 *  only be called from the AWT thread, since it is used in painting
 *  infrastructure.  If property misrendering occurs, run NetBeans
 *  with the argument <code>-J-Dnetbeans.reusable.strictthreads=true</code>
 *  and exceptions will be thrown if any method is called from off the
 *  AWT thread.
 *
 * @author  Tim Boudreau
 */
class ReusablePropertyModel implements ExPropertyModel {
    static final boolean DEBUG = Boolean.getBoolean("netbeans.reusable.strictthreads");
    private transient Property PROPERTY = null;
    private final ReusablePropertyEnv env;

    /** Creates a new instance of ReusablePropertyModel */
    public ReusablePropertyModel(ReusablePropertyEnv env) {
        this.env = env;
        env.setReusablePropertyModel(this);
    }

    void clear() {
        PROPERTY = null;
    }

    /** Does nothing - if a property changes, the sheet will get notification
     *  and the model will be reconfigured with the new value and re-rendered */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    }

    /** Does nothing - if a property changes, the sheet will get notification
     *  and the model will be reconfigured with the new value and re-rendered */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    }

    public PropertyEditor getPropertyEditor() {
        Node.Property p = getProperty();

        // #52179: PropUtils.isExternallyEdited(p) - don't affect just 
        // externally edited properties or their current changes will be lost 
        // due to the firing PropertyChangeEvents to theirs UI counterpart
        return PropUtils.getPropertyEditor(p, !PropUtils.isExternallyEdited(p));
    }

    public Class getPropertyEditorClass() {
        if (DEBUG) {
            checkThread();
        }

        return getPropertyEditor().getClass();
    }

    public Class getPropertyType() {
        if (DEBUG) {
            checkThread();
        }

        return getProperty().getValueType();
    }

    public Object getValue() throws java.lang.reflect.InvocationTargetException {
        if (DEBUG) {
            checkThread();
        }

        try {
            return getProperty().getValue();
        } catch (IllegalAccessException iae) {
            Exceptions.printStackTrace(iae);
        }

        return null;
    }

    public void setValue(Object v) throws java.lang.reflect.InvocationTargetException {
        if (DEBUG) {
            checkThread();
        }

        try {
            getProperty().setValue(v);
        } catch (IllegalAccessException iae) {
            Exceptions.printStackTrace(iae);
        }
    }

    public Object[] getBeans() {
        if (DEBUG) {
            checkThread();
        }

        if (env.getNode() instanceof ProxyNode) {
            return ((ProxyNode) env.getNode()).getOriginalNodes();
        } else {
            return new Object[] { env.getNode() };
        }
    }

    public java.beans.FeatureDescriptor getFeatureDescriptor() {
        if (DEBUG) {
            checkThread();
        }

        return getProperty();
    }

    /** Ensure we're really running on the AWT thread, otherwise bad things can
     *  happen.  */
    static void checkThread() {
        if (SwingUtilities.isEventDispatchThread() == false) {
            throw new IllegalStateException("Reusable property model accessed from off the AWT thread.");
        }
    }

    public Node.Property getProperty() {
        return PROPERTY;
    }

    public void setProperty(Node.Property PROPERTY) {
        this.PROPERTY = PROPERTY;
    }
}
