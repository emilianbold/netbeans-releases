/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.uml.project.ui.common;

import java.awt.*;
import java.io.File;
import java.util.Set;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.openide.util.NbBundle;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.util.Lookup;


/** Handles adding, removing, reordering of source roots.
 *
 * @author Mike Frisino
 */
public class JavaSourceRootsUI
{
    
    public static final int NUM_COLS = 3;
    public static final int COL_INCLUDE_FLAG = 0;
    public static final int COL_SOURCE_GROUP = 1;
    public static final int COL_SOURCE_GROUP_DISPLAY_NAME = 2;
    
    
    /*
     * MCF
     * ths version can be called from wizards when we have not yet constructed
     * the uml project and therefore there is no pre-existing project properties
     * metadata to read values from.
     *
     */
    public static JavaSourceRootsModel createModel(
        ReferencedJavaProjectModel javaRefModel,
        DefaultListModel jsrm)
    {
        Object[][] data = new Object[0][NUM_COLS];
        SourceGroup[] sourceGroups = new SourceGroup[0];
        Project javaProject = javaRefModel.getProject();
        
        if (javaRefModel.isBroken() || javaProject == null)
        {
            // must handle the condition where the project is not available
            // because it has not been mounted.
            // so we can't compare to actual mounts, we can only list the
            // src groups as we previously recorded them in their
            // abbreviated form.
            data = new Object[jsrm.getSize()][NUM_COLS];
            sourceGroups = new SourceGroup[jsrm.getSize()];
            
            for (int i=0; i< jsrm.getSize(); i++)
            {
                data[i][COL_INCLUDE_FLAG] = Boolean.valueOf(true);
                
                Object obj = jsrm.getElementAt(i);
                if(obj instanceof SourceGroup)
                {
                    SourceGroup group = (SourceGroup)obj;
                    
                    // TODO figure out what to disply in the display name. It is
                    // not knowable since the ref project is not mounted
                    data[i][COL_SOURCE_GROUP] = group.getRootFolder().getName();
                    sourceGroups[i] = group;
//                    sourceGroups[i] = (SourceGroup)jsrm.getElementAt(i);
                }
            }
        }
        
        else
        {
            // we are good, java proj is mounted, so we can compose
            // list against actual sources
            Sources srcs =
                (Sources)javaProject.getLookup().lookup(Sources.class);
            
            if (srcs == null)
            {
                // TODO Do we need to do anything at this point?
                // yes, something is out of sync with old values and real srcs
                return new JavaSourceRootsModel(data, sourceGroups);
            }
            
            // now check for Java sources
            SourceGroup[] javaSrcGrps =
                srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            
            data = new Object[javaSrcGrps.length][NUM_COLS];
            sourceGroups = new SourceGroup[javaSrcGrps.length];
            
            if (javaSrcGrps.length == 0 && jsrm.getSize() == 0)
            {
                // This is ok, this is a case where there are no src groups
                // and there were none previously. It is not a good case
                // for the association b/t uml and java, but that is to
                // be handled elsewhere
                return new JavaSourceRootsModel(data, sourceGroups);
            }
            
            if (javaSrcGrps.length == 0 || javaSrcGrps.length < jsrm.getSize())
            {
                // TODO Do we need to do anything at this point?
                // yes, this is a problem because our jsrm suggests that there
                // were more src groups before? Where have they gone?
                return new JavaSourceRootsModel(data, sourceGroups);
            }
            
            // Ok, this is the normative situation, now we go through
            // and reconcile the list of prior selections with the
            // available src groups
            for (int i=0; i < javaSrcGrps.length; i++)
            {
                // check to see if the group is already included in selc
                
                if (jsrm.contains(javaSrcGrps[i].getName()))
                    data[i][COL_INCLUDE_FLAG] = Boolean.valueOf(true);
                
                else
                    data[i][COL_INCLUDE_FLAG] = Boolean.valueOf(false);
                
                
                data[i][COL_SOURCE_GROUP] =
                    javaSrcGrps[i].getRootFolder().getName();
                
                data[i][COL_SOURCE_GROUP_DISPLAY_NAME] =
                    javaSrcGrps[i].getDisplayName();
                
                sourceGroups[i] = javaSrcGrps[i];
            }
        }
        
        return new JavaSourceRootsModel(data, sourceGroups);
    }
    
    
    // this is the version called from wizard
    public static JavaSourceRootsModel createModel(Project javaProject)
    {
        Object[][] data = new Object[0][NUM_COLS];
        SourceGroup[] sourceGroups = new SourceGroup[0];
        Sources srcs = retrieveJavaProjectSources(javaProject);
        
        if (srcs == null)
            return new JavaSourceRootsModel(data);
        
        SourceGroup[] javaSrcGrps = retrieveJavaProjectSourceGroups(srcs);
        
        data = new Object[javaSrcGrps.length][NUM_COLS];
        sourceGroups = new SourceGroup[javaSrcGrps.length];
        
        for (int i=0; i< javaSrcGrps.length; i++)
        {
            // check to see if the group is already included in selc
            data[i][COL_INCLUDE_FLAG] = Boolean.valueOf(true);
            
            data[i][COL_SOURCE_GROUP] =
                javaSrcGrps[i].getRootFolder().getName();
            
            data[i][COL_SOURCE_GROUP_DISPLAY_NAME] =
                javaSrcGrps[i].getDisplayName();
            
            sourceGroups[i] = javaSrcGrps[i];
        }
        
        return new JavaSourceRootsModel(data, sourceGroups);
    }

