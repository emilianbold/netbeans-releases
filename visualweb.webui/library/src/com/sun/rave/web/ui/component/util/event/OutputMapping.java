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
