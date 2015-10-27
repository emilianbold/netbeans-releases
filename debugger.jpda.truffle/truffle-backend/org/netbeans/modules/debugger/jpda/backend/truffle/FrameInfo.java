/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.instrument.Visualizer;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.SourceSection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class FrameInfo {
    final FrameInstance frame;  // the top frame instance
    final FrameSlot[] slots;
    final String[] slotNames;
    final String[] slotTypes;
    final FrameInstance[] stackTrace;
    final String topFrame;
    final Object thisObject;

    public FrameInfo(MaterializedFrame frame, Visualizer visualizer, Node astNode,
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
            if (visualizer != null) {
                slotNames[i] = visualizer.displayIdentifier(slots[i]); // slots[i].getIdentifier().toString();
            } else {
                slotNames[i] = slots[i].getIdentifier().toString();
            }
            slotTypes[i] = slots[i].getKind().toString();
        }
        //System.err.println("FrameInfo: arguments = "+Arrays.toString(arguments));
        //System.err.println("           identifiers = "+frameDescriptor.getIdentifiers());
        if (false /* TODO: frame instanceof VirtualFrame*/) {
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
            */
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
         */
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
            */
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
        }*/
        //System.err.println("  stack trace = "+java.util.Arrays.toString(stackTrace));
        //System.err.println("  stack names = "+Arrays.toString(stackNames));
        if (visualizer != null) {
            topFrame = visualizer.displayCallTargetName(astNode.getRootNode().getCallTarget()) + "\n" + visualizer.displayMethodName(astNode) + "\n" + visualizer.displaySourceLocation(astNode) + "\n" + position.id + "\n" + position.name + "\n" + position.path + "\n" + position.line;
        } else {
            topFrame = astNode.getRootNode().getCallTarget().toString() + "\n" + astNode.toString() + "\n" + astNode.getSourceSection().getShortDescription() + "\n" + position.id + "\n" + position.name + "\n" + position.path + "\n" + position.line;
        }
        //System.err.println("  top frame = \n'"+topFrame+"'");
    }
    
}
