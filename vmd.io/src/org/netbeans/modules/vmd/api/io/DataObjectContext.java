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
package org.netbeans.modules.vmd.api.io;

import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;

import java.io.Serializable;

/**
 * @author David Kaspar
 */
// TODO - check for possible memory leaks - check whether the dataobject is really closed when its editor window is closed
// TODO - should not it be final class?
public interface DataObjectContext extends Serializable {

    public String getProjectID ();

    public String getProjectType ();

    public DataObject getDataObject ();

    public CloneableEditorSupport getCloneableEditorSupport ();

    public void notifyModified ();

    public void addDesignDocumentAwareness (DesignDocumentAwareness listener);

    public void removeDesignDocumentAwareness (DesignDocumentAwareness listener);

    /**
     * WARNING - Temporary method - do not use
     */
    public void forceSave ();

}
