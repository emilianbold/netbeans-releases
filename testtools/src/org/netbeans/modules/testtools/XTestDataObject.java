/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools;

/*
 * XTestDataObject.java
 *
 * Created on May 2, 2002, 4:07 PM
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openide.cookies.DebuggerCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.nodes.AntProjectNode;
import org.apache.tools.ant.module.xml.AntProjectSupport;
import org.apache.tools.ant.module.loader.AntCompilerSupport;
import org.openide.execution.Executor;
import org.openide.compiler.CompilerType;
import org.openide.cookies.CompilerCookie;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class XTestDataObject extends MultiDataObject implements PropertyChangeListener {

    public XTestDataObject(FileObject pf, XTestDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        cookies.add(new XTestEditorSupport(this));
        FileObject prim = getPrimaryFile();
        AntProjectCookie proj = new AntProjectSupport(prim);
        cookies.add(proj);
        if(proj.getFile() != null) {
            MultiDataObject.Entry pe = getPrimaryEntry();
            cookies.add(new Compile(pe));
            cookies.add(new Build(pe));
            cookies.add(new Clean(pe));
            cookies.add(new CleanResults(pe));
            cookies.add(new XTestExecSupport(pe));
        }
        addPropertyChangeListener(this);
    }
    
    public Node.Cookie getCookie(Class clazz) {
        if(clazz == DebuggerCookie.class) {
            return null;
        }
        return super.getCookie(clazz);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(XTestDataObject.class);
    }

    protected Node createNodeDelegate() {
        return new AntProjectNode(this);
    }

    /** Helper method. Adds save cookie to the data object. */
    public void addSaveCookie(SaveCookie saveCookie) {
        if(getCookie(SaveCookie.class) == null) {
            getCookieSet().add(saveCookie);
            setModified(true);
        }
    }

   /** Helper method. Removes save cookie from the data object. */
    public void removeSaveCookie(SaveCookie saveCookie) {
        Node.Cookie cookie = getCookie(SaveCookie.class);

        if(cookie != null && cookie.equals(saveCookie)) {
            getCookieSet().remove(saveCookie);
            setModified(false);
        }
    }

    public void propertyChange(PropertyChangeEvent ev) {
        String prop = ev.getPropertyName();
        if(prop == null || prop.equals(DataObject.PROP_PRIMARY_FILE)) {
           ((AntProjectSupport) getCookie(AntProjectSupport.class)).setFileObject(getPrimaryFile());
        }
    }
    
    public static class XTestExecSupport extends ExecSupport {
        public XTestExecSupport (MultiDataObject.Entry entry) {
            super (entry);
        }
        
        protected Executor defaultExecutor () {
            return new XTestExecutor();
        }
    }
    
    public static class XTestCompilerSupport extends AntCompilerSupport {

        protected XTestCompilerSupport (MultiDataObject.Entry entry, Class cookie) {
            super (entry, cookie);
        }

        protected CompilerType defaultCompilerType () {
            return new XTestCompilerType();
        }
    }
    
    public static class Compile extends XTestCompilerSupport implements CompilerCookie.Compile {
        public Compile(MultiDataObject.Entry entry) {
            super(entry, CompilerCookie.Compile.class);
        }
    }
    
    public static class Build extends XTestCompilerSupport implements CompilerCookie.Build {
        public Build (MultiDataObject.Entry entry) {
            super(entry, CompilerCookie.Build.class);
        }
    }
    
    public static class Clean extends XTestCompilerSupport implements CompilerCookie.Clean {
        public Clean(MultiDataObject.Entry entry) {
            super(entry, CompilerCookie.Clean.class);
        }
    }
    
    public static class CleanResults extends XTestCompilerSupport {
        public CleanResults(MultiDataObject.Entry entry) {
            super(entry, CleanResults.class);
        }
    }
}
