/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
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
 * Makes easier to implement searching criteria for <code>javax.swing.text.StyledDocument</code>
 * @see org.netbeans.jemmy.operators.JTextComponentOperator#getPositionByText(java.lang.String, 
 * org.netbeans.jemmy.operators.JTextComponentOperator.TextChooser, int)
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public abstract class AbstractTextStyleChooser implements TextChooser {
    /**
     * Constructor.
     */
    public AbstractTextStyleChooser() {
    }
    /**
     * Should return true if position fulfils criteria.
     * @param doc 
     * @param Element 
     * @param offset
     */
    public abstract boolean checkElement(StyledDocument doc, Element element, int offset);
    public abstract String getDescription();
    /**
     * Simple invokes <code>checkElement(javax.swing.text.StyledDocument, 
     * javax.swing.text.Element, int)</code>
     */
    public final boolean checkPosition(Document document, int offset) {
	return(checkElement(((StyledDocument)document), 
			    ((StyledDocument)document).getCharacterElement(offset),
			    offset));
    }
}
