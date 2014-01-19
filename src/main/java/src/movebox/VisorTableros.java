package movebox;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class VisorTableros extends JFrame{

	
	private static final String DIRECTORY = "soluciones-2010-08-29";
	private JPanel _avanceRetrocesoPanel;
	private JButton _adelanteButton;
	private JButton _atrasButton;
	private JTextArea _visor;

	private ITablero[] _tableros;
	private int _indiceTablero;
	private JList _listaFicheros;
	private JList _listaPasos;
	
	public VisorTableros() {
		init();
	}
	
	private void init(){
		add( avanceRetrocesoPanel(), BorderLayout.SOUTH );
		add( visor(), BorderLayout.CENTER );
		add( new JScrollPane(listaFicheros()), BorderLayout.EAST );
		add( new JScrollPane(listaPasos()), BorderLayout.WEST );
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	private ITablero[] cargaTableros(String file){
		try {
			return Tableros.parseaTableros(file);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void adelante() {
		_indiceTablero = Math.min( _tableros.length-1, _indiceTablero+1 );
		actualizaVisor();
	}
	private void atras() {
		_indiceTablero = Math.max( 0, _indiceTablero -1 );
		actualizaVisor();
	}

	private ITablero tablero(){
		return _tableros[_indiceTablero];
	}
	
	private void actualizaVisor() {
		JTextArea visor = visor();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		ps.println( "PASO " + _indiceTablero );
		tablero().dump(ps);
		ps.close();
		visor.setText(baos.toString());
	}

	private JPanel avanceRetrocesoPanel(){
		if( _avanceRetrocesoPanel != null ){
			return _avanceRetrocesoPanel;
		}
		_avanceRetrocesoPanel = new JPanel();
		_adelanteButton = new JButton( ">>" );
		_atrasButton = new JButton( "<<");
		
		_adelanteButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				adelante();
			}
		});

		_atrasButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				atras();
			}
		});
		
		_avanceRetrocesoPanel.add( _atrasButton, BorderLayout.WEST );
		_avanceRetrocesoPanel.add( _adelanteButton, BorderLayout.EAST );
		return _avanceRetrocesoPanel;
	}
	
	private JTextArea visor(){
		if( _visor != null ){
			return _visor;
		}
		_visor = new JTextArea();
		_visor.setFont( Font.decode( "courier-bold-24" ) );
		return _visor;
	}

	private JList listaFicheros(){
		if( _listaFicheros != null ){
			return _listaFicheros;
		}
		
		_listaFicheros = new JList();
		File directory = new File(DIRECTORY);
		final DefaultListModel model = new DefaultListModel();
		for( File f: directory.listFiles() ){
			model.addElement(f);
		}
		_listaFicheros.setModel(model);
		
		_listaFicheros.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if( e.getValueIsAdjusting() ){
					return;
				}
				int index = _listaFicheros.getSelectedIndex();
				File f = (File) model.get(index);
				cargaFichero(f);
			}

		});
		
		return _listaFicheros;
	}

	private void cargaFichero(File f) {
		_tableros = cargaTableros(f.getAbsolutePath());
		
		DefaultListModel model = (DefaultListModel) listaPasos().getModel();
		model.clear();
		for( int i = 0 ; i < _tableros.length ; i++ ){
			model.addElement(i);
		}
		
		_indiceTablero = 0;
		actualizaVisor();
	}
	
	
	private JList listaPasos(){
		if( _listaPasos != null ){
			return _listaPasos;
		}
		
		_listaPasos = new JList();
		_listaPasos.setModel( new DefaultListModel() );
		_listaPasos.addListSelectionListener( new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if( e.getValueIsAdjusting() ){
					return;
				}
				int index = _listaPasos.getSelectedIndex();
				_indiceTablero = index;
				actualizaVisor();
			}
		});
		
		return _listaPasos;
	}
	
	public static void main(String[] args) {
		VisorTableros vt = new VisorTableros();
		vt.pack();
		vt.setVisible(true);
	}
}
