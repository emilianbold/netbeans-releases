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

package org.netbeans.modules.editor.options;

import java.awt.Insets;
import java.text.MessageFormat;
import java.util.StringTokenizer;
import java.util.ResourceBundle;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * A property editor for Insets class allowing to specify per cent value
 * represented as negative number.
 *
 * @author   Petr Nejedly    
 * @author   Petr Hamernik
 */
public class ScrollInsetsEditor extends java.beans.PropertyEditorSupport {

  // the bundle to use
  static ResourceBundle bundle = NbBundle.getBundle ( ScrollInsetsEditor.class);

  public boolean supportsCustomEditor () {
    return true;
  }

  public java.awt.Component getCustomEditor () {
    return new ScrollInsetsCustomEditor( this );
  }
  
//  public Object getValue() {
//    if( editorPanel != null ) {
//      try {
//        return editorPanel.getValue();
//      } catch( NumberFormatException e ) {
//        return super.getValue();
//        // PENDING
//      }
//    } else {
//      return super.getValue();
//    }
//  }
    
  /**
   * @return The property value as a human editable string.
   * <p>   Returns null if the value can't be expressed as an editable string.
   * <p>   If a non-null value is returned, then the PropertyEditor should
   *       be prepared to parse that string back in setAsText().
   */
  public String getAsText() {
    Insets val = (Insets) getValue();
    if (val == null)
      return null;
    else {
      return "[" + int2percent( val.top ) + ',' + int2percent( val.left ) + ',' +
        int2percent( val.bottom ) + ',' + int2percent( val.right ) + ']';
    }
  }
  
  /** Set the property value by parsing a given String.  May raise
  * java.lang.IllegalArgumentException if either the String is
  * badly formatted or if this kind of property can't be expressed
  * as text.
  * @param text  The string to be parsed.
  */
  public void setAsText(String text) throws IllegalArgumentException {
    int[] newVal = new int[4];
    int nextNumber = 0;

    StringTokenizer tuk = new StringTokenizer( text, "[] ,;", false ); // NOI18N
    while( tuk.hasMoreTokens() ) {
      String token = tuk.nextToken();
      if( nextNumber >= 4 ) badFormat();
      
      try {
        newVal[nextNumber++] = percent2int( token );
      } catch( NumberFormatException e ) {
        badFormat();
      }
    }

    // if less numbers are entered, copy the last entered number into the rest
    if( nextNumber != 4 ) {
      if( nextNumber > 0 ) {
        int copyValue = newVal[ nextNumber - 1 ];
        for( int i = nextNumber; i < 4; i++ ) newVal[i] = copyValue;
      }
    }
    setValue( new Insets( newVal[0], newVal[1], newVal[2], newVal[3] ) );
  }

  /** Always throws the new exception */
  private void badFormat() throws IllegalArgumentException {
    throw new IllegalArgumentException( bundle.getString("SIE_EXC_BadFormatValue" ) );
  } 
  
  private String int2percent( int i ) {
    if( i < 0 ) return( "" + (-i) + '%' );
    else return( "" + i );
  }

  private int percent2int( String val ) throws NumberFormatException {
    val = val.trim();
    if( val.endsWith( "%" ) ) {
      return -Integer.parseInt( val.substring( 0, val.length() - 1 ) );
    } else {
      return Integer.parseInt( val );      
    }
  }

  
}

/*
 * Log
 *  1    Gandalf-post-FCS1.0         3/10/00  Petr Nejedly    initial revision
 * $
 */
