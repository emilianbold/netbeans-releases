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

package org.netbeans.spi.autoupdate;

import java.io.IOException;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;

/** <code>UpdateProvider</code> providers items for Autoupdate infrastructure. The items
 * are available on e.g. Update Center. Items can represents NetBeans Module,
 * its Localization, Feature as group of NetBeans Modules or special
 * components which needs own native installer to make them accessible in NetBeans product.
 * The infrastructure finds out <code>UpdateProvider</code> in <code>Lookup.getDefault()</code>,
 * the provider can be registring declaratively in XML layer.
 * Note: the former Autoupdate module allows declaration of former <code>AutoupdateType</code> on XML
 * layer, these declaration are read as new one UpdateProvider by reason of backward compatability.
 *
 * @author Jiri Rechtacek
 */
public interface UpdateProvider {
    
    /** Name of provider, this name is used by Autoupdate infrastructure for manimulating
     * of providers.
     * 
     * @return name of provider
     */
    public String getName ();
    
    /** Display name of provider. This display name can be visualized in UI.
     * 
     * @return display name of provider
     */
    public String getDisplayName ();
    
    /** Description of provider. This description can be visualized in UI.
     * 
     * @return description of provider or null
     */
    public String getDescription ();


    /**
     * @return <code>UpdateUnitProvider.CATEGORY</code> for a quality classification 
     * of updates comming from this instance
     */    
    public CATEGORY getCategory();
    
    /** Returns <code>UpdateItem</code>s which is mapped to its unique ID.
     * Unique ID depends on the type of <code>UpdateItem</code>.
     * 
     * @see UpdateItem
     * @return Map of code name of UpdateItem and instance of UpdateItem
     * @throws java.io.IOException when any network problem appreared
     */
    public Map<String, UpdateItem> getUpdateItems () throws IOException;
    
    /** Make refresh of content of the provider. The content can by read from
     * a cache. The <code>force</code> parameter forces reading content from
     * remote server.
     * 
     * @param force if true then forces to reread the content from server
     * @return true if refresh succeed
     * @throws java.io.IOException when any network problem appreared
     */
    public boolean refresh (boolean force) throws IOException;
    
}
