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

import javax.swing.text.AttributeSet;
import com.netbeans.editor.GuardedDocument;
import com.netbeans.editor.Syntax;
import com.netbeans.ide.text.NbDocument;

/** 
* BaseDocument extension managing the readonly blocks of text
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorDocument extends GuardedDocument
implements NbDocument.PositionBiasable, NbDocument.WriteLockable {

  NbEditorDocument(Class kitClass, Syntax syntax) {
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
        if (((Boolean)val).booleanValue() == true) {
          super.setCharacterAttributes(offset, length, guardedSet, replace);
        }
        if (((Boolean)val).booleanValue() == false) {
          super.setCharacterAttributes(offset, length, unguardedSet, replace);
        }
      }
    }
  }


  
}

/*
 * Log
 *  4    Gandalf   1.3         4/22/99  Miloslav Metelka 
 *  3    Gandalf   1.2         4/8/99   Miloslav Metelka 
 *  2    Gandalf   1.1         3/23/99  Miloslav Metelka 
 *  1    Gandalf   1.0         3/18/99  Miloslav Metelka 
 * $
 */

