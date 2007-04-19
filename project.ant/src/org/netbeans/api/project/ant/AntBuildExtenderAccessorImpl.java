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
package org.netbeans.api.project.ant;

import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.ant.AntBuildExtender.Extension;
import org.netbeans.modules.project.ant.AntBuildExtenderAccessor;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;

/**
 *
 * @author mkleint
 */
class AntBuildExtenderAccessorImpl extends  AntBuildExtenderAccessor {

    static void createAccesor() {
        if (DEFAULT == null) {
            DEFAULT= new AntBuildExtenderAccessorImpl();
        }
    }
    
    private AntBuildExtenderAccessorImpl() {
    }
    

    public AntBuildExtender createExtender(AntBuildExtenderImplementation impl) {
        return new AntBuildExtender(impl);
    }

    public Set<Extension> getExtensions(AntBuildExtender ext) {
        return ext.getExtensions();
    }

    public String getPath(Extension extension) {
        return extension.getPath();
    }

    public Map<String, Collection<String>> getDependencies(Extension extension) {
        return extension.getDependencies();
    }

}
