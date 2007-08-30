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

import java.awt.EventQueue;
import java.util.logging.Logger;
import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.ModelSetListener;
import org.netbeans.modules.visualweb.insync.models.FacesModel;

/**
 *
 * @author joelle
 */
public class FacesModelSetListener implements ModelSetListener {

    private static Logger LOGGER = Logger.getLogger(FacesModelSetListener.class.getName());
    private final VWPContentModel vwpContentModel;

    public FacesModelSetListener(VWPContentModel vwpContentModel) {
        this.vwpContentModel = vwpContentModel;
    }

    public void modelAdded(Model model) {
        LOGGER.finest("Model Added()");
        //DO NOTHING
    }

    public void modelChanged(final Model myModel) {
        FacesModel facesModel = vwpContentModel.getFacesModel();
        LOGGER.finest("Model Changed()");
        if ((myModel == facesModel) || (myModel.getFile().getExt().equals("jspf") && vwpContentModel.isKnownFragementModel(facesModel, facesModel.getRootBean(), myModel))) {
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    vwpContentModel.updateModel();
                }
            });
        }
    }

    public void modelRemoved(Model model) {
        LOGGER.finest("Model Removed()");
        //DO NOTHING
    }

    public void modelProjectChanged() {
        LOGGER.finest("Model Project Changed()");
        //DO NOTHING
    }
}
