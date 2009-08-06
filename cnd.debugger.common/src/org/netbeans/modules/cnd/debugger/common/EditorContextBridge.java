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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.debugger.common;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.common.breakpoints.AddressBreakpoint;
import org.netbeans.modules.cnd.debugger.common.breakpoints.CndBreakpoint;
import org.netbeans.modules.cnd.debugger.common.breakpoints.FunctionBreakpoint;
import org.netbeans.modules.cnd.debugger.common.breakpoints.LineBreakpoint;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Utilities;

/*
 *
 * @author Jan Jancura and Gordon Prieur
 */
public class EditorContextBridge {
    
    public static final String FUNCTION = "function"; // NOI18N
    public static final String LINE = "line"; // NOI18N
    
    private static EditorContext context;
    //private static Logger log = Logger.getLogger("gdb.logger"); // NOI18N
    
    public static EditorContext getContext() {
        if (context == null) {
            List l = DebuggerManager.getDebuggerManager().lookup(null, EditorContext.class);
            context = (EditorContext) l.get(0);
            int i, k = l.size();
            for (i = 1; i < k; i++) {
                context = new CompoundContextProvider((EditorContext) l.get(i), context);
            }
        }
        return context;
    }
    
    public static boolean showSource(String url, int lineNumber, Object timeStamp) {
        return getContext().showSource(url, lineNumber, timeStamp);
    }
    
    public static boolean showSource(CallStackFrame csf, boolean inDis) {
        if (inDis) {
            //return showDis(csf);
            return false;
        } else {
            return showCode(csf);
        }
    }
    
    public static boolean showCode(CallStackFrame csf) {
        String fullname = csf.getFullname();
        
        if (fullname != null) {
            File file = new File(fullname);
	    if (file.exists()) {
                FileObject fo = FileUtil.toFileObject(CndFileUtils.normalizeFile(file));
                if (fo != null) {
                    try {
                        return getContext().showSource(DataObject.find(fo), csf.getLineNumber(), null);
                    } catch (DataObjectNotFoundException dex) {
                        // do nothing
                    }
                }
	    }
        }
	return false;
    }

    // FIXME
//    public static boolean showDis(CallStackFrame csf) {
//        Disassembly dis = Disassembly.getCurrent();
//        if (dis == null) {
//            return false;
//        }
//        int line = dis.getAddressLine(csf.getAddr());
//        if (line != -1) {
//            FileObject fo = Disassembly.getFileObject();
//            if (fo != null) {
//                try {
//                    return getContext().showSource(DataObject.find(fo), line, null);
//                } catch (DataObjectNotFoundException dex) {
//                    // do nothing
//                }
//            }
//        } else {
//            Disassembly.open();
//        }
//        return false;
//    }
    
    public static String getUrl(File file) {
        try {
            file = file.getCanonicalFile();
        } catch (IOException ioe) {
        }
        FileObject fo = FileUtil.toFileObject(file);
        String url;
        try {
            url = fo.getURL().toExternalForm();
        } catch (FileStateInvalidException ex) {
            if (Utilities.isWindows()) {
                url = "file:/" + fo.getPath().replace(" ", "%20"); // NOI18N
            } else {
                url = "file:/" + fo.getPath(); // NOI18N
            }
        }
        return url;
    }
    
    /**
     * Creates a new time stamp.
     *
     * @param timeStamp a new time stamp
     */
    public static void createTimeStamp(Object timeStamp) {
        getContext().createTimeStamp(timeStamp);
    }

    /**
     * Disposes given time stamp.
     *
     * @param timeStamp a time stamp to be disposed
     */
    public static void disposeTimeStamp(Object timeStamp) {
        getContext().disposeTimeStamp(timeStamp);
    }

    /**
     * Adds annotation to given url on given line.
     *
     * @param url a url of source annotation should be set into
     * @param lineNumber a number of line annotation should be set into
     * @param annotationType a type of annotation to be set
     *
     * @return annotation
     */    
    public static Object annotate(String url, int lineNumber, String annotationType, Object timeStamp) {
        return getContext().annotate(url, lineNumber, annotationType, timeStamp);
    }
    
    public static Object annotate(DataObject dobj, int lineNumber, String annotationType, Object timeStamp) {
        return getContext().annotate(dobj, lineNumber, annotationType, timeStamp);
    }

