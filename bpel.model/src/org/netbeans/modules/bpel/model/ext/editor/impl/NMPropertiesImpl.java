/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.model.ext.editor.impl;

import java.util.List;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.support.EntityUpdater;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.NMProperties;
import org.netbeans.modules.bpel.model.ext.editor.api.NMProperty;
import org.netbeans.modules.bpel.model.ext.editor.xam.EditorElements;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 *
 * @author anjeleevich
 */
public class NMPropertiesImpl extends EditorEntityImpl implements NMProperties {

    NMPropertiesImpl(BpelModelImpl model, Element e) {
        super(model, e);
    }

    NMPropertiesImpl(BpelBuilderImpl builder) {
        super(builder, EditorElements.NM_PROPERTIES);
    }

    public Class<? extends BpelEntity> getElementType() {
        return NMProperties.class;
    }

    protected Attribute[] getDomainAttributes() {
        return new Attribute[0];
    }

    public EntityUpdater getEntityUpdater() {
        return NMPropertiesEntityUpdater.getInstance();
    }

    public NMProperty[] getNMProperties() {
        readLock();
        try {
            List<NMProperty> list = getChildren(NMProperty.class);
            return list.toArray(new NMProperty[list.size()]);
        }
        finally {
            readUnlock();
        }
    }

    public NMProperty getNMProperty(int i) {
        return getChild(NMProperty.class , i);
    }

    public void removeNMProperty(int i) {
        removeChild(NMProperty.class , i);
    }

    public void setNMProperties(NMProperty[] nmProperties) {
        setArrayBefore(nmProperties, NMProperty.class );
    }

    public void setNMProperty(NMProperty nmProperty, int i) {
        setChildAtIndex(nmProperty, NMProperty.class, i);
    }

    public void addNMProperty(NMProperty nmProperty) {
        addChild(nmProperty, NMProperty.class);
    }

    public void insertNMProperty(NMProperty nmProperty, int i) {
        insertAtIndex(nmProperty, NMProperty.class, i);
    }

    private static class NMPropertiesEntityUpdater implements EntityUpdater {
        private static EntityUpdater INSTANCE =
                new NMPropertiesEntityUpdater();

        public static EntityUpdater getInstance() {
            return INSTANCE;
        }

        private NMPropertiesEntityUpdater() {
        }

        public void update(BpelEntity target, ExtensionEntity child, 
                Operation operation) 
        {
            if (target instanceof Editor) {
                Editor editor = (Editor) target;
                NMProperties nmProperties = (NMProperties) child;
                switch (operation) {
                case ADD:
                    editor.setNMProperties(nmProperties);
                    break;
                case REMOVE:
                    editor.removeNMProperties();
                    break;
                }
            }
        }

        public void update(BpelEntity target, ExtensionEntity child, int index, 
                Operation operation) 
        {
            update(target, child, operation);
        }
    }
}
