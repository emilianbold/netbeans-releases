/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.visual;

import java.util.Comparator;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.model.api.Declaration;

/**
 *
 * @author marekfukala
 */
public class PropertyUtils {

    public static final Comparator<PropertyDefinition> PROPERTY_DEFINITIONS_COMPARATOR = new Comparator<PropertyDefinition>() {
        @Override
        public int compare(PropertyDefinition pd1, PropertyDefinition pd2) {
            String pd1name = pd1.getName();
            String pd2name = pd2.getName();

            //sort the vendor spec. props below the common ones
            boolean d1vendor = Properties.isVendorSpecificPropertyName(pd1name);
            boolean d2vendor = Properties.isVendorSpecificPropertyName(pd2name);

            if (d1vendor && !d2vendor) {
                return +1;
            } else if (!d1vendor && d2vendor) {
                return -1;
            }

            return pd1name.compareTo(pd2name);
        }
    };
    
    public static final Comparator<Declaration> DECLARATIONS_COMPARATOR = new Comparator<Declaration>() {
        @Override
        public int compare(Declaration d1, Declaration d2) {
            String d1Name = d1.getProperty().getContent().toString();
            String d2Name = d2.getProperty().getContent().toString();

            //sort the vendor spec. props below the common ones
            boolean d1vendor = Properties.isVendorSpecificPropertyName(d1Name);
            boolean d2vendor = Properties.isVendorSpecificPropertyName(d2Name);

            if (d1vendor && !d2vendor) {
                return +1;
            } else if (!d1vendor && d2vendor) {
                return -1;
            }

            return d1Name.compareTo(d2Name);
        }
    };
}
