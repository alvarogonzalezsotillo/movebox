package movebox;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import movebox.CalculadorDeZonas.Zona;

public class CalculadorDeZonasEvolucionables {

	private static final PrintStream DEBUG = Movebox.NULLDEBUG;//System.out;
	
	private static ColocadorDePersonaje _colocadorDePersonaje;
	
	public static boolean calculaEvolucionable(ITablero t){
		
		System.out.println( "*******************************************" );
		System.out.println( "*******************************************" );
		System.out.println( "*******************************************" );
		System.out.println( "*******************************************" );

		//DEBUG.println( "CalculaEvolucionable");
		//t.dump(DEBUG);
		
		EstadoTablero e = EstadoTablero.convierteAEstadoTablero(t);
		_colocadorDePersonaje = new ColocadorDePersonaje(e);
		CalculadorDeZonas cz = new CalculadorDeZonas(e);
		for( Zona zona: cz.zonas() ){
			if( !calculaEvolucionable(zona) ){
				//t.dump(DEBUG);
				//DEBUG.println("*******************************************CalculaEvolucionable(" + t.id() + "): false" );
				return false;
			}
		}
		//t.dump(DEBUG);
		//DEBUG.println("*******************************************CalculaEvolucionable(" + t.id() + "): true" );
		return true;
	}
	
	private static boolean calculaEvolucionable(Zona zona) {
		ArrayList<Zona> list = new ArrayList<Zona>();
		return calculaEvolucionableImpl(list, zona);
	}
	
	private static boolean calculaEvolucionableImpl( List<Zona> zonasOriginales, Zona zona ){
		Zona zonaOriginal = null;
		if( zonasOriginales.size() >= 1 ){
			zonaOriginal = zonasOriginales.get(zonasOriginales.size()-1);
		}
//		DEBUG.println( "calculaEvolucionableImpl" );
//		DEBUG.println( "  zonasOriginales:" + zonasOriginales );
//		DEBUG.println( "  zonaOriginal:" + zonaOriginal );
//		DEBUG.println( "  zona:" + zona.id() );
//		zona.dump(DEBUG);
//		zona.tablero().dump(DEBUG);
//		if( zonaOriginal != null ){
//			zonaOriginal.dump(DEBUG);
//			zonaOriginal.tablero().dump(DEBUG);
//		}

		// SI NO HAY BLOQUES, NO ES INTERESANTE
		if( zona.bloques() == 0 ){
//			DEBUG.println( "  " + zona.id() + " no tiene bloques" );
			return true;
		}
		
		// MIRO LAS CONDICIONES DE BASE PARA SABER SI ES EVOLUCIONABLE
		EstadoTablero e = EstadoTablero.convierteAEstadoTablero( zona.tablero() );
		if( e.esNoExplotableSimple() ){
//			DEBUG.println( "  " + zona.id() + " es no explotable simple" );
			return false;
		}

		// SI NO SE PUEDE LLEGAR A LA ZONA NO ES EVOLUCIONABLE
		boolean personajeValido = zonaOriginal == null;

		if( personajeValido && !_colocadorDePersonaje.puedeLlegar(zona) ){
			zona.dump(System.out);
			zona.tablero().dump(System.out);
			System.out.println( "  " + zona.id() + " no se puede llegar: " + Arrays.asList( ColocadorDePersonaje.puntosALosQueLlegar(zona) ) );
			System.exit(0);
			return false;
		}
		
		// ES EVOLUCIONABLE SI ES ANCHO
		if( !zona.esDeAnchoUno() ){
//			DEBUG.println( "  " + zona.id() + " no es de ancho 1" );
			return true;
		}
		
		// SI ESTA EL PERSONAJE, SE PODRA ABRIR Y JUNTAR CON OTRAS ZONAS
		// ESTO SOLO SE TIENE EN CUENTA SI LA ZONA Y LA ZONA ORIGINAL SON IGUALES,
		// SI NO PUEDE SER UN MOVIMIENTO "FICTICIO" PARA VER SI SE ABRE LA ZONA
		if( zona.hayPersonajeEnInterior() && personajeValido ) {
//			DEBUG.println( "  " + zona.id() + " tiene el personaje" );
			return true;
		}


		// SI HAY SUFICIENTES OBJETIVOS PARA LOS BLOQUES, ES EVOLUCIONABLE
		if( zona.bloques() <= zona.objetivos() ){
//			DEBUG.println( "  " + zona.id() + " tiene menos bloques o igual que objetivos" );
			return true;
		}
		
		// SI LA ZONA SE HA SALIDO FUERA DE LA ZONA ORIGINAL, ES EVOLUCIONABLE
		if( zonaOriginal != null && !zona.contenidaEn(zonaOriginal) ){
//			DEBUG.println( "  " + zona.id() + " se ha salido fuera de la zona original " + zonaOriginal.id() );
			return true;
		}

		// MOVEMOS LOS BLOQUES HACIA ADENTRO (YA QUE EL PERSONAJE ESTA FUERA)			
		// ES EVOLUCIONABLE SI CONSEGUIMOS JUNTAR ESTA ZONA CON OTRAS ZONAS, O LO
		// QUE ES IGUAL, SI PODEMOS METER EL PERSONAJE PARA EMPUJAR LOS BLOQUES A
		// OTRO SITIO
		PuntoYDireccion[] puntosDePrueba = puntosDePruebaDePersonaje(zona,e);
//		DEBUG.println( "  zona " + zona.id() + ": probare con personaje en puntos " + Arrays.asList(puntosDePrueba) );
		
		for( PuntoYDireccion pd: puntosDePrueba ){
			
			Punto p = pd.p;
			Direccion d = pd.d;
			
			EstadoTablero hijo = e.creaHijo(p, d);
			if( hijo == null ){
				// ESTE MOVIMIENTO ERA IMPOSIBLE, PORQUE EL PERSONAJE DEBERIA
				// ESTAR EN UNA PARED O PORQUE UNA CAJA ACABARIA DENTRO DE
				// UNA PARED
				continue;
			}
			
			// AHORA HAY QUE VER SI CON EL MOVIMIENTO SE HA ABIERTO LA ZONA
			// PUEDE OCURRIR QUE:
			// 1- SE ABRA LA ZONA (LAS ZONAS QUE SOLAPAN NO ESTAN CONTENIDAS EN LA ZONA ORIGINAL)
			// 2- EL ESTADO QUEDE NO EVOLUCIONABLE POR LAS CONDICIONES DE BASE
			// 3- SE CREEN VARIAS ZONAS DENTRO DE LA ZONA QUE HAYA QUE COMPROBAR
			//    RECURSIVAMENTE
			// 4- LA ZONA DESAPARECE, POR LO QUE SE HA CREADO UN CUADRADO O UN BLOQUE
			//    EN ESQUINA
			Zona[] zonas = zonasSolapadasConZona(zona, hijo);
			if( zonas.length == 0 ){
				continue;
			}
			int zonasAbiertas = 0;
			for (Zona nuevaZona : zonas) {
				List<Zona> list = new ArrayList<Zona>();
				list.addAll(zonasOriginales);
				list.add(zona);
				if( calculaEvolucionableImpl( list, nuevaZona) ){
					// SI HE CONSEGUIDO ABRIR LA ZONA, AHORA TENGO QUE
					// VER SI EL TABLERO QUE LE QUEDA A LA ZONA NUEVA
					// ES EVOLUCIONABLE (QUE NO LE HE HECHO UN ROTO A OTRA
					// ZONA
					// PERO ESO ES UNA BUSQUEDA QUE PUEDE DAR LUGAR A CICLOS
					// ASI QUE SUPONGO QUE ES EVOLUCIONABLE
					zonasAbiertas++;
				}
				else{
					// HAY UNA ZONA NO ABIERTA, NO HACE FALTA SEGUIR
					break;
				}
			}
			if( zonasAbiertas == zonas.length ){
//				DEBUG.println( "  " + zona.id() + " moviendo desde " + p + " " + d + " se pueden abrir las zonas" );  
				return true;
			}
		}
		
		// SI LLEGO AQUI, NO HE CONSEGUIDO ABRIR LA ZONA, ASI QUE
		// NO ES EVOLUCIONABLE
//		DEBUG.println( "  " + zona.id() + " no se pueden abrir las zonas" );
		return false;
	}

