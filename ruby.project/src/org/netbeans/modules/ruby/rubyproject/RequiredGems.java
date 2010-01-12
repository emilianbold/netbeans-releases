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
package org.netbeans.modules.ruby.rubyproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ruby.platform.gems.Gem;
import org.netbeans.modules.ruby.platform.gems.GemFilesParser;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 * Represents the explicit gem requirements of a Ruby/Rails application.
 *
 * @author Erno Mononen
 */
public final class RequiredGems implements PropertyChangeListener {

    public static final String REQUIRED_GEMS_PROPERTY = "required.gems"; //NOI18N
    private List<GemRequirement> requirements;
    private final RubyBaseProject project;

    public static RequiredGems create(RubyBaseProject project) {
        RequiredGems result = new RequiredGems(project);
        PropertyEvaluator evaluator = project.evaluator();
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(result, evaluator));
        return result;
    }

    private RequiredGems(RubyBaseProject project) {
        this.project = project;
    }

    /**
     * Gets the gem requirements or <code>null</code> if no requirements
     * have been explicitly set.
     * @return
     */
    public synchronized List<GemRequirement> getGemRequirements() {
        if (requirements == null) {
            String required = project.evaluator().getProperty(REQUIRED_GEMS_PROPERTY);
            if (required != null) {
                requirements = fromString(required);
            } else {
                return null;
            }
        }
        List<GemRequirement> result = mergeVersions(requirements);
        Collections.sort(result);
        return result;
    }

    public synchronized String asString() {
        if (requirements == null || requirements.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (Iterator<GemRequirement> it = requirements.iterator(); it.hasNext();) {
            GemRequirement gemRequirement = it.next();
            result.append(gemRequirement.asString());
            if (it.hasNext()) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    private static List<GemRequirement> mergeVersions(List<GemRequirement> requirements) {
        // XXX: performs a very basic version comparison; doesn't take into account the operator etc.
        Map<String, GemRequirement> map = new HashMap<String, GemRequirement>();
        for (GemRequirement each : requirements) {
            GemRequirement existing = map.get(each.getName());
            if (existing != null) {
                if (existing.compareTo(each) < 0) {
                    map.put(each.getName(), each);
                }
            } else {
                map.put(each.getName(), each);
            }
        }

        return new ArrayList(map.values());
    }

    static List<GemRequirement> fromString(String str) {
        String[] gems = str.split(",");
        List<GemRequirement> result = new ArrayList<GemRequirement>();
        for (String gem : gems) {
            GemRequirement requirement = GemRequirement.fromString(gem.trim());
            if (!result.contains(requirement)) {
                result.add(requirement);
            }
        }
        return result;
    }

    /**
     * Sets the required gems. If <code>requirements</code> is <code>null</code>,
     * clears the list of required gems.
     * 
     * @param requirements
     */
    public synchronized void setRequiredGems(List<GemRequirement> requirements) {
        List<GemRequirement> old = this.requirements;
        this.requirements = requirements;

        UpdateHelper helper = project.getUpdateHelper();
        EditableProperties projectProperties = helper.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
        if (requirements == null) {
            projectProperties.remove(REQUIRED_GEMS_PROPERTY);
        } else {
            projectProperties.put(REQUIRED_GEMS_PROPERTY, asString());
        }
        helper.putProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);

        try {
            ProjectManager.getDefault().saveProject(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Filters out the gems that are not required from the given <code>gemUrls</code>.
     * 
     * @param gemUrls 
     * @return the filtered collection.
     */
    public synchronized Collection<URL> filterNotRequiredGems(Collection<URL> gemUrls) {
        if (requirements == null) {
            return gemUrls;
        }

        List<URL> result = new ArrayList<URL>();
        for (URL url : gemUrls) {
            String[] nameAndVersion = GemFilesParser.parseNameAndVersion(Gem.getGemName(url));
            if (nameAndVersion != null) {
                String name = nameAndVersion[0];
                String version = nameAndVersion[1];
                // special cases, rails and rake (which are not listed by rake gems)
                if (Gem.isRailsGem(name) || "rake".equals(name)) { //NOI18N
                    result.add(url);
                    continue;
                }
                for (GemRequirement each : requirements) {
                    if (each.getName().equals(name) && each.satisfiedBy(version)) {
                        result.add(url);
                        break;
                    }
                }
            }
        }
        return result;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (REQUIRED_GEMS_PROPERTY.equals(evt.getPropertyName())) {
            requirements = null;
        }
    }
}
