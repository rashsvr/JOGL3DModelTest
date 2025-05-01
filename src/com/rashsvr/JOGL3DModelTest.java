package com.rashsvr;

/**
 *
 * @author rmdrsvr
 */

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JOGL3DModelTest extends JFrame implements GLEventListener {
    private GLCanvas glCanvas;
    private float scaleWidth = 1.0f, scaleHeight = 1.0f, scaleDepth = 1.0f;
    private Color objectColor = Color.WHITE;
    private float rotateX = 0.0f, rotateY = 0.0f;
    private float autoRotateY = 0.0f;
    private float zoom = -5.0f;
    private Point lastMousePoint = null;
    private ObjModel sofaModel;
    private boolean useColorTint = false;

    public JOGL3DModelTest() {
        super("JOGL 3D Model Test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Set dark theme
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("control", new Color(45, 45, 45));
            UIManager.put("nimbusBase", new Color(51, 51, 51));
            UIManager.put("nimbusFocus", new Color(115, 164, 209));
            UIManager.put("text", Color.WHITE);
        } catch (Exception e) {
            System.err.println("Failed to set Nimbus look and feel: " + e.getMessage());
        }
        getContentPane().setBackground(new Color(30, 30, 30));

        // Initialize JOGL
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        glCanvas = new GLCanvas(capabilities);
        glCanvas.addGLEventListener(this);
        FPSAnimator animator = new FPSAnimator(glCanvas, 60);
        animator.start();

        // Load OBJ model from classpath
        sofaModel = new ObjModel();
        String objPath = "/com/rashsvr/model/obj/sofa/sofa.obj";
        try {
            URL objUrl = getClass().getResource(objPath);
            if (objUrl == null) {
                throw new IOException("Resource not found in classpath: " + objPath);
            }
            System.out.println("Loading sofa.obj from: " + objUrl);
            sofaModel.load(objUrl);
            System.out.println("Loaded sofa.obj: " + sofaModel.getVertexCount() + " vertices, " + sofaModel.getFaceCount() + " faces");
            if (sofaModel.getVertexCount() == 0 || sofaModel.getFaceCount() == 0) {
                throw new IOException("No vertices or faces loaded from sofa.obj");
            }
        } catch (IOException e) {
            System.out.println("Error loading sofa.obj: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to load sofa.obj: " + e.getMessage() + "\nEnsure sofa.obj is in resources/com/rashsvr/model/obj/sofa/.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Mouse controls for rotation
        glCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePoint = e.getPoint();
            }
        });
        glCanvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePoint != null) {
                    int dx = e.getX() - lastMousePoint.x;
                    int dy = e.getY() - lastMousePoint.y;
                    rotateY += dx * 0.5f;
                    rotateX += dy * 0.5f;
                    lastMousePoint = e.getPoint();
                    glCanvas.repaint();
                }
            }
        });

        // Mouse wheel for zooming
        glCanvas.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                zoom += notches * 0.5f;
                zoom = Math.max(-10.0f, Math.min(-1.0f, zoom));
                glCanvas.repaint();
            }
        });

        // Control Panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(new Color(45, 45, 45));
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Custom rounded border for buttons
        Border line = new LineBorder(Color.GRAY, 1, true);
        Border margin = new EmptyBorder(5, 15, 5, 15);
        Border compound = new CompoundBorder(line, margin);

        // Color Button
        JButton colorButton = new JButton("Change Color");
        colorButton.setBackground(new Color(60, 60, 60));
        colorButton.setForeground(Color.WHITE);
        colorButton.setBorder(compound);
        colorButton.setFocusPainted(false);
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Model Color", objectColor);
            if (newColor != null) {
                objectColor = newColor;
                useColorTint = true;
                glCanvas.repaint();
            }
        });
        colorButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                colorButton.setBackground(new Color(80, 80, 80));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                colorButton.setBackground(new Color(60, 60, 60));
            }
        });

        // Reset Tint Button
        JButton resetTintButton = new JButton("Reset Tint");
        resetTintButton.setBackground(new Color(60, 60, 60));
        resetTintButton.setForeground(Color.WHITE);
        resetTintButton.setBorder(compound);
        resetTintButton.setFocusPainted(false);
        resetTintButton.addActionListener(e -> {
            objectColor = Color.WHITE;
            useColorTint = false;
            glCanvas.repaint();
        });
        resetTintButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                resetTintButton.setBackground(new Color(80, 80, 80));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                resetTintButton.setBackground(new Color(60, 60, 60));
            }
        });

        // Zoom Buttons
        JButton zoomInButton = new JButton("Zoom In");
        zoomInButton.setBackground(new Color(60, 60, 60));
        zoomInButton.setForeground(Color.WHITE);
        zoomInButton.setBorder(compound);
        zoomInButton.setFocusPainted(false);
        zoomInButton.addActionListener(e -> {
            zoom += 0.5f;
            zoom = Math.max(-10.0f, Math.min(-1.0f, zoom));
            glCanvas.repaint();
        });
        zoomInButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                zoomInButton.setBackground(new Color(80, 80, 80));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                zoomInButton.setBackground(new Color(60, 60, 60));
            }
        });

        JButton zoomOutButton = new JButton("Zoom Out");
        zoomOutButton.setBackground(new Color(60, 60, 60));
        zoomOutButton.setForeground(Color.WHITE);
        zoomOutButton.setBorder(compound);
        zoomOutButton.setFocusPainted(false);
        zoomOutButton.addActionListener(e -> {
            zoom -= 0.5f;
            zoom = Math.max(-10.0f, Math.min(-1.0f, zoom));
            glCanvas.repaint();
        });
        zoomOutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                zoomOutButton.setBackground(new Color(80, 80, 80));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                zoomOutButton.setBackground(new Color(60, 60, 60));
            }
        });

        // Scale Sliders
        JLabel widthLabel = new JLabel("Width Scale (0.5-2.0):");
        widthLabel.setForeground(Color.WHITE);
        JSlider widthSlider = new JSlider(JSlider.HORIZONTAL, 50, 200, 100);
        widthSlider.setBackground(new Color(45, 45, 45));
        widthSlider.setForeground(Color.WHITE);
        widthSlider.setMajorTickSpacing(50);
        widthSlider.setPaintTicks(true);
        widthSlider.setPaintLabels(true);
        widthSlider.addChangeListener(e -> {
            scaleWidth = widthSlider.getValue() / 100.0f;
            glCanvas.repaint();
        });

        JLabel heightLabel = new JLabel("Height Scale (0.5-2.0):");
        heightLabel.setForeground(Color.WHITE);
        JSlider heightSlider = new JSlider(JSlider.HORIZONTAL, 50, 200, 100);
        heightSlider.setBackground(new Color(45, 45, 45));
        heightSlider.setForeground(Color.WHITE);
        heightSlider.setMajorTickSpacing(50);
        heightSlider.setPaintTicks(true);
        heightSlider.setPaintLabels(true);
        heightSlider.addChangeListener(e -> {
            scaleHeight = heightSlider.getValue() / 100.0f;
            glCanvas.repaint();
        });

        JLabel depthLabel = new JLabel("Depth Scale (0.5-2.0):");
        depthLabel.setForeground(Color.WHITE);
        JSlider depthSlider = new JSlider(JSlider.HORIZONTAL, 50, 200, 100);
        depthSlider.setBackground(new Color(45, 45, 45));
        depthSlider.setForeground(Color.WHITE);
        depthSlider.setMajorTickSpacing(50);
        depthSlider.setPaintTicks(true);
        depthSlider.setPaintLabels(true);
        depthSlider.addChangeListener(e -> {
            scaleDepth = depthSlider.getValue() / 100.0f;
            glCanvas.repaint();
        });

        // GridBagConstraints for layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; controlPanel.add(colorButton, gbc);
        gbc.gridx = 1; controlPanel.add(resetTintButton, gbc);
        gbc.gridx = 0; gbc.gridy = 1; controlPanel.add(zoomInButton, gbc);
        gbc.gridx = 1; controlPanel.add(zoomOutButton, gbc);
        gbc.gridx = 0; gbc.gridy = 2; controlPanel.add(widthLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; controlPanel.add(widthSlider, gbc);
        gbc.gridx = 0; gbc.gridy = 3; controlPanel.add(heightLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 3; controlPanel.add(heightSlider, gbc);
        gbc.gridx = 0; gbc.gridy = 4; controlPanel.add(depthLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 4; controlPanel.add(depthSlider, gbc);

        // 3D Panel
        JPanel glPanel = new JPanel(new BorderLayout());
        glPanel.setBackground(new Color(30, 30, 30));
        glPanel.add(glCanvas, BorderLayout.CENTER);
        glPanel.setBorder(BorderFactory.createTitledBorder(null, "3D Model View", 0, 0, null, Color.WHITE));

        add(controlPanel, BorderLayout.NORTH);
        add(glPanel, BorderLayout.CENTER);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        float[] lightPos = {0.0f, 0.0f, 1.0f, 0.0f};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glEnable(GL2.GL_TEXTURE_2D);
        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustum(-1.0, 1.0, -1.0, 1.0, 1.5, 20.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, zoom);

        autoRotateY += 0.5f;
        gl.glRotatef(rotateX, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(rotateY + autoRotateY, 0.0f, 1.0f, 0.0f);

        gl.glScalef(scaleWidth, scaleHeight, scaleDepth);

        float[] materialDiffuse = {objectColor.getRed() / 255.0f, objectColor.getGreen() / 255.0f, objectColor.getBlue() / 255.0f, 1.0f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, materialDiffuse, 0);

        sofaModel.bindTextures(gl);

        sofaModel.render(gl);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    static class ObjModel {
        private List<float[]> vertices = new ArrayList<>();
        private List<float[]> normals = new ArrayList<>();
        private List<float[]> texCoords = new ArrayList<>();
        private List<int[]> faces = new ArrayList<>();
        private List<int[]> normalIndices = new ArrayList<>();
        private List<int[]> texIndices = new ArrayList<>();
        private float scaleFactor = 1.0f;
        private Texture diffuseTexture;
        private Texture normalTexture;
        private String baseDir;

        public void load(URL objUrl) throws IOException {
            vertices.clear();
            normals.clear();
            texCoords.clear();
            faces.clear();
            normalIndices.clear();
            texIndices.clear();
            float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
            float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
            float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

            String objPath = objUrl.getPath();
            baseDir = new File(objPath).getParent();
            String mtlFile = null;

            try (InputStream is = objUrl.openStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("mtllib ")) {
                        mtlFile = line.split("\\s+")[1];
                        System.out.println("Found MTL reference: " + mtlFile);
                    } else if (line.startsWith("v ")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 4) {
                            float x = Float.parseFloat(parts[1]);
                            float y = Float.parseFloat(parts[2]);
                            float z = Float.parseFloat(parts[3]);
                            vertices.add(new float[]{x, y, z});
                            minX = Math.min(minX, x);
                            maxX = Math.max(maxX, x);
                            minY = Math.min(minY, y);
                            maxY = Math.max(maxY, y);
                            minZ = Math.min(minZ, z);
                            maxZ = Math.max(maxZ, z);
                        }
                    } else if (line.startsWith("vn ")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 4) {
                            float nx = Float.parseFloat(parts[1]);
                            float ny = Float.parseFloat(parts[2]);
                            float nz = Float.parseFloat(parts[3]);
                            normals.add(new float[]{nx, ny, nz});
                        }
                    } else if (line.startsWith("vt ")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 3) {
                            float u = Float.parseFloat(parts[1]);
                            float v = Float.parseFloat(parts[2]);
                            texCoords.add(new float[]{u, v});
                        }
                    } else if (line.startsWith("f ")) {
                        String[] parts = line.split("\\s+");
                        List<Integer> vertexIndices = new ArrayList<>();
                        List<Integer> normalIndicesList = new ArrayList<>();
                        List<Integer> texIndicesList = new ArrayList<>();
                        for (int i = 1; i < parts.length; i++) {
                            if (!parts[i].isEmpty()) {
                                String[] indices = parts[i].split("/");
                                vertexIndices.add(Integer.parseInt(indices[0]) - 1);
                                if (indices.length >= 2 && !indices[1].isEmpty()) {
                                    texIndicesList.add(Integer.parseInt(indices[1]) - 1);
                                } else {
                                    texIndicesList.add(-1);
                                }
                                if (indices.length >= 3 && !indices[2].isEmpty()) {
                                    normalIndicesList.add(Integer.parseInt(indices[2]) - 1);
                                } else {
                                    normalIndicesList.add(-1);
                                }
                            }
                        }
                        if (vertexIndices.size() >= 3) {
                            for (int i = 1; i < vertexIndices.size() - 1; i++) {
                                faces.add(new int[]{vertexIndices.get(0), vertexIndices.get(i), vertexIndices.get(i + 1)});
                                normalIndices.add(new int[]{normalIndicesList.get(0), normalIndicesList.get(i), normalIndicesList.get(i + 1)});
                                texIndices.add(new int[]{texIndicesList.get(0), texIndicesList.get(i), texIndicesList.get(i + 1)});
                            }
                        }
                    }
                }
            }
            System.out.println("Loaded " + texCoords.size() + " texture coordinates");

            float maxDim = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
            if (maxDim > 0) {
                scaleFactor = 2.0f / maxDim;
                for (float[] vertex : vertices) {
                    vertex[0] = (vertex[0] - (minX + maxX) / 2) * scaleFactor;
                    vertex[1] = (vertex[1] - (minY + maxY) / 2) * scaleFactor;
                    vertex[2] = (vertex[2] - (minZ + maxZ) / 2) * scaleFactor;
                }
            }

            if (mtlFile != null) {
                loadMaterial(mtlFile);
            } else {
                System.out.println("No MTL file specified in sofa.obj");
            }
        }

        private void loadMaterial(String mtlFileName) throws IOException {
            String mtlPath = "/com/rashsvr/model/obj/sofa/" + mtlFileName;
            String diffuseTextureFile = null;
            URL mtlUrl = getClass().getResource(mtlPath);
            if (mtlUrl == null) {
                System.out.println("MTL file not found in classpath: " + mtlPath);
                return;
            }
            System.out.println("Loading MTL file from: " + mtlUrl);
            try (InputStream is = mtlUrl.openStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("map_Kd ")) {
                        diffuseTextureFile = line.split("\\s+")[1];
                        System.out.println("Found diffuse texture in MTL: " + diffuseTextureFile);
                        break;
                    }
                }
            }
            if (diffuseTextureFile != null) {
                String texPath = "/com/rashsvr/model/obj/sofa/default_texture/" + diffuseTextureFile;
                URL texUrl = getClass().getResource(texPath);
                if (texUrl == null) {
                    System.out.println("Diffuse texture file not found in classpath: " + texPath);
                } else {
                    System.out.println("Loading diffuse texture from: " + texUrl);
                    File texFile = new File(texUrl.getPath());
                    if (texFile.exists()) {
                        diffuseTexture = TextureIO.newTexture(texFile, true);
                        diffuseTexture.setTexParameteri(null, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
                        diffuseTexture.setTexParameteri(null, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
                        System.out.println("Successfully loaded diffuse texture");
                    } else {
                        System.out.println("Diffuse texture file not found on disk: " + texFile.getPath());
                    }
                }
            } else {
                System.out.println("No map_Kd found in MTL file");
            }
            String normalTexPath = "/com/rashsvr/model/obj/sofa/default_texture/2 seat sofa_S01 M02_Normal.png";
            URL normalTexUrl = getClass().getResource(normalTexPath);
            if (normalTexUrl == null) {
                System.out.println("Normal map not found in classpath: " + normalTexPath);
            } else {
                System.out.println("Loading normal map from: " + normalTexUrl);
                File normalTexFile = new File(normalTexUrl.getPath());
                if (normalTexFile.exists()) {
                    normalTexture = TextureIO.newTexture(normalTexFile, true);
                    normalTexture.setTexParameteri(null, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
                    normalTexture.setTexParameteri(null, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
                    System.out.println("Successfully loaded normal map");
                } else {
                    System.out.println("Normal map not found on disk: " + normalTexFile.getPath());
                }
            }
        }

        public void bindTextures(GL2 gl) {
            if (diffuseTexture != null) {
                gl.glActiveTexture(GL2.GL_TEXTURE0);
                diffuseTexture.enable(gl);
                diffuseTexture.bind(gl);
                System.out.println("Diffuse texture bound to GL_TEXTURE0");
            } else {
                System.out.println("No diffuse texture to bind");
            }
            if (normalTexture != null) {
                gl.glActiveTexture(GL2.GL_TEXTURE1);
                normalTexture.enable(gl);
                normalTexture.bind(gl);
                System.out.println("Normal texture bound to GL_TEXTURE1");
            }
        }

        public void render(GL2 gl) {
            gl.glBegin(GL2.GL_TRIANGLES);
            for (int i = 0; i < faces.size(); i++) {
                int[] face = faces.get(i);
                int[] normal = normalIndices.get(i);
                int[] tex = texIndices.get(i);
                for (int j = 0; j < 3; j++) {
                    if (normal[j] >= 0 && normal[j] < normals.size()) {
                        float[] n = normals.get(normal[j]);
                        gl.glNormal3f(n[0], n[1], n[2]);
                    }
                    if (tex[j] >= 0 && tex[j] < texCoords.size()) {
                        float[] t = texCoords.get(tex[j]);
                        gl.glMultiTexCoord2f(GL2.GL_TEXTURE0, t[0], t[1]);
                    }
                    if (face[j] >= 0 && face[j] < vertices.size()) {
                        float[] vertex = vertices.get(face[j]);
                        gl.glVertex3f(vertex[0], vertex[1], vertex[2]);
                    }
                }
            }
            gl.glEnd();
        }

        public int getVertexCount() {
            return vertices.size();
        }

        public int getFaceCount() {
            return faces.size();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JOGL3DModelTest().setVisible(true));
    }
}