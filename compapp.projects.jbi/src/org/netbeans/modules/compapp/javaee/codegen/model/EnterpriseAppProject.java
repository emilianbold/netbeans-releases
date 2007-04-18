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

/*
 * EnterpriseAppProject.java
 *
 * Created on October 6, 2006, 4:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.javaee.codegen.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author gpatil
 */
public class EnterpriseAppProject extends AbstractProject{
    private List<JavaEEProject> subProjs = new ArrayList<JavaEEProject>();
    
    /**
     * Creates a new instance of EnterpriseAppProject
     */
    public EnterpriseAppProject(String path2eEar) {
        super(path2eEar);
        this.projType = JavaEEProject.ProjectType.ENT;
    }
    
    public void addSubproject(JavaEEProject subProj){
        this.subProjs.add(subProj);
    }
    
    public List<Endpoint> getWebservicesEndpoints() throws IOException {
        Iterator<JavaEEProject> itr = this.subProjs.iterator();
        JavaEEProject subp = null;
        List<Endpoint> epts = new ArrayList<Endpoint>();
        while (itr.hasNext()){
            subp = itr.next();
            epts.addAll(subp.getWebservicesEndpoints());
        }
        
        return epts;
    }
}
