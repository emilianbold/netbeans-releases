/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.vmd.io;

import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.io.providers.DocumentSerializer;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * @author David Kaspar
 */
public class DataObjectContextImpl implements DataObjectContext {

    private static final String PROJECT_TYPE_VMD_UNKNOWN = "#vmd-unknown-project-type"; // NOI18N

    public transient static final HashMap<String, WeakReference<Project>> projectID2project = new HashMap<String, WeakReference<Project>> ();

    private static final long serialVersionUID = -1;

    private DataObject dataObject;
    private transient volatile boolean initialized;
    private transient String projectID;
    private transient String projectType;

    static {
        ActiveViewSupport.getDefault ();
    }

    public DataObjectContextImpl () {
    }

    public DataObjectContextImpl (DataObject dataObject) {
        this ();
        assert dataObject != null;
        this.dataObject = dataObject;
    }

    private void initialize () {
        synchronized (this) {
            if (initialized)
                return;            
            if (ProjectUtils.getProject (this) == null) {
                return;
            }
            projectID = ProjectUtils.getProjectID (ProjectUtils.getProject (this));
            DocumentSerializer documentSerializer = IOSupport.getDocumentSerializer (dataObject);
            projectType = documentSerializer.getProjectType ();
            if (projectType == null)
                projectType = PROJECT_TYPE_VMD_UNKNOWN;
            initialized = true;
        }
    }

    public String getProjectID () {
        initialize ();
        return projectID;
    }

    public String getProjectType () {
        initialize ();
        return projectType;
    }

    public DataObject getDataObject () {
        return dataObject;
    }

    public CloneableEditorSupport getCloneableEditorSupport () {
        return IOSupport.getCloneableEditorSupport (dataObject);
    }

    public void notifyModified () {
        IOSupport.getDataObjectInteface (dataObject).notifyEditorSupportModified ();
    }

    public void updateFromMultiViewElementCallback (MultiViewElementCallback callback) {
    }

    public void addDesignDocumentAwareness (DesignDocumentAwareness listener) {
        IOSupport.getDocumentSerializer (dataObject).addDesignDocumentAwareness (listener);
    }

    public void removeDesignDocumentAwareness (DesignDocumentAwareness listener) {
        IOSupport.getDocumentSerializer (dataObject).removeDesignDocumentAwareness (listener);
    }

    public void forceSave () {
        IOSupport.getDocumentSerializer (dataObject).saveDocument ();
    }
    
}
