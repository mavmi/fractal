package fractal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

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
        return (((r << 8) + g) << 8) + b;
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
    public static Complex sub(Complex left, Complex right){
        return new Complex(left.getRe() - right.getRe(), left.getIm() - right.getIm());
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

    private double mouse_x;
    private double mouse_y;

    private final RGB black;
    private final RGB white;
    private final int width = 800;
    private final FRACTAL fractal;
    private final BufferedImage image;

    public Frame(String name) throws Exception {
        if (name.equals("Julia")){
            fractal = FRACTAL.JULIA;
            mouse_x = juliaToCartesian(-1);
            mouse_y = juliaToCartesian(0);
        } else if (name.equals("Mandelbrot")) {
            fractal = FRACTAL.MANDELBROT;
            mouse_x = juliaToCartesian(0);
            mouse_y = juliaToCartesian(0);
        } else {
            throw new Exception("Bad fractal name");
        }

        image = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
        black = new RGB(0, 0, 0);
        white = new RGB(255, 255, 255);

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

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_ESCAPE){
                    System.exit(0);
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                mouse_x = e.getX();
                mouse_y = e.getY();
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
        final Complex C = new Complex(cartesianToJulia(mouse_x),
                                        cartesianToJulia(mouse_y));

        for (int y = 0; y < width; y++){
        for (int x = 0; x < width; x++){
            Z.setRe(cartesianToJulia(x));
            Z.setIm(cartesianToJulia(y));

            int iters = 0;
            while (iters < max_iters) {
                Z = Complex.sum(Complex.pow_2(Z), C);
                if (Z.getModulus() > 2.0){
                    break;
                }
                iters++;
            }

            int rgb;
            if (iters == max_iters){
                rgb = white.toInt();
            } else {
                rgb = black.toInt();
            }
            image.setRGB(x, y, rgb);
        }
        }
        repaint();
    }

    private void mandelbrot(){
        final int max_iters = 50;

        Complex Z = new Complex();
        Complex C = new Complex(cartesianToJulia(mouse_x),
                                  cartesianToJulia(mouse_y));

        for (int y = 0; y < width; y++){
        for (int x = 0; x < width; x++){
            Z.setRe(cartesianToJulia(x));
            Z.setIm(cartesianToJulia(y));

            int iters = 0;
            while (iters < max_iters) {
                C = Complex.sum(Complex.pow_2(C), Z);
                if (C.getModulus() > 2.0){
                    break;
                }
                iters++;
            }

            int rgb;
            if (iters == max_iters){
                rgb = white.toInt();
            } else {
                rgb = black.toInt();
            }
            image.setRGB(x, y, rgb);
        }
        }
        repaint();
    }

    private double cartesianToJulia(double a){
        return -2.0 + 4.0 / width * a;
    }
    private double juliaToCartesian(double a){
        return (a + 2.0) * width / 4.0;
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
