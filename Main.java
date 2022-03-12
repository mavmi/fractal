package fractal;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.GregorianCalendar;

class RGB{
    private final int r;
    private final int g;
    private final int b;

    public RGB(int r, int g, int b){
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int toInt(){
        return (r << 16) + (g << 8) + b;
    }
}

class Complex{
    private double im;
    private double re;

    public Complex(){
        re = 0;
        im = 0;
    }
    public Complex(double re, double im){
        this.re = re;
        this.im = im;
    }

    public void setRe(double re){
        this.re = re;
    }
    public void setIm(double im){
        this.im = im;
    }

    public double getRe(){
        return re;
    }
    public double getIm(){
        return im;
    }
    public double getModulus(){
        return Math.sqrt(Math.pow(re, 2) + Math.pow(im, 2));
    }

    public static Complex sum(Complex left, Complex right){
        return new Complex(left.getRe() + right.getRe(), left.getIm() + right.getIm());
    }
    public static Complex multi(Complex left, Complex right){
        return new Complex(left.getRe() * right.getRe() - left.getIm() * right.getIm(),
                left.getRe() * right.getIm() + left.getIm() * right.getRe());
    }
    public static Complex pow_2(Complex c){
        return Complex.multi(c, c);
    }
}

class Frame extends JFrame {
    private enum FRACTAL{
        JULIA,
        MANDELBROT
    }

    private double C_x;
    private double C_y;
    private double zoom;
    private double zoom_x;
    private double zoom_y;
    private double delta_x;
    private double delta_y;
    private FRACTAL fractal;

    private RGB black;
    private RGB color_1;
    private RGB color_2;
    private RGB color_3;
    private RGB color_4;
    private boolean CTRL;
    private BufferedImage image;

    private final int width = 800;

    public Frame(String name) throws Exception {
        initVars();
        initFractal(name);
        initColors();
        setUpWindow();
        setUpEvents();
    }
    private void initVars(){
        zoom = 1.0;
        zoom_x = 0.0;
        zoom_y = 0.0;
        delta_x = 0.0;
        delta_y = 0.0;
        CTRL = false;
        image = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
    }
    private void initFractal(String name) throws Exception {
        if (name.equals("Julia")){
            fractal = FRACTAL.JULIA;
            C_x = juliaToCartesian(-1);
            C_y = juliaToCartesian(0);
        } else if (name.equals("Mandelbrot")) {
            fractal = FRACTAL.MANDELBROT;
            C_x = juliaToCartesian(0);
            C_y = juliaToCartesian(0);
        } else {
            throw new Exception("Bad fractal name");
        }
    }
    private void initColors(){
        black = new RGB(0, 0, 0);
        color_1 = new RGB(0, 15, 0);
        color_2 = new RGB(0, 128, 0);
        color_3 = new RGB(154, 215, 50);
        color_4 = new RGB(173, 255, 47);
    }
    private void setUpWindow(){
        JPanel panel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(image, 0, 0, null);
            }
        };
        panel.setBounds(0, 0, width, width);

