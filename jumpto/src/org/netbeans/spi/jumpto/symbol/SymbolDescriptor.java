/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.spi.jumpto.symbol;

import javax.swing.Icon;
import org.openide.filesystems.FileObject;

/**
 * A SymbolDescriptor describes a symbol for display in the Go To Symbol dialog.
 * @since 1.7
 * @author Tomas Zezula
 */
public abstract class SymbolDescriptor {
    
    /** 
     * Return an icon that should be shown for this symbol descriptor. The icon
     * should give a visual indication of the type of match, e.g. method versus
     * field.  A default icon will be supplied if this method returns null.
     * 
     * @return An Icon to be shown on the left hand side with the type entry
     */
    public abstract Icon getIcon();
    
    /**
     * Returns symbol display name
     * @return the symbol display name
     */
    public abstract String getSymbolName();
    
    /**
     * Returns display name of the owner in which the symbol is declared.
     * @return the owner display name
     */
    public abstract String getOwnerName();
        
    /**
     * Return the display name of the project owning the file containing the
     * symbol declaration.
     * 
     * @return The display name of the project owning the file containing the
     * symbol declaration.
     */
    public abstract String getProjectName();
         
    /**
     * Return an icon that is applicable for the project owning the file containing the
     * symbol declaration.
     * Generally, this should be the same as the project icon.  This method will only
     * be called if {@link #getProjectName} returned a non-null value.
     * 
     * @return A project icon corresponding to the project owning the file containing the
     * symbol declaration.
     */
    public abstract Icon getProjectIcon();
    
    /**
     * Return a FileObject for this symbol.
     * This will only be called when the dialog is opening the type or when
     * the user selects the file, so it does not have to be as fast as the other
     * descriptor attributes.
     * 
     * @return The file object where the declared symbol
     */
    public abstract FileObject getFileObject();
    
    /**
     * Return the document offset corresponding to the symbol.
     * This will only be called when the dialog is opening the symbol, so
     * does not have to be as fast as the other descriptor attributes.
     * 
     * @return The document offset of the type declaration in the declaration file
     */
    public abstract int getOffset();
    
    /**
     * Open the type declaration in the editor. 
     */
    public abstract void open();

}
