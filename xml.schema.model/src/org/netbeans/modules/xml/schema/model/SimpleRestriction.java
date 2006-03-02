/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.schema.model;

import java.util.Collection;
import org.netbeans.modules.xml.xam.GlobalReference;

/**
 * This interface includes the common facet handling in restrictions.
 * @author ChrisWebster
 */
public interface SimpleRestriction {
    public static final String ENUMERATION_PROPERTY = "enumeration";
    public static final String PATTERN_PROPERTY = "pattern";
    public static final String MIN_EXCLUSIVE_PROPERTY = "minexclusive";
    public static final String MIN_LENGTH_PROPERTY = "minlength";
    public static final String MAX_LENGTH_PROPERTY  = "maxlength";
    public static final String FRACTION_DIGITS_PROPERTY = "fractiondigits";
    public static final String WHITESPACE_PROPERTY = "whitespace";
    public static final String MAX_INCLUSIVE_PROPERTY = "maxinclusive";
    public static final String TOTAL_DIGITS_PROPERTY = "totaldigits";
    public static final String LENGTH_PROPERTY = "length";
    public static final String MIN_INCLUSIVE_PROPERTY = "mininclusive";
    public static final String MAX_EXCLUSIVE_PROPERTY = "maxexclusive";
    public static final String BASE_PROPERTY = "base";
    public static final String INLINETYPE_PROPERTY  = "inlinetype";
    
    Collection<TotalDigits> getTotalDigits();
    void addTotalDigit(TotalDigits td);
    void removeTotalDigit(TotalDigits td);
    
    Collection<MinExclusive> getMinExclusives();
    void addMinExclusive(MinExclusive me);
    void removeMinExclusive(MinExclusive me);
    
    Collection<MinInclusive> getMinInclusives();
    void addMinInclusive(MinInclusive me);
    void removeMinInclusive(MinInclusive me);
    
    Collection<MinLength> getMinLengths();
    void addMinLength(MinLength ml);
    void removeMinLength(MinLength ml);
    
    Collection<MaxLength> getMaxLengths();
    void addMaxLength(MaxLength ml);
    void removeMaxLength(MaxLength ml);
    
    Collection<Pattern> getPatterns();
    void addPattern(Pattern p);
    void removePattern(Pattern p);
    
    Collection<MaxExclusive> getMaxExclusives();
    void addMaxExclusive(MaxExclusive me);
    void removeMaxExclusive(MaxExclusive me);
    
    Collection<MaxInclusive> getMaxInclusives();
    void addMaxInclusive(MaxInclusive me);
    void removeMaxInclusive(MaxInclusive me);
    
    Collection<Length> getLengths();
    void addLength(Length me);
    void removeLength(Length me);
    
    Collection<Whitespace> getWhitespaces();
    void addWhitespace(Whitespace me);
    void removeWhitespace(Whitespace me);
    
    Collection<FractionDigits> getFractionDigits();
    void addFractionDigits(FractionDigits fd);
    void removeFractionDigits(FractionDigits fd);
    
    Collection<Enumeration> getEnumerations();
    void addEnumeration(Enumeration fd);
    void removeEnumeration(Enumeration fd);
    
    GlobalReference<GlobalSimpleType> getBase();
    void setBase(GlobalReference<GlobalSimpleType> type);
    
    LocalSimpleType getInlineType();
    void setInlineType(LocalSimpleType aSimpleType);
    
}
