/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.sap.model;

/**
 *
 * @author jqian
 */
public class IDocType {
    private String name;
    private String description;
    private String releaseIn;

    public IDocType(String name, String description, String releaseIn) {
        this.name = name;
        this.description = description;
        this.releaseIn = releaseIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReleaseIn() {
        return releaseIn;
    }

    public void setReleaseIn(String releaseIn) {
        this.releaseIn = releaseIn;
    }

    @Override
    public String toString() {
        return "IDocType [name=" + getName() +
                ", description=" + getDescription() +
                ", releaseIn=" + getReleaseIn() + "]";
    }

}
