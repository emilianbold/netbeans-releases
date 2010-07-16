/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.my.beans;

import javax.faces.bean.ManagedBean;

/**
 *
 * @author jindra
 */
@ManagedBean(name = "slunicko")
public class ManagedBeanWithName {

    private int cloudsCount;
    private double shine;

    public int getCloudsCount() {
        return cloudsCount;
    }

    public void setCloudsCount(int cloudsCount) {
        this.cloudsCount = cloudsCount;
    }

    public double getShine() {
        return shine;
    }

    public void setShine(double shine) {
        this.shine = shine;
    }
}
