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
package org.netbeans.installer.wizard.components;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 *
 * @author Kirill Sorokin
 */
public class WizardSequence extends WizardComponent {
    protected Wizard childWizard;
    
    public void executeForward() {
        this.childWizard = getWizard().createSubWizard(
                getChildren(), -1);
        
        childWizard.next();
    }
    
    public void executeBackward() {
        this.childWizard = getWizard().createSubWizard(
                getChildren(), getChildren().size());
        
        childWizard.previous();
    }
    
    public void initialize() {
        // does nothing
    }
    
    public boolean canExecuteForward() {
        // whether a sequence can be executed completely depends on whether its 
        // children can be executed, thus we should run through them and check
        for (int i = 0; i < getChildren().size(); i++) {
            WizardComponent component = getChildren().get(i);
            
            // if the component can be executed forward the whole sequence can be 
            // executed as well
            if (component.canExecuteForward()) {
                return true;
            }
        }
        
        // if none of the components can be executed, it does not make sense to
        // execute the sequence as well
        return false;
    }
    
    public boolean canExecuteBackward() {
        // whether a sequence can be executed completely depends on whether its 
        // children can be executed, thus we should run through them and check
        for (int i = getChildren().size() - 1; i > -1; i--) {
            WizardComponent component = getChildren().get(i);
            
            // if the component can be executed backward the whole sequence can be 
            // executed as well
            if (component.canExecuteBackward()) {
                return true;
            }
            
            // if the currently examined component is a point of no return and it
            // cannot be executed (since we passed the previous statement) - we have
            // no previous component
            if (component.isPointOfNoReturn()) {
                return false;
            }
        }
        
        // if none of the components can be executed it does not make sense to
        // execute the sequence as well
        return false;
    }
    
    public boolean isPointOfNoReturn() {
        // if there is a point-of-no-return child and it has already been passed,
        // then the sequence of a point of no return, otherwise it's not
        if (childWizard != null) {
            for (int i = 0; i < getChildren().size(); i++) {
                if (getChildren().get(i).isPointOfNoReturn() && 
                        (i < childWizard.getIndex())) {
                    return true;
                }
            }
        }
        
        // otherwise, it's not
        return false;
    }
    
    public WizardUi getWizardUi() {
        // a sequence does not have a UI of its own, thus returning null
        return null;
    }
}