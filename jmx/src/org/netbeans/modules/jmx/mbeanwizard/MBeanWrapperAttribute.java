/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
