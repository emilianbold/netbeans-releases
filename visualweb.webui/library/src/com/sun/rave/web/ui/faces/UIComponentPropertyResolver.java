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
package com.sun.rave.web.ui.faces;

import com.sun.rave.web.ui.util.LogUtil;

import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;


/**
 * <p>Custom JSF <code>PropertyResolver that, when the <code>base</code>
 * object is a <code>UIComponent</code>, scans for a child with the
 * <code>id</code> specified by the property name.</p>
 */
public class UIComponentPropertyResolver extends PropertyResolver {


    // -------------------------------------------------------- Static Variables


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The original <code>PropertyResolver</code> to which we
     * will delegate unrecognized base objects.</p>
     */
    private PropertyResolver original = null;


    // ------------------------------------------------------------ Constructors


    public UIComponentPropertyResolver() {
        LogUtil.warning(UIComponentPropertyResolver.class, "UIComponentPropertyResolver(" + "NONE" + ")");
    }


    /**
     * <p>Construct a new {@link UIComponentPropertyResolver} that decorates
     * the specified <code>PropertyResolver</code>.</p>
     *
     * @param original Original <code>PropertyResolver</code> to decorate
     */
    public UIComponentPropertyResolver(PropertyResolver original) {

        this.original = original;
        if (LogUtil.fineEnabled(UIComponentPropertyResolver.class)) {
            LogUtil.fine(UIComponentPropertyResolver.class, "UIComponentPropertyResolver(" + original + ")"); // NOI18N
        }

    }
    

    // ------------------------------------------------ PropertyResolver Methods


    /**
     * <p>When the base object is a <code>UIComponent</code>, treat the
     * property name as the <code>id</code> of a child component to be
     * returned.  If there is no such child, delegate to the rest of
     * the chain (so that properties of a component may be accessed).</p>
     *
     * @param base Base object
     * @param property Property name
     */
    public Object getValue(Object base, Object property) {

        if (LogUtil.finestEnabled(UIComponentPropertyResolver.class)) {
            LogUtil.finest(UIComponentPropertyResolver.class, "getValue(" + base + "," + property + ")"); // NOI18N
        }

        if ((base == null) || (!(base instanceof UIComponent)) ||
            (property == null)) {
            if (LogUtil.finestEnabled(UIComponentPropertyResolver.class)) {
                LogUtil.finest(UIComponentPropertyResolver.class, "  Delegated to decorated PropertyResolver");
            }
            return original.getValue(base, property);
        }

	// Try to resolve to facet or child UIComponent
        UIComponent component = (UIComponent) base;
        String id = property.toString();

	// First check for a facet w/ that name
	UIComponent kid = (UIComponent) component.getFacets().get(id);
	if (kid != null) {
	    return kid;
	}

	// Now check for child component w/ that id
        if (component.getChildCount() < 1) {
            return original.getValue(base, property);
        }
        List kids = component.getChildren();
        for (int i = 0; i < kids.size(); i++) {
            kid = (UIComponent) kids.get(i);
            if (id.equals(kid.getId())) {
                if (LogUtil.finestEnabled(UIComponentPropertyResolver.class)) {
                    LogUtil.finest(UIComponentPropertyResolver.class, "  Returning child " + kid);
                }
                return kid;
            }
        }

	// Not found, delegate to original...
        return original.getValue(base, property);
    }


    /**
     * <p>When the base object is a <code>UIComponent</code>, treat the
     * index as the zero-relative index of the child to be returned.</p>
     *
     * @param base Base object
     * @param index Zero-relative child index
     */
    public Object getValue(Object base, int index) {

        if ((base == null) || (!(base instanceof UIComponent))) {
            return original.getValue(base, index);
        }

        UIComponent component = (UIComponent) base;
        if (component.getChildCount() < 1) {
            throw new PropertyNotFoundException("" + index);
        }
        try {
            return component.getChildren().get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new PropertyNotFoundException("" + index);
        }

    }


    /**
     * <p>When the base object is a <code>UIComponent</code>, treat the
     * property name as the <code>id</code> of a child component to be
     * replaced.  If there is no such child, delegate to the rest of the
     * chain (so that properties of a component may be accessed).</p>
     *
     * @param base Base object
     * @param property Property name
     * @param value Replacement component
     */
    public void setValue(Object base, Object property, Object value) {

        if ((base == null) || (!(base instanceof UIComponent)) ||
            (property == null) ||
            (value == null) || (!(value instanceof UIComponent))) {
            original.setValue(base, property, value);
            return;
        }

        UIComponent component = (UIComponent) base;
        String id = property.toString();
	// First check to for facet w/ this name
	if (component.getFacets().get(id) != null) {
	    component.getFacets().put(id, value);
	    return;
	}
	// Not a facet, see if it's a child
        if (component.getChildCount() < 1) {
            original.setValue(base, property, value);
            return;
        }
        List kids = component.getChildren();
        for (int i = 0; i < kids.size(); i++) {
            UIComponent kid = (UIComponent) kids.get(i);
            if (id.equals(kid.getId())) {
                kids.set(i, value);
                return;
            }
        }
        original.setValue(base, property, value);

    }


