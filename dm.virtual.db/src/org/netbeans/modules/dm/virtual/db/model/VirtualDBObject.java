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
package org.netbeans.modules.dm.virtual.db.model;

import java.util.ArrayList;
import java.util.List;

import org.openide.util.NbBundle;

/**
 * This basic class provides sql framework functionality to all SQLObjects
 * 
 * @author Ahimanikya Satapathy
 */
public abstract class VirtualDBObject implements Cloneable {

    protected transient String displayName;
    protected transient String id;
    protected transient String objectType;
    protected transient Object parentObject;
    protected int type;
    private transient boolean isIdSet = false;

    public VirtualDBObject() {
    }

    public Object cloneSQLObject() throws CloneNotSupportedException {
        return this.clone();
    }

    public void copyFromSource(VirtualDBObject source) {
        if (source == null) {
            throw new java.lang.IllegalArgumentException(NbBundle.getMessage(VirtualDBObject.class, "MSG_Null_SQLInstance") + source);
        }
        this.displayName = source.getDisplayName();
        // id is set only once for an object
        if (this.getId() == null) {
            this.id = source.getId();
        }

        this.type = source.getObjectType();
        this.parentObject = source.getParentObject();

    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        }

        boolean response = false;

        if (o instanceof VirtualDBObject) {
            VirtualDBObject target = (VirtualDBObject) o;

            // check for type
            response = (type == target.type);

            // check for display name
            response &= (this.getDisplayName() != null) ? this.getDisplayName().equals(target.getDisplayName()) : (target.getDisplayName() == null);

            // check for id
            // FOR NOW we check if both ids are avialable then only do equal
            // comparison, TODO: in future we should always do id comparison
            // we need to make sure that id is always available for that.
            if (this.id != null && target.id != null) {
                response &= (this.id != null) ? this.id.equals(target.getId()) : (target.getId() == null);
            }
        }

        return response;
    }

    public List getChildSQLObjects() {
        return new ArrayList(1);
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getId() {
        return this.id;
    }

    public int getObjectType() {
        return type;
    }

    public VirtualDBObject getOutput(String argName) throws VirtualDBException {
        return this;
    }

    public Object getParentObject() {
        return this.parentObject;
    }

    @Override
    public int hashCode() {
        return type + ((id != null) ? id.hashCode() : 0);
    }

    public void reset() {
        this.id = null;
        this.isIdSet = false;
        this.parentObject = null;
    }

    public void setDisplayName(String newName) {
        displayName = (newName != null) ? newName.trim() : "";
    }

    public void setId(String newId) throws VirtualDBException {
        // ID will be set only once in this object's lifetime.
        if (isIdSet) {
            return;
        }

        if (newId == null) {
            throw new VirtualDBException(NbBundle.getMessage(VirtualDBObject.class, "MSG_Null_newId"));
        }

        isIdSet = true;
        this.id = newId;
    }

    public void setParentObject(Object newParent) throws VirtualDBException {
        if (newParent == null) {
            throw new VirtualDBException(NbBundle.getMessage(VirtualDBObject.class, "MSG_Null_newParent"));
        }
        this.parentObject = newParent;
    }
}

