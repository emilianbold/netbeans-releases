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
 * BackgroundModel.java
 *
 * Created on October 27, 2004, 5:24 PM
 */

package org.netbeans.modules.css.visual.model;

import javax.swing.DefaultComboBoxModel;

/**
 * Model for the Background Style Editor data
 * @author  Winston Prakash
 * @version 1.0
 */
public class BackgroundModel {

    public DefaultComboBoxModel getBackgroundRepeatList(){

        return new BackgroundRepeatList();
    }

    public DefaultComboBoxModel getBackgroundScrollList(){
        return new BackgroundScrollList();
    }

    public DefaultComboBoxModel getBackgroundPositionList(){
        return new BackgroundPositionList();
    }

    public DefaultComboBoxModel getBackgroundPositionUnitList(){
        return new BackgroundPositionUnitList();
    }

    public static class BackgroundRepeatList extends DefaultComboBoxModel{
        public BackgroundRepeatList(){
            String[] propValues = CssProperties.getCssPropertyValues(CssProperties.BACKGROUND_REPEAT);
            addElement(CssStyleData.NOT_SET);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
        }
    }

    public static class BackgroundScrollList extends DefaultComboBoxModel{
        public BackgroundScrollList(){
            String[] propValues = CssProperties.getCssPropertyValues(CssProperties.BACKGROUND_ATTACHMENT);
            addElement(CssStyleData.NOT_SET);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
        }
    }

    public static class BackgroundPositionList extends DefaultComboBoxModel{
        public BackgroundPositionList(){
            String[] propValues = CssProperties.getCssPropertyValues(CssProperties.BACKGROUND_POSITION);
            addElement(CssStyleData.NOT_SET);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
            addElement(CssStyleData.VALUE);
        }
    }

    public static class BackgroundPositionUnitList extends DefaultComboBoxModel{
        public BackgroundPositionUnitList(){
            String[] unitValues = CssProperties.getCssLengthUnits();
            for(int i=0; i< unitValues.length; i++){
                addElement(unitValues[i]);
            }
        }
    }
}
