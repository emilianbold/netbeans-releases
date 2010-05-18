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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.vmd.api.inspector;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import javax.swing.Action;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;

/**
 * @author Karol Harezlak
 */

/**
 * This class suits as a descriptor for the Mobility Visual Designer Navigator node.
 */
public abstract class InspectorFolder implements  InspectorPositionController {
    
    /**
     * Returns TypeID of the component connected with this folder descriptor.
     * @return components TypeID
     */
    public abstract TypeID getTypeID();
    
    /**
     * Returns ComponentsID of the component connected with this folder descriptor.
     * @return Long components ID
     */
    public abstract Long getComponentID();
    
    /**
     * Returns image icon which represents folder in the Visual Designer Navigator.
     * @return folder's image icon
     */
    public abstract Image getIcon();
    
    /**
     * Returns display name of the folder.
     * @return folder's display name
     */
    public abstract String getDisplayName();
    
    /**
     * Returns HTML display name of the folder.
     * @return folder's HTML display name
     */
    public abstract String getHtmlDisplayName();
    
    /**
     * Returns name of the folder.
     * @return folder's name
     */
    public abstract String getName();
    
    /**
     * Returns array of actions available for this folder. 
     * @return array of folder's actions
     */
    public abstract Action[] getActions();
    
    /**
     * Indicates if folder can be rename.
     * @return returns boolean value. Boolean.TRUE folder can be rename. Boolean.FALSE folder can't be rename. 
     */
    public abstract boolean canRename();
    
    /**
     * Returns array of InspectorOrderingControllers. 
     * @return returns array of the InspectorOrderingControllers
     */
    public abstract InspectorOrderingController[] getOrderingControllers();
    
     /**
     * Creates object with implemented interface AcceptSuggestion.Created object 
     * can have any type of functionality that helps with folder dragging and dropping.
     * AcceptSuggestion interface is just a empty interface used as a marker.
     * @return default implementation returns null
     */
    public AcceptSuggestion createSuggestion(Transferable transferable) {
        return null;
    }
    
}
