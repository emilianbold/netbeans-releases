/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject.ui;

import java.awt.Image;
import javax.swing.Action;

import java.util.List;


import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import org.openide.util.Utilities;
import org.netbeans.api.project.Project;

import org.netbeans.modules.j2ee.common.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.earproject.ui.actions.OpenModuleProjectAction;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.modules.j2ee.earproject.ProjectPropertyProvider;
import org.netbeans.modules.j2ee.common.ui.customizer.ArchiveProjectProperties;
import org.netbeans.api.project.FileOwnerQuery;

/**
 * A simple node with no children.
 * Often used in conjunction with some kind of underlying data model, where
 * each node represents an element in that model. In this case, you should see
 * the Container Node template which will permit you to create a whole tree of
 * such nodes with the proper behavior.
 * @author vkraemer
 * @author Ludovic Champenois
 */
public class ModuleNode extends AbstractNode implements Node.Cookie {
    private VisualClassPathItem key;
    private AntProjectHelper helper;
    
    // will frequently accept an element from some data model in the constructor:
    public ModuleNode(VisualClassPathItem key, AntProjectHelper helper) {
        super(Children.LEAF);
        this.key = key;
        this.helper = helper;
//        this.epp = epp;
        // Whatever is most relevant to a user:
        //setDefaultAction(SystemAction.get(PropertiesAction.class));
        // Set FeatureDescriptor stuff:
        setName("preferablyUniqueNameForThisNodeAmongSiblings"); // or, super.setName if needed
        setDisplayName(key.getCompletePathInArchive()); // toString());
        setShortDescription(NbBundle.getMessage(ModuleNode.class, "HINT_ModuleNode"));
//        getCookieSet().add(key);
        // Add cookies, e.g.:
        /*
        getCookieSet().add(new OpenCookie() {
                public void open() {
                    // Open something useful...
                    // will typically use the data model somehow
                }
            });
         */
        // If this node represents an element in a data model of some sort, consider
        // creating your own cookie which captures the existence of that underlying data,
        // and add it to the cookie set. Then you can write actions sensitive to that cookie,
        // and they will not need to directly refer to this node class - only to the cookie
        // and the data model.
    }
    
