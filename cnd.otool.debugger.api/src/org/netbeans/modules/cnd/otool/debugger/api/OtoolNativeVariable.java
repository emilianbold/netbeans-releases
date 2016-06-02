/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.cnd.otool.debugger.api;

/**
 *
 * @author Nikolay Koldunov
 */
abstract public class OtoolNativeVariable implements java.io.Serializable {  //FIXME package visibility

    private String userName;
    private String name;
    private String type;
    private String value;
    protected OtoolNativeVariable[] children = null;

    protected final OtoolNativeVariable parent;
    protected boolean isPtr = false;

    abstract public boolean isEditable();

    @Override
    public boolean equals(Object obj) {     //for testing purposes only
        if (!(obj instanceof OtoolNativeVariable)) {
            return false;
        }
        if (!((OtoolNativeVariable) obj).name.equals(name)) {
            return false;
        }
        if (!((OtoolNativeVariable) obj).type.equals(type)) {
            return false;
        }
        if (!((OtoolNativeVariable) obj).value.equals(value)) {
            return false;
        }
        return true;
    }

    protected OtoolNativeVariable(OtoolNativeVariable parent, String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.parent = parent;
        this.userName = name;
    }

    public String getType() {
        return type;
    }

    public void setUserFriendlyName (String userName) {
        this.userName = userName;
    }

    public void setPtr(boolean e) {
	isPtr = e;
    }

    public boolean isPtr() {
	return isPtr;
    }

    protected final void setType(String type) {
        this.type = type;
    }

    /**
     * Getter for textual representation of the value. It converts the value to
     * a string representation. So if the variable represents null reference,
     * the returned string will be for example "null". That is why null can be
     * returned when the watch is not valid
     *
     * @return the value of this watch or null if the watch is not in the scope
     */
    public String getAsText() {
        return value;
    }

    public String getFullName() {
        StringBuilder res = new StringBuilder(name);
        OtoolNativeVariable p = parent;
        while (p != null) {
            // LATER: need to insert -> or . based on type
            res.insert(0, p.getName() + '.');
            p = p.parent;
        }
        return res.toString();
    }

    public void setAsText(String value) {
        // This is intended for use by user actions, not setting by the engine
        // the way I'm currently usingit.
        Object ovalue = this.value;
        // 6536351, 6520382
        if (value == null) {
            this.value = "<null>"; // NOI18N
        } else {
            this.value = value.trim();
        }
    }

    /*public void setType(String type) {
        this.type = type;
    }*/
    public String getName() {
        //return user friendly name
        return userName;
    }

    /*public void setName(String name) {
        this.name = name;
    }*/
    public String getValue() {
        return value;
    }

    public boolean isWatch() {
        return false;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "{type:\"" + type + "\",name:\"" + name + "\",value:\"" + value + "\"}"; // NOI18N
    }

    public String getIdentity() {
        return name;
    }

    public void setChildren(OtoolNativeVariable[] children, boolean andUpdate) {
        this.children = children;
    }

    private void addChildren(OtoolNativeVariable[] extra) {
        OtoolNativeVariable[] child_list = new OtoolNativeVariable[extra.length + children.length];
        int vx = 0;
        for (vx = 0; vx < children.length; vx++) {
            child_list[vx] = children[vx];
        }
        int index = vx;
        for (vx = 0; vx < extra.length; vx++) {
            child_list[index + vx] = extra[vx];
        }

        children = child_list;
    }

    public void addChildren(OtoolNativeVariable[] extra, boolean andUpdate) {
        if (extra == null) {
            return;
        }

        addChildren(extra);

    }

    public int getNumChild() {
        return getChildren().length;
    }

    public OtoolNativeVariable[] getChildren() {
        if (children != null) {
            return children;
        } else {
            return new OtoolNativeVariable[0];		// see IZ 99042
        }
    }

    /**
     * Shows the next 100 children of the variable Support for
     * GdbVariable.getNextChildren()
     */
    public void getMoreChildren() {
    }
}
