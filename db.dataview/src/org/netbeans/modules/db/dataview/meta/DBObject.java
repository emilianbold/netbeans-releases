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
package org.netbeans.modules.db.dataview.meta;

/**
 * This basic class provides sql framework functionality to all DBObjects
 * 
 * @author Ahimanikya Satapathy
 */
public abstract class DBObject <Parent> {

    protected transient String displayName;
    protected transient Parent parentObject;

    /** Creates a new instance of DBObject */
    public DBObject () {
    }

    /**
     * Overrides default implementation to compute hashcode based on any associated
     * attributes as well as values of non-transient member variables.
     * 
     * @param o Object to test for equality with this
     * @return hashcode for this instance
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        }

        boolean response = false;

        if (o instanceof DBObject) {
            DBObject target = (DBObject) o;

            // check for display name
            response &= (this.getDisplayName() != null) ? this.getDisplayName().equals(target.getDisplayName()) : (target.getDisplayName() == null);

        }

        return response;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Parent getParentObject() {
        return this.parentObject;
    }

    public void setDisplayName(String newName) {
        displayName = (newName != null) ? newName.trim() : "";
    }

    public void setParentObject(Parent newParent) {
        this.parentObject = newParent;
    }

    /**
     * Indicates whether a string is null or empty.
     *
     * @param str string to chec for null.
     * @return true if string is null or blank, else false.
     */
    static boolean isNullString(String str) {
        return (str == null || str.trim().length() == 0);
    }
}

