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


package org.netbeans.modules.vmd.properties;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.properties.common.PropertiesSupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;


/**
 *
 * @author devil
 */
public class PropertiesNode extends AbstractNode{
    
    private DesignComponent component;
    private String displayName;
    
    /** Creates a new instance of PropertiesNode */
    public PropertiesNode(DesignComponent component, Lookup lookup) {
        super(Children.LEAF, lookup);
        this.component = component;
    }
    
    public Sheet createSheet() {
        return PropertiesSupport.createSheet(component);
    }
    
    public String getDisplayName() {
        component.getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                displayName = InfoPresenter.getDisplayName(component);
            }
        });
        return displayName;
    }
    
    
}
