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

package org.netbeans.modules.soa.mapper.common.basicmapper.methoid;

import java.util.List;

import javax.swing.Icon;

/**
 * <p>
 *
 * Title: IMetoid </p> <p>
 *
 * Description: Generic interface describes the functionalities of a Methoid.
 * IMethoid is the base interface holding the meta data for IMethoidNode to be
 * constructed and added to the mapper model. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IMethoid {

    /**
     * Return the icon repersents this methoid.
     *
     * @return   the icon repersents this methoid.
     */
    public Icon getIcon();

    /**
     * Return the name of this methoid.
     *
     * @return   the name of this methoid.
     */
    public String getName();

    /**
     * Return the text of this tooltip.
     *
     * @return   the text of this tooltip.
     */
    public String getToolTipText();
    
    /**
     * Return the data object of this methoid.
     *
     * @return   the data object of this methoid.
     */
    public Object getData();

    /**
     * Return the methoid field repersents the name space of this methoid.
     *
     * @return   the methoid field repersents the name space of this methoid.
     */
    public IField getNamespace();

    /**
     * Return the input fields of this methoid.
     *
     * @return   the input fields of this methoid.
     */
    public List getInput();

    /**
     * Return the output fields of this methoid.
     *
     * @return   the output fields of this methoid.
     */
    public List getOutput();
    
    /**
     * Return whether this methoid's input fields can increase dynamically.
     * 
     * @return true if the methoid input can grow
     */
    public boolean isAccumulative();
    
    /**
     * Return whether this methoid represents a single literal value.
     * 
     * @return true if the methoid is a literal methoid
     */
    public boolean isLiteral();
    
    public void setIcon(Icon icon);
    
    public void setName(String name);
    
    public void setToolTipText(String text);
    
    public void setData(Object data);
}
