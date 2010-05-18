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
 * <p>The ValueExpressionFilterCriteria is an implementation of
 * {@link FilterCriteria} that compares the value of a {@link String}
 * (created with the specified value expression) with a predefined
 * <code>compareValue</code>.  A user may specify matches to include less than
 * (<), equal to (==), or greater than (>) the <code>compareValue</code> Object,
 * or any combination of the above.</p>
 *
 * @see TableDataProvider
 * @see TableDataFilter
 *
 * @author Joe Nuxoll
 */
public class ValueExpressionFilterCriteria extends FilterCriteria {

    /**
     *
     */
    public ValueExpressionFilterCriteria() {}

    /**
     *
     * @param valueExpression String
     */
    public ValueExpressionFilterCriteria(String valueExpression) {
        this.valueExpression = valueExpression;
    }

    /**
     *
     * @param compareValue The desired compare value
     */
    public ValueExpressionFilterCriteria(Object compareValue) {
        this.compareValue = compareValue;
    }

    /**
     *
     * @param valueExpression String
     * @param compareValue The desired compare value
     */
    public ValueExpressionFilterCriteria(String valueExpression, Object compareValue) {
        this.valueExpression = valueExpression;
        this.compareValue = compareValue;
    }

    /**
     *
     * @param valueExpression String
     * @param compareValue Object
     * @param matchLessThan boolean
     * @param matchEqualTo boolean
     * @param matchGreaterThan boolean
     */
    public ValueExpressionFilterCriteria(String valueExpression, Object compareValue,
        boolean matchLessThan, boolean matchEqualTo, boolean matchGreaterThan) {

        this.valueExpression = valueExpression;
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
        sb.append(valueExpression != null ? valueExpression : "<no value expression>");
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
     * @param valueExpression String
     */
    public void setValueExpression(String valueExpression) {
        this.valueExpression = valueExpression;
    }

    /**
     *
     * @return String
     */
    public String getValueExpression() {
        return valueExpression;
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
     * and the data value stored under the {@link String}.  The passed
     * TableDataProvider and RowKey parameters are ignored.  The
     * <code>matchLessThan</code>,  <code>matchEqualTo</code>, and
     * <code>matchGreaterThan</code> properties  are used to determine if a
     * match was found.  The <code>compareLocale</code> is used for String
     * comparisons.</p>
     *
     * {@inheritDoc}
     */
    public boolean match(TableDataProvider provider, RowKey row) {
        if (valueExpression == null || "".equals(valueExpression)) {
            return true;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ValueBinding valueBinding = facesContext.getApplication().createValueBinding(valueExpression);

        if (valueBinding == null) {
            return true;
        }

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

    private String valueExpression;
    private Object compareValue;
    private String requestMapKey = "currentRow"; // NOI18N
    private transient TableRowDataProvider rowProvider;
    private String rowProviderLock = "rowProviderLock"; // this is a monitor lock for rowProvider
}
