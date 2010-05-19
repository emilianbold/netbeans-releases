/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.wlm.model.api.TAction;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;

/**
 * Listen to the changes in the WLM model and synchronize the mapper view.
 * Now it actually only initialize full reload of the mapper.
 * 
 * @author nk160297
 */
public class WlmModelSynchListener implements Runnable,
        PropertyChangeListener,
        ComponentListener 
{
    private WLMModel mWlmModel;
    private List<WSDLModel> wsdlModels;
    private List<SchemaModel> schemaModels;
    private DesignContextController mController;
    
    private EventObject scheduledEvent = null;
    
    /** Creates a new instance of DesignContextSynchronizationListener */
    public WlmModelSynchListener(DesignContextController controller) {
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

    public void register(WLMModel wlmModel) {
        if (this.mWlmModel != wlmModel) {
            if (this.mWlmModel != null) {
                this.mWlmModel.removePropertyChangeListener(this);
            }

            if (wlmModel != null) {
                wlmModel.addPropertyChangeListener(this);
            }

            this.mWlmModel = wlmModel;
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
        if (mWlmModel != null) {
            mWlmModel.removePropertyChangeListener(this);
            mWlmModel = null;
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

    public void propertyChange(PropertyChangeEvent event) {
        if (event.getOldValue() instanceof TAction &&
                event.getNewValue() == null) {
            // An action deleted
            TAction deletedAction = TAction.class.cast(event.getOldValue());
            mController.processActionDeletion(deletedAction);
        } else {
            // In case of any other change in model:
            reloadMapper(event);
        }
    }

    /**
     * Reloads mapper on event from WLM model
     */
    private void reloadMapper(final PropertyChangeEvent event) {
        // if (!event.isLastInAtomic()) return;

        if (SwingUtilities.isEventDispatchThread()) {
            if (mWlmModel != null) {
                mController.reloadMapper(event);
            }
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (mWlmModel != null) {
                        mController.reloadMapper(event);
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
        
        if (event != null && mWlmModel != null) {
            mController.reloadMapper(event);
        }
    }

}
