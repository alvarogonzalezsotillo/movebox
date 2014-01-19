package movebox;

import java.io.PrintStream;

public interface ITablero {
	Objeto getObjeto(Punto p);
	Objeto getObjeto(int x, int y);
	int ancho();
	int alto();
	void dump(PrintStream out);
	int id();
	Punto personaje();
}
