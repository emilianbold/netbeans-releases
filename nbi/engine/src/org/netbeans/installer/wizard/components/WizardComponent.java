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

import java.util.Properties;
import org.netbeans.installer.wizard.*;
import org.netbeans.installer.wizard.conditions.WizardCondition;

/**
 *
 * @author Kirill Sorokin
 */
public interface WizardComponent {
    public abstract void executeComponent(Wizard aWizard);
    
    public abstract void addChildComponent(WizardComponent aWizardComponent);
    
    public abstract boolean evaluateConditions();
    
    public abstract void addCondition(WizardCondition condition);
    
    public abstract boolean isActive();
    
    public abstract void setActive(boolean isActive); 
    
    public abstract String getProperty(String name);
    
    public abstract void setProperty(String name, String value);
    
    public abstract Properties getProperties();
}