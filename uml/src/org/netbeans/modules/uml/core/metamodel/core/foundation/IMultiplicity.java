/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.core.support.umlutils.ETList;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface IMultiplicity extends IElement {

//   Sets the lower bounds of this multiplicity, so it becomes [LowerBounds..*].  If ranges currently exist they are deleted and a new one is created.
//HRESULT SetRange([in] BSTR sLowerBounds);
  public void setRange(String bounds);

//   Sets the bounds of this multiplicity, so it becomes [LowerBounds..UpperBounds].  If ranges currently exist they are deleted and a new one is created.
//HRESULT SetRange2([in] BSTR sLowerBounds, [in] BSTR sUpperBounds);
  public void setRange2(String lower, String upper);

//   Get the range(s) of this multiplicity according to a well formed string [LowerBounds..Upperbounds, LowerBounds2..Upperbounds2,...].
//HRESULT GetRangeAsString([out] BSTR* sRangeString, [in, defaultvalue(-1)] VARIANT_BOOL bIncludeBrackets);
  public String getRangeAsString(boolean includeBrackets /*=true*/);

//   Adds a MultiplicityRange to this element.
//HRESULT AddRange([in] IMultiplicityRange* range);
  public void addRange(IMultiplicityRange range);

// Inserts a MultiplicityRange to this element.
//HRESULT InsertRange([in] IMultiplicityRange* existingRange, [in] IMultiplicityRange* newRange );
  public void insertRange(IMultiplicityRange existingRange, IMultiplicityRange newRange);

//   Removes a range from this element.
//HRESULT RemoveRange([in] IMultiplicityRange* range);
  public void removeRange(IMultiplicityRange range);

//   Removes all the ranges from the element.
//HRESULT RemoveAllRanges();
  public void removeAllRanges();

//   Retrieves the set of MultiplicityRanges for this element.
//HRESULT Ranges([out, retval] IMultiplicityRanges* *pVal);
  public ETList<IMultiplicityRange> getRanges();

// Sets / Gets the ordered flag. For a multiplicity that permits multiple values, this attribute specifies whether the values are sequentially ordered.
//HRESULT IsOrdered([out, retval] VARIANT_BOOL* pVal);
  public boolean getIsOrdered();

//   Sets / Gets the ordered flag. For a multiplicity that permits multiple values, this attribute specifies whether the values are sequentially ordered.
//HRESULT IsOrdered([in] VARIANT_BOOL newVal);
  public void setIsOrdered(boolean val);

// .
//HRESULT RangeCount([out, retval] long* pVal);
  public long getRangeCount();

// Creates a MultiplicityRange to this element. It will need to be added to the Multiplicity eventually.
//HRESULT CreateRange([out,retval] IMultiplicityRange** pRange );
  public IMultiplicityRange createRange();

//  [hidden, propput, helpstring("Used by the edit control to set the range(s) of this multiplicity according to a well formed string [LowerBounds..Upperbounds, LowerBounds2..Upperbounds2,...].If ranges currently exist they are deleted and a new ones are created.
//HRESULT RangeThroughString([in] BSTR sWellFormedRangeString);
  public void setRangeThroughString(String wellFormedString);

//   Used by the edit control to get the range(s) of this multiplicity according to a well formed string [LowerBounds..Upperbounds, LowerBounds2..Upperbounds2,...].
//HRESULT RangeAsString([out,retval] BSTR* sRangeString);
  public String getRangeAsString();

}