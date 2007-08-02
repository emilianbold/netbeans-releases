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

import java.util.List;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ClassLoaderListener;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IClassLoaderListener;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParser;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ProcessTypeKind;
import org.netbeans.modules.uml.core.roundtripframework.codegeneration.IParseInformationCache;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.IADProduct;

/**
 */
public class UMLParserUtilities
{
    public static final String PACKAGE_SEPARATOR = "::";

    // Element range specification
    public static final int ER_ENTIRE_ELEMENT = 0;
    public static final int ER_ELEMENT_HEAD   = 1;

    /** 
     * Retrieves an Element's Token Descriptors which contains file position
     * information for the Element.
     * 
     * @param pElement 
     * @param ppTokenDescriptors 
     * 
     * @return 
     */
    public static ETList<ITokenDescriptor> getElementTokenDescriptors(IElement element)
    {
        ISourceFileArtifact sfa = getSourceFileArtifact(element);
        if (sfa != null)
        {    
            IParseInformationCache pcache = getParseInformationCache();
            if (pcache != null)
            {
                IFileInformation fi = pcache.getParseInformation(sfa);
                if (fi != null)
                    return findElementTokenDescriptors(element, fi);
            }
        }
        return null;
    }
    
    /** 
     * returns the line number that the given element @a pElement starts on
     * 
     * @param pElement[in] the element whose starting line number you want
     * @param pLineNumber[out] the line number
     * 
     * @return HRESULT
     */
    public static int getElementStartLineNumber(IElement el)
    {
        ETList<ITokenDescriptor> toks = getElementTokenDescriptors(el);
        int line = 0;
        if (toks != null)
        {
            for (int i = 0, count = toks.size(); i < count; ++i)
            {
                ITokenDescriptor t = toks.get(i);
                if ("StartPosition".equals(t.getType()))
                    line = t.getLine();
            }
        }
        return line;
    }
    
    /** 
     * gets the source file name of a given element
     * 
     * @param pElement[in] the element whose source file name you want
     * @param fileName[out] the name of the source file that contains the element.
     * 
     * @return HRESULT
     */
    public static String getElementSourceFileName(IElement el)
    {
        ISourceFileArtifact art = getSourceFileArtifact(el);
        return art != null? art.getSourceFile() : null;
    }
    
    /** 
     * returns a UML Parser
     * 
     * @param ppParser[out] a UML Parser object
     * 
     * @return HRESULT
     */
    public static IUMLParser getUMLParser()
    {
        ICoreProduct product = ProductRetriever.retrieveProduct();
        if (product != null)
        {
            IFacilityManager fman = product.getFacilityManager();
            // AZTEC: TODO: Might we need to give the fully-qualified class name
            //              here? It'd be preferable to look that up.
            IFacility fac = fman.retrieveFacility("Parsing.UMLParser");
            if (fac instanceof IUMLParser)
                return (IUMLParser) fac;
        }
        return null;
    }
    
    /** 
     * Parses a Source File Artifact and returns the parse information.
     * 
     * @param pSourceFileArtifact[in] the source file artifact to parse
     * @param ppResults[out] the parse information for the source file artifact
     * 
     * @return HRESULT
     */
    public static IFileInformation parseSourceFileArtifact(
            ISourceFileArtifact art)
    {
        // Slurp the whole thing in and pray that we have enough memory.
        String source = art.getSourceCode();
        
        if (source != null && source.length() > 0)
        {
            ILanguage lan = art.getLanguage();
            if (lan != null)
                return parseText(source, lan.getName(), false, 
                                 null, null, null);
        }
        return null;
    }
    
    /** 
     * returns the start position of the ParserData object passed in.  If the parser data object
     * has a comment, then the comment is considered the start position.  If no comment is found,
     * the parser data's StartPosition token descriptor's position is considered the start position
     * 
     * @param pParserData[in] could be an attribute, an operation, a class, etc.
     * @param pStartPosition[out] the start position of @a pParserData
     * 
     * @return HRESULT
     */
    public static long getStartPosition(IParserData pdata)
    {
        ITokenDescriptor desc = pdata.getTokenDescriptor("Comment");
        if (desc == null)
            desc = pdata.getTokenDescriptor("StartPosition");
        return desc != null? desc.getPosition() : -1;
    }
    
