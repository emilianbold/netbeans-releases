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

import java.util.Set;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dialog;
import java.awt.datatransfer.Transferable;
import javax.swing.JPanel;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.actions.*;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeTransfer;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;


/** Object that provides main functionality for properties data loader.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
* @author Ian Formanek
*/

public class PropertiesLocaleNode extends FileEntryNode {

    static final String PROPERTIES_ICON_BASE2 = PropertiesDataObject.PROPERTIES_ICON_BASE2;

    /** Creates a new PropertiesLocaleNode for the given locale-specific file */
    public PropertiesLocaleNode (PropertiesFileEntry fe) {
        super(fe, fe.getChildren());
        setDisplayName(Util.getPropertiesLabel(fe));
        setIconBase(PROPERTIES_ICON_BASE2);
        setDefaultAction (SystemAction.get(OpenAction.class));

        getCookieSet().add(((PropertiesDataObject)getFileEntry().getDataObject()).getOpenSupport());
    }

    /** Lazily initialize set of node's actions (overridable).
    * The default implementation returns <code>null</code>.
    * <p><em>Warning:</em> do not call {@link #getActions} within this method.
    * If necessary, call {@link NodeOp#getDefaultActions} to merge in.
    * @return array of actions for this node, or <code>null</code> to use the default node actions
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

    /** Get the name. Note: It gets only the local part of the name  (e.g. "de_DE_EURO").
    * Reason is to allow user change only this part of name by renaming (on Node).
    * @return locale part of name
    */
    public String getName() {
        String localeName = Util.getLocalePartOfFileName (getFileEntry());
        if (localeName.length() > 0)
            if (localeName.charAt(0) == PropertiesDataLoader.PRB_SEPARATOR_CHAR)
                localeName = localeName.substring(1);
        
        return localeName;
    }
    
    /** Set the system name. Fires a property change event.
    * Also may change the display name according to {@link #displayFormat}.
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
        setDisplayName (Util.getPropertiesLabel(getFileEntry()));
    }

    /** Clones this node */
    public Node cloneNode() {
        return new PropertiesLocaleNode((PropertiesFileEntry)getFileEntry());
    }

    /** This node can be renamed. */
    public boolean canRename() {
        return getFileEntry().isDeleteAllowed ();
    }

    /** Returns a string from my bundle. */
    private String getString(String what) {
        return NbBundle.getBundle(PropertiesLocaleNode.class).getString(what);
    }

    /** Returns all the item in addition to "normal" cookies. */
    public Node.Cookie getCookie(Class cls) {
        if (cls.isInstance(getFileEntry())) return getFileEntry();
        return super.getCookie(cls);
    }

    /* List new types that can be created in this node.
    * @return new types
    */
    public NewType[] getNewTypes () {
        return new NewType[] {
                   new NewType() {

                       public String getName() {
                           return NbBundle.getBundle(PropertiesDataNode.class).getString("LAB_NewPropertyAction");
                       }

                       public HelpCtx getHelpCtx() {
                           return new HelpCtx (PropertiesLocaleNode.class.getName () + ".new_property");
                       }

                       public void create() throws IOException {
                           NewPropertyDialog dia = new NewPropertyDialog();
                           Dialog d = dia.getDialog();
                           dia.focusKey();
                           d.setVisible(true);
                           dia.focusKey();
                           if (dia.getOKPressed ()) {
                               if (((PropertiesFileEntry)getFileEntry()).getHandler().getStructure().addItem(
                                           dia.getKeyText(), dia.getValueText(), dia.getCommentText()))
                                   ;
                               else {
                                   NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                                                      java.text.MessageFormat.format(
                                                                          NbBundle.getBundle(PropertiesLocaleNode.class).getString("MSG_KeyExists"),
                                                                          new Object[] {dia.getKeyText()}),
                                                                      NotifyDescriptor.ERROR_MESSAGE);
                                   TopManager.getDefault().notify(msg);
                               }
                           }
                       }

                   } // end of inner class
               };
    }

    /* Creates paste types for this node. */
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
        /** transferred item */
        private Element.ItemElem item;

        /** the node to destroy or null */
        private Node node;

        /** Paste mode */
        int mode;

        public static final int MODE_PASTE_WITH_VALUE = 1;
        public static final int MODE_PASTE_WITHOUT_VALUE = 2;

        /** Constructs new KeyPasteType for the specific type of operation paste.*/
        public KeyPasteType(Element.ItemElem item, Node node, int mode) {
            this.item = item;
            this.node = node;
            this.mode = mode;
        }

        /* @return Human presentable name of this paste type. */
        public String getName() {
            String pasteKey = mode == 1 ? "CTL_PasteKeyValue" : "CTL_PasteKeyNoValue";
            return NbBundle.getBundle(PropertiesLocaleNode.class).getString(pasteKey);
        }

        /** Performs the paste action.
        * @return Transferable which should be inserted into the clipboard after
        *         paste action. It can be null, which means that clipboard content
        *         should stay the same.
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
    }

}

/*
 * <<Log>>
 *  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         10/12/99 Petr Jiricka    Changes in cookies
 *  9    Gandalf   1.8         7/16/99  Petr Jiricka    
 *  8    Gandalf   1.7         6/30/99  Ian Formanek    NodeTransfer related 
 *       changes to make it compilable
 *  7    Gandalf   1.6         6/24/99  Petr Jiricka    
 *  6    Gandalf   1.5         6/16/99  Petr Jiricka    
 *  5    Gandalf   1.4         6/10/99  Petr Jiricka    
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/6/99   Petr Jiricka    
 *  2    Gandalf   1.1         5/13/99  Petr Jiricka    
 *  1    Gandalf   1.0         5/12/99  Petr Jiricka    
 * $
 */
