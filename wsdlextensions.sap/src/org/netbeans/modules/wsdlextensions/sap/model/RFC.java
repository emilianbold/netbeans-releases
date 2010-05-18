/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.sap.model;

/**
 *
 * @author jqian
 */
public class RFC {
    private String functionName;
    private String groupName;
    private String shortText;
    private String devClass;

    public RFC(String functionName, String groupName,
            String shortText, String devClass) {
        this.functionName = functionName;
        this.groupName = groupName;
        this.shortText = shortText;
        this.devClass = devClass;
    }
    
    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getShortText() {
        return shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }

    public String getDevClass() {
        return devClass;
    }

    public void setDevClass(String devClass) {
        this.devClass = devClass;
    }

    @Override
    public String toString() {
        return "RFC [" +
                "functionName=" + getFunctionName() +
                ", groupName=" + getGroupName() +
                ", shortText=" + getShortText() +
                ", devClass=" + getDevClass();
    }
}
