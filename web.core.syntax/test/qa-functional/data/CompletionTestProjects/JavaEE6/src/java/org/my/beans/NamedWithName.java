/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.my.beans;

import javax.inject.Named;

/**
 *
 * @author jindra
 */
@Named("beruska")
public class NamedWithName {

    private int count;
    private boolean isValid;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isIsValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }
}
