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
package org.netbeans.modules.ruby.rubyproject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.platform.gems.Gem;
import org.netbeans.modules.ruby.platform.gems.GemFilesParser;
import org.netbeans.modules.ruby.platform.gems.Gems;
import org.netbeans.modules.ruby.rubyproject.GemRequirement.Status;
import org.openide.util.Parameters;

/**
 * Helper class for dealing with the explicit gem requirements of
 * a Ruby or Rails application. Contains info on the gem requirements, i.e. the gems
 * and their versions that the application requires, and the gems and their versions
 * that have actually been indexed.
 *
 * @author Erno Mononen
 */
public final class RequiredGems  {

    /**
     * The project property for required gems.
     */
    public static final String REQUIRED_GEMS_PROPERTY = "required.gems"; //NOI18N
    /**
     * The project property for the gems required in tests.
     */
    public static final String REQUIRED_GEMS_TESTS_PROPERTY = "required.gems.tests"; //NOI18N

    /** @GuardedBy("this") */
    private List<GemRequirement> requirements;
    /** @GuardedBy("this") */
    private final List<URL> indexedGems = new ArrayList<URL>();

    private final boolean forTests;

    private RequiredGems(boolean forTests) {
        this.forTests = forTests;
    }

    public static RequiredGems create(RubyBaseProject project) {
        RequiredGems result = new RequiredGems(false);
        result.setRequiredGems(fromString(project.evaluator().getProperty(REQUIRED_GEMS_PROPERTY)));
        return result;
    }

    public static RequiredGems createForTests(RubyBaseProject project) {
        RequiredGems result = new RequiredGems(true);
        result.setRequiredGems(fromString(project.evaluator().getProperty(REQUIRED_GEMS_TESTS_PROPERTY)));
        return result;
    }

    /**
     * Looks up <code>RequiredGems</code> from the given <code>project</code>.
     * 
     * @param project
     * @return an array containing <code>RequiredGems</code>; <code>[0]</code> for sources and
     * <code>[1]</code> for tests.
     */
    public static RequiredGems[] lookup(Project project) {
        Collection<? extends RequiredGems> reqGems = project.getLookup().lookupAll(RequiredGems.class);
        assert reqGems.size() == 2;
        RequiredGems rg = null;
        RequiredGems rgTest = null;
        for (RequiredGems each : reqGems) {
            if (each.isForTests()) {
                rgTest = each;
            } else {
                rg = each;
            }
        }
        return new RequiredGems[]{rg, rgTest};
    }
    /**
     * @return true if this represents required gems for tests.
     */
    public boolean isForTests() {
        return forTests;
    }

    /**
     * Gets the gem requirements or <code>null</code> if no requirements
     * have been explicitly set.
     * @return
     */
    public synchronized List<GemRequirement> getGemRequirements() {
        if (requirements == null) {
            return null;
        }
        List<GemRequirement> result = mergeVersions(requirements);
        Collections.sort(result);
        return result;
    }

    /**
     * Adds the given requirements.
     * @param gemRequirements
     */
    public void addRequirements(Collection<GemRequirement> gemRequirements) {
        Parameters.notNull("gemRequirements", gemRequirements);
        synchronized (this) {
            if (requirements == null) {
                requirements = new ArrayList<GemRequirement>();
                for (GemIndexingStatus status : getGemIndexingStatuses()) {
                    requirements.add(status.getRequirement());
                }
            } 
            for (GemRequirement each : gemRequirements) {
                if (!requirements.contains(each)) {
                    requirements.add(each);
                }
            }
        }
    }

