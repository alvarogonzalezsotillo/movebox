function echo(msg){
	WScript.echo(msg);
}

function input(){
	return WScript.stdIn.readLine();
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


function BuscarMaximoMCM(a,b){
	var max = mcm(a,b);
	var maxi = 0;
	for( var i = 0 ; i < 1000 ; i++ ){
		var m = mcm(a+i,b+i);
		if( m> max ){
			max = m;
			maxi = i;
		}
		max = m>max?m:max;
		echo( i + ":" + max + ":" + maxi );
	}
}

BuscarMaximoMCM(225,487);

