/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.testtools;

/*
 * XTestDataObject.java
 *
 * Created on May 2, 2002, 4:07 PM
 */

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.ServiceType;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;
import org.openide.execution.Executor;
import org.openide.compiler.CompilerType;
import org.openide.filesystems.FileObject;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.CompilerCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.ExecutionSupport;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;

import org.apache.tools.ant.module.api.AntProjectCookie;
import org.openide.loaders.CompilerSupport;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.w3c.dom.Element;

/** Data Object class representing XTest Workspace Build Script
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class XTestDataObject extends MultiDataObject implements PropertyChangeListener {

    static final long serialVersionUID = -8829236025053170557L;
    
    static final Image icon = Utilities.loadImage("org/netbeans/modules/testtools/XTestIcon.gif"); // NOI18N
    static final Image errIcon = Utilities.loadImage("org/netbeans/modules/testtools/XTestIconError.gif"); // NOI18N

    /** creates new XTestDataObject
     * @param pf FileObject
     * @param loader XTestDataLoader
     * @throws DataObjectExistsException when XTestDataObject already exists */    
    public XTestDataObject(FileObject pf, XTestDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        cookies.add(new XTestEditorSupport(this));
        FileObject prim = getPrimaryFile();
        AntProjectCookie proj = new XTestProjectSupport(prim);
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
/*
    public Node.Cookie getCookie(Class clazz) {
        if(clazz == DebuggerCookie.class) {
            return null;
        }
        return super.getCookie(clazz);
    }
*/    
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
           ((XTestProjectSupport) getCookie(XTestProjectSupport.class)).setFileObject(getPrimaryFile());
        }
    }
    
    /** Execution Support class for XTestDataObject */    
    public static class XTestExecSupport extends ExecutionSupport {
        /** creates new XTestExecSupport
         * @param entry MultiDataObject.Entry */        
        public XTestExecSupport (MultiDataObject.Entry entry) {
            super (entry);
        }
        
        /** returns default Executor
         * @return XTestExecutor */        
        protected Executor defaultExecutor () {
            ServiceType.Registry registry=(ServiceType.Registry)Lookup.getDefault().lookup(ServiceType.Registry.class);
            registry.services();
            Executor e=(Executor)registry.find(XTestExecutor.class);
            return e==null?super.defaultExecutor():e;
        }
    }
    
    /** Compiler Support class for XTestDataObject */    
    public static class XTestCompilerSupport extends CompilerSupport {

        /** creates new XTestCompilerSupport
         * @param entry MultiDataObject.Entry
         * @param cookie Cookie Class */        
        protected XTestCompilerSupport (MultiDataObject.Entry entry, Class cookie) {
            super (entry, cookie);
        }

        /** returns default Compiler Type for XTestDataObject
         * @return XTestCompilerType */        
        protected CompilerType defaultCompilerType () {
            ServiceType.Registry registry=(ServiceType.Registry)Lookup.getDefault().lookup(ServiceType.Registry.class);
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
    public static class XTestNode extends DataNode implements ChangeListener {
        /** creates new XTestNode for given DataObject
         * @param obj DataObject */        
        public XTestNode (DataObject obj) {
            super(obj, Children.LEAF);
            AntProjectCookie cookie=(AntProjectCookie)obj.getCookie(AntProjectCookie.class);
            cookie.addChangeListener(WeakListener.change(this, cookie));
        }

        public Image getIcon(int type) {
            AntProjectCookie.ParseStatus cookie = (AntProjectCookie.ParseStatus)getDataObject().getCookie(AntProjectCookie.ParseStatus.class);
            if (cookie.getFile() == null && cookie.getFileObject() == null) {
                return errIcon;
            }
            if (!cookie.isParsed()) {
                return icon;
            }
            Throwable exc = cookie.getParseException();
            if (exc != null) {
                return errIcon;
            } else {
                return icon;
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

        public String getShortDescription() {
            AntProjectCookie cookie = (AntProjectCookie)getDataObject().getCookie(AntProjectCookie.class);
            if (cookie.getFile() == null && cookie.getFileObject() == null) {
                // Script has been invalidated perhaps? Don't continue, we would
                // just get an NPE from the getParseException.
                return super.getShortDescription();
            }
            Throwable exc = cookie.getParseException();
            if (exc != null) {
                String m = exc.getLocalizedMessage();
                if (m != null) {
                     return m;
                } else {
                    return exc.toString();
                }
            } else {
                Element pel = cookie.getProjectElement();
                if (pel != null) {
                    String projectName = pel.getAttribute("name"); // NOI18N
                    if (!projectName.equals("")) { // NOI18N
                        // Set the node description in the IDE to the name of the project
                        return NbBundle.getMessage(XTestDataObject.class, "LBL_named_script_description", projectName);
                    } else {
                        // No name specified, OK.
                        return NbBundle.getMessage(XTestDataObject.class, "LBL_anon_script_description");
                    }
                } else {
                    // ???
                    return super.getShortDescription();
                }
            }
        }

        protected Sheet createSheet() {  
            Sheet sheet = super.createSheet();

/*
            // Make sure there is a "Properties" set: // NOI18N
            Sheet.Set props = sheet.get(Sheet.PROPERTIES); // get by name, not display name
            if (props == null)  {
                props = Sheet.createPropertiesSet ();
                sheet.put(props);
            }
            add2Sheet (props);
*/
            Sheet.Set exec = new Sheet.Set ();
            exec.setName ("execution"); // NOI18N
            exec.setDisplayName (NbBundle.getMessage (XTestDataObject.class, "LBL_execution"));
            exec.setShortDescription (NbBundle.getMessage (XTestDataObject.class, "HINT_execution"));
            CompilerSupport csupp = (CompilerSupport) getCookie (CompilerSupport.class);
            if (csupp != null) csupp.addProperties (exec);
            ExecutionSupport xsupp = (ExecutionSupport) getCookie (ExecutionSupport.class);
            if (xsupp != null) xsupp.addProperties (exec);
            exec.remove (ExecutionSupport.PROP_FILE_PARAMS);
            if (csupp != null || xsupp != null) {
                sheet.put (exec);
            }
            return sheet;
        }

        public void stateChanged (ChangeEvent ev) {
            fireIconChange();
            fireOpenedIconChange();
            fireShortDescriptionChange(null, null);
            fireCookieChange();
            firePropertyChange (null, null, null);
        }

        /** Returns true if the Antscript represented by the passed cookie is read-only. */
        public static boolean isScriptReadOnly(AntProjectCookie cookie) {
            if (cookie != null) {
                if (cookie.getFileObject() != null) {
                    return cookie.getFileObject().isReadOnly();
                } else if (cookie.getFile() != null) {
                    return ! cookie.getFile().canWrite();
                }
            }
            return true;
        }
    }
    
}
