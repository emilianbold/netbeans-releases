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
 * Model to hold the presentation data for the Text Block Styles
 * @author  Winston Prakash
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
    
    public class LineHeightList extends DefaultComboBoxModel{
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
    
    public class LetterSpacingList extends DefaultComboBoxModel{
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
    
    public class WordSpacingList extends DefaultComboBoxModel{
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
    
    public class IndentationList extends DefaultComboBoxModel{
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
    
    public class TextDirectionList extends DefaultComboBoxModel{
        public TextDirectionList(){
            addElement(CssStyleData.NOT_SET);
            addElement("ltr"); //NOI18N
            addElement("rtl"); //NOI18N
        }
    }
    
    public class HorizontalAlignmentList extends DefaultComboBoxModel{
        public HorizontalAlignmentList(){
            addElement(CssStyleData.NOT_SET);
            addElement("left"); //NOI18N
            addElement("right"); //NOI18N
            addElement("center"); //NOI18N
            addElement("justify"); //NOI18N
        }
    }
    
    public class VerticalAlignmentList extends DefaultComboBoxModel{
        public VerticalAlignmentList(){
            addElement(CssStyleData.NOT_SET);
            addElement("baseline"); //NOI18N
            addElement("sub"); //NOI18N
            addElement("super"); //NOI18N
            addElement("top"); //NOI18N
            addElement("text-top"); //NOI18N
            addElement("middle"); //NOI18N
            addElement("bottom"); //NOI18N
            addElement(CssStyleData.VALUE);
        }
    }
    
    public class TextBlockUnitList extends DefaultComboBoxModel{
        public TextBlockUnitList(){
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
