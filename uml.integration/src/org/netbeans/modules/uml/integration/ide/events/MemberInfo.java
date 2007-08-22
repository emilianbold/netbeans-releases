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
 * File         : MemberInfo.java
 * Version      : 1.4
 * Description  : Information about the changes to an attribute.
 * Author       : Trey Spiva
 */
package org.netbeans.modules.uml.integration.ide.events;

import java.lang.reflect.Modifier;
import java.util.StringTokenizer;
import java.util.ArrayList;

import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.core.roundtripframework.RTMode;
import org.netbeans.modules.uml.core.roundtripframework.RoundTripModeRestorer;
import org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.JavaAttributeChangeFacility;
import org.netbeans.modules.uml.integration.ide.ChangeUtils;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.util.NbPreferences;

/**
 * The MemberInfo communicates with the EventManager to update Describe.
 * MemberInfo is still a high level class.  It knows how to communicate with
 * the EventMangaer to update Describe, but it does not know any of the details
 * of how to update Describe.
 * <br>
 * Because MemberInfo contains both before and after data, MemberInfo is able
 * to search for the data member and update it to how the source file current
 * represents the member.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-04-25  Darshan     Added (currently empty) constructors to
 *                              create a MemberInfo given an IAttribute.
 *   2  2002-04-26  Darshan     Added constructor to create a MemberInfo off
 *                              an IAttribute and reformatted the file to
 *                              4-space tabs.
 *   3  2002-04-30  Darshan     Used JavaClassUtils to map Describe's modifiers
 *                              to Java modifiers.
 *   4  2002-05-06  Darshan     Changed MemberInfo(ClassInfo, IAttribute) to
 *                              call the ElementInfo(INamedElement) constructor.
 *   5  2002-05-29  Darshan     Added check for navigable ends.
 *   6  2002-06-05  Darshan     Allow creation of MemberInfo from an
 *                              IStructuralFeature instead of an IAttribute,
 *                              since this allows us to construct MemberInfos
 *                              from INavigableEnds (so that we can navigate
 *                              to the attribute for a navigable end).
 *   7  2002-06-21  Darshan     Expanded check for the parent class for an
 *                              INavigableEnd, fixed attribute delete and
 *                              initializer bugs.
 *   8  2002-06-22  Darshan     Included code suggested by Sumitabh to find the
 *                              referencing classifier of an INavigableEnd.
 *
 * @see EventManager
 */
public class MemberInfo extends ElementInfo
{
    /** The containing class information. */
    private ClassInfo  mContainer = null;

    /** The original data type of the data member. */
    private String mOrigType      = null;

    /** The fully qualified name of the original type of the data member. */
    private String mOrigQualType  = null;

    /**
     * The data type of this data member as should be created in the source,
     * assuming that the source does not already contain a member of type
     * mOrigType or mOrigQualType. If null, this can be ignored.
     */
    private String collectionType = null;

    /** A global option that specifies whether or not to use generics when
     *  Collection data types are generated in code
     */
    private boolean useGenerics = false;

    /** The new data type of the data member. */
    private String mNewType       = null;

    /** The fully qualified name of the new type of the data member. */
    private String mNewQualType   = null;

    /** The initializer for the data member. */
    private String mInitializer   = null;

    private int modifierMask;

    /**
     *  The IAttribute from which this MemberInfo was constructed. If the
     * MemberInfo was constructed by an IDE integration, this should be null.
     */
    private IStructuralFeature attribute  = null;

    public IAttribute getAttribute() {
    	return (IAttribute)attribute;   
    }
    /**
     *  Intiailizes a new MemberInfo.
     *  @param container The class that contains the data member.
     *  @param type The transaction type.
     *  @see ElementInfo
     */
    public MemberInfo(ClassInfo container, int type) {
        super(type);
        setContainingClass(container);
    }

    public MemberInfo(ClassInfo container, IStructuralFeature attr) {
        super(attr);
        setContainingClass(container);

        // Remember the attribute we're created from. This should not be
        // used beyond the lifetime of the thread in which this MemberInfo
        // is created.
        attribute = attr;
        setFromAttribute(attribute);
    }

