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

import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.JPanel;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * TcgComponentNodePropertyCustomizer.java
 * 
 * Created on November 7, 2006, 9:52 AM
 * 
 * @author Bing Lu
 */
public abstract class TcgComponentNodePropertyCustomizer extends JPanel{
    protected TcgComponentNodeProperty mProperty;
    protected PropertyEnv mEnv;
    protected Validator mValidator;
    protected TcgComponentNodePropertyCustomizerState mCustomizerState;
    
    /**
     * Creates a new instance of TcgComponentNodePropertyCustomizer
     */
    public TcgComponentNodePropertyCustomizer(TcgComponentNodeProperty prop, PropertyEnv env) {
        mProperty = prop;
        mEnv = env;
        mValidator = new Validator();
        mEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        mEnv.addVetoableChangeListener(mValidator);
        mEnv.addPropertyChangeListener(mValidator);
        initialize();
    }
    
    /**
     * Creates a new instance of TcgComponentNodePropertyCustomizer
     */
    public TcgComponentNodePropertyCustomizer(TcgComponentNodeProperty prop, TcgComponentNodePropertyCustomizerState customizerState) {
        mProperty = prop;
        mCustomizerState = customizerState;
        mValidator = new Validator();
        mCustomizerState.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        mCustomizerState.addVetoableChangeListener(mValidator);
        mCustomizerState.addPropertyChangeListener(mValidator);
        initialize();
    }

    protected abstract void initialize();
    
    public void removeNotify() {
        if (mEnv != null) {
            if (mEnv.getState() == PropertyEnv.STATE_VALID) {
                setValue();
            }
            mEnv.removeVetoableChangeListener(mValidator);
            mEnv.removePropertyChangeListener(mValidator);
            super.removeNotify();
            return;
        } 
        if (mCustomizerState != null) {
            if (mCustomizerState.getState() == PropertyEnv.STATE_VALID) {
                setValue();
            }
            mCustomizerState.removeVetoableChangeListener(mValidator);
            mCustomizerState.removePropertyChangeListener(mValidator);
            super.removeNotify();
            return;
        }
        super.removeNotify();
    }
        
    public abstract void validateContent(PropertyChangeEvent evt) throws PropertyVetoException;
    public abstract void setValue();
    
    class Validator implements VetoableChangeListener, PropertyChangeListener {
        private boolean mVetoStart = false;
        private boolean mVetoEnd = true;
        
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (mVetoEnd && PropertyEnv.PROP_STATE.equals(evt.getPropertyName())) {
                try {
                    validateContent(evt);
                    mVetoStart = false;
                    mVetoEnd = true;
                } catch (PropertyVetoException e) {
                    mVetoStart = true;
                    mVetoEnd = false;
                }
            }
            // otherwise allow the switch to ok state
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (mVetoStart) {
                mVetoStart = false;
                if (mEnv != null) {
                    mEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
                }
                if (mCustomizerState != null) {
                    mCustomizerState.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
                }
            } else {
                mVetoEnd = true;
            }
        }
    }
}
