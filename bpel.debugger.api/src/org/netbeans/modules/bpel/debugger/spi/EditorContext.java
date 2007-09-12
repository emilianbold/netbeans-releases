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

package org.netbeans.modules.bpel.debugger.spi;

import java.beans.PropertyChangeListener;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.api.AnnotationType;
import org.netbeans.modules.bpel.debugger.api.Position;

/**
 * @author Alexander Zgursky
 */
public interface EditorContext {

    /**
     * Opens given file (url) in the editor and navigates to the given position.
     *
     * @param url full path to the source file to show
     * @param xpath xpath of the bpel element to navigate to
     *
     * @return true if succeeded to show the source or false otherwise
     */
    boolean showSource(String url, String xpath);
    
    /**
     * Annotates the bpel element identified by the given xpath in the given
     * file (url) with the given annotation type.
     *
     * @param url full path to the source file to add annotation for
     * @param xpath annotation position
     * @param annotationType annotation type
     *
     * @return a reference to the created annotation object. This object should
     *         be supplied as a parameter to subsequent
     *         {@link #removeAnnotation} call
     */
    Object annotate(String url, String xpath, AnnotationType annotationType);

    /**
     * Removes the given annotation.
     *
     * @param annotation a reference to the annotation object that is returned
     *                   from {@link #annotate} method
     */
    void removeAnnotation(Object annotation);
    
    String getXpath(Object annotation);
    
    QName getProcessQName(String url);
    
    QName getCurrentProcessQName();
    
    /**
     * Returns the more appropriate line number for the
     * given the url and line number.
     */
    int translateBreakpointLine(String url, int lineNumber);
    
    public void addAnnotationListener(Object annotation, PropertyChangeListener l);
    
    public void removeAnnotationListener(Object annotation, PropertyChangeListener l);
}
