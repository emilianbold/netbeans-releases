/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.output;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;


/**
 * compile error editor annotation
 * @author  Milos Kleint 
 */
public final class CompileAnnotation /*extends Annotation */implements /*PropertyChangeListener,*/ OutputListener {
    
    File clazzfile; //for tests..
    private int lineNum;
    private String text;
    
    public CompileAnnotation(File clazz, String line, String textAnn) {
        clazzfile = clazz;
        text = textAnn;
        try {
            lineNum = Integer.parseInt(line);
        } catch (NumberFormatException exc) {
            lineNum = -1;
        }
    }
    
    
    
    public void outputLineSelected(OutputEvent ev) {
        //           cookie.getLineSet().getCurrent(line).show(Line.SHOW_SHOW);
    }
    
    /** Called when some sort of action is performed on a line.
     * @param ev the event describing the line
     */
    public void outputLineAction(OutputEvent ev) {
        FileUtil.refreshFor(clazzfile);
        FileObject file = FileUtil.toFileObject(clazzfile);
        if (file == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try {
            DataObject dob = DataObject.find(file);
            EditorCookie ed = dob.getCookie(EditorCookie.class);
            if (ed != null && file == dob.getPrimaryFile()) {
                if (lineNum == -1) {
                    ed.open();
                } else {
                    ed.openDocument();
                    try {
                        Line l = ed.getLineSet().getOriginal(lineNum - 1);
                        if (! l.isDeleted()) {
                            l.show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS);
                        }
                    } catch (IndexOutOfBoundsException ioobe) {
                        // Probably harmless. Bogus line number.
                        ed.open();
                    }
                }
//                attachAllInFile(ed, this);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        } catch (DataObjectNotFoundException donfe) {
            ErrorManager.getDefault().notify(donfe);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
//    private static void attachAllInFile(EditorCookie cook, CompileAnnotation annot) {
//        Set<CompileAnnotation> newSet = null;
//        synchronized (hyperlinks) {
//            newSet = new HashSet<CompileAnnotation>(hyperlinks);
//        }
//        Iterator it = newSet.iterator();
//        while (it.hasNext()) {
//            CompileAnnotation ann = (CompileAnnotation)it.next();
//            if (ann.getFile().equals(annot.getFile())) {
//                if (ann.getLine() != -1) {
//                    Line l = cook.getLineSet().getOriginal(ann.getLine() - 1);
//                    if (! l.isDeleted()) {
//                        ann.attachAsNeeded(l);
//                    }
//                }
//
//            }
//        }
//    }
    
    /** Called when a line is cleared from the buffer of known lines.
     * @param ev the event describing the line
     */
    public void outputLineCleared(OutputEvent ev) {
//        doDetach();
    }
    
//    void destroy() {
//        doDetach();
//        dead = true;
//    }
//
//    private synchronized void attachAsNeeded(Line l) {
//        if (getAttachedAnnotatable() == null) {
//            Annotatable ann = l;
//            attach(ann);
//            ann.addPropertyChangeListener(this);
//        }
//    }
//
//
//    private synchronized void doDetach() {
//        Annotatable ann = getAttachedAnnotatable();
//        if (ann != null) {
//            ann.removePropertyChangeListener(this);
//            detach();
//        }
//        synchronized (hyperlinks) {
//            hyperlinks.remove(this);
//        }
//    }
//
//    public void propertyChange(PropertyChangeEvent ev) {
//        if (dead) {
//            return;
//        }
//        String prop = ev.getPropertyName();
//        if (    prop == null
//             || prop.equals(Annotatable.PROP_TEXT)
//             || prop.equals(Annotatable.PROP_DELETED)) {
//            doDetach();
//        }
//    }
//
//    public String getAnnotationType() {
//        return "org-codehaus-mevenide-netbeans-project-error"; // NOI18N
//    }
//
//    public String getShortDescription() {
//        return text;
//    }
//
//    public File getFile() {
//        return clazzfile;
//    }
//
//    public int getLine() {
//        return lineNum;
//    }
    
    @Override
    public String toString() {
        return "error[" + clazzfile + ":" + lineNum + ":" + text + "]"; // NOI18N
    }
    
}
