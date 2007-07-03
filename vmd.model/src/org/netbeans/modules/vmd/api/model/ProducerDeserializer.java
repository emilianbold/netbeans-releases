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
 *
 */
package org.netbeans.modules.vmd.api.model;

/**
 * Used for processing custom producers defined as an xml file in component registry.
 * An implementation of this class should be registered in global lookup.
 *
 * @author David Kaspar
 */
public abstract class ProducerDeserializer {

    private String projectType;

    /**
     * Creates a PresenterDeserializer releated to specified project type.
     * @param projectType the project type
     */
    protected ProducerDeserializer (String projectType) {
        this.projectType = projectType;
    }

    /**
     * Returns the related project type.
     * @return the project type
     */
    public final String getProjectType () {
        return projectType;
    }

    /**
     * Called to deserialize a node in a xml file into a presenter descriptor.
     * @param document the document where the producer could be used
     * @param producer the componen producer to be checked.
     * @return the presenter descriptor
     */
    public abstract boolean checkValidity (DesignDocument document, ComponentProducer producer);

}
