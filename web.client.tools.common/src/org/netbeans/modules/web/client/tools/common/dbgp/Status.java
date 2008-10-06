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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.web.client.tools.common.dbgp;

import java.util.logging.Level;
import org.w3c.dom.Node;

/**
 * @author ads, jdeva
 *
 */
public class Status {
    public enum State {
        STARTING,
        STOPPING,  //
        STOPPED,   // Stopped or disconnected
        RUNNING,   // Running
        
        FIRST_LINE, // firts line of javascript executed
        BREAKPOINT,     // Breakpoint
        STEP,      // Stopped due to Step 
        DEBUGGER,  // debugger keyword encountered
        EXCEPTION, // exception encountered
        UNKNOWN; // when status is not set

        public String getState() {
            return name().toLowerCase();
        }
    }

    public enum Reason {
        OK,
        ERROR,
        ABORTED,
        EXCEPTION,
        UNKNOWN;

        public String getReason() {
            return name().toLowerCase();
        }
    }

    public static class StatusCommand extends Command {
        public StatusCommand(int transactionId) {
            super(CommandMap.STATUS.getCommand(), transactionId);
        }
    }

    public static class StatusResponse extends ResponseMessage {
        private static final String REASON = "reason";
        private static final String STATUS = "status";

        StatusResponse(Node node) {
            super(node);
        }

        public State getState() {
            String status = getAttribute(getNode(), STATUS);
            try {
                return State.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                Log.getLogger().log(Level.FINE, "Unknown status: " + status);
                return State.UNKNOWN;
            }
        }

        public Reason getReason() {
            String reason = getAttribute(getNode(), REASON);
            try {
                return Reason.valueOf(reason.toUpperCase());
            } catch (IllegalArgumentException ex) {
                Log.getLogger().log(Level.FINE, "Unknown reason: " + reason);
                return Reason.UNKNOWN;
            }
        }

        public DebugMessage getDebugMessage(){
            return new DebugMessage(getChild(getNode(), DebugMessage.TAG));
        }
    }

    public static class DebugMessage extends BaseMessageChildElement {
        static final String TAG = "message";

        DebugMessage(Node node) {
            super(node);
        }

        public String getFileName() {
            return getAttribute(Breakpoint.FILENAME);
        }

        public int getLineNumber() {
            return getInt(Breakpoint.LINENO);
        }

        public String getBreakpointId() {
            return getAttribute(Breakpoint.ID);
        }
    }
}
