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
package org.netbeans.modules.vmd.api.io;

import org.netbeans.modules.vmd.api.model.TypeID;
import org.openide.util.Lookup;

import java.util.Collection;

/**
 * @author David Kaspar
 */
public abstract class ProjectTypeInfo {

    private String projectType;

    protected ProjectTypeInfo (String projectType) {
        assert projectType != null;
        this.projectType = projectType;
    }

    public abstract String getIconResource ();

    public abstract TypeID getRootCDTypeID ();

    public abstract String getDocumentVersion ();

    public abstract Collection<String> getTags ();
    
    public static ProjectTypeInfo getProjectTypeInfoFor (String projectType) {
        for (ProjectTypeInfo info : Lookup.getDefault ().lookupAll (ProjectTypeInfo.class))
            if (info.projectType.equals (projectType))
                return info;
        return null;
    }

}
