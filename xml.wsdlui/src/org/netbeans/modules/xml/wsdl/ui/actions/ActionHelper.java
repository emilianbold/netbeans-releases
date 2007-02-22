/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Created on Jul 6, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.actions;

import java.beans.PropertyVetoException;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.cookies.WSDLDefinitionNodeCookie;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.Utils;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ActionHelper {

    /**
     * Show and select the Node which represents the given component.
     *
     * @param  comp  model component to select.
     */
    public static void selectNode(WSDLComponent comp) {
        DataObject dobj = getDataObject(comp);
        if (dobj != null) {
            ViewComponentCookie cookie = (ViewComponentCookie) dobj.getCookie(
                    ViewComponentCookie.class);
            if (cookie != null) {
                // Do not switch views, use the currently showing view.
                cookie.view(ViewComponentCookie.View.CURRENT, comp,
                        (Object[]) null);
            }
        }
    }
    
    public static DataObject getDataObject(WSDLComponent comp) {
        try {
            WSDLModel model = comp.getModel();
            if (model != null) {
                FileObject fobj = (FileObject) model.getModelSource().
                        getLookup().lookup(FileObject.class);
                if (fobj != null) {
                    return DataObject.find(fobj);
                }
            }
        } catch (DataObjectNotFoundException donfe) {
            // fall through to return null
        }
        return null;
    }
    
/*	public static void selectNode(Component child, Node parent) {
		if(child != null && parent != null) {
			Children children = parent.getChildren();
			Node[] nodes = children.getNodes();
			if(nodes != null)
				for(int i = 0; i < nodes.length; i++) {
					Node childNode = nodes[i];
					WSDLElementCookie cookie = (WSDLElementCookie) childNode.getCookie(WSDLElementCookie.class);
					if(cookie != null && child.equals(cookie.getWSDLComponent())) {
						selectNode(childNode);
						break;
					}
			}
		}
	}*/
	
/*	public static void selectNode(Element element, Node parent) {
		if(element != null && parent != null) {
			Children children = parent.getChildren();
			Node[] nodes = children.getNodes();
			if(nodes != null)
				for(int i = 0; i < nodes.length; i++) {
					Node childNode = nodes[i];
					SchemaElementCookie cookie = (SchemaElementCookie) childNode.getCookie(WSDLElementCookie.class);
					if(cookie != null && element.equals(cookie.getElement())) {
						selectNode(childNode);
						break;
					}
			}
		}
	}*/
	
    public static void selectNode(final Node node) {
        if(node == null) {
            return;
        }
        
        WSDLDefinitionNodeCookie cookie = Utils.getWSDLDefinitionNodeCookie(node);
        if(cookie != null) {
            final ExplorerManager manager = cookie.getDefinitionsNode().getExplorerManager();
            
            Runnable run = new Runnable() {
                public void run() {
                    if(manager != null) {
                            try {
                                manager.setExploredContextAndSelection(node, new Node[] {node});
                            } catch(PropertyVetoException ex) {
                                //ignore this
                            }
                        
                    }
                }
            };
            SwingUtilities.invokeLater(run);
        }
    }
    
	public static void selectNode(final Node node, final Node parentNode) {
		if(node == null) {
			return;
		}
		
		WSDLDefinitionNodeCookie cookie = Utils.getWSDLDefinitionNodeCookie(node);
		if(cookie != null) {
			final ExplorerManager manager = cookie.getDefinitionsNode().getExplorerManager();
			
			Runnable run = new Runnable() {
				public void run() {
					if(manager != null) {
							try {
								manager.setExploredContextAndSelection(node, new Node[] {node});
							} catch(PropertyVetoException ex) {
								//ignore this
							}
						
					}
				}
			};
			SwingUtilities.invokeLater(run);
		}
	}

    public static void selectExploredContext(final Node node) {
        if(node == null) {
            return;
        }
        WSDLDefinitionNodeCookie cookie = Utils.getWSDLDefinitionNodeCookie(node);
        if(cookie != null) {
            final ExplorerManager manager = cookie.getDefinitionsNode().getExplorerManager();
            
            Runnable run = new Runnable() {
                public void run() {
                    if(manager != null) {
                        manager.setExploredContext(node);
                    }
                }
            };
            
            SwingUtilities.invokeLater(run);
            
        }
    }
}
