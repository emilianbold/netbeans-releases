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



package org.netbeans.test.umllib.customelements;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.test.umllib.DiagramElementChooser;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;

public class LifelineOperator extends DiagramElementOperator{
    
    
    public LifelineOperator(DiagramOperator diagramOperator,  Widget graphObject) throws NotFoundException {
        super(diagramOperator, graphObject);
    }
    
    
    public LifelineOperator(DiagramOperator diagramOperator, String lifelineName, String classifierName) throws NotFoundException {
        super(diagramOperator, new LifelineByFullNameChooser(lifelineName, classifierName), 0);
    }
    
    public LifelineOperator(DiagramOperator diagramOperator, String lifelineName, String classifierName, int index) throws NotFoundException {
        super(diagramOperator, new LifelineByFullNameChooser(lifelineName, classifierName), index);
    }
    
    
    /**
     * Construct DiagramElementOperator by custom finder and index
     * @param diagramOperator Diagram to look for element
     * @param elementFinder custom finder
     * @param index index
     * @throws qa.uml.exceptions.NotFoundException when no suitable element found
     */
    public LifelineOperator(DiagramOperator diagramOperator, DiagramElementChooser elementFinder, int index) throws NotFoundException {
        super(diagramOperator, waitForGraphObject(diagramOperator, elementFinder, index));
        
    }
    
    
    public boolean equals(Object obj) {
        return ( obj instanceof LifelineOperator ) && ( ((LifelineOperator) obj).getGraphObject() == getGraphObject() );
    }
    
    
    
    public static class LifelineByFullNameChooser implements DiagramElementChooser {
        private String lineName = null;
        private String classifierName = null;
        
        /**
         *
         * @param lineName
         * @param classifierName
         */
        public LifelineByFullNameChooser(String lineName, String classifierName){
            this.lineName = lineName;
            this.classifierName = classifierName;
        }
        
        /**
         *
         * @param vn
         * @param elementType
         */
        /*
        public ElementByVNChooser(String vn, String elementType){
            this(vn, elementType, new Operator.DefaultStringComparator(true,true));
        }
         */
        /**
         *
         * @param graphObject
         * @return
         */ 
        public boolean checkElement(Widget graphObject) {
//           TODO: Need to understand lifeline layout
//            if ( !graphObject.getEngine().getElementType().equals(ElementTypes.LIFELINE.toString())){
//                return false;
//            }
//            
//            ETList<ICompartment> compartments = graphObject.getEngine().getCompartments();
//            if(compartments == null){
//                return false;
//            }
//            
//            Iterator<ICompartment> it = compartments.iterator();
//            ArrayList<ICompartment> compartmentsFound = new ArrayList<ICompartment>();
//            while(it.hasNext()) {
//                ICompartment co = it.next();
//                if (co instanceof ETLifelineNameCompartment){
//                    String name = co.getName();
//                    if(name.equals(lineName+" : "+classifierName)){
//                        return true;
//                    } else{
//                        return false;
//                    }
//                }
//6.0            }
            return true;
        }
        
        /**
         *
         * @return
         */
        public String getDescription() {
            return "Chooser for description:"+lineName+" : "+classifierName;
        }     
    }
}
