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

package com.netbeans.developer.modules.loaders.form;

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.util.HashMap;
import java.lang.reflect.Method;
import javax.swing.JComponent;

import com.netbeans.developer.impl.IDESettings;
import com.netbeans.ide.util.Utilities;
import com.netbeans.ide.util.io.*;
//import com.netbeans.developer.modules.loaders.form.layouts.*;
//import com.netbeans.developer.modules.loaders.form.layouts.support.*;
import com.netbeans.developer.modules.loaders.form.util.*;

/** A class that contains utility methods for the formeditor.
*
* @author Ian Formanek
*/
public class FormUtils extends Object {

// -----------------------------------------------------------------------------
// Static variables
  
  private static final boolean debug = (System.getProperty ("netbeans.debug.form") != null);

  /** The IDESettings - useed for output details level */
  private static final IDESettings ideSettings = new IDESettings ();

  /** The list of all well-known heavyweight components */
  private static Class[] heavyweightComponents;

  private static HashMap jComponentIgnored;

  private static HashMap valuesCache = new HashMap ();

  /** The properties whose changes are ignored in JComponent subclasses */
  private static String[] jComponentIgnoredList = new String [] {
    "UI",
    "layout",
    "maximumSize",
    "minimumSize",
    "preferredSize",
    "border",
    "model"
  };

  static {
    try {
      heavyweightComponents = new Class[] {
        java.awt.Button.class,
        java.awt.Canvas.class,
        java.awt.List.class,
        java.awt.Button.class,
        java.awt.Label.class,
        java.awt.TextField.class,
        java.awt.TextArea.class,
        java.awt.Checkbox.class,
        java.awt.Choice.class,
        java.awt.List.class,
        java.awt.Scrollbar.class,
        java.awt.ScrollPane.class,
        java.awt.Panel.class,
      };
    } catch (Exception e) {
      throw new InternalError("Cannot initialize AWT classes");
    }

    jComponentIgnored = new HashMap (15);
    for (int i = 0; i < jComponentIgnoredList.length; i++)
      jComponentIgnored.put (jComponentIgnoredList[i], jComponentIgnoredList[i]);
  }

// -----------------------------------------------------------------------------
// Utility methods
  
  /** Moves specified window to the center of the screen
  */
  public static void centerWindow (Window w) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension dialogSize = w.getSize();
    w.setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);
  }
  
  public static void notifyPropertyException (Class beanClass, String propertyName, String displayName, Throwable t, boolean reading) {
    boolean dontPrint = false;
    // if it is a subclass of Applet, we ignore InvocationTargetException
    // on codeBase, documentBase and appletContext properties
    if (java.applet.Applet.class.isAssignableFrom (beanClass))
      if ("codeBase".equals (propertyName) ||
          "documentBase".equals (propertyName) ||
          "appletContext".equals (propertyName))
         dontPrint = true;
    if ("tearOff".equals (propertyName) || "helpMenu".equals (propertyName))
      dontPrint = true;
    if (!dontPrint) {
//    if (System.getProperty ("netbeans.full.hack") != null)
//      e.printStackTrace ();
      com.netbeans.ide.TopManager.getDefault ().getStdOut ().println (
        java.text.MessageFormat.format (
          reading ? com.netbeans.ide.util.NbBundle.getBundle (FormUtils.class).getString ("FMT_ERR_ReadingProperty") :
                    com.netbeans.ide.util.NbBundle.getBundle (FormUtils.class).getString ("FMT_ERR_WritingProperty"),
          new Object[] {
            t.getClass ().getName (),
            propertyName,
            displayName
          }
        )
      );
    }
  }


  /** A utility method that returns the string that should be used for indenting
  * the generated text. It is a String that is a tabSize of spaces
  */
  public static String getIndentString () {
    return "  "; // EditorSettingsJava.getIndentString ();
  }

  /** A utility method for formatting method header text for specified
  * method, its name and parameter names.
  * @param m The method - its modifiers, return type, parameter types and
  *                       exceptions are used
  * @param indent A indent to use for the method header
  * @param paramNames An array of names of parameters - the length of this
  *            array MUST be the same as the actual number of method's parameters
  */
  public static String getMethodHeaderText (Method m, String indent, String[] paramNames) {
    StringBuffer buf = new StringBuffer() ;
    buf.append (indent);
    buf.append ("public ");
    buf.append (m.getReturnType().getName());
    buf.append (" ");
    buf.append (m.getName());
    buf.append (" (");

    Class[] params = m.getParameterTypes();
    for (int i = 0; i < params.length; i++) {
      buf.append (params[i].getName());
      buf.append (" ");
      buf.append (paramNames[i]);
      if (i != params.length - 1)
        buf.append (", ");
    }
    buf.append (")");

    Class[] exceptions = m.getExceptionTypes();
    if (exceptions.length != 0) {
      buf.append ("\n");
      buf.append (indent);
      buf.append ("throws ");
    }
    for (int i = 0; i < exceptions.length; i++) {
      buf.append (exceptions[i].getName());
      if (i != exceptions.length - 1)
        buf.append (", ");
    }

    return buf.toString();
  }

