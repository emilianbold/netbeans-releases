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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.jumpto.type;

import java.util.List;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.api.project.Project;

/**
 * A Type Provider participates in the Goto Type dialog by providing TypeDescriptors,
 * one for each matched type, when asked to do so.
 * 
 * The Type Providers are registered in Lookup.
 * 
 * @todo Should we return a Collection rather than a List?
 * 
 * @author Tor Norbye
 */
public interface TypeProvider {
    /** 
     * Describe this provider with an internal name, in case we want to provide
     * some kind of programmatic filtering (e.g. a Java EE dialog wanting to include
     * or omit specific type providers, without relying on class names or 
     * localized display names)
     * 
     * @return An internal String uniquely identifying this type provider, such as
     *   "java"
     */
    String name();

    /** 
     * Describe this provider for the user, in case we want to offer filtering
     * capabilities in the Go To Type dialog
     * 
     * @return A display name describing the types being provided by this TypeProvider,
     *  such as "Java Types", "Ruby Types", etc.
     */
    String getDisplayName();
    
    /** 
     * Provide a list of TypeDescriptors that match the given search text for the given
     * search type. This might be a slow operation, and the infrastructure may end
     * up calling {@link #cancel} on the same type provider during the operation, in which
     * case the method can return incomplete results. If there is a "current project",
     * the Go To Type infrastructure will perform the search in two passes; first it
     * will call {@link #getTypeNames} with the current project, which should be a reasonably
     * fast search, and display those types first. It will then call the method again
     * with a null project, which should return all types.
     * <p>
     * Note that a useful performance optimization is for the TypeProvider to cache
     * a few of its most recent search results, and if the next search (e.g. more user
     * keystrokes) is a simple narrowing of the search, just filter the previous search
     * result. There is an explicit {@link #cleanup} call that the Go To Type dialog
     * will make at the end of the dialog interaction, which can be used to clean up the cache.
     * 
     * @param project If not null, limit the type search to the given project.
     * @param text The text to be used for the search; e.g. when type=SearchType.PREFIX,
     *   text is the prefix that all returned types should start with.
     * @param type A type of search to be performed, such as prefix, regexp or camel case.
     * @return A collection of TypeDescriptors that match the given search criteria
     */
    List<? extends TypeDescriptor> getTypeNames(Project project, String text, SearchType type);

    /**
     * Cancel the current operation, if possible. This might be called if the user
     * has typed something (including the backspace key) which makes the current
     * search obsolete and a new one should be initiated.
     */
    void cancel();


    /**
     * The Go To Type dialog is dismissed for now - free up resources if applicable.
     * (A new "session" will be indicated by a new call to getTypeNames.)
     * 
     * This allows the TypeProvider to cache its most recent search result, and if the next
     * search is simply a narrower search, it can just filter the previous result.
     */
    void cleanup();
}
