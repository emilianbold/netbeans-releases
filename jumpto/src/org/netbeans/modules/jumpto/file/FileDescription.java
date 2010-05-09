/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
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
 *
 * Contributor(s): Petr Hrebejk
 */

package org.netbeans.modules.jumpto.file;

import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.jumpto.file.FileDescriptor;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;

/** Contains interesting information about file found in the search.
 *
 * @author Petr Hrebejk
 * @author Tomas Zezula
 * todo: SPI interface if needed. Now FileObject added to result seems to be enough
 * and everyone will use the default impl anyway.
 */
public class FileDescription extends FileDescriptor {
    /**
     * The icon used if unknown project, i.e. {@code project == null}.
     * In such case, we use {@code find.png} - "a file belongs to the find".
     */
    public static Icon UNKNOWN_PROJECT_ICON = ImageUtilities.loadImageIcon(
             "org/netbeans/modules/jumpto/resources/find.gif", false); // NOI18N
    private final FileObject fileObject;
    private final String ownerPath;
    private final Project project; // Project the file belongs to

    private Icon icon;
    private String projectName;
    private Icon projectIcon;

    public FileDescription(FileObject file, String ownerPath, Project project) {
        this.fileObject = file;
        this.ownerPath = ownerPath;
        this.project = project;
    }

    public String getFileName() {
        return fileObject.getNameExt(); // NOI18N
    }

    public synchronized Icon getIcon() {
        if ( icon == null ) {
            DataObject od = getDataObject();
            Image i = od.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
            icon = new ImageIcon( i );
        }
        return icon;
    }

    public String getOwnerPath() {
        return ownerPath;
    }

    public synchronized String getProjectName() {
        if ( projectName == null ) {
            initProjectInfo();
        }
        return projectName;
    }

    public synchronized Icon getProjectIcon() {
        if ( projectIcon == null ) {
            initProjectInfo();
        }
        return projectIcon;
    }


    public void open() {
        DataObject od = getDataObject();
        if ( od != null ) {
            EditCookie ec = (EditCookie) od.getCookie(EditCookie.class);
            if (ec != null) {
                ec.edit();
            }
            else {
                OpenCookie oc = od.getCookie( OpenCookie.class );
                if ( oc != null ) {
                    oc.open();
                }
            }
        }

    }

    public FileObject getFileObject() {
        return fileObject;
    }

    private DataObject getDataObject() {
        try     {
            org.openide.filesystems.FileObject fo = getFileObject();
            return org.openide.loaders.DataObject.find(fo);
        }
        catch (DataObjectNotFoundException ex) {
            return null;
        }
    }

    private void initProjectInfo() {
        // Issue #167198: A file may not belong to any project.
        // Hence, FileOwnerQuery.getOwner(file) can return null as a project,
        // and fileDescription.project will be null too.
        // But! We should not call ProjectUtils.getInformation(null).
        if(project != null) {
            ProjectInformation pi = ProjectUtils.getInformation( project );
            projectName = pi.getDisplayName();
            projectIcon = pi.getIcon();
        }
        else {
            projectName = "";   //NOI18N
            projectIcon = UNKNOWN_PROJECT_ICON;
        }
    }
}
