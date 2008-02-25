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
 * PositionModel.java
 *
 * Created on October 29, 2004, 12:33 PM
 */

package org.netbeans.modules.css.visual.model;

import org.netbeans.modules.css2.editor.model.CssRuleContent;
import javax.swing.DefaultComboBoxModel;

/**
 * Model for the Position Style Editor data
 * @author  Winston Prakash
 * @version 1.0
 */
public class PositionModel {
 
    public DefaultComboBoxModel getModeList(){
        return new ModeList();
    }

    public DefaultComboBoxModel getPositionList(){
        return new PositionList();
    }

    public DefaultComboBoxModel getPositionUnitList(){
        return new PositionUnitList();
    }

    public DefaultComboBoxModel getSizeList(){
        return new SizeList();
    }

    public DefaultComboBoxModel getZIndexList(){
        return new ZIndexList();
    }

    public DefaultComboBoxModel getVisibilityList(){
        return new VisibilityList();
    }

    public static class PositionList extends DefaultComboBoxModel{
        public PositionList(){
            addElement(CssRuleContent.NOT_SET);
            addElement("1"); //NOI18N
            addElement("2"); //NOI18N
            addElement("3"); //NOI18N
            addElement("4");  //NOI18N
            addElement("5"); //NOI18N
            addElement("6"); //NOI18N
            addElement("8"); //NOI18N
            addElement("10"); //NOI18N
            addElement(CssRuleContent.VALUE);
        }
    }

    public static class SizeList extends DefaultComboBoxModel{
        public SizeList(){
            addElement(CssRuleContent.NOT_SET);
            addElement("1"); //NOI18N
            addElement("2"); //NOI18N
            addElement("3"); //NOI18N
            addElement("4");  //NOI18N
            addElement("5"); //NOI18N
            addElement("6"); //NOI18N
            addElement("8"); //NOI18N
            addElement("10"); //NOI18N
            addElement(CssRuleContent.VALUE);
        }
    }

    public static class ModeList extends DefaultComboBoxModel{
        public ModeList(){
            addElement(CssRuleContent.NOT_SET);
            String[] propValues = CssProperties.getCssPropertyValues(CssProperties.POSITION);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
        }
    }

    public static class ZIndexList extends DefaultComboBoxModel{
        public ZIndexList(){
            addElement(CssRuleContent.NOT_SET);
            addElement("1"); //NOI18N
            addElement("2"); //NOI18N
            addElement("3"); //NOI18N
            addElement("4");  //NOI18N
            addElement("5"); //NOI18N
            addElement("6"); //NOI18N
            addElement("8"); //NOI18N
            addElement("10"); //NOI18N
            addElement(CssRuleContent.VALUE);
        }
    }

    public static class VisibilityList extends DefaultComboBoxModel{
        public VisibilityList(){
            addElement(CssRuleContent.NOT_SET);
            String[] propValues = CssProperties.getCssPropertyValues(CssProperties.VISIBILITY);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
        }
    }

    public static class ClipList extends DefaultComboBoxModel{
        public ClipList(){
            addElement(CssRuleContent.NOT_SET);
            addElement("1"); //NOI18N
            addElement("2"); //NOI18N
            addElement("3"); //NOI18N
            addElement("4"); //NOI18N
            addElement("5"); //NOI18N
            addElement("6"); //NOI18N
            addElement("8"); //NOI18N
            addElement("10"); //NOI18N
            addElement(CssRuleContent.VALUE);
        }
    }

    public static class PositionUnitList extends DefaultComboBoxModel{
        public PositionUnitList(){
            String[] unitValues = CssProperties.getCssLengthUnits();
            for(int i=0; i< unitValues.length; i++){
                addElement(unitValues[i]);
            }
        }
    }
}
