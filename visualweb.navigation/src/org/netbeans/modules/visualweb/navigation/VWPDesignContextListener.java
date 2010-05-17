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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.navigation;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.event.DesignContextListener;
import java.awt.EventQueue;
import java.lang.ref.WeakReference;

/**
 *
 * @author joelle
 */
public class VWPDesignContextListener implements DesignContextListener {


    public VWPDesignContextListener(VWPContentModel vwpContentModel) {
        setVwpContentModel( vwpContentModel );
    }

    public void contextActivated(DesignContext context) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void contextDeactivated(DesignContext context) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void contextChanged(DesignContext context) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void beanCreated(DesignBean designBean) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void beanDeleted(DesignBean designBean) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void beanMoved(DesignBean designBean, DesignBean oldParent, Position pos) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void beanContextActivated(DesignBean designBean) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void beanContextDeactivated(DesignBean designBean) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void instanceNameChanged(DesignBean designBean, String oldInstanceName) {
        /* This is what I need tolisten on. */
        System.out.println("InstanceNameChanged");
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                getVwpContentModel().updateModel();
            }
        });
    }

    public void beanChanged(DesignBean designBean) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void propertyChanged(DesignProperty prop, Object oldValue) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void eventChanged(DesignEvent event) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    private WeakReference<VWPContentModel> refVWPContentModel;
    private   VWPContentModel getVwpContentModel() {
        VWPContentModel vwpContentModel = null;
        if( refVWPContentModel != null ){
            vwpContentModel = refVWPContentModel.get();
        }
        return vwpContentModel;
    }

    private void setVwpContentModel(VWPContentModel vwpContentModel) {
        refVWPContentModel = new WeakReference<VWPContentModel>(vwpContentModel);
    }
}
