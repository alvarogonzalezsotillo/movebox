function echo(msg){
	WScript.echo(msg);
}

function input(){
	return WScript.stdIn.readLine();
}


function mcm_recursivo(a,b){
	if( a == b ){
		return a;
	}
	else if( a > b ){
		return mcm_recursivo(b, a-b);
	}
	else{
		return mcm_recursivo(a, b-a);
	}
}

function mcm(a,b){
	
	while(true){
		var A = a>b?a:b;
		var B = a<b?a:b;
		
		if( A == B ){
			return A;
		}
		
		a = B;
		b = A-B;
	}
}


function comprueba(a,b){
	mcm1 = mcm(a,b);
	mcm2 = mcm_recursivo(a,b);
	echo( mcm1 + " -- " + mcm2 + " -- " + (mcm1==mcm2?"bien":"MAL" ) );
}

comprueba(3454, 200);
comprueba(1432, 2345);
comprueba(234523, 20250);
comprueba(100, 3544);
comprueba(2435, 32453);
comprueba(100, 23434);
comprueba(2345, 6765);
comprueba(678, 200);
comprueba(100, 23442);
