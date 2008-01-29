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

import java.util.List;

/**
 * Factory class to create various types of 
 * <code>JBIComponentConfigurationConstraint</code>s.
 * 
 * @author jqian
 */
public class JBIComponentConfigurationConstraintFactory {

    public static final String ENUMERATION     = "enumeration";        // NOI18N
    static final String FRACTION_DIGITS = "fractionDigits";     // NOI18N
    static final String LENGTH          = "length";             // NOI18N
    static final String MAX_EXCLUSIVE   = "maxExclusive";       // NOI18N
    static final String MAX_INCLUSIVE   = "maxInclusive";       // NOI18N
    static final String MAX_LENGTH      = "maxLength";          // NOI18N
    static final String MIN_EXCLUSIVE   = "minExclusive";       // NOI18N
    static final String MIN_INCLUSIVE   = "minInclusive";       // NOI18N
    static final String MIN_LENGTH      = "minLength";          // NOI18N
    static final String PATTERN         = "pattern";            // NOI18N
    static final String TOTAL_DIGITS    = "totalDigits";        // NOI18N
    static final String WHITESPACE      = "whiteSpace";         // NOI18N

    public static JBIComponentConfigurationConstraint newConstraint(
            String facet, Object value) {
        if (ENUMERATION.equals(facet)) {
            return new EnumerationConstraint((List<String>)value);
        } else if (FRACTION_DIGITS.equals(facet)) {
            int intValue = Integer.parseInt((String)value);
            return new FractionDigitsConstraint(intValue);
        } else if (LENGTH.equals(facet)) {
            int intValue = Integer.parseInt((String)value);
            return new LengthConstraint(intValue);
        } else if (MIN_EXCLUSIVE.equals(facet)) {
            double doubleValue = Double.parseDouble((String)value);
            return new MinExclusiveConstraint(doubleValue);
        } else if (MIN_INCLUSIVE.equals(facet)) {
            double doubleValue = Double.parseDouble((String)value);
            return new MinInclusiveConstraint(doubleValue);
        } else if (MAX_EXCLUSIVE.equals(facet)) {
            double doubleValue = Double.parseDouble((String)value);
            return new MaxExclusiveConstraint(doubleValue);
        } else if (MAX_INCLUSIVE.equals(facet)) {
            double doubleValue = Double.parseDouble((String)value);
            return new MaxInclusiveConstraint(doubleValue);
        } else if (MAX_LENGTH.equals(facet)) {
            int intValue = Integer.parseInt((String)value);
            return new MaxLengthConstraint(intValue);
        } else if (MIN_LENGTH.equals(facet)) {
            int intValue = Integer.parseInt((String)value);
            return new MinLengthConstraint(intValue);
        } else if (TOTAL_DIGITS.equals(facet)) {
            int intValue = Integer.parseInt((String)value);
            return new TotalDigitsConstraint(intValue);
        } else if (PATTERN.equals(facet)) {
            return new PatternConstraint((String)value);
//        } else if (WHITESPACE.equals(facet)) {
//            int intValue = Integer.parseInt(value);
//            return new WhiteSpaceConstraint(intValue);
        } else {
            throw new RuntimeException("Unsupported constraint facet: " + facet);
        }
    }
}