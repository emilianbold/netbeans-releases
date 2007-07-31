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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.lib.uihandler;

 
/** A callback interface of a decorated representation of LogRecord.
 * Should be passed into {@link LogRecords#decorate} and will receive
 * appropriate callbacks.
 * 
 * @since 1.13
 */
public interface Decorable {
    public void setName(String n);

    public void setDisplayName(String n);

    public void setIconBaseWithExtension(String base);

    public void setShortDescription(String format);
}
