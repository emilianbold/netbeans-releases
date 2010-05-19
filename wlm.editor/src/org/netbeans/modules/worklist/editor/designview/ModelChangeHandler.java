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

package org.netbeans.modules.worklist.editor.designview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;

/**
 *
 * @author anjeleevich
 */
public class ModelChangeHandler implements ComponentListener, 
        PropertyChangeListener 
{
    private DesignView designView;
    private UpdateViewRunnable updateViewRunnable;

    private final Object sync = new Object();
    
    public ModelChangeHandler(DesignView designView) {
        this.designView = designView;
        getModel().addComponentListener(this);
        getModel().addPropertyChangeListener(this);
    }

    public DesignView getDesignView() {
        return designView;
    }

    public WLMModel getModel() {
        return designView.getModel();
    }

    private void updateView(EventObject event) {
        synchronized (sync) {
            if (updateViewRunnable == null) {
                updateViewRunnable = new UpdateViewRunnable();
                SwingUtilities.invokeLater(updateViewRunnable);
            } 
            updateViewRunnable.addEvent(event);
        }
    }

    private void updateViewImpl(List<EventObject> event) {
        WLMModel model = getModel();
        TTask task = (model == null) ? null : model.getTask();
        
        if ((model == null) || (task == null) 
                || (model.getState() != WLMModel.State.VALID)) 
        {
            designView.setModelIsBroken(true);
        } else {
            designView.setModelIsBroken(false);
            designView.getBasicPropertiesPanel().processWLMModelChanged();
            designView.getEscalationsPanel().processWLMModelChanged(true);
            designView.getActionsPanel().processWLMModelChanged(true);
            designView.getNotificationsPanel().processWLMModelChanged(true);
            designView.updateWidget();
        }
    }

    public void valueChanged(ComponentEvent event) {
        updateView(event);
    }

    public void childrenAdded(ComponentEvent event) {
        updateView(event);
    }

    public void childrenDeleted(ComponentEvent event) {
        updateView(event);
    }

    public void propertyChange(PropertyChangeEvent event) {
        updateView(event);
    }

    private class UpdateViewRunnable implements Runnable {
        List<EventObject> eventObjectList = new ArrayList<EventObject>();

        public void run() {
            synchronized (sync) {
                updateViewRunnable = null;
            }
            updateViewImpl(eventObjectList);
        }

        void addEvent(EventObject event) {
            eventObjectList.add(event);
        }
    }
}
