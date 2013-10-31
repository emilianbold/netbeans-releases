/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.js.frames.models;

import java.lang.reflect.Field;
import org.netbeans.modules.debugger.jpda.js.frames.JSStackFrame;
import javax.swing.Action;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.ActionPerformer;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/JS/DebuggingView",
                             types=NodeActionsProviderFilter.class)
public class DebuggingJSActionsProvider implements NodeActionsProviderFilter {

    @Override
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof JSStackFrame) {
            node = ((JSStackFrame) node).getJavaFrame();
        }
        original.performDefaultAction(node);
    }

    @Override
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof JSStackFrame) {
            node = ((JSStackFrame) node).getJavaFrame();
            Action[] actions = original.getActions(node);
            for (int i = 0; i < actions.length; i++) {
                actions[i] = translateModelAction(actions[i]);
            }
            return actions;
        } else {
            return original.getActions(node);
        }
    }
    
    private static Action translateModelAction(Action action) {
        ActionPerformer ap = getActionPerformer(action);
        if (ap != null) {
            String name = (String) action.getValue(Action.NAME);
            action = Models.createAction(name, new ActionPerformerDelegate(ap), Models.MULTISELECTION_TYPE_EXACTLY_ONE);
        }
        return action;
    }

    private static ActionPerformer getActionPerformer(Action action) {
        // Not a nice way of retrieval of the original action performer:
        try {
            Class<?> asClass = Class.forName(Models.class.getName() + "$ActionSupport");
            if (!asClass.isInstance(action)) {
                return null;
            }
            Field performerField = asClass.getDeclaredField("performer");
            performerField.setAccessible(true);
            Object performer = performerField.get(action);
            return (ActionPerformer) performer;
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    private static class ActionPerformerDelegate implements ActionPerformer {
        
        private final ActionPerformer apDelegate;
        
        ActionPerformerDelegate(ActionPerformer apDelegate) {
            this.apDelegate = apDelegate;
        }

        @Override
        public boolean isEnabled(Object node) {
            if (node instanceof JSStackFrame) {
                node = ((JSStackFrame) node).getJavaFrame();
            }
            return apDelegate.isEnabled(node);
        }

        @Override
        public void perform(Object[] nodes) {
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i] instanceof JSStackFrame) {
                    nodes[i] = ((JSStackFrame) nodes[i]).getJavaFrame();
                }
            }
            apDelegate.perform(nodes);
        }
        
    }
    
}
