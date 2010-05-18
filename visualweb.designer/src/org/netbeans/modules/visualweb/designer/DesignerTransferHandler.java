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


package org.netbeans.modules.visualweb.designer;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.TransferHandler;


/**
 * TransferHandler for the designer component.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (the original code from old DndHandler)
 */
class DesignerTransferHandler extends TransferHandler {


    private final WebForm webForm;


    /** Creates a new instance of DesignerTransferHandler */
    public DesignerTransferHandler(WebForm webForm) {
        this.webForm = webForm;
    }


    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
//        //the following assert is changed by an if statement
//        //assert comp == webform.getPane();
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".canImport(JComponent, DataFlavor[])");
//        }
//        if(comp != webForm.getPane()) {
//            throw(new IllegalArgumentException("Wrong component.")); // NOI18N
//        }
//        if(transferFlavors == null) {
//            throw(new IllegalArgumentException("Transferable has not flavors.")); // NOI18N
//        }

        // XXX
        return webForm.canImport(comp, transferFlavors, null);
    }
    
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors, Transferable transferable) {
        return webForm.canImport(comp, transferFlavors, transferable);
    }

    public boolean importData(JComponent comp, Transferable t) {
        return webForm.getPane().getDndHandler().importData(comp, t);
    }

}
