/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.jsploader;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.NoSuchElementException;
import java.text.MessageFormat;

import javax.swing.text.*;

import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.actions.OpenAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.Task;
import org.openide.text.Line;
import org.openide.src.SourceElement;
import org.openide.compiler.Compiler;
import org.openide.compiler.ExternalCompiler;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.loaders.DataObjectNotFoundException;

import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.java.JavaEditor;
import org.netbeans.modules.java.JavaDataLoader;

/** Dataobject representing a servlet generated from a JSP page
*
* @author Petr Jiricka
*/
public class JspServletDataObject extends JavaDataObject {

    public static final String EA_ORIGIN_JSP_PAGE = "NetBeansAttrOriginJspPage"; // NOI18N

    private transient ServletEditorCookie servletEditor;
    
    /**
     * Helper object that handles execution bits for us. Lazy initialized from
     * {@link #getExecDebugCookie}.
     */
    private transient JspServletExecSupport jspServletExecSupport;
    
    private transient JspServletCompilerSupport compile;
    private transient JspServletCompilerSupport clean;
    private transient JspServletCompilerSupport build;
    

    /** New instance.
    * @param pf primary file object for this data object
    */
    public JspServletDataObject(FileObject pf, MultiFileLoader loader)
    throws DataObjectExistsException {
        super(pf, loader);
        init();
    }

