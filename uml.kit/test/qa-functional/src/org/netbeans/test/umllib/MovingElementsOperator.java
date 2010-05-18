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


package org.netbeans.test.umllib;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.test.umllib.util.LabelsAndTitles;

public class MovingElementsOperator extends JDialogOperator{
    public final static String BTN_YES_CAPTION = "Yes";
    public final static String BTN_NO_CAPTION = "No";
    public final static String BTN_CANCEL_CAPTION = "Cancel";
    public final static String OPTION_APPLY_ALL = "Apply to all items";
    
    public MovingElementsOperator() {
        super(LabelsAndTitles.MOVING_ELEMENTS_DIALOG_TITLE);
    }
    
    public void clickMoveElement(){
        new JButtonOperator(this, BTN_YES_CAPTION).clickMouse(1);
    }
    
    public void clickImportElement(){
        new JButtonOperator(this, BTN_NO_CAPTION).clickMouse(1);
    }

    public void clickCancel(){
        new JButtonOperator(this, BTN_CANCEL_CAPTION).clickMouse(1);
    }

    /**
     * 
     * @param selected 
     */
    public void setOptionApplyToAll(boolean selected){
        new JCheckBoxOperator(this, OPTION_APPLY_ALL).setSelected(selected);
    }
    
}
