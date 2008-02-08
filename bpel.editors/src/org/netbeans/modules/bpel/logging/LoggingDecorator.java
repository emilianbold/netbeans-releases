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
package org.netbeans.modules.bpel.logging;

import java.util.List;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;

import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.ComponentsDescriptor;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.DecorationProvider;
import org.netbeans.modules.bpel.design.decoration.DecorationProviderFactory;
import org.netbeans.modules.bpel.design.decoration.Descriptor;
import org.netbeans.modules.bpel.design.selection.DiagramSelectionListener;
import org.netbeans.modules.bpel.model.ext.logging.api.Trace;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.08.13
 */
public final class LoggingDecorator extends DecorationProvider
        implements DecorationProviderFactory, DiagramSelectionListener {

    public LoggingDecorator() {
    }

    public DecorationProvider createInstance(DesignView view) {
        return new LoggingDecorator(view);
    }

    private LoggingDecorator(DesignView view) {
        super(view);
        getDesignView().getSelectionModel().addSelectionListener(this);
    }

    @Override
    public Decoration getDecoration(BpelEntity entity) {
        if (getTrace(entity) == null) {
            return null;
        }
        LoggingButton button 
                = new LoggingButton(getDesignView(),(ExtensibleElements) entity);
        ComponentsDescriptor descriptor = new ComponentsDescriptor();
        descriptor.add(button, ComponentsDescriptor.RIGHT_TB);
        return new Decoration(new Descriptor[]{descriptor});
    }

    public void selectionChanged(BpelEntity oldSelection, BpelEntity newSelection) {
        if (newSelection instanceof ExtensibleElements) {
            mySelectedElement = (ExtensibleElements) newSelection;

        } else {
            mySelectedElement = null;
        }
        fireDecorationChanged();
    }

    @Override
    public void release() {
        mySelectedElement = null;
        getDesignView().getSelectionModel().removeSelectionListener(this);
    }

    private Trace getTrace(BpelEntity entity) {
        if (entity instanceof ExtensibleElements) {
            ExtensibleElements myElement = (ExtensibleElements) entity;
            List<Trace> traceElements = myElement.getChildren(Trace.class);
            if (traceElements != null && traceElements.size() > 0) {
                return traceElements.get(0);
            }
        }
        return null;
    }
    private ExtensibleElements mySelectedElement;
}
