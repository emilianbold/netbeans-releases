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

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.awt.EventQueue;

import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.print.spi.PrintProvider;
import org.netbeans.modules.print.spi.PrintProviderCookie;
import org.netbeans.modules.xml.validation.ShowCookie;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie.View;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Implementation of various cookie interfaces, in particular
 * ViewComponentCookie and ShowCookie.
 * The instance of this class is in the WSDLDataObject cookie set.
 * 
 * @author Ajit Bhate
 * @author Nathan Fiedler
 */
public class WSDLMultiViewSupport implements ViewComponentCookie, ShowCookie,
        PrintProviderCookie {
    /** The data object */
    private WSDLDataObject dobj;
    
    /**
     * Constructor
     */
    public WSDLMultiViewSupport(WSDLDataObject dobj) {
        this.dobj = dobj;
    }
    
    public void view(final View view, final Component component,
            final Object... parameters) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    viewInSwingThread(view, component, parameters);
                }
            });
        } else {
            viewInSwingThread(view, component, parameters);
        }
    }

    // see schemamultiviewsupport for implementation
    public void viewInSwingThread(View view, Component component,
            Object... parameters) {
        if (canView(view,component)) {
            WSDLEditorSupport editor = dobj.getWSDLEditorSupport();
            editor.open();
            if (view != null) {
                switch (view) {
                    case SOURCE:
                        WSDLMultiViewFactory.requestMultiviewActive(
                                WSDLSourceMultiviewDesc.PREFERRED_ID);
                        break;
                    case STRUCTURE:
                        WSDLMultiViewFactory.requestMultiviewActive(
                                WSDLTreeViewMultiViewDesc.PREFERRED_ID);
                        break;
                    case DESIGN:
                        WSDLMultiViewFactory.requestMultiviewActive(
                                WSDLDesignMultiViewDesc.PREFERRED_ID);
                        break;
                }
            }
            TopComponent activeTC = TopComponent.getRegistry().getActivated();
            ShowCookie showCookie = (ShowCookie) activeTC.getLookup().lookup(
                    ShowCookie.class);
            ResultItem resultItem = null;
            if (parameters != null && parameters.length != 0) {
                for (Object o : parameters) {
                    if (o instanceof ResultItem) {
                        resultItem = (ResultItem) o;
                        break;
                    }
                }
            }
            if (showCookie != null) {
                if (resultItem == null) {
                    resultItem = new ResultItem(null, null, component, null);
                }
                showCookie.show(resultItem);
            }
        }
    }

    // see schemamultiviewsupport for implementation
    public boolean canView(ViewComponentCookie.View view, Component component) {
        if (view != null && component != null) {
            switch(view) {
                case SOURCE:
                case STRUCTURE:
                case DESIGN:
                case CURRENT:
                case SUPER:
                    return true;
            }
        }
        return false;
    }
        
    public void show(ResultItem resultItem) {
        View view = View.STRUCTURE;
        Component component = resultItem.getComponents();
        if (component == null || component.getModel() == null ||
                component.getModel().getState() == WSDLModel.State.NOT_WELL_FORMED) {
            view(View.SOURCE,component,resultItem);
        } else {
            if (component instanceof DocumentComponent) {
                UIUtilities.annotateSourceView(dobj, (DocumentComponent) component,
                        resultItem.getDescription(), false);
            }

            TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
            MultiViewHandler mvh = MultiViews.findMultiViewHandler(tc);

            if (mvh == null) {
                return;
            }

            MultiViewPerspective mvp = mvh.getSelectedPerspective();
            if (mvp.preferredID().equals(WSDLTreeViewMultiViewDesc.PREFERRED_ID)) {
                view(View.STRUCTURE, component, resultItem);
            } else if (mvp.preferredID().equals(WSDLSourceMultiviewDesc.PREFERRED_ID)) {
                view(View.SOURCE, component, resultItem);
            } else if (mvp.preferredID().equals(WSDLDesignMultiViewDesc.PREFERRED_ID)) {
                view(View.DESIGN, component, resultItem);
            }
        }
    }

    public PrintProvider getPrintProvider() {
        TopComponent component = TopComponent.getRegistry().getActivated();
        Lookup lookup = component.getLookup();
        DataObject dobj = (DataObject) lookup.lookup(DataObject.class);
        if (dobj == this.dobj) {
            PrintProviderCookie cookie = (PrintProviderCookie) lookup.lookup(
                    PrintProviderCookie.class);
            // Avoid looping forever by ensuring we find a provider that
            // is not ourselves.
            if (cookie != null && cookie != this) {
                return cookie.getPrintProvider();
            }
        }
        return null;
    }
}
