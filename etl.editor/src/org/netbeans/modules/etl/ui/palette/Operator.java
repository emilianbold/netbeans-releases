/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */

package org.netbeans.modules.etl.ui.palette;

import java.awt.Image;
import java.util.Vector;

/**
 *
 * @author nithya
 */
public class Operator {

    private Integer number;
    private String category;
    private String title;
    private String image;
    private String name;

    /** Creates a new instance of Instrument */
    public Operator() {
    }
    
    /**
     * 
     * @return name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * 
     * @param name 
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return number
     */
    public Integer getNumber() {
        return number;
    }

    /**
     * 
     * @param number 
     */
    public void setNumber(Integer number) {
        this.number = number;
    }

    /**
     * 
     * @return category
     */
    public String getCategory() {
        return category;
    }

    /**
     * 
     * @param category 
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 
     * @return image
     */
    public String getImage() {
        return image;
    }

    /**
     * 
     * @param image 
     */
    public void setImage(String image) {
        this.image = image;
    }

}