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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.netbeans.modules.ruby.debugger.ContextProviderWrapper;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.Exceptions;

/** Simplified, heavily based on Java Debugger code. */
final class BreakpointLineUpdater implements PropertyChangeListener {

    private RubyBreakpoint breakpoint;
    private DataObject dataObject;
    private LineCookie lc;
    private Line line;

    public BreakpointLineUpdater(RubyBreakpoint breakpoint) {
        this.breakpoint = breakpoint;
        try {
            this.dataObject = DataObject.find(breakpoint.getFileObject());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public synchronized void attach() throws IOException {
        this.lc = dataObject.getCookie(LineCookie.class);
        breakpoint.addPropertyChangeListener(this);
        try {
            this.line = lc.getLineSet().getCurrent(breakpoint.getLineNumber() - 1);
            line.addPropertyChangeListener(this);
        } catch (IndexOutOfBoundsException ioobex) {
            // ignore document changes for BP with bad line number
        }
    }

    public synchronized void detach() {
        breakpoint.removePropertyChangeListener(this);
        if (line != null) {
            line.removePropertyChangeListener(this);
        }
    }

    public synchronized void propertyChange(PropertyChangeEvent evt) {
        if (Line.PROP_LINE_NUMBER.equals(evt.getPropertyName()) && line == evt.getSource()) {
            breakpoint.notifyUpdated();
            return;
        }
    }
}
