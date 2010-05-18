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


package org.netbeans.test.umllib.exceptions;

import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.ExpandedElementTypes;

/**
 * Thrown when expected element can't be found on diagram
 * @author Sergey Petrov
 */
public class NotFoundOnDiagramException extends NotFoundException{
    
    /**
     * Creates a new instance of NotFoundOnDiagramException
     * @param elemenType 
     */
    public NotFoundOnDiagramException(ElementTypes elemenType) {
        super(elemenType.toString());     
        missedEl=elemenType;
    }
    /**
     * Creates a new instance of NotFoundOnDiagramException 
     *  and specify history of creation (action for element creation)
     * @param elemenType 
     * @param type 
     */
    public NotFoundOnDiagramException(ExpandedElementTypes elemenType,ActionTypes type) {
        super(elemenType.toString());     
        missedElEx=elemenType;
        missedEl=null;
        preAction=type;
     }
    /**
     * Creates a new instance of NotFoundOnDiagramException 
     *  and specify history of creation (action for element creation)
     * @param elemenType 
     * @param type 
     */
    public NotFoundOnDiagramException(ElementTypes elemenType,ActionTypes type) {
        super(elemenType.toString());     
        missedEl=elemenType;
        preAction=type;
     }
    /**
     * Creates a new instance of NotFoundOnDiagramException 
     *  and specify history of creation (action for element creation)
     * @param elemenType 
     * @param type 
     * @param id 
     */
    public NotFoundOnDiagramException(ElementTypes elemenType,ActionTypes type,int id) {
        super(elemenType.toString());     
        missedEl=elemenType;
        preAction=type;
        missedElEx=null;
        this.id=id;
    }
    
    /**
     * 
     * @return 
     */
    public String getMessage(){
        return "element of type "+super.getMessage()+" is not found on diagram.";
    }
    
    private ActionTypes preAction=ActionTypes.UNKNOWN;
    private ElementTypes missedEl=ElementTypes.ANY; 
    private ExpandedElementTypes missedElEx=ExpandedElementTypes.ANY; 
    private int id;
    
    /**
     * get type of action invoked for element creation
     * @return 
     */
    public ActionTypes getPreAction()
    {
        return preAction;
    }
    
    /**
     * 
     * @return 
     */
    public int getId()
    {
        return id;
    }
    /**
     * 
     * @return 
     */
    public ElementTypes getType()
    {
        return missedEl;
    }
    /**
     * 
     * @return 
     */
    public ExpandedElementTypes getExpandedType()
    {
        return missedElEx;
    }
    /**
     * Actions specific for ellement creation on diagram
     */
    static public enum ActionTypes
    {
        UNKNOWN("Any or unknown type of creation."),
        FROMPALETTE("Element created with with palette."),
        FROMTREE("Drag from prject tree."),
        SHORTCUT("Put with specific shortcut."),
        CDFS("Element created with Create Diagram from Selected elements action."),
        DEPENDENCY("Element created with Create Dependency Diagram action."),
        PATTERN("Element created with design pattern applying."),
        PASTE("Any type of paste operation.");
                
        private String description="";
        
        /**
         * 
         * @param desc 
         */
        ActionTypes(String desc)
        {
            description=desc;
        }
        ActionTypes()
        {
            description=name();
        }
        
    }
}
