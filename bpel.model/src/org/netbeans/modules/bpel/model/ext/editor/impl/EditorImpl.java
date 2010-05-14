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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.model.ext.editor.impl;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.NMProperties;
import org.netbeans.modules.bpel.model.ext.editor.api.NestedExtensionsVisitor;
import org.netbeans.modules.bpel.model.ext.editor.xam.EditorElements;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @author nk160297
 * @version 1.0
 */
public class EditorImpl extends EditorExtensionContainerImpl implements Editor {

    EditorImpl(BpelModelImpl model, Element e ) {
        super(model, e);
    }

    EditorImpl(BpelBuilderImpl builder ) {
        super(builder, EditorElements.EDITOR);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.netbeans.modules.soa.model.bpel2020.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Editor.class;
    }

    protected Attribute[] getDomainAttributes() {
        return new Attribute[0];
    }

    public NMProperties getNMProperties() {
        return getChild(NMProperties.class);
    }

    public void removeNMProperties() {
        removeChild(NMProperties.class);
    }

    public void setNMProperties(NMProperties nmProperties) {
        setChild(nmProperties, NMProperties.class);
    }

    public void accept(NestedExtensionsVisitor visitor) {
        visitor.visit(this);
    }

}
