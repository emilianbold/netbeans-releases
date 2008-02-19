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

package org.netbeans.modules.ruby.platform;

import java.util.prefs.Preferences;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.openide.util.NbPreferences;

/**
 * Preferences for Ruby Debugger.
 */
public class DebuggerPreferences {
    
    private static final DebuggerPreferences INSTANCE = new DebuggerPreferences();
    
    private static final String USE_CLASSIC_DEBUGGER = "use-classic-debugger"; // NOI18N
    private static final String DO_NOT_ASK_AGAIN = "fast-debugger-do-not-ask-again"; // NOI18N
    private static final String IS_FIRST_TIME = "is-called-first-time"; // NOI18N
    
    private DebuggerPreferences() {}
    
    public static DebuggerPreferences getInstance() {
        return INSTANCE;
    }
    
    public void setUseClassicDebugger(final RubyPlatform platform, final boolean useClassicDebugger) {
        getPreferences().putBoolean(USE_CLASSIC_DEBUGGER + platform.getID(), useClassicDebugger);
    }
    
    public boolean isUseClassicDebugger(final RubyPlatform platform) {
        return getPreferences().getBoolean(USE_CLASSIC_DEBUGGER + platform.getID(), true);
    }
    
    public void setDoNotAskAgain(final boolean doNotAskAgain) {
        getPreferences().putBoolean(DO_NOT_ASK_AGAIN, doNotAskAgain);
    }
    
    public boolean isDoNotAskAgain() {
        return getPreferences().getBoolean(DO_NOT_ASK_AGAIN, false);
    }
    
    public void setFirstTime(final boolean firstTime) {
        getPreferences().putBoolean(IS_FIRST_TIME, firstTime);
    }
    
    public boolean isFirstTime() {
        return getPreferences().getBoolean(IS_FIRST_TIME, true);
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(DebuggerPreferences.class);
    }
    
}
