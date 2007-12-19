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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.rt.providers.impl.local;

import java.util.Properties;
import javax.swing.JPanel;
import org.netbeans.modules.php.rt.providers.impl.DefaultServerCustomizer;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.netbeans.modules.php.rt.providers.impl.ServerCustomizerComponent;

/**
 *
 * @author avk
 */
public class LocalServerFileCustomizerComponent extends LocalServerFilePanelVisual  
        implements ServerCustomizerComponent  
{

    public LocalServerFileCustomizerComponent(LocalHostImpl host, DefaultServerCustomizer parentDialog) {
        super();
        myHost = host;
        myParentDialog = parentDialog;

        initListeners();
    }

    public void readValues(Properties properties) {
        Object obj = properties.get(HOST);
        if (obj != null && obj instanceof LocalHostImpl){
            LocalHostImpl impl = (LocalHostImpl)obj;
            
            String documentRoot = (String) impl.getProperty(LocalHostImpl.DOCUMENT_PATH);
            getDocumentRoot().setText(documentRoot);

            doContentValidation();
        }
    }

    public void storeValues(Properties properties) {
        Object obj = properties.get(HOST);
        if (obj != null && obj instanceof LocalHostImpl){
            LocalHostImpl impl = (LocalHostImpl)obj;
            
            impl.setProperty(LocalHostImpl.DOCUMENT_PATH, getDocumentRoot().getText());
            
            properties.put(HOST, impl);
        }
    }

    public JPanel getPanel() {
        return this;
    }

    @Override
    public void stateChanged() {
        getParentDialog().stateChanged();
    }

    protected void setDefaults() {
        // do nothing
    }
    
    @Override
    protected void setErrorMessage(String message) {
        getParentDialog().setErrorMessage(message);
    }

    protected LocalHostImpl getHost() {
        return myHost;
    }

    private DefaultServerCustomizer getParentDialog() {
        return myParentDialog;
    }

    private DefaultServerCustomizer myParentDialog;
    LocalHostImpl myHost;
}