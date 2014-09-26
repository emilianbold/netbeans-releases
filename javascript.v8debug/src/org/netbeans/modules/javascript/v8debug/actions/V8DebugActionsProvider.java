/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.v8debug.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import static org.netbeans.api.debugger.ActionsManager.*;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.V8DebuggerSessionProvider;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;

/**
 *
 * @author Martin Entlicher
 */
@ActionsProvider.Registration(path=V8DebuggerSessionProvider.SESSION_NAME)
public class V8DebugActionsProvider extends ActionsProviderSupport implements V8Debugger.Listener {
    
    private static final Logger LOG = Logger.getLogger(V8DebugActionsProvider.class.getName());

    private static final Set<Object> ACTIONS =
            Collections.unmodifiableSet(
                new HashSet<>(
                    Arrays.asList(new Object[] {
                        ACTION_START,
                        ACTION_KILL,
                        ACTION_CONTINUE,
                        ACTION_PAUSE,
                        ACTION_STEP_INTO,
                        ACTION_STEP_OVER,
                        ACTION_STEP_OUT,
                        ACTION_RUN_TO_CURSOR,
                        ACTION_EVALUATE,
                    })));
    
    private final V8Debugger debugger;
    
    public V8DebugActionsProvider(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, V8Debugger.class);
        debugger.addListener(this);
        setEnabled(ACTION_START, true);
        setEnabled(ACTION_KILL, true);
    }
    
    @Override
    public void doAction(Object action) {
        LOG.fine("doAction("+action+")");
        if (action == ACTION_START) {
            debugger.start();
        } else if (action == ACTION_KILL) {
            debugger.finish();
        }
    }

    @Override
    public Set getActions() {
        return ACTIONS;
    }

    @Override
    public void notifySuspended(boolean suspended) {
        
    }

    @Override
    public void notifyFinished() {
        
    }
    
}
