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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * PositionModel.java
 *
 * Created on October 29, 2004, 12:33 PM
 */

package org.netbeans.modules.css.visual.model;

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
            addElement(CssStyleData.NOT_SET);
            addElement("1"); //NOI18N
            addElement("2"); //NOI18N
            addElement("3"); //NOI18N
            addElement("4");  //NOI18N
            addElement("5"); //NOI18N
            addElement("6"); //NOI18N
            addElement("8"); //NOI18N
            addElement("10"); //NOI18N
            addElement(CssStyleData.VALUE);
        }
    }

    public static class SizeList extends DefaultComboBoxModel{
        public SizeList(){
            addElement(CssStyleData.NOT_SET);
            addElement("1"); //NOI18N
            addElement("2"); //NOI18N
            addElement("3"); //NOI18N
            addElement("4");  //NOI18N
            addElement("5"); //NOI18N
            addElement("6"); //NOI18N
            addElement("8"); //NOI18N
            addElement("10"); //NOI18N
            addElement(CssStyleData.VALUE);
        }
    }

    public static class ModeList extends DefaultComboBoxModel{
        public ModeList(){
            addElement(CssStyleData.NOT_SET);
            String[] propValues = CssProperties.getCssPropertyValues(CssProperties.POSITION);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
        }
    }

    public static class ZIndexList extends DefaultComboBoxModel{
        public ZIndexList(){
            addElement(CssStyleData.NOT_SET);
            addElement("1"); //NOI18N
            addElement("2"); //NOI18N
            addElement("3"); //NOI18N
            addElement("4");  //NOI18N
            addElement("5"); //NOI18N
            addElement("6"); //NOI18N
            addElement("8"); //NOI18N
            addElement("10"); //NOI18N
            addElement(CssStyleData.VALUE);
        }
    }

    public static class VisibilityList extends DefaultComboBoxModel{
        public VisibilityList(){
            addElement(CssStyleData.NOT_SET);
            String[] propValues = CssProperties.getCssPropertyValues(CssProperties.VISIBILITY);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
        }
    }

    public static class ClipList extends DefaultComboBoxModel{
        public ClipList(){
            addElement(CssStyleData.NOT_SET);
            addElement("1"); //NOI18N
            addElement("2"); //NOI18N
            addElement("3"); //NOI18N
            addElement("4"); //NOI18N
            addElement("5"); //NOI18N
            addElement("6"); //NOI18N
            addElement("8"); //NOI18N
            addElement("10"); //NOI18N
            addElement(CssStyleData.VALUE);
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
