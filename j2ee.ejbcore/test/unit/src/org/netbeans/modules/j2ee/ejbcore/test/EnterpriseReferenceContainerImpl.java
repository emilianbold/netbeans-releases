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

package org.netbeans.modules.j2ee.ejbcore.test;

import java.io.IOException;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.api.ejbjar.MessageDestinationReference;
import org.netbeans.modules.j2ee.api.ejbjar.ResourceReference;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class EnterpriseReferenceContainerImpl implements EnterpriseReferenceContainer {

    private EjbReference remoteEjbReference;
    private String remoteEjbRefName;
    private FileObject remoteReferencingFile;
    private String remoteReferencingClass;
    private EjbReference localEjbReference;
    private String localEjbRefName;
    private FileObject localReferencingFile;
    private String localReferencingClass;

    public EnterpriseReferenceContainerImpl() {}

    public String addEjbReference(EjbReference ref, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException {            
        this.remoteEjbReference = ref;
        this.remoteEjbRefName = ejbRefName;
        this.remoteReferencingFile = referencingFile;
        this.remoteReferencingClass = referencingClass;
        return null;
    }

    public String addEjbLocalReference(EjbReference localRef, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException {
        this.localEjbReference = localRef;
        this.localEjbRefName = ejbRefName;
        this.localReferencingFile = referencingFile;
        this.localReferencingClass = referencingClass;
        return null;
    }

    public String getServiceLocatorName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServiceLocatorName(String serviceLocator) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String addDestinationRef(MessageDestinationReference ref, FileObject referencingFile, String referencingClass) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String addResourceRef(ResourceReference ref, FileObject referencingFile, String referencingClass) throws IOException {
        return "testJndiName";
    }

    public String getLocalEjbRefName() {
        return localEjbRefName;
    }

    public EjbReference getLocalEjbReference() {
        return localEjbReference;
    }

    public String getLocalReferencingClass() {
        return localReferencingClass;
    }

    public FileObject getLocalReferencingFile() {
        return localReferencingFile;
    }

    public String getRemoteEjbRefName() {
        return remoteEjbRefName;
    }

    public EjbReference getRemoteEjbReference() {
        return remoteEjbReference;
    }

    public String getRemoteReferencingClass() {
        return remoteReferencingClass;
    }

    public FileObject getRemoteReferencingFile() {
        return remoteReferencingFile;
    }
    
}
