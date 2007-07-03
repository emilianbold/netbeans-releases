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
package org.netbeans.api.gsf;

import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;


/**
 * An item to be added to the code completion dialog
 *
 * @author Tor Norbye
 */
public interface CompletionProposal {
    /** The offset at which the completion item substitution should begin */
    int getAnchorOffset();

    Element getElement();

    String getName();

    String getInsertPrefix();

    String getSortText();

    String getLhsHtml();

    String getRhsHtml();

    ElementKind getKind();

    ImageIcon getIcon();

    Set<Modifier> getModifiers();
    
    /**
     * Return true iff this is a "smart" completion item - one that should be emphasized
     * (currently the IDE flushes these to the top and separates them with a line)
     */
    boolean isSmart();
    
    List<String> getInsertParams();

    /** The strings to be inserted to start and end a parameter list. Should be a String of length 2.
     * In Java we would expect {(,)}, and in Ruby it's either {(,)} or { ,}.
     */
    String[] getParamListDelimiters();
}
