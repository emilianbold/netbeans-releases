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

package gui.window;

import java.awt.Component;
import java.awt.Container;

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class PaletteComponentOperator extends TopComponentOperator {
    /** Creates a new instance of PaletteComponentOperator */
    public PaletteComponentOperator() {
        super(waitTopComponent(null,null,0,new PaletteTopComponentChooser()));
    }
    
    /** invokes default palette
     * @return PaletteComponentOperator for invoked palette */    
    public static PaletteComponentOperator invoke() {
         new Action("Window|Palette",null).perform(); // NOI18N
        return new PaletteComponentOperator();
    }
            
    public void expandCategory(String categoryName) throws Exception {
        JCheckBoxOperator cat = new JCheckBoxOperator(this,categoryName);
        cat.pushNoBlock();
    }
    
    public void collapseCategory(String categoryName) throws Exception {
        JCheckBoxOperator cat = new JCheckBoxOperator(this,categoryName);
        cat.pushNoBlock();        
    }
    
    public JListOperator getCategoryListOperator(String categoryName) {
        //Find Checkbox operator at first
        JCheckBoxOperator cbo =  new JCheckBoxOperator(this,categoryName);
        //Parent component for this checkbox
        Container cbp = cbo.getParent();
        //Find List in this container
        ContainerOperator cto = new ContainerOperator(cbp);
        Component expected = cto.findSubComponent(new CategoryListChooser());
        
        return new JListOperator((javax.swing.JList) expected);
    }
    
    private static class CategoryListChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return (comp.getClass().getName().equals("org.netbeans.modules.palette.ui.CategoryList"));
        }

        public String getDescription() {
            return "Category List Component";
        }
    }
    
    private static class PaletteTopComponentChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return(comp.getClass().getName().equals("org.netbeans.spi.palette.PaletteTopComponent"));
        }
        public String getDescription() {
            return("Any PaletteTopComponent");
        }
    }    
}
