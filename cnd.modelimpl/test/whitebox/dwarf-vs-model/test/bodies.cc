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

int if_1() {
	int x = 0;
	if( int zzz = x )  {
		int zzz1 = 5;
		return zzz + zzz1;
	}
	else {
		int zzz2 = 7;
		return zzz + zzz2;
	}
}

int if_2() {
	int x = 0;
	if( x != 0 )  {
		int zzz_2_1 = 5;
		return x + zzz_2_1;
	}
	else {
		int zzz_2_2 = 7;
		return x + zzz_2_2;
	}
	return x;
}

int if_3() {
	int x = 0;
	if( x != 0 )  
		int z333 = if_3();
	else
		int z333 = if_3();
	return x;
}

int if_4() {
	int x = 0;
	if( int zzz = x )  
		int z333 = zzz + if_3();
	else
		int z333 = zzz + if_3();
	return x;
}

//=============================================

int for_1() {
	int sum = 0;
	for( int i = 0; i < 12; i++ ) {
		int x = i*2;
	}
	return sum;
}

int for_2() {
	int sum = 0;
	for( int j = 0; j < 12; j++ )
		int y = j*2;
	return sum;
}

int for_3() {
	int k;
	for(; k < 5; k++ ) {
		int k3 = k++;
		k += k3;
	}
	return k;
}

//=============================================

int loop_1() {
	int sum = 0;
	while( sum < 10 ) {
		int v0 = loop_1();
		sum += v0;
	}
	return sum;
}

int loop_2() {
	int sum = 0;
	while( int v1 = loop_1() ) {
		int v2 = loop_1();
		sum += v2;
	}
	return sum;
}

int loop_3() {
	int sum = 0;
	do {
		sum++;
	}
	while( sum < 5 ); 
	return sum;
}

//=============================================

int try_catch_1() {
	int res = 0;
	try {
		int tc1 = try_catch_1();
		res += tc1;
	}
	catch( int& p ) {
		int tc2 = 3;
		res += tc2;
	}
	catch( ... ) {
		int tc3 = 3;
		res += tc3;
	}
	return res;
}

//=============================================

int switch_1() {
	int x;
	switch (x) {
		case 0:
			{
				int sw_0 = 3;
				return sw_0;
			}
		case 1:
			{
				int sw_1 = 5;
				return sw_1;
			}
		default:
			{
				int sw_d = x;
				return sw_d;
			}
	}
}

int switch_2() {
	int x;
	switch (int y = x*2) {
		case 0:
			{
				int sw_2_0 = 3;
				return sw_2_0;
			}
		case 1:
			{
				int sw_2_1 = 5;
				return sw_2_1;
			}
		default:
			{
				int sw_2_d = x;
				return sw_2_d;
			}
	}
}

//=============================================

int just_compound_1() {
	int sum;
	{
		int jc1 = just_compound_1();
		sum += jc1;
	}
	{
		int jc2 = just_compound_1();
		sum += jc2;
	}
	return sum;
}

//==============================================
int deep(int a, int b) {
    int res = 0;
    if( a > 0 ) {
	if( int deep_b = b ) {
	    int deep_x =  a+b;
	    return deep_x;
	}
	if( b < a ) {
	    int deep_y =  a+b;
	    return deep_y;
	}
	{
	    int deep_z =  a+b;
	    if( b < 2 ) {
		int very_deep_1 = a;
		return very_deep_1;
	    }
	    if( b < 2 ) {
		if( b < 1 ) {
		    int very_deep_2 = a;
		    return very_deep_2;
		}
	    }
	    if( b > 2 ) {
		if( b > 3 ) {
		    if( b > 4 ) {
			int very_deep_3 = a;
			return very_deep_3;
		    }
		}
	    }
	    return deep_z;
	}
    }
    return res;
}
//============================================
int class_enum_in_body() {
    struct Point {  
	int x;
	int y;
    };
    Point p;
    if( p.x > 0 ) {
	return p.x;
    }
    enum Coins {
	Nickel,
	Dim
    };
    if( p.y > 0 ) {
	return Dim;
    }
    union U {
	int i;
	char c;
    };
    U u;
    return u.i;
}


//===========================================
int equal_names_in_body(bool b) {
    if( b ) {
	int x = 3;
	return x;
    }
    else {
	long x = 5;
	return x;
    }
}
