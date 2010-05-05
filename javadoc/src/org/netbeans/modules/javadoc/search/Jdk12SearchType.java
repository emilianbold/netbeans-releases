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

package org.netbeans.modules.javadoc.search;

import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/* Base class providing search for JDK1.2/1.3 documentation
 * @author Petr Hrebejk, Petr Suchomel
 */
// no position since it must be the last service
@ServiceProvider(service=JavadocSearchType.class)
public class Jdk12SearchType extends JavadocSearchType {

    private boolean caseSensitive = true;

    /** generated Serialized Version UID */
    private static final long serialVersionUID = -2453877778724454324L;
    
    /** Getter for property caseSensitive.
     * @return Value of property caseSensitive.
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    /** Setter for property caseSensitive.
     * @param caseSensitive New value of property caseSensitive.
    */
    public void setCaseSensitive(boolean caseSensitive) {
        boolean oldVal = this.caseSensitive;
        this.caseSensitive = caseSensitive;
//        this.firePropertyChange("caseSensitive", oldVal ? Boolean.TRUE : Boolean.FALSE, caseSensitive ? Boolean.TRUE : Boolean.FALSE);   //NOI18N
    }

    public @Override FileObject getDocFileObject(FileObject apidocRoot) {
    
        FileObject fo = apidocRoot.getFileObject( "index-files" ); // NOI18N
        if ( fo != null ) {
            return fo;
        }

        fo = apidocRoot.getFileObject( "index-all.html" ); // NOI18N
        if ( fo != null ) {
            return fo;
        }

        return null;
    }    
    
    /** Returns Java doc search thread for doument
     * @param toFind String to find
     * @param fo File object containing index-files
     * @param diiConsumer consumer for parse events
     * @return IndexSearchThread
     * @see IndexSearchThread
     */    
    public @Override IndexSearchThread getSearchThread(String toFind, FileObject fo, IndexSearchThread.DocIndexItemConsumer diiConsumer) {
        return new SearchThreadJdk12 ( toFind, fo, diiConsumer, isCaseSensitive() );
    }


    public @Override boolean accepts(FileObject apidocRoot, String encoding) {
        //XXX returns always true, must be the last JavadocType
        return true;
    }
    
}
