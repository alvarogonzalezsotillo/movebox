function echo(msg){
	WScript.echo(msg);
}

function input(){
	return WScript.stdIn.readLine();
}

function numero(n){
	var r = Math.random()*n;
	return 1+Math.floor( r );
}

function numero1(){
	return 8;
}

function numero2(){
	return numero(10);
	
}


aciertos = 0;
fallos = 0;
seguidos = 0;

while(true){

	n1 = numero1();
	n2 = numero2();
	
	if( seguidos > 1 ){
		echo( "Llevas " + seguidos + " aciertos seguidos" );
	}
	echo( n1 + " x " + n2 + "?" );
	resultado = input();
	
	multiplicacion = n1*n2;
	
	if( resultado == "resultados" ){
		echo( "Aciertos:" + aciertos );
		echo( "Fallos  :" + fallos );
		echo( "Seguidos:" + seguidos );
	}
	else if( multiplicacion == resultado ){
		aciertos += 1;
		seguidos += 1;
		echo( "Muy bien!!!!" );
	}
	else{
		seguidos = 0;
		fallos += 1;
		echo( " ¡MAMA  QUE..." + n1 + " x " + n2 + " = " + multiplicacion );
		echo( "Hay que repasar la tabla del " + n1 );
		for( var i = 1 ; i <= 10 ; i++ ){
			echo( "" );
			echo( n1 + " x " + i + " = " + (n1*i) );
		}
	}
		

}