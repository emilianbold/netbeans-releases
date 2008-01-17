/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.php.model;

import java.util.List;

import javax.swing.text.Document;

import org.openide.util.Lookup.Provider;


/**
 * This is entry point for semantic access of PHP content.
 * Model consist from set of management methods and 
 * information retrieval methods.
 * This class is immutable for given Document ( it doesn't
 * mutate when something changed in source ). So you can keep 
 * safely reference to it.
 * All its information already is mutable. So you can't keep 
 * references to objects assuming they always available 
 * ( object can appear as removed ).   
 * 
 *  
 * @author ads
 *
 */
public interface PhpModel extends Acceptor, Provider  {
    
    /**
     * This method is called for synchronizing model with Document text
     * ( source code ).
     * Note that this method is called under read OM lock that is acquired 
     * inside Document lock. It means that one should never tries to lock
     * Document inside OM lock.   
     */
    void sync();
    
    /**
     * Lock OM on reading. See comments for sync : one should never lock 
     * Document inside OM lock. Document should be locked before
     * acquiring OM lock if there is need to lock Document. 
     */
    void readLock();
    
    void readUnlock();
    
    /**
     * Lock OM on writing. See comments for sync : one should never lock 
     * Document inside OM lock. Document should be locked before
     * acquiring OM lock if there is need to lock Document. 
     */
    void writeLock();
    
    void writeUnlock();
    
    /**
     * This is main "informational" model method. 
     * The structure of content is very close to structure of pure PHP nbs file.  
     * @return ordered list of top level language structures. Statement could
     * be some block, class def, .... 
     */
    List<Statement> getStatements();
    
    <T extends Statement> List<T> getStatements( Class<T> clazz );
    
    Document getDocument();
    
    /**
     * Finder method for searching model source element by given offset in Document. 
     * @param offset offset of searched element
     * @return source element at <code>offset</code>
     */
    SourceElement findSourceElement( int offset );
    
    /**
     * @return
     */
    ModelOrigin getModelOrigin();

}
