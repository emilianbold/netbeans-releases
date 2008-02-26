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
 * BackgroundPositionData.java
 *
 * Created on October 21, 2004, 8:04 PM
 */

package org.netbeans.modules.css.visual.model;

import org.netbeans.modules.css2.editor.model.CssRuleContent;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;

/**
 * Data structure for the Background Position
 * @author  Winston Prakash
 * @version 1.0
 */
public class BackgroundPositionData {

    /**
     * Holds value of property horizontalUnit.
     */
    private String horizontalUnit = "px"; //NOI18N

    /**
     * Holds value of property verticalUnit.
     */
    private String verticalUnit = "px"; //NOI18N

    /**
     * Holds value of property verticalValue.
     */
    private String verticalValue = ""; //NOI18N

    /**
     * Holds value of property horizontalValue.
     */
    private String horizontalValue = "left"; //NOI18N

    /** Creates a new instance of BackgroundPositionData */
    public BackgroundPositionData() {
    }

    public void setBackgroundPosition(String bgPositionStr){
        StringTokenizer st = new StringTokenizer(bgPositionStr);
        if(bgPositionStr.indexOf(",") != -1){
            st = new StringTokenizer(bgPositionStr, ",");
        }else{
            st = new StringTokenizer(bgPositionStr);
        }
        // Horizontal Postion
        if(st.hasMoreTokens()){
            String token = st.nextToken();
            horizontalUnit = getUnit(token);
            horizontalValue = token.replaceAll(horizontalUnit,"");
        }
        if(st.hasMoreTokens()){
            String token = st.nextToken();
            verticalUnit = getUnit(token);
            verticalValue = token.replaceAll(verticalUnit,"");
        }
    }

    private String getUnit(String postionStr){
        DefaultComboBoxModel unitList = new BackgroundModel().getBackgroundPositionUnitList();
        for(int i=0; i< unitList.getSize(); i++){
            String unit = (String)unitList.getElementAt(i);
            if(postionStr.endsWith(unit)){
                return unit;
            }
        }
        return "";
    }

    /**
     * Setter for property horizontalUnit.
     * @param horizontalUnit New value of property horizontalUnit.
     */
    public void setHorizontalUnit(String horizontalUnit) {
        this.horizontalUnit = horizontalUnit;
    }

    public String getHorizontalUnit() {
        return this.horizontalUnit;
    }

    /**
     * Setter for property verticalUnit.
     * @param verticalUnit New value of property verticalUnit.
     */
    public void setVerticalUnit(String verticalUnit) {
        this.verticalUnit = verticalUnit;
    }

    public String getVerticalUnit() {
        return this.verticalUnit;
    }

    /**
     * Setter for property verticalValue.
     * @param verticalValue New value of property verticalValue.
     */
    public void setVerticalValue(String verticalValue) {
        this.verticalValue = verticalValue;
    }

    public String getVerticalValue() {
        return this.verticalValue;
    }

    /**
     * Setter for property horizontalValue.
     * @param horizontalValue New value of property horizontalValue.
     */
    public void setHorizontalValue(String horizontalValue) {
        this.horizontalValue = horizontalValue;
    }

    public String getHorizontalValue() {
        return this.horizontalValue;
    }

    public String toString(){
        String bgPosition = "";

        if (!(horizontalValue.equals("") || horizontalValue.startsWith(CssRuleContent.NOT_SET))){
            bgPosition += horizontalValue;
            if(Utils.isInteger(horizontalValue)){
                bgPosition += horizontalUnit;
            }
        }

        if (!(verticalValue.equals("") || verticalValue.startsWith(CssRuleContent.NOT_SET))){
            if(bgPosition.equals("")){
                bgPosition = "left" + " " + verticalValue; //NOI18N
            }else{
                bgPosition = bgPosition + " " + verticalValue;
            }
            if(Utils.isInteger(verticalValue)){
                bgPosition += verticalUnit;
            }
        }
        
        return bgPosition;
    }
}
