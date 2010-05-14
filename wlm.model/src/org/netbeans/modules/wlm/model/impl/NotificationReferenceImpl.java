/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.wlm.model.impl;

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.wlm.model.api.ReferenceableWLMComponent;
import org.netbeans.modules.wlm.model.api.TNotification;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WLMReference;
import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 *
 * @author anjeleevich
 */
public class NotificationReferenceImpl 
        extends AbstractReference<TNotification>
        implements WLMReference<TNotification> 
{
    private boolean isResolved;
    
    public NotificationReferenceImpl(AbstractDocumentComponent parent, 
            String ref) 
    {
        super(TNotification.class, parent, ref);
        isResolved = false;
    }

    public NotificationReferenceImpl(TNotification notification, 
            AbstractDocumentComponent parent) 
    {
        super(TNotification.class, parent, notification.getName());
        setReferenced(notification);
        isResolved = true;
    }

    public TNotification get() {
        TNotification notification = getReferenced();
        
        if (notification != null) {
            return notification;
        }
        
        String ref = getRefString();
        if (ref == null) {
            return null;
        }
        
        ref = ref.trim();
        if ("".equals(ref)) {
            return null;
        }
        
        TTask task = getTask();
        if (task == null) {
            return null;
        }
        
        List<TNotification> notifications = task.getNotifications();
        if (notifications == null) {
            return null;
        }
        
        for (TNotification n : notifications) {
            String name = n.getName();
            if (name == null) {
                continue;
            }

            if (ref.equals(name.trim())) {
                notification = n;
                break;
            }
        }
        
        if (notification != null) {
            setReferenced(notification);
            isResolved = true;
        }
        
        return notification;
    }
    
    private TTask getTask() {
        WLMModel model = (WLMModel) getParent().getModel();
        return (model == null) ? null : model.getTask();
    }

    public boolean isResolved() {
        return isResolved;
    }
}
