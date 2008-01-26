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

import com.sun.esb.management.api.configuration.ConfigurationService;
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
            JBIComponentNode parent,
            Attribute attr,
            MBeanAttributeInfo info,
            String[] keys) {
        super(parent, TabularDataSupport.class, attr, info);
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
            // Remove deleted composite data first
            for (CompositeData cd : deletionList) {
                deleteCompositeData(cd);
            }

            // Add new composite data next
            for (CompositeData cd : additionList) {
                addCompositeData(cd);
            }

            // Update modified composite data
            for (CompositeData cd : updateList) {
                setCompositeData(cd);
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
     */
    protected abstract void addCompositeData(CompositeData compositeData)
            throws ManagementRemoteException;

    /**
     * Deletes a composite data from the tabular data.
     */
    protected abstract void deleteCompositeData(CompositeData compositeData)
            throws ManagementRemoteException;

    /**
     * Updates an existing composite data.
     */
    protected abstract void setCompositeData(CompositeData compositeData)
            throws ManagementRemoteException;

    private static List<CompositeData>[] compareTabularData(
            TabularData oldTabularData,
            TabularData newTabularData,
            String[] keys) {
        @SuppressWarnings("unchecked")
        Collection<CompositeData> oldCDs = oldTabularData.values();
        @SuppressWarnings("unchecked")
        Collection<CompositeData> newCDs = newTabularData.values();

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
