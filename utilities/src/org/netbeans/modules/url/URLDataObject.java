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

package com.netbeans.developer.modules.loaders.url;

import java.io.*;

import org.openide.*;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.*;


/** Object that represents one file containing url in the tree of
* beans representing data systems.
*
* @author Ian Formanek
*/
public class URLDataObject extends MultiDataObject implements EditCookie, OpenCookie, URLNodeCookie {
  /** generated Serialized Version UID */
//  static final long serialVersionUID = -6035788991669336965L;

  private final static String URL_ICON_BASE =
    "com/netbeans/developer/modules/loaders/url/urlObject"; // NOI18N

  /* The FileObject containing the URL String */
  private FileObject urlFile;

static final long serialVersionUID =6829522922370124627L;
  /** New instance.
  * @param pf primary file object for this data object
  */
  public URLDataObject(final FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
    super(pf, loader);
    urlFile = pf;
    getCookieSet ().add (this);
  }

  /** @return the URL String stored in the file. If there are multiple lines of text in the 
  *           file, only the first line is returned
  */
  private String getURLString () {
    String urlString = ""; // NOI18N
    InputStream is = null;
    try {
      urlString = new BufferedReader (new InputStreamReader (is = urlFile.getInputStream ())).readLine ();
    } catch (FileNotFoundException e) {
      TopManager.getDefault ().notify (
          new NotifyDescriptor.Message (
              java.text.MessageFormat.format (
                  NbBundle.getBundle (URLDataObject.class).getString("MSG_FMT_FileNotFoundError"),
                  new Object[] { urlFile.getPackageNameExt (File.separatorChar, '.') }
              ),
              NotifyDescriptor.ERROR_MESSAGE
          )
      );
      return null;
    } catch (IOException e) {
      TopManager.getDefault ().notify (
          new NotifyDescriptor.Message (
              java.text.MessageFormat.format (
                  NbBundle.getBundle (URLDataObject.class).getString("MSG_FMT_IOError"),
                  new Object[] { urlFile.getPackageNameExt (File.separatorChar, '.'), e.getMessage () }
              ),
              NotifyDescriptor.ERROR_MESSAGE
          )
      );
      e.printStackTrace ();
      return null;
    } finally {
      if (is != null)
        try {
          is.close ();
        } catch (IOException e) {
        }
    }
    if (urlString == null)
      urlString = ""; // if the file is empty, return empty string, as null is reserved for notifying failure
    
    return urlString;
  }

  /** Stores specified String into the URL file.
  * @param newUrlString the URL String to be stored in the file.
  */
  private void setURLString (String newUrlString) {
    FileLock lock = null;
    try {
      lock = urlFile.lock ();
      OutputStream os = urlFile.getOutputStream (lock);
      os.write (newUrlString.getBytes ());
      os.close ();
    } catch (IOException e) {
      e.printStackTrace ();
    } finally {
      if (lock != null)
        lock.releaseLock ();
    }
  }
  
  /** Help context for this object.
  * @return help context
  */
  public org.openide.util.HelpCtx getHelpCtx () {
    return new HelpCtx (URLDataObject.class);
  }

  /** Provides node that should represent this data object. When a node for representation
  * in a parent is requested by a call to getNode (parent) it is the exact copy of this node
  * with only parent changed. This implementation creates instance
  * <CODE>DataNode</CODE>.
  * <P>
  * This method is called only once.
  *
  * @return the node representation for this data object
  * @see DataNode
  */
  protected Node createNodeDelegate () {
    return new URLNode (this);
  }

// -----------------------------------------------------------------
// OpenCookie implementation

  /** Invokes the open action */
  public void open () {
    String urlString = getURLString ();
    if (urlString == null) return;
    
    java.net.URL url = null;
    try {
      url = new java.net.URL (urlString);
      TopManager.getDefault ().showUrl (url);
    } catch (java.net.MalformedURLException e) {
      try {
        url = new java.net.URL ("http://"+urlString); // try to prepend http protocol
        TopManager.getDefault ().showUrl (url);
      } catch (java.net.MalformedURLException e2) {
        if (urlString.length () > 50) { // too long URL
          TopManager.getDefault ().notify (
              new NotifyDescriptor.Message (
                  NbBundle.getBundle (URLDataObject.class).getString("MSG_MalformedURLError"),
                  NotifyDescriptor.ERROR_MESSAGE
              )
          );
        } else {            
          TopManager.getDefault ().notify (
              new NotifyDescriptor.Message (
                  java.text.MessageFormat.format (
                      NbBundle.getBundle (URLDataObject.class).getString("MSG_FMT_MalformedURLError"),
                      new Object[] { urlString }
                  ),
                  NotifyDescriptor.ERROR_MESSAGE
              )
          );
        }
      }
    }
  }

// -----------------------------------------------------------------
// URLNodeCookie implementation

