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


package com.sun.data.provider;


import java.io.Serializable;

/**
 * <p>FieldKey is a representation of an identifier for a specific data
 * element that may be retrieved from a {@link DataProvider}.  Specialized
 * implementations might also provide extra capabilities for navigation
 * between elements, or other value added services.</p>
 *
 * <p>FieldKey implements Comparable, to allow for sorting based on the
 * displayName of the FieldKey.  Note that the Comparable equals test may
 * not correspond to the FieldKey.equals(...) test, as the Comparable
 * implementation is working with the displayName, while the equals(...)
 * method works with the fieldId.</p>
 *
 * @author Joe Nuxoll
 * @author Craig McClanahan
 */
public class FieldKey implements Comparable, Serializable {

    /**
     * A convenient static empty array to use for no-op method returns
     */
    public static final FieldKey[] EMPTY_ARRAY = new FieldKey[0];

    /**
     * Constructs a new FieldKey with the specified canonical ID.
     *
     * @param fieldId The desired canonical ID String
     */
    public FieldKey(String fieldId) {
        this.fieldId = fieldId;
        this.displayName = this.fieldId;
    }

    /**
     * Constructs a new FieldKey with the specified canonical ID and display
     * name.
     *
     * @param fieldId The desired canonical ID String for this field
     * @param displayName The desired display name String
     */
    public FieldKey(String fieldId, String displayName) {
        this.fieldId = fieldId;
        this.displayName = displayName;
    }

    /**
     * @param fieldId the canonical internal identifier of this {@link FieldKey}
     */
    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    /**
     * @return the canonical internal identifier of this {@link FieldKey}
     */
    public String getFieldId() {
        return fieldId;
    }

    /**
     * @param displayName The display name for this data element, suitable for
     * inclusion in a menu of available options.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the display name for this data element, suitable for
     * inclusion in a menu of available options.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Standard equals implementation.  This method compares the FieldKey id
     * values for equality.
     *
     * @param o the Object to check equality
     * @return true if equal, false if not
     * @see Object#equals(Object)
     */
    public boolean equals(Object o) {
        if (o instanceof FieldKey) {
            String fkFieldId = ((FieldKey)o).getFieldId();
            String thisFieldId = getFieldId();
            return thisFieldId == fkFieldId || (thisFieldId != null && thisFieldId.equals(fkFieldId));
        }
        return false;
    }
    
    /**
     * @return the hashCode of a blank String if the FieldKey id is null, or the
     * hashCode of the FieldKey id otherwise.
     * @see Object#hashCode()
     */
    public int hashCode() {
        String thisFieldId = getFieldId();
        if (thisFieldId == null) {
            return "".hashCode();
        }
        return thisFieldId.hashCode();
    }

    /**
     * Standard compareTo implementation (for {@link Comparable} interface).
     * This method compares FieldKeys for sorting by comparing the displayName
     * values.
     *
     * @param o Object to compare
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o) {
        String thisDisplayName = getDisplayName();
        if (o instanceof FieldKey && thisDisplayName != null) {
            String fkDisplayName = ((FieldKey)o).getDisplayName();
            return thisDisplayName.compareTo(fkDisplayName);
        }
        return 0;
    }

    private String fieldId;
    private String displayName;

    public String toString() {
        return "FieldKey[" + getFieldId() + "]"; // NOI18N
    }
}
