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

import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.loaders.DataFolder;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.Lookups;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.Utilities;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.earproject.ui.actions.AddModuleAction;

import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;

import org.netbeans.spi.project.support.ant.AntProjectHelper;

/**
 * A node with some children.
 * The children are controlled by some underlying data model.
 * Edit this template to work with the classes and logic of your data model.
 * @author vkraemer
 * @author Ludovic Champenois
 */
public class LogicalViewNode extends AbstractNode {

	private static Image J2EE_MODULES_BADGE = Utilities.loadImage( "org/netbeans/modules/j2ee/earproject/ui/resources/application_16.gif", true ); // NOI18N
    
    private final AntProjectHelper model;
    private final DataFolder aFolder;
	
    public LogicalViewNode(AntProjectHelper model, DataFolder folder) {
        super(new LogicalViewChildren(model), Lookups.fixed( new Object[] { model }));
        this.model = model;
        this.aFolder = folder;
        // Set FeatureDescriptor stuff:
        setName("preferablyUniqueNameForThisNodeAmongSiblings"); // or, super.setName if needed
        setDisplayName(NbBundle.getMessage(LogicalViewNode.class, "LBL_LogicalViewNode"));
        setShortDescription(NbBundle.getMessage(LogicalViewNode.class, "HINT_LogicalViewNode"));
        // Add cookies, e.g.:
        /*
        getCookieSet().add(new OpenCookie() {
                public void open() {
                    // Open something useful...
                    // typically using the data model.
                }
            });
         */
        // Make reorderable (typically will pass in the data model):
        // getCookieSet().add(new ReorderMe());
    }
    
	public Image getIcon( int type ) {        
		return computeIcon( false, type );
	}

	public Image getOpenedIcon( int type ) {
		return computeIcon( true, type );
	}

	private Image computeIcon( boolean opened, int type ) {
		if(aFolder != null) {
			Node folderNode = aFolder.getNodeDelegate();
			Image image = opened ? folderNode.getOpenedIcon( type ) : folderNode.getIcon( type );
			return Utilities.mergeImages( image, J2EE_MODULES_BADGE, 7, 7 );
		} else {
			// !PW FIXME We need a guarranteed folder node resource to avoid this edge case.
			return J2EE_MODULES_BADGE;
		}
	}
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(AddModuleAction.class),
        };
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // When you have help, change to:
        // return new HelpCtx(LogicalViewNode.class);
    }
    
    // RECOMMENDED - handle cloning specially (so as not to invoke the overhead of FilterNode):
    /*
    public Node cloneNode() {
        // Try to pass in similar constructor params to what you originally got:
        return new LogicalViewNode(model);
    }
     */
    
    // SOMETIMES RECOMMENDED - if you plan to make this the root node of an
    // Explorer window, you will need a handle so that the window can be persisted
    // across sessions. Child nodes do not generally need special handles; they
    // will be remembered (if needed, e.g. for node selection) according to the path
    // from the root node, traversed by Node.name.
    /*
    public Node.Handle getHandle() {
        return new LogicalViewHandle(model);
    }
    private static final class LogicalViewHandle implements Node.Handle {
        private static final long serialVersionUID = 1L;
        private MyDataModel model; // must be serializable for this to work of course!
        public LogicalViewHandle(MyDataModel model) {
            this.model = model;
        }
        public Node getNode() throws IOException {
            return new LogicalViewNode(model);
        }
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
        // Typically will pass the model to the property classes:
        props.put(new MyProp(someParams));
        return sheet;
    }
     */
    
    // Permit new subnodes to be created:
    /*
    public NewType[] getNewTypes() {
        return new NewType[] {new NewType() {
                public String getName() {
                    return NbBundle.getMessage(LogicalViewNode.class, "LBL_NewType");
                }
                // If you have help:
                // public HelpCtx getHelpCtx() {
                //     return LogicalViewNode.class.getName() + ".newType";
                // }
                public void create() throws IOException {
                    // Normally implemented by prompting the user with a dialog
                    // for some information; then creating and adding a new element
                    // with those parameters to the data model. This should cause the
                    // children to update automatically because of its listener.
                    // throw IOException if the data model operation fails.
                }
            }
        };
    }
     */
    
    // Permit things to be pasted into this node:
    /*
    protected void createPasteTypes(final Transferable t, List l) {
        // Make sure to pick up super impl, which adds intelligent node paste type:
        super.createPasteTypes(t, l);
        if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            l.add(new PasteType() {
                    public String getName() {
                        return NbBundle.getMessage(LogicalViewNode.class, "LBL_PasteType");
                    }
                    // If you have help:
                    // public HelpCtx getHelpCtx() {
                    //     return LogicalViewNode.class.getName() + ".pasteType";
                    // }
                    public Transferable paste() throws IOException {
                        try {
                            String data = (String)t.getTransferData(DataFlavor.stringFlavor);
                            // Or, you can look for nodes and related things in the transferable, using e.g.:
                            // Node n = NodeTransfer.node(t, NodeTransfer.COPY);
                            // Node[] ns = NodeTransfer.nodes(t, NodeTransfer.MOVE);
                            // MyCookie cookie = (MyCookie)NodeTransfer.cookie(t, NodeTransfer.COPY, MyCookie.class);
                            // do something, typically involving the data model...
                            // throw IOException if the data model operation fails.
                            // To leave the clipboard as is:
                            return null;
                            // To clear the clipboard:
                            // return ExTransferable.EMPTY;
                        } catch (UnsupportedFlavorException ufe) {
                            // Should not happen, since t said it supported this flavor, but:
                            throw new IOException(ufe.getMessage());
                        }
                    }
                });
        }
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
        // For example, you might write this method as:
        // model.setName(nue);
        // where you would also have:
        // public String getName() {return model.getName();}
        // and in the constructor, if LogicalViewNode implements ModelListener:
        // model.addModelListener((ModelListener)WeakListener.create(ModelListener.class, this, model));
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
        // For example, you might write this method as:
        // model.getParentModel().removeSubModel(model);
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
                    // typically based on the data model, but for example:
                    return LogicalViewNode.this.getDisplayName();
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
                    // typically based on the data model, but for example:
                    return LogicalViewNode.this.getDisplayName();
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
        // will typically be passed the data model to customize:
        return new MyCustomizingPanel(this);
    }
     */
    
    // Permit node to be reordered (you may also want to put
    // MoveUpAction and MoveDownAction on the subnodes, if you can,
    // but ReorderAction on the parent is enough):
    /*
    private class ReorderMe extends Index.Support {
     
        public Node[] getNodes() {
            return LogicalViewNode.this.getChildren().getNodes();
        }
     
        public int getNodesCount() {
            return getNodes().length;
        }
     
        // This assumes that there is exactly one child node per key.
        // If you are using e.g. Children.Array, you can use shortcut implementations
        // of the Index cookie.
        public void reorder(int[] perm) {
            // Remember: {2, 0, 1} cycles three items forwards.
            MyDataElement[] items = model.getChildElements();
            if (items.length != perm.length) throw new IllegalArgumentException();
            MyDataElement[] nue = new MyDataElement[perm.length];
            for (int i = 0; i < perm.length; i++) {
                nue[i] = old[perm[i]];
            }
            // Should trigger an automatic child node update because the children
            // should be listening:
            model.setChildElements(nue);
        }
     
    }
     */
    
}
