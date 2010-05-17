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
 * Verifier.java
 *
 * Created on February 21, 2005, 1:45 PM
 */

package org.netbeans.test.umllib.vrf.sanity;

import java.awt.Point;
import java.io.PrintStream;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.exceptions.NotFoundException;

/**
 *
 * @author Alexei Mokeev
 */
public abstract class AbstractDiagramVerifier {
    public static String elementPrefix = null;;
    public ElementTypes[] elements = null;
    DiagramOperator diagram = null;
    PrintStream out = null;
    
    /** This is default constructor. It intends that diagram already opened */
    public AbstractDiagramVerifier(String diagramName, PrintStream out, String elementPrefix) {
        diagram = new DiagramOperator(diagramName);        
        this.out = out;
        this.elementPrefix = elementPrefix;
        elements = getSupportedElements();
    }
    
    public AbstractDiagramVerifier(String diagramName, PrintStream out) {
        this(diagramName, out, "CRE");
    }
    
    public AbstractDiagramVerifier(String diagramName) {
        this(diagramName, null);
    }
    
    protected DiagramElementOperator createElementByType(ElementTypes el, int index){
        try{
            Point p2 = diagram.getDrawingArea().getFreePoint(30);
            DiagramElementOperator e = diagram.putElementOnDiagram(elementPrefix+el+index,el,p2.x,p2.y);
            return e;
        }catch(Exception e){
            return null;
        }                        
    }
    
    public boolean doCheck() {
        boolean isOk = true;
        for(int i = 0; i<elements.length;i++) {
            try {
                
                DiagramElementOperator e = createElementByType(elements[i], i);
                if(e==null) {
                    isOk = false;
                    log("Element " + elements[i] + " was not found");
                }else{
                    boolean elementOk = doElementCheck(e);
                    if (!elementOk){
                        isOk = false;
                        log("Element " + elements[i] + " did not pass the check");
                    }
                }
                
            }catch(TimeoutExpiredException e1) {
                isOk = false;
                log("TimeoutExpired for:" + elements[i]);
            }
        }
        return isOk;
    }
    
    public abstract boolean doElementCheck(DiagramElementOperator element);
    
    protected void log(String s) {
        if(out!= null) {
            out.println(this.getClass().getName() + ":" + s);
        }
    }
    protected ElementTypes[] getSupportedElements() {
        return new ElementTypes[]{};
    }
}