    public static SourceGroup[] retrieveJavaProjectSourceGroups(final Sources srcs)
    {
        return srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    }

    public static Sources retrieveJavaProjectSources(Project javaProject)
    {
        Lookup lookup=javaProject.getLookup();
        return (Sources)lookup.lookup(Sources.class);
    }
    
    
    public static DefaultTableModel createEmptyModel()
    {
        Object[][] data = new Object[0][NUM_COLS];
        SourceGroup[] sourceGroups = new SourceGroup[0];
        
        return new JavaSourceRootsModel(data, sourceGroups);
    }
    
    
    public static class JavaSourceRootsModel extends DefaultTableModel
    {
        SourceGroup[] sourceGroups = null;
        
        static String colCheck =
            (NbBundle.getMessage(JavaSourceRootsUI.class,
            "LBL_SourceGroupsColCheck")); //NOI18N
        
        static String colCheckAlt =
            (NbBundle.getMessage(JavaSourceRootsUI.class,
            "LBL_SourceGroupsColCheckAlt")); //NOI18N
        
        static String colGroupFolder =
            (NbBundle.getMessage(JavaSourceRootsUI.class,
            "LBL_SourceGroupsColFolder")); //NOI18N
        
        static String colGroupLabel =
            (NbBundle.getMessage(JavaSourceRootsUI.class,
            "LBL_SourceGroupsColLabel")); //NOI18N
        
        
        public JavaSourceRootsModel(Object[][] data)
        {
            super(data, new Object[]{colCheck, colGroupFolder, colGroupLabel});
        }
        
        public JavaSourceRootsModel(Object[][] data, SourceGroup[] sourceGroups)
        {
            super(data, new Object[]{colCheck, colGroupFolder, colGroupLabel});
            this.sourceGroups = sourceGroups;
        }
        
        
        public boolean isCellEditable(int row, int column)
        {
            return column == 0;
        }
        
        public Class getColumnClass(int columnIndex)
        {
            switch (columnIndex)
            {
                case JavaSourceRootsUI.COL_INCLUDE_FLAG:
                    return Boolean.class;
                    
                case JavaSourceRootsUI.COL_SOURCE_GROUP:
                    return String.class;
                    
                case JavaSourceRootsUI.COL_SOURCE_GROUP_DISPLAY_NAME:
                    return String.class;
                    
                default:
                    return super.getColumnClass(columnIndex);
            }
        }
        
        public SourceGroup[] getSourceGroups()
        {
            return sourceGroups;
        }
        
        public SourceGroup getSourceGroup(int index)
        {
            if (sourceGroups == null)
                return null;
            
            return sourceGroups[index];
        }
    }
    
    
    