    /** 
     * Returns an IParserData's EndPosition.
     * 
     * @param pParserData[in] the parser data derived class whose end position you want.
     *                        Could be an IREClass, an IREOperation, etc.
     * @param pEndPosition[out] the parser data's end position (if it has one).
     * 
     * @return HRESULT
     */
    public static long getEndPosition(IParserData pdata)
    {
        return getTokenDescriptorEndPosition(pdata, "EndPosition");
    }
    
    /** 
     * Returns the sum of the file offset and length  of a TokenDescriptor named @a
     * tokenDescriptorName
     * 
     * @param pParserData[in] the ParserData object that you want token descriptors for
     * @param tokenDescriptorName[in] the name of the TokenDescriptor (e.g. StartPosition, EndPosition)
     * @param pPosition[out] the file offset of the TokenDescriptor's end position
     * 
     * @return HRESULT
     */
    public static long getTokenDescriptorEndPosition(IParserData pdata, 
                                                    String tag)
    {
        ITokenDescriptor desc = pdata.getTokenDescriptor(tag);
        if (desc != null)
        {
            long pos = desc.getPosition();
            if (pos != -1)
                pos += desc.getLength();
            return pos;
        }
        return -1;
    }
    
    /** 
     * Finds the IOperation that matches the IREOperation @a pREOperation
     * 
     * @param pClassifier[in] the classifier that owns the IOperation
     * @param pREOperation[in] the IREOperation you're searching for.
     * @param ppOperation[out] the IOperation that matches @a pREOperation (if it exists)
     * 
     * @return HRESULT
     */
    public static IOperation findMatchingOperation(IClassifier c,
                                                   IREOperation reo)
    {
        ETList<IOperation> ops = c.getOperations();
        if (ops != null)
        {
            for (int i = 0, count = ops.size(); i < count; ++i)
            {
                IOperation op = ops.get(i);
                if (isMatchingOperation(op, reo))
                    return op;
            }
        }
        return null;
    }
    
    /** 
     * returns the IAttribute that matches the IREAttribute @a pREAttribute
     * 
     * @param pClassifier[in] the classifier that owns the attribute
     * @param pREAttribute[in] the IREAttribute that you're searching for
     * @param ppAttribute[out] the IAttribute that matches @a pREAttribute (if it exists)
     * 
     * @return HRESULT
     */
    public static IAttribute findMatchingAttribute(IClassifier c,
                                                   IREAttribute rat)
    {
        String name = rat.getName();
        IAttribute att = c.getAttributeByName(name);
        if (att == null)
        {
            ETList<INavigableEnd> ends = c.getOutboundNavigableEnds();
            if (ends != null)
            {    
                for (int i = 0, count = ends.size(); i < count; ++i)
                {
                    INavigableEnd end = ends.get(i);
                    if (end == null) continue;
                    
                    if (end instanceof IAttribute && name.equals(end.getName()))
                    {
                        att = (IAttribute) end;
                        break;
                    }
                }
            }
        }
        return att;
    }
    
    /** 
     * returns the number of non-return type parameters for @a pOperation
     * 
     * @param pOperation[in] the operation whose parameter count you want
     * @param pNumParameters[out] the number of parameters
     * 
     * @return HRESULT
     */
    public static int getParameterCount(IOperation op)
    {
        ETList<IParameter> pars = op.getParameters();
        if (pars == null) return 0;
            
        int parcount = 0;
        for (int i = 0, count = pars.size(); i < count; ++i)
            if (pars.get(i).getDirection() != BaseElement.PDK_RESULT)
                parcount++;
        return parcount;
    }
    
    /** 
     * Returns the Nth parameter for the given operation.
     * 
     * @param pOperation[in] the operation whose Nth parameter you want.
     * @param index[in] this is "N" (starts from 0).
     * @param ppParameter[out] the Nth parameter
     * 
     * @return HRESULT
     */
    public static IParameter getNthParameter(IOperation op, int n)
    {
        ETList<IParameter> pars = op.getParameters();
        if (pars == null) return null;
            
        int parcount = 0;
        for (int i = 0, count = pars.size(); i < count; ++i)
        {    
            IParameter par = pars.get(i);
            if (par.getDirection() != BaseElement.PDK_RESULT && parcount++ == n)
                return par;
        }
        return null;
    }
    
