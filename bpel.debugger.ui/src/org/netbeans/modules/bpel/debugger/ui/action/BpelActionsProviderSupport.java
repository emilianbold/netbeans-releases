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

package org.netbeans.modules.bpel.debugger.ui.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;

import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.Position;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;



/**
 * Wrapper around ActionsProviderSupport to provide for convenience methods
 * to subclasses.
 *
 * @author Josh Sandusky
 */
public abstract class BpelActionsProviderSupport extends ActionsProviderSupport {
    
    protected ContextProvider mLookupProvider;
    private BpelDebugger mDebugger;
    private Object mAction;
    
    
    public BpelActionsProviderSupport(ContextProvider lookupProvider, Object action) {
        mLookupProvider = lookupProvider;
        mAction = action;
        mDebugger = 
            (BpelDebugger) mLookupProvider.lookupFirst(null, BpelDebugger.class);
        setEnabled(true);
    }
    
    public void setEnabled(boolean isEnabled) {
        super.setEnabled(mAction, isEnabled);
    }
    
    public Set getActions() {
        return Collections.singleton(mAction);
    }
    
    public BpelDebugger getDebugger() {
        return mDebugger;
    }
    
    protected void positionChanged(Position oldPosition, Position newPosition) {
        // by default, do nothing
    }
    
    
    /**
     * Listens on the active break position. If it has changed, the associated
     * action is enabled or disabled.
     */
    static class PositionListener implements PropertyChangeListener {

        private BpelActionsProviderSupport mProvider;
        
        
        public PositionListener(BpelActionsProviderSupport provider) {
            mProvider = provider;
        }
        
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName() == BpelDebugger.PROP_CURRENT_POSITION) {
                mProvider.positionChanged(
                        (Position) e.getOldValue(),
                        (Position) e.getNewValue());
            }
        }
    }
}
