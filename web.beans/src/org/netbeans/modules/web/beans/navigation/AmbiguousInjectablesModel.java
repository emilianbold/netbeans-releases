/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.web.beans.navigation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.openide.filesystems.FileObject;

/**
 * @author ads
 */
public final class AmbiguousInjectablesModel extends DefaultTreeModel {
    
    private static final long serialVersionUID = -6845959436250662000L;

    private static final Logger LOG = Logger.getLogger(
            AmbiguousInjectablesModel.class.getName());
    
    static Element[] EMPTY_ELEMENTS_ARRAY = new Element[0];
    static ElementHandle<?>[] EMPTY_ELEMENTHANDLES_ARRAY = new ElementHandle[0];

    public AmbiguousInjectablesModel(Collection<Element> elements, 
            CompilationController controller ,MetadataModel<WebBeansModel> model ) 
    {
        super(null);

        myModel = model;
        if (elements.size() == 0 ) {
            myElementHandles = EMPTY_ELEMENTHANDLES_ARRAY;
        } else {
            List<ElementHandle<?>> elementHandlesList = 
                new ArrayList<ElementHandle<?>>(elements.size());

            for (Element el : elements) {
                elementHandlesList.add(ElementHandle.create(el));
            }

            myElementHandles = elementHandlesList.toArray(EMPTY_ELEMENTHANDLES_ARRAY);
        }

        update(elements , controller );
    }
    
    void update() {
        update(myElementHandles);
    }

    void fireTreeNodesChanged() {
        super.fireTreeNodesChanged(this, getPathToRoot((TreeNode)getRoot()), 
                null, null);
    }

    private void update(final ElementHandle<?>[] elementHandles) {
        if ((elementHandles == null) || (elementHandles.length == 0)) {
            return;
        }

            try {
                getModel().runReadAction(new MetadataModelAction<WebBeansModel, Void>() {
                        public Void run( WebBeansModel model ){
                            List<Element> elementsList = new ArrayList<Element>(
                                    elementHandles.length);

                            for (ElementHandle<?> elementHandle : elementHandles) {
                                final Element element = elementHandle.resolve(
                                        model.getCompilationController());
                                if (element != null) {
                                    elementsList.add(element);
                                }
                                else {
                                    LOG.warning(elementHandle.toString()+
                                            " cannot be resolved using: " 
                                            +model.getCompilationController().
                                            getClasspathInfo());
                                }
                            }

                            update(elementsList, model.getCompilationController());
                            return null;
                        }
                    });

                return;
            } 
            catch (MetadataModelException e ){
                LOG.log(Level.WARNING, e.getMessage(), e);
            }
            catch (IOException e ){
                LOG.log(Level.WARNING, e.getMessage(), e);
            }
    }

    private void update(final Collection<Element> elements, 
            CompilationController controller) 
    {
        if (elements.size()==0 ) {
            return;
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        Map<Element, InjectableTreeNode<? extends Element>> elementMap= 
            new LinkedHashMap<Element, InjectableTreeNode<? extends Element>>();

        for (Element element : elements) {
            FileObject fileObject = SourceUtils.getFile(
                    ElementHandle.create(element), controller.getClasspathInfo());
            if (element instanceof TypeElement) {
                // Type declaration 
                TypeTreeNode node = new TypeTreeNode(fileObject, 
                        (TypeElement)element, controller); 
                insertTreeNode( elementMap , (TypeElement)element, node , 
                        root , controller);
            }
            else if ( element instanceof ExecutableElement ){
                // Method definition
                MethodTreeNode node = new MethodTreeNode(fileObject, 
                        (ExecutableElement)element, controller);
                insertTreeNode( elementMap , (ExecutableElement)element , 
                        node , root , controller);
            }
            else  {
                // Should be produces field.
                InjectableTreeNode<Element> node = 
                    new InjectableTreeNode<Element>(fileObject, element,  
                            controller);
                insertTreeNode( elementMap , node , root );
            }
        }

        setRoot(root);
    }
    
    private void insertTreeNode( Map<Element, 
            InjectableTreeNode<? extends Element>> elementMap,TypeElement element , 
            TypeTreeNode node, DefaultMutableTreeNode root ,
            CompilationController controller)
    {
        TypeTreeNode parent = null;
        
        for( Entry<Element, InjectableTreeNode<? extends Element>> entry : 
                elementMap.entrySet())
        {
            Element key = entry.getKey();
            if ( !( key instanceof TypeElement )){
                continue;
            }
            TypeTreeNode injectableNode = (TypeTreeNode)entry.getValue();
            TypeElement typeElement = injectableNode.getElementHandle().
                resolve(controller);
            if (typeElement == null ){
                continue;
            }
            if ( controller.getTypes().isAssignable( element.asType(), 
                    typeElement.asType()))
            {
                if ( parent == null ){
                    parent = injectableNode;
                }
                else {
                    TypeElement parentElement = parent.getElementHandle().
                        resolve( controller );
                    if ( parentElement == null || 
                            controller.getTypes().isAssignable( 
                                    typeElement.asType(), parentElement.asType()))
                    {
                        parent = injectableNode;
                    }
                }
            }
        }
        
        DefaultMutableTreeNode parentNode = parent;
        
        if ( parentNode == null ){
            parentNode = root;
        }
        Enumeration children = parentNode.children();
        List<TypeTreeNode> movedChildren = new LinkedList<TypeTreeNode>();
        while (children.hasMoreElements()) {
            TypeTreeNode childNode = (TypeTreeNode) children.nextElement();
            ElementHandle<TypeElement> elementHandle = childNode
                    .getElementHandle();
            TypeElement child = elementHandle.resolve(controller);
            if (child == null) {
                continue;
            }
            if (controller.getTypes().isAssignable(child.asType(),
                    element.asType()))
            {
                movedChildren.add(childNode);
            }
        }

        for (TypeTreeNode typeTreeNode : movedChildren) {
            parentNode.remove(typeTreeNode);
            node.add(typeTreeNode);
        }
        parentNode.add(node);
        elementMap.put(element, node);
    }
    
    private void insertTreeNode( Map<Element, 
            InjectableTreeNode<? extends Element>> elementMap,
            ExecutableElement element , MethodTreeNode node, 
            DefaultMutableTreeNode root , CompilationController controller)
    {
        ExecutableElement parent = null;
        // TODO Auto-generated method stub
        
        if ( parent == null ){
            root.add( node );
        }
        elementMap.put( element, node  );
    }
    
    private void insertTreeNode( Map<Element, 
            InjectableTreeNode<? extends Element>> elementMap,
            InjectableTreeNode<Element> node, DefaultMutableTreeNode root )
    {
        root.add( node );
    }

    private MetadataModel<WebBeansModel> getModel(){
        return myModel;
    }
    
    private ElementHandle<?>[] myElementHandles;
    private MetadataModel<WebBeansModel> myModel;
}
