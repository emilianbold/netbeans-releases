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

package org.netbeans.modules.websvc.rest.wadl.model.impl;

import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.netbeans.modules.websvc.rest.wadl.model.visitor.WadlVisitor;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 * Visitor to add or remove a child of a Wadl component.
 * @author Ayub Khan
 */
public class ChildComponentUpdateVisitor<T extends WadlComponent> implements WadlVisitor, ComponentUpdater<T> {
    
    private Operation operation;
    private WadlComponent parent;
    private int index;
    private boolean canAdd = false;
    
    /**
     * Creates a new instance of ChildComponentUpdateVisitor
     */
    public ChildComponentUpdateVisitor() {
    }
    
    public boolean canAdd(WadlComponent target, Component child) {
        if (!(child instanceof WadlComponent)) return false;
        update(target, (WadlComponent) child, null);
        return canAdd;
    }
    
    public void update(WadlComponent target, WadlComponent child, Operation operation) {
        update(target, child, -1, operation);
    }
    
    public void update(WadlComponent target, WadlComponent child, int index, Operation operation) {
        assert target != null;
        assert child != null;

        this.parent = target;
        this.operation = operation;
        this.index = index;
        child.accept(this);
    }
    
    private void addChild(String eventName, DocumentComponent child) {
        ((AbstractComponent) parent).insertAtIndex(eventName, child, index);
    }
    
    private void removeChild(String eventName, DocumentComponent child) {
        ((AbstractComponent) parent).removeChild(eventName, child);
    }
    
    public void visit(Application child) {
        checkOperationOnUnmatchedParent();
    }
    
    public void visit(ExtensibilityElement child) {
        if (parent instanceof ExtensibilityElement.UpdaterProvider) {
            ExtensibilityElement.UpdaterProvider target = (ExtensibilityElement.UpdaterProvider) parent;
            ComponentUpdater<ExtensibilityElement> updater = target.getComponentUpdater();
            if (operation != null) {
                updater.update(target, child, index, operation);
            } else {
                canAdd = false;
                if (updater instanceof ComponentUpdater.Query) {
                    canAdd = ((ComponentUpdater.Query) updater).canAdd(target, child);
                } 
            }
        } else {
//            if (operation == Operation.ADD) {
//                parent.addExtensibilityElement(child);
//            } else if (operation == Operation.REMOVE) {
//                parent.removeExtensibilityElement(child);
//            } else if (operation == null) {
//                canAdd = true;
//                if (child instanceof ExtensibilityElement.ParentSelector) {
//                    canAdd = ((ExtensibilityElement.ParentSelector)child).canBeAddedTo(parent);
//                }
//            }
        }
    }

    private void checkOperationOnUnmatchedParent() {
        if (operation != null) {
            // note this unmatch should be caught by validation, 
            // we don't want the UI view to go blank on invalid but still well-formed document
            //throw new IllegalArgumentException("Unmatched parent-child components"); //NO18N
        } else {
            canAdd = false;
        }
    }

    public void visit(Doc doc) {
        if (operation == Operation.ADD) {
            addChild(WadlComponent.DOC_PROPERTY, doc);
        } else if (operation == Operation.REMOVE) {
            removeChild(WadlComponent.DOC_PROPERTY, doc);
        } else if (operation == null) {
            canAdd = true;
        }
    }

