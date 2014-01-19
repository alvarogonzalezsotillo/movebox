package movebox;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class Tablero implements ITablero{
	

	protected Objeto[][] _celdas;
	private int _alto;
	private int _ancho;
	
	public Objeto getObjeto( Punto p ){
		return getObjeto( p.x(), p.y() );
	}
	
	public Objeto getObjeto(int x, int y){
		if( _celdas == null || _celdas.length <= x || x < 0 ){
			return Objeto.PARED;
		}
		
		if( _celdas[x] == null || _celdas[x].length <= y || y < 0 ){
			return Objeto.PARED;
		}
		
		Objeto ret = _celdas[x][y];
		return ret;
	}

	public Tablero(){
	}

	public void setObjeto(Punto p, Objeto o){
		int x = p.x();
		int y = p.y();
		setObjeto( x, y, o);
	}
	
	public void setObjeto(int x, int y, Objeto o){
		
		if( x < 0 ) throw new IllegalArgumentException();
		if( y < 0 ) throw new IllegalArgumentException();
		
		// SITIO PARA LA X
		if( _celdas == null ){
			_celdas = new Objeto[x+1][];
			Arrays.fill(_celdas, Objeto.ZEROARRAY);
		}
		if( _celdas.length <= x ){
			Objeto[][] c = new Objeto[x+1][];
			System.arraycopy(_celdas, 0, c, 0, _celdas.length);
			Arrays.fill(c,_celdas.length,c.length-1, Objeto.ZEROARRAY);
			_celdas = c;
		}
		
		// SITIO PARA LA Y
		if( _celdas[x] == null ){
			_celdas[x] = new Objeto[y+1];
			Arrays.fill(_celdas[x], Objeto.PARED);
		}
		if( _celdas[x].length <= y ){
			Objeto[] c = new Objeto[y+1];
			System.arraycopy(_celdas[x], 0, c, 0, _celdas[x].length);
			Arrays.fill(c,_celdas[x].length,c.length-1,Objeto.PARED);
			_celdas[x] = c;
		}

		// PONGO EL OBJETO
		_celdas[x][y] = o;
		
		// CALCULO EL TAMANIO
		actualizaAnchoAlto();
	}

	protected void actualizaAnchoAlto() {
		_alto = 0;
		_ancho = 0;
		if( _celdas == null ){
			return;
		}
		
		_ancho = _celdas.length;
		for( int x = 0 ; x < _celdas.length ; x++ ){
			_alto = Math.max(_alto, _celdas[x].length);
		}
	}
	
	public int alto(){
		return _alto;
	}
	
	public int ancho(){
		return _ancho;
	}
	
	
	public void dump(PrintStream out){
		dump( out, this );
	}
	
	public static void dump( PrintStream out, ITablero t ){
		// COORDENADAS
		out.print( " " );
		for( int x = 0 ; x < t.ancho(); x++ ){
			out.print(x%10);
		}
		
		out.println();
		for( int y = 0 ; y < t.alto() ; y++ ){
			
			out.print( y%10 );
			
			for( int x = 0 ; x < t.ancho() ; x++ ){
				Objeto o = t.getObjeto(x, y);
				out.print( o.toChar() );
			}
			out.println();
		}
	}
	
	public void parsea( String[] t ){
		for( int y = 0 ; y < t.length ; y++ ){
			for( int x = 0 ; x < t[y].length() ; x++ ){
				Objeto o = Objeto.fromChar( t[y].charAt(x) );
				setObjeto( x, y, o);
			}
		}
	}

	public void parsea( Iterable<String> t ){
		ArrayList<String> list = new ArrayList<String>();
		for( String s: t ){
			list.add(s);
		}
		parsea( (String[]) list.toArray(new String[list.size()]) );
	}

	public static void main(String[] args) {
		{
			Tablero t = new Tablero();
			t.parsea( Tableros.tablerosDePrueba[5] );
			t.dump(System.out);
		}
		{
			Tablero t = new TableroSinFichas();
			t.parsea( Tableros.tablerosDePrueba[5] );
			t.dump(System.out);
		}
	}

	@Override
	public int id() {
		return System.identityHashCode(this);
	}

	@Override
	public Punto personaje() {
		throw new UnsupportedOperationException();
	}
}
