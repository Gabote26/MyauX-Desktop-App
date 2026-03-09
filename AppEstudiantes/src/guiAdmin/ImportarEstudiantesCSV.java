package guiAdmin;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import db.ConexionMysql;
import utils.Recargable;

public class ImportarEstudiantesCSV extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTable tablePreview;
	private DefaultTableModel model;
	private JButton btnSeleccionar, btnImportar, btnCancelar;
	private JLabel lblArchivo, lblEstado;
	private JProgressBar progressBar;
	private final ConexionMysql connectionDB = new ConexionMysql();
	private final Recargable parentFrame;
	private File archivoSeleccionado;
	private List<String[]> datosValidos = new ArrayList<>();

	public ImportarEstudiantesCSV(Recargable parentFrame) {
		this.parentFrame = parentFrame;

		setTitle("📥 Importar Estudiantes desde CSV");
		setSize(950, 650);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);
		getContentPane().setBackground(Color.WHITE);

		// ========== HEADER ==========
		JPanel headerPanel = new JPanel(null);
		headerPanel.setBounds(0, 0, 950, 80);
		headerPanel.setBackground(new Color(52, 73, 94));
		getContentPane().add(headerPanel);

		JLabel lblTitulo = new JLabel("📥 Importar Estudiantes Masivamente");
		lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setBounds(30, 20, 500, 40);
		headerPanel.add(lblTitulo);

		// ========== INSTRUCCIONES ==========
		JTextArea txtInstrucciones = new JTextArea(
				"📝 Formato CSV requerido (con encabezado):\n" + "nombre,apellido,email,password,no_control,grupo\n\n"
						+ "✅ Ejemplo válido:\n" + "Juan,Pérez,juan@email.com,password123,20241001000001,3AM-ADMRH\n"
						+ "María,López,maria@email.com,clave456,20241002000002,5BV-MEC\n\n"
						+ "⚠️ No. Control: Exactamente 14 dígitos numéricos\n"
						+ "⚠️ Grupo: SEMESTRE+TURNO-ESPECIALIDAD (ej: 3AM-ADMRH, 5BV-MEC)");
		txtInstrucciones.setEditable(false);
		txtInstrucciones.setFont(new Font("Consolas", Font.PLAIN, 11));
		txtInstrucciones.setBackground(new Color(255, 252, 240));
		txtInstrucciones.setForeground(new Color(60, 60, 60));
		txtInstrucciones.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(255, 193, 7), 2), new EmptyBorder(10, 10, 10, 10)));
		JScrollPane scrollInst = new JScrollPane(txtInstrucciones);
		scrollInst.setBounds(20, 100, 910, 110);
		getContentPane().add(scrollInst);

		// ========== SELECTOR DE ARCHIVO ==========
		JPanel panelArchivo = new JPanel(null);
		panelArchivo.setBounds(20, 230, 910, 60);
		panelArchivo.setBackground(Color.WHITE);
		panelArchivo.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 200, 200)), new EmptyBorder(5, 10, 5, 10)));
		getContentPane().add(panelArchivo);

		JLabel lblTituloArchivo = new JLabel("📁 Archivo CSV:");
		lblTituloArchivo.setFont(new Font("Segoe UI", Font.BOLD, 13));
		lblTituloArchivo.setBounds(10, 10, 100, 25);
		panelArchivo.add(lblTituloArchivo);

		lblArchivo = new JLabel("Ningún archivo seleccionado");
		lblArchivo.setBounds(10, 30, 600, 25);
		lblArchivo.setForeground(Color.GRAY);
		lblArchivo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
		panelArchivo.add(lblArchivo);

		btnSeleccionar = new JButton("📂 Seleccionar CSV");
		btnSeleccionar.setBounds(700, 15, 180, 35);
		btnSeleccionar.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
		btnSeleccionar.setBackground(new Color(52, 152, 219));
		btnSeleccionar.setForeground(Color.WHITE);
		btnSeleccionar.setFocusPainted(false);
		btnSeleccionar.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnSeleccionar.addActionListener(e -> seleccionarArchivo());
		panelArchivo.add(btnSeleccionar);

		// ========== VISTA PREVIA ==========
		JLabel lblPreview = new JLabel("👁️ Vista Previa de Registros:");
		lblPreview.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
		lblPreview.setBounds(20, 300, 300, 25);
		getContentPane().add(lblPreview);

		model = new DefaultTableModel(new Object[] { "Nombre", "Apellido", "Email", "No. Control", "Grupo", "Estado" },
				0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		tablePreview = new JTable(model);
		tablePreview.setRowHeight(32);
		tablePreview.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		tablePreview.setGridColor(new Color(230, 230, 230));
		tablePreview.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
		tablePreview.getTableHeader().setBackground(new Color(245, 245, 250));

		// Renderer personalizado para la columna Estado
		tablePreview.getColumnModel().getColumn(5).setCellRenderer(new EstadoRenderer());

		JScrollPane scrollTable = new JScrollPane(tablePreview);
		scrollTable.setBounds(20, 330, 910, 200);
		scrollTable.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
		getContentPane().add(scrollTable);

		// ========== BARRA DE PROGRESO ==========
		progressBar = new JProgressBar();
		progressBar.setBounds(20, 540, 910, 25);
		progressBar.setStringPainted(true);
		progressBar.setVisible(false);
		getContentPane().add(progressBar);

		// ========== ESTADO ==========
		lblEstado = new JLabel("ℹ️ Selecciona un archivo CSV para comenzar");
		lblEstado.setBounds(20, 570, 700, 25);
		lblEstado.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
		lblEstado.setForeground(new Color(100, 100, 100));
		getContentPane().add(lblEstado);

		// ========== BOTONES DE ACCIÓN ==========
		btnImportar = new JButton("💾 Importar Estudiantes");
		btnImportar.setBounds(590, 570, 180, 35);
		btnImportar.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
		btnImportar.setBackground(new Color(46, 204, 113));
		btnImportar.setForeground(Color.WHITE);
		btnImportar.setFocusPainted(false);
		btnImportar.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnImportar.setEnabled(false);
		btnImportar.addActionListener(e -> importarEstudiantes());
		getContentPane().add(btnImportar);

		btnCancelar = new JButton("❌ Cerrar");
		btnCancelar.setBounds(790, 570, 120, 35);
		btnCancelar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
		btnCancelar.setFocusPainted(false);
		btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnCancelar.addActionListener(e -> dispose());
		getContentPane().add(btnCancelar);
	}

	// ========== SELECTOR DE ARCHIVO ==========
	private void seleccionarArchivo() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Seleccionar archivo CSV");
		fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos CSV (*.csv)", "csv"));

		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			archivoSeleccionado = fileChooser.getSelectedFile();
			lblArchivo.setText(
					"📄 " + archivoSeleccionado.getName() + " (" + formatearTamano(archivoSeleccionado.length()) + ")");
			lblArchivo.setForeground(new Color(52, 152, 219));
			lblArchivo.setFont(new Font("Segoe UI", Font.BOLD, 12));
			procesarCSV();
		}
	}

	// ========== PROCESAR CSV CON UTF-8 ==========
	private void procesarCSV() {
		model.setRowCount(0);
		datosValidos.clear();
		int errores = 0;
		int lineaActual = 0;

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(archivoSeleccionado), StandardCharsets.UTF_8))) {
			String linea;
			boolean primeraLinea = true;

			while ((linea = br.readLine()) != null) {
				lineaActual++;

				// Saltar encabezado
				if (primeraLinea) {
					primeraLinea = false;
					continue;
				}

				// Saltar líneas vacías
				if (linea.trim().isEmpty()) {
					continue;
				}

				String[] datos = linea.split(",");

				// Validar número de columnas
				if (datos.length != 6) {
					errores++;
					model.addRow(new Object[] { datos.length > 0 ? datos[0].trim() : "?",
							datos.length > 1 ? datos[1].trim() : "?", datos.length > 2 ? datos[2].trim() : "?",
							datos.length > 4 ? datos[4].trim() : "?", datos.length > 5 ? datos[5].trim() : "?",
							"❌ Formato incorrecto (línea " + lineaActual + ")" });
					continue;
				}

				// Limpiar espacios en blanco
				for (int i = 0; i < datos.length; i++) {
					datos[i] = datos[i].trim();
				}

				// Validar datos
				String estado = validarDatos(datos, lineaActual);

				if (estado.startsWith("✅")) {
					datosValidos.add(datos);
				} else {
					errores++;
				}

				model.addRow(new Object[] { datos[0], datos[1], datos[2], datos[4], datos[5], estado });
			}

			// Actualizar estado final
			actualizarEstado(datosValidos.size(), errores);

		} catch (IOException e) {
			JOptionPane.showMessageDialog(this,
					"❌ Error al leer el archivo:\n" + e.getMessage()
							+ "\n\nVerifica que el archivo no esté abierto en otra aplicación.",
					"Error de Lectura", JOptionPane.ERROR_MESSAGE);
		}
	}

	// ========== VALIDACIONES EXHAUSTIVAS ==========
	private String validarDatos(String[] datos, int linea) {
		// 1. Validar campos vacíos
		String[] nombresCampos = { "Nombre", "Apellido", "Email", "Contraseña", "No. Control", "Grupo" };
		for (int i = 0; i < datos.length; i++) {
			if (datos[i].isEmpty()) {
				return "❌ " + nombresCampos[i] + " vacío (línea " + linea + ")";
			}
		}

		// 2. Validar email
		if (!datos[2].matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
			return "❌ Email inválido (línea " + linea + ")";
		}

		// 3. Validar contraseña (mínimo 8 caracteres)
		if (datos[3].length() < 8) {
			return "❌ Contraseña < 8 caracteres (línea " + linea + ")";
		}

		// 4. Validar no_control (solo números, 8-10 dígitos)
		if (!datos[4].matches("^\\d{14}$")) {
			return "❌ No. Control debe ser de 14 digitos (línea " + linea + ")";
		}

		// 5. Validar formato de grupo (ej: 3AM-ADMRH, 5BV-MEC)
		if (!datos[5].matches("^[1-6](A|B|C)(M|V)-[A-Z]{3,6}$")) {
			return "❌ Formato de grupo incorrecto (línea " + linea + ")";
		}

		// 6. Validar que el grupo existe en BD
		if (obtenerIdGrupo(datos[5]) == -1) {
			return "❌ Grupo no existe en BD (línea " + linea + ")";
		}

		// 7. Validar que email no esté duplicado en el CSV actual
		long countEmail = datosValidos.stream().filter(d -> d[2].equalsIgnoreCase(datos[2])).count();
		if (countEmail > 0) {
			return "❌ Email duplicado en CSV (línea " + linea + ")";
		}

		// 8. Validar que no_control no esté duplicado en el CSV actual
		long countControl = datosValidos.stream().filter(d -> d[4].equals(datos[4])).count();
		if (countControl > 0) {
			return "❌ No. Control duplicado en CSV (línea " + linea + ")";
		}

		// 9. Validar que email no exista ya en BD
		if (existeEmailEnBD(datos[2])) {
			return "❌ Email ya existe en BD (línea " + linea + ")";
		}

		// 10. Validar que no_control no exista ya en BD
		if (existeControlEnBD(datos[4])) {
			return "❌ No. Control ya existe en BD (línea " + linea + ")";
		}

		return "✅ Válido";
	}

	// ========== CONSULTAS A BD ==========
	private int obtenerIdGrupo(String nombreGrupo) {
		String sql = "SELECT id FROM grupos WHERE nombre_grupo = ?";
		try (Connection cn = connectionDB.conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {
			ps.setString(1, nombreGrupo);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return rs.getInt("id");
		} catch (SQLException e) {
			System.err.println("Error al obtener grupo: " + e.getMessage());
		}
		return -1;
	}

	private boolean existeEmailEnBD(String email) {
		String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
		try (Connection cn = connectionDB.conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return rs.getInt(1) > 0;
		} catch (SQLException e) {
			System.err.println("Error al verificar email: " + e.getMessage());
		}
		return false;
	}

	private boolean existeControlEnBD(String noControl) {
		String sql = "SELECT COUNT(*) FROM usuarios WHERE no_control = ?";
		try (Connection cn = connectionDB.conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {
			ps.setString(1, noControl);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return rs.getInt(1) > 0;
		} catch (SQLException e) {
			System.err.println("Error al verificar no_control: " + e.getMessage());
		}
		return false;
	}

	// ========== IMPORTAR CON TRANSACCIÓN ==========
	private void importarEstudiantes() {
		if (datosValidos.isEmpty()) {
			JOptionPane.showMessageDialog(this, "⚠️ No hay datos válidos para importar", "Advertencia",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this,
				String.format("¿Confirmas la importación de %d estudiante(s)?\n\n"
						+ "Esta acción agregará nuevos registros a la base de datos.", datosValidos.size()),
				"Confirmar Importación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (confirm != JOptionPane.YES_OPTION)
			return;

		// Mostrar barra de progreso
		progressBar.setVisible(true);
		progressBar.setMaximum(datosValidos.size());
		progressBar.setValue(0);
		btnImportar.setEnabled(false);
		btnSeleccionar.setEnabled(false);

		SwingWorker<Void, Integer> worker = new SwingWorker<>() {
			int insertados = 0;
			List<String> erroresImportacion = new ArrayList<>();

			@Override
			protected Void doInBackground() {
				String sql = """
						INSERT INTO usuarios (nombre, apellido, email, password, role, no_control, grupo_id)
						VALUES (?, ?, ?, ?, 'ESTUDIANTE', ?, ?)
						""";

				Connection cn = null;
				try {
					cn = connectionDB.conectar();
					cn.setAutoCommit(false); // Iniciar transacción

					try (PreparedStatement ps = cn.prepareStatement(sql)) {
						for (int i = 0; i < datosValidos.size(); i++) {
							String[] datos = datosValidos.get(i);
							try {
								ps.setString(1, datos[0]); // nombre
								ps.setString(2, datos[1]); // apellido
								ps.setString(3, datos[2]); // email
								ps.setString(4, datos[3]); // password
								ps.setString(5, datos[4]); // no_control
								ps.setInt(6, obtenerIdGrupo(datos[5])); // grupo_id
								ps.addBatch();
								insertados++;
								publish(i + 1); // Actualizar progreso
							} catch (Exception e) {
								erroresImportacion.add(datos[2] + ": " + e.getMessage());
							}
						}
						ps.executeBatch();
					}

					cn.commit(); // Confirmar transacción

				} catch (SQLException e) {
					if (cn != null) {
						try {
							cn.rollback(); // Revertir cambios
							erroresImportacion.add("ROLLBACK: " + e.getMessage());
						} catch (SQLException ex) {
							System.err.println("Error en rollback: " + ex.getMessage());
						}
					}
				} finally {
					if (cn != null) {
						try {
							cn.setAutoCommit(true);
							cn.close();
						} catch (SQLException e) {
							System.err.println("Error al cerrar conexión: " + e.getMessage());
						}
					}
				}
				return null;
			}

			@Override
			protected void process(List<Integer> chunks) {
				progressBar.setValue(chunks.get(chunks.size() - 1));
			}

			@Override
			protected void done() {
				progressBar.setVisible(false);
				btnImportar.setEnabled(true);
				btnSeleccionar.setEnabled(true);

				StringBuilder mensaje = new StringBuilder();
				mensaje.append(
						String.format(
								"✅ Importación completada:\n\n" + "• %d estudiantes agregados exitosamente\n"
										+ "• %d errores durante la importación",
								insertados, erroresImportacion.size()));

				if (!erroresImportacion.isEmpty()) {
					mensaje.append("\n\n⚠️ Errores detectados:\n");
					int maxErrores = Math.min(5, erroresImportacion.size());
					for (int i = 0; i < maxErrores; i++) {
						mensaje.append("• ").append(erroresImportacion.get(i)).append("\n");
					}
					if (erroresImportacion.size() > 5) {
						mensaje.append("... y ").append(erroresImportacion.size() - 5).append(" más");
					}
				}

				JOptionPane.showMessageDialog(ImportarEstudiantesCSV.this, mensaje.toString(),
						"Resultado de Importación",
						erroresImportacion.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

				// Refrescar tabla principal
				if (parentFrame != null && insertados > 0) {
					parentFrame.cargarEstudiantes();
				}

				if (insertados > 0) {
					dispose();
				}
			}
		};

		worker.execute();
	}

	// ========== MÉTODOS AUXILIARES ==========
	private void actualizarEstado(int validos, int errores) {
		if (validos == 0 && errores == 0) {
			lblEstado.setText("ℹ️ El archivo está vacío o no contiene datos válidos");
			lblEstado.setForeground(Color.GRAY);
			btnImportar.setEnabled(false);
		} else if (validos == 0) {
			lblEstado.setText(String.format("❌ 0 registros válidos | %d errores encontrados", errores));
			lblEstado.setForeground(new Color(231, 76, 60));
			btnImportar.setEnabled(false);
		} else if (errores == 0) {
			lblEstado.setText(String.format("✅ %d registros válidos | Sin errores", validos));
			lblEstado.setForeground(new Color(46, 204, 113));
			btnImportar.setEnabled(true);
		} else {
			lblEstado.setText(String.format("⚠️ %d registros válidos | %d errores", validos, errores));
			lblEstado.setForeground(new Color(255, 140, 0));
			btnImportar.setEnabled(true);
		}
	}

	private String formatearTamano(long bytes) {
		if (bytes < 1024)
			return bytes + " B";
		if (bytes < 1024 * 1024)
			return String.format("%.1f KB", bytes / 1024.0);
		return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
	}

	// ========== RENDERER PARA COLUMNA ESTADO ==========
	class EstadoRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			String estado = value.toString();
			setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
			setHorizontalAlignment(SwingConstants.CENTER);

			if (estado.startsWith("✅")) {
				setBackground(new Color(232, 245, 233));
				setForeground(new Color(27, 94, 32));
			} else if (estado.startsWith("❌")) {
				setBackground(new Color(255, 235, 238));
				setForeground(new Color(183, 28, 28));
			}

			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			}

			return this;
		}
	}
}