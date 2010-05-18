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

package org.netbeans.performance.visualweb;

import org.netbeans.performance.visualweb.dialogs.*;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class JSFComponentOptionsDialog extends PerformanceTestCase {
    
    protected PaletteComponentOperator palette;
    protected WebFormDesignerOperator surface;
    protected String categoryName;
    protected String componentName;
    protected java.awt.Point addPoint;
    
    /** Creates a new instance of JSFComponentOptionsDialog */
    public JSFComponentOptionsDialog(String testName) {
        super(testName);
    }
    
    /**
     *
     * @param testName
     * @param performanceDataName
     */
    public JSFComponentOptionsDialog(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }


    @Override
    public void initialize() {
        PaletteComponentOperator.invoke();
        openPageAndAddComponent();
    }
    
    private void openPageAndAddComponent() throws Error {
        surface = org.netbeans.performance.visualweb.VWPUtilities.openedWebDesignerForJspFile("UltraLargeWA", "TestPage");
        palette = new PaletteComponentOperator();

        //Select component in palette
        palette.getCategoryListOperator(categoryName).selectItem(componentName);
        //Click on design surface to add selected component on page
        surface.clickOnSurface(new Double(addPoint.getX()).intValue(),new Double(addPoint.getY()).intValue());
        new QueueTool().waitEmpty();
    }
    
    public void prepare() {
        surface = WebFormDesignerOperator.findWebFormDesignerOperator("TestPage",false);
    }
    
   public ComponentOperator open(){
        return null;
    }
    
    @Override
    protected void shutdown() {
        surface.closeDiscard();
        try {
            new PropertySheetOperator("TestPage").close();
        } catch (TimeoutExpiredException timeoutExpiredException) {
            //do nothing...can be not opened properties and help tabs
        }        
    }
    
}
