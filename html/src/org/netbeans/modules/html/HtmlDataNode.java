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

package org.netbeans.modules.html;

import java.lang.reflect.InvocationTargetException;
import org.openide.actions.OpenAction;
import org.openide.actions.ViewAction;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
/**
 * Node that represents HTML data object
 *
 * @author  rkubacki
 * @version 
 */
public class HtmlDataNode extends org.openide.loaders.DataNode {
    
    // private static final String PROP_FOR_EDIT = "ForEdit";  // NOI18N

    static final java.util.ResourceBundle bundle = NbBundle.getBundle (HtmlDataNode.class);

    /** Creates new HtmlDataNode */
    public HtmlDataNode (DataObject dobj, Children ch) {
        super (dobj, ch);
    }

    /** Get the default action for this node.
     * This action can but need not be one from the list returned
     * from {@link #getActions}. If so, the popup menu returned from {@link #getContextMenu}
     * is encouraged to highlight the action.
     *
     * @return default action, or <code>null</code> if there should be none
     */
    public SystemAction getDefaultAction () {
        /*
        if (getHtmlDataObject ().isForEdit ())
            return SystemAction.get (OpenAction.class);
        else
            return SystemAction.get (ViewAction.class);
         */
        try {
            if (getDataObject ().getPrimaryFile ().getFileSystem ().getCapability ().capableOf (FileSystemCapability.DOC))
                return SystemAction.get (ViewAction.class);
            else
                return SystemAction.get (OpenAction.class);
        }
        catch (FileStateInvalidException exc) {
            return SystemAction.get (OpenAction.class);
        }
    }
    
    /** Initialize a default
     * property sheet; commonly overridden. If {@link #getSheet}
     * is called and there is not yet a sheet,
     * this method is called to allow a subclass
     * to specify its properties.
     * <P>
     * <em>Warning:</em> Do not call <code>getSheet</code> in this method.
     * <P>
     * The default implementation returns an empty sheet.
     *
     * @return the sheet with initialized values (never <code>null</code>)
     *
    protected Sheet createSheet () {
        Sheet sheet = super.createSheet();

        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        ps.put(new PropertySupport.ReadWrite (
                   PROP_FOR_EDIT,
                   boolean.class,
                   bundle.getString("PROP_forEdit"),
                   bundle.getString("HINT_forEdit")
               ) {
                   public Object getValue() {
                       return new Boolean(getHtmlDataObject ().isForEdit ());
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       if (val instanceof Boolean) {
                           try {
                               getHtmlDataObject ().setForEdit (((Boolean) val).booleanValue());
                               return;
                           }
                           catch(java.io.IOException e) {
                           }
                       }
                       throw new IllegalArgumentException();
                   }
                   // public PropertyEditor getPropertyEditor() 
               });

        return sheet;
    }
     */

    private HtmlDataObject getHtmlDataObject () {
        return (HtmlDataObject) getDataObject ();
    }
}
