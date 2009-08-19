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

package org.netbeans.modules.ruby.debugger;

import java.awt.EventQueue;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;

public final class EditorUtil {

    private static final Logger LOGGER = Logger.getLogger(EditorUtil.class.getName());

    /** For unit tests to be able to prevent editor pop-ups. */
    public static boolean showLines = true;
    
    private EditorUtil() {}
    
    private static DebuggerAnnotation currentLineDA;
    
//    public static boolean contains(final Object currentLine, final Line line) {
//        if (currentLine == null) return false;
//        final Annotatable[] annotables = (Annotatable[]) currentLine;
//        for (Annotatable ann : annotables) {
//            if (ann.equals(line)) {
//                return true;
//            }
//            if (ann instanceof Line.Part && ((Line.Part) ann).getLine().equals(line)) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * Make line in the editor current - shows the line and colorizes it
     * appropriately (green-striped by default). Note that editor counts lines
     * from zero.
     *
     * @see unmarkCurrent()
     */
    static void markCurrent(final String filePath, final int lineNumber) {
        markCurrent(getLineAnnotable(filePath, lineNumber));
    }
    
    private static void markCurrent(final Line line) {
        unmarkCurrent();
        if (line == null) {
            return;
        }
        currentLineDA = new DebuggerAnnotation(DebuggerAnnotation.CURRENT_LINE_ANNOTATION_TYPE, line);
        showLine(line, true);
    }
    
    /**
     * Cancel effect of {@link #markCurrent(String, int)} method. I.e. removes
     * annotation, usually the green stripe.
     */
    static void unmarkCurrent() {
        if (currentLineDA != null) {
            currentLineDA.detach();
            currentLineDA = null;
        }
    }
    
    public static Line getLineAnnotable(final String filePath, final int lineNumber) {
        return getLine(filePath, lineNumber);
    }

    public static Line getLine(final String filePath, final int lineNumber) {
        if (filePath == null || lineNumber < 0) {
            return null;
        }

        File file = new File(filePath);
        FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fileObject == null) {
            LOGGER.info("Cannot resolve \"" + filePath + '"');
            return null;
        }

        LineCookie lineCookie = getLineCookie(fileObject);
        if (lineCookie == null) {
            LOGGER.info("Can't find LineCookie for: " + fileObject);
            return null;
        }
        try {
            return lineCookie.getLineSet().getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException ioobe) {
            // invalid line number for the document
            return null;
        }
    }

    public static LineCookie getLineCookie(final FileObject fo) {
        LineCookie result = null;
        try {
            DataObject dataObject = DataObject.find(fo);
            if (dataObject != null) {
                result = dataObject.getCookie(LineCookie.class);
            }
        } catch (DataObjectNotFoundException e) {
            LOGGER.log(Level.INFO, "Cannot find DataObject for: " + fo, e.getMessage());
        }
        return result;
    }
    
    public static void showLine(final Line lineToShow, final boolean toFront) {
        if (!showLines) {
            return;
        }
        if (lineToShow == null) {
            return;
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                lineToShow.show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS);
            }
        });
    }
    
    public static void showLine(final Line line) {
        showLine(line, false);
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Editor boilerplate ">
    /**
     * Returns current editor line (the line where the carret currently is).
     * Might be <code>null</code>. Works only for {@link Util#isRubySource
     * supported mime-types}. For unsupported ones returns <code>null</code>.
     */
    public static Line getCurrentLine() {
        FileObject fo = EditorContextDispatcher.getDefault().getCurrentFile();
        if (fo == null) { return null; }
        if (!Util.isRubySource(fo)) {
            return null;
        }
        return EditorContextDispatcher.getDefault().getCurrentLine();
    }
    
    // </editor-fold>
    
}
