function echo(msg){
	WScript.echo(msg);
}


function Objetable(param){
	var arg = param;
	
	
	var ret = {
		metodoNormal: function(){ return arg; },
		kk : function(){ return this.metodoNormal()*2; }
	};
	
	return ret;
}

Objetable.prototype.kk = function(){
	return "hola";
}



array = new Array();
array[0] = new Objetable(0);
array[1] = new Objetable(1);

for( var i in array ){
	echo( i+":" + array[i] );
    echo( "  metodoNormal:" + array[i].metodoNormal() );
}

