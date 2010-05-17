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

package org.netbeans.lib.collab.xmpp.jso.impl.x.muc;

import org.netbeans.lib.collab.xmpp.jso.iface.x.muc.MUCUserQuery;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamObject;
import org.jabberstudio.jso.StreamNode;

import net.outer_planes.jso.ElementNode;
import net.outer_planes.jso.ExtensionNode;

/**
 *
 *
 * @author Rahul Shah
 *
 */
public class MUCUserQueryNode extends ExtensionNode implements MUCUserQuery {

    //"Constants"
    public static final NSI     ELEMNAME_ALT    = new NSI("alt", null);
    public static final NSI     ELEMNAME_PASSWD    = new NSI("password", null);
    public static final NSI     ELEMNAME_STATUS    = new NSI("status", null);
    
    /** Creates a new instance of MUCUserQueryNode */
    public MUCUserQueryNode(StreamDataFactory sdf) {
        super(sdf, NAME);
    }
    
    protected MUCUserQueryNode(StreamElement parent, MUCUserQueryNode base) {
        super(parent, base);
    }
    
    public String getAlt() {
        StreamElement alt = getFirstElement(ELEMNAME_ALT);
        return ((alt != null) ? alt.normalizeTrimText() : null);
    }
    
    public String getPassword() {
        StreamElement pass = getFirstElement(ELEMNAME_PASSWD);
        return ((pass != null) ? pass.normalizeTrimText() : null);
    }
    
    public int getStatus() {
        StreamElement elem = getFirstElement(ELEMNAME_STATUS);
        if (elem == null) return -1;
        String val = elem.normalizeTrimText();
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
            }
        }
        return -1;
    }
    
    public void setAlt(String value) throws IllegalArgumentException {
        StreamElement   alt = getFirstElement(ELEMNAME_ALT);
        if (alt == null)
            alt = addElement(ELEMNAME_ALT);
        alt.addText(value);
    }
    
    public void setPassword(String password) throws IllegalArgumentException {
        StreamElement   pass = getFirstElement(ELEMNAME_PASSWD);
        if (pass == null)
            pass = addElement(ELEMNAME_PASSWD);
        pass.addText(password);
    }
    
    public void setStatus(int n) throws IllegalArgumentException {
        if ((n != 100) || (n != 201) || (n != 301) || (n != 303) || (n != 307)) 
            throw new IllegalArgumentException("status should be in (100,201,301,303,307)");
        StreamElement status = getFirstElement(ELEMNAME_STATUS);
        if (status == null)
            status = addElement(ELEMNAME_STATUS);
        status.addText((new Integer(n)).toString());
    }
    
    public StreamObject copy(StreamElement parent) {
        return new MUCUserQueryNode(parent, this);
    }
}
