package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IDataType;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.roundtripframework.ChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.RequestDetailKind;
import org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.JavaRequestProcessor;

public class JavaRequestProcessorTestCase extends AbstractUMLTestCase
{


/**
 * EnsureUniqueAttributeTestCase
 */
  public void testEnsureUniqueAttribute()
  {
	  // Create attribute, class and add attribute to the class
	  IClass cls = createClass("TestClass");
	  IAttribute attr = cls.createAttribute("int","TstAttr");
	  cls.addAttribute(attr);
	  
	  // Create another attribute and add it to the class
	  IAttribute newAttr = cls.createAttribute("int","TstAttr2");
	  cls.addAttribute(newAttr);
	  
	  // Check whether the attribute names are unique in the class
	  JavaRequestProcessor javaReqProc = new JavaRequestProcessor();
	  boolean uniqAttr = javaReqProc.ensureUniqueAttribute(cls,"TstAttr");
	  assertFalse(uniqAttr);
	  uniqAttr = javaReqProc.ensureUniqueAttribute(cls,"TstAttr1");
	  assertTrue(uniqAttr);
	  uniqAttr = javaReqProc.ensureUniqueAttribute(cls,"TstAttr2");
	  assertFalse(uniqAttr);
  }

/**
 * PreValidationCheckTestCase
 * This tests that no roundtrip processing should happen for data type request objects.
 */
  public void testPreValidationCheck()
  {
	  // Create a data type
	  IDataType dataType = createType("DataType");
	  dataType.setName("TestDataType");
	  
	  // Create change request and request validator for the data type.
	  IChangeRequest chngReq = new ChangeRequest();
	  chngReq.setState(0);
	  chngReq.setAfter(dataType);
	  IRequestValidator pRequest = new RequestValidator(chngReq);
	  
	  // Do the pre validation check for the data type. 
	  JavaRequestProcessor javaReqProc = new JavaRequestProcessor();
	  boolean validateChk = javaReqProc.preValidationCheck(pRequest);
	  assertFalse(validateChk);
	  
	  // Create attribute,  class and add the attribute to the class.
	  IClass newCls = createClass("TestClass");
	  IAttribute attr = newCls.createAttribute("int","NewAttr");
	  newCls.addAttribute(attr);
	  
	  // Create change request and request validator for the data type.
	  chngReq.setState(0);
	  chngReq.setAfter(attr);
	  pRequest = new RequestValidator(chngReq);
	  
	  // Do the pre validation check for the data type.
	  validateChk = javaReqProc.preValidationCheck(pRequest);
	  assertTrue(validateChk);
  }
  
/**
 * GetClassOfRequestTestCase
 * This method tests whether the request object is a data type or not. 
 */
  public void testGetClassOfRequest()
  {
	  // Create a data type
	  IDataType dataType = createType("DataType");
	  dataType.setName("TestDataType");
	  
	  // Create change request and request validator for the data type.
	  IChangeRequest chngReq = new ChangeRequest();
	  chngReq.setState(0);
	  chngReq.setAfter(dataType);
	  IRequestValidator requestVal = new RequestValidator(chngReq);
	  
	  // Do the pre validation check for the data type. 
	  JavaRequestProcessor javaReqProc = new JavaRequestProcessor();
	  IClassifier reqCls = javaReqProc.getClassOfRequest(requestVal.getRequest(),false);
	  assertTrue(reqCls instanceof IDataType);
	  
	  // Create a attribute, operation, classifier and add the attribute and operation to the class  
	  IClass tstCls = createClass("TestClass");
	  IAttribute tstAttr = tstCls.createAttribute("int","TestAttr");
	  tstCls.addAttribute(tstAttr);
	  IOperation tstOper = tstCls.createOperation("int","TestOper");
	  tstCls.addOperation(tstOper);
	  
	  // Create change request and request validator for the the classifier.
	  chngReq.setState(0);
	  chngReq.setAfter(tstAttr);
	  requestVal = new RequestValidator(chngReq);
	  reqCls = javaReqProc.getClassOfRequest(requestVal.getRequest(),false);
	  assertTrue(reqCls instanceof IClass);
  }

/**
 * CheckForInvalidNameTestCase
 */
  public void testCheckForInvalidName()
  {
	  
	  // Create a class and check invalid name for class
	  IClass tstCls = createClass("TestClass");
	  JavaRequestProcessor javaReqProc = new JavaRequestProcessor();
	  assertFalse(javaReqProc.checkForInvalidName(tstCls,"TestClass"));
	  assertTrue(javaReqProc.checkForInvalidName(tstCls,"public"));
	  assertTrue(javaReqProc.checkForInvalidName(tstCls,"int"));
	  assertTrue(javaReqProc.checkForInvalidName(tstCls,"boolean"));
	  
	  // Create a an attribute and check invalid name for attribute
	  IAttribute tstAttr =  tstCls.createAttribute("int","TestAttr");
	  assertFalse(javaReqProc.checkForInvalidName(tstAttr,"TestAttr"));
	  assertTrue(javaReqProc.checkForInvalidName(tstAttr,"private"));
	  // String should not be considered as data type
//	  assertTrue(javaReqProc.checkForInvalidName(tstAttr,"String"));
	  	  
	  // Create a an Operation and check invalid name for Operation
	  IOperation tstOper = tstCls.createOperation("int","TestOper");
	  assertFalse(javaReqProc.checkForInvalidName(tstOper,"TestOper"));
	  assertFalse(javaReqProc.checkForInvalidName(tstOper,"operation"));
	  assertTrue(javaReqProc.checkForInvalidName(tstOper,"protected"));
	  assertTrue(javaReqProc.checkForInvalidName(tstOper,"long"));
	  assertTrue(javaReqProc.checkForInvalidName(tstOper,"byte"));
	  
	  
	  // Create a Package and check invalid name for Package
	  IPackage tstPack = createType("Package");
	  assertFalse(javaReqProc.checkForInvalidName(tstPack,"TestPack"));
	  assertTrue(javaReqProc.checkForInvalidName(tstPack,"package"));
	  assertTrue(javaReqProc.checkForInvalidName(tstPack,"short"));
	  assertTrue(javaReqProc.checkForInvalidName(tstPack,"char"));
	  
	  // Create a Project and check invalid name for Project
	  IProject tstProject = createType("Project");
	  assertFalse(javaReqProc.checkForInvalidName(tstProject,"TestProject"));
	  assertTrue(javaReqProc.checkForInvalidName(tstProject,"TestProject~"));
	  
	  // Create a Parameter and check invalid name for Parameter
	  IParameter tstParam =createType("Parameter");
	  assertFalse(javaReqProc.checkForInvalidName(tstParam,"TestParam"));
	  assertTrue(javaReqProc.checkForInvalidName(tstParam,"default"));
	  assertTrue(javaReqProc.checkForInvalidName(tstParam,"float"));
	  assertTrue(javaReqProc.checkForInvalidName(tstParam,"synchronized"));
	  assertTrue(javaReqProc.checkForInvalidName(tstParam,"double"));
	  assertTrue(javaReqProc.checkForInvalidName(tstParam,"void"));

  }
/**
 * PostValidationCheckTestCase
 * This tests that no roundtrip processing should happen for unnamed Objects.
 */
  
  public void testPostValidationCheck()
  {
	  // Creating unnamed class
	  IClass cls = createClass("");
	  
	  // Create change request for the data type.
	  IChangeRequest chngReq = new ChangeRequest();
	  chngReq.setState(0);
	  chngReq.setBefore(cls);
	  chngReq.setAfter(cls);
	  chngReq.setRequestDetailType(RequestDetailKind.RDT_FEATURE_MOVED);
	  
	  // Doing the post validation check
	  JavaRequestProcessor javaReqProc = new JavaRequestProcessor();
	  assertFalse(javaReqProc.postValidationCheck(chngReq));
  }
}
