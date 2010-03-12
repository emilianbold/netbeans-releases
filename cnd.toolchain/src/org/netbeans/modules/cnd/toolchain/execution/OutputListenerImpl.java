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

package org.netbeans.modules.cnd.toolchain.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.StyledDocument;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

public final class OutputListenerImpl implements OutputListener {
    private static final String CC_compiler_errors = "C/C++ compiler errors"; // NOI18N

    private final OutputListenerFactory manager;
    private final FileObject file;
    private final int line;
    private final boolean isError;
    private final String description;

    public OutputListenerImpl(OutputListenerFactory manager, FileObject file, int line, boolean isError, String description) {
        this.manager = manager;
        this.file = file;
        this.line = line;
	this.isError = isError;
        this.description = description;
    }

    @Override
    public void outputLineSelected(OutputEvent ev) {
        showLine(false);
    }

    @Override
    public void outputLineAction(OutputEvent ev) {
        showLine(true);
    }

    @Override
    public void outputLineCleared(OutputEvent ev) {
        try {
            DataObject dob = DataObject.find(file);
            StyledDocument doc = null;
            if (dob.isValid()) {
                EditorCookie ec = dob.getCookie(EditorCookie.class);
                if (ec != null) {
                    doc = ec.getDocument();
                }
            }
            if (doc != null) {
                HintsController.setErrors(doc, CC_compiler_errors, Collections.<ErrorDescription>emptyList());
            }
        } catch (DataObjectNotFoundException ex) {
        }
    }

    public boolean isError(){
	return isError;
    }

    private void showLine(boolean openTab) {
        try {
            DataObject dob = DataObject.find(file);
            LineCookie lc = dob.getCookie(LineCookie.class);
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
                        StyledDocument doc = null;
                        if (dob.isValid()) {
                            EditorCookie ec = dob.getCookie(EditorCookie.class);
                            if (ec != null) {
                                doc = ec.getDocument();
                            }
                        }
                        if (doc != null) {
                            List<ErrorDescription> errors = new ArrayList<ErrorDescription>();
                            for(OutputListenerImpl impl : manager.getFileListeners(file)){
                                String aDescription = impl.description;
                                if (impl.isError) {
                                    if (aDescription == null) {
                                        aDescription = NbBundle.getMessage(OutputListenerImpl.class, "HINT_CompilerError"); // NOI18N
                                    }
                                    errors.add(ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, impl.description, doc, impl.line + 1));
                                } else {
                                    if (aDescription == null) {
                                        aDescription = NbBundle.getMessage(OutputListenerImpl.class, "HINT_CompilerWarning"); // NOI18N
                                    }
                                    errors.add(ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, impl.description, doc, impl.line + 1));
                                }
                            }
                            HintsController.setErrors(doc, CC_compiler_errors, errors);
                        }
                    }
                } catch (IndexOutOfBoundsException ex) {
                }
            }
        } catch (DataObjectNotFoundException ex) {
        }
    }
}
