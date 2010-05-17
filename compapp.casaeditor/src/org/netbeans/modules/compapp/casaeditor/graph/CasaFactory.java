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
