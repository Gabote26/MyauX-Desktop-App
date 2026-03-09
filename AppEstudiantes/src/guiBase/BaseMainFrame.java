package guiBase;

import db.ConexionMysql;
import main.*;
import utils.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public abstract class BaseMainFrame extends JFrame implements Recargable {

    private static final long serialVersionUID = 1L;

    protected final ConexionMysql connectionDB = new ConexionMysql();
    
    protected JTable tableEstudiantes;
    protected DefaultTableModel model;
    protected JComboBox<String> cbGrupos;
    protected int grupoSeleccionadoId = -1;
    
    protected RoundedButton btnGestionar, btnRefrescar, btnAgregar, btnEliminar, btnEditar, btnSendMsg;
    protected JPanel actionPanel;
    
    protected boolean darkMode = false;
    private RoundedButton btnSettings;
    protected RoundedButton btnCalificaciones;
    protected RoundedButton btnImportarCSV;
    
    // Componentes para el tema
    private JPanel headerPanel;
    private JLabel lblTitulo;
    private JPanel headerRight;
    private JPanel searchPanel;
    private JLabel lblBuscar;
    private JComboBox<String> cbFiltro;
    private JTextField txtBuscar;
    private JScrollPane scrollPane;
    private JPanel grupoPanel;
    private JLabel lblGrupo;
    
    // Parámetros para la animación de ventana
    private boolean maximizado = false;
    private Rectangle prevBounds;
    private Timer animTimer;
    
    // Variables para el redimensionamiento
    private static final int RESIZE_MARGIN = 5;
    private int resizeDirection = 0;
    private Point initialClick;
    
    // Constantes para direcciones de redimensionamiento
    private static final int RESIZE_NONE = 0;
    private static final int RESIZE_N = 1;
    private static final int RESIZE_S = 2;
    private static final int RESIZE_W = 4;
    private static final int RESIZE_E = 8;
    private static final int RESIZE_NW = RESIZE_N | RESIZE_W;
    private static final int RESIZE_NE = RESIZE_N | RESIZE_E;
    private static final int RESIZE_SW = RESIZE_S | RESIZE_W;
    private static final int RESIZE_SE = RESIZE_S | RESIZE_E;
    
    // Constantes para dimensiones y márgenes
    private static final int HEADER_HEIGHT = 72;
    private static final int SEARCH_PANEL_HEIGHT = 60;
    private static final int GRUPO_PANEL_HEIGHT = 50;
    private static final int ACTION_PANEL_HEIGHT = 50;
    private static final int HEADER_RIGHT_WIDTH = 180;
    private static final int MARGIN = 20;

    public BaseMainFrame(String tituloVentana, String tituloHeader) {
        setTitle(tituloVentana);
        setSize(1350, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        
        setLocationRelativeTo(null);
        getContentPane().setLayout(null);
        getContentPane().setBackground(new Color(250, 250, 252));
        
        // Se registra la ventana en el ThemeManager
        ThemeManager.registerFrame(this);
        
        // Cargar el tema guardado
        darkMode = ConfigManager.isDarkMode();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ThemeManager.unregisterFrame(BaseMainFrame.this);
            }
        });
        
        // ------ Header --------
        headerPanel = new JPanel(null);
        headerPanel.setBackground(new Color(80, 90, 140));
        
        lblTitulo = new JLabel(tituloHeader);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(18, 20, 400, 30);
        headerPanel.add(lblTitulo);
        
        // Botones de ventana
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        
        JButton btnMin = createTopButton("—", e -> setState(Frame.ICONIFIED));
        JButton btnMax = createTopButton("▢", e -> toggleMaximize());
        JButton btnClose = createTopButton("X", e -> System.exit(0));
        btnClose.setBackground(new Color(153, 61, 61));
        
        btnPanel.add(btnMin);
        btnPanel.add(btnMax);
        btnPanel.add(btnClose);
        
        headerPanel.add(btnPanel);
        getContentPane().add(headerPanel);
        
        // ---------- Panel derecho
        headerRight = new JPanel(null);
        headerRight.setBackground(Color.WHITE);
        
        btnSettings = new RoundedButton("⚙️ Configuración", 18);
        btnSettings.setBackground(new Color(247, 248, 250));
        btnSettings.setForeground(new Color(45, 45, 45));
        btnSettings.setToolTipText("Abrir configuración");
        btnSettings.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        btnSettings.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSettings.setFocusPainted(false);
        btnSettings.setBorderPainted(false);
        btnSettings.setOpaque(true);
        btnSettings.setBounds(15, 30, 150, 38);
        btnSettings.addActionListener(e -> new Settings());
        
        // Cerrar Sesión
        RoundedButton logOutBtn = new RoundedButton("🔒 Cerrar Sesion", 20);
        logOutBtn.setBackground(new Color(247, 79, 79));
        logOutBtn.setForeground(Color.WHITE);
        logOutBtn.setBounds(25, 400, 132, 39);
        logOutBtn.addActionListener(e -> LoginSystem.cerrarSesion(this));
        
        headerRight.add(btnSettings);
        headerRight.add(logOutBtn);
        getContentPane().add(headerRight);
        
        //Panel de busqueda de usuarios
        searchPanel = new JPanel(null);
        searchPanel.setBackground(Color.WHITE);
        
        lblBuscar = new JLabel("Buscar por:");
        lblBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblBuscar.setForeground(new Color(45, 45, 45));
        lblBuscar.setBounds(20, 15, 90, 30);
        searchPanel.add(lblBuscar);
        
        String[] criterios = {"Todos", "Nombre", "Apellido", "Email", "No-Control"};
        cbFiltro = new JComboBox<>(criterios);
        cbFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbFiltro.setBackground(Color.WHITE);
        cbFiltro.setForeground(new Color(45, 45, 45));
        cbFiltro.setBounds(110, 15, 140, 30);
        searchPanel.add(cbFiltro);
        
        txtBuscar = new JTextField();
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscar.setBackground(Color.WHITE);
        txtBuscar.setForeground(new Color(45, 45, 45));
        txtBuscar.setBounds(260, 15, 400, 30);
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        searchPanel.add(txtBuscar);
        getContentPane().add(searchPanel);
        
        // Tabla
        model = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Apellido", "Email", "Rol", "No-Control", "Grupo"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        
        tableEstudiantes = new JTable(model);
        tableEstudiantes.setRowHeight(34);
        tableEstudiantes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableEstudiantes.setFillsViewportHeight(true);
        tableEstudiantes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableEstudiantes.setIntercellSpacing(new Dimension(8, 6));
        tableEstudiantes.setGridColor(new Color(240, 240, 240));
        tableEstudiantes.setShowGrid(false);
        tableEstudiantes.setBackground(Color.WHITE);
        tableEstudiantes.setForeground(new Color(40, 40, 40));
        tableEstudiantes.setSelectionBackground(new Color(200, 220, 255));
        tableEstudiantes.setSelectionForeground(new Color(32, 32, 32));
        
        JTableHeader th = tableEstudiantes.getTableHeader();
        th.setBackground(new Color(245, 245, 250));
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setForeground(new Color(70, 70, 70));
        th.setReorderingAllowed(false);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        
        scrollPane = new JScrollPane(tableEstudiantes);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        getContentPane().add(scrollPane);
        
        // Filtro de busqueda
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        tableEstudiantes.setRowSorter(sorter);
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String texto = txtBuscar.getText().trim();
                String filtro = cbFiltro.getSelectedItem().toString();
                if (texto.isEmpty()) {
                    sorter.setRowFilter(null);
                    return;
                }
                int columna = switch (filtro) {
                    case "Nombre" -> 1;
                    case "Apellido" -> 2;
                    case "Email" -> 3;
                    case "No-Control" -> 5;
                    default -> -1;
                };
                if (columna == -1)
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
                else
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, columna));
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        
        // Permitir arrastrar la ventana desde el header
        addDragListener(headerPanel);
        
        // Permitir redimensionar desde los bordes
        setupResizeListeners();
        
        // --------- Panel de los grupos --------
        grupoPanel = new JPanel(null);
        grupoPanel.setBackground(Color.WHITE);
        
        lblGrupo = new JLabel("Seleccionar Grupo:");
        lblGrupo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblGrupo.setForeground(new Color(51, 51, 51));
        lblGrupo.setBounds(10, 10, 140, 30);
        grupoPanel.add(lblGrupo);
        
        cbGrupos = new JComboBox<>();
        cbGrupos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbGrupos.setBackground(Color.WHITE);
        cbGrupos.setForeground(new Color(45, 45, 45));
        cbGrupos.setBounds(150, 10, 220, 30);
        grupoPanel.add(cbGrupos);
        getContentPane().add(grupoPanel);
        
        // -------- Panel de acciones --------
        actionPanel = new JPanel(null);
        actionPanel.setBackground(Color.WHITE);
        getContentPane().add(actionPanel);
        
        btnRefrescar = new RoundedButton("🔄 Refrescar Lista", 20);
        btnEliminar = new RoundedButton("🗑️ Eliminar Estudiante", 20);
        btnEditar = new RoundedButton("✏️ Editar Estudiante", 20);
        btnAgregar = new RoundedButton("➕ Agregar Estudiante", 20);
        btnSendMsg = new RoundedButton("📣 Enviar Mensaje", 20);
        btnGestionar = new RoundedButton("📋 Gestionar Estudiante", 20);
        
        btnCalificaciones = new RoundedButton("📝 Gestionar Calificaciones", 20);
        btnCalificaciones.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btnCalificaciones.setBackground(new Color(245, 245, 245));
        btnCalificaciones.setForeground(new Color(48, 48, 48));
        btnCalificaciones.setFocusPainted(false);
        btnCalificaciones.setBorderPainted(false);
        btnCalificaciones.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCalificaciones.setOpaque(true);
        
        btnImportarCSV = new RoundedButton("📥 Importar CSV", 20);
        
        // Estilo de botones
        styleActionButton(btnGestionar, new Color(245, 245, 245), new Color(230, 230, 230));
        styleActionButton(btnRefrescar, new Color(245, 245, 245), new Color(230, 230, 230));
        styleActionButton(btnAgregar, new Color(245, 245, 245), new Color(230, 230, 230));
        styleActionButton(btnEditar, new Color(245, 245, 245), new Color(230, 230, 230));
        styleActionButton(btnEliminar, new Color(245, 245, 245), new Color(230, 230, 230));
        styleActionButton(btnSendMsg, new Color(245, 245, 245), new Color(230, 230, 230));
        styleActionButton(btnCalificaciones, new Color(245, 245, 245), new Color(230, 230, 230));
        styleActionButton(btnImportarCSV, new Color(245, 245, 245), new Color(230, 230, 230));
        
        actionPanel.add(btnRefrescar);
        actionPanel.add(btnAgregar);
        actionPanel.add(btnEditar);
        actionPanel.add(btnEliminar);
        actionPanel.add(btnSendMsg);
        actionPanel.add(btnCalificaciones);
        actionPanel.add(btnImportarCSV);
        
        // Adaptacion responsive
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustLayout();
            }
        });
        
        // Listeners
        cbGrupos.addActionListener(e -> {
            grupoSeleccionadoId = (cbGrupos.getSelectedIndex() > 0)
                    ? obtenerIdGrupo(cbGrupos.getSelectedItem().toString())
                    : -1;
            cargarEstudiantes();
        });
        
        cargarGrupos();
        cargarEstudiantes();
        aplicarTema();
        
        // Ajustar layout inicial
        SwingUtilities.invokeLater(this::adjustLayout);
    }
    
    // Ajustar el layout
    private void adjustLayout() {
        int frameWidth = getWidth();
        int frameHeight = getHeight();
        
        // Header
        headerPanel.setBounds(0, 0, frameWidth, HEADER_HEIGHT);
        
        // Reposicionar botones de ventana en el header
        Component[] headerComponents = headerPanel.getComponents();
        for (Component comp : headerComponents) {
            if (comp instanceof JPanel && ((JPanel) comp).getLayout() instanceof FlowLayout) {
                comp.setBounds(frameWidth - 130, 10, 120, 40);
                break;
            }
        }
        
        // Panel derecho
        int headerRightX = frameWidth - HEADER_RIGHT_WIDTH;
        int headerRightHeight = frameHeight - HEADER_HEIGHT;
        headerRight.setBounds(headerRightX, HEADER_HEIGHT, HEADER_RIGHT_WIDTH, headerRightHeight);
        
        // Reposicionar botón de logout en el panel derecho
        Component[] rightComponents = headerRight.getComponents();
        for (Component comp : rightComponents) {
            if (comp instanceof RoundedButton) {
                RoundedButton btn = (RoundedButton) comp;
                if (btn.getText().contains("Cerrar Sesion")) {
                    btn.setBounds(25, headerRightHeight - 70, 132, 39);
                }
            }
        }
        
        // Panel de busqueda
        int searchPanelWidth = headerRightX - MARGIN;
        searchPanel.setBounds(MARGIN, HEADER_HEIGHT, searchPanelWidth, SEARCH_PANEL_HEIGHT);
        
        // Ajustar ancho del campo de búsqueda
        int txtBuscarWidth = Math.max(200, searchPanelWidth - 280);
        txtBuscar.setBounds(260, 15, txtBuscarWidth, 30);
        
        // Tabla
        int tablePanelWidth = searchPanelWidth - MARGIN;
        int tableY = HEADER_HEIGHT + SEARCH_PANEL_HEIGHT + MARGIN;
        int tableHeight = frameHeight - tableY - GRUPO_PANEL_HEIGHT - ACTION_PANEL_HEIGHT - (MARGIN * 3);
        scrollPane.setBounds(MARGIN, tableY, tablePanelWidth, tableHeight);
        
        // Panel de grupos
        int grupoPanelY = tableY + tableHeight + MARGIN;
        grupoPanel.setBounds(MARGIN, grupoPanelY, tablePanelWidth, GRUPO_PANEL_HEIGHT);
        
        // Panel de acciones
        int actionPanelY = grupoPanelY + GRUPO_PANEL_HEIGHT + MARGIN;
        actionPanel.setBounds(MARGIN, actionPanelY, tablePanelWidth, ACTION_PANEL_HEIGHT);
        
        // Redistribuir botones en actionPanel según el ancho disponible
        redistributeActionButtons(tablePanelWidth);
        
        revalidate();
        repaint();
    }
    
    // -------- Redistribuir los botones ----------
    private void redistributeActionButtons(int availableWidth) {
        Component[] buttons = actionPanel.getComponents();
        if (buttons.length == 0) return;
        
        int buttonCount = buttons.length;
        int buttonWidth = 160;
        int buttonHeight = 30;
        int gap = 10;
        
        // Calcular cuántos botones caben por fila
        int buttonsPerRow = Math.max(1, (availableWidth + gap) / (buttonWidth + gap));
        
        // Si todos los botones caben en una fila
        if (buttonCount <= buttonsPerRow) {
            int totalWidth = (buttonCount * buttonWidth) + ((buttonCount - 1) * gap);
            int startX = (availableWidth - totalWidth) / 2;
            
            for (int i = 0; i < buttonCount; i++) {
                int x = startX + (i * (buttonWidth + gap));
                buttons[i].setBounds(x, 10, buttonWidth, buttonHeight);
            }
        } else {
            // Distribuir en múltiples filas
            int rows = (int) Math.ceil((double) buttonCount / buttonsPerRow);
            int currentRow = 0;
            int currentCol = 0;
            
            for (int i = 0; i < buttonCount; i++) {
                int buttonsInThisRow = Math.min(buttonsPerRow, buttonCount - (currentRow * buttonsPerRow));
                int totalRowWidth = (buttonsInThisRow * buttonWidth) + ((buttonsInThisRow - 1) * gap);
                int startX = (availableWidth - totalRowWidth) / 2;
                
                int x = startX + (currentCol * (buttonWidth + gap));
                int y = 10 + (currentRow * (buttonHeight + gap));
                
                buttons[i].setBounds(x, y, buttonWidth, buttonHeight);
                
                currentCol++;
                if (currentCol >= buttonsPerRow) {
                    currentCol = 0;
                    currentRow++;
                }
            }
            
            // Ajustar altura del panel si es necesario
            int requiredHeight = (rows * buttonHeight) + ((rows - 1) * gap) + 20;
            if (requiredHeight > ACTION_PANEL_HEIGHT) {
                actionPanel.setPreferredSize(new Dimension(availableWidth, requiredHeight));
            }
        }
    }
    
    // // Botones del topBar
    private JButton createTopButton(String txt, ActionListener evt) {
        JButton b = new JButton(txt);
        b.setFocusable(false);
        b.setBackground(new Color(60, 60, 60));
        b.setForeground(Color.WHITE);
        b.setBorder(null);
        b.setPreferredSize(new Dimension(40, 40));
        b.addActionListener(evt);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
    
    // Maximizar o reducir la ventana con una animacion
    private void toggleMaximize() {
        Rectangle target;
        
        if (!maximizado) {
            prevBounds = getBounds();
            target = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        } else {
            target = prevBounds != null ? prevBounds : new Rectangle(100, 100, 1350, 600);
        }
        
        animateBounds(getBounds(), target, 250);
        maximizado = !maximizado;
    }
    
    private void animateBounds(Rectangle start, Rectangle end, int durationMs) {
        if (animTimer != null)
            animTimer.stop();
        
        final long startTime = System.currentTimeMillis();
        animTimer = new Timer(15, e -> {
            float t = (System.currentTimeMillis() - startTime) / (float) durationMs;
            if (t > 1f)
                t = 1f;
            
            float f = (float) (1 - Math.pow(1 - t, 3));
            
            int nx = start.x + Math.round((end.x - start.x) * f);
            int ny = start.y + Math.round((end.y - start.y) * f);
            int nw = start.width + Math.round((end.width - start.width) * f);
            int nh = start.height + Math.round((end.height - start.height) * f);
            
            setBounds(nx, ny, nw, nh);
            
            if (t == 1f)
                animTimer.stop();
        });
        animTimer.start();
    }
    
    // Arrastrar la ventana
    private void addDragListener(JPanel panel) {
        final Point[] p = new Point[1];
        
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Component c = panel.getComponentAt(e.getPoint());
                if (c == panel || c == lblTitulo) {
                    p[0] = e.getPoint();
                }
            }
            
            public void mouseReleased(MouseEvent e) {
                p[0] = null;
            }
        });
        
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Component c = panel.getComponentAt(e.getPoint());
                if ((c == panel || c == lblTitulo) && p[0] != null) {
                    Point now = e.getLocationOnScreen();
                    Point loc = getLocation();
                    setLocation(loc.x + now.x - p[0].x - loc.x, loc.y + now.y - p[0].y - loc.y);
                }
            }
        });
    }
    
    // ----- Poder redimensionar la ventana arrastrando los bordes
    private void setupResizeListeners() {
        MouseAdapter resizeAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                resizeDirection = getResizeDirection(e.getPoint());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                resizeDirection = RESIZE_NONE;
                setCursor(Cursor.getDefaultCursor());
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                updateCursor(e.getPoint());
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (resizeDirection != RESIZE_NONE) {
                    resizeWindow(e.getPoint());
                }
            }
        };
        
        addMouseListener(resizeAdapter);
        addMouseMotionListener(resizeAdapter);
    }
    
    private int getResizeDirection(Point p) {
        int dir = RESIZE_NONE;
        
        if (p.x < RESIZE_MARGIN) dir |= RESIZE_W;
        else if (p.x > getWidth() - RESIZE_MARGIN) dir |= RESIZE_E;
        
        if (p.y < RESIZE_MARGIN) dir |= RESIZE_N;
        else if (p.y > getHeight() - RESIZE_MARGIN) dir |= RESIZE_S;
        
        return dir;
    }
    
    private void updateCursor(Point p) {
        int dir = getResizeDirection(p);
        
        switch (dir) {
            case RESIZE_N:
            case RESIZE_S:
                setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                break;
            case RESIZE_W:
            case RESIZE_E:
                setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                break;
            case RESIZE_NW:
            case RESIZE_SE:
                setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                break;
            case RESIZE_NE:
            case RESIZE_SW:
                setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                break;
            default:
                setCursor(Cursor.getDefaultCursor());
                break;
        }
    }
    
    private void resizeWindow(Point currentPoint) {
        Rectangle bounds = getBounds();
        int dx = currentPoint.x - initialClick.x;
        int dy = currentPoint.y - initialClick.y;
        
        int minWidth = 800;
        int minHeight = 500;
        
        // Redimensionar según la dirección
        if ((resizeDirection & RESIZE_W) != 0) {
            int newWidth = bounds.width - dx;
            if (newWidth >= minWidth) {
                bounds.x += dx;
                bounds.width = newWidth;
                initialClick.x = currentPoint.x;
            }
        }
        
        if ((resizeDirection & RESIZE_E) != 0) {
            int newWidth = bounds.width + dx;
            if (newWidth >= minWidth) {
                bounds.width = newWidth;
                initialClick.x = currentPoint.x;
            }
        }
        
        if ((resizeDirection & RESIZE_N) != 0) {
            int newHeight = bounds.height - dy;
            if (newHeight >= minHeight) {
                bounds.y += dy;
                bounds.height = newHeight;
                initialClick.y = currentPoint.y;
            }
        }
        
        if ((resizeDirection & RESIZE_S) != 0) {
            int newHeight = bounds.height + dy;
            if (newHeight >= minHeight) {
                bounds.height = newHeight;
                initialClick.y = currentPoint.y;
            }
        }
        
        setBounds(bounds);
    }

    // Métodos auxiliares para obtener datos de la base de datos
    protected void cargarGrupos() {
        cbGrupos.removeAllItems();
        cbGrupos.addItem("Todos los grupos");
        try (Connection cn = connectionDB.conectar();
             PreparedStatement ps = cn.prepareStatement("SELECT id, nombre_grupo FROM grupos");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cbGrupos.addItem(rs.getString("nombre_grupo"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar grupos:\n" + e.getMessage());
        }
    }
    
    protected int obtenerIdGrupo(String nombreGrupo) {
        String sql = "SELECT id FROM grupos WHERE nombre_grupo = ?";
        try (Connection cn = connectionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombreGrupo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            System.err.println("Error al obtener id del grupo: " + e.getMessage());
        }
        return -1;
    }
    
    @Override
    public void cargarEstudiantes() {
        model.setRowCount(0);
        String base = "SELECT u.id, u.nombre, u.apellido, u.email, u.role, u.no_control, g.nombre_grupo " +
                "FROM usuarios u LEFT JOIN grupos g ON u.grupo_id = g.id WHERE u.role = 'ESTUDIANTE'";
        boolean filtroGrupo = grupoSeleccionadoId != -1;
        String query = filtroGrupo ? base + " AND u.grupo_id = ?" : base;
        
        try (Connection cn = connectionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(query)) {
            if (filtroGrupo) ps.setInt(1, grupoSeleccionadoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("nombre"), rs.getString("apellido"),
                        rs.getString("email"), rs.getString("role"),
                        rs.getString("no_control"), rs.getString("nombre_grupo")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar estudiantes:\n" + e.getMessage());
        }
    }
    
    // ======= Métodos abstractos =======
    protected abstract void eliminarEstudiante();
    protected abstract void editarEstudiante();
    protected abstract void agregarEstudiante();
    protected abstract void enviarMensaje();
    
    // ------ Establecer el estilo de los botones --------
    private void styleActionButton(RoundedButton b, Color bg, Color bgHoverBase) {
        b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        b.setBackground(bg);
        b.setForeground(new Color(48, 48, 48));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Color hover = deriveHoverColor(bgHoverBase);
                b.setBackground(hover);
                if (isDark(hover))
                    b.setForeground(Color.WHITE);
                else
                    b.setForeground(new Color(30, 30, 30));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(bg);
                b.setForeground(new Color(48, 48, 48));
            }
        });
    }
    
    private Color deriveHoverColor(Color base) {
        int r = clamp(base.getRed() - 6);
        int g = clamp(base.getGreen() - 6);
        int b = clamp(base.getBlue() - 6);
        return new Color(r, g, b);
    }
    
    private int clamp(int v) {
        return Math.min(255, Math.max(0, v));
    }
    
    private boolean isDark(Color c) {
        double lum = 0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue();
        return lum < 140;
    }
    
    // ----- Implementar ThemeManager ---------
    
    // Establecer el modo obscuro
    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
    }
    
    // Estado actual del modo
    public boolean isDarkMode() {
        return this.darkMode;
    }
    
    // ------- Tema claro / oscuro -------
    public void aplicarTema() {
        if (darkMode) {
            // Modo Obscuro
            Color bg = new Color(34, 38, 48);
            Color panel = new Color(42, 46, 60);
            Color header = new Color(60, 70, 110);
            Color text = new Color(230, 230, 235);
            Color inputBg = new Color(60, 65, 78);
            Color border = new Color(70, 75, 90);
            Color tableHeader = new Color(48, 52, 66);
            
            getContentPane().setBackground(bg);
            
            // Header
            headerPanel.setBackground(header);
            lblTitulo.setForeground(text);
            
            // Panel derecho
            headerRight.setBackground(panel);
            btnSettings.setBackground(new Color(60, 65, 78));
            btnSettings.setForeground(Color.WHITE);
            
            // Panel de búsqueda
            searchPanel.setBackground(panel);
            lblBuscar.setForeground(text);
            cbFiltro.setBackground(inputBg);
            cbFiltro.setForeground(text);
            txtBuscar.setBackground(inputBg);
            txtBuscar.setForeground(text);
            txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(border),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
            
            // Tabla
            scrollPane.getViewport().setBackground(panel);
            JTableHeader th = tableEstudiantes.getTableHeader();
            th.setBackground(tableHeader);
            th.setForeground(text);
            tableEstudiantes.setBackground(new Color(44, 48, 62));
            tableEstudiantes.setForeground(text);
            tableEstudiantes.setSelectionBackground(new Color(70, 80, 110));
            tableEstudiantes.setGridColor(new Color(50, 55, 70));
            
            // Panel de grupos
            grupoPanel.setBackground(panel);
            lblGrupo.setForeground(text);
            cbGrupos.setBackground(inputBg);
            cbGrupos.setForeground(text);
            
            // Panel de acciones
            actionPanel.setBackground(panel);
            
            // Actualizar estilo de botones
            updateButtonStyle(btnGestionar, new Color(60, 65, 78), new Color(50, 55, 68), Color.WHITE);
            updateButtonStyle(btnRefrescar, new Color(60, 65, 78), new Color(50, 55, 68), Color.WHITE);
            updateButtonStyle(btnAgregar, new Color(60, 65, 78), new Color(50, 55, 68), Color.WHITE);
            updateButtonStyle(btnEditar, new Color(60, 65, 78), new Color(50, 55, 68), Color.WHITE);
            updateButtonStyle(btnEliminar, new Color(60, 65, 78), new Color(50, 55, 68), Color.WHITE);
            updateButtonStyle(btnSendMsg, new Color(60, 65, 78), new Color(50, 55, 68), Color.WHITE);
            updateButtonStyle(btnCalificaciones, new Color(60, 65, 78), new Color(50, 55, 68), Color.WHITE);
            
        } else {
            // Modo claro
            Color bg = new Color(250, 250, 252);
            Color panel = new Color(255, 255, 255);
            Color header = new Color(80, 90, 140);
            Color text = new Color(45, 45, 45);
            Color inputBg = new Color(255, 255, 255);
            Color border = new Color(220, 220, 220);
            Color tableHeader = new Color(245, 245, 250);
            
            getContentPane().setBackground(bg);
            
            // Header
            headerPanel.setBackground(header);
            lblTitulo.setForeground(Color.WHITE);
            
            // Panel derecho
            headerRight.setBackground(panel);
            btnSettings.setBackground(new Color(247, 248, 250));
            btnSettings.setForeground(new Color(45, 45, 45));
            
            // Panel de búsqueda
            searchPanel.setBackground(panel);
            lblBuscar.setForeground(text);
            cbFiltro.setBackground(inputBg);
            cbFiltro.setForeground(text);
            txtBuscar.setBackground(inputBg);
            txtBuscar.setForeground(text);
            txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(border),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
            
            // Tabla
            scrollPane.getViewport().setBackground(panel);
            JTableHeader th = tableEstudiantes.getTableHeader();
            th.setBackground(tableHeader);
            th.setForeground(new Color(70, 70, 70));
            tableEstudiantes.setBackground(Color.WHITE);
            tableEstudiantes.setForeground(new Color(40, 40, 40));
            tableEstudiantes.setSelectionBackground(new Color(200, 220, 255));
            tableEstudiantes.setGridColor(new Color(240, 240, 240));
            
            // Panel de grupos
            grupoPanel.setBackground(panel);
            lblGrupo.setForeground(text);
            cbGrupos.setBackground(inputBg);
            cbGrupos.setForeground(text);
            
            // Panel de acciones
            actionPanel.setBackground(panel);
            
            // Actualizar estilo de botones
            updateButtonStyle(btnGestionar, new Color(245, 245, 245), new Color(230, 230, 230), new Color(48, 48, 48));
            updateButtonStyle(btnRefrescar, new Color(245, 245, 245), new Color(230, 230, 230), new Color(48, 48, 48));
            updateButtonStyle(btnAgregar, new Color(245, 245, 245), new Color(230, 230, 230), new Color(48, 48, 48));
            updateButtonStyle(btnEditar, new Color(245, 245, 245), new Color(230, 230, 230), new Color(48, 48, 48));
            updateButtonStyle(btnEliminar, new Color(245, 245, 245), new Color(230, 230, 230), new Color(48, 48, 48));
            updateButtonStyle(btnSendMsg, new Color(245, 245, 245), new Color(230, 230, 230), new Color(48, 48, 48));
            updateButtonStyle(btnCalificaciones, new Color(245, 245, 245), new Color(230, 230, 230), new Color(48, 48, 48));
        }
        
        repaint();
    }
    
    // Actualizar el estilo del boton
    private void updateButtonStyle(RoundedButton btn, Color bg, Color hoverBg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        
        for (MouseListener ml : btn.getMouseListeners()) {
            if (ml instanceof MouseAdapter) {
                btn.removeMouseListener(ml);
            }
        }
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
                if (isDark(hoverBg))
                    btn.setForeground(Color.WHITE);
                else
                    btn.setForeground(new Color(30, 30, 30));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
                btn.setForeground(fg);
            }
        });
    }
}