    // Inner Classes
    ////////////////
    
//	protected static class SourceGroupRenderer extends DefaultTableCellRenderer
//	{
//		public SourceGroupRenderer()
//		{
//            super();
//		}
//
//		public Component getTableCellRendererComponent(
//				JTable table,
//				Object value,
//				boolean isSelected,
//				boolean hasFocus,
//				int row,
//				int column)
//		{
//			String rootFolderName = "unknown";
//            String rootFolderPath = "unknown";
//
//			if (value instanceof SourceGroup)
//			{
//				SourceGroup sourceGroup = (SourceGroup)value;
//				rootFolderName = sourceGroup.getRootFolder().getName();
//                rootFolderPath = sourceGroup.getRootFolder().getPath();
//			}
//
//			setText(rootFolderName);
//			setToolTipText(rootFolderPath);
//
//			return super.getTableCellRendererComponent(
//					table, rootFolderName, isSelected, hasFocus, row, column);
//		}
//	}
    
//	protected static class SourceGroupLabelRenderer extends JLabel
//		implements TableCellRenderer
//	{
//		public SourceGroupLabelRenderer()
//		{
//			super();
//		}
//
//		public Component getTableCellRendererComponent(
//				JTable table,
//				Object value,
//				boolean isSelected,
//				boolean hasFocus,
//				int row,
//				int column)
//		{
//			String rootFolderName = "unknown";
//            String rootFolderPath = "unknown";
//
//			if (value instanceof SourceGroup)
//			{
//				SourceGroup sourceGroup = (SourceGroup)value;
//				rootFolderName = sourceGroup.getRootFolder().getName();
//                rootFolderPath = sourceGroup.getRootFolder().getPath();
//			}
//
//			setText(rootFolderName);
//			setToolTipText(rootFolderPath);
//
//			return this;
//		}
//
//		// The following methods override the defaults for performance reasons
//		public void validate() {}
//		public void revalidate() {}
//		protected void firePropertyChange(
//				String propertyName, Object oldValue, Object newValue) {}
//		public void firePropertyChange(
//				String propertyName, boolean oldValue, boolean newValue) {}
//	}
    
    private static class WarningDlg extends JPanel
    {
        
        public WarningDlg(Set invalidRoots)
        {
            this.initGui(invalidRoots);
        }
        
        private void initGui(Set invalidRoots)
        {
            setLayout( new GridBagLayout());
            JLabel label = new JLabel();
            
            label.setText(NbBundle.getMessage(
                JavaSourceRootsUI.class,"LBL_InvalidRoot")); //NOI18N
            
            label.setDisplayedMnemonic(NbBundle.getMessage(
                JavaSourceRootsUI.class,
                "MNE_InvalidRoot").charAt(0)); //NOI18N
            
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1.0;
            c.insets = new Insets(12,0,6,0);
            ((GridBagLayout)this.getLayout()).setConstraints(label,c);
            this.add(label);
            JList roots = new JList(invalidRoots.toArray());
            roots.setCellRenderer(new InvalidRootRenderer(true));
            JScrollPane p = new JScrollPane(roots);
            c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = c.weighty = 1.0;
            c.insets = new Insets(0,0,12,0);
            ((GridBagLayout)this.getLayout()).setConstraints(p,c);
            this.add(p);
            label.setLabelFor(roots);
            
            roots.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(
                JavaSourceRootsUI.class, "AD_InvalidRoot")); //NOI18N
            
            JLabel label2 = new JLabel();
            
            label2.setText(NbBundle.getMessage(
                JavaSourceRootsUI.class,"MSG_InvalidRoot2")); //NOI18N
            
            c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1.0;
            c.insets = new Insets(0,0,0,0);
            ((GridBagLayout)this.getLayout()).setConstraints(label2,c);
            this.add(label2);
        }
        
        private static class InvalidRootRenderer extends DefaultListCellRenderer
        {
            
            private boolean projectConflict;
            
            public InvalidRootRenderer(boolean projectConflict)
            {
                this.projectConflict = projectConflict;
            }
            
            public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus)
            {
                File f = (File) value;
                String message = f.getAbsolutePath();
                
                if (projectConflict)
                {
                    Project p = FileOwnerQuery.getOwner(f.toURI());
                    
                    if (p!=null)
                    {
                        ProjectInformation pi = (ProjectInformation)
                        p.getLookup().lookup(ProjectInformation.class);
                        
                        if (pi != null)
                        {
                            String projectName = pi.getDisplayName();
                            
                            if (projectName != null)
                            {
                                message = MessageFormat.format(
                                    NbBundle.getMessage(
                                    JavaSourceRootsUI.class,
                                    "TXT_RootOwnedByProject"), //NOI18N
                                    new Object[] {
                                    message,
                                    projectName});
                            }
                        }
                    }
                }
                
                return super.getListCellRendererComponent(
                    list, message, index, isSelected, cellHasFocus);
            }
        }
    }
}
