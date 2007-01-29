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
package org.netbeans.modules.visualweb.xhtml;


/**
 * <b>IFramesetFramesetChoiceChoice</b> is generated from xhtml.rng by Relaxer.
 * Concrete classes of the interface are Frame, FramesetFrameset and Body.
 *
 * @version xhtml.rng (Tue Apr 20 01:31:09 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public interface IFramesetFramesetChoiceChoice {
    /**
     * @return String
     */
    String getId();

    /**
     * @param id
     */
    void setId(String id);

    /**
     * @return String[]
     */
    String[] getClassValue();

    /**
     * @param classValue
     */
    void setClassValue(String[] classValue);

    /**
     * @param classValue
     */
    void setClassValue(String classValue);

    /**
     * @param classValue
     */
    void addClassValue(String classValue);

    /**
     * @param classValue
     */
    void addClassValue(String[] classValue);

    /**
     * @return int
     */
    int sizeClassValue();

    /**
     * @param index
     * @return String
     */
    String getClassValue(int index);

    /**
     * @param index
     * @param classValue
     */
    void setClassValue(int index, String classValue);

    /**
     * @param index
     * @param classValue
     */
    void addClassValue(int index, String classValue);

    /**
     * @param index
     */
    void removeClassValue(int index);

    /**
     * @param classValue
     */
    void removeClassValue(String classValue);

    /**
     */
    void clearClassValue();

    /**
     * @return String
     */
    String getTitle();

    /**
     * @param title
     */
    void setTitle(String title);

    /**
     * @return String
     */
    String getStyle();

    /**
     * @param style
     */
    void setStyle(String style);
}
