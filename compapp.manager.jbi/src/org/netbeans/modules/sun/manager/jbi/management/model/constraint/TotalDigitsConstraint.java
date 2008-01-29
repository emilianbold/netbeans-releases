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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.sun.manager.jbi.management.model.constraint;

import org.openide.util.NbBundle;

/**
 * Total digits constraint for numeric type.
 *
 * @author jqian
 */
public class TotalDigitsConstraint implements JBIComponentConfigurationConstraint {

    private int totalDigits;

    TotalDigitsConstraint(int totalDigits) {
        if (totalDigits <= 0) {
            String msg = NbBundle.getMessage(getClass(),
                    "MSG_ILLEGAL_TOTAL_DIGITS", totalDigits); // NOI18N
            throw new IllegalArgumentException(msg);
        }
        this.totalDigits = totalDigits;
    }
    
    public int getValue() {
        return totalDigits;
    }

    public String validate(Object value) {
        if (value == null) {
            return NbBundle.getMessage(getClass(), "MSG_NULL_VALUE"); // NOI18N
        }
                
        double doubleValue = Double.parseDouble(value.toString());
        
        int n = 0;
        double i = doubleValue;
        while (i != Math.round(i)) {
            i *= 10;
            n++;
        }
        // doubleValue = i Ã 10^-n
        // Requirement: |i| < 10^{value} and 0 <= n <= {value}.
        if (Math.abs(i) < Math.pow(10, totalDigits) && 
                0 <= n && 
                n <= totalDigits) {
            return null;
        } else {
            return NbBundle.getMessage(getClass(),
                    "MSG_EXCEED_TOTAL_DIGITS", // NOI18N
                    doubleValue, totalDigits);
        }
    }
}
