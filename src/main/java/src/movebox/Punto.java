package movebox;

import java.util.Arrays;
import java.util.Comparator;


public class Punto {

	private static final int AMPLITUD = 20;
	private static final Comparator<Punto> ComparadorPorIndice = new Comparator<Punto>() {
		@Override
		public int compare(Punto o1, Punto o2) {
			return o1.indice() - o2.indice();
		}
	};
	public static final Punto[] ZEROARRAY = new Punto[0];
	private int _x;
	private int _y;
	private int _index;
	
	private static Punto[] _cache;

	private int indice(){
		return coordenadasAIndice(this);
	}
	
	private static int coordenadasAIndice(Punto p){
		return coordenadasAIndice(p.x(), p.y() );
	}
	
	private static int coordenadasAIndice(int x, int y){
		int ret = (y+AMPLITUD)*(AMPLITUD*2) + (x+AMPLITUD);
		return ret;
	}
	
	private static Punto[] getCache(){
		if (_cache == null) {
			_cache = new Punto[coordenadasAIndice(AMPLITUD-1, AMPLITUD-1)+1];
			for( int y = -AMPLITUD ; y < AMPLITUD ; y++ ){
				for( int x = -AMPLITUD ; x < AMPLITUD ; x++ ){
					int index = coordenadasAIndice(x, y);
					Punto p = new Punto(x,y);
					_cache[index] = p;
				}
			}
		}

		return _cache;
	}
	
	private Punto(int x, int y){
		_y = y;
		_x = x;
		_index = coordenadasAIndice(x, y);
	}
	
	public int x(){
		return _x;
	}
	
	public int y(){
		return _y;
	}
	
	public static Punto P(int x, int y){
		int index = coordenadasAIndice(x, y);
		return getCache()[index];
	}
	
	public Punto mueve( Direccion d ){
		if( d == Direccion.ARRIBA ){
			return P( x(), y()-1 );
		}
		if( d == Direccion.ABAJO ){
			return P( x(), y()+1 );
		}
		if( d == Direccion.IZQUIERDA ){
			return P( x()-1, y() );
		}
		if( d == Direccion.DERECHA ){
			return P( x()+1, y() );
		}
		throw new IllegalArgumentException();
	}
	
	@Override
	public String toString() {
		return "(" + x() + "," + y() +")";
	}
	
	public static void ordenaArray( Punto p[] ){
		Arrays.sort( p, ComparadorPorIndice );
	}
	
	public static int buscaPuntoEnArray( Punto p, Punto a[] ){
		int indice = p.indice();
		
		for( int i = 0 ; i < a.length && indice >= a[i].indice() ; i++ ){
			if( a[i] == p ){
				return i;
			}
		}
		
		return -1;
	}
	
	public static Punto[] copiaArray( Punto p[] ){
		Punto[] ret = new Punto[p.length];
		System.arraycopy(p, 0, ret, 0, p.length);
		return ret;
	}
	
	
	@Override
	public int hashCode() {
		return _index; 
	}
	
	public static int hashCode( Punto p[] ){
		int ret = 0;
		ordenaArray(p);
		for (Punto punto : p) {
			ret <<= 3;
			ret ^= punto.hashCode();
		}
		return ret;
	}
	
	
	public static void main(String[] args) {
		Punto p = P(1,1);
		System.out.println(p);
		for( Direccion d: Direccion.values() ){
			Punto m = p.mueve(d);
			System.out.println(d.name() + ":" + m + " :" + m.hashCode() );
		}
		
		Punto a[] = { P(0,1), P(4,3), P(3,2) };
		System.out.println( hashCode(a) );
	}

	public static boolean equals(Punto[] p1, Punto[] p2) {
		if( p1.length != p2.length ){
			return false;
		}
		
		for( int i = 0 ; i < p1.length ; i++ ){
			if( p1[i] != p2[i] ){
				return false;
			}
		}
		return true;
	}
	
	
}
