/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
public final class PreferenceNode extends AbstractNode
{
    /** Icon resource string for folder node */
    static final String FOLDER_ICON_BASE =
        "org/openide/loaders/defaultFolder"; // NOI18N
    private final IPropertyElement elment;
    private final PreferenceChildren prefChildren;
    
    public PreferenceNode()
    {
        this(findRootElement());
        setDisplayName(NbBundle.getMessage(PreferenceNode.class, "LAB_PreferenceNodeDisplayName")); // NOI18N
    }
    
    public PreferenceNode(IPropertyElement pe)
    {
        this(pe, new PreferenceChildren(pe));
    }
    
    private PreferenceNode(IPropertyElement pe, PreferenceChildren ch)
    {
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
            Image img = (Image)UIManager.get("Nb.Explorer.Folder.icon");
            if (img == null)
            {
                setIconBase(FOLDER_ICON_BASE);
                img = super.getIcon(type);
            }
            return img;
        }
        return super.getIcon(type);
    }
    
    
    /**
     *
     *
     */
    public Image getOpenedIcon(int type)
    {
        
         if (getElement() == findRootElement() )
        { 
             Image img = (Image)UIManager.get("Nb.Explorer.Folder.openedIcon");
             if (img == null)
             {
                 setIconBase(FOLDER_ICON_BASE);
                 img = super.getOpenedIcon(type);
             }
             return img;
         }
        return super.getOpenedIcon(type);
    }
    
    private void resolveIconBase(IPropertyDefinition pdef)
    {
        String imagePath = pdef.getImage();
        if (imagePath != null && imagePath.length() > 0)
        { // ignore empty image path
            int endOffset = imagePath.indexOf('.');
            if (endOffset > 0)
            { // remove .gif or whatever suffix
                imagePath = imagePath.substring(0, endOffset);
            }
            setIconBase(imagePath);
        }
    }
    
    public String getShortDescription()
    {
        return this.elment.getPropertyDefinition().getHelpDescription();
    }
    
    protected Sheet createSheet()
    {
        List<IPropertyElement> properties = prefChildren.getProperties();
        return PreferenceHelper.createNodeProperties(properties);
    }
    
    public static Node factory()
    {
        IPropertyElement rootEl = findRootElement();
        AbstractNode n = new PreferenceNode(rootEl);
        n.setDisplayName(NbBundle.getMessage(PreferenceNode.class, "LAB_PreferenceNodeDisplayName")); // NOI18N
//        n.setIconBase("org/netbeans/modules/uml/resources/uml"); // NOI18N
        n.setIconBaseWithExtension("org/netbeans/modules/uml/resources/uml"); // NOI18N
        return n;
    }
    
    private static IPropertyElement findRootElement()
    {
        IPropertyElement retVal = null;
        IPreferenceManager2 prefMan = ProductRetriever.retrieveProduct().getPreferenceManager();
        if (prefMan != null)
        {
            IPropertyElement[] pes = prefMan.getPropertyElements();
            if (pes != null && pes.length == 1)
            {
                retVal = pes[0];
            }
        }
        return retVal;
    }
    
    private static final class PreferenceChildren extends Children.Keys
    {
        
        private final List<IPropertyElement> children = new LinkedList<IPropertyElement>();
        private final List<IPropertyElement> properties = new LinkedList<IPropertyElement>();;
        private boolean initSubElements = true;
        private final IPropertyElement parentEl;
        
        public PreferenceChildren(IPropertyElement parent)
        {
            if (parent == null)
            {
                throw new NullPointerException();
            }
            this.parentEl = parent;
        }
        
        protected void addNotify()
        {
            super.addNotify();
            setKeys(getChildren());
        }
        
        protected Node[] createNodes(Object key)
        {
            Node[] retVal;
            if (key instanceof IPropertyElement)
            {
                retVal = new Node[] {new PreferenceNode((IPropertyElement) key)};
            }
            else
            {
                retVal = new Node[0];
            }
            return retVal;
        }
        
        public List<IPropertyElement> getChildren()
        {
            initSubElements();
            return children;
        }
        
        public List<IPropertyElement> getProperties()
        {
            initSubElements();
            return properties;
        }
        
        private void initSubElements()
        {
            synchronized (this.children)
            {
                if (this.initSubElements)
                {
                    PreferenceHelper.analyzeSubElements(this.parentEl, children, properties);
                    this.initSubElements = false;
                }
            }
        }
        
    }
    
}
