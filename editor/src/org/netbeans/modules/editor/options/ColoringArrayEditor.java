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
    return "Colorings";
  }

  public void setAsText(String s) {
    throw new IllegalArgumentException();
  }

  public java.awt.Component getCustomEditor() {
    Object[] vals = (Object[])getValue();
    Coloring[] colorings =  (Coloring[])vals[0];
    Coloring defaultColoring = (Coloring)vals[1];
    String typeName = (String)vals[2];
    
    ColoringProperty[] cps = new ColoringProperty[colorings.length];
    ResourceBundle bundle = NbBundle.getBundle(ColoringArrayEditor.class);
    for (int i = 0; i < colorings.length; i++) {
//      System.out.println("ColoringArrayEditor.java:63 typeName=" + typeName + ", name=" + colorings[i].getName());
      String desc = bundle.getString("HINT_coloring_" + typeName
          + "_" + colorings[i].getName());
      String example = bundle.getString("EXAMPLE_coloring_" + typeName
          + "_" + colorings[i].getName());
      cps[i] = new ColoringProperty(colorings, i, desc, example, defaultColoring);
    }

    FakeNode fn = new FakeNode(cps);
    PropertySheet psheet = new PropertySheet();
    psheet.setNodes(new Node[] {fn});
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
      Settings.touchValue(Settings.COLORING_MANAGER);
      System.out.println("setValue(): colorings=" + System.identityHashCode(colorings) + ", coloring=" + colorings[index]);
    }

    public PropertyEditor getPropertyEditor() {
      return new ColoringEditor();
    }

  }

}

/*
* Log
*  4    Gandalf   1.3         7/29/99  Miloslav Metelka 
*  3    Gandalf   1.2         7/26/99  Miloslav Metelka 
*  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
*  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
* $
*/
