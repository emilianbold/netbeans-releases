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

package org.netbeans.modules.html;

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
import org.openide.text.EditorSupport;
import org.openide.loaders.OpenSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.nodes.Children;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableTopComponent;


/**
* Loader for Html DataObjects.
*
* @author Jan Jancura
*/
public class HtmlLoader extends UniFileLoader {


    static final long serialVersionUID =-5809935261731217882L;
    public HtmlLoader() {
        super (HtmlDataObject.class);
    }

    protected void initialize () {
        setDisplayName(NbBundle.getBundle(HtmlLoader.class).
                       getString("PROP_HtmlLoader_Name"));
        getExtensions ().addExtension ("html"); // NOI18N
        getExtensions ().addExtension ("htm"); // NOI18N
        getExtensions ().addExtension ("shtml"); // NOI18N

        setActions (new SystemAction[] {
                        SystemAction.get (ViewAction.class),
                        SystemAction.get (OpenAction.class),
                        SystemAction.get (FileSystemAction.class),
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
                    });

/*ExtensionList ext = new ExtensionList();
        ext.addExtension("txt"); // NOI18N
        setExtensions(ext);
*/
    }

    
    protected MultiDataObject createMultiObject (final FileObject primaryFile)
    throws DataObjectExistsException, IOException {

        final HtmlDataObject obj = new HtmlDataObject (primaryFile, this);
        obj.getCookieSet ().add (new ViewCookie () {
                                     public void view () {
                                         try {
                                             TopManager.getDefault ().showUrl (obj.getPrimaryEntry ().getFile ().getURL ());
                                         } catch (FileStateInvalidException e) {
                                         }
                                     }
                                 });
        return obj;
    }
}

/*
* Log
*  26   Gandalf   1.25        1/13/00  Ian Formanek    NOI18N
*  25   Gandalf   1.24        11/27/99 Patrik Knakal   
*  24   Gandalf   1.23        11/25/99 Jesse Glick     Fixed bogus 
*       representation class.
*  23   Gandalf   1.22        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  22   Gandalf   1.21        9/30/99  Jan Jancura     Bug 3921 & 3392
*  21   Gandalf   1.20        8/9/99   Ian Formanek    HtmlDataObject is a 
*       standalone class
*  20   Gandalf   1.19        8/8/99   Ian Formanek    
*  19   Gandalf   1.18        7/8/99   Jesse Glick     Context help.
*  18   Gandalf   1.17        7/8/99   Michal Fadljevic FileSystemAction added  
*  17   Gandalf   1.16        6/10/99  Jan Jancura     Bug 1772
*  16   Gandalf   1.15        6/9/99   Ian Formanek    ToolsAction
*  15   Gandalf   1.14        6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  14   Gandalf   1.13        5/13/99  Jan Jancura     What action is default? 
*       It's a question.
*  13   Gandalf   1.12        5/12/99  Jan Jancura     Edit in txt editor & use 
*       common Html view window
*  12   Gandalf   1.11        4/1/99   Jaroslav Tulach Does not recognize .txt 
*       files.
*  11   Gandalf   1.10        3/26/99  Ian Formanek    Fixed use of obsoleted 
*       NbBundle.getBundle (this)
*  10   Gandalf   1.9         3/17/99  Ian Formanek    Made compilable
*  9    Gandalf   1.8         3/17/99  Jaroslav Tulach No setIconBase
*  8    Gandalf   1.7         3/14/99  Jaroslav Tulach Change of 
*       MultiDataObject.Entry.
*  7    Gandalf   1.6         3/2/99   Jan Jancura     
*  6    Gandalf   1.5         2/16/99  Jan Jancura     
*  5    Gandalf   1.4         2/11/99  Jan Jancura     
*  4    Gandalf   1.3         2/3/99   Jaroslav Tulach 
*  3    Gandalf   1.2         1/11/99  Jan Jancura     
*  2    Gandalf   1.1         1/11/99  Jan Jancura     
*  1    Gandalf   1.0         1/8/99   Jan Jancura     
* $
*/
