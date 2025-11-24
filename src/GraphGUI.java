import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class Graph<V> {
    private final Map<V, java.util.List<Edge<V>>> adjacencyList;
    private final boolean isDirected;
    
    public Graph(boolean isDirected) {
        this.adjacencyList = new HashMap<>();
        this.isDirected = isDirected;
    }
    
    public Graph() {
        this(false);
    }
    
    private static class Edge<V> {
        V to;
        int weight;
        
        Edge(V to, int weight) {
            this.to = to;
            this.weight = weight;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Edge<?> edge = (Edge<?>) obj;
            return Objects.equals(to, edge.to);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(to);
        }
    }
    
    public void addVertex(V v) {
        if (v == null) {
            throw new IllegalArgumentException("Вершина не может быть null");
        }
        adjacencyList.putIfAbsent(v, new ArrayList<>());
    }
    
    public void addEdge(V from, V to, int weight) {
        if (!adjacencyList.containsKey(from) || !adjacencyList.containsKey(to)) {
            throw new IllegalArgumentException("Обе вершины должны существовать в графе");
        }
        
        java.util.List<Edge<V>> edgesFrom = adjacencyList.get(from);
        Edge<V> edge = new Edge<>(to, weight);
        edgesFrom.removeIf(e -> e.to.equals(to));
        edgesFrom.add(edge);
        
        if (!isDirected) {
            java.util.List<Edge<V>> edgesTo = adjacencyList.get(to);
            Edge<V> reverseEdge = new Edge<>(from, weight);
            edgesTo.removeIf(e -> e.to.equals(from));
            edgesTo.add(reverseEdge);
        }
    }
    
    public void addEdge(V from, V to) {
        addEdge(from, to, 1);
    }
    
    public void removeVertex(V v) {
        if (!adjacencyList.containsKey(v)) {
            throw new IllegalArgumentException("Вершина не существует в графе");
        }
        
        for (java.util.List<Edge<V>> edges : adjacencyList.values()) {
            edges.removeIf(edge -> edge.to.equals(v));
        }
        
        adjacencyList.remove(v);
    }
    
    public void removeEdge(V from, V to) {
        if (!adjacencyList.containsKey(from) || !adjacencyList.containsKey(to)) {
            throw new IllegalArgumentException("Обе вершины должны существовать в графе");
        }
        
        java.util.List<Edge<V>> edgesFrom = adjacencyList.get(from);
        edgesFrom.removeIf(edge -> edge.to.equals(to));
        
        if (!isDirected) {
            java.util.List<Edge<V>> edgesTo = adjacencyList.get(to);
            edgesTo.removeIf(edge -> edge.to.equals(from));
        }
    }
    
    public java.util.List<V> getAdjacent(V v) {
        if (!adjacencyList.containsKey(v)) {
            throw new IllegalArgumentException("Вершина не существует в графе");
        }
        
        java.util.List<V> adjacent = new ArrayList<>();
        for (Edge<V> edge : adjacencyList.get(v)) {
            adjacent.add(edge.to);
        }
        return adjacent;
    }
    
    public void dfs(V start) {
        if (!adjacencyList.containsKey(start)) {
            throw new IllegalArgumentException("Начальная вершина не существует в графе");
        }
        
        Set<V> visited = new HashSet<>();
        System.out.print("DFS обход: ");
        dfsRecursive(start, visited);
        System.out.println();
    }
    
    private void dfsRecursive(V current, Set<V> visited) {
        visited.add(current);
        System.out.print(current + " ");
        
        for (Edge<V> edge : adjacencyList.get(current)) {
            if (!visited.contains(edge.to)) {
                dfsRecursive(edge.to, visited);
            }
        }
    }
    
    public void bfs(V start) {
        if (!adjacencyList.containsKey(start)) {
            throw new IllegalArgumentException("Начальная вершина не существует в графе");
        }
        
        Set<V> visited = new HashSet<>();
        Queue<V> queue = new LinkedList<>();
        
        System.out.print("BFS обход: ");
        visited.add(start);
        queue.offer(start);
        
        while (!queue.isEmpty()) {
            V current = queue.poll();
            System.out.print(current + " ");
            
            for (Edge<V> edge : adjacencyList.get(current)) {
                if (!visited.contains(edge.to)) {
                    visited.add(edge.to);
                    queue.offer(edge.to);
                }
            }
        }
        System.out.println();
    }
    
    public int getEdgeWeight(V from, V to) {
        if (!adjacencyList.containsKey(from)) {
            return -1;
        }
        
        for (Edge<V> edge : adjacencyList.get(from)) {
            if (edge.to.equals(to)) {
                return edge.weight;
            }
        }
        return -1;
    }
    
    public java.util.List<V> dijkstra(V start, V end) {
        if (!adjacencyList.containsKey(start) || !adjacencyList.containsKey(end)) {
            throw new IllegalArgumentException("Вершины должны существовать в графе");
        }
        
        Map<V, Integer> distances = new HashMap<>();
        Map<V, V> previous = new HashMap<>();
        PriorityQueue<V> queue = new PriorityQueue<>(
            Comparator.comparingInt(v -> distances.getOrDefault(v, Integer.MAX_VALUE))
        );
        
        for (V vertex : adjacencyList.keySet()) {
            distances.put(vertex, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        queue.offer(start);
        
        while (!queue.isEmpty()) {
            V current = queue.poll();
            
            if (current.equals(end)) {
                break;
            }
            
            for (Edge<V> edge : adjacencyList.get(current)) {
                int newDist = distances.get(current) + edge.weight;
                if (newDist < distances.get(edge.to)) {
                    distances.put(edge.to, newDist);
                    previous.put(edge.to, current);
                    queue.offer(edge.to);
                }
            }
        }
        
        return reconstructPath(previous, start, end);
    }
    
    private java.util.List<V> reconstructPath(Map<V, V> previous, V start, V end) {
        java.util.List<V> path = new ArrayList<>();
        if (!previous.containsKey(end) && !start.equals(end)) {
            return path;
        }
        
        for (V at = end; at != null; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        
        if (path.get(0).equals(start)) {
            return path;
        } else {
            return new ArrayList<>();
        }
    }
    
    public Set<V> getVertices() {
        return new HashSet<>(adjacencyList.keySet());
    }
    
    public boolean containsVertex(V v) {
        return adjacencyList.containsKey(v);
    }
    
    public boolean isDirected() {
        return isDirected;
    }
}

//графический интерфейс
public class GraphGUI {
    private Graph<String> graph;
    private JFrame frame;
    private GraphPanel graphPanel;
    private JTextArea outputArea;
    private JTextField vertexField;
    private JTextField fromField;
    private JTextField toField;
    private JTextField weightField;
    private JComboBox<String> graphTypeCombo;

    private final Color PINK_BACKGROUND = new Color(255, 240, 245); 
    private final Color PINK_PANEL = new Color(255, 228, 225);      
    private final Color PINK_BUTTON = new Color(255, 192, 203);     
    private final Color PINK_DARKER = new Color(219, 112, 147);     
    private final Color PINK_VERTEX = new Color(255, 182, 193);     
    private final Color PINK_EDGE = new Color(199, 21, 133);        

    public GraphGUI() {
        graph = new Graph<>(false);
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("Graph Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(PINK_BACKGROUND);
        //панель управления
        JPanel controlPanel = createControlPanel();
        frame.add(controlPanel, BorderLayout.NORTH);
        //панель графа
        graphPanel = new GraphPanel();
        frame.add(new JScrollPane(graphPanel), BorderLayout.CENTER);
        //вывод
        outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        outputArea.setBackground(PINK_PANEL);
        outputArea.setForeground(Color.DARK_GRAY);
        outputArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBackground(PINK_PANEL);
        frame.add(outputScroll, BorderLayout.SOUTH);
        frame.pack();
        frame.setSize(800, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.setBackground(PINK_PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel row1 = new JPanel();
        stylePanel(row1);
        row1.add(createStyledLabel("Тип графа:"));
        graphTypeCombo = new JComboBox<>(new String[]{"Неориентированный", "Ориентированный"});
        styleComboBox(graphTypeCombo);
        graphTypeCombo.addActionListener(e -> updateGraphType());
        row1.add(graphTypeCombo);
        row1.add(createStyledLabel("Вершина:"));
        vertexField = new JTextField(5);
        styleTextField(vertexField);
        row1.add(vertexField);
        JButton addVertexBtn = createStyledButton("Добавить вершину");
        addVertexBtn.addActionListener(e -> addVertex());
        row1.add(addVertexBtn);
        JButton removeVertexBtn = createStyledButton("Удалить вершину");
        removeVertexBtn.addActionListener(e -> removeVertex());
        row1.add(removeVertexBtn);
        JPanel row2 = new JPanel();
        stylePanel(row2);
        row2.add(createStyledLabel("Из:"));
        fromField = new JTextField(3);
        styleTextField(fromField);
        row2.add(fromField);

        row2.add(createStyledLabel("В:"));
        toField = new JTextField(3);
        styleTextField(toField);
        row2.add(toField);

        row2.add(createStyledLabel("Вес:"));
        weightField = new JTextField(3);
        styleTextField(weightField);
        weightField.setText("1");
        row2.add(weightField);

        JButton addEdgeBtn = createStyledButton("Добавить ребро");
        addEdgeBtn.addActionListener(e -> addEdge());
        row2.add(addEdgeBtn);
        JButton removeEdgeBtn = createStyledButton("Удалить ребро");
        removeEdgeBtn.addActionListener(e -> removeEdge());
        row2.add(removeEdgeBtn);
        JPanel row3 = new JPanel();
        stylePanel(row3);
        JButton dfsBtn = createStyledButton("DFS обход");
        dfsBtn.addActionListener(e -> performDFS());
        row3.add(dfsBtn);

        JButton bfsBtn = createStyledButton("BFS обход");
        bfsBtn.addActionListener(e -> performBFS());
        row3.add(bfsBtn);
        JButton adjacentBtn = createStyledButton("Смежные вершины");
        adjacentBtn.addActionListener(e -> getAdjacent());
        row3.add(adjacentBtn);
        JButton dijkstraBtn = createStyledButton("Дейкстра");
        dijkstraBtn.addActionListener(e -> performDijkstra());
        row3.add(dijkstraBtn);
        JButton clearBtn = createStyledButton("Очистить");
        clearBtn.addActionListener(e -> clearGraph());
        row3.add(clearBtn);
        panel.add(row1);
        panel.add(row2);
        panel.add(row3);
        return panel;
    }
    private void stylePanel(JPanel panel) {
        panel.setBackground(PINK_PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(PINK_DARKER);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        return label;
    }
    private void styleTextField(JTextField field) {
        field.setBackground(Color.WHITE);
        field.setForeground(Color.DARK_GRAY);
        field.setFont(new Font("SansSerif", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PINK_DARKER, 1),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
    }
    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.DARK_GRAY);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PINK_BUTTON);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PINK_DARKER, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        button.setFocusPainted(false);
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(PINK_DARKER);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(PINK_BUTTON);
            }
        });
        
        return button;
    }

    private void updateGraphType() {
        boolean directed = graphTypeCombo.getSelectedIndex() == 1;
        graph = new Graph<>(directed);
        log("Создан " + (directed ? "ориентированный" : "неориентированный") + " граф");
        graphPanel.repaint();
    }

    private void addVertex() {
        try {
            String vertex = vertexField.getText().trim();
            if (vertex.isEmpty()) {
                showError("Введите название вершины");
                return;
            }
            graph.addVertex(vertex);
            log("Добавлена вершина: " + vertex);
            vertexField.setText("");
            graphPanel.repaint();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void removeVertex() {
        try {
            String vertex = vertexField.getText().trim();
            if (vertex.isEmpty()) {
                showError("Введите название вершины");
                return;
            }
            graph.removeVertex(vertex);
            log("Удалена вершина: " + vertex);
            vertexField.setText("");
            graphPanel.repaint();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void addEdge() {
        try {
            String from = fromField.getText().trim();
            String to = toField.getText().trim();
            String weightText = weightField.getText().trim();
            
            if (from.isEmpty() || to.isEmpty()) {
                showError("Введите вершины 'Из' и 'В'");
                return;
            }
            
            int weight = 1;
            if (!weightText.isEmpty()) {
                weight = Integer.parseInt(weightText);
            }
            
            graph.addEdge(from, to, weight);
            log("Добавлено ребро: " + from + " → " + to + " (вес: " + weight + ")");
            fromField.setText("");
            toField.setText("");
            graphPanel.repaint();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void removeEdge() {
        try {
            String from = fromField.getText().trim();
            String to = toField.getText().trim();
            
            if (from.isEmpty() || to.isEmpty()) {
                showError("Введите вершины 'Из' и 'В'");
                return;
            }
            
            graph.removeEdge(from, to);
            log("Удалено ребро: " + from + " → " + to);
            fromField.setText("");
            toField.setText("");
            graphPanel.repaint();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void performDFS() {
        try {
            String start = JOptionPane.showInputDialog(frame, "Введите начальную вершину для DFS:");
            if (start != null && !start.trim().isEmpty()) {
                log(" DFS обход ");
                graph.dfs(start.trim());
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void performBFS() {
        try {
            String start = JOptionPane.showInputDialog(frame, "Введите начальную вершину для BFS:");
            if (start != null && !start.trim().isEmpty()) {
                log("BFS обход ");
                graph.bfs(start.trim());
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void getAdjacent() {
        try {
            String vertex = JOptionPane.showInputDialog(frame, "Введите вершину для получения смежных:");
            if (vertex != null && !vertex.trim().isEmpty()) {
                java.util.List<String> adjacent = graph.getAdjacent(vertex.trim());
                log(" Смежные с " + vertex + ": " + adjacent);
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void performDijkstra() {
        try {
            String from = JOptionPane.showInputDialog(frame, "Введите начальную вершину:");
            String to = JOptionPane.showInputDialog(frame, "Введите конечную вершину:");
            
            if (from != null && to != null && !from.trim().isEmpty() && !to.trim().isEmpty()) {
                java.util.List<String> path = graph.dijkstra(from.trim(), to.trim());
                if (path.isEmpty()) {
                    log("Путь от " + from + " до " + to + " не существует");
                } else {
                    log("Кратчайший путь от " + from + " до " + to + ": " + path);
                }
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void clearGraph() {
        graph = new Graph<>(graphTypeCombo.getSelectedIndex() == 1);
        log(" Граф очищен");
        graphPanel.repaint();
    }

    private void log(String message) {
        outputArea.append(" " + message + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private class GraphPanel extends JPanel {
        private static final int VERTEX_RADIUS = 20;
        private static final int PANEL_SIZE = 600;
        
        public GraphPanel() {
            setPreferredSize(new Dimension(PANEL_SIZE, PANEL_SIZE));
            setBackground(PINK_BACKGROUND);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            drawGraph(g2d);
        }
        
        private void drawGraph(Graphics2D g2d) {
            Set<String> vertices = graph.getVertices();
            if (vertices.isEmpty()) return;
            
            Map<String, Point> vertexPositions = calculateVertexPositions(vertices);
            drawEdges(g2d, vertexPositions);
            drawVertices(g2d, vertexPositions);
        }
        
        private Map<String, Point> calculateVertexPositions(Set<String> vertices) {
            Map<String, Point> positions = new HashMap<>();
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = Math.min(centerX, centerY) - 50;
            
            double angleStep = 2 * Math.PI / vertices.size();
            int i = 0;
            
            for (String vertex : vertices) {
                double angle = i * angleStep;
                int x = centerX + (int)(radius * Math.cos(angle));
                int y = centerY + (int)(radius * Math.sin(angle));
                positions.put(vertex, new Point(x, y));
                i++;
            }
            
            return positions;
        }
        
        private void drawEdges(Graphics2D g2d, Map<String, Point> positions) {
            g2d.setColor(PINK_EDGE);
            g2d.setStroke(new BasicStroke(2));
            
            for (String from : positions.keySet()) {
                Point fromPoint = positions.get(from);
                
                try {
                    java.util.List<String> adjacent = graph.getAdjacent(from);
                    for (String to : adjacent) {
                        if (positions.containsKey(to)) {
                            Point toPoint = positions.get(to);
                            
                            if (graph.isDirected()) {
                                drawArrow(g2d, fromPoint, toPoint);
                            } else {
                                g2d.drawLine(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y);
                            }
                            
                            int weight = graph.getEdgeWeight(from, to);
                            if (weight != 1) {
                                int labelX = (fromPoint.x + toPoint.x) / 2;
                                int labelY = (fromPoint.y + toPoint.y) / 2;
                                g2d.setColor(PINK_DARKER);
                                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                                g2d.drawString(String.valueOf(weight), labelX, labelY);
                                g2d.setColor(PINK_EDGE);
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        
        private void drawArrow(Graphics2D g2d, Point from, Point to) {
            g2d.drawLine(from.x, from.y, to.x, to.y);
            
            double angle = Math.atan2(to.y - from.y, to.x - from.x);
            int arrowSize = 10;
            int endX = to.x - (int)(VERTEX_RADIUS * Math.cos(angle));
            int endY = to.y - (int)(VERTEX_RADIUS * Math.sin(angle));
            
            Polygon arrowHead = new Polygon();
            arrowHead.addPoint(endX, endY);
            arrowHead.addPoint(endX - arrowSize, endY - arrowSize / 2);
            arrowHead.addPoint(endX - arrowSize, endY + arrowSize / 2);
            
            g2d.fill(arrowHead);
        }
        
        private void drawVertices(Graphics2D g2d, Map<String, Point> positions) {
            g2d.setColor(PINK_VERTEX);
            g2d.setStroke(new BasicStroke(2));
            
            for (Map.Entry<String, Point> entry : positions.entrySet()) {
                Point point = entry.getValue();
                String vertex = entry.getKey();
                g2d.fillOval(point.x - VERTEX_RADIUS, point.y - VERTEX_RADIUS, 
                             VERTEX_RADIUS * 2, VERTEX_RADIUS * 2);
                g2d.setColor(PINK_DARKER);
                g2d.drawOval(point.x - VERTEX_RADIUS, point.y - VERTEX_RADIUS, 
                            VERTEX_RADIUS * 2, VERTEX_RADIUS * 2);
                g2d.setColor(PINK_DARKER);
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(vertex);
                int textHeight = fm.getHeight();
                g2d.drawString(vertex, point.x - textWidth / 2, point.y + textHeight / 4);
                
                g2d.setColor(PINK_VERTEX);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GraphGUI();
        });
    }
}