/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.*;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorListener;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateFilter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateListener;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenFilter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenProcessor;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;

/**
 */
public class ParserBootstrap implements IParserBootstrap
{

    /**
     * Creates and initializes a parser that will be used to parse an operation.
     *
     * @param languageName [in] The language that is to be parsed.
     * @param type [in] The parser type.
     * @param **pParser [out] The initialized parser.
     *
     * @return 
     */
    public ILanguageParser initializeOperationParser(
        IFacilityProperties properties,
        String languageName,
        String type,
        IOpParserOptions options)
    {
        ILanguageManager langman = getLanguageManager();
        if (langman != null)
        {
            ILanguage lang = langman.getLanguage(languageName);
            return retrieveParser(lang, properties, type, true, options);
        }
        return null;
    }

    /**
     * Creates and initializes a parser.
     *
     * @param languageName [in] The language that is to be parsed.
     * @param type [in] The parser type.
     * @param **pParser [out] The initialized parser.
     *
     * @return 
     */
    public ILanguageParser initializeParser(
        IFacilityProperties properties,
        String languageName, 
        String type)
    {
        if (languageName == null || languageName.length() == 0)
            return null;
        
        ILanguageManager langman = getLanguageManager();
        if (langman != null)
        {
            ILanguage lang = langman.getLanguage(languageName);
            return retrieveParser(lang, properties, type, false, null);
        }
        return null;
    }

    /**
     * Retrieve the LanguageManager from the CoreProduct.
     */
    protected ILanguageManager getLanguageManager()
    {
        ICoreProduct prod = ProductRetriever.retrieveProduct();
        return prod != null? prod.getLanguageManager() : null;
    }

    /**
     * Creates and initializes a parser that will be used to parse an operation.  The parser
     * that is created is base on the filename.
     *
     * @param filename [in] The file to be parsed.
     * @param type [in] The parser type.
     * @param **pParser [out] The initialized parser.
     */
    public ILanguageParser initializeOperationParserForFile(
        IFacilityProperties properties,
        String filename,
        String type,
        IOpParserOptions options)
    {
        if (filename == null || filename.length() == 0)
            return null;
        
        // Retrieve the LanguageManager from the CoreProduct
        ILanguageManager langman = getLanguageManager();
        if (langman != null)
        {    
            ILanguage lang = langman.getLanguageForFile(filename);
            return retrieveParser(lang, properties, type, true, options);
        }
        return null;
    }

    /**
     * Creates and initializes a parser.  The parser that is created is base 
     * on the filename.
     *
     * @param filename [in] The file to be parsed.
     * @param type [in] The parser type.
     * @param **pParser [out] The initialized parser.
     */
    public ILanguageParser initializeParserForFile(
            IFacilityProperties props, String filename, String type)
    {
        if (filename == null || filename.length() == 0)
            return null;
        
        // Retrieve the LanguageManager from the CoreProduct
        ICoreProduct prod = ProductRetriever.retrieveProduct();
        if (prod != null)
        {
            ILanguageManager langman = prod.getLanguageManager();
            if (langman != null)
            {
                // Use the language manager to retrieve the parser.
                // If the file is not supported I do not want throw the exception.
                ILanguage lang = langman.getLanguageForFile(filename);
                if (lang != null)
                    return retrieveParser(lang, props, type, false, null);
            }
        }
        return null;
    }
    
    /**
     * Retrieves the parser from the language definition and initializes the
     * parsers ports.
     *
     * @param pLanguage [in] The langauge that specifies the parser to create.
     * @param pProperties [in] The properties to use when initializing the parser.
     * @param type [in] The type of parser to create.
     * @param operation [in] True if parsing operation, otherwise false.
     * @param pParser [out] The initialize parser.
     */
    protected ILanguageParser retrieveParser(ILanguage language,
                                             IFacilityProperties props,
                                             String type,
                                             boolean operation,
                                             IOpParserOptions options)
    {
        if (language == null)
            return null;
        
        ILanguageParser parser = language.getParser(type);
        if (parser != null)
        {
            String langname = language.getName();
            String keyPrefix = langname;
            if (operation)
                keyPrefix += "Operation";
            
            Map<String, Object> portMap = new HashMap<String, Object>();
            setStateListener(portMap, props, keyPrefix, parser);
            setTokenProcessor(portMap, props, keyPrefix, parser);
            setErrorListener(portMap, props, keyPrefix, parser);
            setStateFilter(portMap, props, keyPrefix, parser);
            setTokenFilter(portMap, props, keyPrefix, parser);
            
            if (operation && options != null)
                setOpOptions(portMap, props, keyPrefix, options);
        }
        return parser;
    }
    