    /** 
     * returns the number of non-return type parameters for @a pREOperation
     * 
     * @param pREOperation[in] the operation whose parameter count you want
     * @param pNumREParameters[out] the number of parameters
     * 
     * @return HRESULT
     */
    public static int getParameterCount(IREOperation op)
    {
        ETList<IREParameter> pars = op.getParameters();
        if (pars == null) return 0;
            
        int parcount = 0;
        for (int i = 0, count = pars.size(); i < count; ++i)
            if (pars.get(i).getKind() != BaseElement.PDK_RESULT)
                parcount++;
        return parcount;
    }

    /** 
     * Returns the Nth parameter for the given operation.
     * 
     * @param pREOperation[in] the operation whose Nth parameter you want.
     * @param index[in] this is "N"
     * @param ppREParameter[out] the Nth parameter
     * 
     * @return HRESULT
     */
    public static IREParameter getNthParameter(IREOperation op, int n)
    {
        ETList<IREParameter> pars = op.getParameters();
        if (pars == null) return null;
            
        int parcount = 0;
        for (int i = 0, count = pars.size(); i < count; ++i)
        {    
            IREParameter par = pars.get(i);
            if (par.getKind() != BaseElement.PDK_RESULT && parcount++ == n)
                return par;
        }
        return null;
    }
    
    public static SourceCodeRange getElementRange(IParserData pdata,
                                                  int elementRange)
    {
        SourceCodeRange range = new SourceCodeRange();
        range.begin = getStartPosition(pdata);
        
        String endTag = getTokenDescriptorEndTag(pdata, elementRange);
        if (endTag != null)
            range.end = getTokenDescriptorEndPosition(pdata, endTag);
        return range;
    }
    
    /** 
     * Returns the token descriptor that points to the "end" of @a
     * pParserData.  What "end" means is determined by @a elementRange and
     * by what kind of object @a pParserData points to.
     * 
     * @param pParserData[in] the IParserData object that you want the
     * TokenDescriptor end tag for.
     * 
     * @param elementRangeSpec[in] what tag is desired (see enum in header file)
     * @param endTag[out] the name of the "end tag".  This string can be
     *                    passed to IParserData::GetTokenDescriptor()
     * 
     * @return HRESULT
     */
    private static String getTokenDescriptorEndTag(IParserData pdata, int range)
    {
        if (range == ER_ELEMENT_HEAD)
        {    
            // For a classifier, the end tag for a head is different than a method.
            // ER_ELEMENT_HEAD is not relevant to IAttributes
            if (pdata instanceof IREOperation)
                return "OpHeadEndPosition";
            else if (pdata instanceof IREClass)
                return "ClassHeadEndPosition";
        }
        return "EndPosition";
    }

    private static IFileInformation parseText(
            String text,
            String language,
            boolean parseFragment,
            ETList<IREOperation> topLevelOps,
            ETList<IREAttribute> topLevelAttribs,
            ETList<IErrorEvent>  errors)
    {
        IFileInformation fileInf = null;
        int parseKind = parseFragment? ProcessTypeKind.PTK_PROCESS_FRAGMENT
                                     : ProcessTypeKind.PTK_PROCESS_FILE;
        IUMLParser parser = getUMLParser();
        if (parser != null)
        {
            IUMLParserEventDispatcher disp = parser.getUMLParserDispatcher();
            if (disp != null)
            {
                IClassLoaderListener list = new ClassLoaderListener();
                disp.registerForUMLParserEvents(list, null);
                if (parseFragment)
                    disp.registerForUMLParserAtomicEvents(null, null);
                parser.processStreamByType(text, language, parseKind);
                disp.revokeUMLParserSink(list);
                
                fileInf = list.getFileInformation();
                if (parseFragment)
                {
                    disp.revokeUMLParserAtomicSink(list);
                    if (topLevelAttribs != null)
                        topLevelAttribs.addAll( list.getTopLevelAttributes() );
                    if (topLevelOps != null)
                        topLevelOps.addAll( list.getTopLevelOperations() );
                }
                
                // If caller is interested in errors, return them.
                if (errors != null)
                    errors.addAll( fileInf.getErrors() );
            }
        }
        return fileInf;
    }
    