    public static String asString(List<GemRequirement> requirements) {
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

    /**
     * Sets the required gems. If <code>requirements</code> is <code>null</code>,
     * clears the list of required gems.
     * 
     * @param requirements
     */
    public synchronized void setRequiredGems(List<GemRequirement> requirements) {
        if (requirements == null) {
            this.requirements = null;
        } else {
            this.requirements = new ArrayList<GemRequirement>(requirements);
        }
    }

    /**
     * Sets the required gems. If <code>requirements</code> is <code>null</code>,
     * clears the list of required gems.
     *
     * @param requirements a comma separated list of the requirements.
     */
    public void setRequiredGems(String requirements) {
        setRequiredGems(fromString(requirements));
    }

    public synchronized List<URL> getIndexedGems() {
        return Collections.unmodifiableList(indexedGems);
    }

    public synchronized void setIndexedGems(Collection<URL> gemUrls) {
        Parameters.notNull("gemUrls", gemUrls);
        indexedGems.clear();
        indexedGems.addAll(gemUrls);
    }

    /**
     * Filters out the gems that are not required from the given <code>gemUrls</code>.
     * 
     * @param gemUrls 
     * @return the filtered collection.
     */
    public synchronized Collection<URL> filterNotRequiredGems(Collection<URL> gemUrls) {
        if (requirements == null && forTests) {
            return gemUrls;
        }

        List<URL> result = new ArrayList<URL>();
        for (URL url : gemUrls) {
            String[] nameAndVersion = GemFilesParser.parseNameAndVersion(url);
            if (nameAndVersion != null) {
                String name = nameAndVersion[0];
                String version = nameAndVersion[1];
                // special cases, rails and rake (which are not listed by rake gems)
                if (isRailsOrRake(name)) { //NOI18N
                    result.add(url);
                    continue;
                }
                // filter out testing gems if no requirements are specified
                if (requirements == null && !forTests) {
                    // by default exclude testing gems
                    if (!Gems.isTestingGem(name)) {
                        result.add(url);
                    }
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

    public synchronized List<GemIndexingStatus> getGemIndexingStatuses() {
        // if there are no requirements, just add all the indexed gems
        // this will also init requirements
        boolean addAll = requirements == null;

        // copy since we'll be removing elements
        List<GemRequirement> requirementsCopy = new ArrayList<GemRequirement>();
        if (requirements != null) {
            requirementsCopy.addAll(requirements);
        }

        List<GemIndexingStatus> result = new ArrayList<GemIndexingStatus>();
        for (URL gemUrl : indexedGems) {
            String[] nameAndVersion = GemFilesParser.parseNameAndVersion(gemUrl);
            if (nameAndVersion == null) {
                // a warning msg already logged by GemFilesParser
                continue;
            }
            boolean added = false;
            String name = nameAndVersion[0];
            String version = nameAndVersion[1];
            if (addAll) { //NOI18N
                result.add(new GemIndexingStatus(new GemRequirement(name,
                        null, null, Status.INSTALLED), version));
                added = true;
            } else {
                for (Iterator<GemRequirement> it = requirementsCopy.iterator(); it.hasNext();) {
                    GemRequirement req = it.next();
                    if (req.getName().equals(name)) {
                        result.add(new GemIndexingStatus(req, version));
                        it.remove();
                        added = true;
                        break;
                    }
                }
            }
            // add indexed gems that didn't have a corresponding requirement
            if (!added) {
                result.add(new GemIndexingStatus(new GemRequirement(name, null, null, Status.NOT_INSTALLED), version));
            }
        }
        // add in reqs that didn't have a corresponding installed gem
        if (!addAll) {
            for (GemRequirement req : requirementsCopy) {
                result.add(new GemIndexingStatus(req, null));
            }
        }
        // add in gems that were indexed but don't have a corresponding req (typically rails gems)
        Collections.sort(result, new Comparator<GemIndexingStatus>() {
            public int compare(GemIndexingStatus o1, GemIndexingStatus o2) {
                return o1.getRequirement().compareTo(o2.getRequirement());
            }
        });
        
        return result;
    }

    /**
     * Removes the requirement identified by the given <code>name</code>.
     * 
     * @param name the name of requirement to remove.
     */
    public void removeRequirement(String name) {
        List<GemIndexingStatus> statuses = new ArrayList<GemIndexingStatus>(getGemIndexingStatuses());
        
        synchronized(this) {
         // can't just remove from requirements as it might not be set at all yet
            if (this.requirements == null) {
                List<GemRequirement> newReqs = new ArrayList<GemRequirement>();
                for (GemIndexingStatus each : statuses) {
                    if (!each.getRequirement().getName().equals(name)) {
                        newReqs.add(each.getRequirement());
                    }
                }
                if (newReqs.size() < statuses.size()) {
                    setRequiredGems(newReqs);
                }
            } else {
                for (Iterator<GemRequirement> it = requirements.iterator(); it.hasNext();) {
                    GemRequirement each = it.next();
                    if (each.getName().equals(name)) {
                        it.remove();
                    }
                }
            }
        }
    }

    static List<GemRequirement> fromString(String str) {
        if (str == null) {
            return null;
        }
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

    private static boolean isRailsOrRake(String name) {
        return Gems.isRailsGem(name) || Gems.isRakeGem(name);
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

        return new ArrayList<GemRequirement>(map.values());
    }

    public static final class GemIndexingStatus {

        private final GemRequirement requirement;
        private final String indexedVersion;

        private GemIndexingStatus(GemRequirement requirement, String indexedVersion) {
            this.requirement = requirement;
            this.indexedVersion = indexedVersion;
        }

        public String getIndexedVersion() {
            return indexedVersion;
        }

        public GemRequirement getRequirement() {
            return requirement;
        }
    }
}
