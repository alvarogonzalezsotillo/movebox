var echo = this["document"] == undefined ? 
	function(msg){WScript.echo(msg);} : 
	function(msg){ document.write("<pre>" + msg + "</msg>" ); };


function debug(msg){
	echo("**********" + msg);
}

function input(){
	return WScript.stdIn.readLine();
}


function BuscaCaracterEnArrayDeCadenas(cadenas, caracter){
	var ret = BuscaCaracteresEnArrayDeCadenas(cadenas,caracter);
	if( ret.length == 1 ){
		return ret[0];
	}
	if( ret.length > 1 ){
		throw "Muchas ocurrencias";
	}
	return null;
}

function BuscaCaracteresEnArrayDeCadenas(cadenas, caracter){
	var ret = new Array();
	for( var y = 0 ; y < cadenas.length ; y++ ){
		for( var x = 0 ; x < cadenas[y].length ; x++ ){
			if( cadenas[y].charAt(x) == caracter ){
				ret.push( new Posicion(x,y) );
			}
		}
	}
	ret.sort(Posicion.prototype.compara);
	return ret;
}


function TablaHash(tamano){
	this.init(tamano);
}

TablaHash.prototype = {
	init : function(tamano){
		if(!tamano){
			tamano = 1000;
		}
		this.array = new Array(tamano);
		this.numeroElementos = 0;
	},
	
	hashDeObjeto : function(o){
		return o.hash;
	},
	
	indiceDeCubeta: function(hash){
		hash = Math.floor(hash);
		if( hash < 0 ){
			hash = -hash;
		}
		if( hash < 0 ){
			hash = 0;
		}
		return hash % this.array.length;
	},
	
	getCubeta : function( i, crear ){
		var ret = this.array[i];
		if( !ret && crear ){
			this.array[i] = new Array();
			ret = this.array[i];
		}
		return ret;
	},
	
	agrega : function(o){
		var hash = this.hashDeObjeto(o);
		var i = this.indiceDeCubeta(hash);
		var cubeta = this.getCubeta(i,true);
		cubeta.push(o);
		this.numeroElementos++;
	},
	
	extrae : function(o){
		var hash = this.hashDeObjeto(o);
		var i = this.indiceDeCubeta(hash);
		var cubeta = this.getCubeta(i,false);
		var indice = this.busquedaLineal(o,cubeta);
		if( indice == -1 ){
			return null;
		}
		var ret = cubeta[indice];
		cubeta.splice(indice,1);
		this.numeroElementos--;
		return ret;
	},
	
	getNumeroElementos : function(){
		return this.numeroElementos;
	},
	
	esIgual : function(o1,o2){
		if( o1 == o2 ){
			return true;
		}
		return o1.esIgual(o2);
	},
	
	busquedaLineal : function(o,cubeta){
		if( cubeta == null ){
			return -1;
		}
		for( i in cubeta ){
			if( this.esIgual(o,cubeta[i]) ){
				return i;
			}
		}
		return -1;
	},
	
	elementos : function(){
		var ret = new Array();
		for( var i in this.array ){
			ret = ret.concat(this.array[i]);
		}
	},
	
	contiene : function(o){
		var hash = this.hashDeObjeto(o);
		var i = this.indiceDeCubeta(hash);
		var cubeta = this.getCubeta(i,false);
		var indice = this.busquedaLineal(o,cubeta);
		if( indice == -1 ){
			return null;
		}
		return cubeta[indice];
	}
};


function Posicion(x,y){
	this.x = x*1;
	this.y = y*1;
}

Posicion.prototype = {
	toString : function(){
		return "x:" + this.x + " y:" + this.y;
	},
	
	esIgual : function(p){
		return this.x == p.x && this.y == p.y;
	},
	
	generaCopia : function(){
		return new Posicion(this.x,this.y);
	},
	
	compara : function(p1,p2){
		var dx = p1.x - p2.x;
		if( dx != 0 ) return dx;
		var dy = p1.y - p2.y;
		if( dy != 0 ) return dy;
	}
};

