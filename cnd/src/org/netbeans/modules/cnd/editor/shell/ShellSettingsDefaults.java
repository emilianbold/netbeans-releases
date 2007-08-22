/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.editor.shell;

import java.awt.Font;
import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.editor.*;
import org.netbeans.editor.Settings;
import org.netbeans.editor.ext.ExtSettingsDefaults;

/**
* Default settings values for Fortran.
*
*/

public class ShellSettingsDefaults extends ExtSettingsDefaults {

  public static final Boolean defaultWordMatchMatchCase = Boolean.TRUE;


  public static final Acceptor defaultIndentHotCharsAcceptor
    = new Acceptor() {
        public boolean accept(char ch) {
          switch (ch) {
            case '}':
            return true;
          }

          return false;
        }
      };


  public static final String defaultWordMatchStaticWords
  = "Exception IntrospectionException FileNotFoundException IOException" //NOI18N
    + " ArrayIndexOutOfBoundsException ClassCastException ClassNotFoundException" //NOI18N
    + " CloneNotSupportedException NullPointerException NumberFormatException" //NOI18N
    + " SQLException IllegalAccessException IllegalArgumentException"; //NOI18N

  public static Map getAbbrevMap() {
    Map abbrevMap = new TreeMap();

    abbrevMap.put("ec", "echo "); //NOI18N
    abbrevMap.put("ex", "exit "); //NOI18N

	  return abbrevMap;
  }

  // THIS MUST BE MOVED INTO LAYER 
  static class ShellTokenColoringInitializer
  extends SettingsUtil.TokenColoringInitializer {

    Font boldFont = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
    Font italicFont = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);
    Settings.Evaluator boldSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.BOLD);
    Settings.Evaluator italicSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.ITALIC);
    Settings.Evaluator lightGraySubst = new SettingsUtil.ForeColorPrintColoringEvaluator(Color.lightGray);
    Settings.Evaluator darkGraySubst = new SettingsUtil.ForeColorPrintColoringEvaluator(Color.darkGray);

    Coloring commentColoring = new Coloring(italicFont, Coloring.FONT_MODE_APPLY_STYLE, new Color(115, 115, 115), null);
    Coloring shellCommandColoring = new Coloring(boldFont, Coloring.FONT_MODE_APPLY_STYLE, new Color(0, 100, 164), null);
    Coloring numbersColoring = new Coloring(null, new Color(120, 0, 0), null);

    public ShellTokenColoringInitializer() {
            super(ShellTokenContext.context);
    }

    public Object getTokenColoring(TokenContextPath tokenContextPath,
    TokenCategory tokenIDOrCategory, boolean printingSet) {
      int id = tokenIDOrCategory.getNumericID();
      if (!printingSet) {
        if ( (id == ShellTokenContext.WHITESPACE.getNumericID()) ||
             (id == ShellTokenContext.IDENTIFIER.getNumericID()) ||
             (id == ShellTokenContext.TC_MACRO_OPERATORS.getNumericID()) )
          return SettingsDefaults.emptyColoring;

        else if (id == ShellTokenContext.TC_ERRORS.getNumericID())
          return new Coloring(null, Color.white, Color.red);

	// What colors do we use for targets and macros?
	// Let's use character literal for targets
        else if (id == ShellTokenContext.TC_GLOBAL.getNumericID())
          return new Coloring(boldFont, Coloring.FONT_MODE_APPLY_STYLE,
                              new Color(0, 111, 0), null);

        else if (id == ShellTokenContext.TC_MACROS.getNumericID())
          return new Coloring(boldFont, Coloring.FONT_MODE_APPLY_STYLE, new Color(120, 0, 0), null);

        else if (id == ShellTokenContext.LINE_COMMENT.getNumericID())
          return commentColoring;

        else if (id == ShellTokenContext.SHELL_COMMAND.getNumericID())
          return shellCommandColoring;

        else if (id == ShellTokenContext.STRING_LITERAL.getNumericID())
          return new Coloring(null, new Color(153, 0, 107), null);
      }
      else { // printing set
        if (id == ShellTokenContext.LINE_COMMENT.getNumericID())
		      return lightGraySubst; // print fore color will be gray
        else if (id == ShellTokenContext.SHELL_COMMAND.getNumericID())
		      return darkGraySubst; // print fore color will be gray
        else
          return SettingsUtil.defaultPrintColoringEvaluator;
      }

      return null;
    }

  } //ShellTokenColoringInitializer

}//ShellSettingsDefaults
