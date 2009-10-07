/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package echo.simple;

/**
 *
 * @author Lukas Hasik
 */
public class SimpleTypes {
    /**
     * returns true
     */
    public boolean returnsBooleanNoParameter() {
        return true;
    }
    
    /**
     * returns 0 
     */    
    public byte returnsByteNoParameter() {
        return 0;
    }

    /**
     * returns '\u0000' 
     */    
    public char returnsCharNoParameter() {
        return '\u0000';
    }

    /**
     * returns 1.2d 
     */    
    public double returnsDoubleNoParameter() {
        return 1.2d;
    }

    /**
     * returns 1.2f 
     */    
    public float returnsFloatNoParameter() {
        return 1.2f;
    }
    
    /**
     * returns 1234 
     */
    public int returnsIntNoParameter() {
        return 1234;
    }
    /**
     * returns 1234L 
     */
    public long returnsLongNoParameter() {
        return 1234L;
    }
    /**
     * returns NOTHING 
     */
    public void returnsVoidNoParameter() {
        System.out.println("returnsVoidNoParameter");
    }




    
/////////////////////////////////////////////////////////////////    
    public boolean returnsBoolean(boolean param) {
        return param;
    }

    public byte returnsByte(byte param) {
        return param;
    }

    public char returnsChar(char param) {
        return param;
    }
    
    public double returnsDouble(double param) {
        return param;
    }

    public float returnsFloat(float param) {
        return param;
    }

    public int returnsInt(int param) {
        return param;
    }

    public long returnsLong(long param) {
        return param;
    }

    public short returnsShort(short param) {
        return param;
    }

    public String returnString(String param) {
        return param;
    }
    
    public void returnsVoid(String param) {
        System.out.println("returnsNothing " + param);
    }

}
