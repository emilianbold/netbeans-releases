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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
