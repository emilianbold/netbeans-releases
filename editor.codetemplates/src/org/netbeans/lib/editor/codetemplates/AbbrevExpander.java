/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.codetemplates;

import javax.swing.text.JTextComponent;

/**
 * Abbreviation expander attempts to expand the present typed abbreviation
 * in the document.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface AbbrevExpander {
    
    /**
     * Attempt to expand the given abbreviation.
     * <br/>
     *
     * @param component non-null text component in which the abbreviation
     *  is being typed.
     * @param abbrevStartOffset &gt;=0 offset where the abbreviation starts
     *  in the document.
     * @param abbrev non-null abbreviation that was typed in the text and could
     *  possibly be expanded.
     * @return true if the abbreviation was expanded successfully or false
     *  if the abbreviation was not expanded.
     */
    boolean expand(JTextComponent component, int abbrevStartOffset, CharSequence abbrevText);

}
