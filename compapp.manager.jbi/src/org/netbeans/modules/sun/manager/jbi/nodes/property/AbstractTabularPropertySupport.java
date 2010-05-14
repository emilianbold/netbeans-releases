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
package org.netbeans.modules.sun.manager.jbi.nodes.property;

import com.sun.esb.management.common.ManagementRemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import org.netbeans.modules.sun.manager.jbi.management.JBIMBeanTaskResultHandler;
import org.netbeans.modules.sun.manager.jbi.nodes.JBIComponentNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Abstract property support for tabular data.
 * 
 * @author jqian
 */
abstract class AbstractTabularPropertySupport
        extends SchemaBasedConfigPropertySupport<TabularDataSupport> {

    private String[] keys;
    private TabularType tabularType;

    AbstractTabularPropertySupport(
            PropertySheetOwner propertySheetOwner,
            Attribute attr,
            MBeanAttributeInfo info,
            String[] keys) {
        super(propertySheetOwner, TabularDataSupport.class, attr, info);
        this.keys = keys;

        TabularData tabularData = (TabularData) attr.getValue();
        if (tabularData != null) {
            this.tabularType = tabularData.getTabularType();
        }
    }

    @Override
    public void setValue(TabularDataSupport newValue) {
        // Compare old and new tablur data and find out the changes.
        TabularData oldValue = getValue();
        List<CompositeData>[] diffs =
                compareTabularData(oldValue, newValue, keys);
        List<CompositeData> additionList = diffs[0];
        List<CompositeData> deletionList = diffs[1];
        List<CompositeData> updateList = diffs[2];

        try {
            String componentName = "?"; // NOI18N
            
            if (propertySheetOwner instanceof JBIComponentNode) {
                componentName = ((JBIComponentNode) propertySheetOwner).getName();
            }
            
            // Remove deleted composite data first
            String operationName = getDeleteCompositeDataOperationName();
            for (CompositeData cd : deletionList) {
                String result = deleteCompositeData(cd);
                JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                    operationName, componentName, result);
            }

            // Add new composite data next
            operationName = getAddCompositeDataOperationName();
            for (CompositeData cd : additionList) {
                String result = addCompositeData(cd);
                JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                    operationName, componentName, result);
            }

            // Update modified composite data
            operationName = getSetCompositeDataOperationName();
            for (CompositeData cd : updateList) {
                String result = setCompositeData(cd);
                JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                    operationName, componentName, result);
            }

            String attrName = attr.getName();

            // Get the new value from runtime
            TabularData newValueFromRuntime = getTabularData();

            attr = new Attribute(attrName, newValueFromRuntime);

            // Check for difference again
            diffs = compareTabularData(oldValue, newValueFromRuntime, keys);
            if (diffs[0].size() > 0 || diffs[1].size() > 0 || diffs[2].size() > 0) {
                checkForPromptToRestart();
            }
        } catch (ManagementRemoteException e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    protected TabularType getTabularType() {
        return tabularType;
    }

    /**
     * Gets the current tabularData from the runtime.
     */
    protected abstract TabularData getTabularData() 
            throws ManagementRemoteException;        
    
    /**
     * Adds a composite data into the tabular data.
     * 
     * @param compositeData     a composite data to be added
     * @return  an XML management message if the operation is a complete 
     *          or partial success
     * @throw ManagementRemoteException if the operation is a complete failure
     */
    protected abstract String addCompositeData(CompositeData compositeData)
            throws ManagementRemoteException;

    /**
     * Deletes a composite data from the tabular data.
     * 
     * @param compositeData     a composite data to be deleted
     * @return  an XML management message if the operation is a complete 
     *          or partial success
     * @throw ManagementRemoteException if the operation is a complete failure
     */
    protected abstract String deleteCompositeData(CompositeData compositeData)
            throws ManagementRemoteException;
    
    /**
     * Updates an existing composite data.
     * 
     * @param compositeData     a composite data to be updated
     * @return  an XML management message if the operation is a complete 
     *          or partial success
     * @throw ManagementRemoteException if the operation is a complete failure
     */
    protected abstract String setCompositeData(CompositeData compositeData)
            throws ManagementRemoteException;

    /**
     * Gets the operation name for adding composite data. 
     * (Used for display purpose when error occurs.)
     * 
     * @return operation name for adding composite data
     */
    protected abstract String getAddCompositeDataOperationName();
    
    /**
     * Gets the operation name for deleting composite data. 
     * (Used for display purpose when error occurs.)
     * 
     * @return operation name for deleting composite data
     */
    protected abstract String getDeleteCompositeDataOperationName();
    
    /**
     * Gets the operation name for setting composite data. 
     * (Used for display purpose when error occurs.)
     * 
     * @return operation name for setting composite data
     */
    protected abstract String getSetCompositeDataOperationName();
    

    private static List<CompositeData>[] compareTabularData(
            TabularData oldTabularData,
            TabularData newTabularData,
            String[] keys) {
        
        Collection<CompositeData> oldCDs = (Collection<CompositeData>) oldTabularData.values();
        Collection<CompositeData> newCDs = (Collection<CompositeData>) newTabularData.values();

        CompositeType rowType = oldTabularData.getTabularType().getRowType();
        @SuppressWarnings("unchecked")
        Set<String> keySet = rowType.keySet();

        List<String> nonKeyList = new ArrayList<String>();
        nonKeyList.addAll(keySet);
        nonKeyList.removeAll(Arrays.asList(keys));
        String[] nonKeys = nonKeyList.toArray(new String[]{});

        List<CompositeData> additionList = new ArrayList<CompositeData>();
        List<CompositeData> deletionList = new ArrayList<CompositeData>();
        List<CompositeData> updateList = new ArrayList<CompositeData>();
        @SuppressWarnings("unchecked")
        List<CompositeData>[] ret =
                new List[]{additionList, deletionList, updateList};

        for ( CompositeData newCD : newCDs) {
            Object[] newKeyValues = newCD.getAll(keys);
            Object[] newNonKeyValues = newCD.getAll(nonKeys);

            boolean found = false;
            for ( CompositeData oldCD : oldCDs) {
                Object[] oldKeyValues = oldCD.getAll(keys);
                Object[] oldNonKeyValues = oldCD.getAll(nonKeys);

                if (compare(newKeyValues, oldKeyValues)) {
                    found = true;
                    if (!compare(newNonKeyValues, oldNonKeyValues)) {
                        updateList.add(newCD);
                    }
                    break;
                }
            }

            if (!found) {
                additionList.add(newCD);
            }
        }

        for ( CompositeData oldCD : oldCDs) {
            Object[] oldKeyValues = oldCD.getAll(keys);

            boolean found = false;
            for ( CompositeData newCD : newCDs) {
                Object[] newKeyValues = newCD.getAll(keys);
                if (compare(oldKeyValues, newKeyValues)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                deletionList.add(oldCD);
            }
        }

        return ret;
    }

    private static boolean compare(Object[] a, Object[] b) {
        assert a.length == b.length;

        for (int i = 0; i < a.length; i++) {
            if ((a[i] == null && b[i] != null) ||
                    (a[i] != null && !a[i].equals(b[i]))) {
                return false;
            }
        }
        return true;
    }
}
