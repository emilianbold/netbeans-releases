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
package com.sun.rave.web.ui.component.util.event;


/**
 *  <P>	This class holds OutputMapping value meta information for individual
 *	instances of Handler Objects.  This information is necessary to provide
 *	the location to store the output value for a specific invocation of a
 *	handler.  This is data consists of the name the Handler uses for the
 *	output, the OutputType, and optionally the OutputType key to use when
 *	storing/retrieving the output value.</P>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class OutputMapping implements java.io.Serializable {

    /**
     *	<P> Constructor with targetKey as null.  This constructor will
     *	    throw an IllegalArgumentException if outputName or targetOutputType is
     *	    null.</P>
     *
     *	@param	outputName	    The name the Handler uses for output value
     *	@param	targetOutputType    OutputType that will store the output value
     *
     *	@see	OutputTypeManager
     *
     *	@throws	IllegalArumentException If outputName or targetOutputType is null
     */
    public OutputMapping(String outputName, String targetOutputType) {
	this(outputName, null, targetOutputType);
    }


    /**
     *	<P> Constructor with all values supplied as Strings.  This constructor
     *	    will throw an IllegalArgumentException if outputName or
     *	    targetOutputType is null.</P>
     *
     *	@param	outputName	    The name the Handler uses for output value
     *	@param	targetKey	    The key the OutputType will use
     *	@param	targetOutputType    OutputType that will store the output value
     *
     *	@see	OutputTypeManager
     *
     *	@throws	IllegalArumentException If outputName or targetOutputType is null
     */
    public OutputMapping(String outputName, String targetKey, String targetOutputType) {
	// Sanity checks...
	if ((outputName == null) || (outputName.length() == 0)) {
	    throw new NullPointerException("'outputName' is required!");
	}
	if (targetOutputType == null) {
	    throw new NullPointerException("'targetOutputType' is required!");
	}
	_outputName = outputName;
	_targetKey = targetKey;
	_targetOutputType = targetOutputType;
    }

    /**
     *	<P> Constructor with all the values passed in.  This constructor will
     *	    throw an IllegalArgumentException if outputName or
     *	    targetOutputType is null.</P>
     *
     *	@param	outputName	    The name the Handler uses for output value
     *	@param	targetKey	    The key the OutputType will use
     *	@param	targetOutputType    OutputType that will store the output value
     *
     *	@see	OutputTypeManager
     *
     *	@throws	IllegalArumentException	If outputName or
     *	    targetOutputType is null
    public OutputMapping(String outputName, String targetKey, OutputType targetOutputType) {
	_outputName = outputName;
	_targetKey = targetKey;
	_targetOutputType = targetOutputType;
    }
     */

    /**
     *	Accessor for outputName.
     */
    public String getOutputName() {
	return _outputName;
    }

    /**
     *	Accessor for targetKey.
     */
    public String getOutputKey() {
	return _targetKey;
    }

    /**
     *	Accessor for targetOutputType.
     */
    public OutputType getOutputType() {
	return OutputTypeManager.getInstance().getOutputType(_targetOutputType);
    }


    private String	_outputName = null;
    private String	_targetKey  = null;
    private String	_targetOutputType = null;
}
