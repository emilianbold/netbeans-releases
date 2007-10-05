/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
	private final ResourceBundle bundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N
	
        // !PW FIXME needs localization (FYI not used in NB6.0 yet, so no rush).
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
		
        if(!Utils.strEquivalent(pmIdentifier, pmDescriptor.getPmIdentifier())) {
            return true;
        }

        if(!Utils.strEquivalent(pmVersion, pmDescriptor.getPmVersion())) {
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
