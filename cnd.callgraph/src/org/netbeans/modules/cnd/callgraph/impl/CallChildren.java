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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.callgraph.impl;

import org.netbeans.modules.cnd.callgraph.api.*;
import java.util.Collections;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public class CallChildren extends Children.Keys<Call> {
    private Call call;
    private Function function;
    private CallGraphState model;
    private boolean isInited = false;
    private boolean isCalls;

    public CallChildren(Call call, CallGraphState model, boolean isCalls) {
        this.call = call;
        this.model = model;
        this.isCalls = isCalls;
    }

    public CallChildren(Function function, CallGraphState model, boolean isCalls) {
        this.function = function;
        this.model = model;
        this.isCalls = isCalls;
    }
 
    public void dispose(){
        if (isInited) {
            isInited = false;
            setKeys(new Call[0]);
        }
    }
    
    private synchronized void resetKeys(){
        List<Call> set;
        if (isCalls) {
            if (call != null) {
                set = model.getModel().getCallees(call.getCallee());
            } else {
                set = model.getModel().getCallees(function);
            }
        } else {
            if (call != null) {
                set = model.getModel().getCallers(call.getCaller());
            } else {
                set = model.getModel().getCallers(function);
            }
        }
        if (set != null && set.size() > 0) {
            Collections.<Call>sort(set);
            setKeys(set);
            return;
        }
        setKeys(new Call[0]);
    }
    
    protected Node[] createNodes(Call call) {
        if (call instanceof LoadingNode) {
            return new Node[]{(Node)call};
        }
        Node node = new CallNode(call, model, isCalls);
        return new Node[]{node};
    }
    
    @Override
    protected void addNotify() {
        isInited = true;
        setKeys(new Call[]{new LoadingNode()});
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                resetKeys();
            }
        });
        super.addNotify();
    }

    @Override
    protected void removeNotify() {
        super.removeNotify();
        dispose();
    }
}
