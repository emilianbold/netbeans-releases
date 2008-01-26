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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.xslt.lib;

import java.awt.Point;
import javax.swing.JList;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;

/**
 *
 * @author ca@netbeans.org
 */

public class PaletteOperator extends TopComponentOperator {
    public static final String PALETTE_NAME = "Palette";
    
    public enum Groups {
        OPERATOR("Operator"),
        STRING("String"),
        NUMBER("Number"),
        BOOLEAN("Boolean"),
        NODES("Nodes");
        
        private String val;
        
        Groups(String val) {
            this.val = val;
        }
        
        public String getValue() {
            return val;
        }
    }
    
    /** Creates a new instance of PaletteOperator */
    public PaletteOperator() {
        super(PALETTE_NAME);
    }
    
    public void expandGroup(Groups group){
        JToggleButtonOperator groupBtn = new JToggleButtonOperator(this, group.getValue());
        if (!groupBtn.isSelected()){
            groupBtn.pushNoBlock();
            groupBtn.waitSelected(true);
        }
    }
    
    
    public void collapseGroup(Groups group){
        JToggleButtonOperator groupBtn = new JToggleButtonOperator(this, group.getValue());
        if (groupBtn.isSelected()){
            groupBtn.pushNoBlock();
            groupBtn.waitSelected(false);
        }
    }
    
    public JListOperator getGroupListOperator(Groups group) {
        Groups[] groups = Groups.values();
        JListOperator opList = null;
        
        for (int i = 0; i < groups.length; i++) {
            if (groups[i] == group) {
                JComponentOperator opComponent = Helpers.getComponentOperator(this, "org.netbeans.modules.palette.ui.CategoryList", null, i, 200);
                opList = new JListOperator((JList) opComponent.getSource());
                break;
            }
        }
        
        return opList;
    }
    
    public Point prepareNodeForClick(Groups group, String strItem) {
        expandGroup(group);
        
        JListOperator opList = getGroupListOperator(group);
        
        int itemIndex = opList.findItemIndex(new PaletteListItemChooser(strItem));
        
        opList.ensureIndexIsVisible(itemIndex);
        Helpers.waitNoEvent();
        
        Point p = opList.getClickPoint(itemIndex);
        return Helpers.getContainerPoint(opList, p, this);
    }
    
    private class PaletteListItemChooser implements JListOperator.ListItemChooser{
        String itemLabel;
        
        public PaletteListItemChooser(String itemLabel) {
            this.itemLabel = itemLabel;
        }
        
        public boolean checkItem(JListOperator oper, int index) {
            Object obj = oper.getModel().getElementAt(index);
            return itemLabel.equals(obj.toString());
        }
        
        public String getDescription() {
            return("Item equals to \"" + itemLabel + "\" string");
        }
    }
    
}
