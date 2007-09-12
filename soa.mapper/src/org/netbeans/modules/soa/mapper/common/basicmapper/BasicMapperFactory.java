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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.soa.mapper.common.basicmapper;

import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteManager;

import org.netbeans.modules.soa.mapper.common.basicmapper.exception.MapperException;

/**
 * <p>
 *
 * Title: </p> BasicMapperFactory <p>
 *
 * Description: </p> BasicMapperFactory provides an interface for other
 * applications to instantiate a IBasicMapper object instance.<p>
 *
 * @author    Un Seng Leong
 * @created   July 22, 2003
 */

public abstract class BasicMapperFactory {

    /**
     * the default mapper class name to use when
     * System.getProperty("com.stc.editor.mapper.class") return null.
     */
    public static final String DEFAULT_MAPPER_CLASS_NAME =
        "org.netbeans.modules.soa.mapper.basicmapper.BasicMapper";

    /**
     * the default mapper class name to use when
     * System.getProperty("com.stc.editor.mapper.class") return null.
     */
    public static final String DEFAULT_PALETTE_CLASS_NAME =
        "org.netbeans.modules.soa.mapper.common.palette.PaletteManager";

    /**
     * Instantiate and return a IBasicMapper instance with the system class
     * loader. The impl class name is determined by
     * createBasicMapper(ClassLoader loader).
     *
     * @return                     an newly instantiate IBasicMapper object
     * @exception MapperException  throws when error occurs from loading or
     *      instintating an IBasicMapper instnace.
     */
    public static IBasicMapper createBasicMapper()
        throws MapperException {
        return createBasicMapper(BasicMapperFactory.class.getClassLoader());
    }

    /**
     * Instantiate and return a IBasicMapper instance with the specified class
     * loader. The impl class name is loaded from System property of
     * 'com.stc.editor.mapper.class'; or the DEFAULT_MAPPER_CLASS_NAME is used
     * if not found.
     *
     * @param loader               the class loader to use to load the impl
     *      class.
     * @return                     an newly instantiate IBasicMapper object
     * @exception MapperException  throws when error occurs from loading or
     *      instintating an IBasicMapper instnace.
     */
    public static IBasicMapper createBasicMapper(ClassLoader loader)
        throws MapperException {

        String mapperClassName =
            (String) System.getProperty("org.netbeans.modules.soa.mapper.class");

        if (mapperClassName == null) {
            mapperClassName = DEFAULT_MAPPER_CLASS_NAME;
        }

        if (loader == null) {
            throw new MapperException(
                "Unable to create basic mapper from class "
                + mapperClassName + ". The class loader is null");
        }

        try {
            return (IBasicMapper) Class.forName(
                mapperClassName, false, loader).newInstance();
        } catch (Throwable t) {
            throw new MapperException("Unable to create basic mapper from class " + mapperClassName, t);
        }
    }

    /**
     * Instantiate and return a palette manager instance with the specified
     * folder name and the system class loader. The impl class depends on
     * createPaletteManager(String folderName, ClassLoader loader).
     *
     * @param folderName           the folder the paletee manager to initialize
     *      from
     * @return                     an instance of palette manager
     * @exception MapperException  throws when error occurs from loading or
     *      instintating an IPaletteManager instnace.
     */
    public static IPaletteManager createPaletteManager(String folderName)
        throws MapperException {

        return createPaletteManager(folderName,
            BasicMapperFactory.class.getClassLoader());
    }

    /**
     * Instantiate and return a palette manager instance with the specified
     * folder name and the class loader. The impl class name is loaded from
     * System property of 'com.stc.editor.palettemanager.class'; or the
     * DEFAULT_PALETTE_CLASS_NAME is used if not found.
     *
     * @param folderName           the folder the palette manager to initialize
     *      from
     * @param loader               the class loader to load the palette manager
     * @return                     an instance of palette manager
     * @exception MapperException  throws when error occurs from loading or
     *      instintating an IPaletteManager instnace.
     */
    public static IPaletteManager createPaletteManager(String folderName, ClassLoader loader)
        throws MapperException {

        if (folderName == null) {
            throw new MapperException(
                "Unable to create palette manager. The folder name is null");
        }

        if (loader == null) {
            throw new MapperException(
                "Unable to create palette manager. The class loader is null");
        }

        String pManagerClassName =
            (String) System.getProperty("org.netbeans.modules.soa.mapper.palette.class");

        if (pManagerClassName == null) {
            pManagerClassName = DEFAULT_PALETTE_CLASS_NAME;
        }

        try {
            IPaletteManager rslt =
                (IPaletteManager) Class.forName(
                pManagerClassName, false, loader).newInstance();
            rslt.setFolder(folderName);
            return rslt;
        } catch (Throwable t) {
            throw new MapperException(
                "Unable to create palette manager from class: " + pManagerClassName
                + " and initialize with folder " + folderName, t);
        }
    }

}
