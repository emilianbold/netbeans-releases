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
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.JPanel;

import org.openide.actions.*;
import org.openide.DialogDescriptor;
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
 * Object that provides main functionality for properties data loader.
 * This class is final only for performance reasons,
 * can be unfinaled if desired.
 * @author Ian Formanek
 */
public final class PropertiesLocaleNode extends FileEntryNode {

    /** Icon base for the <code>PropertiesDataNode</code> node. */
    private static final String LOCALE_ICON_BASE = "org/netbeans/modules/properties/propertiesLocale"; // NOI18N
    
    /** Creates a new PropertiesLocaleNode for the given locale-specific file */
    public PropertiesLocaleNode (PropertiesFileEntry fe) {
        super(fe, fe.getChildren());
        setDisplayName(Util.getLocaleLabel(fe));
        setIconBase(LOCALE_ICON_BASE);
        setDefaultAction (SystemAction.get(OpenAction.class));

        getCookieSet().add(((PropertiesDataObject)getFileEntry().getDataObject()).getOpenSupport());
    }

    
    /** Lazily initialize set of node's actions.
     * Overrides superclass method.
     *
     * @return array of actions for this node
     */
    protected SystemAction[] createActions () {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(EditAction.class),
            SystemAction.get(FileSystemAction.class),
            null,
            SystemAction.get(CutAction.class),
            SystemAction.get(CopyAction.class),
            SystemAction.get(PasteAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            SystemAction.get(LangRenameAction.class),
            null,
            SystemAction.get(NewAction.class),
            SystemAction.get(SaveAsTemplateAction.class),
            null,
            SystemAction.get(ToolsAction.class),
            SystemAction.get(PropertiesAction.class)
        };
    }

    /** Gets the name. Note: It gets only the local part of the name  (e.g. "de_DE_EURO").
     * Reason is to allow user change only this part of name by renaming (on Node).
     * Overrides superclass method. 
     *
     * @return locale part of name
     */
    public String getName() {
        String localeName = Util.getLocalePartOfFileName (getFileEntry());
        if (localeName.length() > 0)
            if (localeName.charAt(0) == PropertiesDataLoader.PRB_SEPARATOR_CHAR)
                localeName = localeName.substring(1);
        
        return localeName;
    }
    
    /** Sets the system name. Overrides superclass method.
     *
     * @param name the new name
     */
    public void setName (String name) {
        if(!name.startsWith(getFileEntry().getDataObject().getPrimaryFile().getName())) {
            name = Util.assembleName (getFileEntry().getDataObject().getPrimaryFile().getName(), name);
        }
        
        // new name is same as old one, do nothing
        if (name.equals(super.getName())) return;
        
        super.setName (name);
        setDisplayName (Util.getLocaleLabel(getFileEntry()));
    }

    /** Clones this node. Overrides superclass method. */
    public Node cloneNode() {
        return new PropertiesLocaleNode((PropertiesFileEntry)getFileEntry());
    }

    /** This node can be renamed. Overrides superclass method. */
    public boolean canRename() {
        return getFileEntry().isDeleteAllowed ();
    }

    /** Returns all the item in addition to "normal" cookies. Overrides superclass method. */
    public Node.Cookie getCookie(Class cls) {
        if (cls.isInstance(getFileEntry())) return getFileEntry();
        return super.getCookie(cls);
    }

