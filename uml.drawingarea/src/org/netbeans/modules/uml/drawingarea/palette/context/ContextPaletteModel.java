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
package org.netbeans.modules.uml.drawingarea.palette.context;

import org.netbeans.api.visual.widget.Widget;
import java.util.ArrayList;

/**
 * The ContextPaletteModel interface specifies the methods that the Context 
 * Palette uses to interrogate the palette data.
 * <p>
 * A context palette can have a static location on the left side of the 
 * model element, or it can follow the mouse as long as the mouse is over 
 * the associated model element.
 *   
 * @author treyspiva
 */
public interface ContextPaletteModel
{
    /**
     * The FOLLOWMODE enumeration is used to specify the location type of the 
     * palette.
     */
    enum FOLLOWMODE
    {
        /**
         * The palette should have a static location on the side of the 
         * associated model element.
         */
        NONE, 
        
        /**
         * The palette should follow the mouse cursor as the user move the
         * cursor vertically.
         */
        VERTICAL_ONLY, 
        
        /**
         * The palette should follow the mouse cursor as the user moves the 
         * cursor vertically and horizontally. Typically the palette will be 
         * displayed on the left or right side of the associated model element,
         * but the side that palette will be displayed on will be based on the
         * location of the cursor.
         */
        VERTICAL_AND_HORIZONTAL
    
    };
    
    /**
     * Retreive the context palette button models that should be used to 
     * populate the palettes contents.
     * 
     * @return The buttom models used to populate the palette.
     */
    public ArrayList < ContextPaletteButtonModel > getChildren();
            
    /**
     * The Widget that is associated with the context palette.
     * 
     * @return the associated widget.
     */
    public Widget getContext();
    
//    public boolean isList(int index);
    
    /**
     * Retrieves how the palette should behave as the mouse is moved.
     * 
     * @return the mouse behavior.
     */
    public FOLLOWMODE getFollowMouseMode();
}