// -----------------------------------------------------------------------------
// JavaBeans helper mthods

  /** A utility method for checking whether specified component is
  * heavyweight or lightweight.
  * @param comp The component to check
  */
  public static boolean isHeavyweight(java.awt.Component comp) {
    for (int i=0; i < heavyweightComponents.length; i++)
      if (heavyweightComponents[i].isAssignableFrom(comp.getClass()))
        return true;
    return false;
  }

  public static boolean isIgnoredProperty (Class beanClass, String propertyName) {
    if (JComponent.class.isAssignableFrom (beanClass)) {
      if (jComponentIgnored.get (propertyName) != null)
        return true;
    }
    if (javax.swing.JDesktopPane.class.isAssignableFrom (beanClass) && "desktopManager".equals (propertyName))
      return true;
    return false;
  }

  /** @return a default name for event handling method - it is a concatenation of
  * the component name and the name of the listener method (with first letter capital)
  * (e.g. button1MouseReleased).
  */
  public static String getDefaultEventName (RADComponent component, Method listenerMethod) {
    String componentName = component.getName ();
    if (component instanceof FormContainer) {
      componentName = "form";
    }
    StringBuffer sb = new StringBuffer (componentName);
    String lm = listenerMethod.getName ();
    sb.append (lm.substring (0, 1).toUpperCase ());
    sb.append (lm.substring (1));
    return sb.toString ();
  } 

  /** @return a formatted name of specified method
  */
  public static String getMethodName (MethodDescriptor desc) {
    StringBuffer sb = new StringBuffer (desc.getName ());
    Class[] params = desc.getMethod ().getParameterTypes ();
    if ((params == null) || (params.length == 0)) {
      sb.append (" ()");
    } else {
      for (int i = 0; i < params.length; i++) {
        if (i == 0) sb.append (" (");
        else sb.append (", ");
        sb.append (Utilities.getShortClassName (params[i]));
      }
      sb.append (")");
    }

    return sb.toString ();
  }

  /** A utility method that returns a class of event adapter for
  * specified listener. It works only on known listeners from java.awt.event.
  * Null is returned for unknown listeners.
  * @return class of an adapter for specified listener or null if
  *               unknown/does not exist
  */
  public static Class getAdapterForListener (Class listener) {
    if (java.awt.event.ComponentListener.class.equals (listener))
      return java.awt.event.ComponentAdapter.class;
    else if (java.awt.event.ContainerListener.class.equals (listener))
      return java.awt.event.ContainerAdapter.class;
    else if (java.awt.event.FocusListener.class.equals (listener))
      return java.awt.event.FocusAdapter.class;
    else if (java.awt.event.KeyListener.class.equals (listener))
      return java.awt.event.KeyAdapter.class;
    else if (java.awt.event.MouseListener.class.equals (listener))
      return java.awt.event.MouseAdapter.class;
    else if (java.awt.event.MouseMotionListener.class.equals (listener))
      return java.awt.event.MouseMotionAdapter.class;
    else if (java.awt.event.WindowListener.class.equals (listener))
      return java.awt.event.WindowAdapter.class;
    else return null; // not found

  }

