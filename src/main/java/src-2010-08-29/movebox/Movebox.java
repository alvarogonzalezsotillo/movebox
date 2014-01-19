package movebox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class Movebox {
	
	public static final PrintStream NULLDEBUG = new PrintStream( new OutputStream(){
		@Override
		public void write(int b) throws IOException {
		}
	});
	
	public static final PrintStream DEBUG = NULLDEBUG;//System.err;
	private static final int STEP = 1000;

	private static final boolean ESTADOSEVOLUCIONABLES = true;

	public static void main(String[] args) throws IOException {
		
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		
		ITablero[] tableros = Tableros.tableros();
		for (int i = 0; i < tableros.length; i++) {
			if( i != 13 ){
				continue;
			}
			ITablero tablero = tableros[i];
			if( tablero == null ){
				continue;
			}
			PrintStream out = new PrintStream( new File( "tablero"+(i+1)+".txt") );
			resuelveTablero(tablero,out);
			out.close();
		}
	}

	private static void resuelveTablero(String tablero[], PrintStream out) {
		Tablero t = new Tablero();
		t.parsea( tablero );
		resuelveTablero(t, out);
	}
		
	private static void resuelveTablero( ITablero t, PrintStream out ){
		ColocadorDeBloques estados = new ColocadorDeBloques(ESTADOSEVOLUCIONABLES);
		estados.inicializa(t);
		System.out.println( "RESOLVIENDO");
		t.dump(out);
		
		long millis = System.currentTimeMillis();
		bucle(estados,System.out);
		millis = System.currentTimeMillis()-millis;
		long segundos = millis/1000;
		long horas = segundos/3600;
		long minutos = (segundos/60)%60;
		segundos = segundos%60;
		
		if( estados.haySolucion() ){
			System.out.println( "SOLUCION" );
			out.printf( "SOLUCION: %02d:%02d:%02d (%d ms)", horas, minutos, segundos ,millis );
			estados.info(out);
			estados.dumpSolucion(out);
		}
		
		if( estados.noHaySolucion() ){
			System.out.println( "SIN SOLUCION" );
			out.println( "SIN SOLUCION" );
			estados.dump(out);
		}
	}

	private static void bucle(ColocadorDeBloques estados, PrintStream out) {
		while( !estados.haySolucion() && !estados.noHaySolucion() ){
			estados.explotaEstadoConMejorHeuristica();
			if( estados.explotados() % STEP == 0 ){
				out.println();
				estados.info(out);
				EstadoTablero estadoConMejorHeuristica = estados.estadoConMejorHeuristica();
				if( estadoConMejorHeuristica != null ){
					estadoConMejorHeuristica.dump(out);
				}
			}
			if( estados.explotados() % (STEP/10) == 0 ){
				out.print(".");
			}
		}
	}
}
