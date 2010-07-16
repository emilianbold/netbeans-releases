/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.my.beans;

import java.util.Collection;
import javax.faces.bean.ManagedBean;

/**
 *
 * @author jindra
 */
@ManagedBean
public class MyManagedBean {

    private boolean stable;
    private Collection<String> descriptions;

    public Collection<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Collection<String> descriptions) {
        this.descriptions = descriptions;
    }

    public boolean isStable() {
        return stable;
    }

    public void setStable(boolean stable) {
        this.stable = stable;
    }
}
