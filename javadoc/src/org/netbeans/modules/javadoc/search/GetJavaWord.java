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

import org.openide.windows.TopComponent;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;

import javax.swing.JEditorPane;

/*
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;
*/
/** Tries to find actual focused java word.

 @author Petr Hrebejk
*/

class GetJavaWord extends Object {

  static String getCurrentJavaWord() {


    /*
    if ( TopComponent.getRegistry().getActivated() == null )
      return null;
    */
    Node[] n = TopComponent.getRegistry ().getActivatedNodes ();

    if (n.length == 1) {
      EditorCookie ec = (EditorCookie) n[0].getCookie (EditorCookie.class);
      if (ec != null) {
        JEditorPane[] panes = ec.getOpenedPanes ();
        if ( panes == null )
          return null;
        if (panes.length > 0) {
          int cursor = panes[0].getCaret ().getDot ();
          String selection = panes[0].getSelectedText ();
         
          if ( selection != null && selection.length() > 0 )
            return selection;
          else {
           
            String text = panes[0].getText();
            if ( text == null )
              return null;
            int pos = panes[0].getCaretPosition();

            if ( pos < 0 || pos >= text.length() )
              return null;

            int bix, eix;

            for( bix = Character.isJavaIdentifierPart( text.charAt( pos ) ) ? pos : --pos; 
                 bix >= 0 && Character.isJavaIdentifierPart( text.charAt( bix ) ); bix-- );
            for( eix = pos; eix < text.length() && Character.isJavaIdentifierPart( text.charAt( eix )); eix++ );        
       
            return bix == eix ? null : text.substring( bix + 1, eix  );
          }
        }
      }
    }

   return null;
  }
}


/* 
 * Log
 *  8    Gandalf   1.7         11/10/99 Petr Hrebejk    Ctrl+F1 now works in the
 *       first line of editor too.
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         7/30/99  Petr Hrebejk    Search uses 
 *       FileSystemCapabilities
 *  5    Gandalf   1.4         7/26/99  Petr Hrebejk    AutoComment tool 
 *       implemented
 *  4    Gandalf   1.3         7/12/99  Petr Hrebejk    New window system
 *  3    Gandalf   1.2         6/23/99  Petr Hrebejk    Better way to get 
 *       current editor word
 *  2    Gandalf   1.1         6/11/99  Petr Hrebejk    Better support for 
 *       search from editor; Enter for start searching
 *  1    Gandalf   1.0         5/27/99  Petr Hrebejk    
 * $ 
 */ 