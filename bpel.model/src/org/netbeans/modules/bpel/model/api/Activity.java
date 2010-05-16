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

package org.netbeans.modules.bpel.model.api;


/**
 * <p>
 * Java class for tActivity complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 *    &lt;complexType name=&quot;tActivity&quot;&gt;
 *      &lt;complexContent&gt;
 *        &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tExtensibleElements&quot;&gt;
 *          &lt;sequence&gt;
 *            &lt;element name=&quot;targets&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tTargets&quot; minOccurs=&quot;0&quot;/&gt;
 *            &lt;element name=&quot;sources&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tSources&quot; minOccurs=&quot;0&quot;/&gt;
 *          &lt;/sequence&gt;
 *          &lt;attribute name=&quot;name&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}NCName&quot; /&gt;
 *          &lt;attribute name=&quot;suppressJoinFailure&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tBoolean&quot; /&gt;
 *        &lt;/extension&gt;
 *      &lt;/complexContent&gt;
 *    &lt;/complexType&gt;
 * </pre>
 * 
 * @author ads
 */
public interface Activity extends ExtensibleElements, JoinFailureSuppressor,
        NamedElement, ExtendableActivity
{

    /**
     * @return targets child entity.
     */
    TargetContainer getTargetContainer();

    /**
     * Remove argets child entity.
     */
    void removeTargetContainer();

    /**
     * Set <code>target</code> as "targets" child .
     * 
     * @param target
     *            object for set.
     */
    void setTargetContainer( TargetContainer target );

    /**
     * @return "sources" container.
     */
    SourceContainer getSourceContainer();

    /**
     * Remove "sources" child entity.
     */
    void removeSourceContainer();

    /**
     * Set <code>source</code> as child "sources" entity..
     * 
     * @param source
     *            object for set.
     */
    void setSourceContainer( SourceContainer source );
    
    /**
     * Removes "name" attribute.
     */
    void removeName();

}
