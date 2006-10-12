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
 */
/*
 * ListMapping.java
 *
 * Created on January 9, 2004, 3:05 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.List;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptor;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;


/** Class that associates a PmDescriptor element to a calculated string for display
 *  in a combobox or table.
 *  
 *  It is expected that the underlying bean referenced may be changed during this
 *  object's lifetime, and between calls to toString().
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class PmDescriptorMapping {
	
	// Standard resource bundle to use for non-property list fields
	private static final ResourceBundle bundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N
	
	private static final String formatPattern = "(id={0}, version={1})";
	
	private final PmDescriptor pmDescriptor;
    private String pmIdentifier;
    private String pmVersion;
	private String displayText;

	public PmDescriptorMapping(final PmDescriptor pmDesc) {
		pmDescriptor = pmDesc;
		displayText = null;
	}

	public String toString() {
		if(textOutOfDate()) {
			buildDisplayText();
		}
		
		return displayText;
	}
	
	private void buildDisplayText() {
        if(pmDescriptor != null) {
            pmIdentifier = pmDescriptor.getPmIdentifier();
            pmVersion = pmDescriptor.getPmVersion();
            Object [] args = { pmIdentifier, pmVersion };
            displayText = MessageFormat.format(formatPattern, args);
        } else {
            displayText = ""; // NOI18N
        }
	}
	
	private boolean textOutOfDate() {
		// Rebuild display Text if text is null or identifier or version field has changed.
		if(displayText == null) {
			return true;
		}
        
        if(pmDescriptor == null) {
            return false;
        }
		
        if(!Utils.strEquals(pmIdentifier, pmDescriptor.getPmIdentifier())) {
            return true;
        }

        if(!Utils.strEquals(pmVersion, pmDescriptor.getPmVersion())) {
            return true;
        }
		
		return false;
	}
	
	public PmDescriptor getPmDescriptor() {
		return pmDescriptor;
	}

    public boolean equals(Object obj) {
        boolean result = false;
        if(obj instanceof PmDescriptorMapping) {
            PmDescriptorMapping targetMapping = (PmDescriptorMapping) obj;
            result = (pmDescriptor == targetMapping.pmDescriptor);
        }
        return result;
    }
    
    public int hashCode() {
        int hashCode = 37;
        if(pmDescriptor != null) {
            if(Utils.notEmpty(pmDescriptor.getPmIdentifier())) {
                hashCode += 37*hashCode + pmDescriptor.getPmIdentifier().hashCode();
            }
            
            if(Utils.notEmpty(pmDescriptor.getPmVersion())) {
                hashCode += 37*hashCode + pmDescriptor.getPmVersion().hashCode();
            }
        }
        return hashCode;
    }
}
