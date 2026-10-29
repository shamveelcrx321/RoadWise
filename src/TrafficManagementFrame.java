
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@SuppressWarnings("serial")
public class TrafficManagementFrame extends JFrame {

    public static final Color BG_MAIN       = new Color(32, 37, 32);
    public static final Color SIDEBAR_BG    = new Color(25, 30, 26);
    public static final Color CARD_BG       = new Color(43, 51, 44);
    public static final Color CARD_ALT      = new Color(38, 46, 39);
    public static final Color ELEVATED_BG   = new Color(50, 59, 51);
    public static final Color PRIMARY_ACCENT= new Color(162, 186, 145);
    public static final Color ACCENT_HOVER  = new Color(179, 201, 163);
    public static final Color TEXT_MAIN     = new Color(242, 243, 235);
    public static final Color TEXT_MUTED    = new Color(170, 181, 168);
    public static final Color BORDER_COLOR  = new Color(65, 75, 64);
    public static final Color STATUS_WARN   = new Color(216, 172, 96);
    public static final Color STATUS_ERR    = new Color(215, 115, 108);
    public static final Color STATUS_OK     = new Color(125, 175, 120);
    public static final Color INPUT_BG      = new Color(37, 44, 38);
    public static final Color ROW_SELECTED  = new Color(58, 71, 59);

    private static final String VIEW_DASHBOARD   = "Dashboard";
    private static final String VIEW_VEHICLES    = "Vehicles & Drivers";
    private static final String VIEW_VIOLATIONS  = "Violations & Fines";
    private static final String VIEW_CALCULATOR  = "Signal Calculator";
    private static final String VIEW_ACCIDENTS   = "Accident Records";
    private static final String VIEW_RISK        = "Driver Risk";

    private final DatabaseManager db;

    private CardLayout cardLayout;
    private JPanel     contentCards;
    private final List<JButton> navButtons = new ArrayList<>();
    private String currentView = VIEW_DASHBOARD;

    private JLabel lblPageTitle;
    private JLabel lblPageSubtitle;
    private JLabel lblDbStatusIndicator;
    private JButton btnConfigureDb;

    private JLabel lblDashVehicles;
    private JLabel lblDashViolations;
    private JLabel lblDashAccidents;
    private JLabel lblDashUnpaid;
    private JPanel pnlSignalCardContent;
    private DefaultTableModel dashViolationsModel;
    private DefaultTableModel dashAccidentsModel;

    private JTextField txtVehReg;
    private JComboBox<String> cmbVehType;
    private JTextField txtVehDriver;
    private JTextField txtVehLicense;
    private JTextField txtVehContact;
    private JTextField txtVehSearch;
    private DefaultTableModel vehTableModel;
    private JTable vehTable;
    private int selectedVehicleId = -1;

    private JComboBox<VehicleComboItem> cmbViolVehicle;
    private JComboBox<String> cmbViolType;
    private JTextField txtViolDateTime;
    private JTextField txtViolFine;
    private JComboBox<String> cmbViolPayment;
    private JTextField txtViolDesc;
    private JTextField txtViolSearch;
    private JComboBox<String> cmbViolFilterCategory;
    private JComboBox<String> cmbViolFilterStatus;
    private DefaultTableModel violTableModel;
    private JTable violTable;
    private JLabel lblViolUnpaidSummary;
    private int selectedViolationId = -1;

    private JTextField txtNorth;
    private JTextField txtSouth;
    private JTextField txtEast;
    private JTextField txtWest;
    private TrafficAlgorithm.SignalResult lastSignalResult;
    private JLabel lblTimingNorth;
    private JLabel lblTimingSouth;
    private JLabel lblTimingEast;
    private JLabel lblTimingWest;
    private JLabel lblSignalCongestion;
    private JLabel lblSignalBusiest;
    private JTextArea txtSignalExplanation;
    private JButton btnSaveSignalObservation;
    private DefaultTableModel signalHistoryModel;
    private JTable signalHistoryTable;

    private JTextField txtAccLocation;
    private JTextField txtAccDateTime;
    private JComboBox<String> cmbAccSeverity;
    private JTextField txtAccDesc;
    private JTextField txtAccSearch;
    private JComboBox<String> cmbAccFilterSeverity;
    private DefaultTableModel accTableModel;
    private JTable accTable;
    private int selectedAccidentId = -1;

    private JComboBox<VehicleComboItem> cmbRiskVehicle;
    private JLabel lblRiskScore;
    private JLabel lblRiskCategory;
    private JLabel lblRiskDriverName;
    private JLabel lblRiskLicense;
    private JLabel lblRiskTotalViol;
    private JLabel lblRiskUnpaidViol;
    private JTextArea txtRiskBreakdown;