    private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init();
    }

    public CookieSet getCookieSet0 () {
	return getCookieSet ();
    }

    public Node.Cookie getCookie(Class c) {
        if (c.isAssignableFrom(ServletEditorCookie.class)) {
            if (servletEditor == null) {
                EditorCookie ed = (EditorCookie)super.getCookie(EditorCookie.class);
                if (ed != null)
                    servletEditor = new ServletEditorCookie(ed, this);
            }
            if (servletEditor != null)
                return servletEditor;
        }
        if (c.isAssignableFrom(JspServletCompilerSupport.Build.class)) {
            return getCompilerCookie(c);
        } 
        if (c.isAssignableFrom(JspServletCompilerSupport.Compile.class)) {
            return getCompilerCookie(c);
        } 
        if (c.isAssignableFrom(JspServletCompilerSupport.Clean.class)) {
            return getCompilerCookie(c);
        } 
	// all execution-related services -> getExecSupport
        if (c.isAssignableFrom(JspServletExecSupport.class)) {
            return getExecDebugCookie();
        } 
        return super.getCookie(c);
    }

    private void init() {
    }

    private CompilerCookie getCompilerCookie(Class klass) {
        DataObject sourceJsp = getSourceJspPage();
        if (sourceJsp == null)
            return null;
        if (CompilerCookie.class.isAssignableFrom(klass)) {
            if (CompilerCookie.Build.class.isAssignableFrom(klass)) {
                if (build == null) {
                    build = new JspServletCompilerSupport.Build(sourceJsp);
                }
                return build;
            } else if (CompilerCookie.Clean.class.isAssignableFrom(klass)) {
                if (clean == null) {
                    clean = new JspServletCompilerSupport.Clean(sourceJsp);
                }
                return clean;
            } else {
                // ASSERT (klass == CompilerCookie.Compile.class)
                if (compile == null) {
                    compile = new JspServletCompilerSupport.Compile(sourceJsp);
                }
                return compile;
            }
        } 
        return null;
    }

    /**
     * Returns or creates the exec support.
     */
    public JspServletExecSupport getExecDebugCookie() {
        DataObject sourceJsp = getSourceJspPage();
        if (sourceJsp == null)
            return null;
        if (jspServletExecSupport == null) {
            jspServletExecSupport = new JspServletExecSupport(sourceJsp);
        }
        return jspServletExecSupport;
    }
    
    private void changeCookies() {
        compile = null;
        clean = null;
        build = null;
        jspServletExecSupport = null;
    }

    /** Get the name of the data object.
    * Uses the name of the source JSP
    * @return the name
    */
    public String getName () {
        DataObject jsp = getSourceJspPage();
        if (jsp == null)
            return super.getName();
        int markIndex = getPrimaryFile().getName().lastIndexOf(JspServletDataLoader.JSP_MARK);
        String fileIndex = (markIndex == -1) ? "" : getPrimaryFile().getName().substring(
                               markIndex + JspServletDataLoader.JSP_MARK.length());
        if (fileIndex.startsWith("_"))  // NOI18N
            fileIndex = fileIndex.substring(1);
        if ("".equals(fileIndex)) {
            return MessageFormat.format(NbBundle.getBundle(JspServletDataObject.class).
                getString("LBL_ServletDisplayNameNoNumber"), new Object[] {jsp.getPrimaryFile().getName()});
        }
        else {
            return MessageFormat.format(NbBundle.getBundle(JspServletDataObject.class).
                getString("LBL_ServletDisplayName"), new Object[] {fileIndex, jsp.getPrimaryFile().getName()});
        }
    }

    /** Help context for this object.
    * @return help context
    */
    public org.openide.util.HelpCtx getHelpCtx () {
        return org.openide.util.HelpCtx.DEFAULT_HELP;
    }
    
    /** Sets the source JSP page for this servlet */
    public void setSourceJspPage(DataObject jspPage) throws IOException {
        changeCookies();
        setSourceJspPage(getPrimaryFile(), jspPage);
        firePropertyChange(PROP_COOKIE, null, null);
    }
    
    public static void setSourceJspPage(FileObject generatedServlet, DataObject jspPage) throws IOException {
        generatedServlet.setAttribute(EA_ORIGIN_JSP_PAGE, jspPage.getPrimaryFile());
    }
    

    /** Returns the source JSP page for this servlet */
    public DataObject getSourceJspPage() {
    	Object obj = getPrimaryFile().getAttribute(EA_ORIGIN_JSP_PAGE);
    	if (obj instanceof DataObject) return (DataObject)obj;
    	if (obj instanceof FileObject) {
    		if (((FileObject)obj).isValid()) {
    			try {
    				return DataObject.find((FileObject)obj);
    			} catch (DataObjectNotFoundException e) {//nothing to do
    		    }
    		}
        }
        return null;
    }

    private static class ServletEditorCookie implements EditorCookie {
        
        private EditorCookie original;
        private JspServletDataObject servlet;
        
        public ServletEditorCookie(EditorCookie original, JspServletDataObject servlet) {
            this.original = original;
            this.servlet = servlet;
        }

        private EditorCookie currentEditorCookie() {
            DataObject jsp = servlet.getSourceJspPage();
            if ((jsp != null) && (jsp instanceof JspDataObject)) {
                if (((JspDataObject)jsp).getServletDataObject() == servlet) {
                    EditorCookie newCookie = ((JspDataObject)jsp).getServletEditor();
                    if (newCookie != null)
                        return newCookie;
                }
            }
            return original;
        }

        // implementation of EditorCookie
        public Line.Set getLineSet() {
            return currentEditorCookie().getLineSet();
        }

        public void open() {
            currentEditorCookie().open();
        }

        public boolean close() {
            return currentEditorCookie().close();
        }

        public Task prepareDocument() {
            return currentEditorCookie().prepareDocument();
        }

        public javax.swing.text.StyledDocument openDocument() throws java.io.IOException {
            return currentEditorCookie().openDocument();
        }

        public javax.swing.text.StyledDocument getDocument() {
            return currentEditorCookie().getDocument();
        }

        public void saveDocument() throws java.io.IOException {
            currentEditorCookie().saveDocument();
        }

        public boolean isModified() {
            return currentEditorCookie().isModified();
        }

        public javax.swing.JEditorPane[] getOpenedPanes() {
            return currentEditorCookie().getOpenedPanes();
        }

    }

}

