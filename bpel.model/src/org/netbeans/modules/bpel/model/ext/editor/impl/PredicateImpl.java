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

import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.ext.editor.api.NestedExtensionsVisitor;
import org.netbeans.modules.bpel.model.ext.editor.api.Predicate;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.bpel.model.ext.editor.xam.EditorAttributes;
import org.netbeans.modules.bpel.model.ext.editor.xam.EditorElements;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 *
 * @author nk160297
 * @version 1.0
 */
public class PredicateImpl extends EditorExtensionContainerImpl implements Predicate {

    private static AtomicReference<Attribute[]> myAttributes =
        new AtomicReference<Attribute[]>();

    PredicateImpl(BpelModelImpl model, Element e ) {
        super(model, e);
    }

    PredicateImpl(BpelBuilderImpl builder ) {
        super(builder, EditorElements.PREDICATE);
    }

    @Override
    protected BpelEntity create( Element element ) {
        return null;
    }

    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] ret = new Attribute[] {
                EditorAttributes.SOURCE,
                EditorAttributes.PATH,
            };
            myAttributes.compareAndSet( null ,  ret);
        }
        return myAttributes.get();
    }

    public Class<? extends BpelEntity> getElementType() {
        return Predicate.class;
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

    public String getPath() {
        readLock();
        try {
            return getAttribute(EditorAttributes.PATH);
        }
        finally {
            readUnlock();
        }
    }

    public void setPath(String value) throws VetoException {
        setBpelAttribute(EditorAttributes.PATH, value);
    }

    public void removePath() {
        removeAttribute(EditorAttributes.PATH);
    }

    public void accept(NestedExtensionsVisitor visitor) {
        visitor.visit(this);
    }

}

