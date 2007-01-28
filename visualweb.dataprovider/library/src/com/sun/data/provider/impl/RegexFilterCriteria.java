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

package com.sun.data.provider.impl;

import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.FilterCriteria;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableDataProvider;

/**
 * <p>The RegexFilterCriteria is an implementation of {@link FilterCriteria}
 * that matches a regular expression with the <code>toString()</code> value of
 * a {@link FieldKey} in a {@link TableDataProvider}.</p>
 *
 * @author Joe Nuxoll
 */
public class RegexFilterCriteria extends FilterCriteria {

     private transient ResourceBundle bundle = null;
    /**
     *
     */
    public RegexFilterCriteria() {}

    /**
     * <p>Return the resource bundle containing our localized messages.</p>
     */
    private ResourceBundle getBundle() {

        if (bundle == null) {
            bundle = ResourceBundle.getBundle("com/sun/data/provider/impl/Bundle");
        }
        return bundle;

    }
    
    /**
     *
     * @param fieldKey FieldKey
     */
    public RegexFilterCriteria(FieldKey fieldKey) {
        this.fieldKey = fieldKey;
    }

    /**
     *
     * @param expression String
     * @throws PatternSyntaxException
     */
    public RegexFilterCriteria(String expression) throws PatternSyntaxException {
        setExpression(expression);
    }

    /**
     *
     * @param fieldKey FieldKey
     * @param expression String
     * @throws PatternSyntaxException
     */
    public RegexFilterCriteria(FieldKey fieldKey, String expression)
        throws PatternSyntaxException {

        this.fieldKey = fieldKey;
        setExpression(expression);
    }

    /**
     *
     */
    public String getDisplayName() {
        String name = super.getDisplayName();
        if (name != null && !"".equals(name)) {
            return name;
        }

        FieldKey key = getFieldKey();
        String expr = getExpression();
        return (isInclude() ? getBundle().getString("INCLUDE") + " [" : getBundle().getString("EXCLUDE") + " [") +
            (key != null ? key.getDisplayName() : getBundle().getString("NO_DATA_KEY")) + "] " + getBundle().getString("REGULAR_EXPRESSION_MATCH") +
            (expr != null ? "\"" + getExpression() + "\"" : getBundle().getString("NO_EXPRESSION"));
    }

    /**
     *
     * @param fieldKey FieldKey
     */
    public void setFieldKey(FieldKey fieldKey) {
        this.fieldKey = fieldKey;
    }

    /**
     *
     * @return FieldKey
     */
    public FieldKey getFieldKey() {
        return fieldKey;
    }

    /**
     *
     * @param expression String
     * @throws PatternSyntaxException
     */
    public void setExpression(String expression) throws PatternSyntaxException {
        this.pattern = Pattern.compile(expression);
    }

    /**
     *
     * @return String
     */
    public String getExpression() {
        return pattern != null ? pattern.pattern() : null;
    }

    /**
     * <p>This method tests for a pattern match in the <code>toString()</code>
     * value of the data item stored under the {@link FieldKey} at the specified
     * row.  A match is determined using the currently set regular expression.
     * </p>
     *
     * {@inheritDoc}
     */
    public boolean match(TableDataProvider provider, RowKey row)
        throws DataProviderException {

        if (fieldKey == null || pattern == null) {
            return isInclude();
        }
        Object o = provider.getValue(fieldKey, row);
        if (o == null) {
            return !isInclude();
        }
        try {
            return pattern.matcher(o.toString()).matches();
        }
        catch (Exception x) {
            return false;
        }
    }

    /**
     * Storage for the {@link FieldKey}
     */
    private FieldKey fieldKey;

    /**
     * Storage for the {@link Pattern}
     */
    protected Pattern pattern;
}
