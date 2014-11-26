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

package org.netbeans.modules.javascript.v8debug.vars.tooltip;

import java.io.Closeable;
import java.io.IOException;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.javascript2.debug.tooltip.DebuggerTooltipSupport;

/**
 *
 * @author Martin Entlicher
 */
public class V8DebuggerTooltipSupport implements DebuggerTooltipSupport {
    
    private final V8Debugger debugger;
    private final CallFrame currentFrame;
    private V8Debugger.Listener closeableListener;

    V8DebuggerTooltipSupport(V8Debugger debugger, CallFrame currentFrame) {
        this.debugger = debugger;
        this.currentFrame = currentFrame;
    }

    public V8Debugger getDebugger() {
        return debugger;
    }

    public CallFrame getCurrentFrame() {
        return currentFrame;
    }

    @Override
    public void addCloseable(Closeable closeable) {
        closeableListener = new TooltipCloseableListener(closeable);
        debugger.addListener(closeableListener);
    }

    @Override
    public void removeCloseable(Closeable closeable) {
        debugger.removeListener(closeableListener);
    }
    
    private static final class TooltipCloseableListener implements V8Debugger.Listener {
        
        private final Closeable closeable;
        
        public TooltipCloseableListener(Closeable closeable) {
            this.closeable = closeable;
        }

        @Override
        public void notifySuspended(boolean suspended) {
            if (!suspended) {
                doClose();
            }
        }

        @Override
        public void notifyCurrentFrame(CallFrame cf) {
            doClose();
        }
        
        @Override
        public void notifyFinished() {
            doClose();
        }

        private void doClose() {
            try {
                closeable.close();
            } catch (IOException ex) {}
        }
        
    }
    
}