    /* (non-Javadoc)
     * @see com.embarcadero.integration.events.ElementInfo#getOwningProject()
     */
    public IProject getOwningProject() {
        return attribute != null?
                    (IProject) attribute.getProject() :
               getContainingClass() != null?
                    getContainingClass().getOwningProject() :
                    null;
    }

    public void syncFields() {
        super.syncFields();
        if (getNewType() != null) {
            setType(getQualifiedType(), getNewType());
            setNewType(getNewQualifiedType(), null);
        }

        if (getNewQualifiedType() != null) {
            setType(getNewQualifiedType(), getType());
            setNewType(null, getNewType());
        }
    }

    public String getFilename() {
        return (mContainer != null? mContainer.getFilename() : null);
    }

    public IProject getProject() {
        return attribute != null? (IProject) attribute.getProject()
                           : null;
    }

    /**
     * Set all properties for this MemberInfo using info from the given
     * IStructuralFeature.
     * @param attr An <code>IStructuralFeature</code> for the attribute (or
     *             navigable association).
     */
    public void setFromAttribute(IStructuralFeature attr)
    {
        setName(attr.getName());
        int mods = JavaClassUtils.getJavaModifier(attr.getVisibility());
        
        if (attr.getIsFinal())
            mods |= Modifier.FINAL;
        
        if (attr.getIsStatic())
            mods |= Modifier.STATIC;
        
        if (attr.getIsTransient())
            mods |= Modifier.TRANSIENT;
        
        if (attr.getIsVolatile())
            mods |= Modifier.VOLATILE;
        
        setModifiers(new Integer(mods));
        
        setType(JavaClassUtils.getFullyQualifiedName(
            attr.getType()),attr.getTypeName());
        
        collectionType = null;
        int mul = getMultiplicity(attr);
        
        if (mul > 0)
        {
            collectionType = getCollectionOverrideDataType();
            StringBuffer collectionLeft = new StringBuffer();
            StringBuffer collectionRight = new StringBuffer();
            StringBuffer arr = new StringBuffer(mul * 2);
            
            for (int i = 0; i < mul; ++i)
            {
                arr.append("[]"); // NOI18N
                
                if (isUseGenerics())
                {
                    if (i == 0)
                    {
                        collectionLeft.append('<');
                        
                        if (mul == 1)
                            collectionLeft.append(mOrigType);
                        
                        else
                            collectionLeft.append(collectionType).append('<');
                    }
                    
                    else if (i == mul-1)
                        collectionLeft.append(mOrigType);
                    
                    else
                        collectionLeft.append(collectionType).append('<');
                    
                    collectionRight.append('>');
                }
            }
            
            mOrigType += arr.toString();
            mOrigQualType += arr.toString();
            collectionType += collectionLeft.append(collectionRight).toString();
                
            // cvc - CR 6286610
            // disabled because nothing else gets an initial value
            //  in code generation and this is messing with array/collection
            //  mulitiplicity settings in RoundTrip
            // setInitializer("new " + collectionType + "()");
        }
        
        if (attr instanceof IAttribute)
        {
            IAttribute at = (IAttribute)  attr;
            String init = at.getDefault().getBody();
            
            if (init != null && init.trim().length() == 0)
                init = null;
            
            // cvc - CR 6286610
            // disabled because nothing else gets an initial value
            //  in code generation and this is messing with array/collection
            //  mulitiplicity settings in RoundTrip
            //  if (init == null && collectionType != null)
            //      init = "new " + collectionType + "()";
            
            setInitializer(init);
        }

        // Assuming the container hasn't been set, attempt to create a
        // ClassInfo for it.
        if (getContainingClass() == null)
        {
            IElement owner = attr.getOwner();
            
            if (owner instanceof IClassifier)
            {
                IClassifier cp = (IClassifier) owner ;
                ClassInfo inf = ClassInfo.getRefClassInfo(cp, true);
                setContainingClass(inf);
            }
            
            else
            {
                // Might this be a navigable end?
                if (attr instanceof INavigableEnd)
                {
                    INavigableEnd nav = (INavigableEnd)  attr;
                    IClassifier feat = null;
                    
                    if (nav.getOtherEnd2() != null)
                        feat = nav.getOtherEnd2().getParticipant();

                    else
                    {
                        // Do we have a referencing IClassifier?
                        feat = nav.getReferencingClassifier();
                    }
                    
                    if (feat != null)
                    {
                        Log.out("Found participant classifier " 
                            + feat.getName());
                        
                        ClassInfo inf = ClassInfo.getRefClassInfo(feat, true);
                        setContainingClass(inf);
                    }
                }
            }
        }
    }

