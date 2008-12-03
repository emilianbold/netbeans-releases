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

package org.netbeans.modules.hudson.ui.nodes;

import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 * Displays tool tip with specified action
 *
 * @author Michal Mocnak
 */
public class TooltipNode extends AbstractNode {
    
    private Action action;
    
    /** Creates a new instance of TooltipNode */
    public TooltipNode(String text, final Action action) {
        super(Children.LEAF);
        setDisplayName((text.length() > 50) ? (text.substring(0, 50) + "...") : (text));
        setShortDescription(text);
        setIconBaseWithExtension("org/netbeans/modules/hudson/ui/resources/suggestion.gif");
        
        this.action = action;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {};
    }
    
    @Override
    public Action getPreferredAction() {
        return action;
    }
}