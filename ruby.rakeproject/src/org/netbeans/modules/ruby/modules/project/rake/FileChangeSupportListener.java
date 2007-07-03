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

package org.netbeans.modules.ruby.modules.project.rake;
import java.util.EventListener;

/**
 * Listener for changes in file existence and/or contents.
 * Unlike the Filesystems API, renames etc. are not considered special;
 * the "file" is identified uniquely by its path, not an object.
 * @author Jesse Glick
 */
public interface FileChangeSupportListener extends EventListener {

    void fileCreated(FileChangeSupportEvent event);

    void fileDeleted(FileChangeSupportEvent event);

    void fileModified(FileChangeSupportEvent event);

}
