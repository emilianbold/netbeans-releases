/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.execution.impl;

import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

public final class OutputListenerImpl implements OutputListener {

    private FileObject file;
    private int line;

    public OutputListenerImpl(FileObject file, int line) {
        super();
        this.file = file;
        this.line = line;
    }

    public void outputLineSelected(OutputEvent ev) {
        showLine(false);
    }

    public void outputLineAction(OutputEvent ev) {
        showLine(true);
    }

    public void outputLineCleared(OutputEvent ev) {
        ErrorAnnotation.getInstance().detach(null);
    }

    private void showLine(boolean openTab) {
        try {
            DataObject od = DataObject.find(file);
            LineCookie lc = od.getCookie(LineCookie.class);
            if (lc != null) {
                try {
                    // TODO: IZ#119211
                    // Preprocessor supports #line directive =>
                    // line number can be out of scope
                    Line l = lc.getLineSet().getOriginal(line);
                    if (!l.isDeleted()) {
                        if (openTab) {
                            l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                        } else {
                            l.show(Line.ShowOpenType.NONE, Line.ShowVisibilityType.NONE);
                        }
                        ErrorAnnotation.getInstance().attach(l);
                    }
                } catch (IndexOutOfBoundsException ex) {
                }
            }
        } catch (DataObjectNotFoundException ex) {
        }
    }
}
