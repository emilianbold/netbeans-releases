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
 * JavaEEProject.java
 *
 * Created on October 6, 2006, 3:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.javaee.codegen.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author gpatil
 */
public interface JavaEEProject {
    public enum ProjectType {EJB, WEB, ENT};
    
    public void setProjectDir(String dir);
    public void setJarPath(String dir);
    public void setProjectType(ProjectType type);
    public void isDeployThruCA(boolean depThruCA);
    public boolean isDeployThruCA();    
    public ProjectType getProjectType();    
    public void setClassPathURLs(List<URL> classpathURLs);
    public void addSubproject(JavaEEProject subProj);    
    public List<Endpoint> getWebservicesEndpoints() throws IOException;
    public String getJarName();
    public String createJar(String jarDestDir, String additionalJbiFileDir) throws Exception;
    public void setResourceFolder(String resourceFolder);
}
