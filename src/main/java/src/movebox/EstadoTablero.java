package movebox;


import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;

import movebox.CalculadorDeZonas.Zona;

public class EstadoTablero implements ITablero{
	
	private static final boolean LOSOBJETIVOSCUENTAN = false;

	
	public static final Comparator<EstadoTablero> HEURISTICACOMPARATOR = new Comparator<EstadoTablero>() {

		@Override
		public int compare(EstadoTablero o1, EstadoTablero o2) {
			double retD = o1._heuristica - o2._heuristica;
			int ret = (int) retD;
			if( ret == 0 ){
				ret = o1.id() - o2.id();
			}
			return ret;
		}
	};

	public static final EstadoTablero[] ZEROARRAY = new EstadoTablero[0];
	
	private Punto _personaje;
	private Punto[] _objetivos;
	private Punto[] _bloques;
	private TableroSinFichas _tablero;
	private Zona _zonaInterior;
	private EstadoTablero[] _hijos;
	private EstadoTablero[] _padres;
	private int _hash;
	private int _id;
	private static Contador _contador = new Contador();
	private double _heuristica;
	private int _movimientos;
	private int _movimientosBloque;

	private Boolean _esNoExplotableSimple;
	private Boolean _esNoExplotableEvolucionable;

	private Boolean _hayCajasFormandoUnCuadrado;

	private Boolean _hayAlgunBloqueEnEsquinaNoObjetivo;
	
	private EstadoTablero(){
		_id = _contador.next();
	}
	
	public static EstadoTablero convierteAEstadoTablero(ITablero t){
		if( t instanceof EstadoTablero ){
			return (EstadoTablero) t;
		}
		return copia(t);
	}
	
	public static EstadoTablero copia( ITablero t ){
		if( t instanceof EstadoTablero ){
			return new EstadoTablero((EstadoTablero) t);
		}
		EstadoTablero ret = new EstadoTablero();
		ret.inicializa(t);
		return ret;
	}
	
	private EstadoTablero( EstadoTablero e ){
		this();
		_personaje = e._personaje;
		_tablero = e._tablero;
		_objetivos = e._objetivos;
		_bloques = e._bloques;
	}

	public int id(){
		return _id;
	}
	
	private void inicializa(ITablero t) {
		TableroSinFichas tablero = new TableroSinFichas();
		ArrayList<Punto> objetivos = new ArrayList<Punto>();
		ArrayList<Punto> bloques = new ArrayList<Punto>();
		Punto personaje = null;
		
		for( int x = 0 ; x < t.ancho() ; x++ ){
			for( int y = 0 ; y < t.alto() ; y++ ){
				Objeto o = t.getObjeto(x,y);
				tablero.setObjeto(x, y, o );
				if( o.hayBloque() ){
					bloques.add( Punto.P(x,y) );
				}
				if( o.hayObjetivo() ){
					objetivos.add( Punto.P(x,y) );
				}
				if( o.hayPersonaje() ){
					personaje = Punto.P(x, y);
				}
			}
		}
		
		_personaje = personaje;
		_tablero = tablero;
		_objetivos = objetivos.toArray(Punto.ZEROARRAY);
		_bloques = bloques.toArray(Punto.ZEROARRAY);
		
		Punto.ordenaArray(_objetivos);
		Punto.ordenaArray(_bloques);
		calculaHashYHeuristica();
	}

	private boolean esPosible(Punto personaje, Direccion d){
		
		Objeto o = getObjeto(personaje);
		if( o.hayPared() ){
			// SI HAY PARED, EL PERSONAJE NO PUEDE ESTAR AHI
			return false;
		}
		
		Punto p = personaje;
		Punto p2 = p.mueve(d);
		Objeto o2 = getObjeto(p2);
		if( o2.hayVacio() ){
			return true;
		}
		
		if( o2.hayBloque() ){
			Punto p3 = p2.mueve(d);
			Objeto o3 = getObjeto(p3);
			if( o3.hayVacio() || o3.hayPersonaje() ){
				// EL PERSONAJE PUEDE ESTAR EN EL DESTINO DEL BLOQUE
				// ESTO PASA EN LOS MOVIMIENTOS FICTICIOS
				return true;
			}
		}
		return false;
	}
	
