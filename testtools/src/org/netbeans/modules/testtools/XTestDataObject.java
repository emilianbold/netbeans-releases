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

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openide.TopManager;
import org.openide.ServiceType;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;
import org.openide.execution.Executor;
import org.openide.compiler.CompilerType;
import org.openide.filesystems.FileObject;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.DebuggerCookie;
import org.openide.cookies.CompilerCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.ExecSupport;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;

import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.nodes.AntProjectNode;
import org.apache.tools.ant.module.xml.AntProjectSupport;
import org.apache.tools.ant.module.loader.AntCompilerSupport;

/** Data Object class representing XTest Workspace Build Script
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class XTestDataObject extends MultiDataObject implements PropertyChangeListener {

    static final long serialVersionUID = -8829236025053170557L;

    /** creates new XTestDataObject
     * @param pf FileObject
     * @param loader XTestDataLoader
     * @throws DataObjectExistsException when XTestDataObject already exists */    
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
    
    /** returns requested cookie except DebuggerCookie
     * @return Node.Cookie
     * @param clazz Cookie Class */    
    public Node.Cookie getCookie(Class clazz) {
        if(clazz == DebuggerCookie.class) {
            return null;
        }
        return super.getCookie(clazz);
    }
    
    /** returns Help Context
     * @return HelpCtx */    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(XTestDataObject.class);
    }

    /** creates Node delagate of XTestDataObject
     * @return XTestNode */    
    protected Node createNodeDelegate() {
        return new XTestNode(this);
    }

    void addSaveCookie(SaveCookie saveCookie) {
        if(getCookie(SaveCookie.class) == null) {
            getCookieSet().add(saveCookie);
            setModified(true);
        }
    }

    void removeSaveCookie(SaveCookie saveCookie) {
        Node.Cookie cookie = getCookie(SaveCookie.class);
        if(cookie != null && cookie.equals(saveCookie)) {
            getCookieSet().remove(saveCookie);
            setModified(false);
        }
    }

    /** handles change of some property
     * @param ev PropertyChangeEvent */    
    public void propertyChange(PropertyChangeEvent ev) {
        String prop = ev.getPropertyName();
        if(prop == null || prop.equals(DataObject.PROP_PRIMARY_FILE)) {
           ((AntProjectSupport) getCookie(AntProjectSupport.class)).setFileObject(getPrimaryFile());
        }
    }
    
    /** Execution Support class for XTestDataObject */    
    public static class XTestExecSupport extends ExecSupport {
        /** creates new XTestExecSupport
         * @param entry MultiDataObject.Entry */        
        public XTestExecSupport (MultiDataObject.Entry entry) {
            super (entry);
        }
        
        /** returns default Executor
         * @return XTestExecutor */        
        protected Executor defaultExecutor () {
            ServiceType.Registry registry=TopManager.getDefault().getServices();
            registry.services();
            Executor e=(Executor)registry.find(XTestExecutor.class);
            return e==null?super.defaultExecutor():e;
        }
    }
    
    /** Compiler Support class for XTestDataObject */    
    public static class XTestCompilerSupport extends AntCompilerSupport {

        /** creates new XTestCompilerSupport
         * @param entry MultiDataObject.Entry
         * @param cookie Cookie Class */        
        protected XTestCompilerSupport (MultiDataObject.Entry entry, Class cookie) {
            super (entry, cookie);
        }

        /** returns default Compiler Type for XTestDataObject
         * @return XTestCompilerType */        
        protected CompilerType defaultCompilerType () {
            ServiceType.Registry registry=TopManager.getDefault().getServices();
            registry.services();
            CompilerType c=(CompilerType)registry.find(XTestCompilerType.class);
            return c==null?super.defaultCompilerType():c;
        }
    }
    
    /** CompilerCookie.Compile implemntation class for XTestDataObject */    
    public static class Compile extends XTestCompilerSupport implements CompilerCookie.Compile {
        /** creates new Compile
         * @param entry MultiDataObject.Entry */        
        public Compile(MultiDataObject.Entry entry) {
            super(entry, CompilerCookie.Compile.class);
        }
    }
    
    /** CompilerCookie.Build implemntation class for XTestDataObject */    
    public static class Build extends XTestCompilerSupport implements CompilerCookie.Build {
        /** creates new Build
         * @param entry MultiDataObject.Entry */        
        public Build (MultiDataObject.Entry entry) {
            super(entry, CompilerCookie.Build.class);
        }
    }
    
    /** CompilerCookie.Build implemntation class for XTestDataObject */    
    public static class Clean extends XTestCompilerSupport implements CompilerCookie.Clean {
        /** creates new Clean
         * @param entry MultiDataObject.Entry */        
        public Clean(MultiDataObject.Entry entry) {
            super(entry, CompilerCookie.Clean.class);
        }
    }
    
    /** CleanResults Cookie class for XTestDataObject */    
    public static class CleanResults extends XTestCompilerSupport {
        /** creates new CleanResults
         * @param entry MultiDataObject.Entry */        
        public CleanResults(MultiDataObject.Entry entry) {
            super(entry, CleanResults.class);
        }
    }
    
    /** Node class representing XTEstDataObject */    
    public static class XTestNode extends AntProjectNode {
        /** creates new XTestNode for given DataObject
         * @param obj DataObject */        
            public XTestNode (DataObject obj) {
                super(obj);
            }
            /** changes Icon of XTestNode
             * @param base String icon base */            
            public void setIconBase(String base) {
                if (base.indexOf("Error")>=0) 
                    super.setIconBase("org/netbeans/modules/testtools/XTestIconError");
                else
                    super.setIconBase("org/netbeans/modules/testtools/XTestIcon");
            }

        public Image getIcon(int type) {
            AntProjectCookie.ParseStatus cookie = (AntProjectCookie.ParseStatus)getDataObject().getCookie(AntProjectCookie.ParseStatus.class);
            if (cookie.getFile() == null && cookie.getFileObject() == null) {
                // Script has been invalidated perhaps? Don't continue, we would
                // just get an NPE from the getParseException.
                return Utilities.loadImage("org/netbeans/modules/testtools/XTestIconError.gif"); // NOI18N
            }
            if (!cookie.isParsed()) {
                // Assume for now it is not erroneous.
                return Utilities.loadImage("org/netbeans/modules/testtools/XTestIcon.gif"); // NOI18N
            }
            Throwable exc = cookie.getParseException();
            if (exc != null) {
                return Utilities.loadImage("org/netbeans/modules/testtools/XTestIconError.gif"); // NOI18N
            } else {
                return Utilities.loadImage("org/netbeans/modules/testtools/XTestIcon.gif"); // NOI18N
            }
        }

        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
            
            /** returns Help Context
             * @return HelpCtx */    
            public HelpCtx getHelpCtx() {
                return new HelpCtx(XTestNode.class);
            }
    }
    
}