function IndiceEnArrayDePosiciones(posiciones,p){
	for( var i in posiciones ){
		if( posiciones[i].esIgual(p) ){
			return i;
		}
	}
	return -1;
}


function DistanciaEntreArraysDePuntos(ps1,ps2){
	var ret = 0;
	
	for( var i in ps1 ){
		var p1 = ps1[i];
		var suma = 0;
		for( var j in ps2 ){
			var p2 = ps2[j];
			var d = Math.abs(p1.x-p2.x) + Math.abs(p1.y-p2.y);
			if( d == 0 ){
				// LA CAJA ESTA COLOCADA
				suma = 0;
				break;
			}
			suma += d;
		}
		ret += suma;
	}
	return ret;
}

function ReemplazaEnCadena(s,x,objeto){
	if( x == 0 ){
		return objeto + s.substring(1,s.length);
	}
	else if( x == s.length-1 ){
		return s.substring(0,s.length-1) + objeto;
	}
	return s.substring(0,x) + objeto + s.substring(x+1,s.length);
}

function dump(o){
	echo(o);
	for( var n in o ){
		echo( "  " + n + ":" + o[n] );
	}
}

function EchoTablero(tablero){
	var celdas = tablero.celdas();

	if( tablero.tableroYObjetos ){
		celdas = tablero.tableroYObjetos();
	}
	
	var s = "  ";
	for( var i=0 ;i < 10 ; i++ ){
		s += (""+i);
	}
	echo(s);	
	
	for( var s in celdas ){
		echo( s + " " + celdas[s] );
	}
	echo( "Id:" + tablero.id );
	echo( "Movimientos:" + tablero.movimientos );
	echo( "Heuristica:" + tablero.heuristica );
	echo( "Hijos:" + tablero.estadosHijo );
	echo( "Padre:" + tablero.estadosPadre );
	
	if( tablero.esEstadoFinal() ){
		echo( "FINAL FINAL FINAL FINAL FINAL FINAL FINAL FINAL FINAL FINAL FINAL FINAL FINAL FINAL FINAL" );
	}

}


function Tablero(tablero){
	this._celdas = tablero;

	this.objetoEn = function(p){
		var x = p.x;
		var y = p.y;
		if( y < 0 || y >= this._celdas.length ){
			return "#";
		}
		if( x < 0 || x >= this._celdas[y].length ){
			return "#";
		}
		return this._celdas[y].charAt(x);
	}

	this.ponObjetoEn = function(p,objeto){
		var x = p.x;
		var y = p.y;
		var s = this._celdas[y];
		this._celdas[y] = ReemplazaEnCadena(s,x,objeto);
	}
	
	this.esObjetivo = function(p){
		return IndiceEnArrayDePosiciones(this.objetivos,p)>=0;
	}
	
	this.celdas = function(){
		return this._celdas;
	}
}

Contador = (function(){
	var contador = 0;
	ret = new Object();
	ret.siguiente = function(){
		contador++;
		return contador;
	};
	return ret;
})();

Direccion = (function(){
	ret = new Object();
	ret.arriba = function(){ return 0; };
	ret.abajo = function(){ return 1; };
	ret.izquierda = function(){ return 2; };
	ret.derecha = function(){ return 3; };

	ret.direcciones = function(){ return new Array( ret.arriba(), ret.abajo(), ret.izquierda(), ret.derecha() ); };
	
	ret.mueve = function(d,p){
		if( d == ret.arriba() ){
			return new Posicion(p.x,p.y-1);
		}
		if( d == ret.abajo() ){
			return new Posicion(p.x,p.y+1);
		}
		if( d == ret.izquierda() ){
			return new Posicion(p.x-1,p.y);
		}
		if( d == ret.derecha() ){
			return new Posicion(p.x+1,p.y);
		}
	}
	
	ret.opuesto = function(d){
		if( this.arriba() == d ){
			return this.abajo();
		}
		if( this.abajo() == d ){
			return this.arriba();
		}
		if( this.izquierda() == d ){
			return this.derecha();
		}
		if( this.derecha() == d ){
			return this.izquierda();
		}
	}
	
	ret.pArriba = function(p){ return ret.mueve( ret.arriba(), p ); }
	ret.pAbajo = function(p){ return ret.mueve( ret.abajo(), p ); }
	ret.pIzquierda = function(p){ return ret.mueve( ret.izquierda(), p ); }
	ret.pDerecha = function(p){ return ret.mueve( ret.derecha(), p ); }
	
	return ret;
})();

