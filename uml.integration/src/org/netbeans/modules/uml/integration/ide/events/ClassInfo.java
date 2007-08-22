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

/*
 * File         : ClassInfo.java
 * Version      : 1.5
 * Description  : A metaclass for Java classes, used for transferring
 *                information between IDE integrations and the Describe
 *                IDE integration package.
 * Author       : Trey Spiva
 */
package org.netbeans.modules.uml.integration.ide.events;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IDerivationClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;
import org.netbeans.modules.uml.core.support.umlsupport.FileManip;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.integration.ide.ChangeUtils;
import org.netbeans.modules.uml.integration.ide.DiagramKind;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
import org.netbeans.modules.uml.integration.ide.UMLSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * A metaclass for Java classes, used for transferring information between IDE
 * integrations and the Describe IDE integration package.  This class is not
 * synchronized.  If you use a single ClassInfo instance in multiple threads,
 * the responsibility for synchronization is with the caller.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-04-23  Darshan     Added preliminary support for a ClassInfo to
 *                              be constructed from an IClass, to be used
 *                              in model-source operations.
 *   2  2002-04-24  Darshan     Added more model-source support attributes
 *                              and methods.
 *   3  2002-04-24  Darshan     Added more tweaks for model-source support -
 *                              ClassInfo now keeps track of the IClass off
 *                              which it was created and can update the IClass
 *                              directly with the file name of the local file
 *                              with which the IClass is associated.
 *   4  2002-04-25  Darshan     Updated to set the file path to the artifact
 *                              associated with the class and to correctly
 *                              initialize outerClass when creating a ClassInfo
 *                              from an IClass.
 *   5  2002-04-29  Darshan     Added null checks in updateFilename().
 *   6  2002-04-30  Darshan     Used JavaClassUtils to map Describe's modifiers
 *                              to Java modifiers.
 *   7  2002-04-30  Darshan     Reformatted to 4-space tabs, added accessor
 *                              to get the list of interfaces implemented and
 *                              made InterfaceChangeInfo public.
 *   8  2002-05-06  Darshan     Made removeInterface() check for the same
 *                              interface in the list of added interfaces and
 *                              remove it if already present.
 *                              Reformatted braces to Java style indenting.
 *   9  2002-05-13  Darshan     Updated to handle both class and interface
 *                              events by manipulating IClassifier references
 *                              instead of directly dealing with IClass and
 *                              IInterface references.
 *  10  2002-05-14  Darshan     Moved the project selector code from
 *                              update(SymbolTransaction) to update().
 *  11  2002-05-28  Darshan     Synchronized on EventHandler instance to allow
 *                              synch between source-model and model-source
 *                              threads during class update.
 *  12  2002-05-30  Darshan     Fixed bugs in addInterface() and
 *                              removeInterface() and added overloaded
 *                              convenience methods for these.
 *  13  2002-06-04  Darshan     Added code to store only relative paths in a
 *                              class element's artifact. The change is wholly
 *                              internal to ClassInfo, so no other code should
 *                              be affected.
 *  14  2002-06-05  Darshan     Fixed code to correctly pick up superinterfaces
 *                              of an interface.
 *  15  2002-06-10  Darshan     Fixed creating multiple generalization links.
 *  16  2002-06-14  Darshan     Added calls to prevent roundtrip events during
 *                              update.
 *  17  2002-06-19  Darshan     Flagged interfaces abstract by default.
 *  18  2002-06-19  Darshan     Added null pointer check before performing
 *                              sanity check :)
 *  19  2002-06-20  Darshan     Turned on two-way roundtrip for attribute
 *                              updates.
 *  20  2002-06-25  Darshan     Turned on two-way roundtrip for superclass/
 *                              interface updates (fix for 191 JBuilder- methods
 *                              not passed down to children).
 *  21  2002-06-28  Darshan     Added support for creating reference classes for
 *                              generalizations if necessary.
 *  22  2002-07-02  Mukta       Added a method to get the full class name,
 *                              including the package name
 *  23  2002-07-24  Darshan     Setting the relative path to the file as a
 *                              tagged value on the ISourceFileArtifact
 *                              (temporary workaround as project base dir is
 *                              not set correctly).
 * 24  2002-08-14   Mukta       Added code to set exisiting methods and
 *                              attributes to the classinfo.
 *
 * @see EventManager
 */
public class ClassInfo extends ElementInfo
{
    /**
     * Describe id for class elements.
     */
    public static final  String DS_CLASS        = "Class";
    
    /**
     * Describe id for interface elements.
     */
    public static final  String DS_INTERFACE    = "Interface";
    
    /**
     * Describe id for enumeration elements.
     */
    public static final  String DS_ENUMERATION    = "Enumeration";
    
    /**
     * Describe stereotype id for the interface stereotype.
     */
    public static final  String DS_STE_INTERFACE = "interface";
    
    /**
     * Describe stereotype id for the enumeration stereotype.
     */
    public static final  String DS_STE_ENUMERATION = "enumeration";
    
    // This is for private use only.  I will have to keep track of what items
    // have been updated.  I do not want to change
    //private int    mChangesMade   = 0;
    
    private String      id                  = null;
    // Darshan: So that SymbolTransaction can find the correct symbol.
    
    /** The outer class for this class.  Only used when a class is an inner
     * class. */
    private ClassInfo   outerClass          = null;
    
    /** The class that is the new super class. */
    private String  mNewExtendedClass = null;
    private IClassifier  mSuperClass = null;
    
    /** The pacakge that contains the new super class. */
    private String  mNewExtendedPack  = null;
    
    /** The package that contains the old super class. */
    private String  mRemoveExtendedPack   = null;
    
    /** The class that is longer the super class. */
    private String  mRemoveExtendedClass  = null;
    
    /** The import of the class. */
    private Vector  mImports           = new Vector();
    
    /** The class's data memebers. */
    private Vector  mMembers           = new Vector();
    
    /** The class's methods. */
    private Vector  mMethods           = new Vector();
    
    /** The class's constructors. */
    private Vector  mConstructors      = new Vector();
    
    /** The class's inner classes. */
    private Vector<ClassInfo>  mInnerClasses      = new Vector<ClassInfo>();
    
    /** The class's implemented interfaces. */
    private Vector  mInterfaces        = new Vector();
    
    // the new/preferred interfaces data member
    private ArrayList<IClassifier> superInterfaces = 
        new ArrayList<IClassifier>();
        
    /** The enumeration's literals. */
    private Vector mLiterals           = new Vector();
    
    /** The file that the class lives in. */
    private String  mFilename          = null;
    
    /** Flag to indicate that this is an inner class. */
    private Boolean mIsInnerClass      = null;
    
    /** The class's original package. */
    private String  mOrigPackage       = null;
    
    /** The class's new package. */
    private String  mNewPackage        = null;
    
    private EventFilter filter         = null;
    
    /**
     *  The base path of the project that owns this class. Typically used only
     * for model->source updates.
     */
    private String  basePath           = null;
    
    /** Flag to indicate that this class is an interface. */
    private boolean mIsInterface       = false;
    
    /** Flag to indicate that this class is an enumeration. */
    private boolean mIsEnumeration       = false;
    
    private boolean referenceClass          = false;
    
    private static boolean executingAddin   = false;
    
    private String exportSourceFolderName;
    
    
    /**
     *  The ClassElement which this ClassInfo wraps, usually null when updating
     * source to model, non-null otherwise.
     */
    private IClassifier classElement;
    
    /**
     * Contructs a new ClassInfo object.
     * @param type The type of the transaction.  Valid values are:
     *             ElementInfo.CREATE
     *             ElementInfo.DELETE
     *             ElementInfo.MODIFY
     */
    public ClassInfo(int type)
    {
        super(type);
        setName("");
        setPackage("");
    }
    
    /**
     *  Creates a ClassInfo with data from the given IClassifier object.
     *
     * @param clazz
     */
    public ClassInfo(IClassifier clazz)
    {
        super(clazz);
        if (clazz != null)
        {
            eraseRefClass(clazz);
            setFromClassifier(clazz);
        }
    }
    
    /* (non-Javadoc)
     * @see com.embarcadero.integration.events.ElementInfo#getOwningProject()
     */
    public IProject getOwningProject()
    {
        return classElement != null?
            (IProject) classElement.getProject() :
            getOuterClass() != null?
                getOuterClass().getOwningProject()  :
                null;
    }
    
    /**
     * Returns the source directory in which Describe thinks this class should
     * live.  In general, this is just the value of the SourceDir property for
     * the package to which the IClassifier for this class belongs.  This method
     * will return null if a) the ClassInfo doesn't have an IClassifier b) the
     * owning package does not have a valid SourceDir property, or c) a weird
     * error occurs.  Weird errors are logged, but no exceptions will be thrown.
     *
     * @return The absolute path in which Describe fancies this file ought to
     *         live.
     */
    public String getSourceDirectory()
    {
        if (classElement == null) return null;
        try
        {
            IElement owner = classElement;
            
            // Retrieve the package in which this class lives.  If the class
            // is an inner class, we'll want to skip past containing classes.
            while ((owner = owner.getOwner()) != null &&
                !(owner instanceof IPackage));
            
            if (owner == null)
            {
                // What manner of demonic IClassifier is this anyway?
                Log.impossible("Classifier " + classElement.getName()
                + " is not in a package?");
                return null;
            }
            
            IPackage pack = (IPackage)  owner;
            return pack.getSourceDir();
        }
        catch (Exception e)
        {
            Log.stackTrace(e);
        }
        return null;
    }
    
