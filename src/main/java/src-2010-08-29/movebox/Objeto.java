package movebox;

import java.util.HashMap;


public class Objeto{ 
	public static final Objeto PARED = fromCharPredefinido('#');
	public static final Objeto OBJETIVO = fromCharPredefinido('.'); 
	public static final Objeto BLOQUE = fromCharPredefinido('+'); 
	public static final Objeto BLOQUEOBJETIVO = fromCharPredefinido('*'); 
	public static final Objeto PERSONAJE = fromCharPredefinido('@');
	public static final Objeto PERSONAJEOBJETIVO = fromCharPredefinido('$');
	public static final Objeto VACIO = fromCharPredefinido(' ');
	public static final Objeto[] ZEROARRAY = new Objeto[0];
	private static HashMap<Character,Objeto> _objetos;

	private char _char;
	private boolean _predefinido;
	
	private static HashMap<Character,Objeto> objetos(){
		if (_objetos == null) {
			_objetos = new HashMap<Character,Objeto>();
		}

		return _objetos;
	}
	
	protected Objeto() {
	}

	public char toChar(){
		return _char;
	}
	
	@Override
	public String toString() {
		return "" + toChar();
	}
	
	public boolean predefinido(){
		return _predefinido;
	}

	public static Objeto fromInteger(int i) {
		return fromChar((char) ('0' + i) );
	}
	
	private static Objeto fromCharPredefinido(char c){
		Objeto o = fromChar(c);
		o._predefinido = true;
		return o;
	}
	
	public static Objeto fromChar(char c){
		Objeto ret = objetos().get(c);
		if( ret == null ){
			ret = new Objeto();
			ret._char = c;
			objetos().put(c, ret);
		}
		return ret;
	}
	
	public boolean hayBloque(){
		return this == BLOQUE || this == BLOQUEOBJETIVO;
	}
	
	public boolean hayVacio(){
		return this == VACIO || this == OBJETIVO;
	}
	
	public boolean hayVacioPosible(){
		return hayVacio() || hayPersonaje();
	}
	
	public boolean hayObjetivo(){
		return this == BLOQUEOBJETIVO || this == OBJETIVO || this == PERSONAJEOBJETIVO;
	}
	
	public boolean hayPersonaje(){
		return this == PERSONAJE || this == PERSONAJEOBJETIVO;
	}
	
	public boolean hayFicha(){
		return hayPersonaje() || hayBloque();
	}
	
	public Objeto conObjetivo(){
		if( hayPersonaje() ){
			return PERSONAJEOBJETIVO;
		}
		if( hayBloque() ){
			return BLOQUEOBJETIVO;
		}
		if( hayVacio() ){
			return OBJETIVO;
		}
		throw new IllegalStateException();
	}
	
	public Objeto conBloque(){
		if( hayObjetivo() ){
			return BLOQUEOBJETIVO;
		}
		if( this == VACIO ){
			return BLOQUE;
		}
		throw new IllegalStateException();
	}

	public Objeto conPersonaje(){
		if( hayObjetivo() ){
			return PERSONAJEOBJETIVO;
		}
		if( this == VACIO ){
			return PERSONAJE;
		}
		throw new IllegalStateException();
	}

	public boolean hayPared() {
		return this == PARED;
	}


};
