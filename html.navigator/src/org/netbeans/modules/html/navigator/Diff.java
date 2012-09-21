/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.navigator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 * An utility class for merging children descriptions.
 *
 * @author marekfukala
 */
public class Diff {
    
    private static final Logger LOGGER = Logger.getLogger(Diff.class.getSimpleName());

    private static final String ID_ATTR_NAME = "id"; //NOI18N

    /**
     * Does nothing! Just returns the given neww collection.
     * @param old
     * @param neww
     * @param sourceNode
     * @return 
     */
    public static Collection<? extends Description> mergeOldAndNew(Collection<? extends Description> old, Collection<? extends Description> neww, HtmlElementNode sourceNode) {
        //todo - report conflict situations and refresh the whole children in such cases
        return neww; 
    }
    
    /**
     * Merges the source and dom children.
     * 
     * What it really does:
     * 1) creates a hashset of {@link DescriptionSetWrapper} 
     * 2) puts all source descriptions wrapped by the {@link DescriptionSetWrapper}
     * 3) puts all dom descriptions wrapped by the {@link DescriptionSetWrapper}
     * 4) unwraps the descriptions and return it as the result.
     * 
     * It does the wrapping to override the default equals and hashCode methods which does
     * take the description class into account (dom description cannot equal source one and vice versa).
     * 
     * @param source source code element description children
     * @param dom dom element description children
     * @param sourceNode explorer node requesting the merge (just for logging purposes)
     * 
     * @return merged set of {@link Description} 
     */
    public static Collection<? extends Description> mergeSourceAndDOM(Collection<? extends Description> source, Collection<? extends Description> dom, HtmlElementNode sourceNode) {
        LOGGER.log(Level.FINE, "Diff.merge(''{0}'')", sourceNode.getDisplayName());

        final Set<DescriptionSetWrapper> wrappers = new LinkedHashSet<DescriptionSetWrapper>();

        StringBuilder sb = new StringBuilder();
        sb.append("Static Keys (");
        sb.append(source.size());
        sb.append("): ");
        
        //static keys have precedence
        for (Description d : source) {
            sb.append(d.toString());
            sb.append(',');
            
            wrappers.add(new DescriptionSetWrapper(d));
        }
        LOGGER.log(Level.FINE, sb.toString());

        sb = new StringBuilder();
        sb.append("DOM Keys (");
        sb.append(dom.size());
        sb.append("): ");
        for (Description d : dom) {
            sb.append(d.toString());
            sb.append(',');
            
            wrappers.add(new DescriptionSetWrapper(d));
        }
        LOGGER.log(Level.FINE, sb.toString());

        sb = new StringBuilder();
        sb.append("Merged Keys (");
        sb.append(wrappers.size());
        sb.append("): ");
        Collection<Description> result = new ArrayList<Description>();
        for (DescriptionSetWrapper w : wrappers) {
            sb.append(w.getPeer().toString());
            result.add(w.getPeer());
            sb.append(',');
        }
        LOGGER.log(Level.FINE, sb.toString());

        return result;
    }

    public static boolean equals(Description d1, Description d2, boolean forceIdAttrEquality) {
        return equals(d1, d2, true, forceIdAttrEquality);
    }

    /**
     * Does equality check for two descriptions. 
     * 
     * @param d1 first Description
     * @param d2 second Description
     * @param checkIndexInParent if true the method also check the position (index) of the child in its parent children
     * @param forceIdAttrEquality if true two descriptions are same if they have same id attribute, regardless the rest of the attributes.
     * @return 
     */
    private static boolean equals(Description d1, Description d2, boolean checkIndexInParent, boolean forceIdAttrEquality) {
        //if name differs, then it's simple
        if (!d1.getName().equals(d2.getName())) {
            return false;
        }
        
        if("html".equalsIgnoreCase(d1.getName())) {
            //there is only one <html> tag in document. They are equals.
            return true;
        }

        //compare by id attribute, so far quite simple
        if(forceIdAttrEquality) {
            String d1IdAttrVal = d1.getAttributeValue(ID_ATTR_NAME);
            String d2IdAttrVal = d2.getAttributeValue(ID_ATTR_NAME);
            if ((d1IdAttrVal != null) && d1IdAttrVal.equals(d2IdAttrVal)) {
                //the id attributes are the same, lets say they are equal
                //xxx what if one changes the id attribute?
                return true;
            }
        }

        //now lets compare by the other attributes
        Map<String, String> d1Attrs = d1.getAttributes();
        Map<String, String> d2Attrs = d2.getAttributes();
        if (d1Attrs.size() != d2Attrs.size()) {
            return false;
        }
        //compare the name=value entries
        Collection<Map.Entry> attrsSet = new HashSet<Map.Entry>();
        //the algorithm expects same collections size!
        attrsSet.addAll(d1Attrs.entrySet());
        attrsSet.removeAll(d2Attrs.entrySet());
        if (!attrsSet.isEmpty()) {
            //they differ
            return false;
        }

        //ok, now we have same name, same attributes w/o id
        //what next?=>compute the "index in similar nodes"
        if (checkIndexInParent && getIndexInParent(d1, forceIdAttrEquality) != getIndexInParent(d2, forceIdAttrEquality)) {
            return false;
        }

        return true;
    }

    /**
     * Computes hashCode for the given {@link Description}
     * 
     * @param d the Description
     * @param forceIdAttrEquality if true two descriptions are same if they have same id attribute, regardless the rest of the attributes.
     * @return 
     */
    public static int hashCode(Description d, boolean forceIdAttrEquality) {
        //follow the equal logic so we fulfil the equals - hashCode contract
        int hash = 11;

        hash = 37 * hash + d.getName().hashCode();
        
        if ("html".equalsIgnoreCase(d.getName())) {
            //there is only one <html> tag in document. They have the same hash code.
            return hash;
        }

        String idAttrVal = d.getAttributeValue(ID_ATTR_NAME);
        if (idAttrVal != null) {
            hash = 37 * hash + idAttrVal.hashCode();
            return hash;
        }

        //now lets compare by the other attributes
        Map<String, String> d1Attrs = d.getAttributes();
        for (Map.Entry<String, String> entry : d1Attrs.entrySet()) {
            String name = entry.getKey();
            hash = 37 * hash + name.hashCode();

            String value = entry.getValue();
            if (value != null) {
                hash = 37 * hash + value.hashCode();
            }
        }

        hash = 37 * hash + getIndexInParent(d, forceIdAttrEquality);

        return hash;
    }

    /**
     * Tries to find index of the description in SIMILAR descriptions, eg.descriptions which equals.
     * 
     */
    static int getIndexInParent(Description d, boolean forceIdAttrEquality) {
        int index = 0;
        Description parent = d.getParent();
        if (parent == null) {
            //nothing to do w/o parent
            //should possibly return something like Integer.MIN_VALUE, but 
            //return 0 in this case to overcome the problem with different parent's of
            //dom node (html has no parent) and the source node (has the root node as parent)
            return index;
        }
        for (Description ch : parent.getChildren()) {
            if (ch == d) {
                //we found THE "D" element, break
                break;
            }
            if (equals(ch, d, false, forceIdAttrEquality)) {
                //we found equal element, but not THE "D" element, increase index
                index++;
            }
        }
        return index;
    }
}
