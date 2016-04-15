/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        return "{type:\"" + type + "\",name:\"" + name + "\",value:\"" + value + "\"}";
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
