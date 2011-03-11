/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.spi.actions;

import java.io.IOException;
import org.netbeans.api.actions.Savable;
import org.netbeans.modules.openide.awt.SavableRegistry;

/** Default implementation of {@link Savable} interface and
 * additional contracts, including dealing with {@link Savable#REGISTRY}.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since XXX
 */
public abstract class AbstractSavable implements Savable, Savable.DisplayName {
    /** Constructor for subclasses. 
     */
    protected AbstractSavable() {
    }

    /** Implementation of {@link Savable#save} contract. Calls
     * {@link #handleSave} and {@lin #unregister}.
     * 
     * @throws IOException if call to {@link #handleSave} throws IOException
     */
    @Override
    public final void save() throws IOException {
        handleSave();
        unregister();
    }
    
    /** Finds suitable display name for the object this {@link Savable}
     * represents.
     * @return human readable, localized short string name
     */
    @Override
    public abstract String findDisplayName();
    
    /** To be overriden by subclasses to handle the actual save of 
     * the object.
     * 
     * @throws IOException 
     */
    protected abstract void handleSave() throws IOException;

    /** Equals and {@link #hashCode} need to be properly implemented 
     * by subclasses to correctly implement equality contract. 
     * Two {@link Savable}s should be equal
     * if they represent the same underlying object beneath them. Without
     * correct implementation proper behavior of {@link #register()} and
     * {@link #unregister()} cannot be guaranteed.
     * 
     * @param obj object to compare this one to, 
     * @return true or false
     */
    @Override
    public abstract boolean equals(Object obj);

    /** HashCode and {@link #equals} need to be properly implemented
     * by subclasses, so two {@link Savable}s representing the same object
     * beneath are really equal and have the same {@link #hashCode()}.
     * @return integer hash
     * @see #equals(java.lang.Object)
     */
    @Override
    public abstract int hashCode();
    
    
    /** Tells the system to register this {@link Savable} into {@link Savable#REGISTRY}.
     * Only one {@link Savable} (according to {@link #equals(java.lang.Object)} and
     * {@link #hashCode()}) can be in the registry. New call to {@link #register()}
     * replaces any previously registered and equal {@link Savable}s. After this call
     * the {@link Savable#REGISTRY} holds a strong reference to <code>this</code>
     * which prevents <code>this</code> object to be garbage collected until it
     * is {@link #unregister() unregistered} or {@link #register() replaced by
     * equal one}.
     */
    protected final void register() {
        SavableRegistry.register(this);
    }
    
    /** Removes this {@link Savable} from the {@link Savable#REGISTRY} (if it 
     * is present there, by relying on {@link #equals(java.lang.Object)} 
     * and {@link #hashCode()}). 
     */
    protected final void unregister() {
        SavableRegistry.unregister(this);
    }
    
}