    public MemberInfo(IStructuralFeature attr) 
    {
        this(null, attr);
    }


    /**
     * Retrieves the containing clas of the data member.
     * @return The containing class.
     */
    public ClassInfo getContainingClass()
    {
        return mContainer;
    }

    /**
     * Sets the containing clas of the data member.
     * @param container The containing class.
     */
    public void setContainingClass(ClassInfo container)
    {
        mContainer = container;
    }

    /**
     * Sets the original type of the data member.
     * @param fullName The fully qualified type data member
     * @param value The data types simple name.
     */
    public void setType (String fullName, String value )
    {
        mOrigType = value;
        mOrigQualType = fullName;
    }

    /**
     * Retrieves the simple name for the data member's original type.
     * @return The data types simple name.
     */
    public String getType()
    {
        return mOrigType;
    }

    /**
     * Retrieves the full name for the data member's original type.
     * @return The fully qualified type data member.
     */
    public String getQualifiedType()
    {
        return mOrigQualType;
    }

    public String getCollectionOverrideDataType() 
    {
        // TODO: conover - change this to use attribute level property
        // rather than the global preference
        //kris richards - made change to nbpreferences
        return NbPreferences.forModule(DummyCorePreference.class).get("UML_COLLECTION_OVERRIDE_DEFAULT", "java.util.ArrayList"); // NOI18N
    }

    public boolean isCollectionType() 
    {
        return Util.isValidCollectionDataType(getQualifiedType());
    }


    public boolean isUseCollectionOverride()
    {
        // TODO: conover - change this to use attribute level property
        // rather than the global preference
        return getAttribute().getMultiplicity().getRangeCount() > 0 &&
            Util.isValidCollectionDataType(getCollectionOverrideDataType());
    }

    public boolean isUseGenerics()
    {
        // TODO: conover - eventually, use the atribute level property
        // instead of this global preference
        //kris richards - made change to nbpreferences
        return NbPreferences.forModule(DummyCorePreference.class).getBoolean("UML_USE_GENERICS_DEFAULT", true); // NOI18N
        
    }

    /**
     * Sets the new type of the data member.
     * @param fullName The fully qualified type data member
     * @param value The data types simple name.
     */
    public void setNewType( String fullName, String value )
    {
        mNewType = value;
        mNewQualType = fullName;
    }

    /**
     * Retrieves the simple name for the data member's new type.
     * @return The data types simple name.
     */
    public String getNewType()
    {
        return mNewType;
    }

    /**
     * Retrieves the full name for the data member's new type.
     * @return The fully qualified type data member.
     */
    public String getNewQualifiedType()
    {
        return mNewQualType;
    }

    /**
     * Checks if the data type of the attribute is primitive
     * @return true if data type is non primitive or not a String
     */
    public boolean isNonPrimitive()
    {
       return "String".equals(mOrigQualType) && // NOI18N
           !JavaClassUtils.isPrimitive(mOrigQualType);
    }

    /**
     * Retrieves the initailizer for the data member.
     * @return The data members initailizer.
     */
    public String getInitializer()
    {
        return mInitializer;
    }

    /**
     * Sets the initailizer for the data member.
     * @return The data members initailizer.
     */
    public void setInitializer(String value)
    {
        mInitializer = value;
    }

    /**
     * Updates the data member.  A Class Transaction is began and {@link #update(GDSymbolTransaction trans) update}
     * is called.
     */
    public void update()
    {
        if(getContainingClass() != null)
        {
            SymbolTransaction trans = new SymbolTransaction(getContainingClass());
            update(trans);
        }
    }

    
    public String getCodeGenType()
    {
	return getCodeGenType(false);
    }

