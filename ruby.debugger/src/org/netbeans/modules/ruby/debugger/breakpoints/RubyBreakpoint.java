/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ruby.debugger.breakpoints;

import java.util.logging.Level;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.ruby.debugger.ContextProviderWrapper;
import org.netbeans.modules.ruby.debugger.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.rubyforge.debugcommons.RubyDebuggerException;
import org.rubyforge.debugcommons.RubyDebuggerProxy;
import org.rubyforge.debugcommons.model.IRubyBreakpoint;

/**
 * @author Martin Krauskopf
 */
public final class RubyBreakpoint extends Breakpoint implements IRubyBreakpoint {
    
    static final String PROP_UPDATED = "updated"; // NOI18N

    private boolean enabled;
    private Line line;

    RubyBreakpoint(final Line line) {
        this.line = line;
        this.enabled = true;
    }

    private void updateBreakpoint() {
        for (RubyDebuggerProxy proxy : RubyDebuggerProxy.PROXIES) {
            try {
                proxy.updateBreakpoint(this);
            } catch (RubyDebuggerException e) {
                Util.LOGGER.log(Level.WARNING, "Exception during breakpoint update.", e);
            }
        }
    }
    
    public void notifyUpdated() {
        ContextProviderWrapper.getBreakpointModel().fireChanges();
        firePropertyChange(RubyBreakpoint.PROP_UPDATED, null, null);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void disable() {
        if (enabled) {
            enabled = false;
            updateBreakpoint();
            firePropertyChange(PROP_ENABLED, true, false);
        }
    }

    public void enable() {
        if (!enabled) {
            enabled = true;
            updateBreakpoint();
            firePropertyChange(PROP_ENABLED, false, true);
        }
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
        firePropertyChange(PROP_UPDATED, false, true);
    }

    public FileObject getFileObject() {
        return getLine().getLookup().lookup(FileObject.class);
    }

    public String getFilePath() {
        return FileUtil.toFile(getFileObject()).getAbsolutePath();
    }

    public int getLineNumber() {
        // Note that Line.getLineNumber() starts at zero
        return getLine().getLineNumber() + 1;
    }

    public @Override String toString() {
        return getFilePath() + ':' + getLineNumber();
    }
}
