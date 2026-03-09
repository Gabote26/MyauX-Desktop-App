package main;

import db.ConexionMysql;
import guiAdmin.MainForAdmin;
import guiEstudiante.ProgramMain;
import guiProfesor.MainForTeachers;
import utils.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.Properties;

public class LoginSystem extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel container;

    private MaterialTextField userField;
    private MaterialPasswordField passField;

    private final ConexionMysql connectionDB = new ConexionMysql();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {

                // Verificar la sesión actual
                Properties sesion = SessionManager.cargarSesion();

                // Si hay una sesión existente, abrir la ventana segun el rol
                if (sesion != null) {
                    abrirVentanaSegunRol(sesion);
                    return;
                }

                // Si no hay sesión exixtente, abrir ventana de inicio de sesión
                MaterialSplash splash = new MaterialSplash();
                splash.setVisible(true);
                Thread.sleep(1200);
                splash.dispose();

                LoginSystem frame = new LoginSystem();
                frame.setUndecorated(true);
                frame.setLocationRelativeTo(null);
                FadeUtils.fadeIn(frame, 300);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

	public LoginSystem() {
        setTitle("Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(480, 520);
        setLocationRelativeTo(null);

        try {
            setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
        } catch (Exception ex) {
        	JOptionPane.showMessageDialog(null, "ADVERTENCIA: La forma de la ventana no es compatible: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	        return;
        }
        
        container = new JPanel();
        container.setBackground(new Color(30, 30, 35));
        container.setLayout(null);
        container.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(container);
        
        // Comprobaciones de teclas
        container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "enterPresionado");
        container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "closeWindow");
        
        container.getActionMap().put("enterPresionado", new AbstractAction() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		System.out.println("Login ejecutandose...");
        		iniciarSesion();
        	}
        });
        
        container.getActionMap().put("closeWindow", new AbstractAction() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		int opcion = JOptionPane.showConfirmDialog(null, "¿Estás seguro de que quieres salir?", "Confirmación", JOptionPane.YES_NO_OPTION);
        		
        		if (opcion == JOptionPane.YES_OPTION) {
        			System.exit(0);
        		} else if (opcion == JOptionPane.NO_OPTION) {
        			System.out.println("Cierre cancelado");
        		}
        	}
        });

        // ------- Título -------
        JLabel title = new JLabel("INICIAR SESIÓN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setBounds(142, 140, 250, 35);
        container.add(title);

        JLabel subtitle = new JLabel("Ingresa tu usuario y contraseña");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(180, 180, 180));
        subtitle.setBounds(132, 175, 280, 25);
        container.add(subtitle);

        // ------- Íconos -------
        Icon userIcon = new ImageIcon(getClass().getResource("/icons/user.png"));
        Icon lockIcon = new ImageIcon(getClass().getResource("/icons/lock.png"));
        Icon eyeOn  = new ImageIcon(getClass().getResource("/icons/eye.png"));
        Icon eyeOff = new ImageIcon(getClass().getResource("/icons/eye_off.png"));

        // ------- Campos / inputs -------
        userField = new MaterialTextField("Correo", userIcon);
        userField.setBounds(101, 230, 280, 60);
        container.add(userField);

        passField = new MaterialPasswordField("Contraseña", lockIcon, eyeOn, eyeOff);
        passField.setBounds(101, 310, 280, 60);
        container.add(passField);

        // ------- Boton de inicio de sesion -------
        RoundedButton loginBtn = new RoundedButton("Ingresar", 22);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(new Color(25, 118, 210));
        loginBtn.setBounds(161, 395, 160, 45);
        container.add(loginBtn);

        loginBtn.addActionListener(e -> iniciarSesion());

        // ------- Boton para cerrar la ventana -------
        JButton closeBtn = new JButton("X");
        closeBtn.setFont(new Font("Tahoma", Font.BOLD, 10));
        closeBtn.setBounds(430, 8, 40, 30);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(new Color(153, 61, 61));
        closeBtn.setBorder(null);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> System.exit(0));
        container.add(closeBtn);
        
        addDragListener(container);
        
        JLabel lblNewLabel = new JLabel("Myaux v1.0.2B");
        lblNewLabel.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 12));
        lblNewLabel.setForeground(new Color(255, 255, 255));
        lblNewLabel.setBounds(377, 485, 93, 25);
        container.add(lblNewLabel);
        
        try {
		    // Carga la imagen desde el classpath
		    java.net.URL imageURL2 = getClass().getResource("/appLogoImg.png");

		    if (imageURL2 == null) {
		        throw new Exception("No se encontró el recurso appLogoImg.png.");
		    }

		    ImageIcon imgOriginal = new ImageIcon(imageURL2);
		    Image imgScaled = imgOriginal.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH);
		    JLabel lblAppLogo = new JLabel(new ImageIcon(imgScaled));

		    lblAppLogo.setBackground(new Color(0, 0, 160));
		    lblAppLogo.setBounds(64, 34, 81, 73);
		    container.add(lblAppLogo);

		} catch (Exception e) {
		    System.err.println("No se pudo cargar la imagen: " + e.getMessage());
		}
        
        // Nombre de la aplicación con degradado aplicado sobreescribiendo el componente paintComponent
        JLabel appTitle = new JLabel("MyauX") {
        	 @Override
        	    protected void paintComponent(Graphics g) {
        	        Graphics2D g2 = (Graphics2D) g.create();
        	        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        	                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        	        // Degradado del texto de izquierda a derecha
        	        GradientPaint gradient = new GradientPaint(
        	                0, 0, new Color(150, 50, 255), // color inicial
        	                getWidth(), 0, new Color(84, 7, 169)  // color final
        	        );

        	        g2.setPaint(gradient);
        	        g2.setFont(getFont());
        	        
        	        FontMetrics fm = g2.getFontMetrics();
        	        int x = 0;
        	        int y = fm.getAscent();

        	        g2.drawString(getText(), x, y);
        	        g2.dispose();
        	    }
        };
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 46));
        appTitle.setBounds(155, 36, 166, 71);
        container.add(appTitle);
        
        // Cambiar fondo del JOptionPane
        UIManager.put("OptionPane.background", new ColorUIResource(35, 35, 55));
        UIManager.put("Panel.background", new ColorUIResource(35, 35, 55));
        UIManager.put("OptionPane.messageForeground", Color.WHITE); // Color del texto del mensaje
        UIManager.put("OptionPane.foreground", Color.WHITE); // Color del texto general del OptionPane
        // Color del texto de los botones del JOptionPane
        UIManager.put("Button.background", new ColorUIResource(60, 60, 90));
        UIManager.put("Button.foreground", Color.BLACK);
    }

    // Permitir que la ventana pueda cambiarse de posición
    private void addDragListener(JPanel panel) {
        final int[] p = new int[2];

        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                p[0] = e.getX();
                p[1] = e.getY();
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                setLocation(getX() + e.getX() - p[0], getY() + e.getY() - p[1]);
            }
        });
    }

    // Iniciar sesion en el sistema
    private void iniciarSesion() {

        String user = userField.getText();
        String password = passField.getText();

        if (user.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Debe completar todos los campos");
            return;
        }

        MaterialLoader loader = new MaterialLoader(this);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            String nombre, apellido, role;
            long numControl;

            @Override
            protected Boolean doInBackground() throws Exception {
                Connection cn = connectionDB.conectar();
                if (cn == null) return false;

                String query = """
                    SELECT nombre, apellido, role, no_control
                    FROM usuarios
                    WHERE email = ? AND password = ?
                """;

                try (PreparedStatement ps = cn.prepareStatement(query)) {
                    ps.setString(1, user);
                    ps.setString(2, password);

                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        nombre = rs.getString("nombre");
                        apellido = rs.getString("apellido");
                        role = rs.getString("role");
                        numControl = rs.getLong("no_control");
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected void done() {
                loader.dispose();

                boolean ok = false;
                try { ok = get(); } catch (Exception ignored) {}

                if (!ok) {
                    JOptionPane.showMessageDialog(LoginSystem.this, "Usuario o contraseña incorrectos");
                    return;
                }

                // Guardar la sesión de forma local
                SessionManager.guardarSesion(user);

                FadeUtils.fadeOut(LoginSystem.this, 300, () -> {
                    dispose();
                    JFrame next = switch (role.toUpperCase()) {
                        case "ADMIN" -> new MainForAdmin(nombre);
                        case "PROFESOR" -> new MainForTeachers(nombre);
                        case "ESTUDIANTE" -> new ProgramMain(numControl, nombre, apellido);
                        default -> null;
                    };

                    if (next != null) {
                        next.setUndecorated(true);
                        next.setLocationRelativeTo(null);
                        FadeUtils.fadeIn(next, 300);
                    }
                });
            }
        };

        worker.execute();
        loader.setVisible(true);
    }
    
    // Abrir la ventana asignada dependiendo del rol del usuario
    private static void abrirVentanaSegunRol(Properties s) {
        String email = s.getProperty("email");

        ConexionMysql cn = new ConexionMysql();
        try (Connection con = cn.conectar()) {
            if (con == null) return;

            String query = """
                SELECT nombre, apellido, role, no_control
                FROM usuarios
                WHERE email = ?
            """;

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                // Si no existe el usuario -> cerrar sesión corrupta
                SessionManager.cerrarSesion();
                return;
            }

            String nombre = rs.getString("nombre");
            String apellido = rs.getString("apellido");
            String role = rs.getString("role");
            long numControl = rs.getLong("no_control");

            JFrame next = switch (role.toUpperCase()) {
                case "ADMIN" -> new MainForAdmin(nombre);
                case "PROFESOR" -> new MainForTeachers(nombre);
                case "ESTUDIANTE" -> new ProgramMain(numControl, nombre, apellido);
                default -> null;
            };

            if (next != null) {
                next.setUndecorated(true);
                next.setLocationRelativeTo(null);
                next.setVisible(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Método auxiliar para cerrar la sesion
    public static void cerrarSesion(JFrame ventanaActual) {
        SessionManager.cerrarSesion();
        
        FadeUtils.fadeOut(ventanaActual, 300, () -> {
            ventanaActual.dispose();

            LoginSystem login = new LoginSystem();
            login.setUndecorated(true);
            login.setLocationRelativeTo(null);
            FadeUtils.fadeIn(login, 300);
        });
    }

    
}
