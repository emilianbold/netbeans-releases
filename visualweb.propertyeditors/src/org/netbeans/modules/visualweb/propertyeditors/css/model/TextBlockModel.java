/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
