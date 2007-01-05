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

package org.netbeans.modules.j2ee.persistence.unit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.InnerPanelFactory;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;

/**
 * Factory for creating persistence unit panels.
 *
 * @author mkuchtiak
 * @author Erno Mononen
 */
public class PersistenceUnitPanelFactory implements InnerPanelFactory, PropertyChangeListener {
    
    private PUDataObject dObj;
    private ToolBarDesignEditor editor;
    /**
     * A naive cache for preventing reinitialization of persistence unit panels
     * if nothing has changed.
     */
    private Map<PersistenceUnit, PersistenceUnitPanel> cache = new HashMap<PersistenceUnit, PersistenceUnitPanel>(10);
    
    /** Creates a new instance of PersistenceUnitPanelFactory */
    PersistenceUnitPanelFactory(ToolBarDesignEditor editor, PUDataObject dObj) {
        this.dObj=dObj;
        this.editor=editor;
        dObj.addPropertyChangeListener(this);
    }
    
    /**
     * Gets the inner panel associated with the given key or creates a new inner
     * panel if the key had no associated panel yet.
     * @param key the persistence unit whose associated panel should be retrieved.
     */ 
    public SectionInnerPanel createInnerPanel(Object key) {
        if (!(key instanceof PersistenceUnit)) {
            throw new IllegalArgumentException("The given key must be an instance of PersistenceUnit"); //NO18N
        }
        PersistenceUnit punit = (PersistenceUnit) key;
        PersistenceUnitPanel panel = cache.get(punit);
        if (panel == null){
            panel = new PersistenceUnitPanel((SectionView)editor.getContentView(), dObj, punit);
            cache.put(punit, panel);
        }
        return panel;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (!XmlMultiViewDataObject.PROPERTY_DATA_MODIFIED.equals(evt.getPropertyName())){
            cache.clear();
        }
    }
}

