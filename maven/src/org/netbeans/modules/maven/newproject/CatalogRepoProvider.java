/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.maven.newproject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeProvider;
import org.openide.util.NbCollections;

/**
 * list of archetypes coming from an archetype catalog
 * @author mkleint
 */
public abstract class CatalogRepoProvider implements ArchetypeProvider {

    private static final String EL_ARCHETYPES = "archetypes"; //NOI18N
    private static final String EL_ARCHETYPE = "archetype"; //NOI18N
    private static final String EL_ARTIFACTID = "artifactId"; //NOI18N
    private static final String EL_DESCRIPTION = "description";
    private static final String EL_GROUPID = "groupId"; //NOI18N
    private static final String EL_REPOSITORY = "repository"; //NOI18N
    private static final String EL_VERSION = "version"; //NOI18N

    private static final Logger LOG = Logger.getLogger(CatalogRepoProvider.class.getName());

    protected CatalogRepoProvider() {}

    protected abstract URL file() throws IOException;

    protected String repository() {
        return null;
    }

    public @Override List<Archetype> getArchetypes() {
        List<Archetype> toRet = new ArrayList<Archetype>();
        try {
            URL file = file();
            if (file == null) {
                return toRet;
            }
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(file);
            Element root = doc.getRootElement();
            Namespace ns = root.getNamespace(); // should be http://maven.apache.org/plugins/maven-archetype-plugin/archetype-catalog/1.0.0 but missing in older copies
            Element list = root.getChild(EL_ARCHETYPES, ns);
            if (list != null) {
                for (Element el : NbCollections.checkedListByCopy(list.getChildren(EL_ARCHETYPE, ns), Element.class, true)) {
                    String grId = el.getChildText(EL_GROUPID, ns);
                    String artId = el.getChildText(EL_ARTIFACTID, ns);
                    String ver = el.getChildText(EL_VERSION, ns);
                    String repo = el.getChildText(EL_REPOSITORY, ns);
                    String desc = el.getChildText(EL_DESCRIPTION, ns);
                    Archetype archetype = new Archetype(false);
                    if (grId != null && artId != null && ver != null) {
                        archetype.setArtifactId(artId);
                        archetype.setGroupId(grId);
                        archetype.setVersion(ver);
                        if (repo != null) {
                            archetype.setRepository(repo);
                        } else {
                            archetype.setRepository(repository());
                        }
                        if (desc != null) {
                            archetype.setDescription(desc);
                        }
                        toRet.add(archetype);
                    }
                }
            }
        } catch (IOException exc) {
            LOG.log(Level.INFO, null, exc);
        } catch (JDOMException exc) {
            LOG.log(Level.INFO, null, exc);
        }
        return toRet;
    }
}
