/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.FunctionBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.LineBreakpoint;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/*
 *
 * @author Jan Jancura and Gordon Prieur
 */
public class EditorContextBridge {
    
    public static final String FUNCTION = "function"; // NOI18N
    public static final String LINE = "line"; // NOI18N
    
    private static EditorContext context;
    
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
    
    public static boolean showSource(CallStackFrame csf) {
        String fullname = csf.getFullname();
        
        if (fullname != null) {
            File file = new File(fullname);
	    if (file.exists()) {
		FileObject fo = FileUtil.toFileObject(file);
		String url;
		try {
		    URL tmpurl = fo.getURL();
		    url = fo.getURL().toExternalForm();
		} catch (FileStateInvalidException ex) {
		    if (Utilities.isWindows()) {
			url = "file:/" + fo.getPath().replace(" ", "%20"); // NOI18N
		    } else {
			url = "file:/" + fo.getPath(); // NOI18N
		    }
		}
		return getContext().showSource(url, csf.getLineNumber(), null);
	    }
        }
	return false;
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
		FileObject fo = FileUtil.toFileObject(file);
		String url;
		try {
		    url = fo.getURL().toExternalForm();
		} catch (FileStateInvalidException ex) {
		    /* Best guesses */
		    if (Utilities.isWindows()) {
			url = "file:/" + fo.getPath().replace(" ", "%20"); // NOI18N
		    } else {
			url = "file:" + fo.getPath(); // NOI18N
		    }
		}
//		System.err.println("ECB.annotate[Set]:     " + fullname + " [" + csf.getLineNumber() + ", " + annotationType + "]");
		return getContext().annotate(url, csf.getLineNumber(), annotationType, null);
	    }
//	    else {
//		System.err.println("ECB.annotate[Ignored]: " + fullname + " [" + csf.getLineNumber() + ", " + annotationType + "]");
//	    }
//        } else {
//	    System.err.println("fullname:");
	}
	return null;
    }
    
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

    public static String getFileName(LineBreakpoint b) { 
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

    
    public static boolean showSource(LineBreakpoint b, Object timeStamp) {
        if (b.getLineNumber() < 1) {
            return EditorContextBridge.showSource(b.getURL(), 1, timeStamp);
        }
        return EditorContextBridge.showSource(b.getURL(), b.getLineNumber(), timeStamp);
    }

    public static String getDefaultType() {
        return LINE;
    }

    public static Object annotate(LineBreakpoint b) {
        String url = b.getURL();
        int lineNumber = b.getLineNumber();
        if (lineNumber < 1) {
            return null;
        }
        String condition = b.getCondition();
        boolean isConditional = (condition != null) && !condition.trim().equals(""); // NOI18N
        String annotationType = b.isEnabled() ?
            (isConditional ? EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.BREAKPOINT_ANNOTATION_TYPE) :
            (isConditional ? EditorContext.DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.DISABLED_BREAKPOINT_ANNOTATION_TYPE);

        return annotate(url, lineNumber, annotationType, null);
    }

    public static Object annotate(FunctionBreakpoint b) {
        String url = b.getURL();
        int lineNumber = b.getLineNumber();
        if (lineNumber < 1) {
            return null;
        }
        String condition = b.getCondition();
        boolean isConditional = (condition != null) && !condition.trim().equals(""); // NOI18N
        String annotationType = b.isEnabled() ?
            (isConditional ? EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.BREAKPOINT_ANNOTATION_TYPE) :
            (isConditional ? EditorContext.DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.DISABLED_BREAKPOINT_ANNOTATION_TYPE);

        return annotate(url, lineNumber, annotationType, null);
    }

    public static String getRelativePath(String className) {
        String sourceName = className.replace('.', '/') + ".java"; // NOI18N
        return sourceName;
    }
    
    private static String convertSlash(String original) {
        return original.replace(File.separatorChar, '/');
    }
    
    

    // innerclasses ............................................................
    
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
        
        public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
            cp1.addPropertyChangeListener(propertyName, l);
            cp2.addPropertyChangeListener(propertyName, l);
        }
        
        public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
            cp1.removePropertyChangeListener(propertyName, l);
            cp2.removePropertyChangeListener(propertyName, l);
        }
    }
    
    private static class CompoundAnnotation {
        public CompoundAnnotation() {}
        
        Object annotation1;
        Object annotation2;
    }

}
