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
package org.netbeans.modules.vmd.api.model;

/**
 * This interface represents a version resolver. It is used e.g. in PropertyDescriptor to describe availability of
 * a property in a particular state of a document.
 *
 * @author David Kaspar
 */
public interface Versionable {

    /**
     * Represents a Versionable which is always available. Often used as a default behaviour.
     */
    public static final Versionable FOREVER = new Versionable () {
        public boolean isAvailable (DesignDocument document) {
            return true;
        }
    };

    /**
     * Called to resolve whether the version, which is represented by implementation of this interface, is available
     * for the currect state of a specified document.
     * @param document the document
     * @return true, if the version is available
     */
    boolean isAvailable (DesignDocument document);

}
