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

package org.netbeans.core.ui;

import java.util.ResourceBundle;

import org.openide.nodes.*;
import org.openide.actions.*;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.util.datatransfer.NewType;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/** Node that displays the content of Services directory and let's user 
* customize it.
*
* @author Jaroslav Tulach
*/
public final class LookupNode extends DataFolder.FolderNode implements NewTemplateAction.Cookie {
    /** extended attribute that signals that this object should not be visible to the user */
    private static final String EA_HIDDEN = "hidden"; // NOI18N

//    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];
    public LookupNode () {
        this ("Services"); // NOI18N
    }

    /** Constructor to be used with different directories.
    */
    private LookupNode (String root) {
        this (findFolder (root, "", false), root); // NOI18N
    }

    /** Constructs this node with given node to filter.
    */
    LookupNode (DataFolder folder, String root) {
        folder.super(new Ch(folder, root));
//        setShortDescription(bundle.getString("CTL_Lookup_hint"));
//        super.setIconBase ("/org/netbeans/modules/url/Lookup"); // NOI18N
        getCookieSet ().add (this);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (LookupNode.class);
    }


    public SystemAction[] createActions () {
        return new SystemAction[] {
            SystemAction.get(FileSystemAction.class),
            null,
            SystemAction.get(PasteAction.class),
            null,
            SystemAction.get(MoveUpAction.class),
            SystemAction.get(MoveDownAction.class),
            SystemAction.get(ReorderAction.class),
            null,
            SystemAction.get(NewAction.class),
            SystemAction.get(NewTemplateAction.class),
            null,
            SystemAction.get(ToolsAction.class),
            SystemAction.get(PropertiesAction.class),
        };
    }

    /** @return empty property sets. *
    public PropertySet[] getPropertySets () {
        return NO_PROPERTIES;
    }

    /** Supports index cookie in addition to standard support.
    * Redefined to prevent using DataFolder's cookies, so that common operations on folder like compile, etc. are not used here.
    * @param type the class to look for
    * @return instance of that class or null if this class of cookie
    *    is not supported
    */
    public Node.Cookie getCookie (Class type) {
        // no index for reordering toolbars, just for toolbar items
        if (type.isAssignableFrom(DataFolder.Index.class)) {
            // search for data object
            DataFolder dataObj = (DataFolder)super.getCookie(DataFolder.class);
            if (dataObj != null) {
                return new DataFolder.Index (dataObj, this);
            }
        }
        return super.getCookie (type);
    }

    /** NewTemplateAction.Cookie method implementation to create the desired
     * template wizard for this node.
     */
    public TemplateWizard getTemplateWizard () {
        TemplateWizard templateWizard = new TemplateWizard ();
        
        templateWizard.setTemplatesFolder (findFolder (root (), findName (), true));
        templateWizard.setTargetFolder (findFolder (root (), findName (), false));
        return templateWizard;
    }

    /** Gets the root from children.
    */
    private String root () {
        return ((Ch)getChildren ()).root;
    }

    /** Finds a prefix for templates.
    * @return prefix 
    */
    private static String prefTemplates (String root) {
        return "Templates/" + root; // NOI18N
    }

    /** Finds a prefix for objects.
    */
    private static String prefObjects (String root) {
        return root;
    }
    
    /** Finds name of the node by extracting the begin of nodes.
     * @return the string name
     */
    private String findName () {
        DataFolder df = (DataFolder)getCookie (DataFolder.class);
        String name = df.getPrimaryFile ().getPackageNameExt ('/', '.');
        if (name.startsWith (prefObjects (root ()))) {
            name = name.substring (prefObjects (root ()).length ());
        }
        return name;
    }

    /** Locates the right folder for given service name.
     * @param name of the resource
     * @param template folder for templates or for instances?
     * @return the folder
     */
    private static DataFolder findFolder (String root, String name, boolean template) {
        try {
            FileSystem fs = TopManager.getDefault ().getRepository ().getDefaultFileSystem ();
            if (template) {
                name = '/' + prefTemplates (root) + name;
            } else {
                name = '/' + prefObjects (root) + name;
            }
            FileObject fo = fs.findResource (name);
            
            if (fo == null && template) {
                // we do not create template directories, if it is missing
                // we use the root services template directory 
                name = prefTemplates (root);
            }
            
            if (fo == null) {
                // if the directory is missing, create new one
                fo = FileUtil.createFolder (fs.getRoot (), name);
            }
            
            return DataFolder.findFolder (fo);
        } catch (java.io.IOException ex) {
            IllegalStateException e = new IllegalStateException (ex.getMessage ());
            TopManager.getDefault ().getErrorManager ().copyAnnotation (e, ex);
            throw e;
        }
    }

    

    /** Children for the LookupNode. Creates LookupNodes or
    * LookupItemNodes as filter subnodes...
    */
    static final class Ch extends FilterNode.Children {
        /** the directory to use as a root of objects to display
        */
        final String root;

        /** @param or original node to take children from */
        public Ch (DataFolder folder, String root) {
            super(folder.getNodeDelegate ());
            this.root = root;
        }

        /** Overriden, returns LookupNode filters of original nodes.
        *
        * @param node node to create copy of
        * @return LookupNode filter of the original node
        */
        protected Node[] createNodes (Object n) {
            Node node = (Node)n;
            
            
            DataObject obj = (DataObject)node.getCookie(DataObject.class);
            //System.err.println("obj="+obj+" node="+node+" hidden="+(obj==null?null:obj.getPrimaryFile ().getAttribute (EA_HIDDEN)));
            
            if (
                obj != null && Boolean.TRUE.equals (obj.getPrimaryFile ().getAttribute (EA_HIDDEN))
            ) {
                return new Node[0];
            }
            
            if (obj instanceof DataFolder && n.equals (obj.getNodeDelegate ())) {
                return new Node[] { new LookupNode((DataFolder)obj, root) };
            }

            return new Node[] { node.cloneNode () }; 
        }

    }

}
