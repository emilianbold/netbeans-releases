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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.editor.hints;

/**TODO: icon?
 *
 * @author Jan Lahoda
 */
public interface Fix {

    public abstract String getText();

    /**
     * Correct the source, doing whatever the hint says it will do.
     * @return A ChangeInfo instance if invoking the hint caused changes
     *  that should change the editor selection/caret position, or null
     *  if no such change was made, or proper caret positioning cannot be
     *  determined.
     */
    public abstract ChangeInfo implement();
    
    public abstract int hashCode();
    
    public abstract boolean equals(Object o);

}
