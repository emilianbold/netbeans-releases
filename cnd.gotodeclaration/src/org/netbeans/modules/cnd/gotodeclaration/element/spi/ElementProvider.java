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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.element.spi;

import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.spi.jumpto.type.SearchType;

/**
 * An Element Provider participates in the Goto Function or Variable 
 * (Go to Elemet) dialog by searching elements by their name 
 * ad returning the list of elements that suit
 * 
 * The Elements Providers are registered via Lookup.
 * 
 * @author Vladimir Kvashin
 */
public interface ElementProvider {
    
    /** 
     * Describe this provider with an internal name, 
     * in case we want to provide
     * some kind of programmatic filtering
     * 
     * @return An internal String uniquely identifying this type provider, 
     * such as "c/c++" or "java"
     */
    String name();

    /** 
     * Describe this provider for the user, 
     * in case we want to offer filtering
     * capabilities in the Go To Element dialog
     * 
     * @return A display name describing the types being provided by this ElementProvider,
     *  such as "Java Types", "C/C++ Types", etc.
     */
    String getDisplayName();
    
    /**
     * Used to determine whether the action should be enable or not
     * Returns true if there is a chance that this provider will return data,
     * otherwise false.
     */
    boolean isSuitable();
    
    /** 
     * Provide a list of ElementDescriptor 
     * that match the given search text for the given search type. 
     * 
     * This might be a slow operation.
     * The {@link #cancel} might be called during the operation,
     * in this case the method should return as soon as possible
     * (the results will be inclimplete in this case)
     * 
     * The method is called for the current project,
     * the for each open project other then current one.
     * 
     * @param project limits the search to the given project.
     * 
     * @param text The text to be used for the search; e.g. when type=SearchType.PREFIX,
     *   text is the prefix that all returned types should start with.
     * 
     * @param type A type of search to be performed, such as prefix, regexp or camel case.
     * 
     * @return A collection of ElementDescriptors that match the given search criteria
     */
    Collection<? extends ElementDescriptor> getElements(Project project, String text, SearchType type);

    /**
     * Cancel the current operation, if possible. 
     * Is called when user has typed something or just cancelled the dialog
     */
    void cancel();


    /**
     * Is called when the Go to Element dialog is dismissed.
     * Should perform necessary cleanup.
     */
    void cleanup();    

}
