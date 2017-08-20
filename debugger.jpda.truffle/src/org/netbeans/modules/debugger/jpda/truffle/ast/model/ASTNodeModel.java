/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.debugger.jpda.truffle.ast.model;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.ast.TruffleNode;
import org.netbeans.modules.debugger.jpda.truffle.ast.view.ASTView;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

@DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/"+ASTView.AST_VIEW_NAME,
                             types={ NodeModel.class })
public class ASTNodeModel implements NodeModel {

    private final JPDADebugger debugger;

    public ASTNodeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
    }

    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof TruffleNode) {
            TruffleNode ast = (TruffleNode) node;
            String label = ast.getClassSimpleName();
            if (ast.getChildren().length == 0) {
                if (ast.isCurrent()) {
                    label = "<html><b>" + label + "</b></html>";
                }
            }
            return label;
        } else {
            throw new UnknownTypeException(node);
        }
    }

    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        return "org/netbeans/modules/debugger/resources/threadsView/RunningThread";
    }

    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof TruffleNode) {
            TruffleNode ast = (TruffleNode) node;
            return ast.getClassName() + " (" + ast.getDescription() +")";
        } else {
            throw new UnknownTypeException(node);
        }
    }

    @Override
    public void addModelListener(ModelListener l) {
        
    }

    @Override
    public void removeModelListener(ModelListener l) {
        
    }
    
}