    /** 
     * Parses a source file and stores the results in @a pResults
     * 
     * @param fileName[in] the source file to parse
     * @param pResults[out] the results of the parse operation  
     * 
     * @return HRESULT
     */
    public static IFileInformation parseFile(String filename)
    {
        IUMLParser parser = getUMLParser();
        if (parser != null)
        {
            IUMLParserEventDispatcher disp = parser.getUMLParserDispatcher();
            if (disp != null)
            {
                IClassLoaderListener list = new ClassLoaderListener();
                disp.registerForUMLParserEvents(list, filename);
                parser.processStreamFromFile(filename);
                return list.getFileInformation();
            }
        }
        return null;
    }
    
    /** 
     * Returns a collection of ITokenDescriptor objects for a particular Element
     * 
     * @param pElement[in] the element whose token descriptors you want
     * @param results[in] a list of parse results.  This list indirectly contains
     *                    TokenDescriptors for all parsed items (e.g. attributes,
     *                    classes, etc.).
     * @param ppTokenDescriptors[out] a collection of TokenDescriptor objects
     * 
     * @return HRESULT
     */
    private static ETList<ITokenDescriptor> findElementTokenDescriptors(
            IElement el, IFileInformation fi)
    {
        IClassifier c = getClassifierFromElement(el);
        if (c != null)
        {
            IREClass rec = getREClassFromClassifier(c, fi);
            if (rec != null)
                return findElementTokenDescriptors(el, rec);
        }
        return null;
    }
    
    /** 
     * Returns a collection of ITokenDescriptor objects for a particular Element
     * 
     * @param pElement[in] the element whose token descriptors you want
     * @param pOwningClass[in] the REClass object that owns or matches @a pElement
     * @param ppTokenDescriptors[out] a collection of TokenDescriptor objects
     * 
     * @return HRESULT
     */
    private static ETList<ITokenDescriptor> findElementTokenDescriptors(
            IElement el, IREClass owningClass)
    {
        IParserData pdata = null;
        if (el instanceof IClassifier)
            pdata = owningClass;
        else if (el instanceof IAttribute &&
            (pdata = 
               findMatchingREAttribute(owningClass, (IAttribute) el)) != null)
            ;
        else if (el instanceof IOperation &&
            (pdata =
               findMatchingREOperation(owningClass, (IOperation) el)) != null)
            ;
        else if (el instanceof IParameter)
        {
            // Get the parameter's owning operation
            IBehavioralFeature owningF = 
                    ((IParameter) el).getBehavioralFeature();
            if (owningF instanceof IOperation)
                pdata = 
                    findMatchingREOperation(owningClass, (IOperation) owningF);
        }
        
        return pdata != null? pdata.getTokenDescriptors() : null;
    }
    
    /** 
     * given an IClassifier, this method returns the IREClass object that matches the IClassifier
     * 
     * @param pClassifier 
     * @param ppREClass 
     *
     * @warning Does not handle namespaces yet
     * @return HRESULT
     */
    public static IREClass getREClassFromClassifier(IClassifier c, IFileInformation fi)
    {
        String cp = getNestedClassPath(c);
        return getREClassFromClassPath(cp, fi);
    }
    