    public TrafficManagementFrame(DatabaseManager db) {
        this.db = db;

        setTitle("Roadwise");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1020, 680));
        setPreferredSize(new Dimension(1180, 720));
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        buildSidebar();
        buildMainContent();

        pack();
        setLocationRelativeTo(null);

        checkDatabaseStatus();
        refreshAllData();
    }

    private void buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(195, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setBackground(SIDEBAR_BG);
        brandPanel.setBorder(new EmptyBorder(16, 14, 12, 14));

        JLabel lblTitle = new JLabel("ROADWISE");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(PRIMARY_ACCENT);
        brandPanel.add(lblTitle, BorderLayout.WEST);

        JPanel topNavContainer = new JPanel();
        topNavContainer.setLayout(new BoxLayout(topNavContainer, BoxLayout.Y_AXIS));
        topNavContainer.setBackground(SIDEBAR_BG);
        topNavContainer.add(brandPanel);

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(SIDEBAR_BG);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        topNavContainer.add(sep);
        topNavContainer.add(Box.createVerticalStrut(8));

        navButtons.clear();
        String[][] navItems = {
            {"Dashboard", VIEW_DASHBOARD},
            {"Vehicles & Drivers", VIEW_VEHICLES},
            {"Violations & Fines", VIEW_VIOLATIONS},
            {"Signal Calculator", VIEW_CALCULATOR},
            {"Accident Records", VIEW_ACCIDENTS},
            {"Driver Risk", VIEW_RISK}
        };

        for (String[] item : navItems) {
            JButton btn = createNavButton(item[0], item[1]);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            btn.setPreferredSize(new Dimension(181, 36));
            topNavContainer.add(btn);
            topNavContainer.add(Box.createVerticalStrut(2));
        }

        sidebar.add(topNavContainer, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
    }

    private JButton createNavButton(String title, String viewName) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> switchView(viewName));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!viewName.equals(currentView)) {
                    btn.setBackground(ELEVATED_BG);
                    btn.setForeground(TEXT_MAIN);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!viewName.equals(currentView)) {
                    btn.setBackground(SIDEBAR_BG);
                    btn.setForeground(TEXT_MUTED);
                }
            }
        });

        btn.putClientProperty("viewName", viewName);
        navButtons.add(btn);
        return btn;
    }

    private void updateNavHighlight() {
        for (JButton b : navButtons) {
            String target = (String) b.getClientProperty("viewName");
            if (target != null && target.equals(currentView)) {
                b.setBackground(CARD_BG);
                b.setForeground(PRIMARY_ACCENT);
                b.setFont(new Font("Segoe UI", Font.BOLD, 12));
                b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, PRIMARY_ACCENT),
                    new EmptyBorder(8, 9, 8, 12)
                ));
            } else {
                b.setBackground(SIDEBAR_BG);
                b.setForeground(TEXT_MUTED);
                b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                b.setBorder(new EmptyBorder(8, 12, 8, 12));
            }
        }
    }

    private void switchView(String viewName) {
        currentView = viewName;
        cardLayout.show(contentCards, viewName);
        updateNavHighlight();
        updateHeaderTitles(viewName);

        if (VIEW_DASHBOARD.equals(viewName)) refreshDashboardData();
        else if (VIEW_VEHICLES.equals(viewName)) refreshVehiclesTable();
        else if (VIEW_VIOLATIONS.equals(viewName)) refreshViolationsData();
        else if (VIEW_CALCULATOR.equals(viewName)) refreshSignalHistoryTable();
        else if (VIEW_ACCIDENTS.equals(viewName)) refreshAccidentsTable();
        else if (VIEW_RISK.equals(viewName)) populateRiskVehicleDropdown();
    }

    private void updateHeaderTitles(String viewName) {
        switch (viewName) {
            case VIEW_DASHBOARD:
                lblPageTitle.setText("Operations Dashboard");
                lblPageSubtitle.setText("Traffic activity and violation overview");
                break;
            case VIEW_VEHICLES:
                lblPageTitle.setText("Vehicles & Drivers");
                lblPageSubtitle.setText("Manage registered vehicles and driver credentials");
                break;
            case VIEW_VIOLATIONS:
                lblPageTitle.setText("Violations & Fines");
                lblPageSubtitle.setText("Record offenses, manage fines, and track payment status");
                break;
            case VIEW_CALCULATOR:
                lblPageTitle.setText("Traffic Signal Calculator");
                lblPageSubtitle.setText("Adaptive proportional-allocation timing engine (120s cycle)");
                break;
            case VIEW_ACCIDENTS:
                lblPageTitle.setText("Accident Records");
                lblPageSubtitle.setText("Log traffic collisions and severity classifications");
                break;
            case VIEW_RISK:
                lblPageTitle.setText("Driver Risk Analysis");
                lblPageSubtitle.setText("Demerit scoring (0-100) based on recorded offenses");
                break;
        }
    }

    private void buildMainContent() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BG_MAIN);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(SIDEBAR_BG);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(10, 18, 10, 18)
        ));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        lblPageTitle = new JLabel("Operations Dashboard");
        lblPageTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblPageTitle.setForeground(TEXT_MAIN);

        lblPageSubtitle = new JLabel("Traffic activity and violation overview");
        lblPageSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPageSubtitle.setForeground(TEXT_MUTED);

        titlePanel.add(lblPageTitle);
        titlePanel.add(Box.createVerticalStrut(1));
        titlePanel.add(lblPageSubtitle);
        topBar.add(titlePanel, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        lblDbStatusIndicator = new JLabel("Checking connection...");
        lblDbStatusIndicator.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDbStatusIndicator.setForeground(STATUS_WARN);
        rightActions.add(lblDbStatusIndicator);

        btnConfigureDb = createSecondaryButton("Configure Database");
        btnConfigureDb.addActionListener(e -> showDatabaseConfigDialog());
        rightActions.add(btnConfigureDb);

        JButton btnRefresh = createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> {
            checkDatabaseStatus();
            refreshAllData();
        });
        rightActions.add(btnRefresh);

        topBar.add(rightActions, BorderLayout.EAST);
        centerPanel.add(topBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setBackground(BG_MAIN);

        contentCards.add(buildDashboardView(), VIEW_DASHBOARD);
        contentCards.add(buildVehiclesView(), VIEW_VEHICLES);
        contentCards.add(buildViolationsView(), VIEW_VIOLATIONS);
        contentCards.add(buildCalculatorView(), VIEW_CALCULATOR);
        contentCards.add(buildAccidentsView(), VIEW_ACCIDENTS);
        contentCards.add(buildRiskView(), VIEW_RISK);

        centerPanel.add(contentCards, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        updateNavHighlight();
    }

    private JPanel buildDashboardView() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 10, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(0, 75));

        lblDashVehicles   = new JLabel("0");
        lblDashViolations = new JLabel("0");
        lblDashAccidents  = new JLabel("0");
        lblDashUnpaid     = new JLabel("₹0");

        statsRow.add(createDashboardCard("Registered Vehicles", lblDashVehicles, PRIMARY_ACCENT));
        statsRow.add(createDashboardCard("Recorded Violations", lblDashViolations, STATUS_WARN));
        statsRow.add(createDashboardCard("Recorded Accidents",  lblDashAccidents,  STATUS_ERR));
        statsRow.add(createDashboardCard("Unpaid Fines",       lblDashUnpaid,     STATUS_WARN));

        panel.add(statsRow, BorderLayout.NORTH);

        JPanel mainGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        mainGrid.setOpaque(false);

        JPanel leftCol = new JPanel(new BorderLayout(0, 10));
        leftCol.setOpaque(false);

        JPanel pnlSignal = createPanelCard("Latest Signal Timing");
        pnlSignal.setLayout(new BorderLayout());
        pnlSignal.setPreferredSize(new Dimension(0, 125));

        pnlSignalCardContent = new JPanel();
        pnlSignalCardContent.setLayout(new BoxLayout(pnlSignalCardContent, BoxLayout.Y_AXIS));
        pnlSignalCardContent.setOpaque(false);
        pnlSignal.add(pnlSignalCardContent, BorderLayout.CENTER);
        leftCol.add(pnlSignal, BorderLayout.NORTH);

        JPanel pnlRecentAccidents = createPanelCard("Recent Accident Incident Reports");
        pnlRecentAccidents.setLayout(new BorderLayout(0, 6));

        dashAccidentsModel = new DefaultTableModel(
            new String[]{"Incident Location", "Date & Time", "Severity Level", "Description"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblAccRecent = createStyledTable(dashAccidentsModel, "No accident records logged yet.");
        setTableColumnWidths(tblAccRecent, 130, 110, 80, 150);
        pnlRecentAccidents.add(createScrollPane(tblAccRecent), BorderLayout.CENTER);
        leftCol.add(pnlRecentAccidents, BorderLayout.CENTER);

        mainGrid.add(leftCol);

        JPanel pnlRecent = createPanelCard("Recent Recorded Violations");
        pnlRecent.setLayout(new BorderLayout(0, 6));

        dashViolationsModel = new DefaultTableModel(
            new String[]{"Vehicle Reg", "Offense Category", "Fine (₹)", "Status"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblRecent = createStyledTable(dashViolationsModel, "No violations recorded yet.");
        setTableColumnWidths(tblRecent, 110, 150, 75, 80);
        pnlRecent.add(createScrollPane(tblRecent), BorderLayout.CENTER);

        mainGrid.add(pnlRecent);

        panel.add(mainGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildVehiclesView() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel mainLayout = new JPanel(new BorderLayout(0, 10));
        mainLayout.setOpaque(false);

        JPanel formCard = createPanelCard("Register Vehicle & Driver Record");
        formCard.setLayout(new BorderLayout(0, 8));

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtVehReg     = createInputField("", 12);
        cmbVehType    = createDropdown(new String[]{"Car", "Motorcycle", "Scooter", "Bus", "Truck", "Auto-Rickshaw", "Other"});
        cmbVehType.setPreferredSize(new Dimension(140, 28));
        txtVehDriver  = createInputField("", 16);
        txtVehLicense = createInputField("", 14);
        txtVehContact = createInputField("", 12);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.30;
        formGrid.add(createLabeledField("Registration Number *", txtVehReg), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.25;
        formGrid.add(createLabeledField("Vehicle Type", cmbVehType), gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 0.45;
        formGrid.add(createLabeledField("Driver / Owner Name *", txtVehDriver), gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.30;
        formGrid.add(createLabeledField("Driver License Number *", txtVehLicense), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.25;
        formGrid.add(createLabeledField("Contact Phone", txtVehContact), gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setOpaque(false);
        JButton btnSaveVeh = createPrimaryButton("Save Vehicle");
        JButton btnClearVeh = createSecondaryButton("Clear");
        btnSaveVeh.addActionListener(e -> saveOrUpdateVehicle());
        btnClearVeh.addActionListener(e -> clearVehicleForm());
        btnPanel.add(btnSaveVeh);
        btnPanel.add(btnClearVeh);

        JPanel btnWrapper = new JPanel(new BorderLayout(0, 3));
        btnWrapper.setOpaque(false);
        JLabel lblSpacer = new JLabel(" ");
        lblSpacer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnWrapper.add(lblSpacer, BorderLayout.NORTH);
        btnWrapper.add(btnPanel, BorderLayout.CENTER);

        gbc.gridx = 2; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 0.45;
        formGrid.add(btnWrapper, gbc);

        formCard.add(formGrid, BorderLayout.CENTER);
        mainLayout.add(formCard, BorderLayout.NORTH);

        JPanel tableCard = createPanelCard(null);
        tableCard.setLayout(new BorderLayout(0, 8));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setOpaque(false);
        txtVehSearch = createInputField("", 24);
        txtVehSearch.setPreferredSize(new Dimension(240, 28));
        JButton btnSearch = createSecondaryButton("Search");
        JButton btnReset = createSecondaryButton("Reset");
        btnSearch.addActionListener(e -> searchVehiclesTable());
        btnReset.addActionListener(e -> { txtVehSearch.setText(""); refreshVehiclesTable(); });

        searchRow.add(txtVehSearch);
        searchRow.add(btnSearch);
        searchRow.add(btnReset);
        tableCard.add(searchRow, BorderLayout.NORTH);

        vehTableModel = new DefaultTableModel(
            new String[]{"ID", "Registration No", "Type", "Driver / Owner", "License No", "Contact", "Registration Date"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        vehTable = createStyledTable(vehTableModel, "No vehicles registered yet.");
        setTableColumnWidths(vehTable, 45, 120, 100, 160, 130, 110, 140);
        tableCard.add(createScrollPane(vehTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(4, 0, 0, 0));
        JButton btnEdit = createSecondaryButton("Edit Selected");
        JButton btnDelete = createDestructiveButton("Delete Selected");
        btnEdit.addActionListener(e -> editSelectedVehicle());
        btnDelete.addActionListener(e -> deleteSelectedVehicle());
        actions.add(btnEdit);
        actions.add(btnDelete);
        tableCard.add(actions, BorderLayout.SOUTH);

        vehTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSel = vehTable.getSelectedRow() != -1;
            btnEdit.setEnabled(hasSel);
            btnDelete.setEnabled(hasSel);
        });
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        mainLayout.add(tableCard, BorderLayout.CENTER);
        panel.add(mainLayout, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildViolationsView() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel mainLayout = new JPanel(new BorderLayout(0, 10));
        mainLayout.setOpaque(false);

        JPanel formCard = createPanelCard("Record a Violation");
        formCard.setLayout(new BorderLayout(0, 8));

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cmbViolVehicle  = new JComboBox<>();
        cmbViolVehicle.setPreferredSize(new Dimension(200, 28));
        styleDropdownComponent(cmbViolVehicle);

        cmbViolType     = createDropdown(Violation.CATEGORIES);
        cmbViolType.setPreferredSize(new Dimension(190, 28));

        txtViolDateTime = createInputField(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()), 14);
        txtViolFine     = createInputField("1500", 8);
        cmbViolPayment  = createDropdown(new String[]{"UNPAID", "PAID"});
        cmbViolPayment.setPreferredSize(new Dimension(100, 28));

        txtViolDesc     = createInputField("", 20);

        cmbViolType.addActionListener(e -> {
            String cat = (String) cmbViolType.getSelectedItem();
            if (cat != null) txtViolFine.setText(String.valueOf((int) Violation.getDefaultFine(cat)));
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.35;
        formGrid.add(createLabeledField("Registered Vehicle *", cmbViolVehicle), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.35;
        formGrid.add(createLabeledField("Violation Category *", cmbViolType), gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 0.30;
        formGrid.add(createLabeledField("Date & Time *", txtViolDateTime), gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.22;
        formGrid.add(createLabeledField("Fine Amount (₹) *", txtViolFine), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.22;
        formGrid.add(createLabeledField("Payment Status *", cmbViolPayment), gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.38;
        formGrid.add(createLabeledField("Notes / Location", txtViolDesc), gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setOpaque(false);
        JButton btnSaveViol = createPrimaryButton("Save Violation");
        JButton btnClearViol = createSecondaryButton("Clear");
        btnSaveViol.addActionListener(e -> saveOrUpdateViolation());
        btnClearViol.addActionListener(e -> clearViolationForm());
        btnPanel.add(btnSaveViol);
        btnPanel.add(btnClearViol);

        JPanel btnWrapper = new JPanel(new BorderLayout(0, 3));
        btnWrapper.setOpaque(false);
        JLabel lblSpacer = new JLabel(" ");
        lblSpacer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnWrapper.add(lblSpacer, BorderLayout.NORTH);
        btnWrapper.add(btnPanel, BorderLayout.CENTER);

        gbc.gridx = 3; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.18;
        formGrid.add(btnWrapper, gbc);

        formCard.add(formGrid, BorderLayout.CENTER);
        mainLayout.add(formCard, BorderLayout.NORTH);

        JPanel tableCard = createPanelCard(null);
        tableCard.setLayout(new BorderLayout(0, 8));

        JPanel filterRow = new JPanel(new BorderLayout());
        filterRow.setOpaque(false);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        txtViolSearch = createInputField("", 16);
        txtViolSearch.setPreferredSize(new Dimension(180, 28));

        List<String> catOptions = new ArrayList<>();
        catOptions.add("All Categories");
        catOptions.addAll(Arrays.asList(Violation.CATEGORIES));
        cmbViolFilterCategory = createDropdown(catOptions.toArray(new String[0]));
        cmbViolFilterCategory.setPreferredSize(new Dimension(150, 28));

        cmbViolFilterStatus = createDropdown(new String[]{"All Statuses", "UNPAID", "PAID"});
        cmbViolFilterStatus.setPreferredSize(new Dimension(110, 28));

        JButton btnFilter = createSecondaryButton("Filter");
        btnFilter.addActionListener(e -> searchViolationsTable());
        JButton btnReset = createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            txtViolSearch.setText("");
            cmbViolFilterCategory.setSelectedIndex(0);
            cmbViolFilterStatus.setSelectedIndex(0);
            refreshViolationsTable();
        });

        filters.add(txtViolSearch);
        filters.add(cmbViolFilterCategory);
        filters.add(cmbViolFilterStatus);
        filters.add(btnFilter);
        filters.add(btnReset);
        filterRow.add(filters, BorderLayout.WEST);

        lblViolUnpaidSummary = new JLabel("Total Unpaid Fines: ₹0", SwingConstants.RIGHT);
        lblViolUnpaidSummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblViolUnpaidSummary.setForeground(STATUS_WARN);
        filterRow.add(lblViolUnpaidSummary, BorderLayout.EAST);

        tableCard.add(filterRow, BorderLayout.NORTH);

        violTableModel = new DefaultTableModel(
            new String[]{"ID", "Vehicle Reg", "Driver Name", "Violation Category", "Date & Time", "Fine (₹)", "Status", "Notes"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        violTable = createStyledTable(violTableModel, "No violations recorded yet.");
        setTableColumnWidths(violTable, 45, 110, 130, 160, 125, 75, 85, 180);
        tableCard.add(createScrollPane(violTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(4, 0, 0, 0));

        JButton btnToggle = createSecondaryButton("Toggle Paid / Unpaid");
        JButton btnEdit = createSecondaryButton("Edit Selected");
        JButton btnDelete = createDestructiveButton("Delete Selected");

        btnToggle.addActionListener(e -> toggleViolationPaymentStatus());
        btnEdit.addActionListener(e -> editSelectedViolation());
        btnDelete.addActionListener(e -> deleteSelectedViolation());

        actions.add(btnToggle);
        actions.add(btnEdit);
        actions.add(btnDelete);
        tableCard.add(actions, BorderLayout.SOUTH);

        violTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSel = violTable.getSelectedRow() != -1;
            btnToggle.setEnabled(hasSel);
            btnEdit.setEnabled(hasSel);
            btnDelete.setEnabled(hasSel);
        });
        btnToggle.setEnabled(false);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        mainLayout.add(tableCard, BorderLayout.CENTER);
        panel.add(mainLayout, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCalculatorView() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel mainLayout = new JPanel(new BorderLayout(0, 10));
        mainLayout.setOpaque(false);

        JPanel topCard = createPanelCard("Calculate Signal Timings (Proportional Allocation)");
        topCard.setLayout(new BorderLayout(0, 8));

        JPanel inputsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        inputsRow.setOpaque(false);

        txtNorth = createInputField("35", 5);
        txtSouth = createInputField("20", 5);
        txtEast  = createInputField("45", 5);
        txtWest  = createInputField("15", 5);
        txtNorth.setPreferredSize(new Dimension(65, 28));
        txtSouth.setPreferredSize(new Dimension(65, 28));
        txtEast.setPreferredSize(new Dimension(65, 28));
        txtWest.setPreferredSize(new Dimension(65, 28));

        inputsRow.add(createLabeledField("North Count:", txtNorth));
        inputsRow.add(createLabeledField("South Count:", txtSouth));
        inputsRow.add(createLabeledField("East Count:", txtEast));
        inputsRow.add(createLabeledField("West Count:", txtWest));

        JButton btnCalc = createPrimaryButton("Calculate Signal Timing");
        btnSaveSignalObservation = createSecondaryButton("Save to Database");
        btnSaveSignalObservation.setEnabled(false);

        btnCalc.addActionListener(e -> calculateSignalTiming());
        btnSaveSignalObservation.addActionListener(e -> saveSignalObservationToDb());

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(btnCalc);
        btnWrap.add(btnSaveSignalObservation);

        JPanel btnWrapOuter = new JPanel(new BorderLayout(0, 3));
        btnWrapOuter.setOpaque(false);
        JLabel spacer = new JLabel(" ");
        spacer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnWrapOuter.add(spacer, BorderLayout.NORTH);
        btnWrapOuter.add(btnWrap, BorderLayout.CENTER);
        inputsRow.add(btnWrapOuter);

        topCard.add(inputsRow, BorderLayout.NORTH);

        JPanel resultsGrid = new JPanel(new GridLayout(1, 4, 10, 0));
        resultsGrid.setOpaque(false);
        resultsGrid.setBorder(new EmptyBorder(6, 0, 6, 0));

        lblTimingNorth = new JLabel("30s", SwingConstants.CENTER);
        lblTimingSouth = new JLabel("30s", SwingConstants.CENTER);
        lblTimingEast  = new JLabel("30s", SwingConstants.CENTER);
        lblTimingWest  = new JLabel("30s", SwingConstants.CENTER);

        resultsGrid.add(createDirectionalBadge("North Approach", lblTimingNorth));
        resultsGrid.add(createDirectionalBadge("South Approach", lblTimingSouth));
        resultsGrid.add(createDirectionalBadge("East Approach", lblTimingEast));
        resultsGrid.add(createDirectionalBadge("West Approach", lblTimingWest));
        topCard.add(resultsGrid, BorderLayout.CENTER);

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        statusRow.setOpaque(false);
        lblSignalCongestion = new JLabel("Congestion Rating: -");
        lblSignalCongestion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSignalCongestion.setForeground(TEXT_MAIN);

        lblSignalBusiest = new JLabel("Busiest Approach: -");
        lblSignalBusiest.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSignalBusiest.setForeground(PRIMARY_ACCENT);

        statusRow.add(lblSignalCongestion);
        statusRow.add(lblSignalBusiest);
        topCard.add(statusRow, BorderLayout.SOUTH);

        mainLayout.add(topCard, BorderLayout.NORTH);

        JPanel centerSplit = new JPanel(new GridLayout(1, 2, 12, 0));
        centerSplit.setOpaque(false);

        JPanel explCard = createPanelCard("Calculation Explanation");
        explCard.setLayout(new BorderLayout());
        txtSignalExplanation = new JTextArea("Enter directional counts above and click 'Calculate Signal Timing' to see the proportional allocation calculation.");
        txtSignalExplanation.setEditable(false);
        txtSignalExplanation.setLineWrap(true);
        txtSignalExplanation.setWrapStyleWord(true);
        txtSignalExplanation.setBackground(INPUT_BG);
        txtSignalExplanation.setForeground(TEXT_MAIN);
        txtSignalExplanation.setFont(new Font("Consolas", Font.PLAIN, 11));
        txtSignalExplanation.setBorder(new EmptyBorder(8, 8, 8, 8));
        explCard.add(createScrollPane(txtSignalExplanation), BorderLayout.CENTER);
        centerSplit.add(explCard);

        JPanel histCard = createPanelCard("Saved Observations History");
        histCard.setLayout(new BorderLayout(0, 6));
        signalHistoryModel = new DefaultTableModel(
            new String[]{"ID", "Timestamp", "Total", "Congestion", "N", "S", "E", "W"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        signalHistoryTable = createStyledTable(signalHistoryModel, "No signal observations saved yet.");
        setTableColumnWidths(signalHistoryTable, 45, 125, 55, 65, 50, 50, 50, 50);
        histCard.add(createScrollPane(signalHistoryTable), BorderLayout.CENTER);

        JPanel histActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        histActions.setOpaque(false);
        JButton btnDeleteObs = createDestructiveButton("Delete Selected");
        btnDeleteObs.addActionListener(e -> deleteSelectedObservation());
        histActions.add(btnDeleteObs);
        histCard.add(histActions, BorderLayout.SOUTH);

        signalHistoryTable.getSelectionModel().addListSelectionListener(e -> {
            btnDeleteObs.setEnabled(signalHistoryTable.getSelectedRow() != -1);
        });
        btnDeleteObs.setEnabled(false);

        centerSplit.add(histCard);
        mainLayout.add(centerSplit, BorderLayout.CENTER);

        panel.add(mainLayout, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAccidentsView() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel mainLayout = new JPanel(new BorderLayout(0, 10));
        mainLayout.setOpaque(false);

        JPanel formCard = createPanelCard("Log Traffic Accident Incident");
        formCard.setLayout(new BorderLayout(0, 8));

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtAccLocation = createInputField("", 16);
        txtAccDateTime = createInputField(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()), 14);
        cmbAccSeverity = createDropdown(Accident.SEVERITY_LEVELS);
        cmbAccSeverity.setPreferredSize(new Dimension(140, 28));
        txtAccDesc     = createInputField("", 20);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.40;
        formGrid.add(createLabeledField("Incident Location / Junction *", txtAccLocation), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.30;
        formGrid.add(createLabeledField("Date & Time *", txtAccDateTime), gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 0.30;
        formGrid.add(createLabeledField("Severity Level *", cmbAccSeverity), gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 0.70;
        formGrid.add(createLabeledField("Collision Notes / Description", txtAccDesc), gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setOpaque(false);
        JButton btnSaveAcc = createPrimaryButton("Save Incident");
        JButton btnClearAcc = createSecondaryButton("Clear");
        btnSaveAcc.addActionListener(e -> saveOrUpdateAccident());
        btnClearAcc.addActionListener(e -> clearAccidentForm());
        btnPanel.add(btnSaveAcc);
        btnPanel.add(btnClearAcc);

        JPanel btnWrapper = new JPanel(new BorderLayout(0, 3));
        btnWrapper.setOpaque(false);
        JLabel lblSpacer = new JLabel(" ");
        lblSpacer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnWrapper.add(lblSpacer, BorderLayout.NORTH);
        btnWrapper.add(btnPanel, BorderLayout.CENTER);

        gbc.gridx = 2; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 0.30;
        formGrid.add(btnWrapper, gbc);

        formCard.add(formGrid, BorderLayout.CENTER);
        mainLayout.add(formCard, BorderLayout.NORTH);

        JPanel tableCard = createPanelCard(null);
        tableCard.setLayout(new BorderLayout(0, 8));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setOpaque(false);

        txtAccSearch = createInputField("", 20);
        txtAccSearch.setPreferredSize(new Dimension(200, 28));

        List<String> sevOptions = new ArrayList<>();
        sevOptions.add("All Severities");
        sevOptions.addAll(Arrays.asList(Accident.SEVERITY_LEVELS));
        cmbAccFilterSeverity = createDropdown(sevOptions.toArray(new String[0]));
        cmbAccFilterSeverity.setPreferredSize(new Dimension(140, 28));

        JButton btnSearch = createSecondaryButton("Filter");
        btnSearch.addActionListener(e -> searchAccidentsTable());
        JButton btnReset = createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            txtAccSearch.setText("");
            cmbAccFilterSeverity.setSelectedIndex(0);
            refreshAccidentsTable();
        });

        searchRow.add(txtAccSearch);
        searchRow.add(cmbAccFilterSeverity);
        searchRow.add(btnSearch);
        searchRow.add(btnReset);
        tableCard.add(searchRow, BorderLayout.NORTH);

        accTableModel = new DefaultTableModel(
            new String[]{"ID", "Location", "Date & Time", "Severity", "Description", "Recorded Date"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        accTable = createStyledTable(accTableModel, "No accident records logged yet.");
        setTableColumnWidths(accTable, 45, 180, 125, 90, 200, 130);
        tableCard.add(createScrollPane(accTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(4, 0, 0, 0));
        JButton btnEdit = createSecondaryButton("Edit Selected");
        JButton btnDelete = createDestructiveButton("Delete Selected");
        btnEdit.addActionListener(e -> editSelectedAccident());
        btnDelete.addActionListener(e -> deleteSelectedAccident());

        actions.add(btnEdit);
        actions.add(btnDelete);
        tableCard.add(actions, BorderLayout.SOUTH);

        accTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSel = accTable.getSelectedRow() != -1;
            btnEdit.setEnabled(hasSel);
            btnDelete.setEnabled(hasSel);
        });
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        mainLayout.add(tableCard, BorderLayout.CENTER);
        panel.add(mainLayout, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRiskView() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel mainLayout = new JPanel(new BorderLayout(0, 10));
        mainLayout.setOpaque(false);

        JPanel topCard = createPanelCard("Select Registered Vehicle to Analyze");
        topCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        cmbRiskVehicle = new JComboBox<>();
        cmbRiskVehicle.setPreferredSize(new Dimension(280, 28));
        styleDropdownComponent(cmbRiskVehicle);

        JButton btnAnalyze = createPrimaryButton("Analyze Driver Risk");
        btnAnalyze.addActionListener(e -> runDriverRiskAnalysis());

        JLabel lblVeh = new JLabel("Registered Vehicle: ");
        lblVeh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblVeh.setForeground(TEXT_MUTED);

        topCard.add(lblVeh);
        topCard.add(cmbRiskVehicle);
        topCard.add(btnAnalyze);
        mainLayout.add(topCard, BorderLayout.NORTH);

        JPanel centerSplit = new JPanel(new GridLayout(1, 2, 12, 0));
        centerSplit.setOpaque(false);

        JPanel profCard = createPanelCard("Driver Risk Summary");
        profCard.setLayout(new BoxLayout(profCard, BoxLayout.Y_AXIS));

        lblRiskDriverName = new JLabel("Driver: -");
        lblRiskDriverName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRiskDriverName.setForeground(TEXT_MAIN);

        lblRiskLicense = new JLabel("License: -");
        lblRiskLicense.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRiskLicense.setForeground(TEXT_MUTED);

        lblRiskTotalViol = new JLabel("Total Recorded Violations: 0");
        lblRiskTotalViol.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRiskTotalViol.setForeground(TEXT_MUTED);

        lblRiskUnpaidViol = new JLabel("Unpaid Violations: 0");
        lblRiskUnpaidViol.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRiskUnpaidViol.setForeground(STATUS_WARN);

        lblRiskScore = new JLabel("Score: 0 / 100");
        lblRiskScore.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblRiskScore.setForeground(TEXT_MAIN);

        lblRiskCategory = new JLabel("LOW RISK");
        lblRiskCategory.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblRiskCategory.setForeground(STATUS_OK);

        profCard.add(lblRiskDriverName);
        profCard.add(Box.createVerticalStrut(6));
        profCard.add(lblRiskLicense);
        profCard.add(Box.createVerticalStrut(6));
        profCard.add(lblRiskTotalViol);
        profCard.add(Box.createVerticalStrut(6));
        profCard.add(lblRiskUnpaidViol);
        profCard.add(Box.createVerticalStrut(14));
        profCard.add(lblRiskScore);
        profCard.add(Box.createVerticalStrut(6));
        profCard.add(lblRiskCategory);

        centerSplit.add(profCard);

        JPanel breakCard = createPanelCard("Demerit Breakdown & Educational Scoring");
        breakCard.setLayout(new BorderLayout());
        txtRiskBreakdown = new JTextArea("Select a driver above and click 'Analyze Driver Risk'.\n\n"
            + "Points Allocation:\n"
            + " - Drunk Driving: +35 pts\n"
            + " - Reckless Driving: +25 pts\n"
            + " - Signal Jumping: +20 pts\n"
            + " - Wrong-Way Driving: +20 pts\n"
            + " - Over Speeding: +15 pts\n"
            + " - Using Mobile Phone: +15 pts\n"
            + " - No Helmet / Seatbelt: +10 pts\n"
            + " - Illegal Parking: +5 pts\n"
            + " - Unpaid Fine Surcharge: +5 pts per unpaid violation\n\n"
            + "Risk Rating Tiers:\n"
            + "  0 - 29: LOW RISK (Safe Driving Record)\n"
            + " 30 - 69: MEDIUM RISK (Caution Advised)\n"
            + " 70 - 100: HIGH RISK (Immediate License Action Recommended)");
        txtRiskBreakdown.setEditable(false);
        txtRiskBreakdown.setLineWrap(true);
        txtRiskBreakdown.setWrapStyleWord(true);
        txtRiskBreakdown.setBackground(INPUT_BG);
        txtRiskBreakdown.setForeground(TEXT_MAIN);
        txtRiskBreakdown.setFont(new Font("Consolas", Font.PLAIN, 11));
        txtRiskBreakdown.setBorder(new EmptyBorder(8, 8, 8, 8));
        breakCard.add(createScrollPane(txtRiskBreakdown), BorderLayout.CENTER);

        centerSplit.add(breakCard);
        mainLayout.add(centerSplit, BorderLayout.CENTER);

        panel.add(mainLayout, BorderLayout.CENTER);
        return panel;
    }

    public void showDatabaseConfigDialog() {
        JDialog dialog = new JDialog(this, "Database Configuration", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BG_MAIN);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("MySQL Connection Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_MAIN);
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));

        JTextField tfUrl = createInputField(db.getJdbcUrl(), 26);
        JTextField tfUser = createInputField(db.getUser(), 14);
        JPasswordField tfPass = new JPasswordField(14);
        styleInputComponent(tfPass);

        panel.add(createLabeledField("JDBC Connection URL:", tfUrl));
        panel.add(createLabeledField("MySQL Username:", tfUser));
        panel.add(createLabeledField("MySQL Password:", tfPass));
        panel.add(Box.createVerticalStrut(10));

        JLabel lblDialogStatus = new JLabel("Enter your MySQL credentials and click 'Save & Connect'.");
        lblDialogStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDialogStatus.setForeground(TEXT_MUTED);
        panel.add(lblDialogStatus);
        panel.add(Box.createVerticalStrut(14));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);

        JButton btnTest = createSecondaryButton("Test Connection");
        JButton btnSave = createPrimaryButton("Save & Connect");
        JButton btnInit = createSecondaryButton("Initialize Tables");
        JButton btnClose = createSecondaryButton("Close");

        btnTest.addActionListener(e -> {
            String u = tfUrl.getText().trim();
            String usr = tfUser.getText().trim();
            String pwd = new String(tfPass.getPassword());
            db.setCredentials(u, usr, pwd);
            DatabaseManager.ConnectionStatus st = db.testConnection();
            if (st.connected) {
                lblDialogStatus.setText(" Connection successful: " + st.message);
                lblDialogStatus.setForeground(STATUS_OK);
            } else {
                lblDialogStatus.setText(" " + st.message);
                lblDialogStatus.setForeground(STATUS_ERR);
            }
            checkDatabaseStatus();
        });

        btnSave.addActionListener(e -> {
            String u = tfUrl.getText().trim();
            String usr = tfUser.getText().trim();
            String pwd = new String(tfPass.getPassword());

            try {
                db.saveCredentialsToProperties(u, usr, pwd);
                DatabaseManager.ConnectionStatus st = db.testConnection();
                if (st.connected) {
                    showSuccess("Credentials saved and connected to MySQL successfully!");
                    checkDatabaseStatus();
                    refreshAllData();
                    dialog.dispose();
                } else {
                    lblDialogStatus.setText(" Saved to db.properties, but connection failed: " + st.message);
                    lblDialogStatus.setForeground(STATUS_ERR);
                    checkDatabaseStatus();
                }
            } catch (IOException ioEx) {
                showError("File I/O Error: Unable to save credentials to db.properties: " + ioEx.getMessage());
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
            }
        });

        btnInit.addActionListener(e -> {
            try {
                if (db.initializeSchema()) {
                    showSuccess("Database schema initialized and verified successfully!");
                    lblDialogStatus.setText(" Database tables verified.");
                    lblDialogStatus.setForeground(STATUS_OK);
                    checkDatabaseStatus();
                    refreshAllData();
                } else {
                    showError("Schema initialization failed: " + db.getLastConnectionError());
                }
            } catch (Exception ex) {
                showError("Database Error: " + ex.getMessage());
            }
        });

        btnClose.addActionListener(e -> dialog.dispose());

        btnRow.add(btnTest);
        btnRow.add(btnSave);
        btnRow.add(btnInit);
        btnRow.add(btnClose);
        panel.add(btnRow);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void checkDatabaseStatus() {
        DatabaseManager.ConnectionStatus st = db.testConnection();
        if (st.connected) {
            lblDbStatusIndicator.setText("Connected");
            lblDbStatusIndicator.setForeground(STATUS_OK);
        } else if (st.requiresPassword) {
            lblDbStatusIndicator.setText("Configuration Required");
            lblDbStatusIndicator.setForeground(STATUS_WARN);
        } else {
            lblDbStatusIndicator.setText("Disconnected");
            lblDbStatusIndicator.setForeground(STATUS_ERR);
        }
    }

    public void refreshAllData() {
        refreshDashboardData();
        refreshVehiclesTable();
        refreshViolationsData();
        refreshSignalHistoryTable();
        refreshAccidentsTable();
        populateRiskVehicleDropdown();
    }

    private void refreshDashboardData() {
        SwingWorker<DatabaseManager.DashboardStats, Void> worker = new SwingWorker<>() {
            @Override
            protected DatabaseManager.DashboardStats doInBackground() {
                try {
                    return db.getDashboardStats();
                } catch (SQLException e) {
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    DatabaseManager.DashboardStats stats = get();
                    if (stats == null) {
                        lblDashVehicles.setText("0");
                        lblDashViolations.setText("0");
                        lblDashAccidents.setText("0");
                        lblDashUnpaid.setText("₹0");
                        dashViolationsModel.setRowCount(0);
                        dashAccidentsModel.setRowCount(0);
                        renderLatestSignalEmpty();
                        return;
                    }

                    lblDashVehicles.setText(String.valueOf(stats.totalVehicles));
                    lblDashViolations.setText(String.valueOf(stats.totalViolations));
                    lblDashAccidents.setText(String.valueOf(stats.totalAccidents));
                    lblDashUnpaid.setText("₹" + (int) stats.totalUnpaidFines);

                    if (stats.latestObservation != null) {
                        renderLatestSignalCard(stats.latestObservation);
                    } else {
                        renderLatestSignalEmpty();
                    }

                    dashViolationsModel.setRowCount(0);
                    for (Violation v : stats.recentViolations) {
                        dashViolationsModel.addRow(new Object[]{
                            v.getRegistrationNumber(),
                            v.getViolationType(),
                            (int) v.getFineAmount(),
                            v.getPaymentStatus()
                        });
                    }

                    dashAccidentsModel.setRowCount(0);
                    for (Accident a : stats.recentAccidents) {
                        dashAccidentsModel.addRow(new Object[]{
                            a.getLocation(),
                            a.getDateTime(),
                            a.getSeverity(),
                            a.getDescription()
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void renderLatestSignalCard(TrafficObservation obs) {
        pnlSignalCardContent.removeAll();

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel lblTime = new JLabel("#" + obs.getId() + "  -  " + obs.getDateTime());
        lblTime.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTime.setForeground(TEXT_MAIN);

        JLabel lblCong = new JLabel(obs.getCongestionLevel() + " Traffic (" + obs.getTotalVehicles() + " vehicles)");
        lblCong.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCong.setForeground(PRIMARY_ACCENT);

        headerRow.add(lblTime, BorderLayout.WEST);
        headerRow.add(lblCong, BorderLayout.EAST);
        pnlSignalCardContent.add(headerRow);
        pnlSignalCardContent.add(Box.createVerticalStrut(10));

        JPanel badgeGrid = new JPanel(new GridLayout(1, 4, 8, 0));
        badgeGrid.setOpaque(false);

        badgeGrid.add(createDirectionalBadge("North", new JLabel(obs.getNorthGreen() + "s (" + obs.getNorthCount() + ")", SwingConstants.CENTER)));
        badgeGrid.add(createDirectionalBadge("South", new JLabel(obs.getSouthGreen() + "s (" + obs.getSouthCount() + ")", SwingConstants.CENTER)));
        badgeGrid.add(createDirectionalBadge("East",  new JLabel(obs.getEastGreen()  + "s (" + obs.getEastCount()  + ")", SwingConstants.CENTER)));
        badgeGrid.add(createDirectionalBadge("West",  new JLabel(obs.getWestGreen()  + "s (" + obs.getWestCount()  + ")", SwingConstants.CENTER)));

        pnlSignalCardContent.add(badgeGrid);
        pnlSignalCardContent.revalidate();
        pnlSignalCardContent.repaint();
    }

    private void renderLatestSignalEmpty() {
        pnlSignalCardContent.removeAll();
        JLabel emptyLbl = new JLabel("No signal calculations saved yet.", SwingConstants.CENTER);
        emptyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emptyLbl.setForeground(TEXT_MUTED);
        emptyLbl.setAlignmentX(CENTER_ALIGNMENT);

        JButton btnOpen = createSecondaryButton("Open Signal Calculator");
        btnOpen.setAlignmentX(CENTER_ALIGNMENT);
        btnOpen.addActionListener(e -> switchView(VIEW_CALCULATOR));

        pnlSignalCardContent.add(Box.createVerticalStrut(14));
        pnlSignalCardContent.add(emptyLbl);
        pnlSignalCardContent.add(Box.createVerticalStrut(8));
        pnlSignalCardContent.add(btnOpen);
        pnlSignalCardContent.revalidate();
        pnlSignalCardContent.repaint();
    }

    private void refreshVehiclesTable() {
        SwingWorker<List<Vehicle>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Vehicle> doInBackground() throws SQLException {
                return db.getAllVehicles();
            }
            @Override
            protected void done() {
                try {
                    List<Vehicle> list = get();
                    vehTableModel.setRowCount(0);
                    for (Vehicle v : list) {
                        vehTableModel.addRow(new Object[]{
                            v.getId(), v.getRegistrationNumber(), v.getVehicleType(),
                            v.getDriverName(), v.getLicenseNumber(), v.getContactNumber(),
                            (v.getCreatedAt() != null ? v.getCreatedAt() : "-")
                        });
                    }
                    populateVehicleDropdowns(list);
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void searchVehiclesTable() {
        String query = txtVehSearch.getText().trim();
        if (query.isEmpty()) { refreshVehiclesTable(); return; }

        SwingWorker<List<Vehicle>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Vehicle> doInBackground() throws SQLException {
                return db.searchVehicles(query);
            }
            @Override
            protected void done() {
                try {
                    List<Vehicle> list = get();
                    vehTableModel.setRowCount(0);
                    for (Vehicle v : list) {
                        vehTableModel.addRow(new Object[]{
                            v.getId(), v.getRegistrationNumber(), v.getVehicleType(),
                            v.getDriverName(), v.getLicenseNumber(), v.getContactNumber(),
                            (v.getCreatedAt() != null ? v.getCreatedAt() : "-")
                        });
                    }
                } catch (Exception e) {
                    showError("Vehicle search failed: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void saveOrUpdateVehicle() {
        String reg = txtVehReg.getText().trim().toUpperCase();
        String type = (String) cmbVehType.getSelectedItem();
        String driver = txtVehDriver.getText().trim();
        String lic = txtVehLicense.getText().trim().toUpperCase();
        String contact = txtVehContact.getText().trim();

        try {
            Vehicle.validate(reg, driver, lic);

            if (selectedVehicleId == -1) {
                Vehicle v = new Vehicle(reg, type, driver, lic, contact);
                if (db.addVehicle(v)) {
                    showSuccess("Vehicle registered successfully.");
                    clearVehicleForm();
                    refreshVehiclesTable();
                    refreshDashboardData();
                } else {
                    showError("Could not register vehicle.");
                }
            } else {
                Vehicle v = new Vehicle(selectedVehicleId, reg, type, driver, lic, contact, null);
                if (db.updateVehicle(v)) {
                    showSuccess("Vehicle record updated successfully.");
                    clearVehicleForm();
                    refreshVehiclesTable();
                    refreshDashboardData();
                } else {
                    showError("Could not update vehicle.");
                }
            }
        } catch (IllegalArgumentException ex) {
            showWarning("Input Validation Notice:\n" + ex.getMessage());
        } catch (SQLException ex) {
            showError("Database Error: " + ex.getMessage());
        } catch (Exception ex) {
            showError("An unexpected error occurred: " + ex.getMessage());
        }
    }

    private void editSelectedVehicle() {
        int row = vehTable.getSelectedRow();
        if (row == -1) { showWarning("Please select a vehicle row to edit."); return; }

        selectedVehicleId = (int) vehTableModel.getValueAt(row, 0);
        txtVehReg.setText((String) vehTableModel.getValueAt(row, 1));
        cmbVehType.setSelectedItem((String) vehTableModel.getValueAt(row, 2));
        txtVehDriver.setText((String) vehTableModel.getValueAt(row, 3));
        txtVehLicense.setText((String) vehTableModel.getValueAt(row, 4));
        txtVehContact.setText((String) vehTableModel.getValueAt(row, 5));
    }

    private void deleteSelectedVehicle() {
        int row = vehTable.getSelectedRow();
        if (row == -1) { showWarning("Please select a vehicle row to delete."); return; }

        int id = (int) vehTableModel.getValueAt(row, 0);
        String reg = (String) vehTableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete vehicle '" + reg + "'?\nAssociated violations will also be removed.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (db.deleteVehicle(id)) {
                    showSuccess("Vehicle deleted.");
                    if (selectedVehicleId == id) clearVehicleForm();
                    refreshVehiclesTable();
                    refreshViolationsData();
                    refreshDashboardData();
                } else {
                    showError("Could not delete vehicle.");
                }
            } catch (SQLException e) {
                showError("Database Error: " + e.getMessage());
            } catch (Exception e) {
                showError("Error: " + e.getMessage());
            }
        }
    }

    private void clearVehicleForm() {
        selectedVehicleId = -1;
        txtVehReg.setText("");
        cmbVehType.setSelectedIndex(0);
        txtVehDriver.setText("");
        txtVehLicense.setText("");
        txtVehContact.setText("");
    }

    private void refreshViolationsData() {
        refreshViolationsTable();
        updateUnpaidFinesBanner();
    }

    private void refreshViolationsTable() {
        SwingWorker<List<Violation>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Violation> doInBackground() throws SQLException {
                return db.getAllViolations();
            }
            @Override
            protected void done() {
                try {
                    List<Violation> list = get();
                    violTableModel.setRowCount(0);
                    for (Violation v : list) {
                        violTableModel.addRow(new Object[]{
                            v.getId(), v.getRegistrationNumber(), v.getDriverName(),
                            v.getViolationType(), v.getDateTime(), (int) v.getFineAmount(),
                            v.getPaymentStatus(), v.getDescription()
                        });
                    }
                    updateUnpaidFinesBanner();
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void searchViolationsTable() {
        final String query = txtViolSearch.getText().trim();
        String c = (String) cmbViolFilterCategory.getSelectedItem();
        final String cat = (c != null && c.startsWith("All")) ? "All" : c;

        String s = (String) cmbViolFilterStatus.getSelectedItem();
        final String status = (s != null && s.startsWith("All")) ? "All" : s;

        SwingWorker<List<Violation>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Violation> doInBackground() throws SQLException {
                return db.searchViolations(query, cat, status);
            }
            @Override
            protected void done() {
                try {
                    List<Violation> list = get();
                    violTableModel.setRowCount(0);
                    for (Violation v : list) {
                        violTableModel.addRow(new Object[]{
                            v.getId(), v.getRegistrationNumber(), v.getDriverName(),
                            v.getViolationType(), v.getDateTime(), (int) v.getFineAmount(),
                            v.getPaymentStatus(), v.getDescription()
                        });
                    }
                } catch (Exception e) {
                    showError("Violation filter failed: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateUnpaidFinesBanner() {
        SwingWorker<Double, Void> worker = new SwingWorker<>() {
            @Override
            protected Double doInBackground() throws SQLException {
                return db.getTotalUnpaidFines();
            }
            @Override
            protected void done() {
                try {
                    double unpaid = get();
                    lblViolUnpaidSummary.setText("Total Unpaid Fines: ₹" + (int) unpaid);
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void saveOrUpdateViolation() {
        VehicleComboItem item = (VehicleComboItem) cmbViolVehicle.getSelectedItem();
        String type = (String) cmbViolType.getSelectedItem();
        String dt = txtViolDateTime.getText().trim();
        String fineStr = txtViolFine.getText().trim();
        String status = (String) cmbViolPayment.getSelectedItem();
        String desc = txtViolDesc.getText().trim();

        try {
            if (item == null) {
                throw new IllegalArgumentException("A registered vehicle must be selected.");
            }

            double fine;
            try {
                fine = Double.parseDouble(fineStr);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Fine amount must be a valid numeric value.");
            }

            Violation.validate(item.vehicleId, type, dt, fine);

            if (selectedViolationId == -1) {
                Violation v = new Violation(item.vehicleId, type, dt, fine, status, desc);
                if (db.addViolation(v)) {
                    showSuccess("Violation recorded successfully.");
                    clearViolationForm();
                    refreshViolationsData();
                    refreshDashboardData();
                } else {
                    showError("Could not save violation.");
                }
            } else {
                Violation v = new Violation(selectedViolationId, item.vehicleId, item.regNumber, "",
                                            type, dt, fine, status, desc, null);
                if (db.updateViolation(v)) {
                    showSuccess("Violation record updated successfully.");
                    clearViolationForm();
                    refreshViolationsData();
                    refreshDashboardData();
                } else {
                    showError("Could not update violation.");
                }
            }
        } catch (IllegalArgumentException ex) {
            showWarning("Validation Notice:\n" + ex.getMessage());
        } catch (SQLException ex) {
            showError("Database Error: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }

    private void toggleViolationPaymentStatus() {
        int row = violTable.getSelectedRow();
        if (row == -1) { showWarning("Please select a violation row to toggle payment status."); return; }

        int id = (int) violTableModel.getValueAt(row, 0);
        String current = (String) violTableModel.getValueAt(row, 6);
        String newStatus = "PAID".equalsIgnoreCase(current) ? "UNPAID" : "PAID";

        try {
            if (db.updatePaymentStatus(id, newStatus)) {
                showSuccess("Status changed to " + newStatus + ".");
                refreshViolationsData();
                refreshDashboardData();
            } else {
                showError("Could not update payment status.");
            }
        } catch (SQLException e) {
            showError("Database Error: " + e.getMessage());
        }
    }

    private void editSelectedViolation() {
        int row = violTable.getSelectedRow();
        if (row == -1) { showWarning("Please select a violation row to edit."); return; }

        selectedViolationId = (int) violTableModel.getValueAt(row, 0);
        String reg = (String) violTableModel.getValueAt(row, 1);
        String type = (String) violTableModel.getValueAt(row, 3);
        String dt = (String) violTableModel.getValueAt(row, 4);
        int fine = (int) violTableModel.getValueAt(row, 5);
        String status = (String) violTableModel.getValueAt(row, 6);
        String desc = (String) violTableModel.getValueAt(row, 7);

        for (int i = 0; i < cmbViolVehicle.getItemCount(); i++) {
            VehicleComboItem it = cmbViolVehicle.getItemAt(i);
            if (it.regNumber.equalsIgnoreCase(reg)) {
                cmbViolVehicle.setSelectedIndex(i);
                break;
            }
        }

        cmbViolType.setSelectedItem(type);
        txtViolDateTime.setText(dt);
        txtViolFine.setText(String.valueOf(fine));
        cmbViolPayment.setSelectedItem(status);
        txtViolDesc.setText(desc);
    }

    private void deleteSelectedViolation() {
        int row = violTable.getSelectedRow();
        if (row == -1) { showWarning("Please select a violation row to delete."); return; }

        int id = (int) violTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete violation record #" + id + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (db.deleteViolation(id)) {
                    showSuccess("Violation deleted.");
                    if (selectedViolationId == id) clearViolationForm();
                    refreshViolationsData();
                    refreshDashboardData();
                } else {
                    showError("Could not delete violation.");
                }
            } catch (SQLException e) {
                showError("Database Error: " + e.getMessage());
            }
        }
    }

    private void clearViolationForm() {
        selectedViolationId = -1;
        if (cmbViolVehicle.getItemCount() > 0) cmbViolVehicle.setSelectedIndex(0);
        cmbViolType.setSelectedIndex(0);
        txtViolDateTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        txtViolFine.setText(String.valueOf((int) Violation.getDefaultFine((String) cmbViolType.getSelectedItem())));
        cmbViolPayment.setSelectedIndex(0);
        txtViolDesc.setText("");
    }

    private void calculateSignalTiming() {
        try {
            int n, s, e, w;
            try {
                n = Integer.parseInt(txtNorth.getText().trim());
                s = Integer.parseInt(txtSouth.getText().trim());
                e = Integer.parseInt(txtEast.getText().trim());
                w = Integer.parseInt(txtWest.getText().trim());
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Directional counts must be valid integers.");
            }

            lastSignalResult = TrafficAlgorithm.calculate(n, s, e, w);

            lblTimingNorth.setText(lastSignalResult.greenTimings[0] + "s (" + n + " veh)");
            lblTimingSouth.setText(lastSignalResult.greenTimings[1] + "s (" + s + " veh)");
            lblTimingEast.setText(lastSignalResult.greenTimings[2] + "s (" + e + " veh)");
            lblTimingWest.setText(lastSignalResult.greenTimings[3] + "s (" + w + " veh)");

            lblSignalCongestion.setText("Congestion Rating: " + lastSignalResult.congestionLevel);
            lblSignalBusiest.setText("Busiest Approach: " + lastSignalResult.busiestDirection);
            txtSignalExplanation.setText(lastSignalResult.explanation);
            btnSaveSignalObservation.setEnabled(true);

        } catch (IllegalArgumentException ex) {
            showWarning("Calculation Input Error:\n" + ex.getMessage());
        } catch (Exception ex) {
            showError("Unexpected error during calculation: " + ex.getMessage());
        }
    }

    private void saveSignalObservationToDb() {
        if (lastSignalResult == null) return;

        try {
            String dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            TrafficObservation obs = new TrafficObservation(
                dt,
                lastSignalResult.counts[0], lastSignalResult.counts[1],
                lastSignalResult.counts[2], lastSignalResult.counts[3],
                lastSignalResult.totalVehicles,
                lastSignalResult.congestionLevel,
                lastSignalResult.greenTimings[0], lastSignalResult.greenTimings[1],
                lastSignalResult.greenTimings[2], lastSignalResult.greenTimings[3]
            );

            if (db.saveObservation(obs)) {
                showSuccess("Signal observation saved to database.");
                btnSaveSignalObservation.setEnabled(false);
                refreshSignalHistoryTable();
                refreshDashboardData();
            } else {
                showError("Could not save observation.");
            }
        } catch (SQLException ex) {
            showError("Database Error: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }

    private void refreshSignalHistoryTable() {
        SwingWorker<List<TrafficObservation>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TrafficObservation> doInBackground() throws SQLException {
                return db.getAllObservations();
            }
            @Override
            protected void done() {
                try {
                    List<TrafficObservation> list = get();
                    signalHistoryModel.setRowCount(0);
                    for (TrafficObservation o : list) {
                        signalHistoryModel.addRow(new Object[]{
                            o.getId(), o.getDateTime(), o.getTotalVehicles(), o.getCongestionLevel(),
                            o.getNorthGreen() + "s", o.getSouthGreen() + "s",
                            o.getEastGreen() + "s", o.getWestGreen() + "s"
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void deleteSelectedObservation() {
        int row = signalHistoryTable.getSelectedRow();
        if (row == -1) { showWarning("Please select a saved observation row to delete."); return; }

        int id = (int) signalHistoryModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Delete saved calculation record #" + id + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (db.deleteObservation(id)) {
                    showSuccess("Observation record deleted.");
                    refreshSignalHistoryTable();
                    refreshDashboardData();
                } else {
                    showError("Could not delete observation.");
                }
            } catch (SQLException ex) {
                showError("Database Error: " + ex.getMessage());
            }
        }
    }

    private void refreshAccidentsTable() {
        SwingWorker<List<Accident>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Accident> doInBackground() throws SQLException {
                return db.getAllAccidents();
            }
            @Override
            protected void done() {
                try {
                    List<Accident> list = get();
                    accTableModel.setRowCount(0);
                    for (Accident a : list) {
                        accTableModel.addRow(new Object[]{
                            a.getId(), a.getLocation(), a.getDateTime(),
                            a.getSeverity(), a.getDescription(),
                            (a.getCreatedAt() != null ? a.getCreatedAt() : "-")
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void searchAccidentsTable() {
        final String query = txtAccSearch.getText().trim();
        String sv = (String) cmbAccFilterSeverity.getSelectedItem();
        final String sev = (sv != null && sv.startsWith("All")) ? "All" : sv;

        SwingWorker<List<Accident>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Accident> doInBackground() throws SQLException {
                return db.searchAccidents(query, sev);
            }
            @Override
            protected void done() {
                try {
                    List<Accident> list = get();
                    accTableModel.setRowCount(0);
                    for (Accident a : list) {
                        accTableModel.addRow(new Object[]{
                            a.getId(), a.getLocation(), a.getDateTime(),
                            a.getSeverity(), a.getDescription(),
                            (a.getCreatedAt() != null ? a.getCreatedAt() : "-")
                        });
                    }
                } catch (Exception e) {
                    showError("Accident filter failed: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void saveOrUpdateAccident() {
        String loc = txtAccLocation.getText().trim();
        String dt = txtAccDateTime.getText().trim();
        String sev = (String) cmbAccSeverity.getSelectedItem();
        String desc = txtAccDesc.getText().trim();

        try {
            Accident.validate(loc, dt);

            if (selectedAccidentId == -1) {
                Accident a = new Accident(loc, dt, sev, desc);
                if (db.addAccident(a)) {
                    showSuccess("Accident record saved.");
                    clearAccidentForm();
                    refreshAccidentsTable();
                    refreshDashboardData();
                } else {
                    showError("Could not save accident record.");
                }
            } else {
                Accident a = new Accident(selectedAccidentId, loc, dt, sev, desc, null);
                if (db.updateAccident(a)) {
                    showSuccess("Accident record updated.");
                    clearAccidentForm();
                    refreshAccidentsTable();
                    refreshDashboardData();
                } else {
                    showError("Could not update accident record.");
                }
            }
        } catch (IllegalArgumentException ex) {
            showWarning("Validation Notice:\n" + ex.getMessage());
        } catch (SQLException ex) {
            showError("Database Error: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }

    private void editSelectedAccident() {
        int row = accTable.getSelectedRow();
        if (row == -1) { showWarning("Please select an accident row to edit."); return; }

        selectedAccidentId = (int) accTableModel.getValueAt(row, 0);
        txtAccLocation.setText((String) accTableModel.getValueAt(row, 1));
        txtAccDateTime.setText((String) accTableModel.getValueAt(row, 2));
        cmbAccSeverity.setSelectedItem((String) accTableModel.getValueAt(row, 3));
        txtAccDesc.setText((String) accTableModel.getValueAt(row, 4));
    }

    private void deleteSelectedAccident() {
        int row = accTable.getSelectedRow();
        if (row == -1) { showWarning("Please select an accident to delete."); return; }

        int id = (int) accTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Delete accident record #" + id + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (db.deleteAccident(id)) {
                    showSuccess("Record deleted.");
                    if (selectedAccidentId == id) clearAccidentForm();
                    refreshAccidentsTable();
                    refreshDashboardData();
                } else {
                    showError("Could not delete record.");
                }
            } catch (SQLException ex) {
                showError("Database Error: " + ex.getMessage());
            }
        }
    }

    private void clearAccidentForm() {
        selectedAccidentId = -1;
        txtAccLocation.setText("");
        txtAccDateTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        cmbAccSeverity.setSelectedIndex(0);
        txtAccDesc.setText("");
    }

    private void runDriverRiskAnalysis() {
        VehicleComboItem item = (VehicleComboItem) cmbRiskVehicle.getSelectedItem();

        try {
            if (item == null) {
                throw new IllegalArgumentException("Please select a registered vehicle from the dropdown.");
            }

            DatabaseManager.RiskAnalysisResult res = db.analyzeDriverRisk(item.vehicleId);
            if (res == null) {
                showError("Unable to analyze driver records.");
                return;
            }

            lblRiskDriverName.setText("Driver: " + res.vehicle.getDriverName() + " (" + res.vehicle.getRegistrationNumber() + ")");
            lblRiskLicense.setText("License: " + res.vehicle.getLicenseNumber() + " | Type: " + res.vehicle.getVehicleType());
            lblRiskTotalViol.setText("Total Recorded Violations: " + res.totalViolations + " (Fines: ₹" + (int) res.totalFines + ")");
            lblRiskUnpaidViol.setText("Unpaid Violations: " + res.unpaidViolations + " (Pending: ₹" + (int) res.totalUnpaidFines + ")");

            lblRiskScore.setText("Score: " + res.riskScore + " / 100");
            lblRiskCategory.setText(res.riskLevel + " RISK");

            if ("LOW".equals(res.riskLevel)) lblRiskCategory.setForeground(STATUS_OK);
            else if ("MEDIUM".equals(res.riskLevel)) lblRiskCategory.setForeground(STATUS_WARN);
            else lblRiskCategory.setForeground(STATUS_ERR);

            txtRiskBreakdown.setText(res.explanation);

        } catch (IllegalArgumentException ex) {
            showWarning("Validation Notice:\n" + ex.getMessage());
        } catch (SQLException ex) {
            showError("Database Error: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }

    private static class VehicleComboItem {
        public final int vehicleId;
        public final String regNumber;
        public final String driverName;

        public VehicleComboItem(int vehicleId, String regNumber, String driverName) {
            this.vehicleId = vehicleId;
            this.regNumber = regNumber;
            this.driverName = driverName;
        }

        @Override public String toString() {
            return regNumber + "  -  " + driverName;
        }
    }

    private void populateVehicleDropdowns(List<Vehicle> list) {
        cmbViolVehicle.removeAllItems();
        cmbRiskVehicle.removeAllItems();
        for (Vehicle v : list) {
            VehicleComboItem item = new VehicleComboItem(v.getId(), v.getRegistrationNumber(), v.getDriverName());
            cmbViolVehicle.addItem(item);
            cmbRiskVehicle.addItem(item);
        }
    }

    private void populateRiskVehicleDropdown() {
        SwingWorker<List<Vehicle>, Void> worker = new SwingWorker<>() {
            @Override protected List<Vehicle> doInBackground() throws SQLException { return db.getAllVehicles(); }
            @Override protected void done() {
                try { populateVehicleDropdowns(get()); } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private JPanel createPanelCard(String title) {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));

        if (title != null && !title.isEmpty()) {
            JLabel lbl = new JLabel(title);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(TEXT_MAIN);
            lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
            p.setLayout(new BorderLayout());
            p.add(lbl, BorderLayout.NORTH);
        }
        return p;
    }

    private JPanel createDashboardCard(String label, JLabel valLabel, Color accent) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(TEXT_MUTED);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        valLabel.setForeground(accent);

        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(valLabel);
        return p;
    }

    private JPanel createDirectionalBadge(String direction, JLabel valLabel) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(INPUT_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 8, 6, 8)
        ));

        JLabel l = new JLabel(direction);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(CENTER_ALIGNMENT);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valLabel.setForeground(PRIMARY_ACCENT);
        valLabel.setAlignmentX(CENTER_ALIGNMENT);

        p.add(l);
        p.add(Box.createVerticalStrut(2));
        p.add(valLabel);
        return p;
    }

    private JPanel createLabeledField(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(TEXT_MUTED);

        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JTextField createInputField(String text, int cols) {
        JTextField tf = new JTextField(text, cols);
        styleInputComponent(tf);
        return tf;
    }

    private JComboBox<String> createDropdown(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        styleDropdownComponent(cb);
        return cb;
    }

    private void styleInputComponent(JComponent comp) {
        comp.setBackground(INPUT_BG);
        comp.setForeground(TEXT_MAIN);
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        Border normalBorder = BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(4, 6, 4, 6)
        );
        Border focusBorder = BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY_ACCENT, 1),
            new EmptyBorder(4, 6, 4, 6)
        );
        comp.setBorder(normalBorder);
        comp.setPreferredSize(new Dimension(comp.getPreferredSize().width, 28));

        if (comp instanceof JTextField tf) {
            tf.setCaretColor(TEXT_MAIN);
            tf.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { tf.setBorder(focusBorder); }
                @Override public void focusLost(FocusEvent e) { tf.setBorder(normalBorder); }
            });
        }
    }

    private void styleDropdownComponent(JComboBox<?> cb) {
        cb.setUI(new ModernComboBoxUI());
        cb.setBackground(INPUT_BG);
        cb.setForeground(TEXT_MAIN);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        Border normalBorder = new LineBorder(BORDER_COLOR, 1);
        Border focusBorder = new LineBorder(PRIMARY_ACCENT, 1);
        cb.setBorder(normalBorder);
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width, 28));
        cb.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { cb.setBorder(focusBorder); }
            @Override public void focusLost(FocusEvent e) { cb.setBorder(normalBorder); }
        });
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBackground(isSelected ? ROW_SELECTED : INPUT_BG);
                l.setForeground(TEXT_MAIN);
                l.setBorder(new EmptyBorder(4, 6, 4, 6));
                return l;
            }
        });
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(PRIMARY_ACCENT);
        btn.setForeground(SIDEBAR_BG);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(ACCENT_HOVER); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(PRIMARY_ACCENT); }
        });
        return btn;
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(ELEVATED_BG);
        btn.setForeground(TEXT_MAIN);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(58, 68, 59)); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(ELEVATED_BG); }
        });
        return btn;
    }

    private JButton createDestructiveButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(ELEVATED_BG);
        btn.setForeground(STATUS_ERR);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(110, 55, 52), 1),
            new EmptyBorder(5, 10, 5, 10)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(75, 42, 40)); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(ELEVATED_BG); }
        });
        return btn;
    }

    private JScrollPane createScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(CARD_BG);
        sp.getViewport().setBackground(CARD_BG);
        sp.setBorder(new LineBorder(BORDER_COLOR, 1));
        sp.getVerticalScrollBar().setUnitIncrement(14);
        return sp;
    }

    private void setTableColumnWidths(JTable table, int... widths) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            if (widths[i] > 0) {
                table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
                table.getColumnModel().getColumn(i).setMinWidth(Math.min(30, widths[i]));
            }
        }
    }

    private JTable createStyledTable(DefaultTableModel model) {
        return createStyledTable(model, "No records found.");
    }

    private JTable createStyledTable(DefaultTableModel model, String emptyMsg) {
        JTable table = new JTable(model) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getRowCount() == 0) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    FontMetrics fm = g2.getFontMetrics();
                    String msg = (emptyMsg != null && !emptyMsg.isEmpty()) ? emptyMsg : "No records found.";
                    int x = Math.max(12, (getWidth() - fm.stringWidth(msg)) / 2);
                    int y = Math.max(fm.getHeight() + 28, getHeight() / 2);
                    g2.drawString(msg, x, y);
                    g2.dispose();
                }
            }
        };
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_MAIN);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(TEXT_MAIN);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        JTableHeader th = table.getTableHeader();
        th.setBackground(SIDEBAR_BG);
        th.setForeground(TEXT_MUTED);
        th.setFont(new Font("Segoe UI", Font.BOLD, 11));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(0, 28));

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, row, col);
                l.setBorder(new EmptyBorder(0, 8, 0, 8));

                if (isSel) {
                    l.setBackground(ROW_SELECTED);
                    l.setForeground(TEXT_MAIN);
                } else {
                    l.setBackground((row % 2 == 0) ? CARD_BG : CARD_ALT);
                    l.setForeground(TEXT_MAIN);
                }

                String s = (val != null) ? val.toString() : "";

                if ("PAID".equalsIgnoreCase(s)) {
                    l.setForeground(STATUS_OK);
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                } else if ("UNPAID".equalsIgnoreCase(s)) {
                    l.setForeground(STATUS_WARN);
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                } else if ("Fatal".equalsIgnoreCase(s) || "Severe".equalsIgnoreCase(s)) {
                    l.setForeground(STATUS_ERR);
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                } else if ("Moderate".equalsIgnoreCase(s)) {
                    l.setForeground(STATUS_WARN);
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    l.setHorizontalAlignment(SwingConstants.LEFT);
                }

                return l;
            }
        };

        table.setDefaultRenderer(Object.class, cellRenderer);
        table.setDefaultRenderer(Number.class, cellRenderer);
        table.setDefaultRenderer(Integer.class, cellRenderer);
        table.setDefaultRenderer(Double.class, cellRenderer);
        table.setDefaultRenderer(String.class, cellRenderer);

        return table;
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Notice", JOptionPane.WARNING_MESSAGE);
    }

    private static class ModernComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton btn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(INPUT_BG);
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    g2.setColor(TEXT_MAIN);
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    Polygon poly = new Polygon();
                    poly.addPoint(cx - 4, cy - 2);
                    poly.addPoint(cx + 4, cy - 2);
                    poly.addPoint(cx, cy + 3);
                    g2.fillPolygon(poly);
                    g2.dispose();
                }
            };
            btn.setBackground(INPUT_BG);
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            g.setColor(INPUT_BG);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox) {
                @Override
                protected JScrollPane createScroller() {
                    JScrollPane sp = super.createScroller();
                    sp.setBorder(null);
                    sp.getViewport().setBackground(INPUT_BG);
                    return sp;
                }
            };
            popup.setBorder(new LineBorder(BORDER_COLOR, 1));
            return popup;
        }
    }

}
