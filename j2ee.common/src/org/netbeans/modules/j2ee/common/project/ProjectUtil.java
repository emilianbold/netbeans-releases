/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.common.project;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import org.w3c.dom.Element;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

public class ProjectUtil {

    public static void updateDirsAttributeInCPSItem(org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item item,
            Element element) {
        String dirs = item.getAdditionalProperty(Util.DESTINATION_DIRECTORY);
        if (dirs == null) {
            dirs = Util.DESTINATION_DIRECTORY_LIB;
            if (item.getType() == org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item.TYPE_ARTIFACT && !item.isBroken()) {
                if (item.getArtifact() != null && item.getArtifact().getProject() != null
                        && item.getArtifact().getProject().getLookup().lookup(J2eeModuleProvider.class) != null) {
                    dirs = Util.DESTINATION_DIRECTORY_ROOT;
                }

            }
        }
        element.setAttribute("dirs", dirs); // NOI18N
    }

    public static void backupBuildImplFile(UpdateHelper updateHelper) throws IOException {
        //When the project.xml was changed from the customizer and the build-impl.xml was modified
        //move build-impl.xml into the build-impl.xml~ to force regeneration of new build-impl.xml.
        //Never do this if it's not a customizer otherwise user modification of build-impl.xml will be deleted
        //when the project is opened.
        final FileObject projectDir = updateHelper.getAntProjectHelper().getProjectDirectory();
        final FileObject buildImpl = projectDir.getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
        if (buildImpl != null) {
            final String name = buildImpl.getName();
            final String backupext = String.format("%s~", buildImpl.getExt());   //NOI18N
            final FileObject oldBackup = buildImpl.getParent().getFileObject(name, backupext);
            if (oldBackup != null) {
                oldBackup.delete();
            }
            FileLock lock = buildImpl.lock();
            try {
                buildImpl.rename(lock, name, backupext);
            } finally {
                lock.releaseLock();
            }
        }
    }

    public static void initTwoColumnTableVisualProperties(Component component, JTable table) {
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        // set the color of the table's JViewport
        table.getParent().setBackground(table.getBackground());
        updateColumnWidths(table);
        component.addComponentListener(new TableColumnSizeComponentAdapter(table));
    }

    private static void updateColumnWidths(JTable table) {
        //we'll get the parents width so we can use that to set the column sizes.
        double pw = table.getParent().getSize().getWidth();

        //#88174 - Need horizontal scrollbar for library names
        //ugly but I didn't find a better way how to do it
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn column = table.getColumnModel().getColumn(1);
        int w = ((int) pw / 2) - 1;
        if (w > column.getMaxWidth()) {
            // second column sometimes might have max width (packace column in Libraries)
            w = column.getMaxWidth();
        }
        column.setWidth(w);
        column.setPreferredWidth(w);

        w = (int) pw - w;
        column = table.getColumnModel().getColumn(0);
        column.setWidth(w);
        column.setPreferredWidth(w);
    }

    private static class TableColumnSizeComponentAdapter extends ComponentAdapter {

        private JTable table = null;

        public TableColumnSizeComponentAdapter(JTable table) {
            this.table = table;
        }

        public void componentResized(ComponentEvent evt) {
            updateColumnWidths(table);
        }
    }
}
