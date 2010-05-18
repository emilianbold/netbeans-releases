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
package org.netbeans.modules.bpel.mapper.multiview;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListener;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;

/**
 * Listen to the changes in the BPEL, WSDL and Schema models in order to
 * synchronize the mapper view.
 * 
 * @author Nikita Krjukov
 */
public class SourcesSynchListener implements Runnable,
        ChangeEventListener,
        ComponentListener 
{
    private BpelModel bpelModel;
    private List<WSDLModel> wsdlModels;
    private List<SchemaModel> schemaModels;
    private BpelDesignContextController mController;
    
    private EventObject scheduledEvent = null;
    
    /** Creates a new instance of DesignContextSynchronizationListener */
    public SourcesSynchListener(BpelDesignContextController controller) {
        assert controller != null;
        mController = controller;
    }

    public void processDataObject(Object dataObject) {
        if (dataObject instanceof SchemaComponent) {
            register(((SchemaComponent) dataObject).getModel());
        } else if (dataObject instanceof Part) {
            register(((Part) dataObject).getModel());
        } else if (dataObject instanceof Message) {
            register(((Message) dataObject).getModel());
        }
    }

    public void register(BpelModel bpelModel) {
        if (this.bpelModel != bpelModel) {
            if (this.bpelModel != null) {
                this.bpelModel.removeEntityChangeListener(this);
            }

            if (bpelModel != null) {
                bpelModel.addEntityChangeListener(this);
            }

            this.bpelModel = bpelModel;
        }
    }

    public void register(WSDLModel wsdlModel) {
        if (wsdlModel == null) {
            return;
        }

        if (wsdlModels == null) {
            wsdlModels = new ArrayList<WSDLModel>();
        }

        if (!wsdlModels.contains(wsdlModel)) {
            wsdlModels.add(wsdlModel);
            wsdlModel.addComponentListener(this);
        }
    }

    public void register(SchemaModel schemaModel) {
        if (schemaModel == null) {
            return;
        }

        if (schemaModels == null) {
            schemaModels = new ArrayList<SchemaModel>();
        }

        if (!schemaModels.contains(schemaModel)) {
            schemaModels.add(schemaModel);
            schemaModel.addComponentListener(this);
        }
    }

    public void unregisterAll() {
        if (bpelModel != null) {
            bpelModel.removeEntityChangeListener(this);
            bpelModel = null;
        }

        if (wsdlModels != null) {
            for (WSDLModel model : wsdlModels) {
                model.removeComponentListener(this);
            }
            wsdlModels.clear();
            wsdlModels = null;
        }

        if (schemaModels != null) {
            for (SchemaModel model : schemaModels) {
                model.removeComponentListener(this);
            }
            schemaModels.clear();
            schemaModels = null;
        }
    }

    public void valueChanged(ComponentEvent event) {
        reloadMapper(event);
    }

    public void childrenAdded(ComponentEvent event) {
        reloadMapper(event);
    }

    public void childrenDeleted(ComponentEvent event) {
        reloadMapper(event);
    }

    public void notifyPropertyUpdated(PropertyUpdateEvent event) {
        reloadMapper(event);
    }

    public void notifyArrayUpdated(ArrayUpdateEvent event) {
        reloadMapper(event);
    }

    public void notifyEntityInserted(EntityInsertEvent event) {
        reloadMapper(event);
    }

    public void notifyEntityRemoved(EntityRemoveEvent event) {
        reloadMapper(event);
    }

    public void notifyEntityUpdated(EntityUpdateEvent event) {
        reloadMapper(event);
    }

    public void notifyPropertyRemoved(PropertyRemoveEvent event) {
        reloadMapper(event);
    }
    
    /**
     * Reloads mapper on event from bpel model
     */
    private void reloadMapper(final ChangeEvent event) {
        if (!event.isLastInAtomic()) return;

        if (SwingUtilities.isEventDispatchThread()) {
            if (bpelModel != null) {
                mController.invalidateMapper(event);
            }
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (bpelModel != null) {
                        mController.invalidateMapper(event);
                    }
                }
            });
        }
    }

    /**
     * Reloads mapper on event from WSDL or schema model
     */
    private void reloadMapper(ComponentEvent event) {
        synchronized (this) {
            if (scheduledEvent == null) {
                scheduledEvent = event;
                SwingUtilities.invokeLater(this);
            }
        }
    }

    public void run() {
        EventObject event;
        
        synchronized (this) {
            event = scheduledEvent;
            scheduledEvent = null;
        }
        
        if (event != null && bpelModel != null) {
            mController.invalidateMapper(event);
        }
    }
}
