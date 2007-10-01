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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.options;

import java.io.IOException;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.XMLDataObject;

/** XML Processor for MIME Options settings files
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public abstract class MIMEProcessor implements XMLDataObject.Processor, InstanceCookie {

    /** <code>XMLDataObject</code> this processor is linked to. */
    protected XMLDataObject xmlDataObject;

    /** Attaches this processor to specified xml data object. Implements <code>XMLDataObject.Processor</code> interface.
     * @param xmlDataObject xml data object to which attach this processor */
    public void attachTo(XMLDataObject xmlDataObject) {
        this.xmlDataObject = xmlDataObject;
    }
    
    /** Gets name of instance. Implements <code>InstanceCookie</code> interface method.
     * @return name of <code>xmlDataObject</code> */
    public String instanceName() {
        return xmlDataObject.getName();
    }
    
    /** Gets instance class. Implements <code>InstanceCookie</code> interface method.
     * @return MIME specific processor class */
    public Class instanceClass() throws IOException, ClassNotFoundException {
        return this.getClass();
    }
    
    /** Gets XMLDataObject to which this processor is linked to */
    public XMLDataObject getXMLDataObject(){
        return xmlDataObject;
    }
    
    /** Gets DTD's PUBLIC_ID */
    public abstract String getPublicID();
    
    /** Gets DTD's SYSTEM_ID */
    public abstract String getSystemID();
    
    /** Gets the class of MIMEOption file that handle this XML file type */
    public abstract Class getAsociatedMIMEOptionFile();
    
    /** Creates appropriate MIME Option file
     * @param o BaseOptions subClass
     * @param b object of MIMEProcessor */
    public abstract MIMEOptionFile createMIMEOptionFile(BaseOptions o, Object b);
    
    /** Creates instance. Implements <code>InstanceCookie</code> interface method. */
    public Object instanceCreate() throws IOException, ClassNotFoundException {
        return this;
    }
}
