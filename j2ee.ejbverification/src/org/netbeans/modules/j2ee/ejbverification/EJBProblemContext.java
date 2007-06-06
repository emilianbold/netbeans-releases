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
package org.netbeans.modules.j2ee.ejbverification;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomasz.Slota
 */
public class EJBProblemContext {
    private FileObject fileObject;private TypeElement clazz;
    private Ejb ejb;
    private EjbJarMetadata metadata;
    private CompilationInfo complilationInfo;

    public EJBProblemContext(FileObject fileObject, TypeElement clazz, Ejb ejb, EjbJarMetadata metadata) {
        this.fileObject = fileObject;
        this.clazz = clazz;
        this.ejb = ejb;
        this.metadata = metadata;
    }
    
    public CompilationInfo getComplilationInfo() {
        return complilationInfo;
    }
    
    public FileObject getFileObject() {
        return fileObject;
    }

    public TypeElement getClazz() {
        return clazz;
    }

    public Ejb getEjb() {
        return ejb;
    }

    public EjbJarMetadata getMetadata() {
        return metadata;
    }
}
