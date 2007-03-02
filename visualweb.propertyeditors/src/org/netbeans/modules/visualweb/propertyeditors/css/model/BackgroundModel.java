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
package org.netbeans.modules.visualweb.propertyeditors.css.model;

import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author  Winston Prakash
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

    public class BackgroundRepeatList extends DefaultComboBoxModel{
        public BackgroundRepeatList(){
            addElement(CssStyleData.NOT_SET);
            addElement("repeat"); //NOI18N
            addElement("repeat-x"); //NOI18N
            addElement("repeat-y"); //NOI18N
            addElement("no-repeat"); //NOI18N
        }
    }
    
    public class BackgroundScrollList extends DefaultComboBoxModel{
        public BackgroundScrollList(){
            addElement(CssStyleData.NOT_SET);
            addElement("fixed"); //NOI18N
            addElement("scroll"); //NOI18N
        }
    }
    
    public class BackgroundPositionList extends DefaultComboBoxModel{
        public BackgroundPositionList(){
            addElement(CssStyleData.NOT_SET);
            addElement("center"); //NOI18N
            addElement("left"); //NOI18N
            addElement("right"); //NOI18N
            addElement("top"); //NOI18N
            addElement("bottom"); //NOI18N
            addElement(CssStyleData.VALUE);
        }
    }
    
    public class BackgroundPositionUnitList extends DefaultComboBoxModel{
        public BackgroundPositionUnitList(){
            addElement("px"); //NOI18N
            addElement("%"); //NOI18N
            addElement("in"); //NOI18N
            addElement("cm"); //NOI18N
            addElement("mm"); //NOI18N
            addElement("em"); //NOI18N
            addElement("ex"); //NOI18N
            addElement("picas"); //NOI18N
        }
    }
}
