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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb.MessageDestinationUiSupport;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres.SendJMSMessageUiSupport.MdbHolder;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Support for SendJmsMessagePanel class.
 * <p>
 * This class contains only static methods.
 * @author Tomas Mysik
 * @see MessageDestinationUiSupport
 */
public abstract class SendJMSMessageUiSupport extends MessageDestinationUiSupport {
    
    /**
     * Get list of message-driven beans with all required properties.
     * @return list of message-driven beans.
     */
    public static List<MdbHolder> getMdbs() {
        List<MdbHolder> mdbs = new ArrayList<MdbHolder>();
        
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        for (Project p : openProjects) {
            if (EjbJar.getEjbJars(p).length > 0) {
                try {
                    Map<String, String> drivens = getMdbs(p);
                    populateMdbs(mdbs, drivens, p);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
        
        return mdbs;
    }
    
    /**
     * Populate given combo box and text field with given message-driven beans.
     * @param mdbs message-driven beans for given combo box and text field.
     * @param comboBox combo box to populate.
     * @param textField text field to populate.
     */
    public static void populateMessageDrivenBeans(final List<MdbHolder> mdbs, final JComboBox comboBox,
            final JTextField textField) {
        assert mdbs != null;
        assert comboBox != null;
        assert textField != null;
        
        comboBox.setRenderer(new MdbHolderListCellRenderer());
        
        List<MdbHolder> sortedMdbs = new ArrayList<MdbHolder>(mdbs);
        Collections.sort(sortedMdbs, new MdbHolderComparator());
        
        comboBox.removeAllItems();
        textField.setText("");
        for (MdbHolder mdbHolder : sortedMdbs) {
            comboBox.addItem(mdbHolder);
        }
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                populateMdbTextField((JComboBox) actionEvent.getSource(), textField);
            }
        });
        populateMdbTextField(comboBox, textField);
    }
    
    private static void populateMdbTextField(final JComboBox comboBox, final JTextField textField) {
        MdbHolder selectedItem = (MdbHolder) comboBox.getSelectedItem();
        if (selectedItem != null) {
            textField.setText(selectedItem.getMessageDestination().getName());
        }
    }
    
    private static void populateMdbs(List<MdbHolder> mdbs, final Map<String, String> drivens, final Project project) {
        for (Map.Entry<String, String> mdbEntry : drivens.entrySet()) {
            J2eeModuleProvider j2eeModuleProvider = getJ2eeModuleProvider(project);
            try {
                MessageDestination messageDestination = null;
                String mdbName = mdbEntry.getKey();
                String mappedName = mdbEntry.getValue();
                String destName = j2eeModuleProvider.getConfigSupport().findMessageDestinationName(mdbName);
                if (destName == null && mappedName != null) {
                    destName = mappedName;
                }
                if (destName != null) {
                    messageDestination = j2eeModuleProvider.getConfigSupport().findMessageDestination(destName);
                    mdbs.add(new MdbHolder(mdbName, messageDestination, project));
                }
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
    }
    
    private static J2eeModuleProvider getJ2eeModuleProvider(Project project) {
        return project.getLookup().lookup(J2eeModuleProvider.class);
    }

    // kay ejb-name and value is mapped-name
    private static Map<String, String> getMdbs(Project project) throws IOException {
        
        Map<String, String> mdbs = new HashMap<String, String>();
        
        for (EjbJar ejbModule : EjbJar.getEjbJars(project)) {
            MetadataModel<EjbJarMetadata> metadataModel = ejbModule.getMetadataModel();
            Map<String, String> mdbsInModule = metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, Map<String, String>>() {
                public Map<String, String> run(EjbJarMetadata metadata) throws Exception {
                    Map<String, String> result = new HashMap<String, String>();
                    EnterpriseBeans eb = metadata.getRoot().getEnterpriseBeans();
                    if (eb == null) {
                        return Collections.<String, String>emptyMap();
                    }

                    MessageDriven[] messageDrivens = eb.getMessageDriven();
                    for (MessageDriven mdb : messageDrivens) {
                        result.put(mdb.getEjbName(), findMsgDest(mdb));
                    }
                    return result;
                }
            });
            mdbs.putAll(mdbsInModule);
        }
        
        return mdbs;
    }
    
    // fix for 162899
    // not very nice solution, but the interface
    // org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven is not nice too.
    private static String findMsgDest(MessageDriven mdb) {
        try {
            // try EJB 3.0 style first
            return  mdb.getMappedName();
        }
        catch (VersionNotSupportedException e) {
            try {
                // try EJB 2.1 style
                return mdb.getMessageDestinationLink();
            }
            catch (VersionNotSupportedException e2) {
                // sorry, we don't resolve older versions than 2.1...
                return null;
            }
        }
    }

    /**
     * Holder for message-driven bean and its properties.
     */
    public static class MdbHolder {
        
        private final String mdbEjbName;
        private final MessageDestination messageDestination;
        private final Project project;

        /** Constructor with all properties. */
        public MdbHolder(String mdbEjbName, final MessageDestination messageDestination, final Project project) {
            assert mdbEjbName != null;
            assert messageDestination != null;
            assert project != null;
            
            this.mdbEjbName = mdbEjbName;
            this.messageDestination = messageDestination;
            this.project = project;
        }

        public MessageDestination getMessageDestination() {
            return messageDestination;
        }

        public Project getProject() {
            return project;
        }
        
        public String getMdbEjbName() {
            return mdbEjbName;
        }
        
        public String getProjectName() {
            return ProjectUtils.getInformation(project).getDisplayName();
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(getClass().getName());
            sb.append(" [");
            sb.append(getMdbEjbName());
            sb.append(" (");
            sb.append(getMessageDestination().getType().toString());
            sb.append("), ");
            sb.append(getProjectName());
            sb.append("]");
            return sb.toString();
        }
    }
    
    // optional - create factory method for this class
    private static class MdbHolderComparator implements Comparator<MdbHolder> {
        
        public int compare(MdbHolder mdbHolder1, MdbHolder mdbHolder2) {
            
            if (mdbHolder1 == null) {
                return mdbHolder2 == null ? 0 : -1;
            }
            
            if (mdbHolder2 == null) {
                return 1;
            }
            
            String name1 = mdbHolder1.getMdbEjbName();
            String name2 = mdbHolder2.getMdbEjbName();
            if (name1 == null) {
                return name2 == null ? 0 : -1;
            }
            
            return name2 == null ? 1 : name1.compareToIgnoreCase(name2);
        }
    }
    
    // optional - create factory method for this class
    private static class MdbHolderListCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            if (value instanceof MdbHolder) {
                MdbHolder mdbHolder = (MdbHolder) value;
                setText(mdbHolder.getMdbEjbName());
                // tooltip
                MessageDestination messageDestination = mdbHolder.getMessageDestination();
                if (messageDestination == null) {
                    setToolTipText(""); // NOI18N
                } else {
                    String type = MessageDestination.Type.QUEUE.equals(messageDestination.getType()) ? "LBL_Queue" : "LBL_Topic"; // NOI18N
                    StringBuilder sb = new StringBuilder(mdbHolder.getProjectName());
                    sb.append(" : "); // NOI18N
                    sb.append(mdbHolder.getMdbEjbName());
                    sb.append(" ["); // NOI18N
                    sb.append(NbBundle.getMessage(MessageDestinationUiSupport.class, type));
                    sb.append("]"); // NOI18N
                    setToolTipText(sb.toString());
                }
            } else {
                setText(value != null ? value.toString() : ""); // NOI18N
                setToolTipText(""); // NOI18N
            }
            return this;
        }
    }
}