    public String getExportSourcePackage()
    {
        if (classElement == null) 
            return null;
        
        try
        {
            IElement owner = classElement;
            
            // Retrieve the package in which this class lives.  If the class
            // is an inner class, we'll want to skip past containing classes.
            while ((owner = owner.getOwner()) != null &&
                !(owner instanceof IPackage));
            

	    
            if (owner == null)
            {
                // What manner of demonic IClassifier is this anyway?
                Log.impossible("Classifier " + classElement.getName()
                + " is not in a package?");
                return null;
            }
            
            String packName = "";
	    if ( ! (owner instanceof IProject) ) 
	    {
		IPackage pack = (IPackage)owner;
		String fqn = pack.getFullyQualifiedName(false);
		if (fqn != null) {
		    packName = fqn.replace("::", File.separator);
		}		
	    }
            return createPath(getExportSourceFolderName(), packName);
        }
        
        catch (Exception e)
        {
            Log.stackTrace(e);
        }
        
        return null;
    }
    
    
    /**
     *
     * Ensures that an appropriate path is constructed given the source directory and the
     * name of this package
     *
     * @param sourceDir[in] The current source directory
     * @param name[in]      Name of this package
     *
     * @return The final source directory
     *
     */
    private String createPath(String sourceDir, String name)
    {
        String path = sourceDir;
        if (sourceDir.length() >0 && name.length() > 0)
        {
            String str = FileSysManip.addBackslash(sourceDir);
            str += name;
            
            // Now we want to support the ability for specific machines to handle
            // expansion variables for path expansion specific to that machine
            path = FileManip.resolveVariableExpansion(str);
        }
        return path;
    }
    
    
    /**
     * Retrieves the source directory root that Describe expects the package
     * containing this class to live in.  This is obtained by retrieving the
     * SourceDir property of the owning package/project, and stripping off the
     * package directories.
     *
     * @return The absolute path to the root of the source directory.
     */
    public String getRootSourceDirectory()
    {
        String sourceDir;
        
        if (getExportSourceFolderName() != null)
            sourceDir = getExportSourceFolderName();
        
        else
            sourceDir = getSourceDirectory();
        
        
        if (sourceDir != null)
        {
            // No need to check classElement for null, since it can't be null
            // if sourceDir is non-null.
            try
            {
                IElement element = classElement;
                // Strip off a directory segment for each level of package.
                // We don't check to see whether the directory name matches the
                // package name, but we probably should, to make sure things
                // don't go horribly awry.
                while ((element = element.getOwner()) != null &&
                    !(element instanceof IProject))
                    if (element instanceof IPackage)
                        sourceDir = new File(sourceDir).getParent();
            }
            catch (Exception e)
            {
                Log.stackTrace(e);
            }
        }
        return sourceDir;
    }
    
    public IProject getProject()
    {
        return classElement != null? (IProject) classElement.getProject()
        : null;
    }
    
    protected void setRefInfo(IClassifier clazz)
    {
        classElement = clazz;
        
        if (clazz == null) return ;
        
        String fullname = JavaClassUtils.getFullyQualifiedName(clazz);
        
        setId(clazz.getXMIID());
        setName(JavaClassUtils.getFullInnerClassName(fullname));
        setPackage(JavaClassUtils.getPackageName(fullname));
        
        setIsInterface(clazz instanceof IInterface);
        setIsEnumeration(clazz instanceof IEnumeration);
        
        // If this is an inner class, set the owner
        IElement owner = clazz.getOwner();
        if (owner instanceof IClassifier)
        {
            IClassifier cp = (IClassifier)  owner;
            outerClass = getRefClassInfo(cp, true);
            /*outerClass = new ClassInfo(null);
            outerClass.setRefInfo(cp);
             */
        }
        
        updateFilename(null);
    }
    
    /**
     *  Initializes this ClassInfo using the given Describe IClassifier. Note:
     * this is not recommended for use by IDEs.
     * 
     * 
     * @param cinfo The Describe IClassifier that's used to initialize this
     *              ClassInfo.
     */
    public void setFromClassifier(IClassifier classifier)
    {
        if (classifier == null || classifier.getName() == null)
            return;
        
        setRefInfo(classifier);
        referenceClass = JavaClassUtils.isReferenceClass(classifier);
        
        int mods = JavaClassUtils.getJavaModifier(classifier.getVisibility());
        
        if (classifier.getIsLeaf())
            mods |= Modifier.FINAL;

        if (classifier.getIsAbstract() && !(classifier instanceof IInterface))
            mods |= Modifier.ABSTRACT;

        setModifiers(new Integer(mods));
        setSuperInterfaces(classifier);
        setSuperclass(classifier);

        if (classifier instanceof IEnumeration)
            setLiterals((IEnumeration)classifier);
    }
    
    public void setLiterals(IEnumeration en)
    {
        if (mLiterals == null)
            mLiterals = new Vector();

        else
            mLiterals.clear();
        
        for (Iterator iter = en.getLiterals().iterator(); iter.hasNext(); )
        {
            IEnumerationLiteral lit = (IEnumerationLiteral) iter.next();
            mLiterals.add(new LiteralInfo(this, lit));
        }
    }
    
    public boolean isReferenceClass()
    {
        return referenceClass;
    }
    
    protected IProject getOwningProject(IElement el)
    {
        for (IElement owner = el.getOwner(); owner != null;
        owner = owner.getOwner())
        {
            if (owner instanceof IProject)
            {
                return (IProject)  owner ;
            }
        }
        return null;
    }
    
    public void setMethodsAndMembers(IClassifier clazz)
    {
        ETList<IOperation> ops = clazz.getOperations();
        for(int i = 0; i < ops.getCount(); i++)
        {
            IOperation op = ops.item(i);
            addMethod(new MethodInfo(this, op));
        }
        
        ETList<IAttribute> attribs = clazz.getAttributes();
        for(int i = 0; i < attribs.getCount(); i++)
        {
            IAttribute attr = attribs.item(i);
            addMember(new MemberInfo(this, attr));
        }
        
        // Add attributes for any navigable associations we participate in
        ETList<IAssociation> assocs = clazz.getAssociations();
        for (int i = 0, count = assocs.size(); i < count; ++i)
        {
            IAssociation assoc = assocs.get(i);
            ETList<IAssociationEnd> ends = assoc.getEnds();
            if (ends.size() == 2)
            {
                if (ends.get(0) instanceof INavigableEnd)
                {
                    INavigableEnd navEnd = (INavigableEnd) ends.get(0);
                    if (!clazz.isSame(navEnd.getParticipant()))
                        addMember(new MemberInfo(this, navEnd));
                }
                
                if (ends.get(1) instanceof INavigableEnd)
                {
                    INavigableEnd navEnd = (INavigableEnd) ends.get(1);
                    if (!clazz.isSame(navEnd.getParticipant()))
                        addMember(new MemberInfo(this, navEnd));
                }

                // if both ends have the same participant, 
                // then we have a self-referencing relationship
                if (clazz.isSame(ends.get(0).getParticipant()) &&
                    clazz.isSame(ends.get(1).getParticipant()))
                {
                    INavigableEnd navEnd = null;
                    
                    // if one of the ends is navigable, we need to 
                    // generate an attribute for it
                    
                    if (ends.get(0) instanceof INavigableEnd)
                        navEnd = (INavigableEnd)ends.get(0);
                    
                    else if (ends.get(1) instanceof INavigableEnd)
                        navEnd = (INavigableEnd)ends.get(1);

                    if (navEnd != null)
                        addMember(new MemberInfo(this, navEnd));
                }
            }
        }

	List<INamedElement> owned = clazz.getOwnedElements();
	if (owned != null) 
	{
	    for(INamedElement el : owned) {
		if (el instanceof IClass 
		    || el instanceof IInterface 
		    || el instanceof IEnumeration) 
		{
		    ClassInfo cinfo = getRefClassInfo((IClassifier)el, true, true);
		    addInnerClass(cinfo);
		}	    
	    }
	}
    }
    
    protected void setSuperclass(IClassifier clazz)
    {
        String superClass = getSuperclass(clazz);
        
        if (superClass != null)
        {
            if (!(clazz instanceof IEnumeration))
            {
                setExtendedClass(JavaClassUtils.getPackageName(superClass),
                   JavaClassUtils.getFullInnerClassName(superClass));
            }
            
            // In UML, Enumeration elements can't implement Interface elements,
            // but they generalize (extend) them, but translation to Java code
            // should be "implements" not "extends"
            else
            {
                IClassifier genClazz = 
                        clazz.getGeneralizations().get(0).getGeneral();
                
                if (genClazz instanceof IInterface)
                    addSuperInterface(genClazz);
            }
        }
    }
    