// -----------------------------------------------------------------------------
// Visual utility methods

  /**
  * This method is intended to be started from container's paint method to do all the
  * things concerning its grid. It is not necessary to use it, but it can hide some
  * low-level details to programmers who write their own containers and do not want to
  * go deep into implementation details. Moreover, by using this method it is sure that
  * the grid is done in a standard way.
  * @param xvc  The container which wants to have a grid painted
  * @param g    The Graphics givenas param. to paint method
  * @param gi    Xvc's gridInfo
  * @param offsX  x-offset to paint grid from
  * @param offsY  y-offset to paint grid from
  * @param imW  width of grid
  * @param imH  height of grid
  */
  public static void paintGrid(Component comp, Graphics g, GridInfo gi, int offsX, int offsY, int imW, int imH) {
    if (imW <= 0 || imH <=0 ) return;
    if (gi.getGridX() == 1 && gi.getGridY() == 1 ) return; // no grid
    if (gi.gridImage == null || gi.imWidth != imW || gi.imHeight != imH)
      new GridThread(comp, gi, imW, imH).run();

    if (gi.gridImage != null)
      g.drawImage(gi.gridImage, offsX, offsY, null);
  }

  /**
  * This method converts array of Rectangles (with compoment bounds) to
  * GridBagConstraints.
  *
  * Some bug (properly in GridBagLayout) appeares:
  *
  *  here will be bad size!!!!
  *               |
  *               V
  * mmmm  mmmm  mmmm  mmmm
  *         mmmmm
  */
  public static GridBagConstraints[] convertToConstraints (Rectangle[] r, Component[] com) {
    int i, k = r.length;
    GridBagConstraints[] c = new GridBagConstraints[k];
    for (i = 0; i < k; i++) {
      c [i] = new GridBagConstraints ();
      int gx = 0, x1 = r [i].x;
      int gy = 0, y1 = r [i].y;
      int gw = 1, x2 = x1 + r [i].width;
      int gh = 1, y2 = y1 + r [i].height;
      int fromX = 0, fromY = 0;
      int j, l = r.length;
      for (j = 0; j < l; j++) {
        int xe = r [j].x + r [j].width;
        int ye = r [j].y + r [j].height;
        if (xe <= x1) {
          gx++;
          fromX = Math.max (fromX, xe);
        }
        if (ye <= y1) {
          gy++;
          fromY = Math.max (fromY, ye);
        }
        if ((xe > x1) && (xe < x2)) gw++;
        if ((ye > y1) && (ye < y2)) gh++;
      }
      c [i].gridx = gx;
      c [i].gridy = gy;
      c [i].gridwidth = gw;
      c [i].gridheight = gh;
      c [i].insets = new Insets (y1 - fromY, x1 - fromX, 0, 0);
      c [i].fill = GridBagConstraints.BOTH;
      c [i].ipadx = (r [i].width - com [i].getPreferredSize ().width);
      c [i].ipady = (r [i].height - com [i].getPreferredSize ().height);
    }
    return c;
  }


// -----------------------------------------------------------------------------
// DEBUG utilities

  public static void DEBUG () {
    if (debug) {
      Thread.dumpStack();
    }
  }
  
  public static void DEBUG (String s) {
    if (debug) {
      System.out.println(s);
    }
  }
  
// -----------------------------------------------------------------------------
// Safe Serialization

  public static void writeSafely (ObjectOutput oo, Object obj)
  throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream (200);
    ObjectOutputStream oos = new ObjectOutputStream (bos);
    oos.writeObject (obj);
    oos.flush ();
    bos.close ();

    oo.writeInt (bos.size ());
    oo.write (bos.toByteArray ());
  }

  public static Object readSafely (ObjectInput oi)
  throws IOException, ClassNotFoundException {
    int size = oi.readInt ();
    byte[] byteArray = new byte [size];
    oi.readFully (byteArray, 0, size);

    ByteArrayInputStream bis = new ByteArrayInputStream (byteArray);
    ObjectInputStream ois = new ObjectInputStream (bis);
    Object obj = ois.readObject ();
    bis.close ();

    return obj;
  }

}

/*
 * Log
 *  11   Gandalf   1.10        5/15/99  Ian Formanek    
 *  10   Gandalf   1.9         5/15/99  Ian Formanek    
 *  9    Gandalf   1.8         5/13/99  Ian Formanek    
 *  8    Gandalf   1.7         5/10/99  Ian Formanek    
 *  7    Gandalf   1.6         5/4/99   Ian Formanek    Package change
 *  6    Gandalf   1.5         4/29/99  Ian Formanek    
 *  5    Gandalf   1.4         4/7/99   Ian Formanek    Debug finalized, 
 *       Hashtable->HashMap
 *  4    Gandalf   1.3         3/29/99  Ian Formanek    Added DEBUG methods
 *  3    Gandalf   1.2         3/28/99  Ian Formanek    
 *  2    Gandalf   1.1         3/24/99  Ian Formanek    
 *  1    Gandalf   1.0         3/17/99  Ian Formanek    
 * $
 */