	public EstadoTablero creaHijo( Punto personaje, Direccion d ){
		// SE PASA EL PERSONAJE POR SI SE QUIEREN HACER MOVIMIENTOS
		// FICTICIOS, SIN EL PERSONAJE HAY PUESTO
		if( !esPosible(personaje,d) ){
			return null;
		}
		
		EstadoTablero ret = new EstadoTablero(this);
		
		ret._movimientosBloque = _movimientosBloque;
		ret._personaje = personaje.mueve(d);
		int indiceBloque = Punto.buscaPuntoEnArray(ret._personaje, _bloques );
		if( indiceBloque >= 0 ){
			// HAY QUE HACER UNA COPIA
			Punto[] bloques = Punto.copiaArray(_bloques);
			bloques[indiceBloque] = bloques[indiceBloque].mueve(d);
			Punto.ordenaArray(bloques);
			ret._bloques = bloques;
			ret._movimientosBloque++;
		}
		
		ret._padres = new EstadoTablero[Direccion.values().length];
		ret._padres[d.opuesto().ordinal()] = this;
		
		
		ret._movimientos = _movimientos+1;
		ret.chequea();
		
		ret.calculaHashYHeuristica();
		
		return ret;
	}
	
	
	public EstadoTablero creaHijo(Direccion d){
		return creaHijo( _personaje, d );
	}
	
	private void chequea() {
		// el personaje no esta encima de un bloque
		if( Punto.buscaPuntoEnArray(_personaje, _bloques) >= 0 ){
			throw new IllegalStateException();
		}
		
		// EL PERSONAJE NO ESTA ENCIMA DE UNA PARED
		if( !_tablero.getObjeto(_personaje).hayVacio() ){
			throw new IllegalStateException();
		}
		
		// TODOS LOS BLOQUES DISTINTOS
		for( int i = 0 ; i < _bloques.length ; i++ ){
			for( int j = i+1 ; j < _bloques.length ; j++ ){
				if( _bloques[i] == _bloques[j] ){
					throw new IllegalStateException();
				}
			}
		}
		
	}

	EstadoTablero[] getPadres(){
		return _padres;
	}

	EstadoTablero[] getHijos(){
		return _hijos;
	}
	
	
	public EstadoTablero getPadre(){
		if( _padres == null ){
			return null;
		}
	
		// EL PADRE ES EL QUE TENGA MENOS MOVIMIENTOS
		EstadoTablero ret = null;
		for (EstadoTablero p : _padres) {
			if( p == null ){
				continue;
			}
			if( ret == null || ret._movimientos > p._movimientos ){
				ret = p;
			}
		}
		
		return ret;
	}
	
	private void calculaHashYHeuristica() {
		{
			_hash = Punto.hashCode( _bloques );
			_hash <<= 3;
			if( _personaje != null ){
				_hash ^= _personaje.hashCode();
			}
		}
		{
			_heuristica = 0;
			_heuristica += distanciaEntreArrays( _bloques, _objetivos );
			_heuristica += _movimientos/2;
			_heuristica += _movimientosBloque;
		}
	}
	
	private static int distanciaEntreArrays( Punto bloques[], Punto objetivos[] ){
		// DISTANCIA ENTRE CADA PUNTO CON TODOS LOS OTROS
		// PERO SI ESTA ENCIMA DE LOS OTROS NO SUMA NADA
		int ret = 0;
		
		for( int i = 0 ; i < bloques.length ; i++ ){
			int suma = 0;
			for( int j = 0 ; j < objetivos.length ; j++ ){
				int dx = Math.abs( bloques[i].x() - objetivos[j].x() );
				int dy =  Math.abs( bloques[i].y() - objetivos[j].y() );
				int d = dx*dx + dy*dy;	
				suma += d;
				if( d == 0 && !LOSOBJETIVOSCUENTAN ){
					suma = 0;
					break;
				}
			}
			ret += suma;
		}
		return ret;
	}
	

	@Override
	public int hashCode() {
		return _hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		EstadoTablero e = (EstadoTablero) obj;
		
		if( e == this ){
			return true;
		}
		
		if( e._tablero != _tablero ){
			return false;
		}
		
		if( e.hashCode() != hashCode() ){
			return false;
		}
		
		if( e._personaje != _personaje ){
			return false;
		}
		
		if( !Punto.equals( e._bloques, _bloques ) ){
			return false;
		}
		
		return true;
	}
	