    protected String getSuperclass(IClassifier clazz)
    {
        if (clazz == null || clazz instanceof IInterface || 
            clazz instanceof IEnumeration)
        {
            return null;
        }
    
        ETList<IGeneralization> gens = clazz.getGeneralizations();
        
        if (gens != null && gens.getCount() > 0)
        {
            IGeneralization gen = gens.item(0);
            IClassifier general;
        
            if (gen != null && (general = gen.getGeneral()) != null)
            {
                // UML allows a Class element to generalize an Enumeration
                // element, but this is not OK in Java, so just ignore it
                if (general instanceof IEnumeration)
                    return null;

                // Class is generalizing one of its inner classes;
                // UML allows this, but Java does not
                else if (JavaClassUtils.isAnOwner(clazz, general))
                    return null;

		else
                {
		    if (general instanceof IDerivationClassifier) 
		    {
			IDerivation drv = general.getDerivation();
			if (drv != null) 
			{
			    IClassifier templ = drv.getTemplate();
			    if (templ instanceof IInterface) 
			    {
				addSuperInterface(general);
				return null;
			    }
			}
		    }
                    setSuperClass(general);
                    String superClass = JavaClassUtils.getFullyQualifiedName(general);
                    return superClass;
                }
            }
        }
        
        return null;
    }
    
    
    protected void setSuperInterfaces(IClassifier clazz)
    {
        mInterfaces.clear();
        superInterfaces.clear();
        
        if (isInterface())
        {
            ETList<IGeneralization> gens = clazz.getGeneralizations();
            
            if (gens != null && gens.getCount() > 0)
            {
                for (int i = 0; i < gens.getCount(); ++i)
                {
                    INamedElement interf = gens.item(i).getGeneral();
                    
                    if (interf == null) 
                        continue;
                    
                    // Interface is implementing one of its inner interfaces;
                    // UML allows this, but Java does not, so just ignore it
                    if (JavaClassUtils.isAnOwner(clazz, (IClassifier)interf))
                    {
                        continue;
                    }

                    String fullName = JavaClassUtils.getFullyQualifiedName(interf);
                    addInterface(fullName);
                    superInterfaces.add((IClassifier)interf);
                }
            }
        }
        
        else
        {
            ETList<IImplementation> impls = clazz.getImplementations();
            
            if (impls != null && impls.getCount() > 0)
            {
                for (int i = 0; i < impls.getCount(); ++i)
                {
                    INamedElement interf = impls.item(i).getSupplier();
                    
                    if (interf == null) 
                        continue;
                    
                    // Class is implementing one of its inner interfaces;
                    // UML allows this, but Java does not, so just ignore it
                    if (JavaClassUtils.isAnOwner(clazz, (IClassifier)interf))
                    {
                        continue;
                    }
                    
                    String fullName = JavaClassUtils.getFullyQualifiedName(interf);
                    addInterface(fullName);
                    superInterfaces.add((IClassifier)interf);
                }
            }
        }
    }
    
    public static void setExecutingAddin(boolean state)
    {
        executingAddin = state;
    }
    
    public static boolean isExecutingAddin()
    {
        return executingAddin;
    }
    
    /**
     * Creates a new ClassInfo object.
     * @param name The name of the class.
     * @param origPackage The package that contained the class before the
     *                    current change.
     * @param type The type of the transaction.  Valid values are:
     *             ElementInfo.CREATE
     *             ElementInfo.DELETE
     *             ElementInfo.MODIFY
     */
    public ClassInfo(String name, String origPackage, int type)
    {
        super(type);
        setName(name);
        setPackage(origPackage);
    }
    
    public ClassInfo(String decoratedName, int type)
    {
        super(type);
        setName(JavaClassUtils.getFullInnerClassName(decoratedName));
        setPackage(JavaClassUtils.getPackageName(decoratedName));
    }
    
    /**
     * Updates the Describe symbol that represents the class.
     * @param system The describe system that is to be updated.
     */
    public void update()
    {
        UMLSupport support = UMLSupport.getUMLSupport();
        IProject proj = null; /* NB60TBD  UMLSupport.getProjectForPath(getFilename());*/
        if(proj != null)
        {
            support.getProjectManager().setCurrentProject(proj);
        }
        else if (UMLSupport.getDefaultProject() != null)
        {
            support.getProjectManager().setCurrentProject(UMLSupport.getDefaultProject());
            Log.out("THERE is no matching source path for this file location so symbol will be added to the default project");
        }
        
        EventManager        manager = EventManager.getEventManager();
        //manager.setSystem(system);
        
        Log.out("=============================================");
        Log.out("Update called: " + this);
        
        //synchronized (gd.getRoundtripQueue()) {
        filter = manager.getEventFilter();
        filter.blockEvents(this, null);
        SymbolTransaction trans = null;
        try
        {
            Log.out("Creating SymbolTransaction");
            trans = new SymbolTransaction(this);
            Log.out("Done creating SymbolTransaction");
        }
        catch(Throwable e)
        {
            ErrorManager.getDefault().notify(e);
        }
        finally
        {
            filter.unblockEvents(this, null);
        }
        
        try
        {
            UMLSupport.setRoundTripEnabled(false);
            update(trans);
        }
        finally
        {
            UMLSupport.setRoundTripEnabled(true);
        }
        //}
    }
    
    public void setId(String id)
    {
        this.id = id;
    }
    
