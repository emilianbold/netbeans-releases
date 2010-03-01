/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.spi.storage;

/**
 * This is a factory to be registered in the ServiceProvider which can open the storage using the unique
 * key which is returned by the 
 * @param <T> Storage instance this factory is responsible for
 * @author Maria Tishkova
 */
public interface PersistentDataStorageFactory<T extends PersistentDataStorage> extends DataStorageFactory<T> {

     /**
      * Opens storage with the unique key <code>uniqueKey</code>
      * @param uniqueKey unique key generated using <link>getUniqueKey(T)</link> method
      * @return storage opened if exists, <code>null</code> it the storage doesn't exists or cannot be opened
      */
     T openStorage(String uniqueKey);

     /**
      * Creates storage with the unique key <code>uniqueKey</code>
      * @param uniqueKey unique key generated using <link>getUniqueKey(T)</link> method
      * @return storage opened if exists, <code>null</code> it the storage doesn't exists or cannot be opened
      */
     T createStorage(String uniqueKey);


     /**
      * Opens storage with the unique key <code>uniqueKey</code>
      * @param uniqueKey unique key generated using <link>getUniqueKey(T)</link> method
      * @param mode the mode the storage will be opened: ReadOnly, ReadWrite
      * @return storage opened if exists, <code>null</code> it the storage doesn't exists or cannot be opened
      */
     T openStorage(String uniqueKey, Mode mode);


     /**
      * Unique key which can be used later to
      * @param storage
      * @return unique key
      */
     String getUniqueKey(T storage);

     enum Mode{
         ReadOnly,
         ReadWrite
     }
}
