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


package org.netbeans.modules.properties;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import java.text.MessageFormat;

import org.openide.DialogDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.NbBundle;

/** 
 * Node representing a <code>PropertiesDataObject</code>.
 * Its children ({@link PropertiesLocaleNode}s) represent
 * the {@link PropertiesFileEntry} PropertyFileEntries.
 *
 * @author Petr Jiricka, Peter Zavadsky
 * @see PropertiesDataObject
 * @see org.openide.loaders.DataNode
 */
public class PropertiesDataNode extends DataNode {

    /**
     * Listener for changes on <code>propDataObject</code> name and cookie properties.
     * Changes display name of components accordingly.
     */
    private final transient PropertyChangeListener dataObjectListener;
    
    private boolean multiLocale;

    PropertiesDataNode(PropertiesDataObject propDO) {
        this(propDO, createChildren(propDO));
        multiLocale = propDO.isMultiLocale();
    }

    /** Creates data node for a given data object.
     * The provided children object will be used to hold all child nodes.
     * @param dataObject  object to work with
     * @param children container for the node
     */
    public PropertiesDataNode(DataObject dataObject, Children children) {
        super(dataObject, children);
        setIconBaseWithExtension("org/netbeans/modules/properties/propertiesObject.png"); // NOI18N
        dataObjectListener = new NameUpdater();
        dataObject.addPropertyChangeListener(
                WeakListeners.propertyChange(dataObjectListener, dataObject));
    }

    private static Children createChildren(PropertiesDataObject propDO) {
        return ((PropertiesFileEntry) propDO.getPrimaryEntry()).getChildren();
        // used to use propDO.getChildren() if propDO.isMultiLocale()
        // but that is now always false, and anyway too slow to call isMultiLocale here
        // (observed 1811 msec), should avoid calculations unless and until children expanded
    }

    /**
     * Listener which listens on changes of the set of
     * <code>PropertiesDataObject</code>'s files.
     * When the set of files changes, we fire a change of the DataObject's name,
     * thus forcing update of the display name. We need this update because
     * the CVS status of the PropertiesDataObject may change when the set
     * of files is changed.
     */
    final class NameUpdater implements PropertyChangeListener {
        
        /**
         */
        public void propertyChange(PropertyChangeEvent e) {
            if (DataObject.PROP_FILES.equals(e.getPropertyName())) {
                PropertiesDataObject propDO = (PropertiesDataObject) getDataObject();
                propDO.fireNameChange();

                // If the number of locales changes to more than one or down to
                // one, we must exchange the children.
                boolean newMultiLocale = propDO.isMultiLocale();
                if (newMultiLocale != multiLocale) {
                    multiLocale = newMultiLocale;
                    setChildren(createChildren(propDO));
            }
        }
        }
        
    }
    
    /** Gets new types that can be created in this node.
     * @return array with <code>NewLocaleType</code> */
    @Override
    public NewType[] getNewTypes() {
        PropertiesDataObject propDO = (PropertiesDataObject) getDataObject();
        if (propDO.isMultiLocale()) {
        return new NewType[] {new NewLocaleType()};
        } else {
            PropertiesFileEntry pfEntry = (PropertiesFileEntry) propDO.getPrimaryEntry();
            return new NewType[] { new NewLocaleType(),
                                   new PropertiesLocaleNode.NewPropertyType(pfEntry) };
    }
    }
    
    /** Indicates whether this node has customizer. Overrides superclass method.
     * @return true */
    @Override
    public boolean hasCustomizer() {
        return true;
    }
    
    /** Gets node customizer. Overrides superclass method. 
     * @return <code>BundleNodeCustomizer</code> instance.
     * @see BundleNodeCustomizer */
    @Override
    public Component getCustomizer() {
        return new BundleNodeCustomizer((PropertiesDataObject)getDataObject());
    }
    
    /** Creates paste types for this node. Overrides superclass method. 
     * @param transferable transferable in clipboard 
     * @param types <code>PasteType</code>'s valid for this node. */
    @Override
    public void createPasteTypes(Transferable transferable, List<PasteType> types) {
        super.createPasteTypes(transferable, types);

        // Copy/paste mode?
        int mode = NodeTransfer.COPY;
        
        Node node = NodeTransfer.node(transferable, mode);
        
        if(node == null || !(node instanceof PropertiesLocaleNode)) {
            // Cut/paste mode?
            mode = NodeTransfer.MOVE;
        
            node = NodeTransfer.node(transferable, mode);

            if(node == null || !(node instanceof PropertiesLocaleNode))
                return;
            
            PropertiesFileEntry entry = (PropertiesFileEntry)((PropertiesLocaleNode)node).getFileEntry();
            if(((PropertiesDataObject)getDataObject()).files().contains(entry.getFile())) {
                return;
            }
        }

        PropertiesFileEntry entry = (PropertiesFileEntry)((PropertiesLocaleNode)node).getFileEntry();
        types.add(new EntryPasteType(entry, mode));
    }