	public EstadoTablero[] creaHijos(){
		if( _hijos != null ){
			return _hijos;
		}
		
		
		Direccion[] direcciones = Direccion.values();
		EstadoTablero[] ret = new EstadoTablero[direcciones.length];
		
		for( int i = 0 ; i < direcciones.length ; i++ ){
			ret[i] = creaHijo(direcciones[i]);
		}
		
		_hijos = ret;
		return ret;
	}

	@Override
	public int alto() {
		return _tablero.alto();
	}

	@Override
	public int ancho() {
		return _tablero.ancho();
	}

	@Override
	public Objeto getObjeto(Punto p) {
		int x = p.x();
		int y = p.y();
		
		boolean personaje = p == _personaje;
		boolean bloque = Punto.buscaPuntoEnArray(p, _bloques) >= 0;
		boolean objetivo = Punto.buscaPuntoEnArray(p, _objetivos) >= 0;
		
		Objeto ret = _tablero.getObjeto(x,y);
		if( personaje ){
			ret = ret.conPersonaje();
		}
		if( bloque ){
			ret = ret.conBloque();
		}
		if( objetivo ){
			ret = ret.conObjetivo();
		}
		
		return ret;
	}

	@Override
	public Objeto getObjeto(int x, int y) {
		return getObjeto( Punto.P(x, y) );
	}

	public void dump(PrintStream out){
		out.println( id() + " (" + hashCode() + "): " + _heuristica );
		Tablero.dump(out, this);
	}
	
	

	public int movimientos() {
		return _movimientos;
	}

	public boolean esFinal() {
		return distanciaEntreArrays(_bloques, _objetivos) == 0;
	}

	public boolean esExplotado() {
		return _hijos != null;
	}

	public boolean esNoExplotableSimple() {
		if( _esNoExplotableSimple != null ){
			return _esNoExplotableSimple.booleanValue();
		}
		
		if( hayAlgunBloqueEnEsquinaNoObjetivo() ){
			_esNoExplotableSimple = Boolean.TRUE;
		}
		else if( hayCajasFormandoUnCuadrado() ){
			_esNoExplotableSimple = Boolean.TRUE;
		}
		else if( hayCajasFormandoUnaEse() ){
			_esNoExplotableSimple = Boolean.TRUE;
		}
		else if( hayBloquesEnParedSinObjetivos() ){
			_esNoExplotableSimple = Boolean.TRUE;
		}
		else{
			_esNoExplotableSimple = Boolean.FALSE;
		}
		
		return _esNoExplotableSimple.booleanValue();
	}
	
	private boolean hayCajasFormandoUnaEse() {
//		 0123456789012
//		 0###########
//		 1# +   **..##
//		 2#   ++#....#
//		 3#    +*.***## <------------
//		 4##+  # ##.. #
//		 5#  + #  ##  #
//		 6#@++ ## #  ##
//		 7#   + #    #
//		 8#     ###  #
//		 9####### ####
		 return false;
	}

	private boolean hayBloquesEnParedSinObjetivos() {
//		 012345678
//		 0  #####
//		 1 ##   ##
//		 2 # *+* #
//		 3##. . *##
//		 4#  *+.  #
//		 5#   . +@#
//		 6### +  ## <-----
//		 7  ######
		return false;
	}

	public Zona zonaInterior(){
		if( _zonaInterior != null ){
			return _zonaInterior;
		}
		_zonaInterior = zonaInteriorDeTablero( _tablero, personaje() );
		return _zonaInterior;
	}
	
	private static Zona zonaInteriorDeTablero( TableroSinFichas t, Punto personaje ){
		CalculadorDeZonas cz = new CalculadorDeZonas(t);
		return cz.zona( personaje );
	}
	
	
	public boolean esNoExplotableEvolucionable(){
		if( _esNoExplotableEvolucionable != null ){
			return _esNoExplotableEvolucionable.booleanValue();
		}

		if( CalculadorDeZonasEvolucionables.calculaEvolucionable(this) ){
			_esNoExplotableEvolucionable = Boolean.FALSE;
		}
		else{
			_esNoExplotableEvolucionable = Boolean.TRUE;
		}
		return _esNoExplotableEvolucionable.booleanValue();
	}

