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

/**
 *
 */
package org.netbeans.modules.bpel.model.api;

/**
 * @author ads
 *         <p>
 *         Java class for tSources complex type.
 *         <p>
 *         The following schema fragment specifies the expected content
 *         contained within this class.
 *
 * <pre>
 *   &lt;complexType name=&quot;tSources&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tExtensibleElements&quot;&gt;
 *         &lt;sequence&gt;
 *           &lt;element name=&quot;source&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tSource&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *         &lt;/sequence&gt;
 *       &lt;/extension&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 */
public interface SourceContainer extends ExtensibleElements {

    /**
     * Returns list of all sources entities.
     * 
     * @return array of sources.
     */
    Source[] getSources();

    /**
     * @param i
     *            position in array.
     * @return <code>i</code>th source entity.
     */
    Source getSource( int i );

    /**
     * Remove i-th Source ( index in Sources set ).
     * 
     * @param i
     *            position in array.
     */
    void removeSource( int i );

    /**
     * Set <code>source</code> to the <code>i</code> place.
     * 
     * @param source
     *            object for set.
     * @param i
     *            position for set.
     */
    void setSource( Source source, int i );

    /**
     * Adds <code>source</code> to the end of sources list.
     * 
     * @param source
     *            object for add.
     */
    void addSource( Source source );

    /**
     * Set new list iof sources for this activity.
     * 
     * @param source
     *            array for set.
     */
    void setSources( Source[] source );

    /**
     * Insert <code>source</code> to the <code>i</code>th position.
     * 
     * @param source
     *            object for insert.
     * @param i
     *            position for insert.
     */
    void insertSource( Source source, int i );

    /**
     * @return size of sources list.
     */
    int sizeOfSource();
}
