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

package org.netbeans.modules.form.editors2;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.net.URL;

import javax.swing.*;

import org.openide.TopManager;

/**
* PropertyEditor for Icons. Depends on existing DataObject for images.
* Images must be represented by some DataObject which returns itselv
* as cookie, and has image file as a primary file. File extensions
* for images is specified in isImage method.
*
* @author Jan Jancura
*/
public class IconEditor extends Object {
  public static final int TYPE_URL = 1;
  public static final int TYPE_FILE = 2;
  public static final int TYPE_CLASSPATH = 3;

  static final String URL_PREFIX = "URL"; // NOI18N
  static final String FILE_PREFIX = "File"; // NOI18N
  static final String CLASSPATH_PREFIX = "Classpath"; // NOI18N

  public static final String BAD_ICON_NAME = "/org/netbeans/modules/form/editors2/badIcon.gif"; // NOI18N

  // innerclasses ...............................................................

  public static class NbImageIcon extends ImageIcon implements Externalizable {
  /** generated Serialized Version UID */
  static final long serialVersionUID = 7018807466471349466L;
    int type;
    String name;

    public NbImageIcon () {
    }

    NbImageIcon (URL url) {
      super (url);
      type = TYPE_URL;
    }

    NbImageIcon (String file) {
      super (file);
      type = TYPE_FILE;
    }

    String getName () {
      return name;
    }

    public void writeExternal (ObjectOutput oo) throws IOException {
      oo.writeObject (new Integer (type));
      oo.writeObject (name);
    }

    public void readExternal (ObjectInput in)
    throws IOException, ClassNotFoundException {
      type = ((Integer)in.readObject ()).intValue ();
      name = (String) in.readObject ();
      ImageIcon ii = null;
      switch (type) {
      case TYPE_URL:
        try {
          ii = new ImageIcon (new URL (name));
        } catch (Exception e) {
          ii = new ImageIcon (IconEditor.class.getResource (BAD_ICON_NAME));
        }
        break;
      case TYPE_FILE:
        ii = new ImageIcon (name);
        break;
      case TYPE_CLASSPATH:
        try {
          java.net.URL url = TopManager.getDefault ().currentClassLoader ().getResource (name);
          ii = new ImageIcon (url);
        } catch (Exception e) {
          ii = new ImageIcon (IconEditor.class.getResource (BAD_ICON_NAME));
          e.printStackTrace ();
        }
        break;
      }
      setImage (ii.getImage ());
    }
  }

}

/*
* Log
*  5    Gandalf   1.4         1/13/00  Ian Formanek    NOI18N #2
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         8/17/99  Ian Formanek    Fixed bug 3431 - Forms 
*       which contain icons referring to non-existing images on Class-path fail 
*       to load.
*  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         5/17/99  Ian Formanek    
* $
*/
