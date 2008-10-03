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

/*
 * PropertyWithUnitData.java
 *
 * Created on October 21, 2004, 9:51 PM
 */

package org.netbeans.modules.css.visual.model;

import org.netbeans.modules.css.editor.model.CssRuleContent;
import javax.swing.DefaultComboBoxModel;

/**
 * Data Structure to hold the value of a property with Unit
 * @author  Winston Prakash
 * @version 1.0
 */
public class PropertyWithUnitData extends PropertyData{

    /**
     * Holds value of property unit.
     */
    protected String unit="px"; //NOI18N

    public void setData(String value){
        if(!((value == null) || value.equals(""))){
            if(Utils.isInteger(value)){
                setValue(value);
                setUnit("px"); //NOI18N
            }else{
                unit = getUnit(value);
                setValue(value.replaceAll(unit,"").trim());
            }
        }
    }

    private String getUnit(String positionStr){
        DefaultComboBoxModel unitList = new BorderModel().getWidthUnitList();
        for(int i=0; i< unitList.getSize(); i++){
            String unit = (String)unitList.getElementAt(i);
            if(positionStr.trim().endsWith(unit)){
                return unit;
            }
        }
        return "";
    }

    public void clear(){
      setValue(CssRuleContent.NOT_SET);
      setUnit("px"); //NOI18N
    }

    public String toString(){
        String valueString = super.toString();
        if(Utils.isFloat(valueString)){
            valueString += unit;
        }
        return valueString;
    }


    /**
     * Setter for property unit.
     * @param unit New value of property unit.
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Getter for property unit.
     * @return Value of property unit.
     */
    public java.lang.String getUnit() {
        return unit;
    }

}
