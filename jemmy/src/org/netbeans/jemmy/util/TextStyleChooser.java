/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.util;

import java.awt.Color;

import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.netbeans.jemmy.operators.JTextComponentOperator.TextChooser;

/**
 * Defines searching criteria for <code>javax.swing.text.StyledDocument</code>
 * <a href="JTextComponentOperator.java">JTextComponentOperator.getPositionByText(String, JTextComponentOperator.TextChooser, int)</a>.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public class TextStyleChooser extends AbstractTextStyleChooser {
    Boolean bold = null;
    Boolean italic = null;
    Boolean strike = null;
    Boolean understrike = null;
    Integer fontSize = null;
    String fontFamily = null;
    Integer alignment = null;
    Color background = null;
    Color foreground = null;
    /**
     * Constructor.
     */
    public TextStyleChooser() {
	super();
    }
    /**
     * Adds boldness checking to the criteria.
     * @param bold Specifies if font needs to be bold.
     */
    public void setBold(boolean bold) {
	this.bold = bold ? Boolean.TRUE : Boolean.FALSE;
    }
    /**
     * Removes boldness checking from the criteria.
     */
    public void unsetBold() {
	this.bold = null;
    }
    /**
     * Adds italic style checking to the criteria.
     * @param italic Specifies if font needs to be italic.
     */
    public void setItalic(boolean italic) {
	this.italic = italic ? Boolean.TRUE : Boolean.FALSE;
    }
    /**
     * Removes italic style checking from the criteria.
     */
    public void unsetItalic() {
	this.italic = null;
    }
    /**
     * Adds strikeness checking to the criteria.
     * @param strike Specifies if font needs to be striked.
     */
    public void setStrike(boolean strike) {
	this.strike = strike ? Boolean.TRUE : Boolean.FALSE;
    }
    /**
     * Removes strikeness checking from the criteria.
     */
    public void unsetStrike() {
	this.strike = null;
    }
    /**
     * Adds understrikeness checking to the criteria.
     * @param understrike Specifies if font needs to be understriked.
     */
    public void setUnderstrike(boolean understrike) {
	this.understrike = understrike ? Boolean.TRUE : Boolean.FALSE;
    }
    /**
     * Removes understrikeness checking from the criteria.
     */
    public void unsetUnderstrike() {
	this.understrike = null;
    }
    /**
     * Adds font size checking to the criteria.
     * @param fontSize Specifies a font size.
     */
    public void setFontSize(int fontSize) {
	this.fontSize = new Integer(fontSize);
    }
    /**
     * Removes font size checking from the criteria.
     */
    public void unsetFontSize() {
	this.fontSize = null;
    }
    /**
     * Adds alignment checking to the criteria.
     * @param alignment Specifies a text alignment.
     */
    public void setAlignment(int alignment) {
	this.alignment = new Integer(alignment);
    }
    /**
     * Removes alignment checking from the criteria.
     */
    public void unsetAlignment() {
	this.alignment = null;
    }
    /**
     * Adds font family checking to the criteria.
     * @param fontFamily Specifies a font family.
     */
    public void setFontFamily(String fontFamily) {
	this.fontFamily = fontFamily;
    }
    /**
     * Removes font family checking from the criteria.
     */
    public void unsetFontFamily() {
	this.fontFamily = null;
    }
    /**
     * Adds backgroung color checking to the criteria.
     * @param background Specifies a background color.
     */
    public void setBackground(Color background) {
	this.background = background;
    }
    /**
     * Removes backgroung color checking from the criteria.
     */
    public void unsetBackground() {
	this.background = null;
    }
    /**
     * Adds foregroung color checking to the criteria.
     * @param foreground Specifies a foreground color.
     */
    public void setForeground(Color foreground) {
	this.foreground = foreground;
    }
    /**
     * Removes foregroung color checking from the criteria.
     */
    public void unsetForeground() {
 	this.foreground = null;
    }

    public boolean checkElement(StyledDocument doc, Element element, int offset) {
	if(bold != null) {
	    if(StyleConstants.isBold(element.getAttributes()) != bold.booleanValue()) {
		return(false);
	    }
	}
	if(italic != null) {
	    if(StyleConstants.isItalic(element.getAttributes()) != italic.booleanValue()) {
		return(false);
	    }
	}
	if(strike != null) {
	    if(StyleConstants.isStrikeThrough(element.getAttributes()) != strike.booleanValue()) {
		return(false);
	    }
	}
	if(understrike != null) {
	    if(StyleConstants.isUnderline(element.getAttributes()) != understrike.booleanValue()) {
		return(false);
	    }
	}
	if(fontSize != null) {
	    if(StyleConstants.getFontSize(element.getAttributes()) != fontSize.intValue()) {
		return(false);
	    }
	}
	if(alignment != null) {
	    if(StyleConstants.getAlignment(element.getAttributes()) != alignment.intValue()) {
		return(false);
	    }
	}
	if(fontFamily != null) {
	    if(!StyleConstants.getFontFamily(element.getAttributes()).equals(fontFamily)) {
		return(false);
	    }
	}
	if(background != null) {
	    if(!StyleConstants.getBackground(element.getAttributes()).equals(background)) {
		return(false);
	    }
	}
	if(foreground != null) {
	    if(!StyleConstants.getForeground(element.getAttributes()).equals(foreground)) {
		return(false);
	    }
	}
	return(true);
    }

    public String getDescription() {
	String result = "";
	if(bold != null) {
	    result = result + (bold.booleanValue() ? "" : "not ") + "bold, ";
	}
	if(italic != null) {
	    result = result + (italic.booleanValue() ? "" : "not ") + "italic, ";
	}
	if(strike != null) {
	    result = result + (strike.booleanValue() ? "" : "not ") + "strike, ";
	}
	if(understrike != null) {
	    result = result + (understrike.booleanValue() ? "" : "not ") + "understrike, ";
	}
	if(fontSize != null) {
	    result = result + fontSize.toString() + " size, ";
	}
	if(alignment != null) {
	    result = result + alignment.toString() + " alignment, ";
	}
	if(fontFamily != null) {
	    result = result + "\"" + fontFamily + "\" font family, ";
	}
	if(background != null) {
	    result = result + background.toString() + " background, ";
	}
	if(foreground != null) {
	    result = result + foreground.toString() + " foreground, ";
	}
	if(result.equals("")) {
	    result =  "any, ";
	}
	return(result.substring(0, result.length() - 2) + " font");
    }
}

