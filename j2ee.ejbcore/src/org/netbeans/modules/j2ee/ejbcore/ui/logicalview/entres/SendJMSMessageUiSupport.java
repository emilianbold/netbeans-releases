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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
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
                    List<String> drivens = getMdbs(p);
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
    
    private static void populateMdbs(List<MdbHolder> mdbs, final List<String> drivens, final Project project) {
        for (String mdbName : drivens) {
            J2eeModuleProvider j2eeModuleProvider = getJ2eeModuleProvider(project);
            try {
                MessageDestination messageDestination = null;
                String destName = j2eeModuleProvider.getConfigSupport().findMessageDestinationName(mdbName);
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
    
    private static List<String> getMdbs(Project project) throws IOException {
        
        List<String> mdbs = new ArrayList<String>();
        
        for (EjbJar ejbModule : EjbJar.getEjbJars(project)) {
            MetadataModel<EjbJarMetadata> metadataModel = ejbModule.getMetadataModel();
            List<String> mdbsInModule = metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, List<String>>() {
                public List<String> run(EjbJarMetadata metadata) throws Exception {
                    List<String> result = new ArrayList<String>();
                    EnterpriseBeans eb = metadata.getRoot().getEnterpriseBeans();
                    if (eb == null) {
                        return Collections.<String>emptyList();
                    }

                    MessageDriven[] messageDrivens = eb.getMessageDriven();
                    for (MessageDriven mdb : messageDrivens) {
                        result.add(mdb.getEjbName());
                    }
                    return result;
                }
            });
            mdbs.addAll(mdbsInModule);
        }
        
        return mdbs;
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
                String type = MessageDestination.Type.QUEUE.equals(mdbHolder.getMessageDestination().getType()) ? "LBL_Queue" : "LBL_Topic"; // NOI18N
                StringBuilder sb = new StringBuilder(mdbHolder.getProjectName());
                sb.append(" : "); // NOI18N
                sb.append(mdbHolder.getMdbEjbName());
                sb.append(" ["); // NOI18N
                sb.append(NbBundle.getMessage(MessageDestinationUiSupport.class, type));
                sb.append("]"); // NOI18N
                setToolTipText(sb.toString());
            } else {
                setText(value != null ? value.toString() : ""); // NOI18N
                setToolTipText(""); // NOI18N
            }
            return this;
        }
    }
}
