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

import java.util.*;
import java.io.IOException;
import java.net.URL;
import java.awt.BorderLayout;

import org.openide.TopManager;
import org.openide.actions.*;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.ViewCookie;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.execution.NbClassPath;
import org.openide.text.EditorSupport;
import org.openide.loaders.OpenSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableTopComponent;

//import org.netbeans.modules.web.core.WebExecSupport;
import org.netbeans.modules.web.core.QueryStringCookie;
//import org.netbeans.modules.web.core.execution.Execute2Action;

/**
* Loader for JSPs.
*
* @author Petr Jiricka
*/
public class JspLoader extends UniFileLoader {

    /** serialVersionUID */
    private static final long serialVersionUID = 1549250022027438942L;

    public static final String JSP_EXTENSION = "jsp"; // NOI18N
    public static final String JSP_MIME_TYPE  = "text/x-jsp"; // NOI18N

    protected void initialize () {
        super.initialize();
        ExtensionList ext = new ExtensionList();
        ext.addExtension(JSP_EXTENSION);
        setExtensions(ext);

    }

    /** Get the default display name of this loader.
     * @return default display name
     */
    protected String defaultDisplayName () {
        return NbBundle.getBundle(JspLoader.class).getString("PROP_JspLoader_Name");
    }
    
    /** Get default actions.
     * @return array of default system actions.
     */
    protected SystemAction[] defaultActions () {
        return new SystemAction[] {
                        SystemAction.get (OpenAction.class),
                        SystemAction.get (EditServletAction.class),
                        SystemAction.get (FileSystemAction.class),
                        null,
                        SystemAction.get (org.openide.actions.ExecuteAction.class),
                        SystemAction.get (org.netbeans.modules.web.execution.ExecuteForceReloadAction.class),
                        null,
                        SystemAction.get (CompileAction.class),
                        null,
                        SystemAction.get (CutAction.class),
                        SystemAction.get (CopyAction.class),
                        SystemAction.get (PasteAction.class),
                        null,
                        SystemAction.get (DeleteAction.class),
                        SystemAction.get (RenameAction.class),
                        null,
                        SystemAction.get (SaveAsTemplateAction.class),
                        null,
                        SystemAction.get (ToolsAction.class),
                        SystemAction.get (PropertiesAction.class),
                    };
    }
    
    public JspLoader() {
        super ("org.netbeans.modules.web.core.jsploader.JspDataObject"); // NOI18N
    }

    /** For subclasses. */
    protected JspLoader(Class clazz) {
        super (clazz);
    }
    
    /** For subclasses. */
    protected JspLoader(String str) {
        super (str);
    }
    
    protected JspDataObject createJspObject(FileObject pf, final UniFileLoader l) 
        throws DataObjectExistsException {
        return new JspDataObject (pf, l);
    }


    protected MultiDataObject createMultiObject (final FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        java.io.File f = NbClassPath.toFile(primaryFile);
        boolean isFileBased = ((f != null) && f.exists());

        JspDataObject obj = createJspObject(primaryFile, this);
        // [PENDING] add these from JspDataObject, not from the loader
        EditorSupport es =  obj.createJspEditor();   //new JspEditor (obj.getPrimaryEntry ());
        if (es != null) {
            es.setMIMEType (JSP_MIME_TYPE);
            obj.getCookieSet0 ().add (es);
        }
        
        obj.getCookieSet0 ().add (new TagLibParseSupport(obj));

        if (isFileBased) {
            Node.Cookie execSupport = obj.createExecSupport();
            if (execSupport != null) {
                obj.getCookieSet0 ().add (execSupport);
            }

            try {
                // check that we can conver this JSP to file
                JspCompileUtil.getFileObjectFileName(obj.getPrimaryFile());
                obj.getCookieSet0 ().add(new JspCompilerSupport.Compile(obj));
                obj.getCookieSet0 ().add(new JspCompilerSupport.Build(obj));
                obj.getCookieSet0 ().add(new JspCompilerSupport.Clean(obj));
            }
            catch (FileStateInvalidException e) {
                // compile cookies not added
            }
        }
        return obj;
    }

}
