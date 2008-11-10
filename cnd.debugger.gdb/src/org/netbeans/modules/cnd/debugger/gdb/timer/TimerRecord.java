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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.debugger.gdb.timer;

/**
 *
 * @author gordon
 */
public class TimerRecord {

    enum RecordType {Title, Start, Mark, Stop, Restart};
    
    private final RecordType type;
    //private final String tname;
    private final long time;
    private final String message;
    
    /** Creates a new instance of TimerRecord */
    public TimerRecord(RecordType type, String message) {
        this.type = type;
        //tname = Thread.currentThread().getName();
        time = System.currentTimeMillis();
        if (message != null && message.length() > 0) {
            this.message = message + ": "; // NOI18N
        } else {
            this.message = "";
        }
    }
    
    public long getTime() {
        return time;
    }
    
    public String toString() {
        return toString(-1);
    }
    
    public String toString(long relativeTo) {
        StringBuilder s = new StringBuilder();
        
        if (type == RecordType.Title) {
            s.append("\n\nGdbTimer Report: " + message); // NOI18N
        } else {
            s.append("    "); // NOI18N
            s.append(message);
            if (type == RecordType.Start) {
                s.append("Started at "); // NOI18N
            } else if (type == RecordType.Mark) {
                s.append("Marked at "); // NOI18N
            } else if (type == RecordType.Stop) {
                s.append("Stopped at "); // NOI18N
            } else if (type == RecordType.Restart) {
                s.append("Restarteded at "); // NOI18N
            }
            if (relativeTo < 0) {
                s.append(time);
            } else {
                s.append(time - relativeTo);
                s.append(" ms"); // NOI18N
        }
        }
        return s.toString();
    }
}
