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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class FileInformation implements IFileInformation
{
    private ETList<IDependencyEvent>    m_Dependencies;
    private ETList<IErrorEvent>         m_Errors;
    private List<IREClass>              m_Classes = new ArrayList<IREClass>();
    private List<IPackageEvent>         m_Packages = 
                                                new ArrayList<IPackageEvent>();
    
    /** 
     * returns a list of Dependencies that were found
     * 
     * @param pVal[out] the list of found dependencies
     * 
     * @return HRESULT
     */
    public ETList<IDependencyEvent> getDependencies()
    {
        if (m_Dependencies == null)
            m_Dependencies = new ETArrayList<IDependencyEvent>();
        return m_Dependencies;
    }

    /** 
     * returns a list of errors encountered
     * 
     * @param pVal[out] the list of encountered errors
     * 
     * @return HRESULT
     */
    public ETList<IErrorEvent> getErrors()
    {
        if (m_Errors == null)
            m_Errors = new ETArrayList<IErrorEvent>();
        return m_Errors;
    }

    /**
     * Retrieves the information about a specific error.
     * 
     * @param index [in] The error to retrieve.
     * @param pVal [out] The error.
     */
    public IErrorEvent getError(int index)
    {
        if (m_Errors == null)
            m_Errors = new ETArrayList<IErrorEvent>();
        return m_Errors.get(index);
    }

    /**
     * Retrieves the number of errors that occur in the file that was parsed.
     * 
     * @param pVal [out] The number of errors.
     */
    public int getTotalErrors()
    {
        return m_Errors != null? m_Errors.size() : 0;
    }

    /**
     * Adds an error event to the file information.
     * 
     * @param newVal [in] A new error.
     */
    public void addError(IErrorEvent e)
    {
        if (m_Errors == null)
            m_Errors = new ETArrayList<IErrorEvent>();
        m_Errors.add(e);
    }

    /**
     * Retrieves the information about one of the top level classes in the soruce file.
     * 
     * @param index [in] The class to retrieve.
     * @param pVal [out] The class.
     */
    public IREClass getClass(int index)
    {
        return m_Classes.get(index);
    }

    /**
     * Adds a new class to the source file information.
     * 
     * @param newVal [in] A new class.
     */
    public void addClass(IREClass newVal)
    {
        m_Classes.add(newVal);
    }

    /**
     * Retrieves the number of top level classes found in a source file.
     * 
     * @param pVal [out] The number of top level classes.
     */
    public int getTotalClasses()
    {
        return m_Classes.size();
    }

    /**
     * Retrieves the information about one of the dependencies in the soruce file.
     * 
     * @param index [in] The dependency to retrieve.
     * @param pVal [out] The dependency.
     */
    public IDependencyEvent getDependency(int index)
    {
        if (m_Dependencies == null)
            m_Dependencies = new ETArrayList<IDependencyEvent>();
        
        return m_Dependencies.get(index);
    }

    /**
     * Adds a new dependency to the source file information.
     * 
     * @param newVal [in] A new dependency.
     */
    public void addDependency(IDependencyEvent newVal)
    {
        if (m_Dependencies == null)
            m_Dependencies = new ETArrayList<IDependencyEvent>();
        m_Dependencies.add(newVal);
    }

    /**
     * Retrieves the number of dependencies found in a source file.
     * 
     * @param pVal [out] The number of dependencies.
     */
    public int getTotalDependencies()
    {
        return m_Dependencies != null? m_Dependencies.size() : 0;
    }

    /** 
     * returns the number of packages found.
     * 
     * @param pVal[out] number of packages found
     * 
     * @return HRESULT
     */
    public int getTotalPackages()
    {
        return m_Packages.size();
    }

    /** 
     * adds a package to this object's collection of packages
     * 
     * @param newVal[in] the package to add
     * 
     * @return HRESULT
     */
    public void addPackage(IPackageEvent newVal)
    {
        m_Packages.add(newVal);
    }

    /** 
     * returns the @a index'th (zero based index) package event.
     * 
     * @param index[in] zero based index of the package event you want
     * @param pVal[out] the package event
     * 
     * @return HRESULT
     */
    public IPackageEvent getPackage(int index)
    {
        return m_Packages.get(index);
    }

}