    public String getCodeGenType(boolean fullyQualified)
    {
        // if no return param, probably a Constructor; return empty string
        if (getAttribute() == null)
            return ""; // NOI18N
        
	if (fullyQualified) 
	{
	    if (codeGenTypeFullyQualified == null) 
	    { 
		codeGenTypeFullyQualified 
		    = GenCodeUtil.getCodeGenType
		    (getAttribute().getType(), 
		     GenCodeUtil.getCollectionOverrideDataTypes
		         (getAttribute().getMultiplicity(), fullyQualified),
		     isUseGenerics(),
		     getAttribute().getMultiplicity(),
		     fullyQualified,
		     getContainingClass());
	    }
	    return codeGenTypeFullyQualified;
	}
	else 
	{
	    if (codeGenTypeShort == null) 
	    { 
		codeGenTypeShort 
		    = GenCodeUtil.getCodeGenType
		    (getAttribute().getType(), 
		     GenCodeUtil.getCollectionOverrideDataTypes
		         (getAttribute().getMultiplicity(), fullyQualified),
		     isUseGenerics(),
		     getAttribute().getMultiplicity(),
		     fullyQualified,
		     getContainingClass());
	    }
	    return codeGenTypeShort;
	}
    }

    private String codeGenTypeFullyQualified = null;
    private String codeGenTypeShort = null;
    
    /**
     * Updates the data member using the specified Symbol transaction.
     * @param trans The transaction that is to be used to update the correct symbol.
     * @return The member transaction that was created to update the data member.
     */
    public MemberTransaction update(SymbolTransaction trans)
    {
        EventManager manager = EventManager.getEventManager();
        
        MemberTransaction retVal = new MemberTransaction(trans, this);
        IStructuralFeature attribute = retVal.getAttribute();
        
        if (attribute == null)
            return null;
        
        manager.getEventFilter()
            .blockEventType(ChangeUtils.RDT_DEPENDENCY_ADDED);
        
        try
        {
            if (retVal != null)
            {
                JavaAttributeChangeFacility facility = new JavaAttributeChangeFacility();
                
                if (getChangeType() == ElementInfo.DELETE)
                {
//                    if (attribute instanceof INavigableEnd)
//                    {
//                        IAssociation assoc = 
//                            ((INavigableEnd)attribute).getAssociation();
//                        
//                        ETList<IAssociationEnd> ends = assoc.getEnds();
//                        
//                        for(int i = 0 ; i < ends.size() ; i++)
//                        {
//                            ends.get(i).delete();
//                        }
//                        assoc.delete();
//                    }
//                
//                    else if (attribute instanceof IAttribute)
//                    {
//                       attribute.delete();
//                    }
                    
                    if(attribute instanceof IAttribute)
                    {
                       facility.delete((IAttribute)attribute, false, false);
                    }
                }
                else
                {
                    if (isCommentSet())
                    {
                        Log.out("Setting the comment ===============" // NOI18N
                            + getComment());
                        
                        attribute.setDocumentation(getComment());
                    }
                    
                    if (getModifiers() != null)
                    {
                        manager.updateMemberModifers(
                            retVal, getModifiers().intValue());
                    }
                    
                    if(getNewName() != null)
                    {
                        if(attribute instanceof IAttribute)
                        {
                            facility.changeName((IAttribute)attribute,
                                getNewName(), false, true);
                        }
                    }
                    
                    if (getNewType() != null)
                    {
                        Log.out("Setting the new member type ....."); // NOI18N

                        // CR 6435621 - cvc
                        // this was added because when an attibute's type
                        // was changed (in source code) from an object to a
                        // primitive (Integer to int for example), the 
                        // NavigableEnd link from the parent class to the 
                        // associated class was not being removed in the model,
                        // and the attribute was not added to the parent class
                        EventManager.getAttributeFacility().changeAttributeType(
                            (IAttribute) attribute,
                            trans.getSymbol(),
                            getName(),
                            JavaClassUtils.convertJavaToUML(
                                getNewQualifiedType()));
                        
                        manager.updateMemberType(
                            retVal, getNewQualifiedType(), getNewType());
                        //attribute.setType2(getNewType());
                        
                        if(attribute instanceof IAttribute)
                        {
                            RoundTripModeRestorer restorer = 
                                new RoundTripModeRestorer();
                            
                            restorer.setMode(RTMode.RTM_LIVE);
                            facility.typeChanged((IAttribute)attribute);
                            // IZ 80035: conover
                            // if there was a type change, there might
                            // have been a multiplicity change
                            facility.multiplicityChanged((IAttribute)attribute);
                            restorer.restoreOriginalMode();
                        }
                    }
                    
                    if (getInitializer() != null)
                    {
                        // Downcasting is okay for the attribute, because whoever
                        // sets it will have set it as either an IAttribute or an
                        // INavigableEnd, not a simple IStructuralFeature.
                        Log.out("setting default value for attribute to : " // NOI18N
                            + getInitializer());
                        
                        String initializer = getInitializer();
                        
                        Log.out("initializer : " + initializer); // NOI18N
                        
                        IAttribute att = null;
                        
                        if (attribute instanceof IAttribute)
                            att = (IAttribute)attribute;
                        
                        else if (attribute instanceof INavigableEnd)
                            att = (IAttribute)  attribute;
                        
                        if (att != null)
                        {
                            IMultiplicity mul = att.getMultiplicity();
                            
                            Log.out("range count : " // NOI18N
                                + mul.getRangeCount());
                            
                            ETList<IMultiplicityRange> ranges = mul.getRanges();
                            int count  = ranges.getCount();
                            
                            if (count > 0 )
                                setMultiplicityRanges(ranges, initializer);
                            
                            att.setDefault2(initializer);
                        }
                    }
                }
            }
        }
        
        catch (Exception ex)
        {
            Log.stackTrace(ex);
        }
        
        finally
        {
            manager.getEventFilter().unblockEventType(
                ChangeUtils.RDT_DEPENDENCY_ADDED);
        }
        
        return retVal;
    }

