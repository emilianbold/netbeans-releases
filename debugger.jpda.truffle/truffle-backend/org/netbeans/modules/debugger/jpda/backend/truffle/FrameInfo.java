/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.debug.DebugStackFrame;
import com.oracle.truffle.api.debug.DebugValue;
import com.oracle.truffle.api.source.SourceSection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class FrameInfo {
    final DebugStackFrame frame;  // the top frame instance
    //final FrameSlot[] slots;
    //final String[] slotNames;
    //final String[] slotTypes;
    final DebugStackFrame[] stackTrace;
    final String topFrame;
    final Object[] topVariables;
    //final Object thisObject;

    /*
    public FrameInfo(MaterializedFrame frame, Node astNode,
                     List<FrameInstance> stack) {
        //System.err.println("new FrameInfo("+frame+" ("+frame.getFrameDescriptor().toString()+"), "+stack+")");
        Object[] arguments = frame.getArguments();
        FrameDescriptor frameDescriptor = frame.getFrameDescriptor();
        Set<Object> identifiers = frameDescriptor.getIdentifiers();
        List<? extends FrameSlot> slotsList = frameDescriptor.getSlots();
        ArrayList<FrameSlot> slotsArr = new ArrayList<>();
        for (FrameSlot fs : slotsList) {
            FrameSlotKind kind = fs.getKind();
            if (FrameSlotKind.Illegal.equals(kind)) {
                continue;
            }
            slotsArr.add(fs);
        }
        slots = slotsArr.toArray(new FrameSlot[]{});
        slotNames = new String[slots.length];
        slotTypes = new String[slots.length];
        for (int i = 0; i < slots.length; i++) {
            slotNames[i] = slots[i].getIdentifier().toString();
            slotTypes[i] = slots[i].getKind().toString();
        }
        //System.err.println("FrameInfo: arguments = "+Arrays.toString(arguments));
        //System.err.println("           identifiers = "+frameDescriptor.getIdentifiers());
        if (false /* TODO: frame instanceof VirtualFrame*//*) {
            /* TODO: Find "this"
            Object thisObj;
            try {
                thisObj = JSFrameUtil.getThisObj((VirtualFrame) frame);
            } catch (ArrayIndexOutOfBoundsException aioobex) {
                aioobex.printStackTrace();
                thisObj = null;
            }
            //System.err.println("           this = "+thisObj);
            thisObject = thisObj;
            *//*
        } else if (arguments.length > 1) {
            thisObject = arguments[0];
        } else {
            thisObject = null;
        }
        SourcePosition position = JPDATruffleDebugManager.getPosition(astNode);
        //thisObject = new TruffleObject(context, "this", thisObj);
        /*
        System.err.println("JPDADebugClient: HALTED AT "+astNode+", "+frame+
        "\n                 src. pos. = "+
        position.path+":"+position.line);
        System.err.println("  frame arguments = "+Arrays.toString(arguments));
        System.err.println("  identifiers = "+Arrays.toString(identifiers.toArray()));
        System.err.println("  slots = "+Arrays.toString(slotsList.toArray()));
        for (int i = 0; i < slots.length; i++) {
        System.err.println("    "+slotNames[i]+" = "+JPDATruffleAccessor.getSlotValue(frame, slots[i]));
        }
         *//*
        List<FrameInstance> frames = null;
        int n = stack.size();
        if (n > 0) {
            this.frame = stack.get(0);
        } else {
            this.frame = null;
        }
        for (int i = 0; i < n; i++) {
            FrameInstance fi = stack.get(i);
            /*
            Frame iFrame = fi.getFrame(FrameInstance.FrameAccess.MATERIALIZE, true);
            System.err.println("stack("+i+"): fi = "+fi+", frame = "+iFrame+" ("+frame.getFrameDescriptor().toString()+"), call node = "+fi.getCallNode());
            *//*
            // Filter frames with null call node. How should we display them?
            Node node = fi.getCallNode();
            SourceSection ss;
            if (node == null ||
                ((ss = node.getSourceSection()) == null && (ss = node.getEncapsulatingSourceSection()) == null) ||
                ss.getSource() == null) {
                
                if (frames == null) {
                    frames = new ArrayList<>();
                    for (int j = 0; j < i; j++) {
                        frames.add(stack.get(j));
                    }
                }
            } else if (frames != null) {
                frames.add(fi);
            }
        }
        if (frames == null) {
            frames = stack;
        }
        stackTrace = frames.toArray(new FrameInstance[frames.size()]);
        
        /*
        String[] stackNames = new String[stackTrace.length];
        for (int i = 0; i < stackTrace.length; i++) {
        //stackNames[i] = stackTrace[i].getCallNode().getDescription();
        stackNames[i] = visualizer.displaySourceLocation(stackTrace[i].getCallNode());
        }*//*
        //System.err.println("  stack trace = "+java.util.Arrays.toString(stackTrace));
        //System.err.println("  stack names = "+Arrays.toString(stackNames));
        topFrame = DebuggerVisualizer.getDisplayName(astNode.getRootNode().getCallTarget()) + "\n" +
                   DebuggerVisualizer.getMethodName(astNode.getRootNode()) + "\n" +
                   DebuggerVisualizer.getSourceLocation(astNode) + "\n" +
                   position.id + "\n" + position.name + "\n" + position.path + "\n" +
                   position.uri.toString() + "\n" + position.line;
        //System.err.println("  top frame = \n'"+topFrame+"'");
    }
    */

    FrameInfo(DebugStackFrame topStackFrame, Iterable<DebugStackFrame> stackFrames) {
        SourceSection topSS = topStackFrame.getSourceSection();
        SourcePosition position = JPDATruffleDebugManager.getPosition(topSS);
        ArrayList<DebugStackFrame> stackFramesArray = new ArrayList<>();
        for (DebugStackFrame sf : stackFrames) {
            SourceSection ss = sf.getSourceSection();
            // Ignore frames without sources:
            if (ss == null || ss.getSource() == null) {
                continue;
            }
            stackFramesArray.add(sf);
        }
        frame = topStackFrame;
        stackTrace = stackFramesArray.toArray(new DebugStackFrame[stackFramesArray.size()]);
        topFrame = topStackFrame.getName() + "\n" +
                   DebuggerVisualizer.getSourceLocation(topSS) + "\n" +
                   position.id + "\n" + position.name + "\n" + position.path + "\n" +
                   position.uri.toString() + "\n" + position.line;
        topVariables = JPDATruffleAccessor.getVariables(topStackFrame);
    }
    
}
