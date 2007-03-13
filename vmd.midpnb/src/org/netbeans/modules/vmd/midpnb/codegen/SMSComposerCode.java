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
 */

package org.netbeans.modules.vmd.midpnb.codegen;

import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.codegen.Parameter;
import org.netbeans.modules.vmd.api.model.DesignComponent;

/**
 *
 * @author Karol Harezlak
 */
public class SMSComposerCode {
    
    public static final String PARAM_BGK_COLOR = "BGColor"; //NOI18N
    
    
    /** Creates a new instance of SMSComposerCodeGen */
    public SMSComposerCode() {
    }
    
    
    public static Parameter createBackgroungColorParameter() {
        return null;
    }
    
    
    private static class BackgroungColorParameter implements Parameter {
        
        public String getParameterName() {
            return PARAM_BGK_COLOR;
        }

        public int getParameterPriority() {
            return -1;
        }

        public void generateParameterCode(DesignComponent component, MultiGuardedSection section, int index) {
        }

        public boolean isRequiredToBeSet(DesignComponent component) {
            return true;
        }

        public int getCount(DesignComponent component) {
            return 1;
        }

        public boolean isRequiredToBeSet(DesignComponent component, int index) {
            return true;
        }
        
    }
    
    
}

