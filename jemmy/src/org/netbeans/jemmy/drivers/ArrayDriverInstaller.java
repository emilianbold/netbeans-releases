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
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
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
 *
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.drivers;

/**
 * Auxiliary class making driver registration easier.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public class ArrayDriverInstaller implements DriverInstaller {
    String[] ids;
    Object[] drivers;

    /**
     * Constructs an ArrayDriverInstaller object.
     * Both parameter arrays mush have same length,
     * <code>drivers</code> must keep instances of
     * <a href = "Driver.html">Driver</a> or
     * <a href = "Driver.html">LightDriver</a> implementations.
     * @param ids      an array of driver IDs
     * @param drivers  an array of drivers.
     */
    public ArrayDriverInstaller(String[] ids, Object[] drivers) {
	this.ids = ids;
	this.drivers = drivers;
    }

    /**
     * Installs drivers from the array passed into constructor.
     */
    public void install() {
	for(int i = 0; i < ids.length; i++) {
	    DriverManager.setDriver(ids[i], drivers[i]);
	}
    }
}