    /** List new types that can be created in this node. Overrides superclass method.
     * @return new types
     */
    public NewType[] getNewTypes () {
        return new NewType[] {
            new NewType() {

                /** Getter for name property. */
                public String getName() {
                    return NbBundle.getBundle(PropertiesDataNode.class).getString("LAB_NewPropertyAction");
                }
                
                /** Gets help context. */ 
                public HelpCtx getHelpCtx() {
                    return new HelpCtx (PropertiesLocaleNode.class.getName () + ".new_property"); // NOI18N
                }

                /** Creates new type. */
                public void create() throws IOException {
                    final Dialog[] dialog = new Dialog[1];
                    final Element.ItemElem item = new Element.ItemElem(
                        null, 
                        new Element.KeyElem(null, ""), // NOI18N
                        new Element.ValueElem(null, ""), // NOI18N
                        new Element.CommentElem(null, "") // NOI18N
                    );
                    final JPanel panel = new PropertyPanel(item);

                    DialogDescriptor dd = new DialogDescriptor(
                        panel,
                        NbBundle.getBundle(BundleEditPanel.class).getString("CTL_NewPropertyTitle"),
                        true,
                        DialogDescriptor.OK_CANCEL_OPTION,
                        DialogDescriptor.OK_OPTION,
                        new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                // OK pressed
                                if(evt.getSource() == DialogDescriptor.OK_OPTION) {
                                    dialog[0].setVisible(false);
                                    dialog[0].dispose();


                                    String key = item.getKey();
                                    String value = item.getValue();
                                    String comment = item.getComment();

                                    // add key to all entries
                                    if(!((PropertiesFileEntry)getFileEntry()).getHandler().getStructure().addItem(key, value, comment)) {
                                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                            MessageFormat.format(
                                                NbBundle.getBundle(PropertiesLocaleNode.class).getString("MSG_KeyExists"),
                                                new Object[] {UtilConvert.unicodesToChars(item.getKey())}
                                            ),
                                            NotifyDescriptor.ERROR_MESSAGE);
                                        TopManager.getDefault().notify(msg);
                                    }

                                // Cancel pressed
                                } else if (evt.getSource() == DialogDescriptor.CANCEL_OPTION) {
                                    dialog[0].setVisible(false);
                                    dialog[0].dispose();
                                }
                            }
                        }
                    );

                    dialog[0] = TopManager.getDefault().createDialog(dd);
                    dialog[0].show();

                }
                
            } // End of annonymous class.
        };
    }

    /** Creates paste types for this node. Overrides superclass method. */
    protected void createPasteTypes(Transferable t, List s) {
        super.createPasteTypes(t, s);
        Element.ItemElem item;
        Node n = NodeTransfer.node(t, NodeTransfer.MOVE);
        // cut
        if (n != null && n.canDestroy ()) {
            item = (Element.ItemElem)n.getCookie(Element.ItemElem.class);
            if (item != null) {
                // are we pasting into the same node
                Node n2 = getChildren().findChild(item.getKey());
                if (n == n2)
                    return;
                s.add(new KeyPasteType(item, n, KeyPasteType.MODE_PASTE_WITH_VALUE));
                s.add(new KeyPasteType(item, n, KeyPasteType.MODE_PASTE_WITHOUT_VALUE));
                return;
            }
        }
        // copy
        else {
            item = (Element.ItemElem)NodeTransfer.cookie(t, NodeTransfer.COPY, Element.ItemElem.class);
            if (item != null) {
                s.add(new KeyPasteType(item, null, KeyPasteType.MODE_PASTE_WITH_VALUE));
                s.add(new KeyPasteType(item, null, KeyPasteType.MODE_PASTE_WITHOUT_VALUE));
                return;
            }
        }
    }

    /** Paste type for keys. */
    private class KeyPasteType extends PasteType {
        
        /** Transferred item. */
        private Element.ItemElem item;

        /** The node to destroy or null. */
        private Node node;

        /** Paste mode. */
        int mode;

        /** Paste with value mode. */
        public static final int MODE_PASTE_WITH_VALUE = 1;
        
        /** Paste without value mode. */
        public static final int MODE_PASTE_WITHOUT_VALUE = 2;

        
        /** Constructs new <code>KeyPasteType</code> for the specific type of operation paste. */
        public KeyPasteType(Element.ItemElem item, Node node, int mode) {
            this.item = item;
            this.node = node;
            this.mode = mode;
        }

        /** Gets name. 
         * @return human presentable name of this paste type. */
        public String getName() {
            String pasteKey = mode == 1 ? "CTL_PasteKeyValue" : "CTL_PasteKeyNoValue";
            return NbBundle.getBundle(PropertiesLocaleNode.class).getString(pasteKey);
        }

        /** Performs the paste action.
         * @return <code>Transferable</code> which should be inserted into the clipboard after
         * paste action. It can be null, which means that clipboard content
         * should stay the same
         */
        public Transferable paste() throws IOException {
            PropertiesStructure ps = ((PropertiesFileEntry)getFileEntry()).getHandler().getStructure();
            String value;
            if (mode == MODE_PASTE_WITH_VALUE)
                value = item.getValue();
            else
                value = "";
            if (ps != null) {
                Element.ItemElem newItem = ps.getItem(item.getKey());
                if (newItem == null) {
                    ps.addItem(item.getKey(), value, item.getComment());
                }
                else {
                    newItem.setValue(value);
                    newItem.setComment(item.getComment());
                }
                if (node != null)
                    node.destroy();
            }

            return null;
        }
    } // End of inner KeyPasteType class.

}
