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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.ui.highlight;

import java.util.EventListener;
import java.util.Set;
import org.netbeans.modules.xml.xam.Component;

/**
 * A Highlighted object has the ability to highlight its visual
 * representation based on a particular Highlight object.
 *
 * @author Nathan Fiedler
 */
public interface Highlighted extends EventListener {

    /**
     * Get the set of components this listener is interested in.
     *
     * @return  set of component references (empty if none).
     */
    Set<Component> getComponents();

    /**
     * The highlight has been added to the highlight manager. The listener
     * should make the highlight visible in some way.
     *
     * @param  hl  Highlight to be shown.
     */
    void highlightAdded(Highlight hl);

    /**
     * The highlight has been removed from the highlight manager. The listener
     * should hide the highlight from the user.
     *
     * @param  hl  Highlight to be hidden.
     */
    void highlightRemoved(Highlight hl);
}
