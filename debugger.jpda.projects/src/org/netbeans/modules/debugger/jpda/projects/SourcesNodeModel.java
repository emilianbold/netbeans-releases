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

package org.netbeans.modules.debugger.jpda.projects;

import java.io.File;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;

import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author   Jan Jancura
 */
public class SourcesNodeModel implements NodeModel {

    public static final String SOURCE_ROOT =
        "org/netbeans/modules/debugger/jpda/resources/root";
    public static final String FILTER =
        "org/netbeans/modules/debugger/jpda/resources/Filter";
    
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getBundle(SourcesNodeModel.class).getString("CTL_SourcesModel_Column_Name_Name");
        } else
        if (o instanceof String) {
            File f = new File ((String) o);
            if (f.exists ()) {
                FileObject fo = FileUtil.toFileObject (f);
                Project p = FileOwnerQuery.getOwner (fo);
                if (p != null) {
                    ProjectInformation pi = (ProjectInformation) p.getLookup ().
                        lookup (ProjectInformation.class);
                    return java.text.MessageFormat.format(NbBundle.getBundle(SourcesNodeModel.class).getString(
                            "CTL_SourcesModel_Column_Name_ProjectSources"), new Object [] { f.getPath(), pi.getDisplayName() });
                }
                return java.text.MessageFormat.format(NbBundle.getBundle(SourcesNodeModel.class).getString(
                        "CTL_SourcesModel_Column_Name_LibrarySources"), new Object [] { f.getPath() });
            } else
            return (String) o;
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return NbBundle.getBundle(SourcesNodeModel.class).getString("CTL_SourcesModel_Column_Name_Desc");
        if (o instanceof String) {
            if (((String) o).startsWith ("D"))
                return NbBundle.getBundle(SourcesNodeModel.class).getString("CTL_SourcesModel_Column_Name_DescExclusion");
            else
                return NbBundle.getBundle(SourcesNodeModel.class).getString("CTL_SourcesModel_Column_Name_DescRoot");
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o instanceof String) {
            if (((String) o).startsWith ("D"))
                return FILTER;
            else
                return SOURCE_ROOT;
        } else
        throw new UnknownTypeException (o);
    }

    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }
}
