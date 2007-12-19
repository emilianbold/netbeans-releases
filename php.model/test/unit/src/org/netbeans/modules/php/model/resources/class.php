<?
class Class {

	var $attr;
	const A = 1;
	
	protected $my = 1, $v ;
	
	abstract function func( $arg );
	
	static function method() {
	}
}

abstract class A extends Class implements InterfaceName, Second {

	public final function op( $arg ){
	}
	
	private function check(){
	}
	
	const A =1 , B=2;
	
	protected $attr , $my =1 ;
	
	public static $C = "const";
}
?>
