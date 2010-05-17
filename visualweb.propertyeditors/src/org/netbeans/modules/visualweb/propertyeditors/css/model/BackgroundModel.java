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
