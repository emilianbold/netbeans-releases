/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.util;

import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;

/**
 * @author Tomas Mysik
 */
public class PhpInterpreterTest extends NbTestCase {

    public PhpInterpreterTest(String name) {
        super(name);
    }

    public void testPhpInterpreter1() {
        PhpInterpreter phpInterpreter = new PhpInterpreter(null);
        assertEquals("", phpInterpreter.getInterpreter());
        assertEquals("", phpInterpreter.getFullCommand());
        assertEquals(0, phpInterpreter.getParameters().length);

        phpInterpreter = new PhpInterpreter("");
        assertEquals("", phpInterpreter.getInterpreter());
        assertEquals("", phpInterpreter.getFullCommand());
        assertEquals(0, phpInterpreter.getParameters().length);
    }

    public void testPhpInterpreter2() {
        PhpInterpreter phpInterpreter = new PhpInterpreter("C:\\Program Files\\php\\bin\\php");
        assertEquals("C:\\Program Files\\php\\bin\\php", phpInterpreter.getInterpreter());
        assertEquals(0, phpInterpreter.getParameters().length);
    }

    public void testPhpInterpreter3() {
        PhpInterpreter phpInterpreter = new PhpInterpreter("C:\\Program Files\\php\\bin\\php /q");
        assertEquals("C:\\Program Files\\php\\bin\\php", phpInterpreter.getInterpreter());
        String[] parameters = phpInterpreter.getParameters();
        assertEquals(1, parameters.length);
        assertEquals("/q", parameters[0]);
    }

    public void testPhpInterpreter4() {
        PhpInterpreter phpInterpreter = new PhpInterpreter("C:\\Program Files\\php\\bin\\php    -q     ");
        assertEquals("C:\\Program Files\\php\\bin\\php", phpInterpreter.getInterpreter());
        String[] parameters = phpInterpreter.getParameters();
        assertEquals(1, parameters.length);
        assertEquals("-q", parameters[0]);
    }

    public void testPhpInterpreter5() {
        PhpInterpreter phpInterpreter = new PhpInterpreter("C:\\Program-Files\\php\\bin\\php   -q    -a");
        assertEquals("C:\\Program-Files\\php\\bin\\php", phpInterpreter.getInterpreter());
        String[] parameters = phpInterpreter.getParameters();
        assertEquals(2, parameters.length);
        assertEquals("-q", parameters[0]);
        assertEquals("-a", parameters[1]);
    }

    public void testPhpInterpreter6() {
        PhpInterpreter phpInterpreter = new PhpInterpreter("/usr/bin/php");
        assertEquals("/usr/bin/php", phpInterpreter.getInterpreter());
        assertEquals(0, phpInterpreter.getParameters().length);
    }

    public void testPhpInterpreter7() {
        PhpInterpreter phpInterpreter = new PhpInterpreter("/usr/bin/php -q");
        assertEquals("/usr/bin/php", phpInterpreter.getInterpreter());
        String[] parameters = phpInterpreter.getParameters();
        assertEquals(1, parameters.length);
        assertEquals("-q", parameters[0]);
    }

    public void testPhpInterpreter8() {
        PhpInterpreter phpInterpreter = new PhpInterpreter("/usr/b-i-n/php -q -a");
        assertEquals("/usr/b-i-n/php", phpInterpreter.getInterpreter());
        String[] parameters = phpInterpreter.getParameters();
        assertEquals(2, parameters.length);
        assertEquals("-q", parameters[0]);
        assertEquals("-a", parameters[1]);
    }

    public void testPhpInterpreter9() {
        PhpInterpreter phpInterpreter = new PhpInterpreter("/usr/bin/phpunit --repeat 3   \"--repeat 3\"  MyTest   ");
        assertEquals("/usr/bin/phpunit", phpInterpreter.getInterpreter());
        String[] parameters = phpInterpreter.getParameters();
        assertEquals(4, parameters.length);
        assertEquals("--repeat", parameters[0]);
        assertEquals("3", parameters[1]);
        assertEquals("--repeat 3", parameters[2]);
        assertEquals("MyTest", parameters[3]);
    }
}