    public String getId()
    {
        return id;
    }
    
    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }
    
    public String getBasePath()
    {
        return basePath;
    }
    
    /**
     * Set this class's outer class.
     * @param outC The outer class definition.
     */
    public void setOuterClass(ClassInfo outC)
    {
        outerClass = outC;
    }
    
    /**
     * Retrieves this class's outer class.
     * @return The class information for the outer class, or null if this class
     *         does not have a outer class.
     */
    public ClassInfo getOuterClass()
    {
        return outerClass;
    }
    
    // only used by update method
    private void checkSanity()
    {
        if (getModifiers() != null)
        {
            int mods = getModifiers().intValue();
            if (!Modifier.isAbstract(mods) && isInterface())
            {
                // Interfaces are always abstract
                setModifiers(new Integer(mods | Modifier.ABSTRACT));
            }
        }
        
        String oldSuperclass = JavaClassUtils.formFullClassName(
            getExtendedPackage(), getExtendedClass()),
            newSuperclass = JavaClassUtils.formFullClassName(
            getRemovedExtendedPackage(),
            getRemovedExtendedClass());
        
        if (oldSuperclass != null && oldSuperclass.equals(newSuperclass))
        {
            setExtendedClass(null, null);
            setRemovedExtendedClass(null, null);
        }
    }
    
    /**
     * Update Describe using the specified Symbol transaction.
     * @see SymbolTransaction
     * @param trans The transaction that represent the Describe symbol to update.
     */
    public void update(SymbolTransaction trans)
    {
        if(trans.getSymbol() == null)
        {
            Log.out("Transaction has no IClassifier, aborting");
            return;
        }
        
        boolean invalidate = false;
        
        // Sanity checks
        checkSanity();
        
        EventManager manager = EventManager.getEventManager();
        filter = manager.getEventFilter();
        
        try
        {
            //manager.setSystem(system);
            IClassifier clazz = trans.getSymbol();
            if(clazz == null)
            {
                return;
            }
            
            setId(clazz.getXMIID());
            
            // // **## Log.out("Updating the class: " + getName());
            if(getChangeType() == ElementInfo.DELETE)
            {
                clazz.delete();
                return;
            }
            else
            {
                if(getImports() != null)
                {
                    updateImports(manager, trans);
                }
                
                if(getFilename() != null)
                {
                    ClassInfo.setSymbolFilename(clazz, getFilename());
                }
                
                Log.out("CI: Old package          : " + getPackage());
                Log.out("CI: Old name             : " + getName());
                Log.out("CI: New package          : " + getNewPackage());
                Log.out("CI: New name             : " + getNewName());
                if(getNewPackage() != null && !getNewPackage().trim().equals(""))
                {
                    IPackage pack = SymbolTransaction.getClassPackage(
                        getNewPackage(),
                        getFilename());
                    if(getOuterClass() == null)
                    {
                        pack.addOwnedElement(clazz);
                        invalidate = true;
                    }
                }
                else
                {
                    if(getNewPackage() != null && getNewPackage().trim().equals(""))
                    {
                        if(getOuterClass() == null)
                        {
                            UMLSupport.getCurrentProject().addOwnedElement(clazz);
                            invalidate = true;
                        }
                    }
                }
                
                if(isCommentSet())
                {
                    try
                    {
                        clazz.setDocumentation(getComment());
                    }
                    catch(Exception e)
                    {
                    }
                    //manager.updateClassComment(trans, getComment());
                }
                
                if(getNewName() != null)
                {
                    String newName = getNewName().replace('.', '$');
                    // inner class name has been changed
                    if(newName.indexOf("$") > 0)
                    {
                        newName = newName.substring(newName.lastIndexOf("$")+1);
                    }
                    if(!clazz.getName().equals(newName))
                    {
                        boolean rst = UMLSupport.isRoundTripEnabled();
                        UMLSupport.setRoundTripEnabled(true);
                        clazz.setName(newName);
                        UMLSupport.setRoundTripEnabled(rst);
                        
                        if(getNewName().lastIndexOf("$") > 0)
                        { // this is ans inner class
                            String outerClassName = getNewName().substring(0, getNewName().lastIndexOf("$"));
                            if(getPackage() != null && !getPackage().equals(""))
                            {
                                outerClassName = getPackage() + "." + outerClassName;
                            }
                            clazz.setOwner(JavaClassUtils.findClassSymbol(outerClassName));
                        }
                        invalidate = true;
                    }
                    //manager.updateClassName(trans, utils.getInnerClassName(getNewName()));
                }
                
                if(getModifiers() != null)
                {
                    manager.updateClassModifers(trans, getModifiers().intValue());
                }
                
                // Update superclass/superinterface data
                UMLSupport.setRoundTripEnabled(true);
                if(getRemovedExtendedClass() != null)
                {
                    // call this method on both super and sub class
                    //removeGeneralization(suClazz);
                    filter.blockEventType(ChangeUtils.RDT_RELATION_DELETED);
                    filter.blockEventType(ChangeUtils.RDT_DEPENDENCY_REMOVED);
                    
                    Log.out("Blocked Relation Delete events " + ChangeUtils.RDT_RELATION_DELETED);
                    try
                    {
                        removeGeneralization(clazz);
                    }
                    finally
                    {
                        filter.unblockEventType(ChangeUtils.RDT_RELATION_DELETED);
                        filter.unblockEventType(ChangeUtils.RDT_DEPENDENCY_REMOVED);
                        Log.out("UnBlocked Relation Delete events " + ChangeUtils.RDT_RELATION_DELETED);
                    }
                }
                
                if(!isEnumeration() && getExtendedClass() != null)
                {
                    filter.blockEventType(ChangeUtils.RDT_RELATION_CREATED);
                    filter.blockEventType(ChangeUtils.RDT_DEPENDENCY_ADDED);
                    Log.out("Blocking Relation Create events " + ChangeUtils.RDT_RELATION_CREATED);
                    try
                    {
                        addGeneralization(clazz);
                    }
                    finally
                    {
                        filter.unblockEventType(ChangeUtils.RDT_RELATION_CREATED);
                        filter.unblockEventType(ChangeUtils.RDT_DEPENDENCY_ADDED);
                        Log.out("UnBlocking Relation Create events " + ChangeUtils.RDT_RELATION_CREATED);
                    }
                }
                try
                {
                    filter.blockEventType(ChangeUtils.RDT_RELATION_DELETED);
                    filter.blockEventType(ChangeUtils.RDT_RELATION_CREATED);
                    filter.blockEventType(ChangeUtils.RDT_DEPENDENCY_ADDED);
                    filter.blockEventType(ChangeUtils.RDT_DEPENDENCY_REMOVED);
                    updateInterfaces(manager, trans);
                }
                finally
                {
                    filter.unblockEventType(ChangeUtils.RDT_RELATION_DELETED);
                    filter.unblockEventType(ChangeUtils.RDT_RELATION_CREATED);
                    filter.unblockEventType(ChangeUtils.RDT_DEPENDENCY_ADDED);
                    filter.unblockEventType(ChangeUtils.RDT_DEPENDENCY_REMOVED);
                }
                UMLSupport.setRoundTripEnabled(false);
                
                try
                {
                    UMLSupport.setRoundTripEnabled(false);
                    
                    updateMethods(trans, ElementInfo.DELETE);
                    updateMembers(trans, ElementInfo.DELETE);
                    if (mIsEnumeration)
                    {
                        updateEnumLiterals(trans, ElementInfo.DELETE);
                    }
                    
                    updateMethods(trans, ElementInfo.MODIFY);
                    updateMembers(trans, ElementInfo.MODIFY);
                    if (mIsEnumeration)
                    {
                        updateEnumLiterals(trans, ElementInfo.MODIFY);
                    }
                    
                    updateMembers(trans, ElementInfo.CREATE);
                    updateMethods(trans, ElementInfo.CREATE);
                    if (mIsEnumeration)
                    {
                        updateEnumLiterals(trans, ElementInfo.CREATE);
                    }
                }
                finally
                {
                    UMLSupport.setRoundTripEnabled(false);
                }
                updateConstructors(trans);
            }
            updateInnerClasses(manager, trans);
            manager.setAsInterface(trans, isInterface());
            manager.setAsEnumeration(trans, isEnumeration());
        }
        catch (Exception e)
        {
//            e.printStackTrace();
            Log.stackTrace(e);
        }
        finally
        {
            if (invalidate)
                eraseRefClass(trans.getSymbol());
        }
    }
    
    /**
     * Issue a command to Describe to add a generalization associated with a
     * class symbol.
     * @param state The transaction to act upon.
     * @param value The value.
     */
    public void addGeneralization(IClassifier clazz)
    {
        String superClassName = null;
        
        superClassName = JavaClassUtils.formFullClassName(getExtendedPackage(),
            getExtendedClass());
        
        // Check if the generalization already exists
        if (superClassName.equals(getSuperclass(clazz)))
        {
            // Presumably no further action is required
            return ;
        }
        
        IClassifier superClazz = JavaClassUtils.findClassSymbol(superClassName);
        if(superClazz == null)
        {
            boolean evt = UMLSupport.isRoundTripEnabled();
            UMLSupport.setRoundTripEnabled(false);
            try
            {
                superClazz = JavaClassUtils.createDataType(
                    JavaClassUtils.formFullClassName(
                    getExtendedPackage(),
                    getExtendedClass()
                    )
                    );
                /*
                superClazz = SymbolTransaction.createClass(getExtendedClass(),
                                               getExtendedPackage(),
                                               false, null,
                                               ElementInfo.CREATE);
                // Tag it as a reference class
                JavaClassUtils.setReferenceClass(superClazz, true);
                 */
            }
            finally
            {
                UMLSupport.setRoundTripEnabled(evt);
            }
        }
        IRelationFactory fact = new RelationFactory();
        IGeneralization g = fact.createGeneralization(superClazz, clazz);
        if (g != null)
        {
            IDiagram dia = UMLSupport.getProduct().getDiagramManager().getCurrentDiagram();
            if(dia != null
                && dia.getDiagramKind() == DiagramKind.DK_CLASS_DIAGRAM)
            {
                ICoreRelationshipDiscovery rel = dia.getRelationshipDiscovery();
                if (rel != null)
                    rel.createPresentationElement(g);
            }
        }
    }
    
    /**
     * Issue a command to Describe to remove a generalization associated with a
     * class symbol.
     * @param state The transaction to act upon.
     * @param value The value.
     */
    public void removeGeneralization(IClassifier clazz)
    {
        if(clazz == null)
            return;
        ETList<IGeneralization> rels = clazz.getGeneralizations();
        if(rels == null)
        {
            return;
        }
        
        int count = rels.getCount();
        for(int i=0; i<count; i++)
        {
            IGeneralization rel = rels.item(i);
            if(rel != null)
            {
                IClassifier gen = rel.getGeneral();
                rel.delete();
                
                if (JavaClassUtils.isReferenceClass(gen) &&
                    JavaClassUtils.isOrphan(gen))
                    gen.delete();
            }
        }
    }
    
    /**
     * Add an implemented interface to the class definition.
     * @param pName The package of the interface.
     * @param name The interface name.
     */
    public void addInterface(String pName, String name)
    {
        if(pName != null && name != null)
        {
            if (!JavaClassUtils.formFullClassName(pName, name).equals(
                JavaClassUtils.formFullClassName(getPackage(), getName())))
            {
                for (int i = 0; i < mInterfaces.size(); ++i)
                {
                    InterfaceChangeInfo ici = (InterfaceChangeInfo)
                    mInterfaces.elementAt(i);
                    if (ici.getInterfaceName().equals(name) &&
                        ici.getPackage().equals(pName))
                    {
                        ici.setChangeType(ElementInfo.CREATE);
                        return ;
                    }
                }
                mInterfaces.add(new InterfaceChangeInfo(pName, name, ElementInfo.CREATE));
            }
        }
    }
    
    public void addInterface(String fullname)
    {
        addInterface(JavaClassUtils.getPackageName(fullname),
            JavaClassUtils.getFullInnerClassName(fullname));
    }
    
    public InterfaceChangeInfo[] getInterfaces()
    {
        InterfaceChangeInfo[] inf = new InterfaceChangeInfo[mInterfaces.size()];
        mInterfaces.copyInto(inf);
        return inf;
    }
    
    /**
     * Remove an implemented interface to the class definition.
     * @param pName The package of the interface.
     * @param name The interface name.
     */
    public void removeInterface(String pName, String name)
    {
        // First check if we already have an InterfaceChangeInfo with the
        // same info, but ADDED
        for (int i = 0; i < mInterfaces.size(); ++i)
        {
            InterfaceChangeInfo ici = (InterfaceChangeInfo)
            mInterfaces.elementAt(i);
            if (ici.getInterfaceName().equals(name) &&
                ici.getPackage().equals(pName))
            {
                ici.setChangeType(ElementInfo.DELETE);
                return ;
            }
        }
        mInterfaces.add(new InterfaceChangeInfo(pName, name, ElementInfo.DELETE));
    }
    
    public void removeInterface(String fullname)
    {
        removeInterface( JavaClassUtils.getPackageName(fullname),
            JavaClassUtils.getFullInnerClassName(fullname));
    }
    
    /**
     * Retrieves all the changes to the list of implemented interfaces.
     * @return A list of InterfaceChangeInfo.
     */
    protected Vector getInterfaceChanges()
    {
        return mInterfaces;
    }
    
    /**
     * Set the class information that is the super class.
     * @param pName The package of the super class.
     * @param name The super class name.
     * @see #setRemovedExtendedClass
     */
    public void setExtendedClass( String pName, String name )
    {
        if(pName != null && name != null)
        {
            if (!JavaClassUtils.formFullClassName(pName, name).equals(
                JavaClassUtils.formFullClassName(getPackage(), getName())))
            {
                mNewExtendedClass = name;
                mNewExtendedPack  = pName;
            }
        }
        else
        {
            //we might be removing them.
            mNewExtendedClass = name;
            mNewExtendedPack  = pName;
        }
    }
    
    /**
     * Retrieve the name of the super class's.
     * @return The name.
     * @see #getRemovedExtendedClass
     */
    public String getExtendedClass()
    {
        return mNewExtendedClass;
    }
    
    /**
     * The package that contains the super class.
     * @return The package.
     * @see #getRemovedExtendedPackage
     */
    public String getExtendedPackage()
    {
        return mNewExtendedPack;
    }
    
    public String getOldSuperclass()
    {
        return JavaClassUtils.formFullClassName(getRemovedExtendedPackage(),
            getRemovedExtendedClass());
    }
    
    public String getNewSuperclass()
    {
        return JavaClassUtils.formFullClassName(getExtendedPackage(),
            getExtendedClass());
    }
    
    /**
     * Set the class information that is no longer the super class.
     * @param pName The package of the super class.
     * @param name The super class name.
     * @see #setExtendedClass
     */
    public void setRemovedExtendedClass(String pName, String name)
    {
        mRemoveExtendedClass = name;
        mRemoveExtendedPack  = pName;
    }
    
    /**
     * Retrieve the name of the super class's that is being removed.
     * @return The name.
     * @see #getExtendedPackage
     */
    public String getRemovedExtendedPackage()
    {
        return mRemoveExtendedPack;
    }
    
    /**
     * Retrieve the name of the super class's that is being removed.
     * @return The name.
     * @see #getExtendedClass
     */
    public String getRemovedExtendedClass()
    {
        return mRemoveExtendedClass;
    }
    
    /**
     * Set the collection of imports that this class uses.
     * @param value A vector of strings.
     */
    public void setImports( Vector value )
    {
        mImports = value;
    }
    
    /**
     * Get the collection of imports that this class uses.
     * @return A vector of strings.
     */
    public Vector getImports()
    {
        return mImports;
    }
    
    /**
     * Get the collection of imports that this class uses.
     * @return A vector of strings.
     */
    public void addImport(String stmt)
    {
        stmt = GenCodeUtil.removeGenericType(stmt);
        
        if (getOuterClass() != null)
            getOuterMostOwner().addImport(stmt);
            
        else if (!mImports.contains(stmt))
            mImports.add(stmt);
    }

    public void addImport(IClassifier classToImport)
    {
        // is this an inner class? no need to import anything
        if (getOuterMostOwner().getClassElement() == 
            getOuterClassifierOwner(classToImport))
        {
            return;
        }
        
        IPackage owningPackage = classToImport.getOwningPackage();

        // classToImport is in same package as this
        if (owningPackage == getClassElement().getOwningPackage())
            return;
        
        // classToImport is owned by (is inner class of) this
        else if (owningPackage == getClassElement())
            return;
        
        // classToImport is in the default package (owned by the Project)
        else if (owningPackage instanceof 
                org.netbeans.modules.uml.core.metamodel.structure.Project)
        {
            return;
        }
        
        // classToImport is void or primitive type
        else if (!GenCodeUtil.isValidClassType(
                classToImport.getFullyQualifiedName(false)))
        {
            return;
        }

        // classToImport is from the java.lang package
        else if (owningPackage.getFullyQualifiedName(false).equals("java::lang")) // NOI18N
            return;
        
        // not importing the outer most class for some reason???    
        IClassifier outerMostClassifier = getOuterClassifierOwner(classToImport);

        addImport(JavaClassUtils.convertUMLtoJava(
            outerMostClassifier.getFullyQualifiedName(false)));
    }
    
    private IClassifier getOuterClassifierOwner(IClassifier innerClass)
    {
        IElement owner = innerClass.getOwner();
        
        if (owner instanceof IPackage || owner instanceof IProject)
            return innerClass;
        
        return getOuterClassifierOwner((IClassifier)owner);
    }
    
    /**
     * Sets the class's members that need to be updated.  This may not include
     * all the class's members, only the ones that need to be updated.
     * @param value A list of MemberInfo objects.
     */
    public void setMembers( Vector value )
    {
        mMembers = value;
    }
    
    /**
     * Adds a data member that needs to be updated.
     * @param info A list of MemberInfo objects.
     */
    public void addMember( MemberInfo info )
    {
        mMembers.add(info);
    }
    
    /**
     * Gets the class's members that need to be updated.  This may not include
     * all the class's members, only the ones that need to be updated.
     * @return A list of MemberInfo objects.
     */
    public Vector getMembers()
    {
        return mMembers;
    }
    
    /**
     * Sets the class's methods that need to be updated.  This may not include
     * all the class's method, only the ones that need to be updated.
     * @param value A list of MethodInfo objects.
     */
    public void setMethods( Vector value )
    {
        mMethods = value;
    }
    
    /**
     * Adds a method that needs to be updated.
     * @param info A list of MethodInfo objects.
     */
    public void addMethod( MethodInfo info )
    {
        mMethods.add(info);
    }
    
    /**
     * Gets the class's methods that need to be updated.  This may not include
     * all the class's methods, only the ones that need to be updated.
     * @return A list of ConstructorInfo objects.
     */
    public Vector getMethods()
    {
        return mMethods;
    }
    
    /**
     * Sets the class's constructors that need to be updated.  This may not include
     * all the class's constructors, only the ones that need to be updated.
     * @param value A list of ConstructorInfo objects.
     */
    public void setConstructors( Vector value )
    {
        mConstructors = value;
    }
    
    /**
     *  Adds a constructor to the list of constructors that need to be updated.
     *
     * @param value The <code>ConstructorInfo</code> to be updated. If this is
     *              an instance of <code>MethodInfo</code>, <code>addMethod()
     *              </code> will be called internally (taking advantage of this
     *              is likely to cause confusion, though).
     */
    public void addConstructor( ConstructorInfo value )
    {
        if (value instanceof MethodInfo)
            // Accommodate stupid callers.
            addMethod((MethodInfo) value);
        else
            mConstructors.add(value);
    }
    
    /**
     * Gets the class's constructors that need to be updated.  This may not include
     * all the class's constructors, only the ones that need to be updated.
     * @return A list of MethodInfo objects.
     */
    public Vector getConstructors()
    {
        return mConstructors;
    }
    
    /**
     * Add an inner class that needs to be updated.
     * @param info The inner class to add.
     */
    public void addInnerClass(ClassInfo info)
    {
        info.setOuterClass(this);
        if (mInnerClasses != null && !mInnerClasses.contains(info))
            mInnerClasses.add(info);
    }
    
    /**
     * Sets the class's inner classes that need to be updated.  This may not include
     * all the class's inner classes, only the ones that need to be updated.
     * @param value A list of ClassInfo objects.
     */
    public void setInnerClasses( Vector value )
    {
        mInnerClasses = value;
    }
    
    /**
     * Gets the class's inner classes that need to be updated.  This may not include
     * all the class's inner classes, only the ones that need to be updated.
     * @return A list of ClassInfo objects.
     */
    public Vector getInnerClasses()
    {
        return mInnerClasses;
    }
    
    /**
     * Sets the enumeration's literals that need to be updated.  This may not include
     * all the enumeration's literals, only the ones that need to be updated.
     * @param value A list of LiteralInfo objects.
     */
    public void setLiterals(Vector value)
    {
        mLiterals = value;
    }
    
    /**
     * Adds a enum literal that needs to be updated.
     * @param info A list of LiteralInfo objects.
     */
    public void addLiteral(LiteralInfo info)
    {
        mLiterals.add(info);
    }
    
    /**
     * Gets the enumeration's literals that need to be updated.  This may not include
     * all the enumeration's literals, only the ones that need to be updated.
     * @return A list of LiteralInfo objects.
     */
    public Vector getLiterals()
    {
        return mLiterals;
    }
    
    /**
     * Sets the file that that contains the class.
     * @param value The name of the file.
     */
    public void setFilename( String value )
    {
        mFilename = value;
    }
    
    /**
     *  Updates the IClass associated with this ClassInfo with the
     * @param filename
     * @return
     */
    public String updateFilename(String filename)
    {
        if (classElement == null || getChangeType() == DELETE)
        {
            if (filename != null)
                setFilename(filename);
            return getFilename();
        }
        if (filename != null)
        {
            setFilename(filename);
            Log.out("Updating symbol filename for " + classElement.getName()
            + " to " + filename);
            setSymbolFilename(classElement, filename);
        }
        else
            setFilename(getSymbolFilename(classElement));
        
        return getFilename();
    }
    
    /**
     *  Returns the first artifact associated with the given class, or the
     * class itself if no artifact is available.
     *
     * @param clazz
     * @return
     */
    private static ISourceFileArtifact getClassArtifact(IClassifier clazz)
    {
        ETList<IElement> artifacts = clazz.getSourceFiles();
        
        if (artifacts == null || artifacts.getCount() == 0)
            return null;
        
        return (ISourceFileArtifact)  artifacts.item(0);
    }
    
    /**
     *  Sets the file with which the given class should be associated.
     * Not intended for direct use by IDE integrations.
     *
     * @param clazz
     * @param filename
     */
    public static void setSymbolFilename(IClassifier clazz, String filename)
    {
        // Portability FIXME: This code forces the OS-specific directory
        // separator, but has the potential to mess up absolute path names in
        // OSes where, say, '\' is a valid character in a filename (all Unixen
        // enjoy this feature).
        if (filename != null)
            filename = filename.replace('/', File.separatorChar)
            .replace('\\', File.separatorChar);
        
        ISourceFileArtifact el = getClassArtifact(clazz);
        if (el == null)
        {
            if (!(clazz.getOwner() instanceof IClassifier))
                clazz.addSourceFile(filename);
        }
        else
        {
            el.setSourceFile(filename);
        }
    }
    
    /**
     *  Gets the file which which the given class is associated. Not intended
     * for direct use by IDE integrations.
     * @param clazz
     * @return
     */
    public static String getSymbolFilename(IClassifier clazz)
    {
        ISourceFileArtifact el = getClassArtifact(clazz);
        
        if (el != null)
            return el.getSourceFile();
        else
        {
            IElement parent = clazz;
            while ((parent = parent.getOwner()) instanceof IClassifier)
            {
                IClassifier cparent = (IClassifier)  parent;
                if ((el = getClassArtifact(cparent)) != null)
                    return el.getSourceFile();
            }
        }
        
        return null;
    }
    
    /**
     * Gets the file that that contains the class.
     * @return The name of the file.
     */
    public String getFilename()
    {
        return mFilename;
    }
    
    /**
     * Specifies if the ClassInfo represents an inner class or a outer class.
     * @param value true if the class is an inner class, false otherwise.
     */
    public void setIsInnerClass( Boolean value )
    {
        mIsInnerClass = value;
    }
    
    /**
     * Retrieves if the ClassInfo represents an inner class or a outer class.
     * @return true if the class is an inner class, false otherwise.
     */
    public Boolean getIsInnerClass()
    {
        return mIsInnerClass;
    }
    
    /**
     * Sets the package name that contains the class.  This is the original
     * package name.
     * @param value The fully qualified name of the pacakge.
     */
    public void setPackage( String value )
    {
        mOrigPackage = value;
    }
    
    /**
     * Gets the package name that contains the class.  This is the original
     * package name.
     * @return The fully qualified name of the pacakge.
     */
    public String getPackage()
    {
        return mOrigPackage;
    }
    
    /**
     * Sets the package name that contains the class.  This is the new
     * package name.
     * @param value The fully qualified name of the pacakge.
     */
    public void setNewPackage( String value )
    {
        mNewPackage = value;
    }
    
    /**
     * Gets the package name that contains the class.  This is the new
     * package name.
     * @return The fully qualified name of the pacakge.
     */
    public String getNewPackage()
    {
        return mNewPackage;
    }
    
    /**
     * Specifies if the ClassInfo represents an interface or a class.
     * @param value true if the class is an interface, false otherwise.
     */
    public void setIsInterface(boolean value)
    {
        mIsInterface = value;
    }
    
    /**
     * Retrieves if the ClassInfo represents an interface or a class.
     * @return true if the class is an interface, false otherwise.
     */
    public boolean isInterface()
    {
        return mIsInterface;
    }
    
    /**
     * Specifies if the ClassInfo represents an enumeration or a class.
     * @param value true if the class is an enumeration, false otherwise.
     */
    public void setIsEnumeration(boolean value)
    {
        mIsEnumeration = value;
    }
    
    /**
     * Retrieves if the ClassInfo represents an enumeration or a regular class.
     * @return true if the class is an enumeration, false otherwise.
     */
    public boolean isEnumeration()
    {
        return mIsEnumeration;
    }
    
    /**
     * Updates all the methods for this symbol.  The methods that will be
     * updated are the MethodInfo object that where added to the class info.
     * @param trans The transaction that represents the Describe symbol.
     * @see SymbolTransaction
     */
    protected void updateMethods(SymbolTransaction trans, int changeType)
    {
        // **## Log.out("Updating Methods: " + mMethods.size());
        for(int i = 0; i < mMethods.size(); i++)
        {
            Log.out("Inside updateMethods ..");
            MethodInfo info = (MethodInfo) mMethods.elementAt(i);
            if (info.getChangeType() == changeType)
            {
                try
                {
                    filter.blockEvents(info, this);
                    info.update(trans);
                }
                finally
                {
                    filter.unblockEvents(info, this);
                }
            }
        }
    }
    
    /**
     * Updates all the data members for this symbol.  The members that will be
     * updated are the MemberInfo object that where added to the class info.
     * @param trans The transaction that represents the Describe symbol.
     * @see SymbolTransaction
     */
    protected void updateMembers(SymbolTransaction trans, int changeType)
    {
        // **## Log.out("Updating Members: " + mMembers.size());
        for(int i = 0; i < mMembers.size(); i++)
        {
            MemberInfo info = (MemberInfo)mMembers.elementAt(i);
            if (info.getChangeType() == changeType)
            {
                try
                {
                    filter.blockEvents(info, this);
                    info.update(trans);
                }
                finally
                {
                    filter.unblockEvents(info, this);
                }
            }
        }
    }
    
    /**
     * Updates all the data members for this symbol.  The members that will be
     * updated are the MemberInfo object that where added to the class info.
     * @param trans The transaction that represents the Describe symbol.
     * @see SymbolTransaction
     */
    protected void updateEnumLiterals(SymbolTransaction trans, int changeType)
    {
        for(int i = 0; i < mLiterals.size(); i++)
        {
            LiteralInfo info = (LiteralInfo)mLiterals.elementAt(i);
            if (info.getChangeType() == changeType)
            {
                try
                {
                    filter.blockEvents(info, this);
                    info.update(trans);
                }
                finally
                {
                    filter.unblockEvents(info, this);
                }
            }
        }
    }
    
    /**
     * Updates all the constructors for this symbol.  The constructors that will be
     * updated are the ConstructorInfo object that where added to the class info.
     * @param trans The transaction that represents the Describe symbol.
     * @see SymbolTransaction
     */
    protected void updateConstructors(SymbolTransaction trans)
    {
        // **## Log.out("Updating Constructors: " + mConstructors.size());
        for(int i = 0; i < mConstructors.size(); i++)
        {
            ConstructorInfo info = (ConstructorInfo)mConstructors.elementAt(i);
            try
            {
                filter.blockEvents(info, this);
                info.update(trans);
            }
            finally
            {
                filter.unblockEvents(info, this);
            }
        }
    }
    
    /**
     * Updates all the inner classes for this symbol.  The inner classes that will be
     * updated are the ClassInfo object that where added to the class info.
     * @param trans The transaction that represents the Describe symbol.
     * @see SymbolTransaction
     */
    protected void updateInnerClasses(EventManager manager, SymbolTransaction trans)
    {
        
        Log.out("Inside updateInnerClasses ..............................");
        Vector innerClasses = getInnerClasses();
        Log.out("vector size is " + innerClasses.size());
        // **## Log.out("Updating Inner Classes: " + innerClasses.size());
        for(int i = 0; i < innerClasses.size(); i++)
        {
            ClassInfo innerClass = (ClassInfo)innerClasses.elementAt(i);
            if (innerClass != null)
                Log.out("Updating inner class " + innerClass);
            //String fullName = innerClass.getPackage() + "." + innerClass.getName();
            SymbolTransaction innerTrans = trans.getInnerClass(innerClass);
            innerClass.update(innerTrans);
        }
    }
    
    /**
     * Updates all the imports statements for this symbol.
     * @param manager The transaction manager.
     * @param trans The transaction that represents the Describe symbol.
     * @see SymbolTransaction
     */
    protected void updateImports(EventManager manager, SymbolTransaction trans)
    {
        // TO DO
    }
    
    /**
     * Updates the interfacres that this class implements.
     * @param manager The transaction manager.
     * @param trans The transaction that represents the Describe symbol.
     * @see SymbolTransaction
     */
    protected void updateInterfaces(EventManager manager, SymbolTransaction trans)
    {
        Log.out("Inside updateInterfaces ...... ");
        Vector iFaces = getInterfaceChanges();
        // **## Log.out("Updating Interfaces: " + iFaces.size());
        
        for(int i = 0; i < iFaces.size(); i++)
        {
            InterfaceChangeInfo info = (InterfaceChangeInfo)iFaces.elementAt(i);
            Log.out("ClassInfo.updateInterfaces: Interface is " +
                info.getPackage() + "." + info.getInterfaceName());
            if(info.getChangeType() == ElementInfo.CREATE)
                manager.addInterface(trans, info.getPackage(), info.getInterfaceName());
            else
            {
                Log.out("Removing interface " + info.getPackage() + "."
                    + info.getInterfaceName());
                manager.removeInterface(trans, info.getPackage(), info.getInterfaceName());
            }
        }
    }
    
    public String getCode()
    {
        return "C";
    }
    
    /**
     * InterfaceChangeInfo is a utility clas that defines interfaces that are 
     * being implemented are that are that are no longer being removed.
     */
    public class InterfaceChangeInfo
    {
        private String mName       = "";
        private String mPackage    = "";
        private int    mChangeType = ElementInfo.CREATE;
        
        public InterfaceChangeInfo(String pName, String name, int type)
        {
            setInterfaceName(pName, name);
            setChangeType(type);
        }
        
        public final String getInterfaceName()
        {
            return mName;
        }
        
        public final String getPackage()
        {
            return mPackage;
        }
        
        public final String getDecoratedName()
        {
            return JavaClassUtils.formFullClassName(mPackage, mName);
        }
        
        public final void setInterfaceName(String pName, String name)
        {
            mPackage = pName;
            mName = name;
        }
        
        public final int getChangeType()
        {
            return mChangeType;
        }
        
        public final void setChangeType(int type)
        {
            mChangeType = type;
        }
    }
    
    public Object clone()
    {
        ClassInfo ci        = (ClassInfo) super.clone();
        if (mConstructors != null)
            ci.mConstructors = (Vector) mConstructors.clone();
        if (mImports != null)
            ci.mImports = (Vector) mImports.clone();
        if (mMembers != null)
            ci.mMembers = (Vector) mMembers.clone();
        if (mMethods != null)
            ci.mMethods = (Vector) mMethods.clone();
        if (mInnerClasses != null)
            ci.mInnerClasses = (Vector) mInnerClasses.clone();
        if (mInterfaces != null)
            ci.mInterfaces = (Vector) mInterfaces.clone();
        if (mLiterals != null)
            ci.mLiterals = (Vector) mLiterals.clone();
        return ci;
    }
    
    /**
     * forms a full class name including the package name.
     */
    public String getFullClassName()
    {
        String pack = this.getPackage();
        String className = this.getName();
        String rep = className;
        if (className != null)
        {
            rep = className.replace('.', '$');
        }
        return (pack == null || pack.equals(""))? rep : (pack + "." + rep);
    }
    
    /**
     *  Returns the IClassifier from which this ClassInfo was constructed, or
     * null if the ClassInfo was not constructed from an IClassifier. Note that
     * this is not recommended for use by IDE-specific code.
     *
     * @return <code>IClassifier</code> The IClassifier from which the ClassInfo
     *                                  was built.
     */
    public IClassifier getClassElement()
    {
        return classElement;
    }
    
    /**
     *  Looks for the IClassifier corresponding to this ClassInfo in the
     * Describe model. The IClassifier found will also be returned by
     * subsequent calls to getClassElement().
     *  Not recommended for use by IDE code.
     *
     * @return The <code>IClassifier</code>, or null if there is no equivalent
     *         to this ClassInfo in the Describe model.
     */
    public IClassifier retrieveClassifier()
    {
        classElement = JavaClassUtils.findClassSymbol(
            JavaClassUtils.formFullClassName(getPackage(), getName()));
        return classElement;
    }
    
    protected String old2new(String oldS, String newS)
    {
        return (newS != null)? (oldS + " -> " + newS) : oldS;
    }
    
    public String toString()
    {
        StringBuffer s = new StringBuffer();
        s.append("ClassInfo (" + getFullClassName() + ") " )
        .append(getChangeName())
        .append("\n")
        .append(" Name       : " + old2new(getName(), getNewName())).append("\n")
        .append(" Package    : " + old2new(getPackage(), getNewPackage()))
        .append("\n")
        .append(" Modifiers  : " + getModifierText(getModifiers())).append("\n");
        
        String oldS = getOldSuperclass(), newS = getNewSuperclass();
        if (oldS != null || newS != null)
        {
            if (oldS == null) oldS = "<none>";
            if (newS == null) newS = "<none>";
            s.append(" Superclass : " + old2new(oldS, newS));
        }
        
        if ((getMethods() != null && getMethods().size() > 0) ||
            (getConstructors() != null && getConstructors().size() > 0))
        {
            s.append("\n --- Methods ---\n");
            Vector meths = getMethods();
            for (int i = 0; i < meths.size(); ++i)
                s.append(meths.elementAt(i).toString() + "\n");
            Vector cons = getConstructors();
            for (int i = 0; i < cons.size(); ++i)
                s.append(cons.elementAt(i).toString() + "\n");
        }
        
        if (getMembers() != null && getMembers().size() > 0)
        {
            s.append("\n --- Fields ---\n");
            Vector fields = getMembers();
            for (Iterator iter = fields.iterator(); iter.hasNext();)
                s.append(iter.next().toString() + "\n");
        }
        return s.toString();
    }
    
    public String getModifierText(Integer mods)
    {
        if (mods == null) return null;
        return Modifier.toString(mods.intValue());
    }
    
    /**
     *  Determines if the given ElementInfo matches this ClassInfo.
     * @see ElementInfo#matches(ElementInfo)
     * @param el <code>ElementInfo</code> The ElementInfo to match against.
     * @return <code>true</code> if the other ElementInfo is a ClassInfo and
     *         matches the name, package, JavaDoc comment, superclass, filename
     *         and modifiers of this ClassInfo.
     */
    public boolean matches(ElementInfo el)
    {
        if (!(el instanceof ClassInfo)) return false;
        ClassInfo other = (ClassInfo) el;
        if (getName() != null && !getName().equals(other.getName()))
            return false;
        if (getPackage() != null && !getPackage().equals(other.getPackage()))
            return false;
        if (getComment() != null && !getComment().equals(other.getComment()))
            return false;
        if (getExtendedClass() != null &&
            !getExtendedClass().equals(other.getExtendedClass()))
            return false;
        if (getExtendedPackage() != null &&
            !getExtendedPackage().equals(other.getExtendedPackage()))
            return false;
        if (getFilename() != null && !getFilename().equals(other.getFilename()))
            return false;
        if (getModifiers() != null
            && !getModifiers().equals(other.getModifiers()))
            return false;
        return true;
    }
    
    /**
     *  Returns a reference ClassInfo, viz. a ClassInfo with the bare minimum
     * information needed to locate the source file.
     *
     * @param c The <code>IClassifier</code> from which to construct the
     *          <code>ClassInfo</code>. If <code>null</code>, returns
     *          <code>null</code>.
     * @param reuse If <code>true</code>, attempt to reuse a cached ClassInfo
     *              created from the same IClassifier. Use with care!
     * @return The reference <code>ClassInfo</code>.
     */
    public static ClassInfo getRefClassInfo(IClassifier c, boolean reuse)
    {
        return getRefClassInfo(c, reuse, false);
    }

    public static ClassInfo getRefClassInfo(IClassifier c, boolean reuse, boolean fullInit)
    {
        if (c == null) return null;
        
        ClassInfo ci = null;
        if (reuse)
        {
            ci = (ClassInfo) refClassInfos.get(c.getXMIID());
            //if (ci != null)
            //    Log.out("Reusing ClassInfo " + ci.getName());
        }
        if (ci == null)
        {
	    if (fullInit) 
	    {
		ci = new ClassInfo(c);
		ci.setMethodsAndMembers(c);
		ci.setComment(c.getDocumentation());
	    }       
	    else 
	    {
		ci = new ClassInfo(null);
		ci.setRefInfo(c);
	    }
            if (reuse) refClassInfos.put(c.getXMIID(), ci);
        }
        
        return ci;
    }

    
    /**
     *  Clears all cached <code>ClassInfo</code>s created by getRefClassInfo().
     */
    public static void eraseRefClasses()
    {
        refClassInfos.clear();
    }
    
    /**
     *  Removes any cached <code>ClassInfo</code> created from the given
     * <code>IClassifier</code>.
     * @param c The <code>IClassifier</code> which has (probably) been modified,
     *          thereby invalidating dependent <code>ClassInfo</code>s.
     */
    public static void eraseRefClass(IClassifier c)
    {
        if (c != null)
        {
            refClassInfos.remove(c.getXMIID());
            Log.out("Erased references to " + c.getName());
        }
    }


    public String getExportSourceFolderName()
    {
        return exportSourceFolderName;
    }
    
    public void setExportSourceFolderName(String sourceFolderName)
    {
        exportSourceFolderName = sourceFolderName;
    }

    public FileObject getExportSourceFolderFileObject()
    {
        File file = new File(getExportSourceFolderName());

        if (!file.exists())
        {
            if (!file.mkdirs())
                return null;
        }
       
        return FileUtil.toFileObject(file);
    }
    
    
    public FileObject getExportPackageFileObject()
    {
        return getExportPackageFileObject(null);
    }
    
    public FileObject getExportPackageFileObject(String subfolder)
    {
        String pathName = getExportSourcePackage();

        if (subfolder != null)
             pathName += File.separatorChar + subfolder;
        
        File file = new File(pathName);

        if (!file.exists())
        {
            if (!file.mkdirs())
                return null;
        }
        
        return FileUtil.toFileObject(file);
    }
    
    private static Hashtable refClassInfos = new Hashtable();

    public IClassifier getSuperClass()
    {
        return mSuperClass;
    }

    public void setSuperClass(IClassifier val)
    {
        this.mSuperClass = val;
    }

    public ArrayList<IClassifier> getSuperInterfaces()
    {
        return superInterfaces;
    }

    public void setSuperInterfaces(ArrayList<IClassifier> val)
    {
        this.superInterfaces = val;
    }
    
    public void addSuperInterface(IClassifier val)
    {
        if (val != null && !superInterfaces.contains(val))
        {
            superInterfaces.add(val);
        }
    }

    public ClassInfo getOuterMostOwner()
    {
        ClassInfo owner = getOuterClass();
        
        if (owner == null)
            return this;
        
        return owner.getOuterMostOwner();
    }


    //
    // added for template codegen
    //

    public ClassInfo getSuperClassInfo()
    {
	if (mSuperClass != null) 
	{
	    return getRefClassInfo(mSuperClass, true);
	}
        return null;
    }

    public ArrayList<ClassInfo> getSuperInterfaceInfos()
    {
	ArrayList<ClassInfo> res = new ArrayList<ClassInfo>();
	if (superInterfaces != null) 
	{
	    for(IClassifier interf : superInterfaces) 
	    {
		res.add(getRefClassInfo(interf, true));
	    }
	}
        return res;
    }

    public String[] getFullyQualifiedCodeGenType()
    {
	if (fullyQualifiedName == null) 
	{	    
	    IClassifier classType = getClassElement();
	    if (classType == null) {
		return null;
	    }
	    IPackage owningPkg = classType.getOwningPackage();
	    if (owningPkg == null) {
		return null;
	    }
	    String fullPkgName = owningPkg.getFullyQualifiedName(false);
	    
	    // default package elements have the project as the owning package
	    if (owningPkg instanceof IProject)
		fullPkgName = "";

	    // get fully qualified name - "com::foo::bar::Outer::Middle::Inner"
	    String qualName = classType.getFullyQualifiedName(false);
	    String fullClassName = qualName;

	    if (GenCodeUtil.isValidClassType(fullClassName))
	    {
		// extract the full class name - "Outer::Middle::Inner"
		// and convert to dot notation = "Outer.Middle.Inner"
		
		if (fullPkgName.length() > 0)
		{
		    fullClassName = JavaClassUtils.convertUMLtoJava
			(qualName.substring(fullPkgName.length()+2));
		    fullPkgName = JavaClassUtils.convertUMLtoJava(fullPkgName);
		}
		// it's in the default package
		else
		    fullClassName = JavaClassUtils.convertUMLtoJava(qualName);
	    
	    }
	    fullyQualifiedName = new String[] {fullPkgName, fullClassName};
	}
	return fullyQualifiedName;
    }

    private String[] fullyQualifiedName = null;

    public String getShortClassName() {
	return JavaClassUtils.getShortClassName(getName());
    }

    public String getCodeGenType(boolean fullyQualified, ClassInfo container)
    {
	if (fullyQualified) 
	{
	    if (codeGenTypeFullyQualified == null) 
	    { 
		codeGenTypeFullyQualified 
		    = GenCodeUtil.getTypeCodeGenType(getClassElement(), 
						     fullyQualified, 
						     container);
	    }
	    return codeGenTypeFullyQualified;
	}
	else 
	{
	    if (codeGenTypeShort == null) 
	    { 
		codeGenTypeShort 
		    = GenCodeUtil.getTypeCodeGenType(getClassElement(), 
						     fullyQualified, 
						     container);
	    }
	    return codeGenTypeShort;
	}
    }

    private String codeGenTypeFullyQualified = null;
    private String codeGenTypeShort = null;

    public Vector getFieldsCodeGenSorted() {
	Vector<MemberInfo> res = new Vector<MemberInfo>();	
	Iterator fs = mMembers.iterator();
	
	while(fs.hasNext()) {
	    MemberInfo field = (MemberInfo)fs.next(); 
	    res.add(field);
	}
	
	Collections.sort(res, new StaticAndAccessModifierComparator());	
	return res;
    }


    public Vector getConstructorsCodeGenSorted() {
	Vector<MethodInfo> res = new Vector<MethodInfo>();
	Iterator ms = mMethods.iterator();
	
	while(ms.hasNext()) {
	    MethodInfo method = (MethodInfo)ms.next(); 
	    IOperation op = method.getOperation();
	    if (op != null && op.getIsConstructor()) {		
		res.add(method);
	    }
	}

	Collections.sort(res, new StaticAndAccessModifierComparator());	
	return res;
    }


    public Vector getMethodsCodeGenSorted() {
	Vector<MethodInfo> res = new Vector<MethodInfo>();
	Iterator ms = mMethods.iterator();
	
	while(ms.hasNext()) {
	    MethodInfo method = (MethodInfo)ms.next(); 
	    IOperation op = method.getOperation();
	    if (op != null && ( ! op.getIsConstructor() ) ) {		
		res.add(method);
	    }
	}

	Collections.sort(res, new StaticAndAccessModifierComparator());	
	return res;
    }


    public static class StaticAndAccessModifierComparator implements Comparator<ElementInfo> 
    {
	
	public int compare(ElementInfo el1, ElementInfo el2) 
	{
	    int mod1 = el1.getModifiers(); 
	    int mod2 = el2.getModifiers();	    
	    if (Modifier.isStatic(mod1) == Modifier.isStatic(mod2)) 
	    {
		int res = compareAccessModifiers(mod1, mod2);
		if (res == 0) 
		{
		    return compareSpecific(el1, el2);	
		}	
		return res;
	    } 
	    else if (Modifier.isStatic(mod1)) 
		return -1;		
	    else // (Modifier.isStatic(mod2))
		return 1;
	}

	public int compareAccessModifiers(int mod1, int mod2) 
	{
	    return getNum(mod1) - getNum(mod2); 
	}

	int getNum(int mod) 
	{
	    if (Modifier.isPublic(mod))
		return 1;
	    else if (Modifier.isProtected(mod))
		return 2;
	    else if (Modifier.isPrivate(mod))
		return 4;
	    else //if package 
		return 3;
	}

	/**
	 *  additional checks, ie. for example to have 
	 *  setters/getters together, getter first
	 */
	public int compareSpecific(ElementInfo el1, ElementInfo el2) 
	{
	    if (el1 instanceof MethodInfo && el2 instanceof MethodInfo) 
	    {
		MethodInfo m1 = (MethodInfo) el1;
		MethodInfo m2 = (MethodInfo) el2;
		String attr1 = m1.getMemberName(); 
		String attr2 = m2.getMemberName(); 
		if (attr1 != null && attr2 != null) 
		{
		    int res = attr1.compareTo(attr2);		    
		    if (res != 0) 			
			return res;
		    if (m1.isAccessor()) 
			return -1;
		    else 
			return 1;
		} 
	    }
	    // by default we can't say nothing, ie. say that they are equal
	    return 0;
	}
    }

    
    public ArrayList<String> getImportedTypes() 
    {
	if(getOuterClass() != null)
	{
	    return getOuterClass().getImportedTypes();
	}

	// we're the outer class 
	ArrayList<String> res = new ArrayList<String>();	
	ArrayList<String[]> refs = getReferredCodeGenTypes();
	
	Iterator iter = refs.iterator();	
	while(iter.hasNext()) {
	    String[] pn = (String[]) iter.next();
	    if (pn != null && pn.length == 2 && pn[0] != null &&  pn[1] != null) {
		String pack = pn[0];
		String name = pn[1];
		if (pack == "") 
		    continue;
		if (pack.equals(getPackage()) || pack.equals("java.lang"))
		    continue;		
		String fq = pack+"."+name;
		res.add(fq);
	    }
	}
	Collections.sort(res);
	return res;	
    }


    public ArrayList<ClassInfo> getMemberTypes() 
    {	
	ArrayList<ClassInfo> res = new ArrayList<ClassInfo>();	
	for(ClassInfo inner : mInnerClasses) 
	{
	    res.add(inner);	    
	}
	return res;
    }


    public ArrayList<String[]> getReferredCodeGenTypes()
    {
	ArrayList<String[]> res = new ArrayList<String[]>();
	HashSet<String> fqNames = new HashSet<String>();

	// referred by the fields
	if (mMembers != null) {
	    Iterator fs = mMembers.iterator();	
	    while(fs.hasNext()) {
		MemberInfo field = (MemberInfo)fs.next();  
		ArrayList<String[]> refs = field.getReferredCodeGenTypes();
		GenCodeUtil.mergeReferredCodeGenTypes(res, fqNames, refs);
	    }
	}

	// referred by the methods' parameters and returns
	if (mMethods != null) {
	    Iterator ms = mMethods.iterator();	
	    while(ms.hasNext()) {
		MethodInfo method = (MethodInfo)ms.next();  
		ArrayList<String[]> refs = method.getReferredCodeGenTypes();
		GenCodeUtil.mergeReferredCodeGenTypes(res, fqNames, refs);
	    }
	}	    

	// referred by the inner types
	ArrayList<ClassInfo> memberTypes = getMemberTypes();
 	if (memberTypes != null) {
	    for(ClassInfo inner : memberTypes) {
		ArrayList<String[]> refs = inner.getReferredCodeGenTypes();
		GenCodeUtil.mergeReferredCodeGenTypes(res, fqNames, refs);		
	    } 
	}


	// referred by itself - extends/imports
	ArrayList<String[]> refs = GenCodeUtil.getReferredCodeGenTypes(mSuperClass, this);
	if (refs != null) {
	    GenCodeUtil.mergeReferredCodeGenTypes(res, fqNames, refs);
	}
	if (superInterfaces != null) {
	    Iterator<IClassifier> sis =  superInterfaces.iterator();
	    while(sis.hasNext()) {
		IClassifier si = sis.next();
		refs = GenCodeUtil.getReferredCodeGenTypes(si, this);
		//refs = GenCodeUtil.getReferredCodeGenTypes(sis.next());
		if (refs != null) {
		    GenCodeUtil.mergeReferredCodeGenTypes(res, fqNames, refs);
		}
	    } 
	}
	
	return res;
    }



}




