package movebox;

public enum Direccion{
	
	ARRIBA, ABAJO, IZQUIERDA, DERECHA;
	
	public Direccion opuesto(){
		if( this == ARRIBA ) return ABAJO;
		if( this == ABAJO ) return ARRIBA;
		if( this == IZQUIERDA ) return DERECHA;
		if( this == DERECHA ) return IZQUIERDA;
		throw new IllegalStateException();
	}
}
