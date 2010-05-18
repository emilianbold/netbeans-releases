/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.lib.collab.xmpp.jso.impl.x.muc;

import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamObject;
import org.jabberstudio.jso.StreamNode;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.format.DateTimeProfileFormat;
import net.outer_planes.jso.ElementNode;
import net.outer_planes.jso.ExtensionNode;
import org.netbeans.lib.collab.xmpp.jso.iface.x.muc.History;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;


/**
 *
 * 
 * @author Rahul Shah
 * 
 */
public class HistoryNode extends ElementNode implements History {
    
    //"Constants"
    public static final NSI     ATTRNAME_MAXCHARS = new NSI("maxchars", null);
    public static final NSI     ATTRNAME_MAXSTANZAS = new NSI("maxstanzas", null);
    public static final NSI     ATTRNAME_SECONDS = new NSI("seconds", null);
    public static final NSI     ATTRNAME_SINCE = new NSI("since", null);
    
    private static DateTimeProfileFormat df =
        DateTimeProfileFormat.getInstance(DateTimeProfileFormat.DATETIME);

    /** Creates a new instance of HistoryNode */
    public HistoryNode(StreamDataFactory sdf) {
        super(sdf, NAME);
    }
    protected HistoryNode(StreamElement parent, HistoryNode base) {
        super(parent, base);
    }
    
    public int getMaxChars() {
        Object  val = getAttributeObject(ATTRNAME_MAXCHARS);
        int maxchars = -1;
        
        if (val instanceof Number) {
            maxchars = ((Number)val).intValue();
        } else if (val != null) {
            Number  temp = null;
            try {
                temp = Integer.valueOf(val.toString());
                maxchars = temp.intValue();
                setAttributeObject(ATTRNAME_MAXCHARS, temp);
            } catch (NumberFormatException nfe) {
            }
        }
        return maxchars;
    }
    
    public int getMaxStanzas() {
        Object  val = getAttributeObject(ATTRNAME_MAXSTANZAS);
        int maxstanzas = -1;
        
        if (val instanceof Number) {
            maxstanzas = ((Number)val).intValue();
        } else if (val != null) {
            Number  temp = null;
            try {
                temp = Integer.valueOf(val.toString());
                maxstanzas = temp.intValue();
                setAttributeObject(ATTRNAME_MAXSTANZAS, temp);
            } catch (NumberFormatException nfe) {
            }
        }
        return maxstanzas;
    }
    
    public int getSeconds() {
        Object  val = getAttributeObject(ATTRNAME_SECONDS);
        int seconds = -1;
        
        if (val instanceof Number) {
            seconds = ((Number)val).intValue();
        } else if (val != null) {
            Number  temp = null;
            try {
                temp = Integer.valueOf(val.toString());
                seconds = temp.intValue();
                setAttributeObject(ATTRNAME_SECONDS, temp);
            } catch (NumberFormatException nfe) {
            }
        }
        return seconds;
    }
    
    public Date getSince() {
        String val = getAttributeValue(ATTRNAME_SINCE);
		try {
		   	return ((val != null) ? DateFormat.getInstance().parse(val) : null);
		} catch(ParseException pe) {
			//pe.printStackTrace();
			return null;
		}
    }
    
    public void setMaxChars(int n) throws IllegalArgumentException{
        if (n < 0)
            throw new IllegalArgumentException("maxchars cannot be less than 0");
        setAttributeObject(ATTRNAME_MAXCHARS, new Integer(n));
    }
    
    public void setMaxStanzas(int n) throws IllegalArgumentException{
        if (n < 0)
            throw new IllegalArgumentException("maxstanzas cannot be less than 0");
        setAttributeObject(ATTRNAME_MAXSTANZAS, new Integer(n));
    }
    
    public void setSeconds(int n) throws IllegalArgumentException{
        if (n < 0)
            throw new IllegalArgumentException("seconds cannot be less than 0");
        setAttributeObject(ATTRNAME_SECONDS, new Integer(n));
    }
    
    public void setSince(Date dateTime) throws IllegalArgumentException{
        if (dateTime == null)
            throw new IllegalArgumentException("datetime cannot be null");
        setAttributeValue(ATTRNAME_SINCE, df.format(dateTime));
    }
    
    public StreamObject copy(StreamElement parent) {
        return new HistoryNode(parent, this);
    }
}
