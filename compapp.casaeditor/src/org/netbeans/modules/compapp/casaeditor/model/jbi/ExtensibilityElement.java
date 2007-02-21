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

package org.netbeans.modules.compapp.casaeditor.model.jbi;

import java.io.IOException;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.ComponentUpdater;

/**
 * Interface for JBI extensibility elements
 *
 * @author rico
 * @author Nam Nguyen
 */
public interface ExtensibilityElement extends JBIComponent {
	public static final String CONTENT_FRAGMENT_PROPERTY = "content";
    
    /**
     * Set/get attribute value.
     */
    public String getAttribute(String attribute);
    public void setAttribute(String attribute, String value);
    
    /**
     * Set/get attribute defined in given namespace.
     */
    public String getAnyAttribute(QName attr);
    public void setAnyAttribute(QName attr, String value);

    /**
     * Set/get content as XML fragment.
     * The XML fragment will be parsed and the resulting nodes will
     * replace the current children of this documentation element.
     * @param text XML fragment text.
     * @exception IOException if the fragment text is not well-form.
     */
    public String getContentFragment();
    public void setContentFragment(String fragment) throws IOException;

    /**
     * Adds child extensibility elements of unknown type.
     * @param anyElement any child component to add
     * @param index absolute index position in children list.
     */
    public void addAnyElement(ExtensibilityElement anyElement, int index);
    
    /**
     * Removes child extensibility element of unknown type.
     */
    public void removeAnyElement(ExtensibilityElement any);
    
    /**
     * @returns list of children extensibility elements of unknown type.
     */
    public List<ExtensibilityElement> getAnyElements();
    
    /**
     * Returns QName of the backing DOM element.
     */
    public QName getQName();
    
    /**
     * Interface for an extensibility element that could provide update visitor
     * to be used during sync from source.
     */
    interface UpdaterProvider extends ExtensibilityElement {
        /**
         * @return component updater to be used in merge operations when source sync happens.
         */
        <T extends ExtensibilityElement> ComponentUpdater<T> getComponentUpdater();
    }
    
    /**
     * Interface for an extensibility element that is a root of an embedded model.
     */
    interface EmbeddedModel extends ExtensibilityElement {
        DocumentModel getEmbeddedModel();
    }
}
