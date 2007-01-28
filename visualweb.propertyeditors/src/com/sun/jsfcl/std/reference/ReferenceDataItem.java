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
package com.sun.jsfcl.std.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ReferenceDataItem implements Comparable {

    protected ReferenceDataItem aliasFor;
    protected boolean isUnsetMarker;
    protected boolean isRemovable;
    protected String name;
    protected Object value;
    protected String javaInitializationString;

    public static ArrayList sorted(List items) {
        ArrayList result;

        result = new ArrayList(items.size());
        result.addAll(items);
        Collections.sort(result);
        return result;
    }

    protected ReferenceDataItem(String name, Object value, String javaInitializationString,
        boolean isUnsetMarker, boolean isRemovable, ReferenceDataItem aliasFor) {

        super();
        this.name = name;
        this.value = value;
        this.javaInitializationString = javaInitializationString;
        this.isUnsetMarker = isUnsetMarker;
        this.isRemovable = isRemovable;
        this.aliasFor = aliasFor;
    }

    public int compareTo(Object object) {
        ReferenceDataItem otherItem;

        otherItem = (ReferenceDataItem)object;
        return getName().compareToIgnoreCase(otherItem.getName());
    }

    public boolean equals(Object object) {

        if (object instanceof ReferenceDataItem) {
            return equals((ReferenceDataItem)object);
        }
        return false;
    }

    public boolean equals(ReferenceDataItem other) {

        if (value != null && other.value != null) {
            if (value.equals(other.value)) {
                return true;
            }
        }
        return false;
    }

    public ReferenceDataItem getAliasFor() {

        return aliasFor;
    }

    public String getDisplayString() {

        if (value instanceof String) {
            return (String)value;
        }
        return name;
    }

    public String getJavaInitializationString() {

        return javaInitializationString;
    }

    public String getName() {

        return name;
    }

    public Object getValue() {

        return value;
    }

    public int hashCode() {

        if (value == null) {
            return 0;
        }
        return value.hashCode();
    }

    public boolean isRemovable() {

        return isRemovable;
    }

    public boolean isUnsetMarker() {

        return isUnsetMarker;
    }

    public boolean matchesPattern(Pattern pattern) {

        Matcher matcher = pattern.matcher(getName());
        if (matcher.matches()) {
            return true;
        }
        if (getValue() instanceof String) {
            matcher = pattern.matcher((String)getValue());
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    public void setIsRemovable(boolean isRemovable) {

        this.isRemovable = isRemovable;
    }

    public void setIsUnsetMarker(boolean isUnsetValue) {

        this.isUnsetMarker = isUnsetValue;
    }

    public void setJavaInitializationString(String string) {

        javaInitializationString = string;
    }

}
