package movebox;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.TreeSet;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ColocadorDeBloques {
	
	private boolean _usaEvolucionable = false;
	
	private class ListaDeEstadosYaExplorados{
		private HashMap<EstadoTablero,EstadoTablero> _estadosExplotados = new HashMap<EstadoTablero,EstadoTablero>();
		private HashMap<EstadoTablero,EstadoTablero> _estadosNoExplotables = new HashMap<EstadoTablero,EstadoTablero>();
		
		public void agrega( EstadoTablero e ){
			if( e.esExplotado() ){
				_estadosExplotados.put( e, e);
			}
			else if( esNoExplotable(e) ){
				_estadosNoExplotables.put(e,e);
			}
			else{
				throw new IllegalArgumentException();
			}
		}
		
		public boolean contains(EstadoTablero e){
			if( _estadosExplotados.containsKey(e) ){
				return true;
			}
			if( _estadosNoExplotables.containsKey(e) ){
				return true;
			}
			return false;
		}

		public int explotados(){
			return _estadosExplotados.size();
		}
		
		public int noExplotables(){
			return _estadosNoExplotables.size();
		}
	}
	
	private static class ListaDeEstadosOrdenadosPorHeuristica{
		private TreeSet<EstadoTablero> _ordenadosPorHeuristica = new TreeSet<EstadoTablero>(EstadoTablero.HEURISTICACOMPARATOR);
		private HashMap<EstadoTablero,EstadoTablero> _estados = new HashMap<EstadoTablero,EstadoTablero>();

		public void agrega( EstadoTablero e ){
			check();
			_ordenadosPorHeuristica.add(e);
			_estados.put(e, e);
			check();
		}

		private void check() {
			if( _ordenadosPorHeuristica.size() != _estados.size() ){
				throw new IllegalStateException();
			}
		}
		
		public void extrae( EstadoTablero e ){
			_ordenadosPorHeuristica.remove(e);
			_estados.remove(e);
			check();
		}
		
		public EstadoTablero estadoYaContenido( EstadoTablero e ){
			return _estados.get(e);
		}
		
		public boolean contains(EstadoTablero e){
			return estadoYaContenido(e) != null;
		}
		
		public EstadoTablero estadoConMenorHeuristica(){
			if( _ordenadosPorHeuristica.size() == 0 ){
				return null;
			}
			return _ordenadosPorHeuristica.first();
		}
		
		public int size(){
			return _estados.size();
		}
	}
	
	private ListaDeEstadosYaExplorados _estadosExplotados = new ListaDeEstadosYaExplorados();
	private HashSet<EstadoTablero> _estadosFinales = new HashSet<EstadoTablero>();
	private ListaDeEstadosOrdenadosPorHeuristica _estadosNoExplotados = new ListaDeEstadosOrdenadosPorHeuristica();
	private boolean _noHaySolucion;
	
	public ColocadorDeBloques(){
		this(false);
	}
	
	public ColocadorDeBloques(boolean usaEvolucionable){
		_usaEvolucionable = usaEvolucionable;
	}
	
	public void inicializa( ITablero t ){
		EstadoTablero e = EstadoTablero.copia(t);
		agrega(e);
	}
	
	private boolean esNoExplotable(EstadoTablero e) {
		boolean esNoExplotableSimple = e.esNoExplotableSimple();
		if( _usaEvolucionable ){
			boolean ret = e.esNoExplotableEvolucionable();
			if( ret == false && esNoExplotableSimple == true ){
				e.dump(System.err);
				throw new IllegalStateException( "No puede ser, evolucionable es mas restrictivo");
			}
			return ret;
		}
		else{
			return esNoExplotableSimple;
		}
	}

	private void agrega( EstadoTablero e ){
		
		// SI ES UN ESTADO FINAL
		if( e.esFinal() ){
			_estadosFinales.add(e);
		}
		
		// SI YA ESTA CONTENIDO EN LOS EXPLORADOS, NADA	
		if( _estadosExplotados.contains(e) ){
			return;
		}
		
		// SI ESTA EXPLOTADO O NO ES EXPLOTABLE
		if( e.esExplotado() || esNoExplotable(e) ){
			_estadosExplotados.agrega(e);
			return;
		}
		
		// SI SE PUEDE EXPLOTAR
		if( !_estadosNoExplotados.contains(e) ){
			// EL ESTADO NO ESTABA
			_estadosNoExplotados.agrega(e);
		}
		else{
			EstadoTablero oldEstado = _estadosNoExplotados.estadoYaContenido(e);
			
			if( !oldEstado.equals(e) ){
				throw new IllegalStateException("Tenia que haber uno igual");
			}
			
			// EL ESTADO ESTABA. SE CAMBIA SI SUS MOVIMIENTOS SON MENORES
			if( oldEstado.movimientos() >= e.movimientos() ){
				_estadosNoExplotados.extrae(oldEstado);
				_estadosNoExplotados.agrega(e);
				
				// HAY QUE CAMBIAR LOS HIJOS DE LOS ESTADOS QUE APUNTABAN AL ANTIGUO
				EstadoTablero[] padres = oldEstado.getPadres();
				
				for( int i = 0 ; i < padres.length ; i++ ){
					Direccion opuesto = Direccion.values()[i].opuesto();
					EstadoTablero padre = padres[i];
					if( padre!= null ){
						padre.getHijos()[ opuesto.ordinal() ] = e;
					}
				}
			}
		}
	}

	private void explotaEstado( EstadoTablero e ){
		_estadosNoExplotados.extrae(e);
		EstadoTablero[] hijos = e.creaHijos();
		for( int i = 0 ; i < hijos.length ; i++ ){
			EstadoTablero hijo = hijos[i];
			if( hijo != null ){
				agrega(hijo);
			}
		}
		_estadosExplotados.agrega(e);
	}
	
	
	
	private EstadoTablero extraeEstadoConMejorHeuristica(){
		EstadoTablero ret = estadoConMejorHeuristica();
		if( ret != null ){
			_estadosNoExplotados.extrae(ret);
		}
		return ret;
	}

	public EstadoTablero estadoConMejorHeuristica() {
		return _estadosNoExplotados.estadoConMenorHeuristica();
	}
	
	public boolean explotaEstadoConMejorHeuristica(){
		EstadoTablero e = extraeEstadoConMejorHeuristica();
		if( e == null ){
			_noHaySolucion = true;
		}
		else{
			explotaEstado(e);
		}
		return e != null;
	}
	
	public boolean haySolucion(){
		return _estadosFinales.size() > 0;
	}
	
	public boolean noHaySolucion(){
		return _noHaySolucion;
	}
	
	public EstadoTablero[] estadosFinales(){
		return _estadosFinales.toArray(EstadoTablero.ZEROARRAY);
	}

	public void dumpSolucion(PrintStream out) {
		EstadoTablero e = _estadosFinales.iterator().next();
		Stack<EstadoTablero> stack = new Stack<EstadoTablero>();
		while( e != null ){
			stack.push(e);
			e = e.getPadre();
		}
		while( !stack.empty() ){
			stack.pop().dump(out);
		}
	}

	public void dump(PrintStream out) {
		throw new NotImplementedException();
	}
	
	public void info( PrintStream out ){
		out.println( "Explotados: " + explotados() + "\tNo explotable:" + noExplotables() + "\tNo explotados: " + _estadosNoExplotados.size() );
	}

	public int explotados() {
		return _estadosExplotados.explotados();
	}
	
	public int noExplotables(){
		return _estadosExplotados.noExplotables();
	}
}
