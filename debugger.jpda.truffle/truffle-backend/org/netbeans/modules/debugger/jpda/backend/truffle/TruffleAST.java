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
package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameInstanceVisitor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author martin
 */
public class TruffleAST {

    private final FrameInstance frameInstance;

    private TruffleAST(FrameInstance frameInstance) {
        this.frameInstance = frameInstance;
    }

    static TruffleAST get(int depth) {
        final AtomicInteger frameDepth = new AtomicInteger(depth);
        FrameInstance frameInstance = Truffle.getRuntime().iterateFrames(new FrameInstanceVisitor<FrameInstance>() {
            @Override
            public FrameInstance visitFrame(FrameInstance frameInstance) {
                CallTarget callTarget = frameInstance.getCallTarget();
                if (!(callTarget instanceof RootCallTarget)) {
                    return null;
                }
                RootCallTarget rct = (RootCallTarget) callTarget;
                SourceSection rootSourceSection = rct.getRootNode().getSourceSection();
                if (rootSourceSection != null) {
                    if (frameDepth.getAndDecrement() == 0) {
                        return frameInstance;
                    }
                }
                return null;
            }
        });
        return frameInstance == null ? null : new TruffleAST(frameInstance);
    }

    public Object[] getRawArguments() {
        return frameInstance.getFrame(FrameInstance.FrameAccess.MATERIALIZE).getArguments();
    }

    public Object[] getRawSlots() {
        Frame frame = frameInstance.getFrame(FrameInstance.FrameAccess.MATERIALIZE);
        List<? extends FrameSlot> slots = frame.getFrameDescriptor().getSlots();
        int n = slots.size();
        Object[] slotInfo = new Object[2*n];
        for (int i = 0; i < n; i++) {
            FrameSlot slot = slots.get(i);
            slotInfo[2*i] = slot.getIdentifier();
            slotInfo[2*i + 1] = frame.getValue(slot);
        }
        return slotInfo;
    }

    /**
     * Get the nodes hierarchy. Every node is described by:
     * <ul>
     *  <li>node class</li>
     *  <li>node description</li>
     *  <li>node source section - either an empty line, or following items:</li>
     *  <ul>
     *   <li>URI</li>
     *   <li>&lt;start line&gt;:&lt;start column&gt;-&lt;end line&gt;:&lt;end column&gt;</li>
     *  </ul>
     *  <li>number of children</li>
     *  <li>&lt;child nodes follow...&gt;</li>
     * </ul>
     * @return a newline-separated list of elements describing the nodes hierarchy.
     */
    public String getNodes() {
        StringBuilder nodes = new StringBuilder();
        RootCallTarget rct = (RootCallTarget) frameInstance.getCallTarget();
        RootNode rootNode = rct.getRootNode();
        fillNode(rootNode, nodes);
        return nodes.toString();
    }

    private static void fillNode(Node node, StringBuilder nodes) {
        nodes.append(node.getClass().getName());
        nodes.append('\n');
        nodes.append(node.getDescription());
        nodes.append('\n');
        SourceSection ss = node.getSourceSection();
        if (ss == null) {
            nodes.append('\n');
        } else {
            nodes.append(ss.getSource().getURI().toString());
            nodes.append('\n');
            nodes.append(Integer.toString(ss.getStartLine()));
            nodes.append(':');
            nodes.append(Integer.toString(ss.getStartColumn()));
            nodes.append('-');
            nodes.append(Integer.toString(ss.getEndLine()));
            nodes.append(':');
            nodes.append(Integer.toString(ss.getEndColumn()));
            nodes.append('\n');
            //nodes.add(ss.getCode());
        }
        List<Node> ch = NodeUtil.findNodeChildren(node);
        nodes.append(Integer.toString(ch.size()));
        nodes.append('\n');
        for (Node n : ch) {
            fillNode(n, nodes);
        }
    }
    
}
