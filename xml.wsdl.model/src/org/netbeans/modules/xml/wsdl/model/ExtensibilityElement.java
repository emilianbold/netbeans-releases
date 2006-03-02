/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model;

import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.xdm.ComponentUpdater;

/**
 *
 * @author rico
 * @author Nam Nguyen
 * Interface for wsdl extensibility elements
 */
public interface ExtensibilityElement extends WSDLComponent {
    
    interface UpdaterProvider extends ExtensibilityElement {
        /**
         * @return component updater to be used in merge operations when source sync happens.
         */
        <T extends ExtensibilityElement> ComponentUpdater<T> getComponentUpdater();
    }
    
    interface Embedder extends ExtensibilityElement {
        Model getEmbeddedModel();
    }
}
