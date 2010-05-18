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
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.Widget;
//6.0import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.test.umllib.CompartmentOperator;
import org.netbeans.test.umllib.CompartmentTypes;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.UMLWidgetOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;

public class SequenceLifelineCompartment extends CompartmentOperator{
    
    
    /**
     * Construct compartment 
     * @param el Instance of DiagramElementOperator to look for compartment     
     * @throws qa.uml.exceptions.NotFoundException when no suitable compartment found
     */
    public SequenceLifelineCompartment(DiagramElementOperator el) throws NotFoundException{
        super(el, CompartmentTypes.SEQUENCE_LIFELINE_COMPARTMENT);
    }
    
    
    /**
     * Construct compartment by index
     * @param el Instance of DiagramElementOperator to look for compartment
     * @param index index
     * @throws qa.uml.exceptions.NotFoundException when no suitable compartment found
     */
    public SequenceLifelineCompartment(DiagramElementOperator el, int index) throws NotFoundException{
        super(el, index);
    }
    
    /**
     * Costruct compartment by type
     * @param el Instance of DiagramElementOperator to look for compartment
     * @param type Compartemnt type.
     * @throws qa.uml.exceptions.NotFoundException when no suitable compartment found
     * @see qa.uml.CompartmentTypes
     */
    public SequenceLifelineCompartment(DiagramElementOperator el, CompartmentTypes type) throws NotFoundException{
        super(el, type);        
    }
    /**
     * Construct compartment by compartment class
     * @param el Instance of DiagramElementOperator
     * @param co Instance of ICompartment
     */
   public SequenceLifelineCompartment(DiagramElementOperator el, Widget co){
        super(el, co);
    }
    
    
    /**
     *@return logical bounding rectangle
     *
     */
    public Rectangle getLogicalRectangle() {
        Rectangle rect = (new UMLWidgetOperator(sourceCompartment)).getRectangle();
        return rect;

    }

    /**
     *
     * @return  bounding rectangle for compartment
     */
    public Rectangle getRectangle() {
        return ((Widget)sourceElement.getGraphObject()).getBounds();        
    }
    
}
