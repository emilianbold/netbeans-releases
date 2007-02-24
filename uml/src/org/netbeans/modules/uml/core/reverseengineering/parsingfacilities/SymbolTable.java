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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.EventExecutor;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class SymbolTable
{
    /**
     * Pops the current scope off the stack.  All instances that
     * was created by inside the current scope will be destroyed.
     *
     * @param pParent [in] The parent node that will recieve the 
     *                     Destroy messages.
     */
    public void popScope(Node parent)
    {
        if (!m_ScopeStack.isEmpty())
        {
            // Since the scope is pushed onto the back of the vector (behavior
            // of vector push_back), the current scope is on the end of the 
            // vector.
            //
            // The call to end returns the position after the last item on the 
            // stack.  So, I have to backup one before using the iterator.
            int last = m_ScopeStack.size() - 1;
            clearScope(m_ScopeStack.get(last), parent);
            
            m_ScopeStack.remove(last);
        }
    }
    
    /**
     * Removes all of the instances in a scope.  The instances will
     * be deleted, so any one holding on to an instance will no longer
     * be valid.
     *
     * @param scope [out] The scope object to clear.
     */
    protected void clearScope(Scope s, Node parent)
    {
        if (parent != null)
        {    
            Iterator< Map.Entry< String, InstanceInformation > > iter = 
                            s.entrySet().iterator();
            while (iter.hasNext())
            {
                InstanceInformation inf = iter.next().getValue();
                EventExecutor.sendDestroyAction(inf, parent);
            }
        }
        s.clear();
    }

    /**
     * Pushes a new scope onto the stack.
     *
     */
    public void pushScope()
    {
        m_ScopeStack.add(new Scope());
    }

    /**
     * Retrieves the instance from the symbol table.  If the instance is not found
     * an empty instance is returned.
     *
     * @param name [in] The instance to find.
     *
     * @return The instance.
     */
    public InstanceInformation findInstance(String name)
    {
        if (!m_ScopeStack.isEmpty())
        {
            // Since the scope is pushed onto the back of the vector (behavior
            // of vector push_back) I must search the stack backwards.
            for (int i = m_ScopeStack.size() - 1; i >= 0; --i)
            {
                Scope s = m_ScopeStack.get(i);
                if (s.containsKey(name))
                    return s.get(name);
            }
            
            if (m_GlobalTable.containsKey(name))
                return m_GlobalTable.get(name);
        }
        return null;
    }

    /**
     * Adds a new instance to the symbol table.  If the isGlobal flag is true than
     * the instance is added to the global table.  If isGlobal is false then the 
     * instance will be added to the current scope.  
     *
     * NOTE: The symbol table will take ownership of the lifecycle of the instance
     *       pointer.
     *
     * @param instance [in] The instance to add.
     * @param isGlobal [in] true - add to global table, false [DEFAULT] add to
     *                      current scope.
     */
    public void addInstance(InstanceInformation instance, boolean isGlobal)
    {
        if (instance != null)
        {
            if (!isGlobal)
            {
                Scope curScope = getCurrentScope();
                curScope.put(instance.getInstanceName(), instance);
            }
            else
            {
                m_GlobalTable.put(instance.getInstanceName(), instance);
            }
        }
    }

    public void removeInstance(String name)
    {
        if (name != null && name.length() > 0 && !m_ScopeStack.isEmpty())
        {
            // Since the scope is pushed onto the back of the vector (behavior
            // of vector push_back) I must search the stack backwards.
            for (int i = m_ScopeStack.size() - 1;
                    i >= 0 && m_ScopeStack.get(i).remove(name) == null;
                    --i)
                ;
        }
    }
    
    /**
     * Retrieves the current scope.  If there is not current scope
     * one will be added to the scope.
     *
     * @return The current scope.
     */
    protected Scope getCurrentScope()
    {
        if (m_ScopeStack.isEmpty())
            pushScope();
        return m_ScopeStack.get(m_ScopeStack.size() - 1);
    }

    // It's not clear to me whether we need the ordered iteration
    // of a TreeMap or if a HashMap is okay; just to be safe,
    // I'm going with a TreeMap for now, but we may want to change
    // this to a HashMap to improve performance.
    private static class Scope extends TreeMap<String, InstanceInformation>
    {
    }
    
    private ETList<Scope> m_ScopeStack  = new ETArrayList<Scope>();
    private Scope         m_GlobalTable = new Scope();
}