        setLayout(null);
        setVisible(true);
        setSize(width, width);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(panel);
    }
    private void setUpEvents(){
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                final double shift_delta = ((double)width / 30) / zoom;
                final int keyCode = e.getKeyCode();

                if (keyCode == KeyEvent.VK_ESCAPE){
                    System.exit(0);
                } else if (keyCode == KeyEvent.VK_S) {
                    int a = JOptionPane.showConfirmDialog(null, "Save current image?", "", JOptionPane.YES_NO_OPTION);
                    if (a == JOptionPane.YES_OPTION){
                        makeScreenshot();
                    }
                    return;
                } else if (keyCode == KeyEvent.VK_CONTROL){
                    CTRL = true;
                    return;
                } else if (keyCode == KeyEvent.VK_UP){
                    delta_y -= shift_delta;
                } else if (keyCode == KeyEvent.VK_DOWN){
                    delta_y += shift_delta;
                } else if (keyCode == KeyEvent.VK_LEFT){
                    delta_x -= shift_delta;
                } else if (keyCode == KeyEvent.VK_RIGHT){
                    delta_x += shift_delta;
                }
                draw();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                final int keyCode = e.getKeyCode();

                if (keyCode == KeyEvent.VK_CONTROL){
                    CTRL = false;
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                final int button = e.getButton();

                if (button == MouseEvent.BUTTON1) {
                    if (fractal == FRACTAL.MANDELBROT) {
                        return;
                    }
                    C_x = e.getX();
                    C_y = e.getY();
                    draw();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!CTRL || fractal == FRACTAL.MANDELBROT){
                    return;
                }

                C_x = e.getX();
                C_y = e.getY();
                draw();
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                final double zoom_delta = 2;
                final int rotation = e.getWheelRotation();

                zoom_x = e.getX();
                zoom_y = e.getY();

                if (rotation < 0){
                    zoom *= zoom_delta;
                } else {
                    zoom /= zoom_delta;
                }

                draw();
            }
        });
    }

    public void draw(){
        if (fractal == FRACTAL.JULIA){
            julia();
        } else if (fractal == FRACTAL.MANDELBROT) {
            mandelbrot();
        }
    }

    private void julia(){
        final int max_iters = 50;

        Complex Z = new Complex();
        final Complex C = new Complex(cartesianToJulia(C_x),
                                        cartesianToJulia(C_y));

        for (int y = 0; y < width; y++){
            for (int x = 0; x < width; x++){
                Z.setRe(cartesianToJulia(x + delta_x * zoom) / zoom);
                Z.setIm(cartesianToJulia(y + delta_y * zoom) / zoom);

                int iters = 0;
                while (iters < max_iters) {
                    Z = Complex.sum(Complex.pow_2(Z), C);
                    if (Z.getModulus() > 2.0){
                        break;
                    }
                    iters++;
                }

                image.setRGB(x, y, itersToColor(iters, max_iters));
            }
        }
        repaint();
    }
    private void mandelbrot(){
        final int max_iters = 50;

        Complex Z = new Complex();
        final Complex C = new Complex();

        for (int y = 0; y < width; y++){
            for (int x = 0; x < width; x++){
                Z.setRe(0);
                Z.setIm(0);

                C.setRe(cartesianToJulia(x));
                C.setIm(cartesianToJulia(y));

                int iters = 0;
                while (iters < max_iters) {
                    Z = Complex.sum(Complex.pow_2(Z), C);
                    if (Z.getModulus() > 2.0){
                        break;
                    }
                    iters++;
                }

                image.setRGB(x, y, itersToColor(iters, max_iters));
            }
        }
        repaint();
    }
    private int itersToColor(int iters, int max_iters){
        if (iters == max_iters){
            return black.toInt();
        } else if (iters < max_iters / 4) {
            return color_1.toInt();
        } else if (iters < max_iters * 2 / 4) {
            return color_2.toInt();
        } else if (iters < max_iters * 3 / 4) {
            return color_3.toInt();
        } else {
            return color_4.toInt();
        }
    }

    private double cartesianToJulia(double a){
        return -2.0 + 4.0 / width * a;
    }
    private double juliaToCartesian(double a){
        return (a + 2.0) * width / 4.0;
    }

    private void makeScreenshot(){
        final String extension = "jpg";
        final String path = System.getProperty("user.home") + "/Desktop/" + getDate() + "." + extension;

        try {
            ImageIO.write(image, extension, new File(path));
            JOptionPane.showMessageDialog(null, "Done!",
                                        null, JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e){
            JOptionPane.showMessageDialog(null, e.getMessage(),
                                        "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }
    private String getDate(){
        GregorianCalendar calendar = new GregorianCalendar();
        StringBuilder builder = new StringBuilder();

        builder.append(parseDateInt(calendar.get(GregorianCalendar.DAY_OF_MONTH)));
        builder.append('.');
        builder.append(parseDateInt(calendar.get(GregorianCalendar.MONTH) + 1));
        builder.append('.');
        builder.append(parseDateInt(calendar.get(GregorianCalendar.YEAR)));
        builder.append('-');
        builder.append(parseDateInt(calendar.get(GregorianCalendar.HOUR_OF_DAY)));
        builder.append('.');
        builder.append(parseDateInt(calendar.get(GregorianCalendar.MINUTE)));
        builder.append('.');
        builder.append(parseDateInt(calendar.get(GregorianCalendar.SECOND)));
        return builder.toString();
    }
    private String parseDateInt(int val_int){
        String val_str = String.valueOf(val_int);

        if (val_int >= 10){
            return val_str;
        }
        return "0" + val_str;
    }

    private double modulus(double a){
        return (a < 0.0) ? a * -1.0 : a;
    }

}

public class Main {
    public static void main(String[] args) {
        if (args.length != 1){
            System.out.println("Bad arguments!");
            printAvailableArgs();
            System.exit(1);
        }

        try {
            Frame frame = new Frame(args[0]);
            frame.draw();
        } catch (Exception e){
            System.out.println(e.getMessage());
            printAvailableArgs();
        }
    }

    private static void printAvailableArgs(){
        System.out.println("Available fractals: Julia, Mandelbrot");
    }
}