function CopiaArray(a){
	var ret = new Array();
	for( i in a ){
		if( a[i].generaCopia ){
			ret[i] = a[i].generaCopia();
		}
		else{
			ret[i] = a[i];
		}
	}
	return ret;
}

/*
	@: muñeco
	.: objetivo
	+: caja 
	*: caja en objetivo
	#: pared
*/
function Estado(){
	this.id = Contador.siguiente();
	this.estadosHijo = new Array();
	this.estadosPadre = new Array();
}

Estado.prototype = {

	esExplotado : function(){
		return this.estadosHijo.length > 0;
	},

	generaEstadosHijo : function(){
		if( !this.esExplotable() ){
			return this.estadosHijo;
		}
		var dir = Direccion.direcciones();
		for( var i in dir ){
			this.estadosHijo.push( this.generaEstadoHijo( dir[i] ) );
		}
		return this.estadosHijo;
	},

	hayCajaEnUnaEsquinaNoObjetivo : function(){
		for( i in this.cajas ){
			var caja = this.cajas[i];
			
			// SI LA CAJA ESTA EN UN OBJETIVO, SE PUEDE
			if( IndiceEnArrayDePosiciones( this.tablero.objetivos, caja ) >= 0 ){
				continue;
			}
				
			// SI LA CAJA ESTA EN UNA ESQUINA, NO SE PUEDE
			var ar = this.objetoEn(Direccion.pArriba(caja));
			var ab = this.objetoEn(Direccion.pAbajo(caja));
			var iz = this.objetoEn(Direccion.pIzquierda(caja));
			var de = this.objetoEn(Direccion.pDerecha(caja));
			if( ar == "#" && iz == "#" ){
				return true;
			}
			if( ar == "#" && de == "#" ){
				return true;
			}
			if( ab == "#" && iz == "#" ){
				return true;
			}
			if( ab == "#" && de == "#" ){
				return true;
			}
		}
		return false;
	},
	
	hayCajasFormandoUnCuadrado : function(){
		for( i in this.cajas ){
			var caja = this.cajas[i];
			
			// SI LA CAJA ESTA EN UN OBJETIVO, SE PUEDE
			if( IndiceEnArrayDePosiciones( this.tablero.objetivos, caja ) >= 0 ){
				continue;
			}
				
			// SI LA CAJA ESTA EN UN CUADRADO, NO SE PUEDE
			var n = this.objetoEn(Direccion.pArriba(caja));
			var s = this.objetoEn(Direccion.pAbajo(caja));
			var e = this.objetoEn(Direccion.pDerecha(caja));
			var o = this.objetoEn(Direccion.pIzquierda(caja));
			var ne = this.objetoEn(Direccion.pArriba(Direccion.pDerecha(caja)));
			var no = this.objetoEn(Direccion.pArriba(Direccion.pIzquierda(caja)));
			var so = this.objetoEn(Direccion.pAbajo(Direccion.pIzquierda(caja)));
			var se = this.objetoEn(Direccion.pAbajo(Direccion.pDerecha(caja)));
			
			if( n != " " && ne != " " && e != " " ){
				return true;
			}
			if( n != " " && no != " " && o != " " ){
				return true;
			}
			if( s != " " && se != " " && e != " " ){
				return true;
			}
			if( s != " " && so != " " && o != " " ){
				return true;
			}
		}
		return false;
	},

	hayOjosImposibles : function(){
		//
		//  +#   ++#    +#   #+#   #
		// + #   + #   # #   + +  $ $
		// ###   ##     #     +#  +++
		//
		
		var numeroDe = function( objeto ){
			var ret = 0;
			for( var i = 1 ; i < arguments.length ; i++ ){
				if( objeto == arguments[i] ){
					ret++;
				}
			}
			return ret;
		};
		
		var fijoSiNoSeMueveOtro = function( medio, lado1, lado2 ){
			if( medio == "#" ){
				return true;
			}
			if( lado1 == "#" || lado2 == "#" ){
				return true;
			}
			return false;
		};

		var celdas = this.tablero.celdas();

		for( var y in celdas ){
			for( var x = 0 ; x < celdas[y].length ; x++ ){
				var p = new Posicion(x,y);

				// SI NO ESTA VACIO, NO ES UN OJO
				if( this.objetoEn(p) != " " ){
					continue;
				}
				
				// SI ES UN OBJETIVO, NO ES UN OJO
				if( IndiceEnArrayDePosiciones( this.tablero.objetivos, p ) >= 0 ){
					continue;
				}
				
				var n = this.objetoEn(Direccion.pArriba(p));
				var s = this.objetoEn(Direccion.pAbajo(p));
				var e = this.objetoEn(Direccion.pDerecha(p));
				var o = this.objetoEn(Direccion.pIzquierda(p));
				var ne = this.objetoEn(Direccion.pArriba(Direccion.pDerecha(p)));
				var no = this.objetoEn(Direccion.pArriba(Direccion.pIzquierda(p)));
				var so = this.objetoEn(Direccion.pAbajo(Direccion.pIzquierda(p)));
				var se = this.objetoEn(Direccion.pAbajo(Direccion.pDerecha(p)));
				
				if( numeroDe( " ", n, s, e, o ) > 0 ){
					// NO ES UN OJO, SE PUEDE SALIR
					continue;
				}

				if( numeroDe( "+", n, s, e, o ) == 0 ){
					// NO HAY CAJAS QUE MOVER
					continue;
				}

				if( fijoSiNoSeMueveOtro( n, no, ne ) &&
					fijoSiNoSeMueveOtro( s, so, se ) &&
					fijoSiNoSeMueveOtro( o, so, no ) &&
					fijoSiNoSeMueveOtro( e, se, ne ) ){
					// HAY UN OJO
					return true;
				}
			}
		}
		return false;
	},

	esExplotable : function(){
		if( this._cacheEsExplotable != undefined ){
			return this._cacheEsExplotable;
		}
		// SI HAY UNA CAJA EN UNA ESQUINA NO OBJETIVO, NADA
		if( this.hayCajaEnUnaEsquinaNoObjetivo() ){
			this._cacheEsExplotable = false;
			return false;
		}
		
		// SI LA CAJA ESTA EN UN CUADRADO Y NO EN OBJETIVO, NADA
		if( this.hayCajasFormandoUnCuadrado() ){
			this._cacheEsExplotable = false;
			return false;
		}
		
		// SI TIENE OJOS, NADA
		if( this.hayOjosImposibles() ){
			this._cacheEsExplotable = false;
			return false;
		}
		
		this._cacheEsExplotable = true;
		return true;
	},
	

	
	estadoPadre : function(){
		// EL PADRE ES EL DE MENOS MOVIMIENTOS DE LOS PADRES
		var ret = null;
		for( p in this.estadosPadre ){
			if( ret == null ){
				ret = this.estadosPadre[p];
			}
			if( ret.movimientos > this.estadosPadre[p] ){
				ret = this.estadosPadre[p];
			}
		}
		return ret;
	},
	
	generaEstadoHijo : function(d){
		if( !this.esMovimientoPosible(d) ){
			return null;
		}
		var ret = this.generaCopia();
		ret.mueve(d);
		ret.estadosPadre[Direccion.opuesto(d)] = this;
		return ret;
	},
	
	mueve : function(d){
		this.posicion = Direccion.mueve(d,this.posicion);
		var cajaI = IndiceEnArrayDePosiciones(this.cajas, this.posicion);
		if( cajaI >= 0 ){
			this.cajas = CopiaArray( this.cajas );
			this.cajas[cajaI] = Direccion.mueve(d,this.cajas[cajaI]);
			this.cajas.sort(Posicion.prototype.compara);
			this.movimientosCajas++;
		}
		this.movimientos++;
		this.calculaHeuristica();
	},
	
	generaCopia : function(){
		var ret = new Estado();
		ret.posicion = this.posicion.generaCopia();
		ret.movimientos = this.movimientos;
		ret.movimientosCajas = this.movimientosCajas;
		ret.tablero = this.tablero;
		ret.cajas = this.cajas;
		return ret;
	},

	esIgual : function(e){

		if( this == e ){
			return true;
		}
		
		if( e.tablero != this.tablero ){
			return false;
		}
		
		if( e.hash != this.hash ){
			return false;
		}
		
		if( !e.posicion.esIgual(this.posicion) ){
			return false;
		}

		// LAS CAJAS ESTAN ORDENADAS
		for( var i in this.cajas ){
			if( !e.cajas[i].esIgual( this.cajas[i] ) ){
				return false;
			}
		}
		
		return true;
	},

	parseaTablero : function(tablero){
		this.movimientos = 0;
		this.movimientosCajas = 0;
		this.tablero = new Tablero(tablero);
		this.posicion = BuscaCaracterEnArrayDeCadenas(this.celdas(),"@");

		// QUITA EL PERSONAJE DEL TABLERO
		if( this.posicion == null ){
			throw "No encuentro el personaje";
		}
		this.tablero.ponObjetoEn(this.posicion," ");
		
		// QUITA LAS CAJAS
		this.cajas = BuscaCaracteresEnArrayDeCadenas( this.celdas(), "+" );
		for( i in this.cajas ){
			this.tablero.ponObjetoEn( this.cajas[i], " " );
		}

		// QUITA LAS CAJAS EN OBJETIVOS
		var masCajas = BuscaCaracteresEnArrayDeCadenas( this.celdas(), "*" );
		for( i in masCajas ){
			this.tablero.ponObjetoEn( masCajas[i], "." );
			this.cajas.push(masCajas[i]);
		}
		
		// QUITA LOS OBJETIVOS
		this.tablero.objetivos = BuscaCaracteresEnArrayDeCadenas( this.celdas(), "." );
		for( i in this.tablero.objetivos ){
			this.tablero.ponObjetoEn( this.tablero.objetivos[i], " " );
		}
		
		this.calculaHeuristica();
	},

	calculaHeuristica : function(){
		var ret = 0;
		
		var distancia = DistanciaEntreArraysDePuntos(this.cajas, this.tablero.objetivos);
		ret += distancia/3;
		ret += this.movimientos/29;
		ret += this.movimientosCajas/17;
		this.heuristica = ret;
		
		this.calculaHash();
		
		return ret;
	},
	
	calculaHash : function(){
		var hash = 0;
		hash = this.posicion.x + 100*this.posicion.y;

		for( var i in this.cajas ){
			hash = hash << 8;
			hash ^= this.cajas[i].x + 100*this.posicion.y;
		}
		this.hash = hash;
		return this.hash;
	},

	objetoEn : function(p){
		// NO SE TIENEN EN CUENTA LOS OBJETIVOS NI PERSONAJE
		if( IndiceEnArrayDePosiciones( this.cajas, p ) >= 0){
			return "+";
		}
		
		return this.tablero.objetoEn(p);
	},

	
	esMovimientoPosible : function(direccion){
		var newP = Direccion.mueve(direccion,this.posicion);
		var objeto = this.objetoEn(newP);
		// SI ESTA VACIO, SE PUEDE MOVER
		if( objeto == " " ){
			return true;
		}
		
		// SI HAY UNA CAJA, PERO DETRAS ESTA VACIO, SE PUEDE MOVER
		else if( objeto == "+" ){
			newP = Direccion.mueve(direccion,newP);
			if( this.objetoEn(newP) == " " ){
				return true;
			}
		}
		return false;
	},


	esEstadoFinal : function(){
		for( c in this.cajas ){
			if( !this.tablero.esObjetivo( this.cajas[c] ) ){
				return false;
			}
		}
		return true;
	},
	
	tableroYObjetos : function(){
		var celdas = new Array();
		for( var c in this.celdas() ){
			celdas[c] = this.celdas()[c] + "";
		}


		for( var c in this.cajas ){
			var p = this.cajas[c];
			celdas[p.y] = ReemplazaEnCadena( celdas[p.y], p.x, "+" );
		}
		
		for( var o in this.tablero.objetivos ){
			var p = this.tablero.objetivos[o];
			var objetivo = ".";
			if( celdas[p.y].charAt(p.x) == "+" ){
				objetivo = "*";
			}
			celdas[p.y] = ReemplazaEnCadena( celdas[p.y], p.x, objetivo );
		}

		celdas[this.posicion.y] = ReemplazaEnCadena( celdas[this.posicion.y], this.posicion.x, "@" );
		
		return celdas;
	},
	
	celdas : function(){
		return this.tablero.celdas();
	},
	
	toString : function(){
		return "" + this.id;
	}
};


