/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.uml.drawingarea;

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import javax.swing.Action;
import org.netbeans.api.visual.widget.Widget;

/**
 * The LabelManager is used to manage a set of labels that can be displayed.
 * 
 * @author treyspiva
 */
public interface LabelManager extends PropertyChangeListener
{
    /**
     * LabelType is an enumeration of the types of labels. 
     */
    public enum LabelType
    {
        /**
         * The edge label.  The edge label will usally be displayed on the 
         * center of the connection.
         */
        EDGE, 
        
        /**
         * Labels that will be placed on the source end of the connection.
         */
        SOURCE, 
        
        /**
         * Labels that will be placed on the target end of teh connection.
         */
        TARGET; 
    }
    
    /**
     * Shows the label with the specified name.  The label will have the 
     * type of LabelType.EDGE.
     * 
     * @param name the name of the label.
     */
    public void showLabel(String name);
    
    /**
     * Shows the label with the specified name and the specified type.
     * 
     * @param name the name of the label.
     * @param type the lable type.
     */
    public void showLabel(String name, LabelType type);
    
    /**
     * Shows the label with the specified name and type at the location specified.
     * @param name
     * @param type
     * @param location
     */
    public void showLabel(String name, LabelType type, Point location);

   /**
     * select and focus on labvel if it's shown
     * @param name
     */
    public void selectLabel(final String name);
   /**
     * select and focus on labvel if it's shown
     * @param name
     * @param type
     */
    public void selectLabel(final String name, final LabelType type);
    
    /**
     * Returns true if the label is currently selected
     */
    public boolean isLabelSelected(final String name, final LabelType type);
    
    /**
     * Hides the label with the specified name.  The label will have the 
     * type of LabelType.EDGE
     * @param name the name of the label.
     */
    public void hideLabel(String name);
    
    /**
     * Hides the label with the specified name and the specified type.
     * 
     * @param name the name of the label.
     * @param type the lable type.
     */
    public void hideLabel(String name, LabelType type);
            
    /**
     * Checks if an edge label is visible.
     * 
     * @param name the name of the label.
     * @return true if the label is visible.
     */
    public boolean isVisible(String name);
    
    /**
     * Checks if an edge label is visible.
     * 
     * @param name the name of the label.
     * @param type the type of the label.
     * @return true if the label is visible.
     */
    public boolean isVisible(String name, LabelType type);
    
    /**
     * The label manager is given the chance to create the labels that should
     * be displayed when an edge is first created.
     */
    public void createInitialLabels();
    
    /**
     * Retrieves the set of actions that should be available on a context menu.
     * The set of actions are used to give the user the ability to control 
     * the label manager.  For example: The actions should give the user the
     * ability to specify which labels should be shown or hidden.
     * 
     * @param type the type of labels.
     * @return the set of actions.
     */ 
    public Action[] getContextActions(LabelType type);
    
    /**
     * A map that specifies the labels that are currently displayed.
     * @return the map.
     */
    public HashMap<String, Widget> getLabelMap();
}