	private static class PuntoYDireccion{
		public Punto p;
		public Direccion d;
		public PuntoYDireccion(Punto p, Direccion d){
			this.p = p;
			this.d = d;
		}
		@Override
		public String toString() {
			return p + " " + d;
		}
	}
	
	private static PuntoYDireccion[] puntosDePruebaDePersonaje(Zona zona, EstadoTablero e) {
		ArrayList<PuntoYDireccion> ret = new ArrayList<PuntoYDireccion>();
		
		
		Punto[] exterior = zona.exteriorYBordeVacio();
		for( Punto p: exterior ){
			Objeto o = e.getObjeto(p);
			if( o.hayPared() ){
				// NO ME PUEDO COLOCAR EN UNA PARED
				continue;
			}
			
			
			// MUEVO EL PERSONAJE
			for (Direccion d : Direccion.values() ) {
				
				Punto destino = p.mueve(d);
				
				if( !zona.enInteriorYBorde(destino) ){
					// SI EL PERSONAJE NO SE ADENTRA EN LA ZONA, NO LO HAGO
					continue;
				}
				
				o = e.getObjeto(destino);
				if( !o.hayBloque() ){
					// SI NO HAY BLOQUE A DONDE VOY A MOVER, NO LO HAGO
					continue;
				}
				
				ret.add( new PuntoYDireccion(p, d) );
			}
			
		}
		
		return (PuntoYDireccion[]) ret.toArray(new PuntoYDireccion[ret.size()]);
	}
	
	private static Zona[] zonasSolapadasConZona( Zona continente, ITablero t ){
		ArrayList<Zona> ret = new ArrayList<Zona>();
		CalculadorDeZonas cz = new CalculadorDeZonas(t);
		Zona[] zonas = cz.zonas();
		for( Zona z: zonas ){
			if( z.solapaCon(continente) ){
				ret.add(z);
			}
		}
		return (Zona[]) ret.toArray(new Zona[ret.size()]);
	}

	public static void main(String[] args) throws IOException {
		ITablero[] tableros = Tableros.parseaTableros("CalculadorDeZonasEvolucionables.txt");
		boolean e = calculaEvolucionable(tableros[0]);
		tableros[0].dump(System.out);
		System.out.println( "evolucionable:" + e );
	}
}