    static private Action[] actions = null;
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        if (null == actions) {
            actions = new Action[] {
            // SystemAction.get(MyFavoriteAction.class),
            // null,                     // separator
//            /* according to what it can do:
//                   SystemAction.get(CutAction.class),
//                   SystemAction.get(CopyAction.class),
//                   null,
                SystemAction.get(OpenModuleProjectAction.class),
                   SystemAction.get(RemoveAction.class),
                   //SystemAction.get(RenameAction.class),
                   //null,
            // */
            //SystemAction.get(ToolsAction.class),
            //null,
            //SystemAction.get(PropertiesAction.class),
        };
        getCookieSet().add(this);
        }
        return actions;
    }
    public Image getIcon(int type){
        if (key.toString().endsWith("war")) //FIXME
            return Utilities.loadImage("org/netbeans/modules/j2ee/earproject/ui/resources/WebModuleNode.gif");
        else
            return Utilities.loadImage("org/netbeans/modules/j2ee/earproject/ui/resources/EjbModuleNodeIcon.gif");
            
    }
    
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // When you have help, change to:
        // return new HelpCtx(ModuleNode.class);
    }
    
    void removeFromJarContent() {
        List newList = new java.util.ArrayList();
        Project p = FileOwnerQuery.getOwner(helper.getProjectDirectory());
        ProjectPropertyProvider ppp =
                (ProjectPropertyProvider) p.getLookup().lookup(ProjectPropertyProvider.class);
        ArchiveProjectProperties epp = ppp.getProjectProperties();
       Object t = epp.get(EarProjectProperties.JAR_CONTENT_ADDITIONAL);
        if (!(t instanceof List)) {
            assert false : "jar content isn't a List???";
            return;
        }
        List vcpis = (List) t;
        newList.addAll(vcpis);
        newList.remove(key);
        epp.put(EarProjectProperties.JAR_CONTENT_ADDITIONAL, newList);
        //goners.clear();
        epp.store();
                try {
                    org.netbeans.api.project.ProjectManager.getDefault().saveProject(epp.getProject());
                }
                catch ( java.io.IOException ex ) {
                    org.openide.ErrorManager.getDefault().notify( ex );
                }
    }
    
    public VisualClassPathItem getVCPI() {
        return key;
    }
    // RECOMMENDED - handle cloning specially (so as not to invoke the overhead of FilterNode):
    /*
    public Node cloneNode() {
        // Try to pass in similar constructor params to what you originally got,
        // typically meaning passing in the same data model element:
        return new ModuleNode();
    }
     */
    
    // Create a property sheet:
    /*
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        // Make sure there is a "Properties" set:
        Sheet.Set props = sheet.get(Sheet.PROPERTIES); // get by name, not display name
        if (props == null) {
            props = Sheet.createPropertiesSet();
            sheet.put(props);
        }
        // typically the property will be constructed based on some underlying data model:
        props.put(new MyProp(someParams));
        return sheet;
    }
     */
    
    // Handle renaming:
    /*
    public boolean canRename() {
        return true;
    }
    public void setName(String nue) {
        // Typically implemented by changing the name of an element from an underlying
        // data model. This node class should be listening to changes in the name of the
        // element and calling super.setName when it notices any (or better, override getName
        // and perhaps getDisplayName and call fireNameChange).
        // For example, if there is an instance field
        // private final MyDataElement data;
        // then you might write this method as:
        // data.setID(nue);
        // where you would also have:
        // public String getName() {return data.getID();}
        // and in the constructor, if ModuleNode implements ModelListener:
        // data.addModelListener((ModelListener)WeakListener.create(ModelListener.class, this, data));
        // where the interface is implemented as:
        // public void modelChanged(ModelEvent ev) {fireNameChange(null, null);}
    }
     */
    
    // Handle deleting:
    /*
    public boolean canDestroy() {
        return true;
    }
    public void destroy() throws IOException {
        // Typically implemented by removing an element from an underlying data model.
        // For example, if there is an instance field
        // private final MyDataElement data;
        // then you might write this method as:
        // data.getContainingModel().removeElement(data);
        // The parent container children should be listening to the model, notice
        // the removal, set a new key list without this data element, and thus
        // remove this node from its children list.
    }
     */
    
    // Handle copying and cutting specially:
    /**/
    public boolean canCopy() {
        return false;
    }
    /*
    public boolean canCut() {
        return true;
    }
    public Transferable clipboardCopy() {
        // Add to, do not replace, the default node copy flavor:
        ExTransferable et = ExTransferable.create(super.clipboardCopy());
        et.put(new ExTransferable.Single(DataFlavor.stringFlavor) {
                protected Object getData() {
                    // just an example:
                    return ModuleNode.this.getDisplayName();
                    // more commonly, will use some underlying data model
                }
            });
        return et;
    }
    public Transferable clipboardCut() {
        // Add to, do not replace, the default node cut flavor:
        ExTransferable et = ExTransferable.create(super.clipboardCut());
        // This is not so useful because this node will not be destroyed afterwards
        // (it is up to the paste type to decide whether to remove the "original",
        // and it is not safe to assume that getData will only be called once):
        et.put(new ExTransferable.Single(DataFlavor.stringFlavor) {
                protected Object getData() {
                    // just an example:
                    return ModuleNode.this.getDisplayName();
                    // more commonly, will use some underlying data model
                }
            });
        return et;
    }
     */
    
    // Permit user to customize whole node at once (instead of per-property):
    /*
    public boolean hasCustomizer() {
        return true;
    }
    public Component getCustomizer() {
        // more commonly, will pass in underlying data:
        return new MyCustomizingPanel(this);
    }
     */
    
//class InnerRemoveAction extends NodeAction {
//    
//    public String getName() {
//        return NbBundle.getMessage(this.getClass(), "LBL_RemoveAction");
//    }
//    
//    public HelpCtx getHelpCtx() {
//        return HelpCtx.DEFAULT_HELP;
//        // If you will provide context help then use:
//        // return new HelpCtx(AddModuleAction.class);
//    }
//    
//    protected boolean asynchronous() {
//        return false;
//    }
//    
//    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
//        return activatedNodes.length >= 0;
//    }
//    
//    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
//        // launch add method dialog
//        // open some kind of dialog to select a project
//        //throw new UnsupportedOperationException(NbBundle.getMessage(this.getClass(), "EX_TEXT_UNIMPLEMENTED"));
//        //ModuleNode n = null;
//        //FilterNode fn = null;
//        //for (int i = 0; i < activatedNodes.length; i++) {
//            //fn = (FilterNode) activatedNodes[i];
//            //n = (ModuleNode) fn.getOriginal();
//            removeFromEar();     
//        //}
//        //if (null != n)
//            forceSave();
//    }
//    
//}
}
