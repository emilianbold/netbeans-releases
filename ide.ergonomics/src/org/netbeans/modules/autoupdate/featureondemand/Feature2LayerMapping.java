/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.autoupdate.featureondemand;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.autoupdate.featureondemand.api.FeatureInfo;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jirka Rechtacek
 */
public class Feature2LayerMapping {
    private Feature2LayerMapping () {}
    
    private static Feature2LayerMapping INSTANCE = new Feature2LayerMapping ();
    
    public static Feature2LayerMapping getInstance () {
        return INSTANCE;
    }
    
    public Collection<URL> getLayerURLs () {
        List<URL> res = new ArrayList<URL>();
        Lookup.Result<FeatureInfo> result = featureTypesLookup().lookupResult(FeatureInfo.class);
        for (FeatureInfo pt2m : result.allInstances ()) {
            URL url = FeatureInfoAccessor.DEFAULT.getDelegateLayer(pt2m);
            if (url != null) {
                res.add(url);
            }
        }
        return res;
    }
    
    public Set<String>getCodeName (URL layer) {
        if (layer == null) {
            return null;
        }
        Lookup.Result<FeatureInfo> result = featureTypesLookup().lookupResult(FeatureInfo.class);
        for (FeatureInfo pt2m : result.allInstances ()) {
            if (layer.equals(FeatureInfoAccessor.DEFAULT.getDelegateLayer(pt2m))) {
                return FeatureInfoAccessor.DEFAULT.getCodeName(pt2m);
            }
        }
        return null;
    }

    private static Lookup featureTypesLookup;
    static synchronized Lookup featureTypesLookup() {
        if (featureTypesLookup != null) {
            return featureTypesLookup;
        }

        String clusters = System.getProperty("netbeans.dirs");
        if (clusters == null) {
            featureTypesLookup = Lookup.EMPTY;
        } else {
            InstanceContent ic = new InstanceContent();
            AbstractLookup l = new AbstractLookup(ic);
            for (String c : clusters.split(File.pathSeparator)) {
                int last = c.lastIndexOf(File.separatorChar);
                String clusterName = c.substring(last + 1);
                String basename = "/org/netbeans/modules/ide/ergonomics/" + clusterName;
                String layerName = basename + "/layer.xml";
                String bundleName = basename + "/Bundle.properties";
                URL layer = Feature2LayerMapping.class.getResource(layerName);
                URL bundle = Feature2LayerMapping.class.getResource(bundleName);
                if (layer != null && bundle != null) {
                    try {
                        Properties p = new Properties();
                        p.load(bundle.openStream());
                        String cnbs = p.getProperty("cnbs");
                        assert cnbs != null;
                        TreeSet<String> s = new TreeSet<String>();
                        s.addAll(Arrays.asList(cnbs.split(",")));
                        ic.add(FeatureInfo.create(s, layer));
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            featureTypesLookup = l;
        }
        return featureTypesLookup;
    }
}
