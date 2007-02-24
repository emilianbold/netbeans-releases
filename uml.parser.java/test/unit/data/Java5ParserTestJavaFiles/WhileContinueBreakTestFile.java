class WhileContinueBreakTestFile {
 public void test()
 {   
	int x = 3;
    while(true){
    x = x+1;
	if (x>=100 && x<=150)continue;
	if (x==200)break;
    }
 }
}