    public String getCode()
    {
        return "F"; // NOI18N
    }

    public String toString()
    {
        StringBuffer str = new StringBuffer("" + getType()); // NOI18N
        
        if (isCollectionType())
            str.append(" (").append(getCollectionOverrideDataType()).append(")"); // NOI18N
        
        str.append(" ").append(getName()); // NOI18N
        
        if (getInitializer() != null)
            str.append(" = ").append(getInitializer()); // NOI18N
        
        if (getModifiers() != null)
        {
            str.insert(0, " "); // NOI18N
            str.insert(0, Modifier.toString(getModifiers().intValue()));
        }
        
        return str.toString();
    }

    /**
     *  Strips out array specifiers from a given type name.
     * @param  name  The type name (possibly including array specifiers)
     * @return For "int[][]" returns "int"
     *         For "String" returns "String"
     */
    public static String getTypeName(String name) {
        if (name == null)
            return null;
        int arrPos = name.indexOf('[');
        return (arrPos == -1)? name : name.substring(0, arrPos).trim();
    }

    /**
     *  Returns the number of array specifiers that qualify the type name given.
     * @param name  The type name (including array specifiers)
     * @return The number of array specifiers. Ex: For "int[][]", returns 2.
     */
    public static int getMultiplicity(String name) {
        int count = 0, pos = -1;
        if (name != null) {
            while ((pos = name.indexOf('[', pos + 1)) != -1)
                count++;
        }
        return count;
    }

    public static int getMultiplicity(ITypedElement attr) {
        if (attr == null)
            return 0;
        return getMultiplicity(attr.getMultiplicity());
    }

    public static int getMultiplicity(IMultiplicity mul) {
        if (mul != null) {
            ETList<IMultiplicityRange> ranges = mul.getRanges();
            if (ranges != null) {
                if (ranges.size() == 1) {
                    IMultiplicityRange range = ranges.get(0);
                    String lower = range.getLower();
                    String upper = range.getUpper();
                    if (lower != null && lower.equals("1") &&
                        upper != null && upper.equals("1")) {
                        return 0;
                    }
                }
                return ranges.getCount();
            }
        }
        return 0;
    }

