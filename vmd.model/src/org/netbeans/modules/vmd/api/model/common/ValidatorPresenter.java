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
package org.netbeans.modules.vmd.api.model.common;

import org.netbeans.modules.vmd.api.model.*;

import java.util.*;

/**
 * @author David Kaspar
 */
public class ValidatorPresenter extends DynamicPresenter {

    // WARNING - "inside-main-tree" validator test is not covered completely because Validator is checked on changed components only
    // for proper work - it requires that all components (at least in main-tree) will be checked at the end of transaction

    private boolean mustHaveDescriptor;
    private boolean referencesFromMainTreeOnly;
    private boolean allChildrenMustBeInProperties;
    private ArrayList<String> propertiesUsingChildrenOnly = new ArrayList<String> ();
    private List<String> propertiesUsingChildrenOnlyUm = Collections.unmodifiableList (propertiesUsingChildrenOnly);
    private ArrayList<TypeID> validChildrenTypeIDs = new ArrayList<TypeID> ();
    private List<TypeID> validChildrenTypeIDsUm = Collections.unmodifiableList (validChildrenTypeIDs);

    public final boolean isMustHaveDescriptor () {
        return mustHaveDescriptor;
    }

    public final ValidatorPresenter setMustHaveDescriptor (boolean mustHaveDescriptor) {
        this.mustHaveDescriptor = mustHaveDescriptor;
        return this;
    }

    public final boolean hasReferencesFromMainTreeOnly () {
        return referencesFromMainTreeOnly;
    }

    public final ValidatorPresenter setReferencesFromMainTreeOnly (boolean referencesFromMainTreeOnly) {
        this.referencesFromMainTreeOnly = referencesFromMainTreeOnly;
        return this;
    }

    public final boolean hasAllChildrenMustBeInProperties () {
        return allChildrenMustBeInProperties;
    }

    public final ValidatorPresenter setAllChildrenMustBeInProperties (boolean allChildrenMustBeInProperties) {
        this.allChildrenMustBeInProperties = allChildrenMustBeInProperties;
        return this;
    }

    public final List<String> getPropertiesUsingChildrenOnly () {
        return propertiesUsingChildrenOnlyUm;
    }

    public final ValidatorPresenter addPropertiesUsingChildrenOnly (String... propertyNames) {
        for (String propertyName : propertyNames)
            propertiesUsingChildrenOnly.add (propertyName);
        return this;
    }

    public final List<TypeID> getValidChildrenTypeIDs () {
        return validChildrenTypeIDsUm;
    }

    public final ValidatorPresenter addValidChildrenTypeID (TypeID... typeIDs) {
        for (TypeID typeID : typeIDs)
            validChildrenTypeIDs.add (typeID);
        return this;
    }

    public final void checkValidity () {
        assert checkValidatyAssert ();

        try {
            checkCustomValidity ();
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable th) {
            Debug.error (th);
        }
    }
    
    private boolean checkValidatyAssert () {
        DesignComponent component = getComponent ();
        assert component != null;

        if (mustHaveDescriptor)
            assert component.getComponentDescriptor () != null;

        if (validChildrenTypeIDs.size () > 0) {
            DescriptorRegistry registry = component.getDocument ().getDescriptorRegistry ();
            Collection<DesignComponent> children = component.getComponents ();
        mainloop:
            for (DesignComponent child : children) {
                for (TypeID typeID : validChildrenTypeIDs) {
                    if (registry.isInHierarchy (typeID, child.getType ()))
                        continue mainloop;
                }
                assert false;
            }
        }

        HashSet<DesignComponent> references = new HashSet<DesignComponent> ();
        if (hasReferencesFromMainTreeOnly ()  ||  hasAllChildrenMustBeInProperties ()) {
            ComponentDescriptor descriptor = component.getComponentDescriptor ();

            Collection<PropertyDescriptor> propertyDescriptors = descriptor.getPropertyDescriptors ();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                PropertyValue value = component.readProperty (propertyDescriptor.getName ());
                Debug.collectAllComponentReferences (value, references);
            }
        }

        if (hasReferencesFromMainTreeOnly ()) {
            DesignComponent rootComponent = component.getDocument ().getRootComponent ();
            for (DesignComponent reference : references) {
                for (;;) {
                    DesignComponent parentComponent = reference.getParentComponent ();
                    if (parentComponent == null)
                        break;
                    reference = parentComponent;
                }
                assert reference == rootComponent;
            }
        }

        if (hasAllChildrenMustBeInProperties ()) {
            Collection<DesignComponent> children = component.getComponents ();
            for (DesignComponent child : children)
                assert references.contains (child);
        }

        if (! propertiesUsingChildrenOnly.isEmpty ()) {
            references = new HashSet<DesignComponent> ();
            for (String propertyName : propertiesUsingChildrenOnly)
                Debug.collectAllComponentReferences (component.readProperty (propertyName), references);
            HashSet<DesignComponent> children = new HashSet<DesignComponent> (component.getComponents ());
            for (DesignComponent reference : references)
                assert children.contains (reference);
        }

        return true;
    }

    public final void notifyAttached (DesignComponent component) {
    }

    public final void notifyDetached (DesignComponent component) {
    }

    public final DesignEventFilter getEventFilter () {
        return null;
    }

    public void designChanged (DesignEvent event) {
    }

    public void presenterChanged (PresenterEvent event) {
    }

    protected void checkCustomValidity () {
    }

}
