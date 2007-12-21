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
package org.netbeans.modules.bpel.nodes.navigator;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.DecorationProvider;
import org.netbeans.modules.xml.xam.Component;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class BpelNavigatorDecorationProvider<T extends Object> implements DecorationProvider<T> {
    private NodeType myNodeType;
    private T myComponent;
    
    private static List<TooltipManager> TOOLTIP_MANAGERS = new ArrayList<TooltipManager>();
    static {
        TOOLTIP_MANAGERS.add(new TooltipManager.CopyTooltipManager());
        TOOLTIP_MANAGERS.add(new TooltipManager.LongTooltipManager());
        TOOLTIP_MANAGERS.add(new TooltipManager.ShortTooltipManager());
    }
    
    public BpelNavigatorDecorationProvider(NodeType nodeType, T reference) {
        this.myNodeType = nodeType;
        this.myComponent = reference;
    }

    public String getTooltip(NodeType nodeType, T component) {
        TooltipManager tooltipManager = getTooltipManager(nodeType, component);
        return tooltipManager == null 
                ? BpelNode.EMPTY_STRING 
                : tooltipManager.getTooltip(nodeType, component);
    }
    
    private TooltipManager getTooltipManager(NodeType nodeType, Object component) {
        for (TooltipManager elem : TOOLTIP_MANAGERS) {
            if (elem.accept(nodeType, component)) {
                return elem;
            }
        }
        return null;
    }
}
