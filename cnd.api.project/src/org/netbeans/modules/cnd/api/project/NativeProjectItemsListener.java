/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.project;

import java.util.List;

public interface NativeProjectItemsListener {
     /**
      * Called when a file is added to the project.
      * @param fileItem the file item that was added.
      */
     public void fileAdded(NativeFileItem fileItem);

     /**
      * Called when multiple files are added to the project.
      * @param fileItems the list of file items that was added.
      */
     public void filesAdded(List<NativeFileItem> fileItems);
     
     /**
      * Called when a file is removed from the project.
      * @param fileItem the file item that was removed.
      */
     public void fileRemoved(NativeFileItem fileItem);

     /**
      * Called when multiple files are removed from the project.
      * @param fileItems the list of file items that was added.
      */
     public void filesRemoved(List<NativeFileItem> fileItems);
     
     /**
      * Called when include paths or macro definitions have changed (and
      * the file needs to be re-parsed).
      * @param fileItem the file item that has changed.
      */
     public void filePropertiesChanged(NativeFileItem fileItem);
     
     /**
      * Called when include paths or macro definitions have changed (and
      * files needs to be re-parsed) for multiple files.
      * @param fileItems the list of file items that has changed.
      */
     public void filesPropertiesChanged(List<NativeFileItem> fileItems);
     
     /**
      * Called when include paths or macro definitions have changed (and
      * files needs to be re-parsed) for all files in project.
      */
     public void filesPropertiesChanged();

     /**
      * Called when item name is changed.
      * @param oldPath the old file path.
      * @param newFileIetm the new file item.
      */
    void fileRenamed(String oldPath, NativeFileItem newFileIetm);
    
    /**
     * Called when the project is deleted
     * @param nativeProject project that is closed
     */
    void projectDeleted(NativeProject nativeProject);
}
