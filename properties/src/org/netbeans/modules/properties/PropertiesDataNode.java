/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.properties;


import java.awt.datatransfer.Transferable;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.openide.actions.OpenAction;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;



/** 
 * Standard node representing <code>PropertiesDataObject</code>.
 *
 * @author Petr Jiricka
 * @see PropertiesDataObject
 */
public class PropertiesDataNode extends DataNode {

    
    /** Create a data node for a given data object.
     * The provided children object will be used to hold all child nodes.
     * @param obj object to work with
     * @param ch children container for the node
     */
    public PropertiesDataNode (DataObject obj, Children ch) {
        super (obj, ch);
        initialize();
    }

    
    /** Initializes instance. Sets icon base and default action. */
    private void initialize () {
        setIconBase("org/netbeans/modules/properties/propertiesObject"); // NOI18N
        setDefaultAction (SystemAction.get(OpenAction.class));
    }

    /** Reads object from outppput stream. Used by deserialization. */
    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        is.defaultReadObject();
        initialize();
    }

    /** Gets new types that can be created in this node.
     * @return new types
     */
    public NewType[] getNewTypes () {
        return new NewType[] { 
            new NewType() {
                
                /** Overrides superclass method. */
                public String getName() {
                    return NbBundle.getBundle(PropertiesDataNode.class).getString("LAB_NewLocaleAction");
                }

                /** Overrides superclass method.*/
                public HelpCtx getHelpCtx() {
                    return new HelpCtx (PropertiesDataNode.class.getName () + ".new_locale"); // NOI18N
                }

                /** Overrides superclass method. */
                public void create() throws IOException {
                    final MultiDataObject prop = (MultiDataObject)getCookie(DataObject.class);

                    final java.awt.Dialog[] dialog = new java.awt.Dialog[1];
                    final LocalePanel panel = new LocalePanel();
                    
                    DialogDescriptor dd = new DialogDescriptor(
                        panel,
                        NbBundle.getBundle(PropertiesDataNode.class).getString("CTL_NewLocaleTitle"),
                        true,
                        DialogDescriptor.OK_CANCEL_OPTION,
                        DialogDescriptor.OK_OPTION,
                        new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent ev) {
                                // OK pressed
                                if (ev.getSource() == DialogDescriptor.OK_OPTION) {
                                    dialog[0].setVisible(false);
                                    dialog[0].dispose();

                                    String locale = panel.getLocale().toString();
                                    
                                    try {
                                        if (locale.length() == 0)
                                            throw new IllegalArgumentException(NbBundle.getBundle(PropertiesDataNode.class).getString("MSG_LangExists"));
                                        
                                        locale = PropertiesDataLoader.PRB_SEPARATOR_CHAR + locale;
                                        
                                        final String newName = locale;

                                        if (prop != null) {
                                            final PropertiesFileEntry entry = (PropertiesFileEntry)prop.getPrimaryEntry();
                                            final PropertiesStructure structure = entry.getHandler().getStructure();
                                            final FileObject folder = prop.getPrimaryFile().getParent();

                                            // Actually create new file.
                                            folder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                                                public void run() throws IOException {
                                                    FileObject newFile = FileUtil.createData(folder, prop.getPrimaryFile().getName() + newName +
                                                        "." + PropertiesDataLoader.PROPERTIES_EXTENSION); // NOI18N
                                                    
                                                    BufferedWriter bw = null;
                                                    FileLock lock = newFile.lock();
                                                    
                                                    try {
                                                        bw = new BufferedWriter(new OutputStreamWriter(
                                                            new PropertiesEditorSupport.NewLineOutputStream(newFile.getOutputStream(lock), entry.getPropertiesEditor().getNewLineType()),
                                                            "8859_1" // NOI18N
                                                        ));
                                                        
                                                        for (Iterator it = structure.allItems(); it.hasNext(); ) {
                                                            Element.ItemElem item1 = (Element.ItemElem)it.next();
                                                            if(item1 != null) {
                                                                String ps = item1.printString();
                                                                bw.write(ps, 0, ps.length());
                                                            }
                                                        }
                                                    } finally {
                                                        if (bw != null) {
                                                            bw.flush();
                                                            bw.close();
                                                        }
                                                        lock.releaseLock();
                                                    }
                                                }
                                            }); // End of annonymous inner class extended from FileSystem.AtomicAction.
                                         }
                                    } catch (IllegalArgumentException e) {   
                                        // catch & report badly formatted names
                                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                            MessageFormat.format(
                                                NbBundle.getBundle(PropertiesDataNode.class).getString("MSG_LangExists"),
                                                    new Object[] {locale}), NotifyDescriptor.ERROR_MESSAGE);
                                        TopManager.getDefault().notify(msg);
                                    } catch (IOException e) {
                                        // catch & report IO error
                                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                            MessageFormat.format(
                                                NbBundle.getBundle(PropertiesDataNode.class).getString("MSG_LangExists"),
                                                    new Object[] {locale}), NotifyDescriptor.ERROR_MESSAGE);
                                            TopManager.getDefault().notify(msg);
                                    }
                                // Cancel pressed
                                } else if (ev.getSource() == DialogDescriptor.CANCEL_OPTION) {
                                    dialog[0].setVisible(false);
                                    dialog[0].dispose();
                                }
                            }
                        }
                    );
                    dialog[0] = TopManager.getDefault().createDialog(dd);
                    dialog[0].show();
                }
            } // End of annonymous inner class extended from NewType.
        };
    }
    
    /** Indicates whether this node has customizer. Overrides superclass method.
     * @return true */
    public boolean hasCustomizer() {
        return true;
    }
    
    /** Gets node customizer. Overrides superclass method. 
     * @return <code>BundleNodeCustomizer</code> instance.
     * @see BundleNodeCustomizer */
    public Component getCustomizer() {
        return new BundleNodeCustomizer((PropertiesDataObject)getDataObject());
    }
    
    /** Creates paste types for this node. Overrides superclass method. 
     * @param transferable transferable in clipboard 
     * @param types <code>PasteType</code>'s valid for this node. */
    public void createPasteTypes(Transferable transferable, List types) {
        super.createPasteTypes(transferable, types);

        int mode = NodeTransfer.COPY;
        
        Node node = NodeTransfer.node(transferable, mode);
        
        if(node == null || !(node instanceof PropertiesLocaleNode))
            mode = NodeTransfer.MOVE;
        
        node = NodeTransfer.node(transferable, mode);
        
        if(node == null || !(node instanceof PropertiesLocaleNode))
            return;

        
        PropertiesFileEntry entry = (PropertiesFileEntry)((PropertiesLocaleNode)node).getFileEntry();
        
        types.add(new EntryPasteType(entry, mode));

        return;
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
            
            String newName = getDataObject().getPrimaryFile().getName() + Util.getLocalePartOfFileName(entry);
            
            int entryIndex = ((PropertiesDataObject)getDataObject()).getBundleStructure().getEntryIndexByFileName(newName);

            // Has such item -> find brother.
            if(entryIndex != -1)
                newName = FileUtil.findFreeFileName(folder, newName, entry.getFile().getExt());
            
            if(flag == NodeTransfer.COPY) {
                FileObject fileObject = entry.getFile();
                fileObject.copy(folder, newName, fileObject.getExt());
                
            } else if(flag == NodeTransfer.MOVE) {
                FileObject fileObject = entry.getFile();
                FileLock lock = entry.takeLock();
                
                try {
                    fileObject.move(lock, folder, newName, fileObject.getExt());
                } finally {
                    lock.releaseLock ();
                }
            }
            
            return null;
        }
        
    } // End of class EntryPasteType.
}