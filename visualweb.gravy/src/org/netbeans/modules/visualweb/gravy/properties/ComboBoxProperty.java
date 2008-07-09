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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.gravy.properties;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;

/**
 * Can be used to set all properties which use JComboBox to change value.
 * <p>
 * Usage:<br>
 * <pre>
 *      PropertySheetOperator pso = new PropertySheetOperator("Properties of MyClass");
 *      PropertySheetTabOperator psto = new PropertySheetTabOperator(pso, "Properties");
 *      ComboBoxProperty pr = new ComboBoxProperty(psto, "Template");
 *      pr.setValue("True");
 *      pr.setValue(1);
 * </pre>
 *
 * @deprecated Use {@link Property} instead
 */
public class ComboBoxProperty extends Property {
    
    /** Waits for property with given name in specified container.
     * @param contOper ContainerOperator where to find property. It is
     * recommended to use {@link PropertySheetTabOperator}.
     * @param name property name
     * @deprecated Use {@link Property} instead
     */
    public ComboBoxProperty(ContainerOperator contOper, String name) {
        super(contOper, name);
    }
    
    /** Starts editing the property and waits for JComboBox
     * @return JComboBoxOperator of property combo box 
     * @deprecated Use {@link #setValue} to change property value
     */
    public JComboBoxOperator comboBox() {
        throw new JemmyException("Don't use this! Property sheet uses JTable instead of SheetButton.");
        /*
        startEditing();
        return new JComboBoxOperator(contOper);
         */
    }
    
    /** Sets value of the property. It makes property editable, finds
     * JComboBox and selects specified item.
     * @param value item to be selected
     */
    /*public void setValue(String value) {
        JComboBoxOperator comboOper = comboBox();
        comboOper.setSelectedItem(value);
    }*/
    
    /** Sets value of the property. It makes property editable, finds
     * JComboBox and selects index-th item.
     * @param index index of item to be selected (Start at 0)
     */
    /*public void setValue(int index) {
        JComboBoxOperator comboOper = comboBox();
        comboOper.setSelectedIndex(index);
    }*/
}
