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

package com.sun.data.provider.impl;

import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.FilterCriteria;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableDataProvider;
import java.util.ResourceBundle;

/**
 * <p>The CompareFilterCriteria is an implementation of {@link FilterCriteria}
 * that compares the value of a {@link FieldKey} in a
 * {@link com.sun.data.provider.TableDataProvider} with a predefined
 * <code>compareValue</code>.  A user may specify matches to include less than
 * (<), equal to (==), or greater than (>) the <code>compareValue</code> Object,
 * or any combination of the above.</p>
 *
 * @see com.sun.data.provider.TableDataProvider
 * @see com.sun.data.provider.TableDataFilter
 *
 * @author Joe Nuxoll
 *         Winston Prakash (Buf Fixes and clean up)
 */
public class CompareFilterCriteria extends FilterCriteria {
    private transient ResourceBundle bundle = null;
    /**
     *
     */
    public CompareFilterCriteria() {}
    
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
    public CompareFilterCriteria(FieldKey fieldKey) {
        this.fieldKey = fieldKey;
    }

    /**
     *
     * @param compareValue The desired compare value
     */
    public CompareFilterCriteria(Object compareValue) {
        this.compareValue = compareValue;
    }

    /**
     *
     * @param fieldKey FieldKey
     * @param compareValue The desired compare value
     */
    public CompareFilterCriteria(FieldKey fieldKey, Object compareValue) {
        this.fieldKey = fieldKey;
        this.compareValue = compareValue;
    }

    /**
     *
     * @param fieldKey FieldKey
     * @param compareValue Object
     * @param matchLessThan boolean
     * @param matchEqualTo boolean
     * @param matchGreaterThan boolean
     */
    public CompareFilterCriteria(FieldKey fieldKey, Object compareValue,
        boolean matchLessThan, boolean matchEqualTo, boolean matchGreaterThan) {

        this.fieldKey = fieldKey;
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
        FieldKey key = getFieldKey();
        Object val = getCompareValue();
        StringBuffer sb = new StringBuffer();
        sb.append(isInclude() ? getBundle().getString("INCLUDE") + " [" : getBundle().getString("EXCLUDE") + " [");
        sb.append(key != null ? key.getDisplayName() : getBundle().getString("NO_DATA_KEY"));
        sb.append("] ");
        boolean anyMatches = false;
        if (matchLessThan) {
            anyMatches = true;
            sb.append(getBundle().getString("LESS_THAN"));
        }
        if (matchEqualTo) {
            if (anyMatches) {
                sb.append(getBundle().getString("OR"));
            }
            anyMatches = true;
            sb.append(getBundle().getString("EQUAL_TO"));
        }
        if (matchGreaterThan) {
            if (anyMatches) {
                sb.append(getBundle().getString("OR"));
            }
            sb.append(getBundle().getString("GREATER_THAN"));
        }
        sb.append("[" + val + "]");
        return sb.toString();
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
     * and the data value stored under the {@link FieldKey} at the specified
     * row.  The <code>matchLessThan</code>, <code>matchEqualTo</code>, and
     * <code>matchGreaterThan</code> properties are used to determine if a
     * match was found.  The <code>compareLocale</code> is used for String
     * comparisons.</p>
     *
     * {@inheritDoc}
     */
    public boolean match(TableDataProvider provider, RowKey row)
        throws DataProviderException {

        if (fieldKey == null) {
            return true;
        }
        Object o = provider.getValue(fieldKey, row);
        int compare = CompareFilterCriteria.compare(o, compareValue, compareLocale);
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

    /*
     * Helper method to compare two objects. This method compares the following
     * types:
     *
     * Boolean
     * Character
     * Comparator
     * Date
     * Number
     * String
     *
     * If the object type is not identified, the object value is compared as a
     * string.
     */
    public static int compare(Object o1, Object o2, Locale compareLocale) {

        // Ensure objects are not null
        if (o1 == null && o2 == null) {
            return 0;
        }
        else if (o1 == o2) {
            return 0;
        }
        else if (o1 == null) {
            return -1;
        }
        else if (o2 == null) {
            return 1;
        }

        // Test object values
        if (o1 instanceof Comparator && o2 instanceof Comparator) {
            return ((Comparator)o1).compare(o1, o2);
        }
        else if (o1 instanceof Character && o2 instanceof Character) {
            return ((Character)o1).compareTo(o2);
        }
        else if (o1 instanceof Date && o2 instanceof Date) {
            return ((Date)o1).compareTo(o2);
        }
        else if (o1 instanceof Number && o2 instanceof Number) {
            Double d1 = new Double(((Number)o1).doubleValue());
            Double d2 = new Double(((Number)o2).doubleValue());
            return d1.compareTo(d2);
        }
        else if (o1 instanceof Boolean && o2 instanceof Boolean) {
            boolean b1 = ((Boolean)o1).booleanValue();
            boolean b2 = ((Boolean)o2).booleanValue();

            if (b1 == b2)
                return 0;
            else if (b1)
                return -1;
            else
                return 1;
        }
        else if (o1 instanceof String && o2 instanceof String) {
            String s1 = (String)o1;
            String s2 = (String)o2;

            // The String.compareTo method performs a binary comparison of the
            // Unicode characters within the two strings. For most languages;
            // however, this binary comparison cannot be relied on to sort
            // strings because the Unicode values do not correspond to the
            // relative order of the characters.
            Collator collator = Collator.getInstance(
                compareLocale != null ? compareLocale : Locale.getDefault());
            collator.setStrength(Collator.IDENTICAL);

            return collator.compare(s1, s2);
        }
        else {
            String s1 = o1.toString();
            String s2 = o2.toString();
            return s1.compareTo(s2);
        }
    }

    private FieldKey fieldKey;
    private Object compareValue;
}
