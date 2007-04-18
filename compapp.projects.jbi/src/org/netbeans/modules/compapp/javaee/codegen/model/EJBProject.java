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
 * EJBProject.java
 *
 * Created on September 29, 2006, 3:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.javaee.codegen.model;

import java.util.logging.Logger;

/**
 * Scans from the root for any Class files.
 * Scans for JAX-WS annotations.
 * @TODO Scaning any other jars embedded inside ejb jar.
 * @author gpatil
 */
public class EJBProject extends AbstractProject{
    private static Logger logger = Logger.getLogger(EJBProject.class.getName());
    
    /**
     * Creates a new instance of EJBProject
     */
    public EJBProject(String ejbJarPath) {
        super(ejbJarPath);
        this.projType = JavaEEProject.ProjectType.EJB;
    }
}
