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

package com.netbeans.developer.modules.javadoc.search;


import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;

/** Tries to find actual focused java word.

 @author Petr Hrebejk
*/


class GetJavaWord extends TextAction {

  /** Creates new TextTest */
  public GetJavaWord () {
    super( "temp" );
  }

  public void actionPerformed( java.awt.event.ActionEvent e) {};

  static String getCurrentJavaWord() {

    GetJavaWord gjw = new GetJavaWord();
    
    JTextComponent tc = gjw.getFocusedComponent();
    
    if ( tc != null ) {
          
      int selStart = tc.getSelectionStart();
      int selEnd = tc.getSelectionEnd();
      
      if ( selStart >= 0 && selEnd >= 0  && selStart != selEnd ) {
        try {
          return tc.getText( selStart, selEnd - selStart );
        }
        catch (javax.swing.text.BadLocationException ex ) {
        }
       }
       else {
       
        String text = tc.getText();
        int pos = tc.getCaretPosition();
              
        if ( pos < 0 )
          return null;

        int bix, eix;

        for( bix = pos; bix > 0 && Character.isJavaIdentifierPart( text.charAt( bix ) ); bix-- );
        for( eix = pos; eix < text.length() && Character.isJavaIdentifierPart( text.charAt( eix )); eix++ );        
       
        return bix == eix ? null : text.substring( bix + 1, eix  );
       }
    }  

    
    return null;
  }
	
}

/* 
 * Log
 *  1    Gandalf   1.0         5/27/99  Petr Hrebejk    
 * $ 
 */ 