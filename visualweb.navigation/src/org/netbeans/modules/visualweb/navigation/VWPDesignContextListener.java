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

/**
 *
 * @author joelle
 */
public class VWPDesignContextListener implements DesignContextListener {

    VWPContentModel vwpContentModel;

    public VWPDesignContextListener(VWPContentModel vwpContentModel) {
        this.vwpContentModel = vwpContentModel;
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
                vwpContentModel.updateModel();
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
}