	public boolean hayAlgunBloqueEnEsquinaNoObjetivo() {
		if( _hayAlgunBloqueEnEsquinaNoObjetivo != null ){
			return _hayAlgunBloqueEnEsquinaNoObjetivo.booleanValue();
		}
		_hayAlgunBloqueEnEsquinaNoObjetivo = new Boolean( hayAlgunBloqueEnEsquinaNoObjetivoImpl() );
		return _hayAlgunBloqueEnEsquinaNoObjetivo.booleanValue();
	}
		
		
	private boolean hayAlgunBloqueEnEsquinaNoObjetivoImpl(){		
		for( Punto p: _bloques ){
			Objeto o = getObjeto(p);
		
			if( o.hayObjetivo() ){
				continue;
			}
			
			if( !o.hayBloque() ){
				continue;
			}
			
			Objeto ar = getObjeto(p.mueve(Direccion.ARRIBA));
			Objeto ab = getObjeto(p.mueve(Direccion.ABAJO));
			Objeto iz = getObjeto(p.mueve(Direccion.IZQUIERDA));
			Objeto de = getObjeto(p.mueve(Direccion.DERECHA));
			
			if( ar.hayPared() && iz.hayPared() ){
				return true;
			}
			if( ar.hayPared() && de.hayPared() ){
				return true;
			}
			if( ab.hayPared() && iz.hayPared() ){
				return true;
			}
			if( ab.hayPared() && de.hayPared() ){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hayCajasFormandoUnCuadrado(){
		
		if( _hayCajasFormandoUnCuadrado != null ){
			return _hayCajasFormandoUnCuadrado.booleanValue();
		}
		_hayCajasFormandoUnCuadrado = new Boolean( hayCajasFormandoUnCuadradoImpl() );
		return _hayCajasFormandoUnCuadrado.booleanValue();
	}
	
	private boolean hayCajasFormandoUnCuadradoImpl(){
		for( Punto p: _bloques ){
			
			Objeto o = getObjeto(p);
			
			// SI LA CAJA ESTA EN UN OBJETIVO, SE PUEDE
			if( o.hayObjetivo() ){
				continue;
			}
				
			// SI LA CAJA ESTA EN UN CUADRADO, NO SE PUEDE
			Objeto ar = getObjeto(p.mueve(Direccion.ARRIBA));
			Objeto ab = getObjeto(p.mueve(Direccion.ABAJO));
			Objeto iz = getObjeto(p.mueve(Direccion.IZQUIERDA));
			Objeto de = getObjeto(p.mueve(Direccion.DERECHA));

			Objeto arde = getObjeto(p.mueve(Direccion.ARRIBA).mueve(Direccion.DERECHA));
			Objeto abde = getObjeto(p.mueve(Direccion.ABAJO).mueve(Direccion.DERECHA));
			Objeto ariz = getObjeto(p.mueve(Direccion.ARRIBA).mueve(Direccion.IZQUIERDA));
			Objeto abiz = getObjeto(p.mueve(Direccion.ABAJO).mueve(Direccion.IZQUIERDA));
			
			if( !ar.hayVacioPosible() && !arde.hayVacioPosible() && !de.hayVacioPosible() ){
				return true;
			}
			if( !ar.hayVacioPosible() && !ariz.hayVacioPosible() && !iz.hayVacioPosible() ){
				return true;
			}
			if( !ab.hayVacioPosible() && !abde.hayVacioPosible() && !de.hayVacioPosible() ){
				return true;
			}
			if( !ab.hayVacioPosible() && !abiz.hayVacioPosible() && !iz.hayVacioPosible() ){
				return true;
			}
		}
		return false;
	}

	
	public static void main(String[] args) {
		Tablero t = new Tablero();
		t.parsea( Tableros.tablerosDePrueba[5] );
		
		t.dump(System.out);
		
		EstadoTablero e = copia(t);
		e.dump(System.out);
		
		EstadoTablero[] hijos = e.creaHijos();
		for( int i = 0 ; i < Direccion.values().length ; i++ ){
			if( hijos[i] != null ){
				System.out.println( Direccion.values()[i].name() );
				hijos[i].dump(System.out);
			}
		}
	}

	@Override
	public Punto personaje() {
		return _personaje;
	}

}