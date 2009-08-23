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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.deps;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Tim Boudreau
 */
class DependenciesParser extends DefaultHandler {
    private boolean inDependencies;
    private boolean inDependency;
    public static final String DEP = "dependency";
    public static final String DEPS = "dependencies";
    public static final String KIND = "kind";
    public static final String DEPLOYMENT_STRATEGY = "deployment";
    public static final String ID = "id";
    
    private final Dependencies deps = new Dependencies();

    DependenciesParser () {
    }

    static Dependencies parse (InputSource in) throws SAXException, ParserConfigurationException, IOException {
        DependenciesParser p = new DependenciesParser();
        SAXParserFactory.newInstance().newSAXParser().parse(in, p);
        return p.deps;
    }

    static Dependencies parse (Element el, PropertyEvaluator eval) throws IOException {
        Dependencies deps = new Dependencies();
        NodeList nl = el.getElementsByTagName(DEP);
        int len = nl.getLength();
        for (int i=0; i < len; i++) {
            Element depElement = (Element) nl.item(i);
            String id = depElement.getAttribute(ID);
            if (id == null) {
                throw new IOException ("Missing ID for dependency element " + depElement);
            }
            String strategy = depElement.getAttribute(DEPLOYMENT_STRATEGY);
            DeploymentStrategy strat = DeploymentStrategy.valueOf(strategy);
            if (strat == null) {
                throw new IOException ("Missing Deployment Strategy for " + depElement);
            }
            String k = depElement.getAttribute(KIND);
            if (k == null) {
                throw new IOException ("Missing dependency kind for " + depElement);
            }
            DependencyKind kind = DependencyKind.valueOf(k);
            Dependency dep = new Dependency(id, kind, strat);
            deps.add (dep);
        }
        return deps;
    }

    /*
    static String getDependenciesXML(Dependencies deps) {
        StringBuilder sb = new StringBuilder(100);
        sb.append ("        <dependencies>\n");
        for (Dependency dep : deps.all()) {
            sb.append("            <dependency id=\"");
            sb.append (dep.getID());
            sb.append ("\" ");
            sb.append (DEPLOYMENT_STRATEGY);
            sb.append ('=');
            sb.append ('"');
            sb.append (dep.getDeploymentStrategy().name());
            sb.append ('"');
            sb.append ("/>\n");
        }
        sb.append ("        </dependencies>\n");
        return sb.toString();
    }
     */

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        boolean nowInDependencies = DEPS.equals(qName);
        if (inDependencies != nowInDependencies) {
            inDependencies = nowInDependencies;
            if (inDependencies) {
                return;
            }
        }
        boolean nowInDependency = DEP.equals(qName);
        if (inDependency && nowInDependency) {
            throw new SAXException("Nested dependencies not supported");
        } else {
            inDependency = nowInDependency;
            if (inDependency) {
                Dependency d = parseDependency(attributes);
                deps.add (d);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        boolean endDependencies = DEPS.equals(qName);
        if (endDependencies) {
            inDependencies = false;
            return;
        }
        boolean endDependency = DEP.equals(qName);
        if (endDependency) {
            inDependency = false;
            return;
        }
//        throw new SAXException ("Unrecognized closing element " + qName);
    }

    private Dependency parseDependency(Attributes a) throws SAXException {
        String k = a.getValue(KIND);
        if (k == null) {
            throw new SAXException(KIND + " missing from dependency attributes");
        }
        DependencyKind kind = DependencyKind.parse(k);
        String depStrategy = a.getValue(DEPLOYMENT_STRATEGY);
        if (depStrategy == null) {
            throw new SAXException (DEPLOYMENT_STRATEGY + " missing from dependency attributes");
        }
        DeploymentStrategy strategy = DeploymentStrategy.valueOf(depStrategy);
        String id = a.getValue(ID);
        if (id == null) {
            throw new SAXException (ID + " missing from dependency attributes");
        }
        return new Dependency(id, kind, strategy);
    }
}
