/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.NbBundle;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

import org.netbeans.api.java.classpath.GlobalPathRegistry;

/**
 * <code>LogSupport</code> class for creating links in the output window.
 *
 * 
 */
public class LogHyperLinkSupport {
    private Map/*<Link, Link>*/ links = Collections.synchronizedMap(new HashMap());
    private Annotation errAnnot;
    
    /**
     * Return a link which implements <code>OutputListener</code> interface. Link
     * is then used to represent a link in the output window. This class also 
     * handles error annotations which are shown after a line is clicked.
     * 
     * @return link which implements <code>OutputListener</code> interface. Link
     *         is then used to represent a link in the output window.
     */
    public Link getLink(String errorMsg, String path, int line) {
        Link newLink = new Link(errorMsg, path, line);
        Link cachedLink = (Link)links.get(newLink);
        if (cachedLink != null) {
            return cachedLink;
        }
        links.put(newLink, newLink);
        return newLink;
    }

    /**
     * Detach error annotation.
     */
    public void detachAnnotation() {
        if (errAnnot != null) {
            errAnnot.detach();
        }
    }
    
    /**
     * <code>LineInfo</code> is used to store info about the parsed line.
     */
    public static class LineInfo {
        private String path;
        private int line;
        private String message;
        private boolean error;
        private boolean accessible;
        
        /**
         * <code>LineInfo</code> is used to store info about the parsed line.
         *
         * @param path path to file
         * @param line line number where the error occurred
         * @param message error message
         * @param error represents the line an error?
         * @param accessible is the file accessible?
         */
        public LineInfo(String path, int line, String message, boolean error, boolean accessible) {
            this.path = path;
            this.line = line;
            this.message = message;
            this.error = error;
            this.accessible = accessible;
        }
        
        public String path() {
            return path;
        }
        
        public int line() {
            return line;
        }
        
        public String message() {
            return message;
        }
        
        public boolean isError() {
            return error;
        }
        
        public boolean isAccessible() {
            return accessible;
        }
        
        public String toString() {
            return "path=" + path + " line=" + line + " message=" + message 
                    + " isError=" + error + " isAccessible=" + accessible;
        }
    }    
    
    /**
     * Error annotation.
     */
    static class ErrorAnnotation extends Annotation {
        private String shortDesc = null;
        
        public ErrorAnnotation(String desc) {
            shortDesc = desc;
        }
        
        public String getAnnotationType() {
            return "org-netbeans-modules-j2ee-sunserver"; // NOI18N
        }
        
        public String getShortDescription() {
            return shortDesc;
        }
        
    }
    
    /**
     * <code>Link</code> is used to create a link in the output window. To create
     * a link use the <code>getLink</code> method of the <code>LogSupport</code>
     * class. This prevents from memory vast by returning already existing instance,
     * if one with such values exists.
     */
    public class Link implements OutputListener {
        private String msg;
        private String path;
        private int line;
        
        private int hashCode = 0;
        
        Link(String msg, String path, int line) {
            this.msg = msg;
            this.path = path;
            this.line = line;
        }
        
