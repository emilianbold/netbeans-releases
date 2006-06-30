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
 * Makes easier to implement searching criteria for <code>javax.swing.text.StyledDocument</code>
 * <code>JTextComponentOperator.getPositionByText(String, JTextComponentOperator.TextChooser, int)</code>.
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
     * @param doc a styled document to be searched.
     * @param element an element to be checked.
     * @param offset checked position.
     * @return true if position fits the criteria.
     */
    public abstract boolean checkElement(StyledDocument doc, Element element, int offset);
    public abstract String getDescription();
    public final boolean checkPosition(Document document, int offset) {
	return(checkElement(((StyledDocument)document), 
			    ((StyledDocument)document).getCharacterElement(offset),
			    offset));
    }
}
