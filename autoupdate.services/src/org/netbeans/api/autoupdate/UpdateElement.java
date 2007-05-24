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

package org.netbeans.api.autoupdate;

import org.netbeans.modules.autoupdate.services.UpdateElementImpl;

/** Instances provided by the <code>UpdateUnit</code> which represents specific version
 * of update (e.g. module or feature). The <code>UpdateElement</code> can be installed,
 * available on a remote server or stored in backup.
 * 
 * @author Jiri Rechtacek (jrechtacek@netbeans.org)
 */
public final class UpdateElement {
    final UpdateElementImpl impl;
    
    UpdateElement (UpdateElementImpl elementImpl) {
        if (elementImpl == null) {
            throw new IllegalArgumentException ("UpdateElementImpl cannot be null while creating UpdateElement.");
        }
        this.impl = elementImpl;
    }
    
    /** Returns <code>UpdateUnit</code> where is this <code>UpdateElement</code> contained.
     * 
     * @return UpdateUnit in which belongs to
     */
    public UpdateUnit getUpdateUnit () {
        assert impl.getUpdateUnit () != null : "UpdateUnit for UpdateElement " + this + " is not null.";
        return impl.getUpdateUnit ();
    }
   
    /** Returns the code name of the update, sans release version.
     * 
     * @return code name of the update
     */
    public String getCodeName () {
        return impl.getCodeName ();
    }
    
    /** Returns the display name of the update, displaying in UI to end users.
     * 
     * @return display name
     */
    public String getDisplayName () {
        return impl.getDisplayName ();
    }
    
    /** Returns the specification version.
     * 
     * @return specification version or null
     */
    public String getSpecificationVersion () {
        return impl.getSpecificationVersion ().toString ();
    }
    
    /** Returns if the <code>UpdateElement</code> is active in the system.
     * 
     * @return true of UpdateElement is active
     */
    public boolean isEnabled () {
        return impl.isEnabled ();
    }
    
    /** Returns the description of update, displaying in UI to end users.
     * 
     * @return description
     */
    public String getDescription () {
        return impl.getDescription ();
    }
    
    /** Returns name of <code>UpdateProvider</code>
     * 
     * @return name of UpdateProvider
     */
    public String getSource () {
        return impl.getSource ();
    }
    
    /** Returns name of the author of the update element.
     * 
     * @return name or null
     */
    public String getAuthor () {
        return impl.getAuthor ();
    }
    
    /** Returns the <code>String</code> representation of <code>URL</code>.
     * 
     * @return String or null
     */
    public String getHomepage () {
        return impl.getHomepage ();
    }
    
    /** Returns size of <code>UpdateElement</code> in Bytes.
     * 
     * @return size
     */
    public int getDownloadSize () {
        return impl.getDownloadSize ();
    }
    
    /** Returns display name of category where <code>UpdateElement</code> belongs to.
     * 
     * @return name of category
     */
    public String getCategory () {
        return impl.getCategory ();
    }

    /** Returns date when <code>UpdateElement</code> was published or install time
     * if the <code>UpdateElement</code> is installed already. Can return null
     * if the date is unknown.
     * 
     * @return date in format "yyyy/MM/dd" or null
     */
    public String getDate () {
        return impl.getDate ();
    }

    /** Returns text of license agreement if the <code>UpdateElement</code> has a copyright.
     * 
     * @return String or null
     */
    public String getLicence () {                
        return impl.getLicence ();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final UpdateElement other = (UpdateElement) obj;

        if (this.impl != other.impl &&
            (this.impl == null || !this.impl.equals(other.impl)))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;

        hash = 53 * hash + (this.impl != null ? this.impl.hashCode()
                                              : 0);
        return hash;
    }
    
    @Override
    public String toString () {
        return impl.getDisplayName() + "[" + impl.getCodeName () + "/" + impl.getSpecificationVersion () + "]";
    }
}

