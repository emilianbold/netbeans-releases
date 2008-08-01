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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.Exceptions;

/** Simplified, heavily based on Java Debugger code. */
final class BreakpointLineUpdater implements PropertyChangeListener {

    private final RubyLineBreakpoint breakpoint;
    private DataObject dataObject;
    private Line line;

    public BreakpointLineUpdater(RubyLineBreakpoint breakpoint) {
        this.breakpoint = breakpoint;
        try {
            this.dataObject = DataObject.find(breakpoint.getFileObject());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public synchronized void attach() throws IOException {
        breakpoint.addPropertyChangeListener(this);
        try {
            LineCookie lc = dataObject.getCookie(LineCookie.class);
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