    /** 
     * Given a Class Name or Class Path (e.g. A::B::C), this operation
     * looks up an IREClass by that name in the IFileInformation
     * collection.
     * 
     * @param classPathBSTR[] 
     * @param pResults[] 
     * @param ppREClass[] 
     * 
     * @return 
     */
    private static IREClass getREClassFromClassPath(String cp, IFileInformation fi)
    {
        ETPairT<String,String> sp = StringUtilities.removeToken(cp, PACKAGE_SEPARATOR);
        String outerCN = sp.getParamOne();
        cp = sp.getParamTwo();

        // Get the number of top level classes
        int numClasses = fi.getTotalClasses();

        // If any classes were found...
        if (numClasses > 0)
        {
            if (outerCN != null && outerCN.length() > 0)
            {
                for (int i = 0; i < numClasses; ++i)
                {
                    IREClass rec = fi.getClass(i);
                    if (rec != null)
                    {    
                        String recName = rec.getName();
                        if (outerCN.equals(recName))
                        {
                            return cp.length() == 0? rec : 
                                                   findMatchingREClass(rec, cp);
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /** 
     * Searches @a pREClass for an REOperation that matches @a pOperation and returns it if found.
     * 
     * @param pREClass[in] the class that is being searched
     * @param pOperation[in] the operation used to find the matching REOperation
     * @param ppREOperation[out] the matching operation (if found).
     * 
     * @return HRESULT
     */
    public static IREOperation findMatchingREOperation(IREClass c, 
                                                        IOperation op)
    {
        ETList<IREOperation> ops = c.getOperations();
        if (ops != null)
        {
            for (int i = 0, count = ops.size(); i < count; ++i)
            {
                IREOperation reop = ops.get(i);
                if (reop == null) continue;
                
                if (isMatchingOperation(op, reop))
                    return reop;
            }
        }
        return null;
    }
    
    /** 
     * determines if two operations (an IOperation and an IREOperation) match
     * 
     * @param pOperation[in] the IOperation object
     * @param pOperationRE[in] the IREOperation object
     * @param isMatching[out]
     *                        - true if they match
     *                        - false if they don't match
     * 
     * @return 
     */
    private static boolean isMatchingOperation(IOperation op, IREOperation reo)
    {
        String name = op.getName();
        if (name == null || !name.equals(reo.getName())) return false;
        
        ETList<IParameter> pars = op.getParameters();
        ETList<IREParameter> repars = reo.getParameters();
        
        if (op.getReturnType().getName().equals("")) {
            for (int i = 0, count = pars.size(); i < count; ++i)
            {
                if (pars.get(i).getDirection() == BaseElement.PDK_RESULT) {
                    String returnType = XMLManip.getAttributeValue(pars.get(i).getDOM4JNode(), "type");
                    if (returnType.indexOf(".") != -1)
                        returnType = returnType.substring(returnType.lastIndexOf(".")+1) ;
                    
                    op.setReturnType2(returnType);
                }
            }
            
        }
        
        if ((pars == null || pars.size() == 0) && 
                (repars == null || repars.size() == 0))
            return true;
        
        if ((pars == null) != (repars == null) ||
                pars.size() != repars.size())
            return false;
        
        for (int i = 0, count = pars.size(); i < count; ++i)
        {
           if (pars.get(i).getDirection() == BaseElement.PDK_RESULT) continue;           
           if(!isMatchingParameter(op,pars.get(i),repars.get(i),false))
              return false;
        }
        return true;
    }
    
    private static boolean matchTemplateParameter(String p1, String p2) {
        
        // looking for a pattern like
        // p1->  MyClass <MyTemplate>
        // p2->  MyClass
        
        int i = p1.indexOf("<");
        if (i < 0) return false;
        
        String tmp1 = p1.substring(0,i);
        tmp1 = tmp1.trim() ;
        
        int j = p2.indexOf("<");
        String tmp2 = p2 ;
        if (j > 0) {
            tmp2 = p2.substring(0,j);
            tmp2 = tmp2.trim() ;
        }
        
        if (tmp1.equals(tmp2)) return true ;
        
        return false;
        
    }
    
    protected static boolean matchCollectionParameter(IParameter parameter, 
                                                      IREParameter parameterRE)
    {
        boolean retVal = false;
        
        if(parameterRE.isTemplateType() == true)
        {
            ILanguage lang = parameter.getLanguages().get(0);
            if(parameterRE.isCollectionType(lang) == true)
            {
                CollectionInformation info = parameterRE.getCollectionTypeInfo();
                String paramType = parameter.getTypeName();
                String reParamType = info.getTypeName();
                
                if(paramType.equals(reParamType) == true)
                {
                    retVal = true;
                    
                    IMultiplicity mult = parameter.getMultiplicity();
                    if(info.getNumberOfRanges() == mult.getRangeCount())
                    {
                        List<IMultiplicityRange> ranges = mult.getRanges();
                        for(int index = 0; index < info.getNumberOfRanges(); index++)
                        {
                            String paramColType = ranges.get(index).getCollectionType(false);
                            String reColType = info.getCollectionForRange(index);
                            
                            if(paramColType.equals(reColType) == false)
                            {
                                retVal = false;
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        return retVal;
    }
   /** 
    * Determines if the IParameter @a pParameter matches the IREParameter
    * @a pParameterRE.  The method used for detecting a match depends on
    * the value of @a strictMatch as follows:
    *
    * strictMatch == VARIANT_TRUE - @a pParameterRE's name must be identical
    *                               to @a pParameter's name
    *
    * strictMatch == VARIANT_FALSE - If pParameterRE's name is not  identical
    *                                to @a pParameter's name, then a match is
    *                                detected if the operation that pParameter
    *                                belongs to is a setter.
    * 
    * @param pOperation[in] operation that owns pParameter
    * @param pParameter[in] the IParameter to test for a match
    * @param pParameterRE[in] the IREParameter to test for a match.
    * @param strictMatch[in] VARIANT_TRUE if a strict match should be performed.
    */
    protected static boolean isMatchingParameter(IOperation operation, IParameter parameter, 
         IREParameter parameterRE, boolean strictMatch)
    {
       boolean isMatching = false;
       
       IClassifier type = parameter.getType();
       String qualifiedParameterType = null;
       
       if(type != null)
       {
          qualifiedParameterType = type.getFullyQualifiedName(false);
       }
       
       String parameterType = parameter.getTypeName();
       String parameterTypeRE = parameterRE.getType();
       
       // If the parameters' type names are the same, that's enough to
       // satisfy a "strict" match.  See if the qualified name or the
       // un-qualified name match the REParameter's name
       if(parameterType.equals(parameterTypeRE) || qualifiedParameterType.equals(parameterTypeRE))
       {
          isMatching = true;
       }
       else if (matchTemplateParameter(parameterType,parameterTypeRE)) {
           isMatching = true;
       }
       else if(matchCollectionParameter(parameter, parameterRE) == true)
       {
           isMatching = true;
       }
       else
       {
          int pos = parameterTypeRE.lastIndexOf("::");
          
          if(pos != -1)
          {
             String name = parameterTypeRE.substring(pos+1);
             
             if(name.equals(parameterType))
             {
                isMatching = true;
             }
          }
          
          if(!isMatching)
          {
             // The parameters' type names were different.  If the caller has
             // requested a non-strict match try to make the non-strict
             // match, otherwise, these parameters don't match.
             if(!strictMatch)
             {
                // Non-strict matching is, at this time, only relevent for
                // setters.  If we make it to this point in the code and
                // pOperation is a setter, we consider it to be a matching
                // parameter.
                isMatching = isSetter(operation);
             }
          }
       }
       
       return isMatching;
    }
    
    /** 
     * Determines if @a pOperation is a setter operation.
     */
    public static boolean isSetter(IOperation operation)
    {
       IAttribute attribute = getSetterAttribute(operation);
       
       return (attribute == null) ? false : true;
    }
    
    public static IAttribute getSetterAttribute(IOperation operation)
    {
       ETList<IDependency> supplierDependencies = operation.getSupplierDependencies();
       
       if(supplierDependencies.getCount() > 0)
       {
          IDependency supplierDependency = supplierDependencies.item(0);
          
          if(supplierDependency != null)
          {
             INamedElement clientElement = supplierDependency.getClient();
             
             if(clientElement instanceof IAttribute)
                return (IAttribute)clientElement;
          }
       }
       return null;
    }
    
    
    /** 
     * Searches @a pREClass for an REAttribute that matches @a pAttribute and returns it if found.
     * 
     * @param pREClass[in] the class that is being searched
     * @param pAttribute[in] the attribute used to find the matching REAttribute
     * @param ppREAttribute[out] the matching attribute (if found).
     * 
     * @return HRESULT
     */
    private static IREAttribute findMatchingREAttribute(IREClass c, 
                                                        IAttribute attr)
    {
        String name = attr.getName();
        ETList<IREAttribute> ratts = c.getAttributes();
        if (ratts != null)
        {    
            for (int i = 0, count = ratts.size(); i < count; ++i)
            {
                IREAttribute rat = ratts.get(i);
                if (rat == null) continue;
                
                if (name != null && name.equals(rat.getName()))
                    return rat;
            }
        }
        return null;
    }
    
    /** 
     * Finds the REClass object (nested or not) refered to by nestedClassPath
     * 
     * @param pSearchOrigin[in] which class the search should start from.
     *                          If this class does not match @a nestedClassPath,
     *                          its inner classes are searched recursively for
     *                          @a nestedClassPath.
     *
     * @param  nestedClassPath[] @param ppREClass[]
     * 
     * @return HRESULT
     */
    private static IREClass findMatchingREClass(IREClass searchOrigin,
                                                String nestedClassPath)
    {
        if (nestedClassPath == null || nestedClassPath.length() == 0)
            return searchOrigin;
        else
        {
            ETPairT<String,String> tokS = 
                StringUtilities.removeToken(nestedClassPath, PACKAGE_SEPARATOR);
            String className = tokS.getParamOne();
            nestedClassPath  = tokS.getParamTwo();
            
            if (className != null && className.length() > 0)
            {    
                ETList<IREClass> recls = searchOrigin.getAllInnerClasses();
                if (recls != null)
                {
                    for (int i = 0, count = recls.size(); i < count; ++i)
                    {
                        IREClass rec = recls.get(i);
                        if (rec == null) continue;
                        
                        if (className.equals(rec.getName()))
                            return findMatchingREClass(rec, nestedClassPath);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * returns the portion of the classifier's fully qualified name that is made up
     * of classifiers.
     *
     * For example:
     *
     * Let's say that class ClassC is in the following hierarchy:
     *
     *    PackageA::PackageB::ClassA::ClassB::ClassC
     *
     * This operation would return:
     *
     *    ClassA::ClassB::ClassC
     * 
     * @param pClassifier[in] the classifier whose nested class path you want
     * @param nestedClassPath[out] the portion of the classifier's fully qualified
     *                             name that is made up of classifiers.
     * 
     * @return HRESULT
     */
    private static String getNestedClassPath(IClassifier c)
    {
        INamespace current = c;
        StringBuffer path  = new StringBuffer();
        while (current instanceof IClassifier)
        {
            // if we're looking at a class (as opposed to a package)
            String name = current.getName();
            if (path.length() > 0)
            {
                // prepend the ::
                path.insert(0, PACKAGE_SEPARATOR);
            }
            
            // prepend the name 
            path.insert(0, name);
            
            IElement owner = current.getOwner();
            if (owner instanceof INamespace)
                current = (INamespace) owner;
        }
        return path.toString();
    }
    
    /** 
     * If @a pElement is a Classifier, this method returns pElement (casted to a pClassifier).
     * Otherwise, the Classifier that owns @a pElement is returned.
     * 
     * @param pElement[in] the element whose Classifier you want.
     * @param ppClassifier[out] the Classifier
     * 
     * @return HRESULT
     */
    public static IClassifier getClassifierFromElement(IElement el)
    {
        if (el instanceof IClassifier)
            return (IClassifier) el;
        else if (el instanceof INavigableEnd)
            return ((INavigableEnd) el).getReferencingClassifier();
        else
        {    
            IElement owner = el.getOwner();
            if (owner != null)
            {
                if (owner instanceof IClassifier)
                    return (IClassifier) owner;
                
                if (owner instanceof IOperation)
                {
                    IElement grandOwner = owner.getOwner();
                    if (grandOwner instanceof IClassifier)
                        return (IClassifier) grandOwner;
                }
            }
        }
        return null;
    }
    
    /** 
     * Returns the GLOBAL ParserInformationCache object
     * 
     * @param ppParseInformationCache[out] the GLOBAL ParserInformationCache
     * 
     * @return HRESULT
     */
    public static IParseInformationCache getParseInformationCache()
    {
        IADProduct product = getADProduct();
        return product != null? product.getParseInformationCache() : null;
    }
    
    /** 
     * returns the one and only IADProduct object
     * 
     * @param ppADProduct[out] the IADProduct object
     * 
     * @return HRESULT
     */
    public static IADProduct getADProduct()
    {
        return (IADProduct) ProductRetriever.retrieveProduct();
    }
    
    /** 
     * Returns an element's source file artifact.  If the element has
     * multiple source files, only the first is returned.
     * 
     * @param pElement[] 
     * @param ppSourceFileArtifact[] 
     * 
     * @return HRESULT
     */
    public static ISourceFileArtifact getSourceFileArtifact(IElement el)
    {
        ETList<IElement> els = el.getSourceFiles();
        if (els != null && els.size() > 0)
        {
            IElement first = els.get(0);
            if (first instanceof ISourceFileArtifact)
                return (ISourceFileArtifact) first;
        }
        return null;
    }
    
    public static class SourceCodeRange
    {
        public long begin = -1, end = -1;
        
        public boolean isValid()
        {
            return begin != -1 && end != -1;
        }
    }
}