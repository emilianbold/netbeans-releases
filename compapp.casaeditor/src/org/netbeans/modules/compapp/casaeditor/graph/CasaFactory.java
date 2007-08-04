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

/*
 * CasaFactory.java
 *
 * Created on January 26, 2007, 2:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.graph;

import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.modules.compapp.casaeditor.graph.actions.CycleCasaSceneSelectProvider;
import org.netbeans.modules.compapp.casaeditor.palette.CasaAcceptProvider;
import org.netbeans.modules.compapp.casaeditor.palette.CasaPaletteAcceptAction;

/**
 *
 * @author rdara
 */
public class CasaFactory {
    
    private static CasaCustomizer msCasaCustomizer = new CasaCustomizer();
    
    private static CasaCustomizerRegistor msCasaCustomizerRegistor = new CasaCustomizerRegistor();

    private static final WidgetAction msCasaCycleCasaSceneSelectAction = ActionFactory.createCycleFocusAction (new CycleCasaSceneSelectProvider());

    /** Creates a new instance of CasaFactory */
    public CasaFactory() {
    }
    
    public static CasaCustomizer getCasaCustomizer() {
        return msCasaCustomizer;
    }
    
    /**
     * Creates a accept action with a specified accept logic provider.
     * @param provider the accept logic provider
     * @return the accept action
     */
    public static WidgetAction createAcceptAction (CasaAcceptProvider provider) {
        assert provider != null;
        return new CasaPaletteAcceptAction (provider);
    }

    public static void registergetCustomizerRegistry(PropertyChangeListener pcl) {
        msCasaCustomizerRegistor.addPropertyListener(pcl);
    }
    
    public static CasaCustomizerRegistor getCasaCustomizerRegistor() {
        return msCasaCustomizerRegistor;
    }
    
    public static WidgetAction createCycleCasaSceneSelectAction() {
        return msCasaCycleCasaSceneSelectAction;
    }
}
