/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.mobility.svgcore.composer.prototypes;

import com.sun.perseus.model.Animate;
import com.sun.perseus.model.AnimateTransform;
import com.sun.perseus.model.DocumentNode;
import com.sun.perseus.model.ElementNode;
import com.sun.perseus.util.SVGConstants;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.w3c.dom.DOMException;

/**
 *
 * @author Pavel Benes
 */
public final class PatchedAnimateTransform extends Animate implements PatchedElement {
    private String m_idBackup = null;
    private int    m_type;  

    public PatchedAnimateTransform(final DocumentNode ownerDocument) {
        super(ownerDocument, SVGConstants.SVG_ANIMATE_TRANSFORM_TAG);
        m_type = AnimateTransform.TYPE_TRANSLATE;
    }
    
    public void attachSVGObject(SVGObject obj) {
        
    }

    public SVGObject getSVGObject() {
        return null;
    }
    
    public void setNullId(boolean isNull) {
        if (isNull) {
            m_idBackup = id;
            id       = null;
        } else {
            id = m_idBackup;
        }
    }
    
    private int [] m_path;
    
    public void setPath(int [] path) {
        m_path = path;
    }
    
    public int [] getPath() {
        return m_path;
    }
    
    /**
     * Builds a new Animate element that belongs to the given
     * document. This <code>Animate</code> will belong 
     * to the <code>DocumentNode</code>'s time container.
     *
     * @param ownerDocument the document this node belongs to.
     * @throws IllegalArgumentException if the input ownerDocument is null
     */

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>TimedElementNode</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>TimedElementNode</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new PatchedAnimateTransform(doc);
    }

    /**
     * Supported traits: to, attributeName
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_TYPE_ATTRIBUTE == traitName) {
            return true;
        } else {
            throw new RuntimeException("Not implemented");
            //return super.supportsTrait(traitName);
        }
    }

    /**
     * AnimateTransform supports the type trait.
     *
     * Returns the trait value as String. In SVG Tiny only certain traits can be
     * obtained as a String value. Syntax of the returned String matches the
     * syntax of the corresponding attribute. This element is exactly equivalent
     * to {@link org.w3c.dom.svg.SVGElement#getTraitNS getTraitNS} with
     * namespaceURI set to null.
     *
     * The method is meant to be overridden by derived classes. The 
     * implementation pattern is that derived classes will override the method 
     * and call their super class' implementation. If the ElementNode 
     * implementation is called, it means that the trait is either not supported
     * or that it cannot be seen as a String.
     *
     * @param name the requested trait name.
     * @return the trait value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_TYPE_ATTRIBUTE == name) {
            switch (m_type) {
            case AnimateTransform.TYPE_TRANSLATE:
                return SVGConstants.SVG_TRANSLATE_VALUE;
            case AnimateTransform.TYPE_SCALE:
                return SVGConstants.SVG_SCALE_VALUE;
            case AnimateTransform.TYPE_ROTATE:
                return SVGConstants.SVG_ROTATE_VALUE;
            case AnimateTransform.TYPE_SKEW_X:
                return SVGConstants.SVG_SKEW_X;
            case AnimateTransform.TYPE_SKEW_Y:
            default:
                return SVGConstants.SVG_SKEW_Y;
            }
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * AnimateTransform supports the type trait.
     *
     * @param name the trait's name.
     * @param value the trait's value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a String
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     */
    public void setTraitImpl(final String name, 
                             final String value)
        throws DOMException {
        if (SVGConstants.SVG_TYPE_ATTRIBUTE == name) {
            checkWriteLoading(name);
            if (SVGConstants.SVG_TRANSLATE_VALUE.equals(value)) {
                m_type = AnimateTransform.TYPE_TRANSLATE;
            } else if (SVGConstants.SVG_SCALE_VALUE.equals(value)) {
                m_type = AnimateTransform.TYPE_SCALE;
            } else if (SVGConstants.SVG_ROTATE_VALUE.equals(value)) {
                m_type = AnimateTransform.TYPE_ROTATE;
            } else if (SVGConstants.SVG_SKEW_X.equals(value)) {
                m_type = AnimateTransform.TYPE_SKEW_X;
            } else if (SVGConstants.SVG_SKEW_Y.equals(value)) {
                m_type = AnimateTransform.TYPE_SKEW_Y;
            } else {
                throw new RuntimeException("Not implemented");
                //throw illegalTraitValue(name, value);
            }
        } else {
            super.setTraitImpl(name, value);
        }
    }
}