  public void openInNewWindow () {
    String urlString = getURLString ();
    if (urlString == null) return;
    
    java.net.URL url = null;
    try {
      url = new java.net.URL (urlString);
    } catch (java.net.MalformedURLException e) {
      try {
        url = new java.net.URL ("http://"+urlString); // try to prepend http protocol
      } catch (java.net.MalformedURLException e2) {
        if (urlString.length () > 50) { // too long URL
          TopManager.getDefault ().notify (
              new NotifyDescriptor.Message (
                  NbBundle.getBundle (URLDataObject.class).getString("MSG_MalformedURLError"),
                  NotifyDescriptor.ERROR_MESSAGE
              )
          );
        } else {            
          TopManager.getDefault ().notify (
              new NotifyDescriptor.Message (
                  java.text.MessageFormat.format (
                      NbBundle.getBundle (URLDataObject.class).getString("MSG_FMT_MalformedURLError"),
                      new Object[] { urlString }
                  ),
                  NotifyDescriptor.ERROR_MESSAGE
              )
          );
        }
        return;
      }
    }
  
    HtmlBrowser.BrowserComponent htmlViewer = new HtmlBrowser.BrowserComponent ();
    htmlViewer.setURL (url);
    htmlViewer.open ();
    htmlViewer.requestFocus ();
  }
  
// -----------------------------------------------------------------
// EditCookie implementation


  /** Instructs an editor to be opened. The operation can
  * return immediately and the editor be opened later.
  * There can be more than one editor open, so one of them is
  * arbitrarily chosen and opened.
  */
  public void edit () {
    String urlString = getURLString ();
    if (urlString == null) return;
    NotifyDescriptor.InputLine urlLine = new NotifyDescriptor.InputLine (
      NbBundle.getBundle (URLDataObject.class).getString("CTL_URL") , 
      NbBundle.getBundle (URLDataObject.class).getString("CTL_EditURL")); 
    urlLine.setInputText (urlString);
    TopManager.getDefault ().notify (urlLine);
    if (urlLine.getValue () == NotifyDescriptor.OK_OPTION) 
      setURLString (urlLine.getInputText ());
  }

// -----------------------------------------------------------------
// Innerclasses


  /** URL Node implementation.
  * Leaf node, default action opens editor or instantiates template.
  * Icons redefined.
  */
  public static final class URLNode extends DataNode {

    /** Default constructor, constructs node */
    public URLNode (final DataObject dataObject) {
      super(dataObject, Children.LEAF);
      setIconBase(URL_ICON_BASE);
    }

    /** Overrides default action from DataNode.
    * Instantiate a template, if isTemplate() returns true.
    * Opens otherwise.
    */
    public SystemAction getDefaultAction () {
      SystemAction result = super.getDefaultAction();
      return result == null ? SystemAction.get(org.openide.actions.OpenAction.class) : result;
    }
  } // end of URLNode inner class
}


/*
 * Log
 *  16   Gandalf   1.15        1/12/00  Ian Formanek    I18N
 *  15   Gandalf   1.14        1/5/00   Ian Formanek    NOI18N
 *  14   Gandalf   1.13        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  13   Gandalf   1.12        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  12   Gandalf   1.11        7/11/99  Ian Formanek    employed EditAction
 *  11   Gandalf   1.10        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  10   Gandalf   1.9         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         6/7/99   Ian Formanek    Fixed bug 1585 - URL 
 *       action "Open in New window" does not work.
 *  8    Gandalf   1.7         5/8/99   Ian Formanek    Fixed displaying icon
 *  7    Gandalf   1.6         4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  6    Gandalf   1.5         4/8/99   Ian Formanek    Removed debug prints
 *  5    Gandalf   1.4         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  4    Gandalf   1.3         3/9/99   Ian Formanek    
 *  3    Gandalf   1.2         3/9/99   Ian Formanek    
 *  2    Gandalf   1.1         2/25/99  Ian Formanek    
 *  1    Gandalf   1.0         1/22/99  Ian Formanek    
 * $
 */
