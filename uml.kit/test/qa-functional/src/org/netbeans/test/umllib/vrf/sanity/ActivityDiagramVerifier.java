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


/*
 * ClassDiagramVerifier.java
 *
 * Created on February 21, 2005, 1:59 PM
 */

package org.netbeans.test.umllib.vrf.sanity;

import java.awt.Point;
import java.io.PrintStream;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.ElementTypes;

/**
 *
 * @author Alexei Mokeev
 */
public class ActivityDiagramVerifier extends AbstractDiagramVerifier{
    //String[] elements =
    /** Creates a new instance of ClassDiagramVerifier */
    public ActivityDiagramVerifier(String diagramName) {
        super(diagramName);
    }
    
    
    public ActivityDiagramVerifier(String diagramName, PrintStream log) {
        super(diagramName, log);
    }
    
    public ActivityDiagramVerifier(String diagramName, PrintStream log, String prefix) {
        super(diagramName, log, prefix);
    }
    public boolean doElementCheck(DiagramElementOperator element) {
        log("Checking " + element.getElementType());
        
        return true;
    }
    protected ElementTypes[] getSupportedElements() {
        return new ElementTypes[] { 
                ElementTypes.INVOCATION,
                ElementTypes.ACTIVITY_GROUP,
                ElementTypes.INITIAL_NODE,
                ElementTypes.ACTIVITY_FINAL_NODE,
                ElementTypes.FLOW_FINAL,
                ElementTypes.DECISION,
                ElementTypes.VERTICAL_FORK,
                ElementTypes.HORIZONTAL_FORK,                                                
                ElementTypes.PARAMETER_USAGE,
                ElementTypes.DATA_STORE,
                ElementTypes.SIGNAL,
                ElementTypes.PARTITION
        };
    }
    
    protected DiagramElementOperator createElementByType(ElementTypes el, int index){        
        if(el == ElementTypes.INITIAL_NODE || el == ElementTypes.ACTIVITY_FINAL_NODE || el == ElementTypes.FLOW_FINAL 
                || el == ElementTypes.DECISION || el == ElementTypes.VERTICAL_FORK || el == ElementTypes.HORIZONTAL_FORK){
            try{
                Point p2 = diagram.getDrawingArea().getFreePoint(30);
                diagram.createGenericElementOnDiagram(null,el,p2.x,p2.y);
                DiagramElementOperator e = new DiagramElementOperator(diagram, new DiagramElementOperator.ElementByTypeChooser(el),0);
                return e;
            }catch(Exception e){
                return null;
            }            
        }else{
            return super.createElementByType(el, index);
        }
                                
    }
    
}
