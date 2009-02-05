/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.subversion;

import java.io.File;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.netbeans.modules.subversion.client.parser.LocalSubversionException;
import org.netbeans.modules.subversion.client.parser.SvnWcParser;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Lets Hudson understand things about Subversion.
 */
@ServiceProvider(service=HudsonSCM.class, position=100)
public class HudsonSubversionSCM implements HudsonSCM {

    public Configuration forFolder(File folder) {
        try {
            ISVNInfo info = new SvnWcParser().getInfoFromWorkingCopy(folder);
            SVNUrl url = info.getUrl();
            if (url == null) {
                return null;
            }
            final String urlS = url.toString();
            return new Configuration() {
                public void configure(Document doc) {
                    Element root = doc.getDocumentElement();
                    Element configXmlSCM = (Element) root.appendChild(doc.createElement("scm"));
                    configXmlSCM.setAttribute("class", "hudson.scm.SubversionSCM");
                    Element loc = (Element) configXmlSCM.appendChild(doc.createElement("locations")).
                            appendChild(doc.createElement("hudson.scm.SubversionSCM_-ModuleLocation"));
                    loc.appendChild(doc.createElement("remote")).appendChild(doc.createTextNode(urlS));
                    loc.appendChild(doc.createElement("local")).appendChild(doc.createTextNode("."));
                    configXmlSCM.appendChild(doc.createElement("useUpdate")).appendChild(doc.createTextNode("true"));
                    root.appendChild(doc.createElement("triggers")). // XXX reuse existing <triggers> if found
                            appendChild(doc.createElement("hudson.triggers.SCMTrigger")).
                            appendChild(doc.createElement("spec")).
                            // XXX pretty arbitrary but seems like a decent first guess
                            appendChild(doc.createTextNode("@hourly"));
                }
            };
        } catch (LocalSubversionException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

}
