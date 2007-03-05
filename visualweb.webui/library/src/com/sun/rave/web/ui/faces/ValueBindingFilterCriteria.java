/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
