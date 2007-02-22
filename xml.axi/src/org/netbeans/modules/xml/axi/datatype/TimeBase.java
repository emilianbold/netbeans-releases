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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.axi.datatype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.axi.datatype.Datatype.Facet;
import org.netbeans.modules.xml.schema.model.Whitespace;

/**
 * This class represents Base for all time types.
 *
 * @author Ayub Khan
 */
public abstract class TimeBase extends Datatype {
    
    static List<Facet> applicableFacets;
    
    private Datatype.Kind kind;
    
    protected List<String> patterns;
    
    protected List<String> enumerations;
    
    protected List<Whitespace.Treatment> whitespaces;
    
    protected List<String> maxInclusives;
    
    protected List<String> maxExclusives;
    
    protected List<String> minInclusives;
    
    protected List<String> minExclusives;
    
    private boolean hasFacets;
    
    private boolean isList;
    
    /** Creates a new instance of StringBase */
    public TimeBase(Kind kind) {
        this.kind = kind;
    }
    
    public Kind getKind() {
        return kind;
    }
    
    public synchronized List<Facet> getApplicableFacets() {
        if(applicableFacets == null) {
            List<Facet> facets = new ArrayList<Facet>();
            facets.add(Facet.PATTERN);
            facets.add(Facet.ENUMERATION);
            facets.add(Facet.WHITESPACE);
            facets.add(Facet.MAXINCLUSIVE);
            facets.add(Facet.MAXEXCLUSIVE);
            facets.add(Facet.MININCLUSIVE);
            facets.add(Facet.MINEXCLUSIVE);
            applicableFacets = Collections.unmodifiableList(facets);
        }
        return applicableFacets;
    }
    
    public boolean hasFacets() {
        return hasFacets;
    }
    
    public boolean isList() {
        return isList;
    }
    
    public void setIsList(boolean isList) {
        this.isList = isList;
    }
    
    /*
     * returns pattern value (this corresponds to the value of pattern facet in schema)
     *
     * @return pattern
     */
    public List<String> getPatterns() {
        return patterns;
    }
    
    /*
     * returns enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @return enumeration
     */
    public List<String> getEnumerations() {
        return enumerations;
    }
    
    /*
     * returns whitespace value (this corresponds to the value of whitespace facet in schema)
     *
     * @return whitespaces
     */
    public List<Whitespace.Treatment> getWhiteSpaces() {
        return whitespaces;
    }
    
    /*
     * returns maximum Inclusive value (this corresponds to the value of maxInclusive facet in schema)
     *
     * @return maxInclusive
     */
    public List<String> getMaxInclusives() {
        return maxInclusives;
    }
    
    /*
     * returns maximum Exclusive value (this corresponds to the value of maxExclusive facet in schema)
     *
     * @return maxExclusive
     */
    public List<String> getMaxExclusives() {
        return maxExclusives;
    }
    
    /*
     * returns minimum Inclusive value (this corresponds to the value of minInclusive facet in schema)
     *
     * @return minInclusive
     */
    public List<String> getMinInclusives() {
        return minInclusives;
    }
    
    /*
     * returns minExclusive value (this corresponds to the value of minExclusive facet in schema)
     *
     * @return minExclusive
     */
    public List<String> getMinExclusives() {
        return minExclusives;
    }
    
    /*
     * set pattern value (this corresponds to the value of pattern facet in schema)
     *
     * @param pattern
     */
    public void addPattern(String pattern) {
        if(patterns == null) {
            patterns = new ArrayList<String>(1);
            hasFacets = true;
        }
        this.patterns.add(pattern);
    }
    
    /*
     * set enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @param enumeration
     */
    public void addEnumeration(String enumeration) {
        if(enumerations == null) {
            enumerations = new ArrayList<String>(1);
            hasFacets = true;
        }
        this.enumerations.add(enumeration);
    }
    
    /*
     * set whitespace value (this corresponds to the value of whitespace facet in schema)
     *
     * @param whitespace
     */
    public void addWhitespace(Whitespace.Treatment whitespace) {
        if(whitespaces == null) {
            whitespaces = new ArrayList<Whitespace.Treatment>(1);
            hasFacets = true;
        }
        this.whitespaces.add(whitespace);
    }
    
    /*
     * set maximum Inclusive value (this corresponds to the value of maxInclusive facet in schema)
     *
     * @param maxInclusive
     */
    public void addMaxInclusive(String maxInclusive) {
        if(maxInclusives == null) {
            maxInclusives = new ArrayList<String>(1);
            hasFacets = true;
        }
        this.maxInclusives.add(maxInclusive);
    }
    
    /*
     * set maximum Exclusive value (this corresponds to the value of maxExclusive facet in schema)
     *
     * @param maxExclusive
     */
    public void addMaxExclusive(String maxExclusive) {
        if(maxExclusives == null) {
            maxExclusives = new ArrayList<String>(1);
            hasFacets = true;
        }
        this.maxExclusives.add(maxExclusive);
    }
    
    /*
     * set minimum Inclusive value (this corresponds to the value of minInclusive facet in schema)
     *
     * @param minInclusive
     */
    public void addMinInclusive(String minInclusive) {
        if(minInclusives == null) {
            minInclusives = new ArrayList<String>(1);
            hasFacets = true;
        }
        this.minInclusives.add(minInclusive);
    }
    
    /*
     * set minExclusive value (this corresponds to the value of minExclusive facet in schema)
     *
     * @param minExclusive
     */
    public void addMinExclusive(String minExclusive) {
        if(minExclusives == null) {
            minExclusives = new ArrayList<String>(1);
            hasFacets = true;
        }
        this.minExclusives.add(minExclusive);
    }
    
    /*
     * set pattern value (this corresponds to the value of pattern facet in schema)
     *
     * @param pattern
     */
    public void removePattern(String pattern) {
        if(patterns != null)
            patterns.remove(pattern);
    }
    
    /*
     * set enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @param enumeration
     */
    public void removeEnumeration(String enumeration) {
        if(enumerations != null)
            enumerations.remove(enumeration);
    }
    
    /*
     * set whitespace value (this corresponds to the value of whitespace facet in schema)
     *
     * @param whitespace
     */
    public void removeWhitespace(Whitespace.Treatment whitespace) {
        if(whitespaces != null)
            whitespaces.remove(whitespace);
    }
    
    /*
     * set maximum Inclusive value (this corresponds to the value of maxInclusive facet in schema)
     *
     * @param maxInclusive
     */
    public void removeMaxInclusive(String maxInclusive) {
        if(maxInclusives != null)
            maxInclusives.remove(maxInclusive);
    }
    
    /*
     * set maximum Exclusive value (this corresponds to the value of maxExclusive facet in schema)
     *
     * @param maxExclusive
     */
    public void removeMaxExclusive(String maxExclusive) {
        if(maxExclusives != null)
            maxExclusives.remove(maxExclusive);
    }
    
    /*
     * set minimum Inclusive value (this corresponds to the value of minInclusive facet in schema)
     *
     * @param minInclusive
     */
    public void removeMinInclusive(String minInclusive) {
        if(minInclusives != null)
            minInclusives.remove(minInclusive);
    }
    
    /*
     * set minExclusive value (this corresponds to the value of minExclusive facet in schema)
     *
     * @param minExclusive
     */
    public void removeMinExclusive(String minExclusive) {
        if(minExclusives != null)
            minExclusives.remove(minExclusive);
    }
}