        public int hashCode() {
            if (hashCode == 0) {
                int result = 17;
                result = 37 * result + line;
                result = 37 * result + (path != null ? path.hashCode() : 0);
                result = 37 * result + (msg != null ? msg.hashCode() : 0);
                hashCode = result;
            }
            return hashCode;
        } 
        
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof Link) {
                Link anotherLink = (Link)obj;
                if ((((msg != null) && msg.equals(anotherLink.msg)) || (msg == anotherLink.msg))
                    && (((path != null) && path.equals(anotherLink.path)) || (path == anotherLink.path))
                    && line == anotherLink.line) {
                        return true;
                }
            }
            return false;
        }
        
        /**
         * If the link is clicked, required file is opened in the editor and an 
         * <code>ErrorAnnotation</code> is attached.
         */
        public void outputLineAction(OutputEvent ev) {
            FileObject sourceFile = GlobalPathRegistry.getDefault().findResource(path);
            if (sourceFile == null) {
                sourceFile = FileUtil.toFileObject(FileUtil.normalizeFile(new File(path)));
            }
            DataObject dataObject = null;
            if (sourceFile != null) {
                try {
                    dataObject = DataObject.find(sourceFile);
                } catch(DataObjectNotFoundException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            if (dataObject != null) {
                EditorCookie editorCookie = (EditorCookie)dataObject.getCookie(EditorCookie.class);
                if (editorCookie == null) {
                    return;
                }
                editorCookie.open();
                Line errorLine = null;
                try {
                    errorLine = editorCookie.getLineSet().getCurrent(line - 1);
                } catch (IndexOutOfBoundsException iobe) {
                    return;
                }
                if (errAnnot != null) {
                    errAnnot.detach();
                }
                String errorMsg = msg;
                if (errorMsg == null || errorMsg.equals("")) { //NOI18N
                    errorMsg = NbBundle.getMessage(Link.class, "MSG_ExceptionOccurred");
                }
                errAnnot = new ErrorAnnotation(errorMsg);
                errAnnot.attach(errorLine);
                errAnnot.moveToFront();
                errorLine.show(ShowOpenType.NONE, ShowVisibilityType.NONE);
            }
        }
        
        /**
         * If a link is cleared, error annotation is detached and link cache is 
         * clared.
         */
        public void outputLineCleared(OutputEvent ev) {
            if (errAnnot != null) {
                errAnnot.detach();
            }
            if (!links.isEmpty()) {
                links.clear();
            }
        }
        
        public void outputLineSelected(OutputEvent ev) {           
        }
    }    
    /**
     * Support class for context log line analyzation and for creating links in 
     * the output window.
     */
    public static class AppServerLogSupport extends LogHyperLinkSupport {
        private final String appServerInstallDir;
        private String context = null;
        private String prevMessage = null;
        private static final String STANDARD_CONTEXT = "StandardContext["; // NOI18N
        private static final int STANDARD_CONTEXT_LENGTH = STANDARD_CONTEXT.length();
        private GlobalPathRegistry globalPathReg = GlobalPathRegistry.getDefault();
        

        public AppServerLogSupport(String catalinaWork, String webAppContext) {
            appServerInstallDir = catalinaWork;
            context = webAppContext;
        }
        
        public LineInfo analyzeLine(String logLine) {
            String path = null;
            int line = -1;
            String message = null;
            boolean error = false;
            boolean accessible = false;

            logLine = logLine.trim();
            int lineLenght = logLine.length();

            // look for unix file links (e.g. /foo/bar.java:51: 'error msg')
            if (logLine.startsWith("/")) {
                error = true;
                int colonIdx = logLine.indexOf(':');
                if (colonIdx > -1) {
                    path = logLine.substring(0, colonIdx);
                    accessible = true;
                    if (lineLenght > colonIdx) {
                        int nextColonIdx = logLine.indexOf(':', colonIdx + 1);
                        if (nextColonIdx > -1) {
                            String lineNum = logLine.substring(colonIdx + 1, nextColonIdx);
                            try {
                                line = Integer.valueOf(lineNum).intValue();
                            } catch(NumberFormatException nfe) { 
                                accessible = true;
                                // ignore it
                                 //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nfe);
                            }
                            if (lineLenght > nextColonIdx) {
                                message = logLine.substring(nextColonIdx + 1, lineLenght); 
                            }
                        }
                    }
                }
            }
            // look for windows file links (e.g. c:\foo\bar.java:51: 'error msg')
            else if (lineLenght > 3 && Character.isLetter(logLine.charAt(0))
                        && (logLine.charAt(1) == ':') && (logLine.charAt(2) == '\\')) {
                error = true;
                int secondColonIdx = logLine.indexOf(':', 2);
                if (secondColonIdx > -1) {
                    path = logLine.substring(0, secondColonIdx);
                    accessible = true;
                    if (lineLenght > secondColonIdx) {
                        int thirdColonIdx = logLine.indexOf(':', secondColonIdx + 1);
                        if (thirdColonIdx > -1) {
                            String lineNum = logLine.substring(secondColonIdx + 1, thirdColonIdx);
                            try {
                                line = Integer.valueOf(lineNum).intValue();
                            } catch(NumberFormatException nfe) { // ignore it
                                accessible = true;
                                 //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nfe);
                            }
                            if (lineLenght > thirdColonIdx) {
                                message = logLine.substring(thirdColonIdx + 1, lineLenght);
                            }
                        }
                    }
                }
            }
            // look for stacktrace links (e.g. at java.lang.Thread.run(Thread.java:595)
            //                                 at t.HyperlinkTest$1.run(HyperlinkTest.java:24))
            else if (logLine.startsWith("at ") && lineLenght > 3) { // NOI18N 
                error = true;
                int parenthIdx = logLine.indexOf('(');
                if (parenthIdx > -1) {
                    String classWithMethod = logLine.substring(3, parenthIdx);
                    int lastDotIdx = classWithMethod.lastIndexOf('.');
                    if (lastDotIdx > -1) {  
                        int lastParenthIdx = logLine.lastIndexOf(')');
                        int lastColonIdx = logLine.lastIndexOf(':');
                        if (lastParenthIdx > -1 && lastColonIdx > -1) {
                            String lineNum = logLine.substring(lastColonIdx + 1, lastParenthIdx);
                            try {
                                line = Integer.valueOf(lineNum).intValue();
                            } catch(NumberFormatException nfe) { // ignore it
                                 //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nfe);
                                error = true;
                            }
                            message = prevMessage;
                        }
                        int firstDolarIdx = classWithMethod.indexOf('$'); // > -1 for inner classes
                        String className = classWithMethod.substring(0, firstDolarIdx > -1 ? firstDolarIdx : lastDotIdx);
                        path = className.replace('.','/') + ".java"; // NOI18N              
                        accessible = globalPathReg.findResource(path) != null;
                        if (className.startsWith("org.apache.jsp.") && context != null) { // NOI18N
                            if (context != null) {
                                String contextPath = context.equals("/") 
                                                        ? "/_"     // hande ROOT context
                                                        : context;
                                path = appServerInstallDir + contextPath + "/" + path;
                                accessible = new File(path).exists();
                            }
                        }
                    }
                }
            }
            // every other message treat as normal info message
            else {
                prevMessage = logLine;
                // try to get context, if stored
                int stdContextIdx = logLine.indexOf(STANDARD_CONTEXT);
                int lBracketIdx = -1;
                if (stdContextIdx > -1) {
                    lBracketIdx = stdContextIdx + STANDARD_CONTEXT_LENGTH;
                }
                int rBracketIdx = logLine.indexOf(']');
                if (lBracketIdx > -1 && rBracketIdx > -1 && rBracketIdx > lBracketIdx) {
                    context = logLine.substring(lBracketIdx, rBracketIdx);
                }
            }
            return new LineInfo(path, line, message, error, accessible);
        }
    }
}
