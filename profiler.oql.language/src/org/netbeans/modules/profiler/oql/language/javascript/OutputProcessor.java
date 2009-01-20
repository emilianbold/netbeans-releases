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
package org.netbeans.modules.profiler.oql.language.javascript;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * An OutputProcessor takes filename and lineno information
 * and produces hyperlinks. Actually resolving filenames
 * into real FileObjects is done lazily via user-supplied
 * FileLocators when the links are actually clicked.
 * 
 * @todo Rename me!
 * @author Tor Norbye
 */
public class OutputProcessor implements OutputListener {
    
    public static final Logger LOGGER = Logger.getLogger(OutputListener.class.getName());
    
    private final FileObject file;
    private final int lineno;

    OutputProcessor(FileObject file, int lineno) {
        if (lineno < 0) {
            lineno = 0;
        }

        // TODO : columns?
        this.file = file;
        this.lineno = lineno;
    }

    public void outputLineSelected(OutputEvent ev) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void outputLineAction(OutputEvent ev) {
        open(file, lineno);
    }

    public void outputLineCleared(OutputEvent ev) {
    }

    public static boolean open(final FileObject fo, final int lineno) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        open(fo, lineno);
                    }
                });

            return true; // not exactly accurate, but....
        }

        try {
            DataObject od = DataObject.find(fo);
            EditorCookie ec = (EditorCookie)od.getCookie(EditorCookie.class);
            LineCookie lc = (LineCookie)od.getCookie(LineCookie.class);

            if ((ec != null) && (lc != null)) {
                Document doc = ec.openDocument();

                if (doc != null) {
                    int line = lineno;

                    if (line < 1) {
                        line = 1;
                    }

                    Line l = lc.getLineSet().getCurrent(line - 1);

                    if (l != null) {
                        l.show(Line.SHOW_GOTO);

                        return true;
                    }
                }
            }

            OpenCookie oc = (OpenCookie)od.getCookie(OpenCookie.class);

            if (oc != null) {
                oc.open();

                return true;
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }

        return false;
    }
}