    /** Paste type for <code>PropertiesDataNode</code>. */
    private class EntryPasteType extends PasteType {

        /** Entry to copy/move. */
        private  PropertiesFileEntry entry;
        
        /** Flag for copying/moving. */
        private int flag;
        

        /** Constructor.
         * @param entry entry to copy/move 
         * @param flag flag for moving/copying */
        public EntryPasteType(PropertiesFileEntry entry, int flag) {
            this.entry = entry;
            this.flag = flag;
        }
        
        /** Peforms paste action. Implements superclass abstract method. 
         * @exception IOException if error occured */
        public Transferable paste() throws IOException {
            DataFolder dataFolder = PropertiesDataNode.this.getDataObject().getFolder();
            
            if(dataFolder == null)
                return null;
            
            FileObject folder = dataFolder.getPrimaryFile();
            
            String newName = getDataObject().getPrimaryFile().getName() + Util.getLocaleSuffix(entry);
            
            int entryIndex = ((PropertiesDataObject)getDataObject()).getBundleStructure().getEntryIndexByFileName(newName);
            
            // Has such item -> find brother.
            if(entryIndex != -1) {
                newName = FileUtil.findFreeFileName(folder, newName, entry.getFile().getExt());
            }
            
            if(flag == NodeTransfer.COPY) {
                FileObject fileObject = entry.getFile();
                fileObject.copy(folder, newName, fileObject.getExt());
                
            } else if(flag == NodeTransfer.MOVE) {
                FileObject fileObject = entry.getFile();
                FileLock lock = entry.takeLock();
                
                // removing secondary entry from original data object
                ((PropertiesDataObject) entry.getDataObject()).removeSecondaryEntry2(entry);
                try {
                    FileObject fo2 = fileObject.move(lock, folder, newName, fileObject.getExt());
                    try {
                        // Invokes the method for recognition fo2's primary fila and data object.
                        // Secondary entry in destination data object is created and registered
                        DataObject.find(fo2);
                    }
                    catch (Exception e) {
                    }
                } finally {
                    lock.releaseLock ();
                }
            }
            
            return null;
        }
        
    } // End of class EntryPasteType.
    

    /** New type for properties node. It creates new locale for ths bundle. */
    private class NewLocaleType extends NewType {

        /** Overrides superclass method. */
        @Override
        public String getName() {
            return NbBundle.getBundle(PropertiesDataNode.class).getString("LAB_NewLocaleAction");
        }

        /** Overrides superclass method. */
        public void create() throws IOException {
            final PropertiesDataObject propertiesDataObject = (PropertiesDataObject)getCookie(DataObject.class);

            final Dialog[] dialog = new Dialog[1];
            final LocalePanel panel = new LocalePanel();

            DialogDescriptor dialogDescriptor = new DialogDescriptor(
                panel,
                NbBundle.getBundle(PropertiesDataNode.class).getString("CTL_NewLocaleTitle"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        if (evt.getSource() == DialogDescriptor.OK_OPTION) {
                            if (containsLocale(propertiesDataObject, panel.getLocale())) {
                                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                    MessageFormat.format(NbBundle.getBundle(PropertiesDataNode.class).getString("MSG_LangExists"), panel.getLocale()), 
                                    NotifyDescriptor.ERROR_MESSAGE);
                                DialogDisplayer.getDefault().notify(msg);
                            } else {
                                Util.createLocaleFile(propertiesDataObject, panel.getLocale().toString(), true);
                                dialog[0].setVisible(false);
                                dialog[0].dispose();
                            }
                        }
                    }
                }
            );
            dialogDescriptor.setClosingOptions(new Object [] { DialogDescriptor.CANCEL_OPTION });
            
            dialog[0] = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
            dialog[0].setVisible(true);
        }

    } // End of NewLocaleType class.

    private static boolean containsLocale(PropertiesDataObject propertiesDataObject, Locale locale) {
        FileObject file = propertiesDataObject.getBundleStructure().getNthEntry(0).getFile();
//        FileObject file = propertiesDataObject.getPrimaryFile();
        String newName = file.getName() + PropertiesDataLoader.PRB_SEPARATOR_CHAR + locale;
        BundleStructure structure = propertiesDataObject.getBundleStructure();
        for (int i = 0; i<structure.getEntryCount();i++) {
            FileObject f = structure.getNthEntry(i).getFile();
            if (newName.startsWith(f.getName()) && f.getName().length() > file.getName().length())
                file = f;
        }
        return file.getName().equals(newName);
    }
}
