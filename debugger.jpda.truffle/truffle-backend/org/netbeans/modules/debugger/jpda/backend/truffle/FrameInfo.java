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
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Collects stack frame information.
 * 
 * @author martin
 */
final class FrameInfo {
    final DebugStackFrame frame;  // the top frame instance
    final DebugStackFrame[] stackTrace; // All but the top frame
    final String topFrame;
    final Object[] topVariables;
    // TODO: final Object[] thisObjects;

    FrameInfo(DebugStackFrame topStackFrame, Iterable<DebugStackFrame> stackFrames) {
        SourceSection topSS = topStackFrame.getSourceSection();
        SourcePosition position = JPDATruffleDebugManager.getPosition(topSS);
        ArrayList<DebugStackFrame> stackFramesArray = new ArrayList<>();
        for (DebugStackFrame sf : stackFrames) {
            if (sf == topStackFrame) {
                continue;
            }
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
                   position.uri.toString() + "\n" + position.line + "\n" + isInternal(topStackFrame);
        topVariables = JPDATruffleAccessor.getVariables(topStackFrame);
    }
    
    /** Calls DebugStackFrame.isInternal() with workarounds for NPEs */
    static boolean isInternal(DebugStackFrame sf) {
        boolean isInternal = false;
        try {
            isInternal = sf.isInternal();
        } catch (Exception ex) {
            LangErrors.exception("Frame "+sf.getName()+" .isInternal()", ex);
            //System.err.println("Is Internal blew up for "+sf+", name = "+sf.getName()+", source = "+DebuggerVisualizer.getSourceLocation(sf.getSourceSection()));
            //System.err.println("  source section = "+sf.getSourceSection());
            try {
                Method findCurrentRootMethod = DebugStackFrame.class.getDeclaredMethod("findCurrentRoot");
                findCurrentRootMethod.setAccessible(true);
                RootNode rn = (RootNode) findCurrentRootMethod.invoke(sf);
                //System.err.println("  root node = "+rn);
                //System.err.println("  source section = "+rn.getSourceSection());
                isInternal = rn.getSourceSection() == null;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                     NoSuchMethodException | SecurityException ex2) {
                LangErrors.exception("Frame "+sf.getName()+" findCurrentRoot() invocation", ex2);
            }
        }
        return isInternal;
    }
    
}
