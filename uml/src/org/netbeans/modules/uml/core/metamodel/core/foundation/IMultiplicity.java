/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