    public static void setMultiplicity(ITypedElement attr,
                                       int newmultiplicity,
                                       int oldmultiplicity) {
        if (newmultiplicity == oldmultiplicity)
            return ;

        IMultiplicity mul = attr.getMultiplicity();
        int diff          = newmultiplicity - oldmultiplicity;
        if (diff < 0) {
            ETList<IMultiplicityRange> ranges = mul.getRanges();
            if (ranges == null)
                return;
            int count  = ranges.getCount();

            for (int i = count - 1; i >= count + diff; --i) {
            	Log.out("Removing multiplicity range: " + ranges.item(i));
                mul.removeRange(ranges.item(i));
            }
        } else {
             StringBuffer range = new StringBuffer(diff * 7);
             for (int i = 0; i < newmultiplicity; ++i) {
                 if (i > 0)
                     range.append(",");
                 range.append("0..*");
             }
             Log.out("Setting multiplicity range to '" + range.toString()
                     + "'");
             mul.setRangeThroughString(range.toString());

             // <drumroll> Horrible kludge warning #8913891231 </drumroll>
             // Now go back through the ranges and explicitly set the upper bound
             // for each, since we still have not got a fix for the above code
             // not working.
             ETList<IMultiplicityRange> ranges = mul.getRanges();
             if (ranges != null && ranges.getCount() == newmultiplicity) {
                 for (int i = 0; i < newmultiplicity; ++i) {
                     ranges.item(i).setUpper("*");
                 }
             }
        }
    }

    public static String getArrayType(String type, int mul) {
         if (mul <= 0)
             return type;

         StringBuffer buf = (mul < cache.length)? cache[mul] : null;
         if (buf == null) {
             buf = new StringBuffer(mul * 2);
             for (int i = 0; i < mul; ++i)
                 buf.append("[]");
             if (mul < cache.length)
                 cache[mul] = buf;
         }
         return type += buf.toString();
     }
     
     public static String getArrayTypeName(ITypedElement el) {
     	String type = el.getType().getName();
		int mul = getMultiplicity(el);
		if (mul > 0) {
			StringBuffer arr = new StringBuffer(mul * 2);
			for (int i = 0; i < mul; ++i)
				arr.append("[]");
			type += arr;
		}
		return type;
     }

     private static void setMultiplicityRanges(ETList<IMultiplicityRange> ranges, String initializer){
        if(initializer.indexOf("[") == -1){
            for (int i = 0; i < ranges.getCount(); i++){
                ranges.item(i).setLower("0");
                ranges.item(i).setUpper("*");
            }
           return;
        }
        String r = initializer.substring(initializer.indexOf("["));
        StringTokenizer tok = new StringTokenizer(r, "]");
        String range = null,
               nextTok = null;
        for (int i = 0; i < ranges.getCount(); i++){
            try {
                 nextTok = tok.nextToken();
            }
            catch (Exception ex) {
            }
            if(nextTok == null)
                ranges.remove(i);
            else{
                range = nextTok.substring(nextTok.indexOf("[") + 1, nextTok.length()); // NOI18N
                Log.out("range val : " + range); // NOI18N
                Log.out("Lower val : " + ranges.item(i).getLower()); // NOI18N
                Log.out("Upper val : " + ranges.item(i).getUpper()); // NOI18N
                int lower = 0, upper = 0, rangeVal = 0;
                try {
                    lower = Integer.parseInt(ranges.item(i).getLower());
                }
                catch (Exception ex) {}

                try {
                    upper = Integer.parseInt(ranges.item(i).getUpper());
                }
                catch (Exception ex) {}

                try {
                    rangeVal = Integer.parseInt(range);
                }
                catch (Exception ex) {
                    ranges.item(i).setLower("0"); // NOI18N
                    ranges.item(i).setUpper("*"); // NOI18N
                }

                if((upper - lower) != rangeVal)
                    upper = rangeVal + lower;

                if(upper > 0 && rangeVal != 0){
                    upper--;
                    ranges.item(i).setLower(Integer.toString(lower));
                    ranges.item(i).setUpper(Integer.toString(upper));
                }
            }
        }
     }

     private static StringBuffer[] cache = new StringBuffer[4];

    public int getmodifierMask() {
        return modifierMask;
    }
    
    public void setmodifierMask(int modifierMask) {
        this.modifierMask = modifierMask;
    }

    public static String stripBrackets(String val)
    {
        return val.substring(0, val.indexOf('['));
    }


    //
    // added for template codegen
    //

    // see getCodeGenType() for how the type string is formed 
    public ArrayList<String[]> getReferredCodeGenTypes()
    {
	return GenCodeUtil
	    .getReferredCodeGenTypes(getAttribute().getType(), 
				     GenCodeUtil.getCollectionOverrideDataTypes
				         (getAttribute().getMultiplicity(), true),
				     isUseGenerics(),
				     getAttribute().getMultiplicity(),
				     getContainingClass());

    }


}
