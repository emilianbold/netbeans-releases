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
 * TextBlockModel.java
 *
 * Created on October 29, 2004, 10:30 AM
 */

package org.netbeans.modules.css.visual.model;

import javax.swing.DefaultComboBoxModel;

/**
 * Model for the Text Block Style Editor data
 * @author  Winston Prakash
 * @version 1.0
 */
public class TextBlockModel {

    public DefaultComboBoxModel getHorizontalAlignmentList(){
        return new HorizontalAlignmentList();
    }

    public DefaultComboBoxModel getVerticalAlignmentList(){
        return new VerticalAlignmentList();
    }

    public DefaultComboBoxModel getIndentationList(){
        return new IndentationList();
    }

    public DefaultComboBoxModel getTextBlockUnitList(){
        return new TextBlockUnitList();
    }

    public DefaultComboBoxModel getTextDirectionList(){
        return new TextDirectionList();
    }

    public DefaultComboBoxModel getWordSpacingList(){
        return new WordSpacingList();
    }

    public DefaultComboBoxModel getLetterSpacingList(){
        return new LetterSpacingList();
    }

    public DefaultComboBoxModel getLineHeightList(){
        return new LineHeightList();
    }

    public static class LineHeightList extends DefaultComboBoxModel{
        public LineHeightList(){
            addElement(CssStyleData.NOT_SET);
            addElement("normal"); //NOI18N
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

    public static class LetterSpacingList extends DefaultComboBoxModel{
        public LetterSpacingList(){
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

    public static class WordSpacingList extends DefaultComboBoxModel{
        public WordSpacingList(){
            addElement(CssStyleData.NOT_SET);
            addElement("1"); //NOI18N
            addElement("2"); //NOI18N
            addElement("3"); //NOI18N
            addElement("4"); //NOI18N
            addElement("5"); //NOI18N
            addElement("6");  //NOI18N
            addElement("8"); //NOI18N
            addElement("10"); //NOI18N
            addElement(CssStyleData.VALUE);
        }
    }

    public static class IndentationList extends DefaultComboBoxModel{
        public IndentationList(){
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

    public static class TextDirectionList extends DefaultComboBoxModel{
        public TextDirectionList(){
            addElement(CssStyleData.NOT_SET);
            String[] propValues = CssProperties.getCssPropertyValues(CssProperties.DIRECTION);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
        }
    }

    public static class HorizontalAlignmentList extends DefaultComboBoxModel{
        public HorizontalAlignmentList(){
            addElement(CssStyleData.NOT_SET);
            String[] propValues = CssProperties.getCssPropertyValues(CssProperties.TEXT_ALIGN);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
        }
    }
    
    public static class VerticalAlignmentList extends DefaultComboBoxModel{
        public VerticalAlignmentList(){
            addElement(CssStyleData.NOT_SET);
            String[] propValues = CssProperties.getCssPropertyValues(CssProperties.VERTICAL_ALIGN);
            for(int i=0; i< propValues.length; i++){
                addElement(propValues[i]);
            }
        }
    }
    
    public static class TextBlockUnitList extends DefaultComboBoxModel{
        public TextBlockUnitList(){
            String[] unitValues = CssProperties.getCssLengthUnits();
            for(int i=0; i< unitValues.length; i++){
                addElement(unitValues[i]);
            }
        }
    }
    
}