    /**
     * Adds annotation to given url on given line.
     *
     * @param csf The current CallStackFrame
     * @return annotation
     */    
    public static Object annotate(CallStackFrame csf, String annotationType) {
        String fullname = csf.getFullname();
        if (fullname != null) {
            File file = new File(fullname);
	    if (file.exists()) {
                FileObject fo = FileUtil.toFileObject(CndFileUtils.normalizeFile(file));
                if (fo != null) {
                    try {
                        return getContext().annotate(DataObject.find(fo), csf.getLineNumber(), annotationType, null);
                    } catch (DataObjectNotFoundException dex) {
                        // do nothing
                    }
                }
	    }
	}
	return null;
    }
    
    // FIXME
//    public static Object annotateDis(CallStackFrame csf, String annotationType) {
//        Disassembly dis = Disassembly.getCurrent();
//        if (dis == null) {
//            return null;
//        }
//        int line = dis.getAddressLine(csf.getAddr());
//        if (line != -1) {
//            FileObject fo = Disassembly.getFileObject();
//            if (fo != null) {
//                try {
//                    return getContext().annotate(DataObject.find(fo), line, annotationType, null);
//                } catch (DataObjectNotFoundException dex) {
//                    // do nothing
//                }
//            }
//        }
//        return null;
//    }
    
    /**
     * Removes given annotation.
     */
    public static void removeAnnotation(Object annotation) {
        getContext().removeAnnotation(annotation);
    }
    
    /**
     * Returns line number given annotation is associated with.
     *
     * @return line number given annotation is associated with
     */
    public static int getLineNumber(Object annotation, Object timeStamp) {
        return getContext().getLineNumber(annotation, timeStamp);
    }
    
    /**
     * Returns number of line currently selected in editor or <code>null</code>.
     *
     * @return number of line currently selected in editor or <code>0</code>
     */
    public static int getCurrentLineNumber() {
        return getContext().getCurrentLineNumber();
    }
    
    /**
     * Returns number of line currently selected in editor or <code>null</code>.
     *
     * @return number of line currently selected in editor or <code>0</code>
     */
    public static int getMostRecentLineNumber() {
        return getContext().getMostRecentLineNumber();
    }

    /**
     * Returns URL of source currently selected in editor or <code>null</code>.
     *
     * @return URL of source currently selected in editor or <code>null</code>
     */
    public static String getCurrentURL() {
        return getContext().getCurrentURL();
    }
    
    /**
     *  Return the most recent URL or empty string. The difference between this and getCurrentURL()
     *  is that this one will return a URL when the editor has lost focus.
     *
     *  @return url in string form
     */
    public static String getMostRecentURL() {
	return getContext().getMostRecentURL();
    }

    /**
     * Returns name of method currently selected in editor or <code>null</code>.
     *
     * @return name of method currently selected in editor or <code>null</code>
     */
    public static String getCurrentFunctionName() {
        return getContext().getCurrentFunctionName();
    }

    /**
     * Returns method name currently selected in editor or <code>null</code>.
     *
     * @return method name currently selected in editor or <code>null</code>
     */
    public static String getSelectedFunctionName() {
        return getContext().getSelectedFunctionName();
    }
    
    /**
     * Returns line number of given field in given class.
     *
     * @param url the url of file the class is deined in
     * @param className the name of class (or innerclass) the field is 
     *                  defined in
     * @param fieldName the name of field
     *
     * @return line number or -1
     */
    public static int getFieldLineNumber(String url, String className, String fieldName) {
        return getContext().getFieldLineNumber(url, className, fieldName);
    }
        
    /**
     * Get the MIME type of the current file.
     *
     * @return The MIME type of the current file
     */
    public String getCurrentMIMEType() {
        return getContext().getCurrentMIMEType();
    }
    
    /**
     * Get the MIME type of the most recently selected file.
     *
     * @return The MIME type of the most recent selected file
     */
    public static String getMostRecentMIMEType() {
	return getContext().getMostRecentMIMEType();
    }
        
