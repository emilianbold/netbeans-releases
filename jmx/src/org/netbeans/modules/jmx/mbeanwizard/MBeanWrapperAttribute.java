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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.mbeanwizard;

import org.netbeans.modules.jmx.MBeanAttribute;
import org.netbeans.modules.jmx.WizardConstants;

/**
 *
 * @author an156382
 */
public class MBeanWrapperAttribute extends MBeanAttribute {

    private boolean selected;
    private String originalAccess;

    /** Creates a new instance of MBeanWrapperAttribute */
    public MBeanWrapperAttribute(Boolean isSelected, String attrName,
            String attrType, String attrAccess, String attrDescription) {
        super(attrName,attrType,attrAccess,attrDescription);
        this.selected = isSelected;
        this.originalAccess = attrAccess;
    }
    
    public boolean isSelected() {
        return this.selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getOriginalAccess() {
        return originalAccess;
    }
    
    public boolean isOriginalReadable() {
        return (WizardConstants.ATTR_ACCESS_READ_ONLY.equals(originalAccess) ||
                WizardConstants.ATTR_ACCESS_READ_WRITE.equals(originalAccess));
    }
    
    public boolean isOriginalWritable() {
        return (WizardConstants.ATTR_ACCESS_WRITE_ONLY.equals(originalAccess) ||
                WizardConstants.ATTR_ACCESS_READ_WRITE.equals(originalAccess));
    }
    
}
