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

package com.netbeans.developer.modules.text.options;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.ArrayList;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport.ReadWrite;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.util.Utilities;

import com.netbeans.editor.ColoringManager;
import com.netbeans.editor.Coloring;
import com.netbeans.editor.Settings;

/** BeanInfo for plain options
*
* @author Miloslav Metelka, Ales Novak
*/
public class ColoringArrayEditor extends PropertyEditorSupport {
    
  public boolean supportsCustomEditor() {
    return true;
  }

  public String getAsText() {
    return "Fonts and Colors";
  }

  public void setAsText(String s) {
    throw new IllegalArgumentException();
  }

  public java.awt.Component getCustomEditor() {
    Object[] vals = (Object[])getValue();
    String typeName = (String)vals[0];
    Coloring defaultColoring = (Coloring)vals[1];
    
    ArrayList cpsList = new ArrayList();
    ResourceBundle bundle = NbBundle.getBundle(ColoringArrayEditor.class);
    for (int coloringSet = 2; coloringSet < vals.length; coloringSet++) {
      Coloring[] colorings =  (Coloring[])vals[coloringSet];

      for (int i = 0; i < colorings.length; i++) {
        String desc;
        try {
          desc = bundle.getString("HINT_coloring_" + typeName
              + "_" + colorings[i].getName());
        } catch (MissingResourceException e) {
          try {
            desc = bundle.getString("HINT_coloring_" + BaseOptions.BASE
                + "_" + colorings[i].getName());
          } catch (MissingResourceException e2) {
            desc = colorings[i].getName();
          }
        }

        String example;
        try {
          example = bundle.getString("EXAMPLE_coloring_" + typeName
            + "_" + colorings[i].getName());
        } catch (MissingResourceException e) {
          try {
            example = bundle.getString("EXAMPLE_coloring_" + BaseOptions.BASE
              + "_" + colorings[i].getName());
          } catch (MissingResourceException e2) {
            example = "";
          }
        }

        cpsList.add(new ColoringProperty(colorings, i, desc, example, defaultColoring));
      }
    }

    ColoringProperty[] cps = new ColoringProperty[cpsList.size()];
    cpsList.toArray(cps);

    FakeNode fn = new FakeNode(cps);
    PropertySheet psheet = new PropertySheet();
    psheet.setNodes(new Node[] {fn});
    HelpCtx.setHelpIDString (psheet, FakeNode.class.getName ());
    return psheet;
  }

  /** This node is passed to a propertysheet instance */
  static class FakeNode extends AbstractNode {

    final ColoringProperty[] cps;

    FakeNode(ColoringProperty[] cps) {
      super (Children.LEAF);
      this.cps = cps;
    }

    public HelpCtx getHelpCtx () {
      return new HelpCtx (FakeNode.class);
    }

    /** Creats a sheet - with ColoringProperties */
    protected Sheet createSheet() {
      Sheet s = Sheet.createDefault();
      Sheet.Set ss = s.get(Sheet.PROPERTIES);

      for (int i = 0; i < cps.length; i++) {
        ss.put(cps[i]);
      }

      return s;
    }
  }

  /** One coloring in node's properties */
  static class ColoringProperty extends ReadWrite {

    Coloring[] colorings;
    int index;
    String desc;
    String example;
    Coloring defaultColoring;

    ColoringProperty(Coloring[] colorings, int index,
    String desc, String example, Coloring defaultColoring) {
      super (
             colorings[index].getName(),
             ColoringBean.class,
             colorings[index].getName(),
             desc
      );
      this.colorings = colorings;
      this.index = index;
      this.desc = desc;
      this.example = example;
      this.defaultColoring = defaultColoring;
    }

    public Object getValue() {
      ColoringBean cb = new ColoringBean();
      cb.setColoring(colorings[index]);
      cb.defaultColoring = defaultColoring;
      cb.example = example;
      return cb;
    }

    public void setValue(Object val) {
      if (val == null) {
        throw new IllegalArgumentException();
      }
      colorings[index] = ((ColoringBean)val).getColoring();
      Settings.touchValue(null, Settings.COLORING_MANAGER); // kit class null right now
    }

    public PropertyEditor getPropertyEditor() {
      return new ColoringEditor();
    }

  }

}

/*
* Log
*  10   Gandalf   1.9         11/14/99 Miloslav Metelka 
*  9    Gandalf   1.8         11/5/99  Jesse Glick     Context help jumbo patch.
*  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    Gandalf   1.6         8/27/99  Miloslav Metelka 
*  6    Gandalf   1.5         8/17/99  Miloslav Metelka 
*  5    Gandalf   1.4         7/30/99  Miloslav Metelka 
*  4    Gandalf   1.3         7/29/99  Miloslav Metelka 
*  3    Gandalf   1.2         7/26/99  Miloslav Metelka 
*  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
*  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
* $
*/