    public void visit(Grammars grammars) {
        if (parent instanceof Application) {
            Application target = (Application)parent;
            if (operation == Operation.ADD) {
                addChild(Application.GRAMMARS_PROPERTY, grammars);
            } else if (operation == Operation.REMOVE) {
                target.removeGrammars(grammars);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visit(Include include) {
        if (parent instanceof Grammars) {
            Grammars target = (Grammars)parent;
            if (operation == Operation.ADD) {
                addChild(Grammars.INCLUDE_PROPERTY, include);
            } else if (operation == Operation.REMOVE) {
                target.removeInclude(include);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visit(Resource resource) {
        if (parent instanceof Resources) {
            Resources target = (Resources)parent;
            if (operation == Operation.ADD) {
                addChild(Resources.RESOURCE_PROPERTY, resource);
            } else if (operation == Operation.REMOVE) {
                target.removeResource(resource);
            } else if (operation == null) {
                canAdd = true;
            }
        } else if (parent instanceof Resource) {
            Resource target = (Resource)parent;
            if (operation == Operation.ADD) {
                addChild(Resource.RESOURCE_PROPERTY, resource);
            } else if (operation == Operation.REMOVE) {
                target.removeResource(resource);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visit(Resources resources) {
        if (parent instanceof Application) {
            Application target = (Application)parent;
            if (operation == Operation.ADD) {
                addChild(Application.RESOURCES_PROPERTY, resources);
            } else if (operation == Operation.REMOVE) {
                target.removeResources(resources);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(ResourceType resourceType) {
        if (parent instanceof Application) {
            Application target = (Application)parent;
            if (operation == Operation.ADD) {
                addChild(Application.RESOURCE_TYPE_PROPERTY, resourceType);
            } else if (operation == Operation.REMOVE) {
                target.removeResourceType(resourceType);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visit(Method method) {
        if (parent instanceof Resource) {
            Resource target = (Resource)parent;
            if (operation == Operation.ADD) {
                addChild(Resource.METHOD_PROPERTY, method);
            } else if (operation == Operation.REMOVE) {
                target.removeMethod(method);
            } else if (operation == null) {
                canAdd = true;
            }
        } else if (parent instanceof Application) {
            Application target = (Application)parent;
            if (operation == Operation.ADD) {
                addChild(Application.METHOD_PROPERTY, method);
            } else if (operation == Operation.REMOVE) {
                target.removeMethod(method);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visit(Representation rep) {
        if (parent instanceof Request) {
            Request target = (Request)parent;
            if (operation == Operation.ADD) {
                addChild(Request.REPRESENTATION_PROPERTY, rep);
            } else if (operation == Operation.REMOVE) {
                target.removeRepresentation(rep);
            } else if (operation == null) {
                canAdd = true;
            }
        } else if (parent instanceof Response) {
            Response target = (Response)parent;
            if (operation == Operation.ADD) {
                addChild(Response.REPRESENTATION_PROPERTY, rep);
            } else if (operation == Operation.REMOVE) {
                target.removeRepresentation(rep);
            } else if (operation == null) {
                canAdd = true;
            }
        } else if (parent instanceof Application) {
            Application target = (Application)parent;
            if (operation == Operation.ADD) {
                addChild(Application.REPRESENTATION_PROPERTY, rep);
            } else if (operation == Operation.REMOVE) {
                target.removeRepresentation(rep);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visit(Fault fault) {
        if (parent instanceof Response) {
            Response target = (Response)parent;
            if (operation == Operation.ADD) {
                addChild(Response.FAULT_PROPERTY, fault);
            } else if (operation == Operation.REMOVE) {
                target.removeFault(fault);
            } else if (operation == null) {
                canAdd = true;
            }
        } else if (parent instanceof Application) {
            Application target = (Application)parent;
            if (operation == Operation.ADD) {
                addChild(Application.FAULT_PROPERTY, fault);
            } else if (operation == Operation.REMOVE) {
                target.removeFault(fault);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Request request) {
        if (parent instanceof Method) {
            Method target = (Method)parent;
            if (operation == Operation.ADD) {
                addChild(Method.REQUEST_PROPERTY, request);
            } else if (operation == Operation.REMOVE) {
                target.removeRequest(request);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visit(Response response) {
        if (parent instanceof Method) {
            Method target = (Method)parent;
            if (operation == Operation.ADD) {
                addChild(Method.RESPONSE_PROPERTY, response);
            } else if (operation == Operation.REMOVE) {
                target.removeResponse(response);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    
    public void visit(Link link) {
        if (parent instanceof Param) {
            Param target = (Param)parent;
            if (operation == Operation.ADD) {
                addChild(Param.LINK_PROPERTY, link);
            } else if (operation == Operation.REMOVE) {
                target.removeLink(link);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Option option) {
        if (parent instanceof Param) {
            Param target = (Param)parent;
            if (operation == Operation.ADD) {
                addChild(Param.OPTION_PROPERTY, option);
            } else if (operation == Operation.REMOVE) {
                target.removeOption(option);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }

    public void visit(Param param) {
        if (parent instanceof Resource) {
            Resource target = (Resource)parent;
            if (operation == Operation.ADD) {
                addChild(Resource.PARAM_PROPERTY, param);
            } else if (operation == Operation.REMOVE) {
                target.removeParam(param);
            } else if (operation == null) {
                canAdd = true;
            }
        } else if (parent instanceof Request) {
            Request target = (Request)parent;
            if (operation == Operation.ADD) {
                addChild(Request.PARAM_PROPERTY, param);
            } else if (operation == Operation.REMOVE) {
                target.removeParam(param);
            } else if (operation == null) {
                canAdd = true;
            }
        } else if (parent instanceof Response) {
            Response target = (Response)parent;
            if (operation == Operation.ADD) {
                addChild(Response.PARAM_PROPERTY, param);
            } else if (operation == Operation.REMOVE) {
                target.removeParam(param);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
}
