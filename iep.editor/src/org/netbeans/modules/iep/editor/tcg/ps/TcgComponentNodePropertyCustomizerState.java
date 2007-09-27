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
package org.netbeans.modules.iep.editor.tcg.ps;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import org.openide.explorer.propertysheet.PropertyEnv;



public class TcgComponentNodePropertyCustomizerState {
    /**
     * Current mState.
     */
    private Object mState = PropertyEnv.STATE_VALID;

    /**
     * The mSupport is lazy initialized in getSupport.
     */
    private VetoableChangeSupport mSupport;

    /**
     * mChange mSupport here
     */
    private PropertyChangeSupport mChange;

    /** Default constructor has package access -
     * we do not want the instances to be created outside
     * our package.
     */
    public TcgComponentNodePropertyCustomizerState() {
    }


    /**
     * A setter that should be used by the property editor
     * to mChange the mState of the environment.
     * Even the mState property is bound, changes made from the editor itself
     * are allowed without restrictions.
     */
    public void setState(Object newState) {
        if (getState().equals(newState)) {
            // no mChange, no fire vetoable and property mChange
            return;
        }

        try {
            getSupport().fireVetoableChange(PropertyEnv.PROP_STATE, getState(), newState);
            mState = newState;

            // always notify mState mChange
            getChange().firePropertyChange(PropertyEnv.PROP_STATE, null, newState);
        } catch (PropertyVetoException pve) {
            // proposal for change is vetoed
        }
    }

    /**
     * A getter for the current mState of the environment.
     * 
     * 
     * @return one of the constants STATE_VALID, STATE_INVALID,
     * STATE_NEEDS_VALIDATION.
     */
    public Object getState() {
        return mState;
    }

    /**
     * Vetoable mChange listener: listenning here you will be notified
     * when the mState of the environment is being changed (when the setState
     * method is being called). You can veto the mChange and provide
     * a displayable information in the thrown exception. Use
     * the ErrorManager annotaion feature for the your exception to modify
     * the message and severity.
     */
    public void addVetoableChangeListener(VetoableChangeListener l) {
        getSupport().addVetoableChangeListener(l);
    }

    /**
     * Property mChange listener: listenning here you will be notified
     * when the mState of the environment is has been changed.
     * 
     * 
     * 
     * @since 2.20
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        getChange().addPropertyChangeListener(l);
    }

    /**
     * Vetoable mChange listener removal.
     */
    public void removeVetoableChangeListener(VetoableChangeListener l) {
        getSupport().removeVetoableChangeListener(l);
    }

    /**
     * Removes Property mChange listener.
     * 
     * @since 2.20
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        getChange().removePropertyChangeListener(l);
    }


    /**
     * Lazy initialization of the VetoableChangeSupport.
     */
    private synchronized VetoableChangeSupport getSupport() {
        if (mSupport == null) {
            mSupport = new VetoableChangeSupport(this);
        }

        return mSupport;
    }

    /**
     * Lazy initialization of the PropertyChangeSupport.
     */
    private synchronized PropertyChangeSupport getChange() {
        if (mChange == null) {
            mChange = new PropertyChangeSupport(this);
        }

        return mChange;
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append("@"); //NOI18N
        sb.append(System.identityHashCode(this));
        sb.append("[state="); //NOI18N
        sb.append(
            (mState == PropertyEnv.STATE_NEEDS_VALIDATION) ? "STATE_NEEDS_VALIDATION"
                                              : ((mState == PropertyEnv.STATE_INVALID) ? "STATE_INVALID" : "STATE_VALID")
        ); //NOI18N
        sb.append("]"); //NOI18N
        return sb.toString();
    }
}
