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

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import org.dom4j.Element;
import org.dom4j.Node;


public interface IVersionableElement {
    /**
     * Sets the versionable element (aggregator) which delegates to this.
     * This is important because, when events are fired by this versionable
     * element, the element being modified must be correctly represented as the
     * aggregating element.
     * 
     * As an example, StructuralFeature aggregates TypedElement. When the 
     * StructuralFeature's type is changed, it delegates to 
     * TypedElement.setType(), which fires a type change event. The versionable
     * element that is passed to the event dispatcher has to be the 
     * StructuralFeature itself, not the aggregated TypedElement, so the 
     * StructuralFeature must call setAggregator on the TypedElement on 
     * construction.
     *
     * @param aggregator The aggregating class instance (StructuralFeature in
     *                    the example).
     */
    public void setAggregator(IVersionableElement aggregator);

//   Retrieves the XML node associated with this element.
//HRESULT Node([out, retval] IXMLDOMNode* *pVal);
  public Node getNode();
  
  public org.dom4j.Node getDOM4JNode();

// Sets the XML node associated with this element.
//HRESULT Node([in] IXMLDOMNode* newVal);
  public void setNode(Node n);

  public void setDom4JNode(org.dom4j.Node n);

// Initializes the internal XML node.
//HRESULT PrepareNode( [ in, defaultvalue(0)] IXMLDOMNode* parentNode );
  public void prepareNode(Node node);

// Set to indicate that the element implementing this interface needs to be extracted to its own file. This property is temporary until the element is saved.
//HRESULT MarkForExtraction([out, retval] VARIANT_BOOL* pVal);
  public boolean isMarkForExtraction();

// Set to indicate that the element implementing this interface needs to be extracted to its own file. This property is temporary until the element is saved.
//HRESULT MarkForExtraction([in] VARIANT_BOOL newVal);
  public void setMarkForExtraction(boolean b);


// Sets or clears the dirty flag of this element.
//HRESULT Dirty([in] VARIANT_BOOL newVal);
  public void setDirty(boolean b);

// Retrieves the dirty status of this element.
//HRESULT Dirty([out, retval] VARIANT_BOOL* dirty );
  public boolean isDirty();

// Retrieves the XMI ID of this element.
//HRESULT XMIID([out, retval] BSTR* pVal);
  public String getXMIID();

// Sets the XMI ID of this element.  Use with EXTREEM Caution!
//HRESULT XMIID([in] BSTR newVal);
  public void setXMIID(String str);

// Determines whether or not this element encapsulates the same data as the passed in element.
//HRESULT IsSame( [ in ] IVersionableElement* element, [ out, retval ]VARIANT_BOOL* result );
  public boolean isSame(IVersionableElement elem);

// Deletes this element. All references to the element will be removed.
//HRESULT Delete();
  public void delete();

// Determines whether or not this element has been deleted.
//HRESULT IsDeleted( [out, retval] VARIANT_BOOL* isDeleted );
  public boolean isDeleted();

// Deletes this element if no other element references it.
//HRESULT SafeDelete( [out,retval] VARIANT_BOOL* wasDeleted );
  public boolean safeDelete();

// The name of the file that this element is versioned in. If this element has not been versioned, an empty string is returned.
//HRESULT VersionedFileName([out, retval] BSTR* pVal);
  public String getVersionedFileName();

// The name of the file that this element is versioned in. If the element has not been versioned, this value is ignored.
//HRESULT VersionedFileName([in] BSTR newVal);
  public void setVersionedFileName(String str);

// Determines whether or not this element has been versioned.
//HRESULT IsVersioned( [out,retval] VARIANT_BOOL* bIsVersioned );
  public boolean isVersioned();

// Saves this element if it has been versioned.
//HRESULT SaveIfVersioned( [out, retval] VARIANT_BOOL* bSaved );
  public boolean saveIfVersioned();

// Returns a complete duplicate of this element. The only difference are the ids. Namespace membership is NOT duplicated.
//HRESULT Duplicate( [out, retval] IVersionableElement** dup );
  public IVersionableElement duplicate();

// Retrieves the URI of this element in regards to version control.
//HRESULT VersionedURI( [out, retval] BSTR* uri );
  public String getVersionedURI();

// Removes all remnants of version control from this element.
//HRESULT RemoveVersionInformation();
  public void removeVersionInformation();

// Ensures that this element is properly represented in memory.
//HRESULT VerifyInMemoryStatus( [out, retval] VARIANT_BOOL* wasModified );
  public boolean verifyInMemoryStatus();

// Retrieves the line number associated with this element.
//HRESULT LineNumber([out, retval] long * lineNumber );
  public int getLineNumber();

// Sets the line number associated with this element.
//HRESULT LineNumber([in] long lineNumber );
  public void setLineNumber(int num);

  public Element getElementNode();
  
  public boolean isClone();
   
   public void setIsClone(boolean value);

}