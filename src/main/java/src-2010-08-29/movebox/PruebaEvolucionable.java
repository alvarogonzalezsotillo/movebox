package movebox;

import java.io.IOException;

public class PruebaEvolucionable {

	public static void main(String[] args) throws IOException {
		ITablero[] tableros = Tableros.parseaTableros("PruebaEvolucionable.txt");
		
		for( ITablero t: tableros ){
			boolean e = CalculadorDeZonasEvolucionables.calculaEvolucionable(t);
			if( !e ){
				System.out.println( "Este tablero no es evolucionable" );
				t.dump(System.out);
				System.exit(0);
			}
		}
	}
}
