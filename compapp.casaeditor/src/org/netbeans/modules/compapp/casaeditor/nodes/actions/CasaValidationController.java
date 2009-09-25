/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.validation.CasaValidationListener;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

/**
 * Listens to the CASA model changes and invokes the validators
 * when appropriate.
 * Clients like the editor, navigator can register a listner with
 * this class to listen to changes to ValidationResults.
 *
 * @author Praveen
 * @author changed by ads
 */
public class CasaValidationController { //extends ChangeEventListenerAdapter {
    
    /**
     * Creates a new instance of CasaValidationController
     */
    public CasaValidationController(CasaModel casaModel) {
        myWeaklisteners = new WeakHashMap<CasaValidationListener, Object>();
        myCasaModel = casaModel;
    }
    
    /**
     *  Add a validation listener.
     *  Listeners are maintained as weaklisteners to clients should maintain
     *  a strong reference to the listener.
     *
     */
    public void addValidationListener(CasaValidationListener listener) {
        synchronized(myWeaklisteners) {
            myWeaklisteners.put(listener, null);
        }
    }
    
    /**
     * Remove a validation listener. Although listners are maintained as
     * weak listeners, clients can explicity unregister a listener.
     */
    public void removeValidationListener(CasaValidationListener listener) {
        synchronized(myWeaklisteners) {
            myWeaklisteners.remove(listener);
        }
    }
        
    public void notifyCompleteValidationResults(List<ResultItem> results) {
        //System.out.println(" Complete validation results obtained.");
        // Filter to keep only slow validator results here.
        myLatestValidationResult = new ArrayList<ResultItem>();
        myLatestValidationResult.addAll(results);
        notifyListeners();
    }
    
    /**
     *  Listeners are notified about change in ValidationResult.
     *  Happens on a non-AWT thread.
     */
    private void notifyListeners() {
        synchronized(myWeaklisteners) {           
            for(CasaValidationListener listener: myWeaklisteners.keySet()) {
                if(listener != null)
                    listener.validationUpdated(myLatestValidationResult);
            }
        }
    }
    
    private Map<CasaValidationListener, Object> myWeaklisteners;
    private CasaModel myCasaModel;
    
    private List<ResultItem> myLatestValidationResult;
    
}
