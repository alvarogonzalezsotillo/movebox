package movebox;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class CalculadorDeZonas implements ITablero{
	
	private static Contador _contadorZonas = new Contador();

	
	public class Zona implements ITablero{
		private ArrayList<Punto> _interiorList = new ArrayList<Punto>();
		private Objeto _nombreZona;
		private Punto[] _borde;
		public Punto[] _interior;
		public Punto[] _interiorYBorde;
		private Punto[] _exteriorYBordeVacio;
		public int _bloques = -1;
		public int _objetivos = -1;
		private int _hayPersonaje = -1;
		private int _bloquesEnObjetivo = -1;
		private Punto[] _esquinas;
		private int _id;
		private int _bloquesSinObjetivo = -1;
				
		public ITablero tablero(){
			return _tablero;
		}
		
		public Zona(Objeto nombreZona ){
			_nombreZona = nombreZona;
			_id = _contadorZonas.next();
		}
		
		public void agregaInterior( Punto p ){
			_interiorList.add(p);
		}
		
		private Punto[] calculaBorde(){
			HashSet<Punto> borde = new HashSet<Punto>();
			
			Direccion[] direcciones = Direccion.values();
			for( Punto p: _interiorList ){

				for( Direccion d: direcciones ){
					Punto p2 = p.mueve(d);
					borde.add( p2 );
					for( Direccion d2: direcciones ){
						if( d == d2 ){
							continue;
						}
						borde.add( p2.mueve(d2) );
					}
				}
			}
			
			borde.removeAll(_interiorList);
			
			return (Punto[]) borde.toArray(new Punto[borde.size()]);
		}

		private Punto[] calculaExteriorYBordeVacio(){
			HashSet<Punto> exterior = new HashSet<Punto>();
			
			Direccion[] direcciones = Direccion.values();
			for( Punto p: borde() ){

				for( Direccion d: direcciones ){
					Punto p2 = p.mueve(d);
					exterior.add( p2 );
					for( Direccion d2: direcciones ){
						if( d == d2 ){
							continue;
						}
						exterior.add( p2.mueve(d2) );
					}
				}
			}
			
			exterior.removeAll(_interiorList);
			exterior.removeAll( Arrays.asList(borde()) );
			
			// PUNTOS DEL BORDE SIN BLOQUE Y SIN CAJA
			for( Punto p: borde() ){
				Objeto o = _tablero.getObjeto(p);
				if( o.hayVacio() || o.hayPersonaje() ){
					exterior.add(p);
				}
			}
			
			return (Punto[]) exterior.toArray(new Punto[exterior.size()]);
		}
		
		private Punto[] calculaEsquinas(){
			// ES UNA ESQUINA LA POSICION DEL BORDE QUE TIENE COMPAÑEROS
			// POR LOS LADOS Y POR ARRIBA-ABAJO
			ArrayList<Punto> esquinas = new ArrayList<Punto>();
			//Movebox.DEBUG.println( "borde:" + Arrays.asList(borde()));
			for( Punto p: borde() ){
				Punto ar = p.mueve(Direccion.ARRIBA);
				Punto ab = p.mueve(Direccion.ABAJO);
				Punto iz = p.mueve(Direccion.IZQUIERDA);
				Punto de = p.mueve(Direccion.DERECHA);
				
				boolean arab = Punto.buscaPuntoEnArray(ar, borde())>=0 || Punto.buscaPuntoEnArray(ab, borde())>=0;
				boolean izde = Punto.buscaPuntoEnArray(iz, borde())>=0 || Punto.buscaPuntoEnArray(de, borde())>=0;				
				boolean esquina = arab && izde;
				if( esquina ){
					esquinas.add(p);
				}
			}
			
			return (Punto[]) esquinas.toArray(new Punto[esquinas.size()]);
		}
		
		
		public Punto[] esquinas(){
			if( _esquinas == null ){
				_esquinas = calculaEsquinas();
				Punto.ordenaArray(_esquinas);
			}
			return _esquinas;
		}
		
		public Punto[] borde(){
			if( _borde == null ){
				_borde = calculaBorde();
				Punto.ordenaArray(_borde);
			}
			return _borde;
		}

		public Punto[] exteriorYBordeVacio(){
			if( _exteriorYBordeVacio == null ){
				_exteriorYBordeVacio = calculaExteriorYBordeVacio();
				Punto.ordenaArray(_exteriorYBordeVacio);
			}
			return _exteriorYBordeVacio;
		}
		
		public Punto[] interior(){
			if( _interior == null ){
				_interior = (Punto[]) _interiorList.toArray(new Punto[_interiorList.size()]);
				Punto.ordenaArray(_interior);
			}
			return _interior;
		}
		
		public Punto[] interiorYBorde(){
			if( _interiorYBorde == null ){
				Punto[] b = borde();
				Punto[] i = interior();
				_interiorYBorde = new Punto[b.length+i.length];
				System.arraycopy(b, 0, _interiorYBorde, 0, b.length);
				System.arraycopy(i, 0, _interiorYBorde, b.length, i.length );
				Punto.ordenaArray(_interiorYBorde);
			}
			return _interiorYBorde;
		}
		
		public Objeto nombre(){
			return _nombreZona;
		}
		
		@Override
		public String toString() {
			return "" + id(); 
		}

		public int bloques(){
			if( _bloques != -1 ){
				return _bloques ;
			}
		
			_bloques = 0;
			for( Punto p: interiorYBorde() ){
				Objeto t = (Objeto) _tablero.getObjeto(p);
				if( t.hayBloque() ){
					_bloques++;
				}
			}
			return _bloques;
		}

		public int objetivos(){
			if( _objetivos != -1 ){
				return _objetivos ;
			}
		
			_objetivos = 0;
			for( Punto p: interiorYBorde() ){
				Objeto t = _tablero.getObjeto(p);
				if( t.hayObjetivo() ){
					_objetivos++;
				}
			}
			return _objetivos;
		}

		public int objetivosSinBloque(){
			return objetivos() - bloquesEnObjetivo();
		}
		
		public int bloquesEnObjetivo(){
			if( _bloquesEnObjetivo != -1 ){
				return _bloquesEnObjetivo  ;
			}
		
			_bloquesEnObjetivo = 0;
			for( Punto p: interiorYBorde() ){
				Objeto t = _tablero.getObjeto(p);
				if( t.hayObjetivo() && t.hayBloque() ){
					_bloquesEnObjetivo++;
				}
			}
			return _bloquesEnObjetivo;
		}
		
		public int bloquesSinObjetivo(){
			if( _bloquesSinObjetivo  != -1 ){
				return _bloquesSinObjetivo  ;
			}
		
			_bloquesSinObjetivo = 0;
			for( Punto p: interiorYBorde() ){
				Objeto t = _tablero.getObjeto(p);
				if( !t.hayObjetivo() && t.hayBloque() ){
					_bloquesSinObjetivo++;
				}
			}
			return _bloquesSinObjetivo;
		}
		
		public boolean hayPersonajeEnInterior(){
			if( _hayPersonaje  != -1 ){
				return _hayPersonaje==1;
			}
			_hayPersonaje = 0;
			for( Punto p: interior() ){
				Objeto t = (Objeto) _tablero.getObjeto(p);
				if( t.hayPersonaje() ){
					_hayPersonaje = 1;
				}
			}
			return _hayPersonaje==1;
		}
		
		
		
		public boolean enInterior(Punto p) {
			int indice = Punto.buscaPuntoEnArray(p, interior());
			return indice >= 0;
		}
		
		public boolean enBorde(Punto p) {
			int indice = Punto.buscaPuntoEnArray(p, borde());
			return indice >= 0;
		}
		
		
		public boolean enInteriorYBorde(Punto p) {
			int indice = Punto.buscaPuntoEnArray(p, interiorYBorde());
			return indice >= 0;
		}

		public boolean esDeAnchoUno( ){
			// NO ES DE ANCHO UNO SI CABE UN CUADRADO DE LADO DOS
			Direccion[] direcciones = Direccion.values();
			for( Punto p: _interiorList ){

				boolean todosDentro = true;
				for( Direccion d1 : direcciones ){
					for( Direccion d2: direcciones ){
						if( d1 == d2 ){
							continue;
						}
						todosDentro = enInterior(p.mueve(d1).mueve(d2));
						if( !todosDentro ){
							break;
						}
					}
					if( !todosDentro ){
						break;
					}
				}
				if( todosDentro ){
					return false;
				}
			}
			
			return true;
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
		public void dump(PrintStream out) {
			out.println( "Zona " + nombre() + "(" + id() + ")  tablero:" + tablero().id() );
			Tablero.dump(out, this);
		}

		public ITablero tableroConZona() {
			Tablero t = new Tablero();
			for( int x = 0 ; x < ancho() ; x++ ){
				for( int y = 0 ; y < alto() ; y++ ){
					Punto p = Punto.P(x, y);
					Objeto o = tablero().getObjeto(x,y);
					
					if( !enInteriorYBorde(p) ){
						// QUITO CAJAS Y PERSONAJES
						if( o == Objeto.BLOQUE || o == Objeto.PERSONAJE ){
							o = Objeto.VACIO;
						}
						if( o == Objeto.BLOQUEOBJETIVO || o == Objeto.PERSONAJEOBJETIVO ){
							o = Objeto.OBJETIVO;
						}
					}
					t.setObjeto(x,y,o);
				}
			}
			return t;
		}
		
		@Override
		public Objeto getObjeto(Punto p){
			if( Punto.buscaPuntoEnArray(p, esquinas() )>= 0){
				return Objeto.fromChar('+');
			}
			if( Punto.buscaPuntoEnArray(p, borde() )>= 0){
				return Objeto.fromChar('-');
			}
			if( Punto.buscaPuntoEnArray(p, interior() )>= 0){
				return nombre();
			}
			return Objeto.fromChar(' ');
		}

		@Override
		public Objeto getObjeto(int x, int y) {
			return getObjeto( Punto.P(x, y));
		}

		public boolean contenidaEn(Zona continente) {
			for( Punto p: interior() ){
				if( !continente.enInterior(p) ){
					return false;
				}
			}
			return true;
		}

		public boolean solapaInteriorconInterior(Zona continente) {
			for( Punto p: interior() ){
				if( continente.enInterior(p) ){
					return true;
				}
			}
			return false;
		}

		
		public boolean solapaBordeConBorde(Zona continente) {
			for( Punto p: interiorYBorde() ){
				if( continente.enInteriorYBorde(p) ){
					return true;
				}
			}
			return false;
		}
		
		public int id() {
			return _id;
		}

		@Override
		public Punto personaje() {
			throw new UnsupportedOperationException();
		}

	
	}

	private static final Objeto SINZONA = Objeto.fromChar('X');
	private Contador _contador = new Contador();
	private ITablero _tablero;
	private Zona[][] _mapa;
	private Zona[] _zonas;
	
	public CalculadorDeZonas( ITablero tablero ){
		_tablero = EstadoTablero.convierteAEstadoTablero(tablero);
	}
	
	public Zona zona(int x, int y) {
		return zona( Punto.P(x, y) );
	}
	
	public Zona zona( Punto p ){
		for( Zona z: zonas() ){
			if( z.enInteriorYBorde(p) ){
				return z;
			}
		}
		return null;
	}

	public Zona[] zonas(){
		if( _zonas != null ){
			return _zonas;
		}
		_zonas = calculaZonas();
		return _zonas;
	}
	
	private Zona[] calculaZonas(){
		
		ITablero t = _tablero;
		
		// MAPA PARA EL CALCULO DE ZONAS
		_mapa = new Zona[t.ancho()][];
		for( int i = 0 ; i < _mapa.length ; i++ ){
			_mapa[i] = new Zona[t.alto()];
			Arrays.fill(_mapa[i], null);
		}
		
		// SE RECORRE EL MAPA
		ArrayList<Zona> zonas = new ArrayList<Zona>();
		for( int x = 0 ; x < t.ancho() ; x++ ){
			for( int y = 0 ; y < t.alto() ; y++ ){
				Zona zona = calculaZonaPara( x, y );
				if( zona != null ){
					zonas.add(zona);
				}
			}
		}
		
		return (Zona[]) zonas.toArray(new Zona[zonas.size()]);
	}

	private Zona calculaZonaPara(int x, int y) {
		if( yaTieneZona(x, y) ){
			return null;
		}
		Objeto nombreZona = Objeto.fromInteger(_contador.next());
		Zona zona = new Zona(nombreZona );
		rellenaZonaRecursiva( zona, Punto.P(x, y) );
		return zona;
	}
	
	private void rellenaZonaRecursiva(Zona zona, Punto p ){
		int x = p.x();
		int y = p.y();
		if( yaTieneZona(x, y) ){
			return;
		}
		_mapa[x][y] = zona;
		zona.agregaInterior( p );
		Direccion[] direcciones = Direccion.values();
		for( Direccion d: direcciones ){
			Punto punto = p.mueve(d);
			rellenaZonaRecursiva(zona, punto);
		}
	}

	private boolean yaTieneZona(int x, int y) {
		if( x < 0 || y < 0 || x >= ancho() || y >= alto() ){
			return true;
		}
		if( _mapa[x][y] != null ){
			return true;
		}
		Objeto objeto = (Objeto) _tablero.getObjeto(x, y);
		if( !objeto.hayVacioPosible() ){
			return true;
		}
		return false;
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
	public void dump(PrintStream out) {
		Tablero.dump(out, this);
	}

	@Override
	public Objeto getObjeto(Punto p) {
		return getObjeto(p.x(), p.y());
	}

	@Override
	public Objeto getObjeto(int x, int y) {
		Zona z = _mapa[x][y];
		if( z == null ){
			return SINZONA;
		}
		return z.nombre();
	}
	

	public static void main(String[] args) {
		System.out.println();
		Tablero t = new Tablero();
		t.parsea(Tableros.tablerosDePrueba[7]);
		t.dump(System.out);
		
		CalculadorDeZonas cz = new CalculadorDeZonas(t);
		cz.dump(System.out);
		for( Zona z : cz.zonas() ){
			z.dump(System.out);
		}
	}

	@Override
	public int id() {
		throw new NotImplementedException();
	}

	@Override
	public Punto personaje() {
		throw new UnsupportedOperationException();
	}
}
