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

import java.util.Collections;
import java.util.Set;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.01
 */
abstract class Action extends ActionsProviderSupport
    implements PropertyChangeListener
{
    Action(ContextProvider provider, Object action) {
        myAction = action;
        myDebugger = (BpelDebugger) provider.lookupFirst (null, BpelDebugger.class);
        myDebugger.addPropertyChangeListener(BpelDebugger.PROP_STATE, this);
    }
    
    public Set getActions() {
        return Collections.singleton(myAction);
    }

    public void propertyChange(PropertyChangeEvent event) {
        checkEnabled();
    }
    
    public boolean isEnabled(Object action) {
        checkEnabled();
        return super.isEnabled(action);
    }
    
    protected BpelDebugger getDebugger() {
        return myDebugger;
    }

    private void checkEnabled() {
        setEnabled(myAction, true);
    }

    private Object myAction;
    private BpelDebugger myDebugger;
}
