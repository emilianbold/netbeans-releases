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
package com.sun.rave.web.ui.faces;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.SortCriteria;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.impl.TableRowDataProvider;

/**
 * <p>The ValueBindingSortCriteria class is an implementation of SortCriteria
 * that simply retrieves the sort value from the {@link ValueBinding}.</p>
 *
 * @author Joe Nuxoll
 */
public class ValueBindingSortCriteria extends SortCriteria {

    /**
     * Constructs a ValueBindingSortCriteria with no associated {@link ValueBinding}.
     */
    public ValueBindingSortCriteria() {}

    /**
     * Constructs a ValueBindingSortCriteria with the specified {@link ValueBinding}.
     *
     * @param valueBinding The desired ValueBinding
     */
    public ValueBindingSortCriteria(ValueBinding valueBinding) {
        this.valueBinding = valueBinding;
    }

    /**
     * Constructs a ValueBindingSortCriteria with the specified {@link ValueBinding} and
     * ascending state.
     *
     * @param valueBinding The desired ValueBinding
     * @param ascending The desired boolean state for the ascending property
     */
    public ValueBindingSortCriteria(ValueBinding valueBinding, boolean ascending) {
        this.valueBinding = valueBinding;
        this.setAscending(ascending);
    }

    /**
     * Returns the ValueBinding to use for this sort criteria.
     *
     * @return The currently set ValueBinding for this sort criteria
     */
    public ValueBinding getValueBinding() {
        return valueBinding;
    }

    /**
     * Sets the ValueBinding for this sort criteria.
     *
     * @param valueBinding The desired ValueBinding for this sort criteria
     */
    public void setValueBinding(ValueBinding valueBinding) {
        this.valueBinding = valueBinding;
    }

    /**
     * Returns the request map variable key that will be used to store the
     * {@link TableRowDataProvider} for the current row being sorted.  This
     * allows value expressions to refer to the "current" row during the sort
     * operation.
     *
     * @return String key to use for the {@link TableRowDataProvider}
     */
    public String getRequestMapKey() {
        return requestMapKey;
    }

    /**
     * Sets the request map variable key that will be used to store the
     * {@link TableRowDataProvider} for the current row being sorted.  This
     * allows value expressions to refer to the "current" row during the sort
     * operation.
     *
     * @param requestMapKey String key to use for the {@link TableRowDataProvider}
     */
    public void setRequestMapKey(String requestMapKey) {
        this.requestMapKey = requestMapKey;
    }

    /**
     * <p>If no display name is set, this returns the {@link ValueBinding}'s
     * display name.</p>
     *
     * {@inheritDoc}
     */
    public String getDisplayName() {
        String name = super.getDisplayName();
        if ((name == null || "".equals(name)) && valueBinding != null) {
            return valueBinding.getExpressionString();
        }
        return name;
    }

    /**
     * Returns the ValueBinding's value expresssion string.
     *
     * {@inheritDoc}
     */
    public String getCriteriaKey() {
        return valueBinding != null ? valueBinding.getExpressionString() : ""; // NOI18N
    }

    /**
     * <p>Returns the value from the {@link ValueBinding} ignoring the arguments.</p>
     *
     * {@inheritDoc}
     */
    public Object getSortValue(TableDataProvider provider, RowKey row) {

        if (valueBinding == null) {
            return null;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map requestMap = facesContext.getExternalContext().getRequestMap();
        Object value = null;

        synchronized (rowProviderLock) {

            Object storedRequestMapValue = null;
            if (requestMapKey != null && !"".equals(requestMapKey)) {
                storedRequestMapValue = requestMap.get(requestMapKey);
                if (rowProvider == null) {
                    rowProvider = new TableRowDataProvider();
                }
                rowProvider.setTableDataProvider(provider);
                rowProvider.setTableRow(row);
                requestMap.put(requestMapKey, rowProvider);
            }

            value = valueBinding.getValue(facesContext);

            if (requestMapKey != null && !"".equals(requestMapKey)) {
                if (rowProvider != null) {
                    rowProvider.setTableDataProvider(null);
                    rowProvider.setTableRow(null);
                }
                requestMap.put(requestMapKey, storedRequestMapValue);
            }
        }

        return value;
    }

    private transient ValueBinding valueBinding;
    private String requestMapKey = "currentRow"; // NOI18N
    private transient TableRowDataProvider rowProvider;
    private String rowProviderLock = "rowProviderLock"; // this is a monitor lock for rowProvider

    private void writeObject(ObjectOutputStream out) throws IOException {

	// Serialize simple objects first
	out.writeObject(requestMapKey);
	out.writeObject(rowProviderLock);

	// Serialize valueBinding specially
        if (valueBinding != null) {
            out.writeObject(valueBinding.getExpressionString());
	} else {
            out.writeObject((String) null);
        }

	// NOTE - rowProvider is reconstituted on demand,
	// so we don't need to serialize it

    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {

	// Deserialize simple objects first
	requestMapKey = (String) in.readObject();
	rowProviderLock = (String) in.readObject();

        // Deserialize valueBinding specially
        String s = (String) in.readObject();
        if (s != null) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            valueBinding = facesContext.getApplication().createValueBinding(s);
        } else {
            valueBinding = null;
        }
    }

}
