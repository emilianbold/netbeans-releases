/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.weblogic9.ui.nodes;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.io.File;
import java.util.ArrayList;
import java.net.URL;
import java.net.URI;
import java.util.List;
import javax.swing.*;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformImpl;
import org.netbeans.spi.project.libraries.LibraryImplementation;


/**
 * JBoss instance customizer which is accessible from server manager.
 *
 * 
 */
public class Customizer extends JTabbedPane {

    private static final String CLASSPATH = J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH;
    private static final String SOURCES = J2eeLibraryTypeProvider.VOLUME_TYPE_SRC;
    private static final String JAVADOC = J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC;

    private J2eePlatformImpl platform;

    public Customizer(J2eePlatformImpl platform) {
        this.platform = platform;
        initComponents ();
    }

    private void initComponents() {
        getAccessibleContext().setAccessibleName (NbBundle.getMessage(Customizer.class,"ACS_Customizer")); // NOI18N
        getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(Customizer.class,"ACS_Customizer")); // NOI18N
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Classes"), createPathTab(CLASSPATH)); // NOI18N
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Sources"), createPathTab(SOURCES)); // NOI18N
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Javadoc"), createPathTab(JAVADOC)); // NOI18N
    }


    private JComponent createPathTab(String type) {
        return new PathView(platform, type);
    }


    private static class PathView extends JPanel {

        private JList resources;
        private JButton addButton;
        private String type;
        private J2eePlatformImpl platform;

        public PathView (J2eePlatformImpl aPlatform, String aType) {
            type = aType;
            platform = aPlatform;
            initComponents();
        }

        private void initComponents() {
            setLayout(new GridBagLayout());
            JLabel label = new JLabel ();
            String key = null;
            String mneKey = null;
            String ad = null;
            if (type.equals(CLASSPATH)) {
                key = "TXT_Classes";       // NOI18N
                mneKey = "MNE_Classes";    // NOI18N
                ad = "AD_Classes";       // NOI18N                
            } else if (type.equals(SOURCES)) {
                key = "TXT_Sources";        // NOI18N
                mneKey = "MNE_Sources";     // NOI18N
                ad = "AD_Sources";          // NOI18N
            } else if (type.equals(JAVADOC)) {
                key = "TXT_Javadoc";        // NOI18N
                mneKey = "MNE_Javadoc";     // NOI18N
                ad = "AD_Javadoc";          // NOI18N                
            } else {
                assert false : "Illegal type of panel"; //NOI18N
                return;
            }
            label.setText(NbBundle.getMessage(Customizer.class,key));
            label.setDisplayedMnemonic(NbBundle.getMessage(Customizer.class,mneKey).charAt(0));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets (6,12,2,0);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            ((GridBagLayout)getLayout()).setConstraints(label,c);
            add(label);
            resources = new JList(new PathModel(platform, type));
            label.setLabelFor(resources);
            resources.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Customizer.class,ad));
            JScrollPane spane = new JScrollPane (this.resources);            
            c = new GridBagConstraints();
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = 1;
            c.gridheight = 5;
            c.insets = new Insets (0,12,12,6);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            ((GridBagLayout)this.getLayout()).setConstraints(spane,c);
            add(spane);
        }
    }


    private static class PathModel extends AbstractListModel/*<String>*/ {

        private J2eePlatformImpl platform;
        private String type;
        private java.util.List data;

        public PathModel (J2eePlatformImpl aPlatform, String aType) {
            platform = aPlatform;
            type = aType;
        }

        public int getSize() {
            return this.getData().size();
        }

        public Object getElementAt(int index) {
            java.util.List list = this.getData();
            URL url = (URL)list.get(index);
            if ("jar".equals(url.getProtocol())) {      //NOI18N
                URL fileURL = FileUtil.getArchiveFile (url);
                if (FileUtil.getArchiveRoot(fileURL).equals(url)) {
                    // really the root
                    url = fileURL;
                } else {
                    // some subdir, just show it as is
                    return url.toExternalForm();
                }
            }
            if ("file".equals(url.getProtocol())) { // NOI18N
                File f = new File (URI.create(url.toExternalForm()));
                return f.getAbsolutePath();
            }
            else {
                return url.toExternalForm();
            }
        }

        private synchronized List getData() {
            if (data == null) {
                data = new ArrayList();
                LibraryImplementation[] libImpl = platform.getLibraries();
                for (int i = 0; i < libImpl.length; i++) {
                    data.addAll(libImpl[i].getContent(type));
                }
            }
            return data;
        }
    }
}
