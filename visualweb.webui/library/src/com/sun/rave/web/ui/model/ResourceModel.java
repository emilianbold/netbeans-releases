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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.web.ui.model;

/**
 *
 * @author deep
 */
public interface ResourceModel {


    /**
     * Returns the root value of the file system in question.
     * For example, in the default implementation of this interface for local
     * filesystems the root value would be "/" in Unix and "C:\" on Windows.
     *
     * @return returns the absolute root (directory for files and folders).
     */
    public String getAbsoluteRoot();


    /**
     * Sets the root value of the resource system in question.
     * For example, in the default implementation of this interface for local
     * filesystems the root value could be set to "/" in Unix and "C:\" on Windows.
     *
     * @param absRoot - the value to be used as the root of this resource system
     */
    public void setAbsoluteRoot(String absRoot);

    /**
     * Return the separator String for this resource system. For a 
     * file system chooser this would be File.separator.
     *
     * @return returns the separator String.
     */
    public String getSeparatorString();
    
     
    /**
     * Get the Server namefrom where the resources are being loaded.
     *
     * @returns the server name
     * 
     */
    public String getServerName();
    
     /**
     * Set the server name from where the resources are being loaded.
     *
     * @param serverName - the server name to be set
     * 
     */
    public void setServerName(String serverName);
    
    /**
     * Return the filter String currently in use.
     *
     * @return returns the filter String.
     */
    public String getFilterValue();
    
        
    /**
     * Set the filter String entered by the user in the Filter text field.
     *
     * @param filterString - the filter string to be used subsequently.
     * 
     */
    public void setFilterValue(String filterString);
    
        
    /**
     * Return the sort field that is currently active.
     * 
     * @return returns the sort field in use.
     */
    public String getSortValue();
    
        
    /**
     * Set the sort field chosen by the user from the drop down menu.
     * 
     * @param sortField - string representing sortField selected by the user.
     * 
     */
    public void setSortValue(String sortField);
    
    /**
     * This method is called to get the current directory of
     * the resuource list being displayed in the filechooser's listbox
     *
     * @return returns the current root (directory for files and folders).
     */
    public String getCurrentDir();
    
    
    /**
     * This method is called to set the current directory of
     * the resuource list that would be displayed in the next
     * display cycle.
     *
     * @param dir - the value to be set the new current root node.
     * 
     */
    public void setCurrentDir(String dir);
    
    /**
     * Returns the list of objects in the container represented by the
     * current directory. This method returns an Array of ResourecItem objects
     */
    public ResourceItem[] getFolderContent(String folder, 
	boolean disableFiles, boolean disableFolders);
    
    /**
     * Given a ResourceItem key return the ResourceItem.
     *
     * @param - the resource item key which is the same as the value of the 
     *   Option element in the listbox.
     * @return - the ResourceItem object
     */
    public ResourceItem getResourceItem(String itemKey);

    /**
     * Returns true if the supplied absolute path is a folder type.
     * 
     * @param  path - the absolute path to the resource
     * @return returns the current root (directory for files and folders).
     */
     public boolean isFolderType(String path);
     
    /**
     * This methods checks if the resource path in question can be accessed
     * by the user trying to select or view it.
     *
     * @param resourceName - the resource name to check for read access
     * @return true if the user can select the resource specified
     *  by the resource name.
     */
    public boolean canRead(String resourceName);
    
        
    /**
     * This methods checks if the resource path in question can be accessed
     * for writes by the user.
     *
     * @param resourceName - the resource name to check for write access
     * @return true if the user can select the resource specified
     *  by the resource name for write.
     * 
     */
    public boolean canWrite(String resourceName);
    
    
        
    /* *
     * Create a resource of the given name in the node specified.
     * In the context of the filechooser this would mean creating a file
     * or folder in the directory specified. The default filechooser does
     * not have this feature yet.
     *
     * @param resourceName the resourceName to be created.
     * @param rootDir the node where this resource shoul dbe created.
     * @return boolen value indicating success or failure.
     *
    public boolean createResource(String resourceName, String rootDir);
     */
    
    /* *
     * Returns true if the user has set if component is to function as
     * a file or directory chooser.
     * 
     * @return boolen value indicating if chooser type has been set
     *
    public boolean isChooserTypeSet();
     */
    
    /* *
     * Set the component to function as a folder chooser. This will 
     * cause all files or non container items to be disabled in the 
     * listbox.
     * 
     * @param flag - set to true if component should function as a 
     *      folder chooser. 
     *
    public void setFolderChooser(boolean flag);
     */
    
    /* *
     * This method returns true if the component is a folder chooser. This will 
     * cause all files or non container items to be disabled in the 
     * listbox.
     * 
     * @returns true if the component is a folder chooser, false otherwise.
     * 
    public boolean isFolderChooser();
     */

    public String getParentFolder();

    public String getEscapeChar();
    public String getDelimiterChar();
    public Object[] getSelectedContent(String[] content, boolean selectFolders)
	throws ResourceModelException;
    public String[] getRoots();
}
