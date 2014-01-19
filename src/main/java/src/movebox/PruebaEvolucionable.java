package movebox;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class PruebaEvolucionable {

	public static void main(String[] args) throws IOException {
		String directorio = "soluciones-2010-08-29";
		compruebaDirectorioDeSoluciones(directorio);
	}

	private static void compruebaDirectorioDeSoluciones(String directorio)
			throws IOException {
		File d = new File(directorio);
		for( File f: d.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".txt");
			}
		}) ){ 
			compruebaFicheroDeSolucion(f.getPath());
		}
	}

	private static void compruebaFicheroDeSolucion(String f) throws IOException {
		ITablero[] tableros = Tableros.parseaTableros(f);
		
		for( ITablero t: tableros ){
			boolean e = CalculadorDeZonasEvolucionables.calculaEvolucionable(t);
			if( !e ){
				System.out.println( "Este tablero no es evolucionable:" + f );
				t.dump(System.out);
				System.exit(0);
			}
		}
	}
}
