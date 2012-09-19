/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.cnd.debugger.gdb2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Platform;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIConst;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIRecord;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIResult;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITList;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITListItem;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIValue;

/**
 * Contains actions which may vary in different versions of gdb
 *
 * @author Egor Ushakov
 */
public class GdbVersionPeculiarity {
    private final double version;
    private final Platform platform;
    private final Set<String> features = Collections.synchronizedSet(new HashSet<String>());

    private GdbVersionPeculiarity(double version, Platform platform) {
        this.version = version;
        this.platform = platform;
    }

    public static GdbVersionPeculiarity create(double version, Platform platform) {
        return new GdbVersionPeculiarity(version, platform);
    }

    public String environmentDirectoryCommand() {
        if (version > 6.3 || platform == Platform.MacOSX_x86) {
            return "-environment-directory"; // NOI18N
        } else {
            return "directory"; // NOI18N
        }
    }

    public String environmentCdCommand() {
        if (version > 6.3) {
            return "-environment-cd"; // NOI18N
        } else {
            return "cd"; // NOI18N
        }
    }

    public String execAbortCommand() {
        if (version > 6.6) {
            return "-exec-abort"; // NOI18N
        } else {
            return "kill"; // NOI18N
        }
    }
    
    public String listChildrenCommand(String expr, int start, int end) {
        String retVal = "-var-list-children --all-values \"" + expr + "\""; // NOI18N
        
        if (version > 6.8) {
            retVal = retVal + " " + start + " " + end; // NOI18N
        }
        
        return retVal;
    }
    
    public boolean isThreadsOutputUnusual() {
        return platform == Platform.MacOSX_x86;
    }

    private static final boolean DISABLE_PENDING = Boolean.getBoolean("gdb.breakpoints.pending.disabled"); //NOI18N

    public String breakPendingFlag() {
        if (!DISABLE_PENDING
                && (version >= 6.8 || platform == Platform.MacOSX_x86)) {
            return " -f"; // NOI18N
        } else {
            return "";
        }
    }
    
    public String breakDisabledFlag() {
        if (version >= 6.8 || platform == Platform.MacOSX_x86) {
            return " -d"; // NOI18N
        } else {
            return "";
        }
    }

    public boolean isSupported() {
        return (version >= 6.8) || (platform == Platform.MacOSX_x86 && version >= 6.3);
    }
    
    // gdb features
    public static enum Feature {
        THREAD_INFO("thread-info"), //NOI18N
        BREAKPOINT_NOTIFICATIONS("breakpoint-notifications"); //NOI18N
        
        private final String command;
        Feature(String command) {
            this.command = command;
        }
    }
    
    public boolean supports(Feature feature) {
        return features.contains(feature.command);
    }
    
    void setFeatures(MIRecord result) {
        synchronized (features) {
            features.clear();
            try {
                MITList results = result.results();
                MIValue value = ((MIResult)results.get(0)).value();
                for (MITListItem item : value.asList()) {
                    features.add(((MIConst)item).value());
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }
}
