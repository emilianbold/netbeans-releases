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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.midp.components.points;

import org.netbeans.modules.vmd.api.codegen.CodeNamePresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.handlers.ExitMidletEventHandlerCD;
import org.netbeans.modules.vmd.midp.components.sources.MobileDeviceResumeEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.MobileDeviceStartEventSourceCD;
import org.netbeans.modules.vmd.midp.flow.FlowInfoNodePresenter;
import org.netbeans.modules.vmd.midp.flow.FlowMobileDevicePinOrderPresenter;
import org.openide.util.NbBundle;

import java.util.*;

/**
 * @author Karol Harezlak
 */
public final class MobileDeviceCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "#MobileDevice"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/mobile_device_16.png"; // NOI18N
    public static final String LARGE_ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/mobile_device_32.png"; // NOI18N

    public static final String PROP_START = "start"; // NOI18N
    public static final String PROP_RESUME = "resume"; // NOI18N
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(PointCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return  Arrays.asList(
            new PropertyDescriptor(PROP_START, MobileDeviceStartEventSourceCD.TYPEID, PropertyValue.createNull(), false, false, Versionable.FOREVER),
            new PropertyDescriptor(PROP_RESUME, MobileDeviceResumeEventSourceCD.TYPEID, PropertyValue.createNull(), false, false, Versionable.FOREVER)
            );
    }
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // info
            InfoPresenter.createStatic(NbBundle.getMessage (MobileDeviceCD.class, "DISP_MobileDevice"), null, ICON_PATH), // NOI18N
            // flow
            new FlowInfoNodePresenter(),
            new FlowMobileDevicePinOrderPresenter(),
            // general
            ExitMidletEventHandlerCD.createExitPointEventHandlerCreatorPresenter(),
            // code
            CodeNamePresenter.fixed(
            "startApp", "pauseApp", "destroyApp", "notifyDestroyed", "notifyPaused", // NOI18N
            "platformRequest", "resumeRequest", "checkPermission", "getAppProperty", // NOI18N
            "midletPaused"), // NOI18N
            // delete
            DeleteDependencyPresenter.createNullableComponentReferencePresenter(PROP_START),
            DeleteDependencyPresenter.createNullableComponentReferencePresenter(PROP_RESUME),
            //inspector
            InspectorOrderingPresenter.create(new MobileDeviceOrdering())
        );
    }
    
    private class MobileDeviceOrdering implements InspectorOrderingController {
        
        private Comparator<InspectorFolder> comparator;
        
        public MobileDeviceOrdering() {
            comparator = new Comparator<InspectorFolder>() {
                public int compare(InspectorFolder folder1, InspectorFolder folder2) {
                    if (folder1.getTypeID().equals(MobileDeviceStartEventSourceCD.TYPEID))
                        return -1;
                    
                    return 1;
                }
            };
        }
        
        public boolean isTypeIDSupported(DesignDocument document, TypeID typeID) {
            if (MobileDeviceStartEventSourceCD.TYPEID.equals(typeID) || MobileDeviceResumeEventSourceCD.TYPEID.equals(typeID))
                return true;
            
            return false;
        }
        
        public List<InspectorFolder> getOrdered(DesignComponent component, Collection<InspectorFolder> folders) {
            List<InspectorFolder> orderedList = new ArrayList<InspectorFolder>(folders);
            
            Collections.sort(orderedList, comparator);
            
            return orderedList;
        }
        
        public Integer getOrder() {
            return 0;
        }
    }
}
