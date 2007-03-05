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
package com.sun.rave.web.ui.model;

import javax.faces.model.SelectItem;
import javax.faces.el.MethodBinding;


/**
 * <p>Model bean that represents a selectable choice in a selection
 * component such as <code>Menu</code>, <code>RadioButtonGroup</code>,
 * etc.
 * </p>
 */

public class Option extends SelectItem {

	private String image;
	// Zero is a valid width and height
	//
	private int imageWidth = -1;
	private int imageHeight = -1;
	private String imageAlt;
	private MethodBinding action;
	private String tooltip;

	/**
	 * Create an instance of Selection.
	 */
	public Option() {
	    super();
	}

        /**
	 * Create an instance of Selection.
	 */
	public Option(Object value) {
	    super(value, null);
	}

        /**
	 * Create an instance of Selection.
	 */
	public Option(Object value, String label) {
	    super(value, label);
	}

        /**
	 * Create an instance of Selection.
	 */
	public Option(Object value, String label, String description) {
	    super(value, label, description);
	}

         /**
	 * Create an instance of Selection.
	 */
	public Option(Object value, String label, String description, boolean disabled) {
	    super(value, label, description, disabled);
	}
        
      
	/**
	 * Get the image resource path.
	 */
	public String getImage() {
	    return image;
	}

	/**
	 * Set an image resource path
	 * Used for an image in a radio button for example.
	 */
	public void setImage(String image) {
	    this.image = image;
	}

	/**
	 * Get the image width.
	 */
	public int getImageWidth() {
	    return imageWidth;
	}

	/**
	 * Set an image resource path
	 * Used for an image in a radio button for example.
	 */
	public void setImageWidth(int imageWidth) {
	    this.imageWidth = imageWidth;
	}

	/**
	 * Get the image height.
	 */
	public int getImageHeight() {
	    return imageHeight;
	}

	/**
	 * Set an image resource path
	 * Used for an image in a radio button for example.
	 */
	public void setImageHeight(int imageHeight) {
	    this.imageHeight = imageHeight;
	}

	/**
	 * Get the alternate text for the image.
	 */
	public String getImageAlt() {
	    return imageAlt;
	}

	/**
	 * Set the alternate text for the image.
	 */
	public void setImageAlt(String imageAlt) {
	    this.imageAlt = imageAlt;
	}

	/**
	 * Get the tooltip for this instance.
	 */
	public String getTooltip() {
            if(tooltip == null){
                tooltip = getDescription();
            }
	    return tooltip;
	}

	/**
	 * Set the tooltip for this instance.
	 */
	public void setTooltip(String tooltip) {
	    this.tooltip = tooltip;
	}
}
