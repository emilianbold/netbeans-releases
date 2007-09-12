/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.bpel.nodes.synchronizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model;
import org.openide.util.WeakListeners;

/**
 * Helper class to create a weak listeners to any XAM model.
 * Dispatches the events from model in AWT thread
 * 
 * @author Alexey
 */
public class ModelSynchronizer implements ComponentListener, PropertyChangeListener {
    
    
    private PropertyChangeListener weakModelListener;
    private ComponentListener weakComponentListener;
    private Model currentModel;
    private SynchronisationListener listener;
    
    public ModelSynchronizer(SynchronisationListener listener){
        this.listener = listener;
        
    }
    
    public void subscribe(Model model){
        if (currentModel != model){
            
            unsubscribe();
            
            if (model != null){
                weakModelListener = WeakListeners.propertyChange(this, model);
                
                model.addPropertyChangeListener(weakModelListener);
                
                weakComponentListener = (ComponentListener) WeakListeners.create(
                        ComponentListener.class, this, model);
                
                model.addComponentListener(weakComponentListener);
            }
            currentModel = model;
        }
    }
    public void unsubscribe(){
        if ( currentModel != null){
            currentModel.removePropertyChangeListener(weakModelListener);
            currentModel.removeComponentListener(weakComponentListener);
        }
        
        currentModel = null;
    }
    public void propertyChange(final PropertyChangeEvent evt) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                if(evt.getSource() instanceof Component) {
                    listener.componentUpdated((Component)evt.getSource());
                }
            }
        });
        
    }
    public void childrenAdded(final ComponentEvent componentEvent) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                if(componentEvent.getSource() instanceof Component) {
                    listener.childrenUpdated((Component)componentEvent.getSource());
                }
            }
        });
        
    }
    public void childrenDeleted(final ComponentEvent componentEvent) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                if(componentEvent.getSource() instanceof Component) {
                    listener.childrenUpdated((Component)componentEvent.getSource());
                }
            }
        });
        
    }
    public void valueChanged(final ComponentEvent componentEvent) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                if(componentEvent.getSource() instanceof Component) {
                    listener.childrenUpdated((Component)componentEvent.getSource());
                }
            }
        });
        
    }
}
