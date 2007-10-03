/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.api.debugger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract definition of breakpoint.
 *
 * @author   Jan Jancura
 */
public abstract class Breakpoint {


    /** Property name for enabled status of the breakpoint. */
    public static final String          PROP_ENABLED = "enabled"; // NOI18N
    /** Property name for disposed state of the breakpoint. */
    public static final String          PROP_DISPOSED = "disposed"; // NOI18N
    /** Property name for name of group of the breakpoint. */
    public static final String          PROP_GROUP_NAME = "groupName"; // NOI18N
    /** Property name for breakpoint validity */
    public static final String          PROP_VALIDITY = "validity"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_HIT_COUNT_FILTER = "hitCountFilter"; // NOI18N
    
    /** Validity values */
    public static enum                  VALIDITY { UNKNOWN, VALID, INVALID }
    
    /** The style of filtering of hit counts.
     * The breakpoint is reported when the actual hit count is "equal to",
     * "greater than" or "multiple of" the number specified by the hit count filter. */
    public static enum                  HIT_COUNT_FILTERING_STYLE { EQUAL, GREATER, MULTIPLE }
    
    /** Support for property listeners. */
    private PropertyChangeSupport       pcs;
    private String                      groupName = "";
    private VALIDITY                    validity = VALIDITY.UNKNOWN;
    private String                      validityMessage;
    private int                         hitCountFilter;
    private HIT_COUNT_FILTERING_STYLE   hitCountFilteringStyle;
    
    { pcs = new PropertyChangeSupport (this); }

    /**
     * Called when breakpoint is removed.
     */
    protected void dispose () {}

    /**
     * Test whether the breakpoint is enabled.
     *
     * @return <code>true</code> if so
     */
    public abstract boolean isEnabled ();
    
    /**
     * Disables the breakpoint.
     */
    public abstract void disable ();
    
    /**
     * Enables the breakpoint.
     */
    public abstract void enable ();
    
    /**
     * Get the validity of this breakpoint.
     * @return The breakpoint validity.
     */
    public final synchronized VALIDITY getValidity() {
        return validity;
    }
    
    /**
     * Get the message describing the current validity. For invalid breakpoints
     * this should describe the reason why it is invalid.<p>
     * Intended for use by ui implementation code, NodeModel.getShortDescription(), for example.
     * @return The validity message.
     */
    public final synchronized String getValidityMessage() {
        return validityMessage;
    }
    
    /**
     * Set the validity of this breakpoint.
     * @param validity The new breakpoint validity.
     * @param reason The message describing why is this validity being set, or <code>null</code>.
     */
    protected final void setValidity(VALIDITY validity, String reason) {
        VALIDITY old;
        synchronized (this) {
            this.validityMessage = reason;
            if (this.validity == validity) return ;
            old = this.validity;
            this.validity = validity;
        }
        firePropertyChange(PROP_VALIDITY, old, validity);
    }
    
    /**
     * Get the hit count filter.
     * @return a positive hit count filter, or <code>zero</code> when no hit count filter is set.
     */
    public final synchronized int getHitCountFilter() {
        return hitCountFilter;
    }
    
    /**
     * Get the style of hit count filtering.
     * @return the style of hit count filtering, or <cpde>null</code> when no count filter is set.
     */
    public final synchronized HIT_COUNT_FILTERING_STYLE getHitCountFilteringStyle() {
        return hitCountFilteringStyle;
    }
    
    /**
     * Set the hit count filter and the style of filtering.
     * @param hitCountFilter a positive hit count filter, or <code>zero</code> to unset the filter.
     * @param hitCountFilteringStyle the style of hit count filtering.
     *        Can be <code>null</code> only when <code>hitCountFilter == 0</code>.
     */
    public final void setHitCountFilter(int hitCountFilter, HIT_COUNT_FILTERING_STYLE hitCountFilteringStyle) {
        Object[] old;
        Object[] newProp;
        synchronized (this) {
            if (hitCountFilter == this.hitCountFilter && hitCountFilteringStyle == this.hitCountFilteringStyle) {
                return ;
            }
            if (hitCountFilteringStyle == null && hitCountFilter > 0) {
                throw new NullPointerException("hitCountFilteringStyle must not be null.");
            }
            if (hitCountFilter == 0) {
                hitCountFilteringStyle = null;
            }
            if (this.hitCountFilter == 0) {
                old = null;
            } else {
                old = new Object[] { this.hitCountFilter, this.hitCountFilteringStyle };
            }
            if (hitCountFilter == 0) {
                newProp = null;
            } else {
                newProp = new Object[] { hitCountFilter, hitCountFilteringStyle };
            }
            this.hitCountFilter = hitCountFilter;
            this.hitCountFilteringStyle = hitCountFilteringStyle;
        }
        firePropertyChange(PROP_HIT_COUNT_FILTER, old, newProp);
    }
    
    public String getGroupName () {
        return groupName;
    }
    
    public void setGroupName (String newGroupName) {
        if (groupName.equals (newGroupName)) return;
        String old = groupName;
        groupName = newGroupName.intern();
        firePropertyChange (PROP_GROUP_NAME, old, newGroupName);
    }
    
    /** 
     * Add a listener to property changes.
     *
     * @param listener the listener to add
     */
    public synchronized void addPropertyChangeListener (
        PropertyChangeListener listener
    ) {
        pcs.addPropertyChangeListener (listener);
    }

    /** 
     * Remove a listener to property changes.
     *
     * @param listener the listener to remove
     */
    public synchronized void removePropertyChangeListener (
        PropertyChangeListener listener
    ) {
        pcs.removePropertyChangeListener (listener);
    }

    /**
     * Adds a property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l the listener to add
     */
    public void addPropertyChangeListener (
        String propertyName, PropertyChangeListener l
    ) {
        pcs.addPropertyChangeListener (propertyName, l);
    }

    /**
     * Removes a property change listener.
     *
     * @param propertyName a name of property to stop listening on
     * @param l the listener to remove
     */
    public void removePropertyChangeListener (
        String propertyName, PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (propertyName, l);
    }

    /**
     * Fire property change.
     *
     * @param name name of property
     * @param o old value of property
     * @param n new value of property
     */
    protected void firePropertyChange (String name, Object o, Object n) {
        pcs.firePropertyChange (name, o, n);
    }
    
    /**
     * Called when breakpoint is removed.
     */
    void disposeOut () {
        dispose ();
        firePropertyChange (PROP_DISPOSED, Boolean.FALSE, Boolean.TRUE);
    }
}
