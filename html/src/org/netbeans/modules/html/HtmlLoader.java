/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.html;

import java.util.*;
import java.io.IOException;
import java.net.URL;
import java.awt.BorderLayout;

import com.netbeans.ide.TopManager;
import com.netbeans.ide.actions.*;
import com.netbeans.ide.awt.HtmlBrowser;
import com.netbeans.ide.cookies.ViewCookie;
import com.netbeans.ide.loaders.UniFileLoader;
import com.netbeans.ide.loaders.MultiDataObject;
import com.netbeans.ide.loaders.DataObject;
import com.netbeans.ide.loaders.DataNode;
import com.netbeans.ide.loaders.DataObjectExistsException;
import com.netbeans.ide.text.EditorSupport;
import com.netbeans.ide.loaders.OpenSupport;
import com.netbeans.ide.filesystems.FileObject;
import com.netbeans.ide.filesystems.FileStateInvalidException;
import com.netbeans.ide.nodes.Children;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.ide.windows.CloneableTopComponent;


/**
* Loader for Html DataObjects.
*
* @author Jan Jancura
*/
public class HtmlLoader extends UniFileLoader {


  {
    setActions (new SystemAction[] {
      SystemAction.get (ViewAction.class),
      SystemAction.get (OpenAction.class),
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
      new PropertiesAction ()
    });
  }

  public HtmlLoader() {
    super (MultiDataObject.class);
    setDisplayName(NbBundle.getBundle(HtmlLoader.class).
                   getString("PROP_HtmlLoader_Name"));
    getExtensions ().addExtension ("html");
    getExtensions ().addExtension ("htm");
    getExtensions ().addExtension ("shtml");
  }

  protected MultiDataObject createMultiObject (final FileObject primaryFile)
  throws DataObjectExistsException, IOException {

    class Obj extends MultiDataObject {
      public Obj (FileObject pf, UniFileLoader l) throws DataObjectExistsException {
        super (pf, l);
      }
      
      protected com.netbeans.ide.nodes.Node createNodeDelegate () {
        DataNode n = new DataNode (Obj.this, Children.LEAF);
        n.setIconBase ("/com/netbeans/developer/modules/loaders/html/htmlObject");
        n.setDefaultAction (SystemAction.get (OpenAction.class));
        return n;
      }
    };

    MultiDataObject obj = new Obj (primaryFile, this);
    EditorSupport es = new EditorSupport (obj.getPrimaryEntry ());
    es.setMIMEType ("text/plain");
    obj.getCookieSet ().add (es);
    obj.getCookieSet ().add (new ViewCookie () {
      public void view () {
        try {
          TopManager.getDefault ().showUrl (primaryFile.getURL ());
        } catch (FileStateInvalidException e) {
        }  
      }
    });    
    return obj;
  }
}

/*
* Log
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
