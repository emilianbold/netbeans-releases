/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.axis2;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Model;
import org.netbeans.modules.websvc.axis2.services.model.ServicesModel;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 *
 * 
 */
@ProjectServiceProvider(service=Axis2ModelProvider.class,
    projectType="org-netbeans-modules-java-j2seproject")
public class Axis2ModelProvider {
    
    public static final String PROP_SERVICES = "services"; //NOI18N
    public static final String PROP_AXIS2 = "axis2"; //NOI18N
    private Axis2Model axis2Model;
    private ServicesModel servicesModel;
    private PropertyChangeSupport propertyChangeSupport;
    
    public Axis2ModelProvider() {
        propertyChangeSupport = new PropertyChangeSupport(this);
    }
    
    public Axis2Model getAxis2Model() {
        return axis2Model;
    }

    public ServicesModel getServicesModel() {
        return servicesModel;
    }

    public void setServicesModel(ServicesModel servicesModel) {
        ServicesModel oldModel = this.servicesModel;      
        this.servicesModel = servicesModel;
        propertyChangeSupport.firePropertyChange(PROP_SERVICES, oldModel, servicesModel);
    }
    
    void setAxis2Model(Axis2Model axis2Model) {
        Axis2Model oldModel = this.axis2Model;      
        this.axis2Model = axis2Model;
        propertyChangeSupport.firePropertyChange(PROP_AXIS2, oldModel, axis2Model);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.addPropertyChangeListener(pcl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.removePropertyChangeListener(pcl);
    }
}
