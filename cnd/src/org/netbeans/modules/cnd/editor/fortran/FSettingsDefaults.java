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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.editor.fortran;

import java.awt.Font;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.KeyStroke;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.ExtSettingsDefaults;

/**
* Default settings values for Fortran.
*
*/

public class FSettingsDefaults extends ExtSettingsDefaults {

    public static final Boolean defaultWordMatchMatchCase = Boolean.TRUE;

    // Formatting
    public static final Boolean defaultFormatSpaceAfterComma = Boolean.TRUE;
    public static final Boolean defaultFreeFormat = Boolean.TRUE;

    //maximum nmber of columns allowed in a line
    public static final int maximumTextWidth = 132;    //for f95

    public static final Acceptor defaultIndentHotCharsAcceptor
	= new Acceptor() {
		public boolean accept(char ch) {
		    switch (ch) {
		    case '\n':
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

    public static Map getFAbbrevMap() {
	Map fAbbrevMap = new TreeMap();

	fAbbrevMap.put("acc", "access= ");           //NOI18N
	fAbbrevMap.put("act", "action= ");           //NOI18N
	fAbbrevMap.put("adv", "advance= ");          //NOI18N
	fAbbrevMap.put("alloc", "allocate ");        //NOI18N
	fAbbrevMap.put("apos", "apostrophe ");       //NOI18N
	fAbbrevMap.put("assign",  "assignment ");    //NOI18N
	fAbbrevMap.put("bck", "backspace ");         //NOI18N
	fAbbrevMap.put("bld", "blockdata ");         //NOI18N
	fAbbrevMap.put("chr", "character ");         //NOI18N
	fAbbrevMap.put("clo", "close ");             //NOI18N
	fAbbrevMap.put("cyc", "cycle ");             //NOI18N
	fAbbrevMap.put("deall", "deallocate ");      //NOI18N
	fAbbrevMap.put("dim", "dimension ");         //NOI18N
	fAbbrevMap.put("dbl", "double ");            //NOI18N
	fAbbrevMap.put("dblp",  "doubleprecision "); //NOI18N
	fAbbrevMap.put("elem", "elemental ");        //NOI18N
	fAbbrevMap.put("elif", "elseif ");           //NOI18N
	fAbbrevMap.put("elwh", "elsewhere ");        //NOI18N
	fAbbrevMap.put("eblk", "endblock ");         //NOI18N
	fAbbrevMap.put("ebld", "endblockdata ");     //NOI18N
	fAbbrevMap.put("eqv", "equivalance ");       //NOI18N
	fAbbrevMap.put("func", "function ");         //NOI18N
	fAbbrevMap.put("impl",  "implicit ");        //NOI18N
	fAbbrevMap.put("inc", "include ");           //NOI18N
	fAbbrevMap.put("int", "integer ");           //NOI18N
	fAbbrevMap.put("mod",  "module ");           //NOI18N
	fAbbrevMap.put("param", "parameter ");       //NOI18N
	fAbbrevMap.put("proc", "procedure ");        //NOI18N
	fAbbrevMap.put("prog", "program ");          //NOI18N
	fAbbrevMap.put("pub","public ");             //NOI18N
	fAbbrevMap.put("recur", "recursive ");       //NOI18N
	fAbbrevMap.put("rew", "rewind ");            //NOI18N
	fAbbrevMap.put("sc", "selectcase ");         //NOI18N
	fAbbrevMap.put("subr","subroutine ");        //NOI18N
	fAbbrevMap.put("wh", "where ");              //NOI18N

	return fAbbrevMap;
    }
    public static final MultiKeyBinding[] defaultKeyBindings
	= new MultiKeyBinding[] {
	    new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_F, 
				InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK ),
				BaseKit.formatAction)
		};

    static class FTokenColoringInitializer
	extends SettingsUtil.TokenColoringInitializer {

	Font boldFont = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
	Font italicFont = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);

	Settings.Evaluator boldSubst = 
	    new SettingsUtil.FontStylePrintColoringEvaluator(Font.BOLD);

	Settings.Evaluator italicSubst = 
	    new SettingsUtil.FontStylePrintColoringEvaluator(Font.ITALIC);

	Settings.Evaluator lightGraySubst = 
	    new SettingsUtil.ForeColorPrintColoringEvaluator(Color.lightGray);

	Coloring commentColoring = 
	    new Coloring(italicFont, Coloring.FONT_MODE_APPLY_STYLE,
						new Color(115, 115, 115), null);

	Coloring numbersColoring = new Coloring(null, new Color(120, 0, 0), null);

	public FTokenColoringInitializer() {
            super(FTokenContext.context);
	}

	public Object getTokenColoring(TokenContextPath tokenContextPath,
				       TokenCategory tokenIDOrCategory, 
				       boolean printingSet) {
	    int id = tokenIDOrCategory.getNumericID();
	    if (!printingSet) {
		// if (id == FTokenContext.TC_NUMERIC_LITERALS.getNumericID())
		if (id == FTokenContext.NUMERIC_LITERALS_ID)
		    return numbersColoring;

		else if ( (id == FTokenContext.WHITESPACE_ID) ||
			  (id == FTokenContext.IDENTIFIER_ID) ||
			  (id == FTokenContext.OPERATORS_ID) ||
			  (id == FTokenContext.SPECIAL_CHARACTERS_ID) )
		    return SettingsDefaults.emptyColoring;

		else if (id == FTokenContext.ERRORS_ID)
		    return new Coloring(null, Color.white, Color.red);

		else if ( (id == FTokenContext.KEYWORDS_ID) ||
			  (id == FTokenContext.KEYWORD_OPERATORS_ID) )
		    return new Coloring(boldFont, 
					Coloring.FONT_MODE_APPLY_STYLE,
					new Color(0, 0, 153), null);
		else if (id == FTokenContext.LINE_COMMENT_ID)
		    return commentColoring;

		else if (id == FTokenContext.STRING_LITERAL_ID)
		    return new Coloring(null, new Color(153, 0, 107), null);
	    }
	    else { // printing set
		if (id == FTokenContext.LINE_COMMENT_ID)
		    return lightGraySubst; // print fore color will be gray
		else
		    return SettingsUtil.defaultPrintColoringEvaluator;
	    }

	    return null;
	}

    } //FTokenColoringInitializer

}//FSettingsDefaults
