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

import com.netbeans.ide.actions.*;
import com.netbeans.ide.awt.HtmlBrowser;
import com.netbeans.ide.cookies.ViewCookie;
import com.netbeans.ide.loaders.UniFileLoader;
import com.netbeans.ide.loaders.MultiDataObject;
import com.netbeans.ide.loaders.DataObject;
import com.netbeans.ide.loaders.DataObjectExistsException;
import com.netbeans.ide.text.EditorSupport;
import com.netbeans.ide.loaders.OpenSupport;
import com.netbeans.ide.filesystems.FileObject;
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
    setDisplayName(NbBundle.getBundle(this).
                   getString("PROP_HtmlLoader_Name"));
    getExtensions ().addExtension ("txt");
    getExtensions ().addExtension ("html");
    getExtensions ().addExtension ("htm");
    getExtensions ().addExtension ("shtml");
  }

  protected MultiDataObject createMultiObject (FileObject primaryFile)
  throws DataObjectExistsException, IOException {

    MultiDataObject obj = new MultiDataObject (primaryFile, this);
    obj.setIconBase ("/com/netbeans/developer/modules/loaders/html/htmlObject");
    EditorSupport es = new EditorSupport (obj.getPrimaryEntry ());
    es.setActions (new SystemAction [] {
      SystemAction.get (CutAction.class),
      SystemAction.get (CopyAction.class),
      SystemAction.get (PasteAction.class),
    });
    obj.getCookieSet ().add (es);
    obj.getCookieSet ().add (
      new BrowserView (obj)
    );
    return obj;
  }


  // innerclasses ......................................................................

  public static class BrowserView extends OpenSupport implements ViewCookie {
    DataObject obj;

    BrowserView (MultiDataObject obj) {
      super (obj.getPrimaryEntry ());
      this.obj = obj;
    }

    /** A method to create a new component. Overriden in subclasses.
    * @return the cloneable top component for this support
    */
    protected CloneableTopComponent createCloneableTopComponent () {
      return new BrowserCloneableTopComponent (obj);
    }
  }

  public static class BrowserCloneableTopComponent extends CloneableTopComponent {
    private HtmlBrowser browser;
    DataObject obj;

    /** Constructor
    * @param obj data object we belong to
    */
    public BrowserCloneableTopComponent (DataObject obj) {
      super (obj);
      this.obj = obj;

      setLayout (new BorderLayout ());
//      if (actions != null)
//        add (SystemAction.createToolbarPresenter (actions), BorderLayout.NORTH);

      browser = new HtmlBrowser ();
      try {
        browser.setURL (obj.getPrimaryFile ().getURL ());
      } catch (com.netbeans.ide.filesystems.FileStateInvalidException e) {
        e.printStackTrace ();
      }
      add (browser, BorderLayout.CENTER);
    }

    /** Is called from the clone method to create new component from this one.
    * This implementation only clones the object by calling super.clone method.
    * @return the copy of this object
    */
    protected CloneableTopComponent createClonedObject () {
      return new BrowserCloneableTopComponent (obj);
    }

    /** This method is called when parent window of this component has focus,
    * and this component is preferred one in it.
    * Override this method to perform special action on component activation.
    * (Typical thing to do here is set performers for your actions)
    * Remember to call superclass to
    */
    protected void componentActivated () {
    }

    /**
    * This method is called when parent window of this component losts focus,
    * or when this component losts preferrence in the parent window.
    * Override this method to perform special action on component deactivation.
    * (Typical thing to do here is unset performers for your actions)
    */
    protected void componentDeactivated () {
    }
  }
}

/*
* Log
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
