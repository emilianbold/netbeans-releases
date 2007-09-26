package nbbuild.misc.bugcompare;


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

public interface ChangelogHandler_1 {
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_name(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;

    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_msg(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_time(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_file(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_date(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_author(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_commondir(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_utag(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_weekday(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_changelog(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_revision(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_entry(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_branch(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_branchroot(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_tag(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    
}