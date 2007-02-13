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

import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignComponent;

import java.awt.datatransfer.Transferable;

/**
 * @author David Kaspar
 */
public final class AcceptSupport {
    
    private AcceptSupport() {
    }
    
    public static boolean isAcceptable(DesignComponent component, Transferable transferable) {
        if (component == null)
            return false;
        ComponentProducer producer = DefaultDataFlavor.decodeFromDataFlavors (component.getDocument (), transferable);
        for (AbstractAcceptPresenter presenter : component.getPresenters (AbstractAcceptPresenter.class))
            switch (presenter.getKind ()) {
                case COMPONENT_PRODUCER:
                    if (presenter.isAcceptable (producer))
                        return true;
                    break;
                case TRANSFERABLE:
                    if (presenter.isAcceptable (transferable))
                        return true;
                    break;
            }
        return false;
    }
    
    public static boolean isAcceptable(DesignComponent component, ComponentProducer producer) {
        if (component == null)
            return false;
        for (AbstractAcceptPresenter presenter : component.getPresenters (AbstractAcceptPresenter.class))
            switch (presenter.getKind ()) {
                case COMPONENT_PRODUCER:
                    if (presenter.isAcceptable (producer))
                        return true;
                    break;
                case TRANSFERABLE:
                    break;
            }
        return false;
    }
    
    public static ComponentProducer.Result accept(DesignComponent component, Transferable transferable) {
        if (component == null)
            return null;
        ComponentProducer producer = DefaultDataFlavor.decodeFromDataFlavors (component.getDocument (), transferable);
        for (AbstractAcceptPresenter presenter : component.getPresenters (AbstractAcceptPresenter.class))
            switch (presenter.getKind ()) {
                case COMPONENT_PRODUCER:
                    if (presenter.isAcceptable (producer))
                        return presenter.accept (producer);
                    break;
                case TRANSFERABLE:
                    if (presenter.isAcceptable (transferable))
                        return presenter.accept (transferable);
                    break;
            }
        return null;
    }
    
    public static ComponentProducer.Result accept(DesignComponent component, ComponentProducer producer) {
        if (component == null)
            return null;
        for (AbstractAcceptPresenter presenter : component.getPresenters (AbstractAcceptPresenter.class))
            switch (presenter.getKind ()) {
                case COMPONENT_PRODUCER:
                    if (presenter.isAcceptable (producer))
                        return presenter.accept (producer);
                    break;
                case TRANSFERABLE:
                    break;
            }
        return null;
    }
    
}
