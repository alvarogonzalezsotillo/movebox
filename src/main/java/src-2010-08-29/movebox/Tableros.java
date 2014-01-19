package movebox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

public class Tableros {
	public static final String[][] tableros = new String[26][];
	
	static{
		String[] tablero5 = {
			" #####  ",
			" #   #  ",
			" #.# ###",
			"## ++  #",
			"# . .* #",
			"#  +# ##",
			"### @ # ",
			"  ##### "
		};
		tableros[5] = tablero5;
		
		String[] tablero6 = {
			"  #####  ",
			" ##   ## ",
			" # .+. # ",
			"##.+.+.##",
			"# +.+.+ #",
			"#  +.+  #",
			"### @  ##",
			"  ###### "
		};
		tableros[6] = tablero6;
		
		String[] tablero15 = {
			"###################",
			"#   ...  @  ...   #",
			"# +++  #####  +++ #",
			"##   ###   ###   ##",
			" ##  #       ##  # ",
			"  ####        #### "
		};
		tableros[15] = tablero15;
		
		String[] tablero20 = {
			"######    ",
			"#   @#####",
			"# +**#   #",
			"## ..  * #",
			" # +  #+##",
			" #+ .##.# ",
			" #   #### ",
			" #####    "
		};
		tableros[20] = tablero20;

		String[] tablero24 = {
				"######",
				"#    #",
				"# *+ ###",
				"##+... ##",
				" #  +#. #",
				" ##  * @#",
				"  ## +* #",
				"   ##   #",
				"    #####"
			};
		tableros[24] = tablero24;

		
		String[] tablero25 ={
			"    ####    ",
			" ####  ###  ",
			" #  +  + #  ",
			" #+..+ . ###",
			"## #.##..  #",
			"#  ..+@+ + #",
			"# + . #. ###",
			"####+  + #  ",
			"   #  ####  ",
			"   ####     "
		};
		tableros[25] = tablero25;

	}
	
	
	public static final String[][] tablerosDePrueba = {
		{//0
			"########",
			"#    +.#",  
			"# @* +.#",
			"########"
		},
	
		{//1
			"########",
			"#    +.#",  
			"#    +.#",  
			"#**    #",  
			"# @*   #",
			"########"
		},
	
		{//2
			"#########",
			"#       #",  
			"#       #",  
			"#  ##+  #",  
			"#*##.   #",  
			"# @#    #",
			"#########"
		},
	
		{//3
			"#########",
			"#    ...#",  
			"#       #",  
			"#   +   #",  
			"#  ##+  #",  
			"# ## +  #",  
			"# @#    #",
			"#########"
		},
	
		{//4
			"########",
			"####   #",
			"##  +  #",
			"#     ##",
			"# #*####",
			"#.+   ##",
			"##@.  ##",
			"########"
		},
		
		{//5
			"  ######## ",
			"###      # ",
			"#   +@+  # ",
			"#  + + + # ",
			"#####+#####",
			"   ## . ..#",
			"   #  .#  #",
			"   # .   .#",
			"   ####  ##",
			"      #### ",
		},
		
		{//6
		  "     ####",
		  "  ####  ###",
		  "  #  +    #",
		  "  # ..+ . ###",
		  " ## #*##*.  #",
		  " #  ** +    #",
		  " #@  * #. ###",
		  " ####++   #",
		  "    #  ####",
		  "    ####",
		},

		{//7
	   "    ####",
	   " ####  ###",
	   " #  +  + #",
	   " #+.*+ *@###",
	   "## #.##*.  #",
	   "#  .* +    #",
	   "#   . #* ###",
	   "####     #",
	   "   #  ####",
	   "   ####",
		}
	};
	
	
	public static ITablero[] parseaTableros( String file ) throws IOException{
		FileInputStream is = new FileInputStream( new File(file) );
		InputStreamReader in = new InputStreamReader(is);
		return parseaTableros(in);
	}
	
	public static ITablero[] parseaTableros( Reader in ) throws IOException{
		BufferedReader bin = new BufferedReader(in);
		
		ArrayList<ITablero> tableros = new ArrayList<ITablero>();
		ArrayList<String> lineas = new ArrayList<String>();
		String line = bin.readLine();
		while( line != null ){
			if( puedeSerDeUnTablero(line) ){
				line = line.substring(1);
				lineas.add(line);
			}
			else{
				if( lineas.size() > 0 ){
					Tablero t = new Tablero();
					t.parsea( lineas );
					tableros.add(t);
				}
				lineas.clear();
			}
			line = bin.readLine();
		}

		if( lineas.size() > 0 ){
			Tablero t = new Tablero();
			t.parsea( lineas );
			tableros.add(t);
		}
		
		return (ITablero[]) tableros.toArray(new ITablero[tableros.size()]);
	}

	private static boolean puedeSerDeUnTablero(String line){
		if( line.length() < 4 ){
			return false;
		}
		line = line.substring(1);
		for( int i = 0 ; i < line.length() ; i++ ){
			if( !Objeto.fromChar(line.charAt(i)).predefinido() ){
				return false;
			}
		}
		return true;
	}
	
	public static ITablero[] tableros() throws IOException{
		 ITablero[] tableros = parseaTableros("Movebox.txt");
		 return tableros;
	}
	
	public static void main(String[] args) throws IOException {
		/*
		 ITablero[] tableros = parseaTableros("Movebox.txt");
		*/
		ITablero[] tableros = new ITablero[Tableros.tableros.length];
		for( int i = 0 ; i < tableros.length ; i++ ){
			if( Tableros.tableros[i] != null ){
				Tablero t = new Tablero();
				t.parsea(Tableros.tableros[i]);
				tableros[i] = t;
			}
		}
		
		
		for( ITablero t: tableros ){
			if( t != null ){
				t.dump(System.out);
			}
		}
		
	}
}
