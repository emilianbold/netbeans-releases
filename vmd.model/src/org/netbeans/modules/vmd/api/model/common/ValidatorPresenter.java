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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

        try {
            checkCustomValidity ();
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable th) {
            Debug.error (th);
        }
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
