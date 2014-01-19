package movebox;

import static movebox.Objeto.*;

public class TableroSinFichas extends Tablero {

	public void inicializa( ITablero t ){
		for( int x = 0 ; x < t.ancho() ; x++ ){
			for( int y = 0 ; y < t.alto() ; y++ ){
				setObjeto(x,y, t.getObjeto(x, y) );
			}
		}
	}
	
	@Override
	public Objeto getObjeto(int x, int y) {
		
		Objeto o = super.getObjeto(x, y);
		if( o == PERSONAJE || o == BLOQUE ){
			o = VACIO;
		}
		
		if( o == BLOQUEOBJETIVO || o == PERSONAJEOBJETIVO ){
			o = OBJETIVO;
		}
		
		return o;
	}
}
