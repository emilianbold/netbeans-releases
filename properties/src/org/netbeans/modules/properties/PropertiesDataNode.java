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


import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.openide.actions.OpenAction;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
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
 * Node representing <code>PropertiesDataObject</code>.
 *
 * @author Petr Jiricka, Peter Zavadsky
 * @see PropertiesDataObject
 * @see org.openide.loaders.DataNode
 */
public class PropertiesDataNode extends DataNode {

    
    /** Creates data node for a given data object.
     * The provided children object will be used to hold all child nodes.
     * @param obj object to work with
     * @param ch children container for the node
     */
    public PropertiesDataNode(DataObject dataObject, Children children) {
        super(dataObject, children);
        initialize();
    }

    
    /** Initializes instance. Sets icon base and default action. */
    private void initialize () {
        setIconBase("org/netbeans/modules/properties/propertiesObject"); // NOI18N
        setDefaultAction(SystemAction.get(OpenAction.class));
    }

    /** Reads object from outppput stream. Used by deserialization. */
    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        is.defaultReadObject();
        initialize();
    }

    /** Gets new types that can be created in this node.
     * @return array with <code>NewLocaleType</code> */
    public NewType[] getNewTypes() {
        return new NewType[] {new NewLocaleType()};
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

        return;
    }
    
    /** Notifies an error happened when attempted to create locale which exists already. 
     * @param locale locale which already exists */ 
    private static void notifyError(String locale) {
        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
            MessageFormat.format(
                NbBundle.getBundle(PropertiesDataNode.class).getString("MSG_LangExists"),
                    new Object[] {locale}), NotifyDescriptor.ERROR_MESSAGE);
            TopManager.getDefault().notify(msg);
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
            if(entryIndex != -1) {
                newName = FileUtil.findFreeFileName(folder, newName, entry.getFile().getExt());
            }
            
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
    

    /** New type for properties node. It creates new locale for ths bundle. */
    private class NewLocaleType extends NewType {

        /** Overrides superclass method. */
        public String getName() {
            return NbBundle.getBundle(PropertiesDataNode.class).getString("LAB_NewLocaleAction");
        }

        /** Overrides superclass method.*/
        public HelpCtx getHelpCtx() {
            return new HelpCtx(Util.HELP_ID_ADDLOCALE);
        }

        /** Overrides superclass method. */
        public void create() throws IOException {
            final DataObject propertiesDataObject = (DataObject)getCookie(DataObject.class);

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
                        // OK pressed
                        if (evt.getSource() == DialogDescriptor.OK_OPTION) {
                            dialog[0].setVisible(false);
                            dialog[0].dispose();

                            String locale = panel.getLocale().toString();

                            try {
                                if(locale.length() == 0) {
                                    // It would mean default locale to create again.
                                    notifyError(locale);
                                    return;
                                }

                                final String newName = PropertiesDataLoader.PRB_SEPARATOR_CHAR + locale;

                                if(propertiesDataObject != null) {
                                    final FileObject folder = propertiesDataObject.getPrimaryFile().getParent();
                                    final FileObject defaultFile = propertiesDataObject.getPrimaryFile();
                                    final PropertiesEditorSupport editor = (PropertiesEditorSupport)propertiesDataObject.getCookie(PropertiesEditorSupport.class);

                                    // Actually create new file.
                                    // First try to create new file and load it by document content from default(=primary) file.
                                    if(editor != null && editor.isDocumentLoaded()) {
                                        // Loading from the document in memory.
                                        final Document document = editor.getDocument();
                                        final String[] buffer = new String[1];

                                        // Safely take the text from the document.
                                        document.render(new Runnable() {
                                            public void run() {
                                                try {
                                                    buffer[0] = document.getText(0, document.getLength());
                                                } catch(BadLocationException ble) {
                                                    // Should be not possible.
                                                    ble.printStackTrace();
                                                }
                                            }
                                        });

                                        if(buffer[0] != null) {
                                            folder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                                                public void run() throws IOException {
                                                    FileObject newFile = folder.createData(defaultFile.getName() + newName, PropertiesDataLoader.PROPERTIES_EXTENSION);
                                                    
                                                    Writer writer = new PropertiesEditorSupport.NewLineWriter(newFile.getOutputStream(newFile.lock()), editor.getNewLineType());

                                                    writer.write(buffer[0]);
                                                    writer.flush();
                                                    writer.close();
                                                }
                                            });
                                        }
                                    }
                                    
                                    // If first attempt failed, copy the default (=primary) file.
                                    if(folder.getFileObject(defaultFile.getName() + newName, PropertiesDataLoader.PROPERTIES_EXTENSION) == null) {
                                        folder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                                            public void run() throws IOException {
                                                defaultFile.copy(folder, defaultFile.getName() + newName, PropertiesDataLoader.PROPERTIES_EXTENSION);
                                            }
                                        }); // End of annonymous inner class extended from FileSystem.AtomicAction.
                                    }
                                }
                            } catch(IOException ioe) {
                                if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                                    ioe.printStackTrace();
                                
                                notifyError(locale);
                            }
                            
                        // Cancel pressed
                        } else if(evt.getSource() == DialogDescriptor.CANCEL_OPTION) {
                            dialog[0].setVisible(false);
                            dialog[0].dispose();
                        }
                    }
                }
            );
            
            dialog[0] = TopManager.getDefault().createDialog(dialogDescriptor);
            dialog[0].show();
        }

    } // End of NewLocaleType class.
    
}
