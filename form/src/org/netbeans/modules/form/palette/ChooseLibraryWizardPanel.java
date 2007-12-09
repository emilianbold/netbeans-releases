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

package org.netbeans.modules.form.palette;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.Component;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.io.File;
import java.net.URL;

import org.openide.WizardDescriptor;
import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.netbeans.api.project.libraries.*;

/**
 * The first panel in the wizard for adding new components to the palette from
 * a library. In this panel the user chooses a library from available libraries
 * installed in the IDE.
 *
 * @author Tomas Pavek
 */

class ChooseLibraryWizardPanel implements WizardDescriptor.Panel {

    private LibrarySelector librarySelector;

    private EventListenerList listenerList;

    public java.awt.Component getComponent() {
        if (librarySelector == null) {
            librarySelector = new LibrarySelector();

            // wizard API: set the caption and index of this panel
            librarySelector.setName(
                PaletteUtils.getBundleString("CTL_SelectLibrary_Caption")); // NOI18N
            librarySelector.putClientProperty("WizardPanel_contentSelectedIndex", // NOI18N
                                              new Integer(0));

            librarySelector.list.addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        fireStateChanged();
                    }
                });

        }

        return librarySelector;
    }

    public org.openide.util.HelpCtx getHelp() {
        return new org.openide.util.HelpCtx("beans.adding"); // NOI18N
    }

    public boolean isValid() {
        return librarySelector != null
               && librarySelector.getSelectedLibraries() != null;
    }

    public void readSettings(Object settings) {
    }

    public void storeSettings(Object settings) {
        if (librarySelector != null) { // create the UI component for the wizard step
            Library[] libraries = librarySelector.getSelectedLibraries();

            // collect the roots making up the classpath of the libraries
            // (presumably JAR files)

            Map<String,File> fileMap = new HashMap<String,File>(); // to avoid duplicities in case some JAR file is in more libraries
            Map<String,String> libraryMap = new HashMap<String,String>(); // to remember libraries for JAR files

            for (int i=0; i < libraries.length; i++) {
                List content = libraries[i].getContent("classpath"); // NOI18N
                // go through classpath roots of the library
                for (Iterator it=content.iterator(); it.hasNext(); ) {
                    URL rootURL = (URL) it.next();
                    if ("jar".equals(rootURL.getProtocol())) { // NOI18N
                        String path = rootURL.getPath();
                        int index = path.lastIndexOf('!');
                        if (index != -1) {
                            try {
                                rootURL = new URL(path.substring(0, index));
                            } catch (java.net.MalformedURLException mex) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mex);
                                continue;
                            }
                        }
                    }
                    File rootFile = FileUtil.toFile(URLMapper.findFileObject(rootURL));
                    String rootPath = rootFile.getAbsolutePath();
                    fileMap.put(rootPath, rootFile);
                    libraryMap.put(rootPath, libraries[i].getName());
                }
            }

            File[] libFiles = new File[fileMap.size()];
            fileMap.values().toArray(libFiles);

            AddToPaletteWizard wizard = (AddToPaletteWizard) settings;
            wizard.setJARFiles(libFiles);
            wizard.libraryNameMap = libraryMap;
        }
    }

    public void addChangeListener(ChangeListener listener) {
        if (listenerList == null)
            listenerList = new EventListenerList();
        listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        if (listenerList != null)
            listenerList.remove(ChangeListener.class, listener);
    }

    void fireStateChanged() {
        if (listenerList == null)
            return;

        ChangeEvent e = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i=listeners.length-2; i >= 0; i-=2) {
            if (listeners[i] == ChangeListener.class) {
                if (e == null)
                    e = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(e);
            }
        }
    }

    // ---------

    static class LibrarySelector extends JPanel {

        JList list;
        List<Library> libList;

        LibrarySelector() {
            list = new JList();
            list.setCellRenderer(new LibraryRenderer());
            list.setLayoutOrientation(JList.VERTICAL_WRAP);
            list.setVisibleRowCount(0);
            updateLibraryList();

            setLayout(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gridBagConstraints;

            JLabel label1 = new JLabel();
            org.openide.awt.Mnemonics.setLocalizedText(
                label1, PaletteUtils.getBundleString("CTL_Libraries")); // NOI18N
            label1.setLabelFor(list);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            add(label1, gridBagConstraints);

            JScrollPane scrollpane1 = new javax.swing.JScrollPane();
            scrollpane1.setViewportView(list);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            add(scrollpane1, gridBagConstraints);

        }

        Library[] getSelectedLibraries() {
            Object[] selected = list.getSelectedValues();
            Library[] libraries = new Library[selected.length];
            for (int i=0; i < selected.length; i++)
                libraries[i] = (Library) selected[i];
            return libraries;
        }

        void updateLibraryList() {
            Library[] libraries = LibraryManager.getDefault().getLibraries();
            libList = new ArrayList<Library>(libraries.length);
            for (int i=0; i < libraries.length; i++) {
                if (libraries[i].getType().equals("j2se")) { // NOI18N
                    libList.add(libraries[i]);
                }
            }
            Collections.sort(libList, new Comparator<Library>() {
                public int compare(Library lib1, Library lib2) {
                    return lib1.getDisplayName().compareTo(lib2.getDisplayName());
                }
            });

            list.setModel(new AbstractListModel() {
                public int getSize() { return libList.size(); }
                public Object getElementAt(int i) { return libList.get(i); }
            });
        }

        @Override
        public void addNotify() {
            super.addNotify();
            list.requestFocus();
        }

        @Override
        public java.awt.Dimension getPreferredSize() {
            return new java.awt.Dimension(400, 300);
        }
    }

    private static class LibraryRenderer extends JLabel
                                         implements ListCellRenderer
    {
        private static final Border hasFocusBorder =
            new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
        private static final Border noFocusBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

        public LibraryRenderer() {
            setOpaque(true);
            setBorder(noFocusBorder);
        }

        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            Library lib = (Library) value;
            setText(lib.getDisplayName());

            if (isSelected){
                setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                setForeground(UIManager.getColor("List.selectionForeground")); // NOI18N
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setBorder(cellHasFocus ? hasFocusBorder : noFocusBorder);
            return this;
        }
    }
}
