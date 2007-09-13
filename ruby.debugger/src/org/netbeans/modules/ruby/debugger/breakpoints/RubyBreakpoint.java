/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.debugger.breakpoints;

import java.util.logging.Level;
import org.netbeans.api.debugger.Breakpoint;
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

    private boolean enabled;
    private final Line line;

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
        return getFilePath() + ":" + getLineNumber();
    }
}