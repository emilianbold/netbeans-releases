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
package org.netbeans.modules.j2ee.websphere6.ui;

import javax.swing.*;
import java.util.*;

/**
 * A combobox model that represents the list of local instances. It
 * contains a vector of objects of Instance class that contain all data
 * for the instance
 *
 * @author Kirill Sorokin
 * @author Dmitry Lipin
 */
public class InstancesModel extends AbstractListModel
        implements ComboBoxModel {
    /**
     * A vector with the instances
     */
    private Vector instances;

    /**
     * The index of the selected instance
     */
    private int selectedIndex = 0;
    
    /**
     * Creates a new instance of InstancesModel
     *
     * @param instances a vector with the locally found instances
     */
    public InstancesModel(Vector instances) {
        // save the instances
        this.instances = instances;
        
        // set the selected index to zero
        this.selectedIndex = 0;
    }
    
    /**
     * Sets the selected index to the index of the supplied item
     *
     * @param item the instance which should be selected
     */
    public void setSelectedItem(Object item) {
        // set the index to the given item's index or to -1
        // if the item does not exists
        selectedIndex = instances.indexOf(item);
    }
    
    /**
     * Get the instance with the specified instance
     *
     * @param index the index of the desired instance
     *
     * @return the instance at the given index
     */
    public Object getElementAt(int index) {
        return instances.elementAt(index);
    }
    
    /**
     * Returns the total number of instances
     *
     * @return the number of instances
     */
    public int getSize() {
        return instances.size();
    }
    
    /**
     * Returns the instance at the selected index
     *
     * @return the instance at the selected index
     */
    public Object getSelectedItem() {
        // if there are no instances return null
        if (instances.size() == 0) {
            return null;
        }
        
        // return the element at the index
        return instances.elementAt(selectedIndex);
    }
}
