/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.propertysupport.nodes;

import org.openide.nodes.*;
import org.openide.util.NbBundle;

import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import java.awt.Image;

import java.util.List;
import java.util.LinkedList;
import javax.swing.UIManager;

/**
 * The node is intended as a presentation layer for UML settings inside Tools|Options dialog.
 * It is plugged into the netbeans settings framework via {@link #factory()} method
 * registered in .../uml/propertysupport/resources/layer.xml
 */ 
public final class PreferenceNode extends AbstractNode {

    private final IPropertyElement elment;
    private final PreferenceChildren prefChildren;

	public PreferenceNode()
	{
        this(findRootElement());
        setDisplayName(NbBundle.getMessage(PreferenceNode.class, "LAB_PreferenceNodeDisplayName")); // NOI18N
	}
	
    public PreferenceNode(IPropertyElement pe) {
        this(pe, new PreferenceChildren(pe));
    }
        
    private PreferenceNode(IPropertyElement pe, PreferenceChildren ch) {
        // Children.LEAF forces nb explorer to paint the node as leaf
        super(ch.getChildren().isEmpty()? Children.LEAF: ch);
            
        this.elment = pe;
        this.prefChildren = ch;
        IPropertyDefinition pdef = pe.getPropertyDefinition();
        setDisplayName(pdef.getDisplayName());

        resolveIconBase(pdef);
    }

	private IPropertyElement getElement()
	{
		return elment;
	}
	/**
	 *
	 *
	 */
	public Image getIcon(int type)
	{
		if (getElement() == findRootElement() )
		{ 
			Image image=(Image)UIManager.get(
				"Nb.Explorer.Folder.icon"); // NOI18N
			if (image!=null)
			{
				return image;
			}
			else
			{
				return super.getIcon(type);
			}
		}
		return super.getIcon(type);
	}
	
	
	/**
	 *
	 *
	 */
	public Image getOpenedIcon(int type)
	{
		return getIcon(type);
	}
	
    private void resolveIconBase(IPropertyDefinition pdef) {
        String imagePath = pdef.getImage();
        if (imagePath != null && imagePath.length() > 0) { // ignore empty image path
            int endOffset = imagePath.indexOf('.');
            if (endOffset > 0) { // remove .gif or whatever suffix
                imagePath = imagePath.substring(0, endOffset);
            }
            setIconBase(imagePath);
        }
    }

    public String getShortDescription() {
        return this.elment.getPropertyDefinition().getHelpDescription();
    }

    protected Sheet createSheet() {
        List<IPropertyElement> properties = prefChildren.getProperties();
        return PreferenceHelper.createNodeProperties(properties);
    }
    
    public static Node factory() {
        IPropertyElement rootEl = findRootElement();
        AbstractNode n = new PreferenceNode(rootEl);
        n.setDisplayName(NbBundle.getMessage(PreferenceNode.class, "LAB_PreferenceNodeDisplayName")); // NOI18N
//        n.setIconBase("org/netbeans/modules/uml/resources/uml"); // NOI18N
		n.setIconBaseWithExtension("org/netbeans/modules/uml/resources/uml"); // NOI18N
        return n;
    }
    
    private static IPropertyElement findRootElement() {
        IPropertyElement retVal = null;
        IPreferenceManager2 prefMan = ProductRetriever.retrieveProduct().getPreferenceManager();
        if (prefMan != null) {
            IPropertyElement[] pes = prefMan.getPropertyElements();
            if (pes != null && pes.length == 1) {
                retVal = pes[0];
            }
        }
        return retVal;
    }

    private static final class PreferenceChildren extends Children.Keys {

        private final List<IPropertyElement> children = new LinkedList<IPropertyElement>();
        private final List<IPropertyElement> properties = new LinkedList<IPropertyElement>();;
        private boolean initSubElements = true;
        private final IPropertyElement parentEl;

        public PreferenceChildren(IPropertyElement parent) {
            if (parent == null) {
                throw new NullPointerException();
            }
            this.parentEl = parent;
        }
        
        protected void addNotify() {
            super.addNotify();
            setKeys(getChildren());
        }

        protected Node[] createNodes(Object key) {
            Node[] retVal;
            if (key instanceof IPropertyElement) {
                retVal = new Node[] {new PreferenceNode((IPropertyElement) key)};
            } else {
                retVal = new Node[0];
            }
            return retVal;
        }

        public List<IPropertyElement> getChildren() {
            initSubElements();
            return children;
        }

        public List<IPropertyElement> getProperties() {
            initSubElements();
            return properties;
        }

        private void initSubElements() {
            synchronized (this.children) {
                if (this.initSubElements) {
                    PreferenceHelper.analyzeSubElements(this.parentEl, children, properties);
                    this.initSubElements = false;
                }
            }
        }

    }
    
}
