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

package org.netbeans.modules.dm.virtual.db.ui.property.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.dm.virtual.db.ui.property.IElement;
import org.netbeans.modules.dm.virtual.db.ui.property.INode;
import org.netbeans.modules.dm.virtual.db.ui.property.IProperty;
import org.netbeans.modules.dm.virtual.db.ui.property.IPropertyGroup;
import org.openide.util.NbBundle;


/**
 * @author Ritesh Adval
 */
public class PropertyGroup implements IPropertyGroup, Comparable {

    private String displayName;
    private String name;
    private INode parent;
    private int position;
    private List<IProperty> propertyList = new ArrayList<IProperty>();

    private PropertyChangeSupport pSupport = new PropertyChangeSupport(this);

    private String toolTip;

    private VetoableChangeSupport vSupport = new VetoableChangeSupport(this);

    public PropertyGroup() {
    }

    public void add(IElement element) {
        element.setParent(this);
        addProperty((IProperty) element);
    }

    public void addProperty(IProperty property) {
        propertyList.add(property);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pSupport.addPropertyChangeListener(listener);
    }

    public void addVetoableChangeListener(VetoableChangeListener listener) {
        vSupport.addVetoableChangeListener(listener);
    }

    public int compareTo(Object o) {
        if (!(o instanceof IPropertyGroup)) {
            throw new ClassCastException(NbBundle.getMessage(PropertyGroup.class, "MSG_Object") + o + NbBundle.getMessage(PropertyGroup.class, "MSG_notSame"));
        }

        IPropertyGroup propertyG = (IPropertyGroup) o;

        if (this.getPosition() < propertyG.getPosition()) {
            return -1;
        } else if (this.getPosition() > propertyG.getPosition()) {
            return 1;
        }

        return 0;
    }

    public void firePropertyChangeEvent() {
        pSupport.firePropertyChange(IPropertyGroup.VALID_ALL, this.isValid(), this.isValid());
    }

    public void firePropertyChangeEvent(String propertyName, Object oldVal, Object newVal) {
        pSupport.firePropertyChange(propertyName, oldVal, newVal);
    }

    public void fireVetoableChangeEvent(String propertyName, Object oldVal, Object newVal) throws PropertyVetoException {
        vSupport.fireVetoableChange(propertyName, oldVal, newVal);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return this.name;
    }

    public INode getParent() {
        return parent;
    }

    public int getPosition() {
        return this.position;
    }

    public List<IProperty> getProperties() {
        return propertyList;
    }

    public String getToolTip() {
        return this.toolTip;
    }

    public boolean isValid() {
        Iterator it = propertyList.iterator();
        while (it.hasNext()) {
            IProperty property = (IProperty) it.next();
            if (property.isRequired() && !property.isValid()) {
                return false;
            }
        }
        return true;
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pSupport.removePropertyChangeListener(listener);
    }

    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        vSupport.removeVetoableChangeListener(listener);
    }

    public void setDisplayName(String dName) {
        this.displayName = dName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParent(INode parent) {
        this.parent = parent;
    }

    public void setPosition(String position) {
        this.position = Integer.parseInt(position);
    }

    public void setToolTip(String tTip) {
        this.toolTip = tTip;
    }
}

