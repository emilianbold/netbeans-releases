
package org.netbeans.modules.uml.core.coreapplication;

import org.netbeans.modules.uml.core.coreapplication.CoreProduct;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.IProductDescriptor;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.ADProduct;

import junit.framework.TestCase;

/**
 *
 */
public class CoreProductManagerTestCase extends TestCase
{
	private ICoreProductManager manager = null;
	public CoreProductManagerTestCase()
	{
		super();		
	}
	
	protected void setUp()
	{
		manager = CoreProductManager.instance(); 
	}
	
	public void testSetCoreProduct()
	{
        ICoreProduct oldProduct = manager.getCoreProduct();
		ICoreProduct prod = new ADProduct();
		manager.setCoreProduct(prod);
		ICoreProduct prod1 = manager.getCoreProduct();
		assertEquals(prod,prod1);
        
        manager.setCoreProduct(oldProduct);
	}
	
	public void testSetProductAlias()
	{
		String alias = "NewProd";
		manager.setProductAlias(alias);
		assertEquals(manager.getProductAlias(),alias);
	}
	
	public void testGetProductManagerOnROT()
	{
		ICoreProductManager mana = manager.getProductManagerOnROT();
		assertEquals(manager,mana);
	}
	
	public void testGetProducts()
	{
// TODO: cvc - re-enable - for some reason, test classes are 
//       being compiled using -source 1.4
//		ETList<IProductDescriptor> prods = manager.getProducts();
		ETList prods = manager.getProducts();
		manager.removeFromROT();
		prods = manager.getProducts();
	}
	
	public static void main(String[] args) 
	{
		junit.textui.TestRunner.run(CoreProductManagerTestCase.class);
	}
}

