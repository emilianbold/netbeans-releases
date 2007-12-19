<?
interface InterfaceName {
	const CONST = 1;
	
	public function op( $arg );
}

interface Second extends InterfaceName, One {
	
		function method();
	
		const A = 1, B =2;
		const C =3;
		
		
		public function func( $arg );
}
?>