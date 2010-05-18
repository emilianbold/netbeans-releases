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
import java.util.Locale;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import com.sun.data.provider.FilterCriteria;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableDataFilter;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.impl.CompareFilterCriteria;
import com.sun.data.provider.impl.TableRowDataProvider;

/**
 * <p>The ValueBindingFilterCriteria is an implementation of {@link FilterCriteria}
 * that compares the value of a {@link ValueBinding} with a predefined
 * <code>compareValue</code>.  A user may specify matches to include less than
 * (<), equal to (==), or greater than (>) the <code>compareValue</code> Object,
 * or any combination of the above.</p>
 *
 * <p>Use the <code>requestMapKey</code> property
 *
 * @see TableDataProvider
 * @see TableDataFilter
 *
 * @author Joe Nuxoll
 */
public class ValueBindingFilterCriteria extends FilterCriteria {

    /**
     *
     */
    public ValueBindingFilterCriteria() {}

    /**
     *
     * @param valueBinding ValueBinding
     */
    public ValueBindingFilterCriteria(ValueBinding valueBinding) {
        this.valueBinding = valueBinding;
    }

    /**
     *
     * @param compareValue The desired compare value
     */
    public ValueBindingFilterCriteria(Object compareValue) {
        this.compareValue = compareValue;
    }

    /**
     *
     * @param valueBinding ValueBinding
     * @param compareValue The desired compare value
     */
    public ValueBindingFilterCriteria(ValueBinding valueBinding, Object compareValue) {
        this.valueBinding = valueBinding;
        this.compareValue = compareValue;
    }

    /**
     *
     * @param valueBinding ValueBinding
     * @param compareValue Object
     * @param matchLessThan boolean
     * @param matchEqualTo boolean
     * @param matchGreaterThan boolean
     */
    public ValueBindingFilterCriteria(ValueBinding valueBinding, Object compareValue,
        boolean matchLessThan, boolean matchEqualTo, boolean matchGreaterThan) {

        this.valueBinding = valueBinding;
        this.compareValue = compareValue;
        this.matchLessThan = matchLessThan;
        this.matchEqualTo = matchEqualTo;
        this.matchGreaterThan = matchGreaterThan;
    }

    /**
     *
     */
    public String getDisplayName() {
        String name = super.getDisplayName();
        if (name != null && !"".equals(name)) {
            return name;
        }

        // if there's no display name, make one...
        Object val = getCompareValue();
        StringBuffer sb = new StringBuffer();
        sb.append(isInclude() ? "Include [" : "Exclude [");
        sb.append(valueBinding != null ? valueBinding.getExpressionString() : "<no value binding>");
        sb.append("] ");
        boolean anyMatches = false;
        if (matchLessThan) {
            anyMatches = true;
            sb.append("is less than ");
        }
        if (matchEqualTo) {
            if (anyMatches) {
                sb.append("OR ");
            }
            anyMatches = true;
            sb.append("is equal to ");
        }
        if (matchGreaterThan) {
            if (anyMatches) {
                sb.append("OR ");
            }
            sb.append("is greater than ");
        }
        sb.append("[" + val + "]");
        return sb.toString();
    }

    /**
     *
     * @param valueBinding ValueBinding
     */
    public void setValueBinding(ValueBinding valueBinding) {
        this.valueBinding = valueBinding;
    }

    /**
     *
     * @return ValueBinding
     */
    public ValueBinding getValueBinding() {
        return valueBinding;
    }

    /**
     * Returns the request map variable key that will be used to store the
     * {@link TableRowDataProvider} for the current row being match tested.
     * This allows value expressions to refer to the "current" row during the
     * filter operation.
     *
     * @return String key to use for the {@link TableRowDataProvider}
     */
    public String getRequestMapKey() {
        return requestMapKey;
    }

    /**
     * Sets the request map variable key that will be used to store the
     * {@link TableRowDataProvider} for the current row being match tested.
     * This allows value expressions to refer to the "current" row during the
     * filter operation.
     *
     * @param requestMapKey String key to use for the {@link TableRowDataProvider}
     */
    public void setRequestMapKey(String requestMapKey) {
        this.requestMapKey = requestMapKey;
    }

    /**
     *
     * @param value Object
     */
    public void setCompareValue(Object value) {
        this.compareValue = value;
    }

    /**
     *
     * @return Object
     */
    public Object getCompareValue() {
        return compareValue;
    }

    /**
     * Storage for the compare locale
     */
    protected Locale compareLocale;

    /**
     *
     * @param compareLocale Locale
     */
    public void setCompareLocale(Locale compareLocale) {
        this.compareLocale = compareLocale;
    }

    /**
     *
     * @return Locale
     */
    public Locale getCompareLocale() {
        return compareLocale;
    }

    /**
     *
     */
    protected boolean matchEqualTo = true;

    /**
     *
     * @param matchEqualTo boolean
     */
    public void setMatchEqualTo(boolean matchEqualTo) {
        this.matchEqualTo = matchEqualTo;
    }

    /**
     *
     * @return boolean
     */
    public boolean isMatchEqualTo() {
        return matchEqualTo;
    }

    /**
     *
     */
    protected boolean matchLessThan = false;

    /**
     *
     * @param matchLessThan boolean
     */
    public void setMatchLessThan(boolean matchLessThan) {
        this.matchLessThan = matchLessThan;
    }

    /**
     *
     * @return boolean
     */
    public boolean isMatchLessThan() {
        return matchLessThan;
    }

    /**
     *
     */
    protected boolean matchGreaterThan = false;

    /**
     *
     * @param matchGreaterThan boolean
     */
    public void setMatchGreaterThan(boolean matchGreaterThan) {
        this.matchGreaterThan = matchGreaterThan;
    }

    /**
     *
     * @return boolean
     */
    public boolean isMatchGreaterThan() {
        return matchGreaterThan;
    }

    /**
     * <p>This method tests a match by comparing the <code>compareValue</code>
     * and the data value stored under the {@link ValueBinding}.  The passed
     * TableDataProvider and RowKey parameters are ignored.  The
     * <code>matchLessThan</code>,  <code>matchEqualTo</code>, and
     * <code>matchGreaterThan</code> properties  are used to determine if a
     * match was found.  The <code>compareLocale</code> is used for String
     * comparisons.</p>
     *
     * {@inheritDoc}
     */
    public boolean match(TableDataProvider provider, RowKey row) {

        if (valueBinding == null) {
            return true;
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

        int compare = CompareFilterCriteria.compare(value, compareValue, compareLocale);
        switch (compare) {
            case -1: // Less Than
                return matchLessThan;
            case 0: // Equal To
                return matchEqualTo;
            case 1: // Greater Than
                return matchGreaterThan;
        }
        return false; // This should never be reached
    }

    private transient ValueBinding valueBinding;
    private Object compareValue;
    private String requestMapKey = "currentRow"; // NOI18N
    private transient TableRowDataProvider rowProvider;
    private String rowProviderLock = "rowProviderLock"; // this is a monitor lock for rowProvider

    private void writeObject(ObjectOutputStream out) throws IOException {

	// Serialize simple objects first
	out.writeObject(compareValue);
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



        if (valueBinding != null) {
            out.writeObject(valueBinding.getExpressionString());
        }
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {

	// Deserialize simple objects first
        compareValue = in.readObject();
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
