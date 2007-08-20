/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.discovery.wizard.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.discovery.wizard.api.NodeConfiguration;

/**
 *
 * @author Alexander Simon
 */
public abstract class NodeConfigurationImpl implements NodeConfiguration {
    private boolean isOverrideIncludes;
    private boolean isOverrideMacros;
    private NodeConfigurationImpl parent;
    private Set<String> userIncludes;
    private Map<String, String> userMacros;

    public NodeConfigurationImpl() {
        userIncludes = new LinkedHashSet<String>();
        userMacros = new HashMap<String,String>();
    }

    public boolean overrideIncludes() {
        return isOverrideIncludes;
    }

    public void setOverrideIncludes(boolean overrideIncludes) {
        isOverrideIncludes = overrideIncludes;
    }

    public boolean overrideMacros() {
        return isOverrideMacros;
    }

    public void setOverrideMacros(boolean overrideMacros) {
        isOverrideMacros = overrideMacros;
    }

    public void setParent(NodeConfigurationImpl parent) {
        this.parent = parent;
    }

    public NodeConfigurationImpl getParent() {
        return parent;
    }

    public Set<String> getUserInludePaths(boolean resulting) {
        if (resulting) {
            return countUserInludePaths();
        } else {
            return userIncludes;
        }
    }

    public void setUserInludePaths(Collection<String> set) {
         userIncludes.clear();
         if (set != null) {
            userIncludes.addAll(set);
         }
    }

    public Map<String, String> getUserMacros(boolean resulting) {
        if (resulting) {
            return countUserMacros();
        } else {
            return userMacros;
        }
    }

    public void setUserMacros(Map<String, String> map) {
        userMacros.clear();
        if (map != null) {
            userMacros.putAll(map);
        }
    }
    
    public Set<String> countUserInludePaths() {
        if (overrideIncludes()) {
            return userIncludes;
        }
        Set<String> result = new LinkedHashSet<String>();
        NodeConfigurationImpl current = this;
        while(current != null){
            result.addAll(current.getUserInludePaths(false));
             if (current.overrideIncludes()) {
                break;
             }
            current = current.getParent();
        }
        return result;
    }
    
    public Map<String, String> countUserMacros() {
        if (overrideMacros()) {
            return userMacros;
        }
        Map<String, String> result =  new HashMap<String,String>();
        NodeConfigurationImpl current = this;
        while(current != null){
            result.putAll(current.getUserMacros(false));
            if (current.overrideMacros()){
                break;
            }
            current = current.getParent();
        }
        return result;
    }
}
