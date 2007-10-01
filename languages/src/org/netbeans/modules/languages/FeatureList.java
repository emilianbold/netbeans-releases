/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.languages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ParserManager.State;

/**
 *
 * @author hanz
 */
class FeatureList {
    
    private Map<String,List<Feature>>   features;
    private Map<String,FeatureList>     lists;
    
    void add (Feature feature) {
        Selector selector = feature.getSelector ();
        FeatureList list = this;
        if (selector != null) {
            List<String> path = selector.getPath ();
            for (int i = path.size () - 1; i >= 0; i--) {
                String name = path.get (i);
                if (list.lists == null)
                    list.lists = new HashMap<String,FeatureList> ();
                FeatureList newList = list.lists.get (name);
                if (newList == null) {
                    newList = new FeatureList ();
                    list.lists.put (name, newList);
                }
                list = newList;
            }
        }
        if (list.features == null)
            list.features = new HashMap<String,List<Feature>> ();
        List<Feature> fs = list.features.get (feature.getFeatureName ());
        if (fs == null) {
            fs = new ArrayList<Feature> ();
            list.features.put (feature.getFeatureName (), fs);
        }
        fs.add (feature);
    }
    
    void importFeatures (FeatureList featureList) {
        
    }
    
    List<Feature> getFeatures (String featureName) {
        if (features == null) return Collections.<Feature>emptyList ();
        List<Feature> result = features.get (featureName);
        if (result != null) return result;
        return Collections.<Feature>emptyList ();
    }
    
    List<Feature> getFeatures (String featureName, String id) {
        if (lists == null) return Collections.<Feature>emptyList ();
        FeatureList list = lists.get (id);
        if (list == null) return Collections.<Feature>emptyList ();
        return list.getFeatures (featureName);
    }
    
    List<Feature> getFeatures (String featureName, ASTPath path) {
        List<Feature> result = null;
        boolean first = true;
        FeatureList list = this;
        for (int i = path.size () - 1; i > 0; i--) {
            ASTItem item = path.get (i);
            String name = item instanceof ASTNode ? ((ASTNode) item).getNT () : ((ASTToken) item).getType ();
            if (list.lists == null) break;
            list = list.lists.get (name);
            if (list == null) break;
            if (list.features == null) continue;
            List<Feature> l = list.features.get (featureName);
            if (l == null) continue;
            if (result == null)
                result = l;
            else
            if (first) {
                List<Feature> newList = new ArrayList<Feature> ();
                newList.addAll (result);
                newList.addAll (l);
                first = false;
            } else
                result.addAll (l);
        }
        if (result == null) return Collections.<Feature>emptyList ();
        return result;
    }
    
    void evaluate (
        State state, 
        List<ASTItem> path, 
        Map<String,Set<ASTEvaluator>> evaluatorsMap                             ,Map<Object,Long> times
    ) {
        FeatureList list = this;
        for (int i = path.size () - 1; i > 0; i--) {
            ASTItem item = path.get (i);
            String name = item instanceof ASTNode ? ((ASTNode) item).getNT () : ((ASTToken) item).getType ();
            if (list.lists == null) return;
            list = list.lists.get (name);
            if (list == null) return;
            if (list.features != null) {
                Iterator<String> it = evaluatorsMap.keySet ().iterator ();
                while (it.hasNext ()) {
                    String featureName = it.next ();
                    if (featureName == null) {
                        Set<ASTEvaluator> evaluators = evaluatorsMap.get (null);
                        Iterator<ASTEvaluator> it2 = evaluators.iterator ();
                        while (it2.hasNext ()) {
                            ASTEvaluator evaluator = it2.next ();
                            Collection<List<Feature>> featureListsC = list.features.values ();
                            Iterator<List<Feature>> it3 = featureListsC.iterator ();
                            while (it3.hasNext ()) {
                                List<Feature> featureList = it3.next ();
                                Iterator<Feature> it4 = featureList.iterator ();
                                while (it4.hasNext ())
                                    evaluator.evaluate (state, path, it4.next ());
                            }
                        }
                    } else {
                        List<Feature> features = list.features.get (featureName);
                        if (features == null) continue;
                        Set<ASTEvaluator> evaluators = evaluatorsMap.get (featureName);

                        Iterator<Feature> it2 = features.iterator ();
                        while (it2.hasNext ()) {
                            Feature feature =  it2.next ();
                            Iterator<ASTEvaluator> it3 = evaluators.iterator ();
                            while (it3.hasNext ()) {
                               ASTEvaluator evaluator = it3.next ();                long time = System.currentTimeMillis ();
                               evaluator.evaluate (state, path, feature);           Long l = times.get (evaluator);time = System.currentTimeMillis () - time; if (l != null) time += l.longValue (); times.put (evaluator, time);
                            }
                        }
                    }
                }
            }
        }
    }
}