    /**
     * Initializes the parser with the state listener.  The listener to use is
     * retrieved from the facilities property.  The property name is 
     * <Langauge>.StateListener.
     * 
     * @param pPortMap [in] A map of port implmentations.
     * @Param language [in] The parsers language.
     * @param pParser [in] The parser to initialize.
     */
    protected void setStateListener(
            Map<String, Object>         portMap,
            IFacilityProperties         props,
            String                      keyPrefix,
            ILanguageParser             parser)
    {
        if (parser == null || props == null) return;
        
        parser.setStateListener((IStateListener) getInstance(portMap, props, 
                                            keyPrefix + ".StateListener"));
    }
    
    /**
     * Initializes the parser with the token processor.  The token processor to use is
     * retrieved from the facilities property.  The property name is 
     * <Langauge>.TokenProcessor.
     * 
     * @Param language [in] The parsers language.
     * @param pParser [in] The parser to initialize.
     */
    protected void setTokenProcessor(
            Map<String, Object>         portMap,
            IFacilityProperties         props,
            String                      keyPrefix,
            ILanguageParser             parser)
    {
        if (parser == null || props == null) return;
        
        parser.setTokenProcessor((ITokenProcessor) getInstance(portMap, props, 
                keyPrefix + ".TokenProcessor"));
    }
    
    /**
     * Initializes the parser with the error listner.  The listener to use is
     * retrieved from the facilities property.  The property name is 
     * <Langauge>.ErrorListener.
     * 
     * @Param language [in] The parsers language.
     * @param pParser [in] The parser to initialize.
     */
    protected void setErrorListener(
            Map<String, Object>         portMap,
            IFacilityProperties         props,
            String                      keyPrefix,
            ILanguageParser             parser)
    {
        if (parser == null || props == null) return;
        
        parser.setErrorListener((IErrorListener) getInstance(portMap, props, 
                keyPrefix + ".ErrorListener"));
    }
    
    /**
     * Initializes the parser with the state filter.  The filter to use is
     * retrieved from the facilities property.  The property name is 
     * <Langauge>.StateFilter.
     * 
     * @Param language [in] The parsers language.
     * @param pParser [in] The parser to initialize.
     */
    protected void setStateFilter(
            Map<String, Object>         portMap,
            IFacilityProperties         props,
            String                      keyPrefix,
            ILanguageParser             parser)
    {
        if (parser == null || props == null) return;
        
        parser.setStateFilter((IStateFilter) getInstance(portMap, props, 
                keyPrefix + ".StateFilter"));
    }
    
    /**
     * Initializes the parser with the token filter.  The filter to use is
     * retrieved from the facilities property.  The property name is 
     * <Langauge>.TokenFilter.
     * 
     * @Param language [in] The parsers language.
     * @param pParser [in] The parser to initialize.
     */
    protected void setTokenFilter(
            Map<String, Object>         portMap,
            IFacilityProperties         props,
            String                      keyPrefix,
            ILanguageParser             parser)
    {
        if (parser == null || props == null) return;
        
        parser.setTokenFilter((ITokenFilter) getInstance(portMap, props, 
                keyPrefix + ".TokenFilter"));
    }
    
    /**
     * Initializes the parser with the token filter.  The filter to use is
     * retrieved from the facilities property.  The property name is 
     * <Langauge>.TokenFilter.
     * 
     * @Param language [in] The parsers language.
     * @param pParser [in] The parser to initialize.
     */
    protected void setOpOptions(
            Map<String, Object>         portMap,
            IFacilityProperties         props,
            String                      keyPrefix,
            IOpParserOptions            options)
    {
        IOperationParserOptionsHandler handler = 
                getInstance(portMap, props, keyPrefix + ".Options");
        if (handler != null)
            handler.setOptions(options);
    }
    
    protected <T> T getInstance(
            Map<String, Object>         portMap,
            IFacilityProperties         props,
            String                      key)
    {
        IFacilityProperty prop = props.get(key);
        if (prop != null)
        {
            String value = prop.getValue();
            if (value != null && value.length() > 0)
            {
                Object mapped = portMap.get(value);
                if (mapped != null)
                    return (T) mapped;
                else
                {
                    try
                    {
                        T list = (T) Class.forName(value).newInstance();
                        portMap.put(value, list);
                        return list;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }    
}
