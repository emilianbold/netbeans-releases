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
import org.netbeans.modules.javacard.project.JCProjectType;
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
    public static final String DEP = "dependency"; //NOI18N
    public static final String DEPS = "dependencies"; //NOI18N
    public static final String KIND = "kind"; //NOI18N
    public static final String DEPLOYMENT_STRATEGY = "deployment"; //NOI18N
    public static final String ID = "id"; //NOI18N
    
    private final Dependencies deps = new Dependencies();

    private DependenciesParser () {
    }

    static Dependencies parse (InputSource in) throws SAXException, ParserConfigurationException, IOException {
        DependenciesParser p = new DependenciesParser();
        SAXParserFactory.newInstance().newSAXParser().parse(in, p);
        return p.deps;
    }

    static Dependencies parse (Element cfgRoot) throws IOException {
        NodeList l = cfgRoot.getElementsByTagNameNS(JCProjectType.PROJECT_CONFIGURATION_NAMESPACE, DEPS);
        if (l.getLength() == 0) {
            throw new IOException ("No dependencies section in project configuration"); //NOI18N
        }
        if (l.getLength() > 1) {
            throw new IOException ("Multiple dependencies sections in project configuration"); //NOI18N
        }
        Dependencies deps = new Dependencies();
        NodeList nl = ((Element) l.item(0)).getElementsByTagName(DEP);
        int len = nl.getLength();
        for (int i=0; i < len; i++) {
            Element depElement = (Element) nl.item(i);
            String id = depElement.getAttribute(ID);
            if (id == null) {
                throw new IOException ("Missing ID for dependency element " + depElement); //NOI18N
            }
            String strategy = depElement.getAttribute(DEPLOYMENT_STRATEGY);
            DeploymentStrategy strat = DeploymentStrategy.valueOf(strategy);
            if (strat == null) {
                throw new IOException ("Missing Deployment Strategy for " + depElement); //NOI18N
            }
            String k = depElement.getAttribute(KIND);
            if (k == null) {
                throw new IOException ("Missing dependency kind for " + depElement); //NOI18N
            }
            DependencyKind kind = DependencyKind.valueOf(k);
            Dependency dep = new Dependency(id, kind, strat);
            deps.add (dep);
        }
        return deps;
    }

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
            throw new SAXException("Nested dependencies not supported"); //NOI18N
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
    }

    private Dependency parseDependency(Attributes a) throws SAXException {
        String k = a.getValue(KIND);
        if (k == null) {
            throw new SAXException(KIND + " missing from dependency attributes " + atts2s(a)); //NOI18N
        }
        DependencyKind kind = DependencyKind.parse(k);
        String depStrategy = a.getValue(DEPLOYMENT_STRATEGY);
        if (depStrategy == null) {
            throw new SAXException (DEPLOYMENT_STRATEGY + " missing from dependency attributes " + atts2s(a)); //NOI18N
        }
        DeploymentStrategy strategy = DeploymentStrategy.valueOf(depStrategy);
        String id = a.getValue(ID);
        if (id == null) {
            throw new SAXException (ID + " missing from dependency attributes " + atts2s(a)); //NOI18N
        }
        return new Dependency(id, kind, strategy);
    }

    private String atts2s(Attributes a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.getLength(); i++) {
            String qName = a.getQName(i);
            sb.append (qName);
            sb.append('='); //NOI18N
            sb.append('"'); //NOI18N
            String val = a.getValue(qName);
            sb.append (val);
            sb.append('"'); //NOI18N
            sb.append(" "); //NOI18N
        }
        return sb.toString();
    }
}
