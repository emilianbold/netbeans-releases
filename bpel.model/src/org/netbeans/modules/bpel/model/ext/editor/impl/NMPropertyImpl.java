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

import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.EntityUpdater;
import org.netbeans.modules.bpel.model.ext.editor.api.NMProperties;
import org.netbeans.modules.bpel.model.ext.editor.api.NMProperty;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.bpel.model.ext.editor.xam.EditorAttributes;
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
public class NMPropertyImpl extends EditorEntityImpl implements NMProperty {

    private static AtomicReference<Attribute[]> myAttributes =
        new AtomicReference<Attribute[]>();

    NMPropertyImpl(BpelModelImpl model, Element e ) {
        super(model, e);
    }

    NMPropertyImpl(BpelBuilderImpl builder ) {
        super(builder, EditorElements.NM_PROPERTY);
    }

    @Override
    protected BpelEntity create( Element element ) {
        return null;
    }

    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] ret = new Attribute[] {
                EditorAttributes.NM_PROPERTY,
                EditorAttributes.DISPLAY_NAME,
                EditorAttributes.SOURCE
            
            };
            myAttributes.compareAndSet( null ,  ret);
        }
        return myAttributes.get();
    }

    public Class<? extends BpelEntity> getElementType() {
        return NMProperty.class;
    }

    public EntityUpdater getEntityUpdater() {
        return NMPropertyEntityUpdater.getInstance();
    }

    public String getDisplayName() {
        readLock();
        String attr = null;
        try {
            attr = getAttribute(EditorAttributes.DISPLAY_NAME);
        } finally {
            readUnlock();
        }
        return attr;
    }

    public void setDisplayName(String value) throws VetoException {
        setBpelAttribute(EditorAttributes.DISPLAY_NAME, value);
    }

    public void removeDisplayName() {
        removeAttribute(EditorAttributes.DISPLAY_NAME);
    }


    public String getNMProperty() {
        readLock();
        String attr = null;
        try {
            attr = getAttribute(EditorAttributes.NM_PROPERTY);
        } finally {
            readUnlock();
        }
        return attr;
    }

    public void setNMProperty(String value) throws VetoException {
        setBpelAttribute(EditorAttributes.NM_PROPERTY, value);
    }

    public void removeNMProperty() {
        removeAttribute(EditorAttributes.NM_PROPERTY);
    }
    
    public Source getSource() {
        readLock();
        try {
            String str = getAttribute(EditorAttributes.SOURCE);
            if (str == null) {
                return Source.FROM;
            } else {
                return Source.forString(str);
            }
        }
        finally {
            readUnlock();
        }
    }

    public void setSource(Source value) {
        setBpelAttribute(EditorAttributes.SOURCE, value);
    }

    public void removeSource() {
        removeAttribute(EditorAttributes.SOURCE);
    }    
    
    private static class NMPropertyEntityUpdater implements EntityUpdater {
        private static EntityUpdater INSTANCE =
                new NMPropertyEntityUpdater();

        public static EntityUpdater getInstance() {
            return INSTANCE;
        }

        private NMPropertyEntityUpdater() {

        }

        public void update(BpelEntity target, ExtensionEntity child, 
                Operation operation) 
        {
            if (target instanceof NMProperties) {
                NMProperties nmProperties = (NMProperties) target;
                NMProperty nmProperty = (NMProperty) child;
                switch (operation) {
                    case ADD:
                        nmProperties.addNMProperty(nmProperty);
                        break;
                    case REMOVE:
                        nmProperties.remove(nmProperty);
                        break;
                }
            }
        }

        public void update(BpelEntity target, ExtensionEntity child, int index, 
                Operation operation) 
        {
            if (target instanceof NMProperties) {
                NMProperties nmProperties = (NMProperties) target;
                NMProperty nmProperty = (NMProperty) child;
                switch (operation) {
                    case ADD:
                        nmProperties.insertNMProperty(nmProperty, index);
                        break;
                    case REMOVE:
                        nmProperties.remove(nmProperty);
                        break;
                }
            }
        }
    }
}

