package movebox;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;

import movebox.CalculadorDeZonas.Zona;

public class ColocadorDePersonaje {

	private static final PrintStream DEBUG = System.out;//Movebox.NULLDEBUG;//DEBUG;
	
	
	private HashSet<Punto> _puntosVisitados = new HashSet<Punto>();
	
	
	public ColocadorDePersonaje(EstadoTablero e) {
		_tableroOriginal = e;
	}

	public static Punto[] puntosALosQueLlegar(Zona zona){
		return zona.borde();
	}
	


	private ITablero _tableroOriginal;
	
	private void check(Zona zona){
		check(zona.tablero());
	}
	
	private void check(ITablero tablero) {
		EstadoTablero e = EstadoTablero.convierteAEstadoTablero(tablero);
		while( e != null ){
			if( e == _tableroOriginal ){
				return;
			}
			e = e.getPadre();
		}
		throw new IllegalStateException();
	}

	public boolean puedeLlegar(Zona zona){
		check(zona);
		if( zona.hayPersonajeEnInterior() ){
			DEBUG.println( "El personaje esta en el interior");
			return true;
		}
		
		Punto[] puntos = puntosALosQueLlegar(zona);
		EstadoTablero e = EstadoTablero.convierteAEstadoTablero(zona.tablero());
		
		_puntosVisitados.add(e.personaje());
		
		DEBUG.println( "*******************************************" );
		DEBUG.println( "Puntos ya visitados:" + _puntosVisitados );
		DEBUG.println( "Puntos a llegar:" + Arrays.asList(puntos) );
		e.dump(DEBUG);
		
		for( Punto p: puntos ){
			
			if( e.getObjeto(p).hayPared() ){
				// SI HAY PARED NO PUEDO LLEGAR
				DEBUG.println( "Hay pared en " + p );
				continue;
			}

			Zona interiorDeTablero = e.zonaInterior();
			if( !interiorDeTablero.enInteriorYBorde(p) ){
				// SI EL PUNTO NO ESTA EN EL INTERIOR DEL TABLERO EL
				// PERSONAJE NO PUEDE LLEGAR
				DEBUG.println( "No esta en el interior del tablero " + p );
				continue;
			}
			
			if( puedeLlegar( e, p, new HashSet<EstadoTablero>() ) ){
				DEBUG.println( "Se puede llegar a " + p );
				return true;
			}
			
			DEBUG.println( "No se puede llegar a " + p );
		}
		
		return false;
	}
	
	

	private boolean puedeLlegar(ITablero t, Punto p, HashSet<EstadoTablero> estadosVisitados ){

		Punto personaje = t.personaje();
		_puntosVisitados.add(personaje);

		if( _puntosVisitados.contains(p) ){
			t.dump(DEBUG);
			DEBUG.println( "  " + p + "  he llegado");
			return true;
		}

		EstadoTablero e = EstadoTablero.convierteAEstadoTablero(t);
		check(e);
		
		for( Direccion d: Direccion.values() ){
			EstadoTablero hijo = e.creaHijo(d);
			if( hijo == null ){
				DEBUG.println( "  " + p + "  no se puede mover " + d );
				continue;
			}
			DEBUG.println( "  " + p + "   muevo " + d );
			
			if( estadosVisitados.contains(hijo) ){
				DEBUG.println( "  " + p + "  " + d + " ya he visitado este estado" );
				continue;
			}
			
			estadosVisitados.add(hijo);
			
			if( hijo.esNoExplotableSimple() ){
				DEBUG.println( "  " + p + "  " + d + " sale un estado no es explotable" );
				continue;
			}
			
			hijo.dump(DEBUG);
			if( puedeLlegar( hijo, p, estadosVisitados ) ){
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) throws IOException {
		
		String[] lineas = {
				
				
				"###########",
				"#     *...##",
				"# ++++#....#",
				"# @+ +..***##",
				"##   # ##.. #",
				"# +++#  ##  #",
				"#    ## #  ##",
				"#  ++ #    #",
				"#     ###  #",
				"####### ####",
					
		};
		
		Tablero t = new Tablero();
		t.parsea(lineas);
		
		
		EstadoTablero e = EstadoTablero.convierteAEstadoTablero(t);
		Punto p = Punto.P(6,1);
		ColocadorDePersonaje cdp = new ColocadorDePersonaje(e);
		boolean ret = cdp.puedeLlegar( e, p, new HashSet<EstadoTablero>() );
		e.dump(DEBUG);
		DEBUG.println( "puede llegar a " + p + ":" + ret );
	}
}
