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

package org.netbeans.modules.uml.diagrams.actions.sqd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.ExecutionSpecificationThinWidget;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;

/**
 * common for all shifts, etc
 * @author sp153251
 */
abstract public class ArrangeMessagesProvider implements ActionProvider {
    
    /**
     * sort execution specifications 
     * @param parentWidget in this widget all children execution specifications will be sorted and returned
     * @return sorted list of execution specifications (top to bottom order)
     */
    public static ArrayList<ExecutionSpecificationThinWidget> getSortedSpecifications(Widget parentWidget)
    {
        ArrayList<ExecutionSpecificationThinWidget> coWidgets=new ArrayList<ExecutionSpecificationThinWidget>();//for now only thin execution specifications are supported on the same level
        //
        //
        for(Widget i: parentWidget.getChildren())
        {
            if(i instanceof ExecutionSpecificationThinWidget)coWidgets.add((ExecutionSpecificationThinWidget)i);
        }
        Collections.sort(coWidgets, new ThinExecutionSpecificationByPositionComparator());
        return coWidgets;
    }
    
    /*
     * 
     * Less if closer to header (i.e. less Y).
     */
    private static class ThinExecutionSpecificationByPositionComparator implements Comparator<ExecutionSpecificationThinWidget>
    {
        private ExecutionSpecificationThinWidget hook;

        public ThinExecutionSpecificationByPositionComparator()
        {
            this(null);
        }
        /*
         * hook affect equal widgets
         */
        public ThinExecutionSpecificationByPositionComparator(ExecutionSpecificationThinWidget hook)
        {
            this.hook=hook;
        }
        
        public int compare(ExecutionSpecificationThinWidget o1, ExecutionSpecificationThinWidget o2) {
            //all coordinates may be translated to scene coordinates later but not necessary because class is private and used to sort only within one widget(may be for now only)
            int res=0;
            int y1_0=o1.getLocation().y+o1.getBounds().y;
            int y2_0=o2.getLocation().y+o2.getBounds().y;;
            int height1=o1.getBounds().height;
            int height2=o2.getBounds().height;
            int y1_1=y1_0+height1;
            int y2_1=y2_0+height2;
            if(y1_0>y2_0 && y1_1>y2_1)
            {
                //first have both boders above both borders of second respectively (here case when first completely above too)
                res=1;
            }
            else if(y1_0<y2_0 && y1_1<y2_1)
            {
                //first less
                res=-1;
            }
            else if(hook!=null && o1==hook)
            {
                //hook is considered to be on farther side
                res=+1;
            }
            else if(y1_1>y2_1)
            {
                res=+1;//bottom border is more important
            }
            else if(y1_1<y2_1)
            {
                res=-1;
            }
            return res;
        }
        
    }
}