    /**
     * Adds a property change listener.
     *
     * @param l the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        getContext().addPropertyChangeListener(l);
    }

    public static void removePropertyChangeListener(PropertyChangeListener l) {
        getContext().removePropertyChangeListener(l);
    } 
    
    
    // utility methods .........................................................

    public static String getFileName(CndBreakpoint b) {
        try {
            return basename(new File(new URL(b.getURL()).getFile()).getName());
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
    private static String basename(String name) {
	int idx = name.lastIndexOf('/');
	
	if (idx > 0) {
	    return name.substring(idx);
	} else {
	    return name;
	}
    }

    
    public static boolean showSource(CndBreakpoint b, Object timeStamp) {
        if (b instanceof LineBreakpoint) {
            if (b.getLineNumber() < 1) {
                return EditorContextBridge.showSource(b.getURL(), 1, timeStamp);
            }
            return EditorContextBridge.showSource(b.getURL(), b.getLineNumber(), timeStamp);
        } else if (b instanceof AddressBreakpoint) {
            // FIXME
//            FileObject fo = Disassembly.getFileObject();
//            if (fo != null) {
//                try {
//                    Disassembly dis = Disassembly.getCurrent();
//                    if (dis != null) {
//                        int line = dis.getAddressLine(((AddressBreakpoint)b).getAddress());
//                        if (line != -1) {
//                            return getContext().showSource(DataObject.find(fo), dis.getAddressLine(((AddressBreakpoint)b).getAddress()), null);
//                        } else {
//                            Disassembly.open();
//                        }
//                    }
//                } catch (DataObjectNotFoundException dex) {
//                    // do nothing
//                }
//            }
        }
        return false;
    }

    public static String getDefaultType() {
        return LINE;
    }

    public static Object annotate(CndBreakpoint b) {
        String url = b.getURL();
        int lineNumber = b.getLineNumber();
        if (lineNumber < 1) {
            return null;
        }
        String condition = b.getCondition();
        boolean isConditional = condition.trim().length() > 0;
        String annotationType;
        if (b instanceof FunctionBreakpoint) {
            if (b.isEnabled()) {
                annotationType = isConditional ? EditorContext.CONDITIONAL_FUNCTION_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.FUNCTION_BREAKPOINT_ANNOTATION_TYPE;
            } else {
                annotationType =  isConditional ? EditorContext.DISABLED_CONDITIONAL_FUNCTION_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.DISABLED_FUNCTION_BREAKPOINT_ANNOTATION_TYPE;
            }
        } else if (b instanceof AddressBreakpoint) {
            // FIXME
//            Disassembly dis = Disassembly.getCurrent();
//            if (dis == null) {
//                return null;
//            }
//            lineNumber = dis.getAddressLine(((AddressBreakpoint)b).getAddress());
//            if (lineNumber == -1) {
//                return null;
//            }
//            if (b.isEnabled()) {
//                annotationType = isConditional ? EditorContext.CONDITIONAL_ADDRESS_BREAKPOINT_ANNOTATION_TYPE :
//                             EditorContext.ADDRESS_BREAKPOINT_ANNOTATION_TYPE;
//            } else {
//                annotationType = isConditional ? EditorContext.DISABLED_CONDITIONAL_ADDRESS_BREAKPOINT_ANNOTATION_TYPE :
//                             EditorContext.DISABLED_ADDRESS_BREAKPOINT_ANNOTATION_TYPE;
//            }
//            try {
//                return annotate(DataObject.find(Disassembly.getFileObject()), lineNumber, annotationType, b);
//            } catch (DataObjectNotFoundException doe) {
//                doe.printStackTrace();
//                return null;
//            }
            return null;
        } else {
            if (b.isEnabled()) {
                annotationType = isConditional ? EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.BREAKPOINT_ANNOTATION_TYPE;
            } else {
                annotationType = isConditional ? EditorContext.DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.DISABLED_BREAKPOINT_ANNOTATION_TYPE;
            }
        }

        return annotate(url, lineNumber, annotationType, b);
    }

    public static String getRelativePath(String className) {
        return className.replace('.', '/') + ".java"; // NOI18N
    }
    
    private static String convertSlash(String original) {
        return original.replace(File.separatorChar, '/');
    }
    

    private static class CompoundContextProvider extends EditorContext {

        private EditorContext cp1, cp2;
        
        CompoundContextProvider(EditorContext cp1, EditorContext cp2) {
            this.cp1 = cp1;
            this.cp2 = cp2;
        }

        public void createTimeStamp(Object timeStamp) {
            cp1.createTimeStamp(timeStamp);
            cp2.createTimeStamp(timeStamp);
        }

        public void disposeTimeStamp(Object timeStamp) {
            cp1.disposeTimeStamp(timeStamp);
            cp2.disposeTimeStamp(timeStamp);
        }
        
        public void updateTimeStamp(Object timeStamp, String url) {
            cp1.updateTimeStamp(timeStamp, url);
            cp2.updateTimeStamp(timeStamp, url);
        }

        public String getCurrentURL() {
            String s = cp1.getCurrentURL();
            if (s.trim().length() < 1) {
                return cp2.getCurrentURL();
            }
            return s;
        }

        public String getMostRecentURL() {
            String s = cp1.getMostRecentURL();
            if (s.trim().length() < 1) {
                return cp2.getMostRecentURL();
            }
            return s;
        }
        
        public int getCurrentLineNumber() {
            int i = cp1.getCurrentLineNumber();
            if (i < 1) {
                return cp2.getCurrentLineNumber();
            }
            return i;
        }
        
        public int getMostRecentLineNumber() {
            int i = cp1.getMostRecentLineNumber();
            if (i < 1) {
                return cp2.getMostRecentLineNumber();
            }
            return i;
        }
        
        public String getCurrentFunctionName() {
            String s = cp1.getCurrentFunctionName();
            if ( (s == null) || (s.trim().length() < 1)) {
                return cp2.getCurrentFunctionName();
            }
            return s;
        }

        public DataObject getCurrentDataObject() {
            DataObject dobj = cp1.getCurrentDataObject();
            if (dobj == null) {
                dobj = cp2.getCurrentDataObject();
            }
            return dobj;
        }

        public DataObject getMostRecentDataObject() {
            DataObject dobj = cp1.getMostRecentDataObject();
            if (dobj == null) {
                dobj = cp2.getMostRecentDataObject();
            }
            return dobj;
        }

        public FileObject getMostRecentFileObject() {
            FileObject fobj = cp1.getMostRecentFileObject();
            if (fobj == null) {
                fobj = cp2.getMostRecentFileObject();
            }
            return fobj;
        }

        public FileObject getCurrentFileObject() {
            FileObject fo = cp1.getCurrentFileObject();
            if (fo == null) {
                fo = cp2.getCurrentFileObject();
            }
            return fo;
        }

        public String getSelectedFunctionName () {
            String s = cp1.getSelectedFunctionName ();
            if ( (s == null) || (s.trim ().length () < 1)) {
                return cp2.getSelectedFunctionName ();
            }
            return s;
        }
        
        public void removeAnnotation(Object annotation) {
            CompoundAnnotation ca = (CompoundAnnotation) annotation;
            cp1.removeAnnotation(ca.annotation1);
            cp2.removeAnnotation(ca.annotation2);
        }

        public Object annotate(String sourceName, int lineNumber, String annotationType, Object timeStamp) {
            CompoundAnnotation ca = new CompoundAnnotation();
            ca.annotation1 = cp1.annotate(sourceName, lineNumber, annotationType, timeStamp);
            ca.annotation2 = cp2.annotate(sourceName, lineNumber, annotationType, timeStamp);
            return ca;
        }
        
        public Object annotate(DataObject dobj, int lineNumber, String annotationType, Object timeStamp) {
            CompoundAnnotation ca = new CompoundAnnotation();
            ca.annotation1 = cp1.annotate(dobj, lineNumber, annotationType, timeStamp);
            ca.annotation2 = cp2.annotate(dobj, lineNumber, annotationType, timeStamp);
            return ca;
        }

        public int getLineNumber(Object annotation, Object timeStamp) {
            CompoundAnnotation ca = new CompoundAnnotation();
            int ln = cp1.getLineNumber(ca.annotation1, timeStamp);
            if (ln >= 0) {
                return ln;
            }
            return cp2.getLineNumber(ca.annotation2, timeStamp);
        }

        public boolean showSource(String sourceName, int lineNumber, Object timeStamp) {
            return cp1.showSource(sourceName, lineNumber, timeStamp) |
                   cp2.showSource(sourceName, lineNumber, timeStamp);
        }
        
        public boolean showSource(DataObject dobj, int lineNumber, Object timeStamp) {
            return cp1.showSource(dobj, lineNumber, timeStamp) |
                   cp2.showSource(dobj, lineNumber, timeStamp);
        }
    
        public int getFieldLineNumber(String url, String className, String fieldName) {
            int ln = cp1.getFieldLineNumber(url, className, fieldName);
            if (ln != -1) {
                return ln;
            }
            return cp2.getFieldLineNumber(url, className, fieldName);
        }
        
        public String getCurrentMIMEType() {
            String s = cp1.getCurrentMIMEType();
            if (s == null) {
                return cp2.getCurrentMIMEType();
            }
            return s;            
        }
        
        public String getMostRecentMIMEType() {
            String s = cp1.getMostRecentMIMEType();
            if (s == null) {
                return cp2.getMostRecentMIMEType();
            }
            return s;            
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
            cp1.addPropertyChangeListener(l);
            cp2.addPropertyChangeListener(l);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
            cp1.removePropertyChangeListener(l);
            cp2.removePropertyChangeListener(l);
        }
        
    }
    
    private static class CompoundAnnotation {
        Object annotation1;
        Object annotation2;
    }
}
