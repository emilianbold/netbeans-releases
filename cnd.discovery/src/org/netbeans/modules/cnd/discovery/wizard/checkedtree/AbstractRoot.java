/*
 * NewInterface.java
 *
 * Created on February 5, 2007, 5:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.discovery.wizard.checkedtree;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author as204739
 */
public interface AbstractRoot {
    Collection<AbstractRoot> getChildren();

    List<String> getFiles();

    String getName();
    
}