    /**
     * <p>When the base object is a <code>UIComponent</code>, treat the
     * index as the zero-relative index of the child to be replaced.</p>
     *
     * @param base Base object
     * @param index Zero-relative child index
     * @param value Replacement component
     */
    public void setValue(Object base, int index, Object value) {

        if ((base == null) || (!(base instanceof UIComponent)) ||
            (value == null) || (!(value instanceof UIComponent))) {
            original.setValue(base, index, value);
            return;
        }

        UIComponent component = (UIComponent) base;
        if (component.getChildCount() < 1) {
            throw new PropertyNotFoundException("" + index);
        }
        try {
            component.getChildren().set(index, value);
        } catch (IndexOutOfBoundsException e) {
            throw new PropertyNotFoundException("" + index);
        }

    }


    /**
     * <p>When the base object is a <code>UIComponent</code>, treat the
     * property name as the <code>id</code> of a child component to be
     * retrieved.  If the specified child actually exists, return
     * <code>false</code> (because replacement is allowed).  If there
     * is no such child, delegate to the rest of the chain (so that
     * component properties may be accessed).</p>
     *
     * @param base Base object
     * @param property Property name
     */
    public boolean isReadOnly(Object base, Object property) {

        if ((base == null) || (!(base instanceof UIComponent)) ||
            (property == null)) {
            return original.isReadOnly(base, property);
        }

        UIComponent component = (UIComponent) base;
        String id = property.toString();
	if (component.getFacets().get(id) != null) {
	    return false;
	}
        if (component.getChildCount() < 1) {
            return original.isReadOnly(base, property);
        }
        List kids = component.getChildren();
        for (int i = 0; i < kids.size(); i++) {
            UIComponent kid = (UIComponent) kids.get(i);
            if (id.equals(kid.getId())) {
                return false;
            }
        }
        return original.isReadOnly(base, property);

    }


    /**
     * <p>When the base object is a <code>UIComponent</code>, treat the
     * index as the zero-relative index of the child to be retrieved.
     * If the specified child actually exists, return <code>false</code>
     * (because replacement is allowed).</p>
     *
     * @param base Base object
     * @param index Zero-relative child index
     */
    public boolean isReadOnly(Object base, int index) {

        if ((base == null) || (!(base instanceof UIComponent))) {
            return original.isReadOnly(base, index);
        }

        UIComponent component = (UIComponent) base;
        if (component.getChildCount() < 1) {
            throw new PropertyNotFoundException("" + index);
        }
        try {
            component.getChildren().get(index);
            return false;
        } catch (IndexOutOfBoundsException e) {
            throw new PropertyNotFoundException("" + index);
        }

    }


    /**
     * <p>When the base object is a <code>UIComponent</code>, treat the
     * property name as the <code>id</code> of a child component to be
     * retrieved.  If the specified child actually exists, return
     * <code>javax.faces.component.UIComponent</code>.  If there is
     * no such child, delegate to the rest of the chain (so that component
     * properties may be accessed).</p>
     *
     * @param base Base object
     * @param property Property name
     */
    public Class getType(Object base, Object property) {

        if ((base == null) || (!(base instanceof UIComponent)) ||
            (property == null)) {
            return original.getType(base, property);
        }

        UIComponent component = (UIComponent) base;
        String id = property.toString();
	if (component.getFacets().get(id) != null) {
	    return UIComponent.class;
	}
        if (component.getChildCount() < 1) {
            return original.getType(base, property);
        }
        List kids = component.getChildren();
        for (int i = 0; i < kids.size(); i++) {
            UIComponent kid = (UIComponent) kids.get(i);
            if (id.equals(kid.getId())) {
                return UIComponent.class;
            }
        }
        return original.getType(base, property);

    }


    /**
     * <p>When the base object is a <code>UIComponent</code>, treat the
     * index as the zero-relative index of the child to be retrieved.
     * If the specified child actually exists,
     * return <code>javax.faces.component.UIComponent</code>.</p>
     *
     * @param base Base object
     * @param index Zero-relative child index
     */
    public Class getType(Object base, int index) {

        if ((base == null) || (!(base instanceof UIComponent))) {
            return original.getType(base, index);
        }

        UIComponent component = (UIComponent) base;
        if (component.getChildCount() < 1) {
            throw new PropertyNotFoundException("" + index);
        }
        try {
            component.getChildren().get(index);
            return UIComponent.class;
        } catch (IndexOutOfBoundsException e) {
            throw new PropertyNotFoundException("" + index);
        }

    }


}
