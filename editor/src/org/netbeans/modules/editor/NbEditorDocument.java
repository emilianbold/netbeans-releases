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

package com.netbeans.developer.modules.text;

import java.awt.Component;
import javax.swing.text.AttributeSet;
import javax.swing.JEditorPane;
import com.netbeans.editor.StyledGuardedDocument;
import com.netbeans.editor.Syntax;
import com.netbeans.editor.Utilities;
import org.openide.text.NbDocument;

/** 
* BaseDocument extension managing the readonly blocks of text
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorDocument extends StyledGuardedDocument
implements NbDocument.PositionBiasable, NbDocument.WriteLockable,
NbDocument.Printable, NbDocument.CustomEditor {

  PrintSupport printSupport;

  public NbEditorDocument(Class kitClass, Syntax syntax) {
    super(kitClass, syntax);
    addStyleToLayerMapping(NbDocument.BREAKPOINT_STYLE_NAME,
        NbDocument.BREAKPOINT_STYLE_NAME + "Layer:5000");
    addStyleToLayerMapping(NbDocument.ERROR_STYLE_NAME,
        NbDocument.ERROR_STYLE_NAME + "Layer:6000");
    addStyleToLayerMapping(NbDocument.CURRENT_STYLE_NAME,
        NbDocument.CURRENT_STYLE_NAME + "Layer:7000");
    setNormalStyleName(NbDocument.NORMAL_STYLE_NAME);
  } 


  public void setCharacterAttributes(int offset, int length, AttributeSet s,
  boolean replace) {
//    System.out.println("NbEditorDocument.java:48 setCharacterAttributes(): offset=" + offset + ", length=" + length + ", attrSet=" + s);
    if (s != null) {
      Object val = s.getAttribute(NbDocument.GUARDED);
      if (val != null && val instanceof Boolean) {
        if (((Boolean)val).booleanValue() == true) { // want make guarded
          super.setCharacterAttributes(offset, length, guardedSet, replace);
        } else { // want make unguarded
          super.setCharacterAttributes(offset, length, unguardedSet, replace);
        }
      } else { // not special values, just pass
        super.setCharacterAttributes(offset, length, s, replace);
      }
    }
  }

  protected PrintSupport getPrintSupport() {
    if (printSupport == null) {
      printSupport = new PrintSupport(this);
    }
    return printSupport;
  }

  public java.text.AttributedCharacterIterator[] createPrintIterators() {
    return getPrintSupport().createPrintIterators();
  }
  
  public Component createEditor(JEditorPane j) {
    return Utilities.getExtUI(j).getExtComponent();
  }

}

/*
 * Log
 *  9    Gandalf   1.8         8/27/99  Miloslav Metelka 
 *  8    Gandalf   1.7         7/9/99   Miloslav Metelka 
 *  7    Gandalf   1.6         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         5/7/99   Miloslav Metelka improved setChar.Attr.()
 *  5    Gandalf   1.4         5/5/99   Miloslav Metelka 
 *  4    Gandalf   1.3         4/22/99  Miloslav Metelka 
 *  3    Gandalf   1.2         4/8/99   Miloslav Metelka 
 *  2    Gandalf   1.1         3/23/99  Miloslav Metelka 
 *  1    Gandalf   1.0         3/18/99  Miloslav Metelka 
 * $
 */