function ConjuntoDeEstados(){
	this.estadosExplotados = new TablaHash(10000);
	this.estadosFinales = new Array();
	this.estadosNoExplotados = new TablaHash(10000);
	this._noTieneSolucion = false;
	
	this.estadosNoExplotados.hashDeObjeto = function(e){
		if( e == null || typeof(e.heuristica) == "undefined" ){
			debug( "EL ESTADO NO TIENE HEURISTICA" );
			debug( e );
			dump(e);
		}
		return Math.min( e.heuristica * 100, this.array.length -1 );
	};
	this.estadosNoExplotados.indiceDeCubeta = function(hash){
		var ret = TablaHash.prototype.indiceDeCubeta.call( this, hash );
		return ret;
	}
	this.explotaEstado = function(e){
		this.estadosExplotados.agrega(e);
		var hijos = e.generaEstadosHijo();
		for( var h in hijos ){
			if( hijos[h] ){
				this.agregaEstado( hijos[h] );
			}
		}
	}
	
	this.estadosNoExplotados.primero = function(){
		var ret = null;
		for( var i = 0 ; i < this.array.length ; i++ ){
			for( j in this.array[i] ){
				ret = this.array[i][j];
				if( ret != null ){
					break;
				}
			}
			if( ret != null ){
				break;
			}
		}
		return ret;
	}
	
	
	this.explotaEstadoConMejorHeuristica = function(){
		var e = this.extraeEstadoConMejorHeuristica();
		if( e != null ){
			this.explotaEstado(e);
		}
		else{
			this._noTieneSolucion = true;
		}
		return e != null;
	}
	
	this.noTieneSolucion = function(){
		return this._noTieneSolucion;
	}
	
	this.tieneSolucion = function(){
		return this.estadosFinales.length > 0;
	}
	
	this.muestraMovimientos = function(e){
		echo( ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::" );
		while( e != null ){
			EchoTablero(e);
			e = e.estadoPadre();
		}
	}
	
	
	this.agregaEstado = function(e){
		if( e.esEstadoFinal() ){
			this.estadosFinales.push(e);
		}
		
		// SI YA ESTA COMO EXPLOTADO, NADA
		if( this.estadosExplotados.contiene(e) ){
			return;
		}
		
		if( e.esExplotado() || !e.esExplotable() ){
			// SI ESTA EXPLOTADO O NO SE PUEDE EXPLOTAR
			this.estadosExplotados.agrega(e);
		}
		else{
			// SI NO ESTA EXPLOTADO
			var oldEstado = this.estadosNoExplotados.contiene(e);
			if( !oldEstado ){
				// EL ESTADO NO ESTABA
				this.estadosNoExplotados.agrega(e);
			}
			else{
				
				// EL ESTADO ESTABA. SE CAMBIA SI SUS MOVIMIENTOS SON MENORES
				if( oldEstado.movimientos >= e.movimientos ){
					this.estadosNoExplotados.extrae(oldEstado);
					this.estadosNoExplotados.agrega(e);
					
					// HAY QUE CAMBIAR LOS HIJOS DE LOS ESTADOS QUE APUNTABAN AL ANTIGUO
					for( var j in oldEstado.estadosPadre ){
						oldEstado.estadosPadre[j].estadosHijo[ Direccion.opuesto(j) ] = e;
					}
				}
			}
		}
	}
	
	this.extraeEstadoConMejorHeuristica = function(){
		var ret = this.estadosNoExplotados.primero();
		if( ret != null  ){
			this.estadosNoExplotados.extrae(ret);
		}
		return ret;
	}
	
	this.dump = function(){
		echo( "-----------------------------------------------------------" );
		var explotados = this.estadosExplotados.elementos();
		for( var i in explotados ){
			echo("");
			EchoTablero( this.explotados[i] );
		}
		var noExplotados = this.estadosNoExplotados.elementos();		
		for( var i in noExplotados ){
			echo("");
			EchoTablero( noExplotados[i] );
		}
	}
}

var tableros = {
	0: new Array(
		"########",
		"#    +.#",  
		"# @* +.#",
		"########"
	),

	1 : new Array(
		"########",
		"#    +.#",  
		"#    +.#",  
		"#**    #",  
		"# @*   #",
		"########"
	),

	2 : new Array(
		"#########",
		"#       #",  
		"#       #",  
		"#  ##+  #",  
		"#*##.   #",  
		"# @#    #",
		"#########"
	),

	3 : new Array(
		"#########",
		"#    ...#",  
		"#       #",  
		"#   +   #",  
		"#  ##+  #",  
		"# ## +  #",  
		"# @#    #",
		"#########"
	),

	4 : new Array(
		"########",
		"####   #",
		"##  +  #",
		"#     ##",
		"# #*####",
		"#.+   ##",
		"##@.  ##",
		"########"
	),
	
	5 : new Array(
		"    ####    ",
		" ####  ###  ",
		" #  +  + #  ",
		" #+..+ . ###",
		"## #.##..  #",
		"#  ..+@+ + #",
		"# + . #. ###",
		"####+  + #  ",
		"   #  ####  ",
		"   ####     "
	),
	
	6 : new Array(
		"@+#",
		"+ #",
		"###"
	)

}
var e = new Estado();
e.parseaTablero(tableros[3]);

EchoTablero(e);


var ce = new ConjuntoDeEstados();
ce.agregaEstado(e);

var cont = 0;

var bucle = function(){

	var pasos = 100;


	while( --pasos ){
		if( ce.noTieneSolucion() ){
			echo( "SIN SOLUCION" );
			ce.dump();
			return false;
		}

		if( ce.tieneSolucion() ){
			echo( "SOLUCION" );
			ce.muestraMovimientos( ce.estadosFinales[0] );
			return false;
		}

		ce.explotaEstadoConMejorHeuristica();

		if( ++cont % 100 == 0 ){
			echo("");
			var noe = ce.estadosExplotados.getNumeroElementos();
			var e = ce.estadosNoExplotados.getNumeroElementos() 
			echo(cont + ":    explotados:" + noe + 	"  no explotados:" + e);
				
			EchoTablero( ce.estadosNoExplotados.primero() );
		}
	}
	
	return true;
}

if( this["window"] == undefined ){
	while( bucle() );
}
else{
	window.setInterval( bucle, 100 );		
}



