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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.jboss4;

import java.util.Vector;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
/**
 *
 * @author whd
 */
class JBTargetModuleID implements TargetModuleID {

    private Target target;
    private String jar_name;
    private String context_url;

    private Vector childs = new Vector();
    private TargetModuleID  parent = null;

    JBTargetModuleID(Target target) {
        this(target, "");
    }

    JBTargetModuleID(Target target, String jar_name) {
        this.target = target;
        this.jar_name = jar_name;

    }
    public void setContextURL(String context_url) {
        this.context_url = context_url;
    }
    public void setJARName(String jar_name) {
        this.jar_name = jar_name;
    }

    public void setParent(JBTargetModuleID parent) {
        this.parent = parent;

    }

    public void addChild(JBTargetModuleID child) {
        childs.add(child);
        child.setParent(this);
    }

    public TargetModuleID[] getChildTargetModuleID() {
        return (TargetModuleID[]) childs.toArray(new TargetModuleID[childs.size()]);
    }
    //Retrieve a list of identifiers of the children of this deployed module.
    public String getModuleID() {
        return jar_name ;
    }
    //         Retrieve the id assigned to represent the deployed module.
    public TargetModuleID getParentTargetModuleID() {

        return parent;
    }
    //Retrieve the identifier of the parent object of this deployed module.
    public Target getTarget() {
        return target;
    }
    //Retrieve the name of the target server.
    public String getWebURL() {
        return context_url;//"http://" + module_id; //NOI18N
    }
    //If this TargetModulID represents a web module retrieve the URL for it.
    public String toString() {
        return getModuleID() +  hashCode();
    }
}