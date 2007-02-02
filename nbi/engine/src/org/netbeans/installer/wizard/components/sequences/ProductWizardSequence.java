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
 *
 * $Id$
 */
package org.netbeans.installer.wizard.components.sequences;

import java.util.List;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.WizardSequence;


public class ProductWizardSequence extends WizardSequence {
    private Product product;
    
    public ProductWizardSequence(final Product product) {
        this.product = product;
    }
    
    public void executeForward() {
        childWizard = getWizard().createSubWizard(
                product, 
                product.getClassLoader(), 
                product.getWizardComponents(), 
                -1);
        
        childWizard.getContext().put(product);
        childWizard.next();
    }
    
    public void executeBackward() {
        childWizard = getWizard().createSubWizard(
                product, 
                product.getClassLoader(), 
                product.getWizardComponents(), 
                product.getWizardComponents().size());
        
        childWizard.getContext().put(product);
        childWizard.previous();
    }
    
    public boolean canExecuteForward() {
        if (product.isLogicDownloaded()) {
            List<WizardComponent> components = product.getWizardComponents();
            
            for (int i = 0; i < components.size(); i++) {
                WizardComponent component = components.get(i);
                
                // if the component can be executed forward the whole sequence 
                // can be executed as well
                if (component.canExecuteForward()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean canExecuteBackward() {
        if (product.isLogicDownloaded()) {
            List<WizardComponent> components = product.getWizardComponents();
            
            for (int i = components.size() - 1; i > -1; i--) {
                WizardComponent component = components.get(i);
                
                // if the component can be executed backward the whole sequence can 
                // be executed as well
                if (component.canExecuteBackward()) {
                    return true;
                }
                
                // if the currently examined component is a point of no return and 
                // it cannot be executed (since we passed the previous statement) - 
                // we have no previous component
                if (component.isPointOfNoReturn()) {
                    return false;
                }
            }
        }
        
        return false;
    }
    
    public boolean isPointOfNoReturn() {
        if (product.isLogicDownloaded()) {
            List<WizardComponent> components = product.getWizardComponents();
            
            if (childWizard != null) {
                for (int i = 0; i < components.size(); i++) {
                    if (components.get(i).isPointOfNoReturn() && (i < childWizard.getIndex())) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
}