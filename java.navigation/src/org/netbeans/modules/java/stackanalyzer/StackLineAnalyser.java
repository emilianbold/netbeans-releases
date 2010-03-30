/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.stackanalyzer;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.StyledDocument;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;

/**
 *
 * @author Jan Jancura
 */
class StackLineAnalyser {

    private static final String IDENTIFIER =
        "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";    // NOI18N
    private static final Pattern LINE_PATTERN = Pattern.compile(
        "at\\s" +                                       //  initial at // NOI18N
        "(("+IDENTIFIER+"(\\."+IDENTIFIER+")*)\\.)?("+IDENTIFIER+")" + // class name // NOI18N
        "\\.("+IDENTIFIER+"|\\<init\\>|\\<clinit\\>)\\("+IDENTIFIER+"\\.java" + // method and file name // NOI18N
        "\\:([0-9]*)\\)");                              // line number // NOI18N

    static boolean matches(String line) {
        Matcher matcher = LINE_PATTERN.matcher(line);
        return matcher.find();
    }

    static Link analyse(String line) {
        Matcher matcher = LINE_PATTERN.matcher(line);
        if (matcher.find()) {
            int lineNumber = -1;
            try {
                lineNumber = Integer.parseInt(matcher.group(6));
            } catch (NumberFormatException nfe) {
                return null;
            }
            return new Link(matcher.group(1) + matcher.group(4).split("\\$")[0],
                            lineNumber,
                            matcher.start(1),
                            matcher.end(6)+1
                            );
        }
        return null;
    }

    static class Link {

        private String          className;
        private int             lineNumber;
        private int             startOffset;
        private int             endOffset;

        private  Link (
            String              className,
            int                 lineNumber,
            int                 startOffset,
            int                 endOffset
        ) {
            this.className = className;
            this.lineNumber = lineNumber;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        int getStartOffset () {
            return startOffset;
        }

        int getEndOffset () {
            return endOffset;
        }

        void show () {
            String resource = className.replace ('.', '/') + ".java";
            ClassPath classPath = ClassPathSupport.createClassPath (
                GlobalPathRegistry.getDefault ().getSourceRoots ().toArray (new FileObject [0])
            );
            FileObject fileObject = classPath.findResource (resource);
            if (fileObject == null) return;
            try {
                DataObject dataObject = DataObject.find (fileObject);
                EditorCookie editorCookie = (EditorCookie) dataObject.getCookie (EditorCookie.class);
                LineCookie lineCookie = (LineCookie) dataObject.getCookie (LineCookie.class);
                if (editorCookie != null && lineCookie != null && lineNumber != -1) {
                    StyledDocument doc = editorCookie.openDocument ();
                    if (doc != null) {
                        if (lineNumber != -1) {
                            Line l = lineCookie.getLineSet ().getCurrent (lineNumber - 1);

                            if (l != null) {
                                l.show (Line.SHOW_GOTO);
                                return;
                            }
                        }
                    }
                }
                OpenCookie openCookie = (OpenCookie) dataObject.getCookie (OpenCookie.class);
                if (openCookie != null) {
                    openCookie.open ();
                    return;
                }
            } catch (IOException e) {
                ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
            }
        }

        boolean hasSource () {
            String resource = className.replace ('.', '/') + ".java";
            ClassPath classPath = ClassPathSupport.createClassPath (
                GlobalPathRegistry.getDefault ().getSourceRoots ().toArray (new FileObject [0])
            );
            return classPath.findResource (resource) != null;
        }
    }
}
