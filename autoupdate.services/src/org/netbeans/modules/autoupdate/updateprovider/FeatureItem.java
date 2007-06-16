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

package org.netbeans.modules.autoupdate.updateprovider;

import java.util.HashSet;
import java.util.Set;
import org.openide.modules.Dependency;

/**
 *
 * @author Jiri Rechtacek
 */
public class FeatureItem extends UpdateItemImpl {
    
    private String codeName;
    private String specificationVersion;
    private Set<String> dependenciesToModules;
    private Set<String> moduleCodeNames;
    private String displayName;
    private String description;
    private String category;

    public FeatureItem (
            String codeName,
            String specificationVersion,
            Set<String> dependencies,
            String displayName,
            String description,
            String category) {
        if (dependencies == null) {
            throw new IllegalArgumentException ("Cannot create FeatureItem " + codeName + " with null modules."); // NOI18N
        }
        this.codeName = codeName;
        this.specificationVersion = specificationVersion;
        this.dependenciesToModules = dependencies;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
    }
    
    public String getCodeName () {
        return this.codeName;
    }
    
    public String getSpecificationVersion () {
        return this.specificationVersion;
    }
    
    public String getDisplayName () {
        return this.displayName;
    }
    
    public String getDescription () {
        return this.description;
    }
    
    public Set<String> getDependenciesToModules () {
        return this.dependenciesToModules;
    }
    
    public Set<String> getModuleCodeNames () {
        if (moduleCodeNames == null) {
            moduleCodeNames = new HashSet<String> ();
            for (String depSpec : dependenciesToModules) {
                Set<Dependency> deps = Dependency.create (Dependency.TYPE_MODULE, depSpec);
                assert deps.size () == 1 : "Only one dependency for " + depSpec;
                Dependency dep = deps.iterator ().next ();
                assert Dependency.TYPE_MODULE == dep.getType () : "Only Dependency.TYPE_MODULE supported, but " + dep;
                String name = dep.getName ();
                // trim release impl.
                if (name.indexOf ('/') != -1) {
                    int to = name.indexOf ('/');
                    name = name.substring (0, to);
                }
                moduleCodeNames.add (name);
            }
        }
        return moduleCodeNames;
    }
    
    public String getAgreement() {
        assert false : "Not provided yet";
        return null;
    }

    public String getCategory () {
        return category;
    }

}
