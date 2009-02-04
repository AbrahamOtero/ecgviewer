package visualizador.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import es.usc.gsi.conversorDatosMIT.FrameConversorMIT;
import net.javahispano.jsignalwb.jsignalmonitor.ChannelProperties;
import net.javahispano.jsignalwb.jsignalmonitor.JSignalMonitor;
import visualizador.*;

/**
 * Esta es la ventana principal de la aplicaci�n; el cascar�n gr�fico. En estos
 * momentos, contiene la l�gica gr�fica, a�adir y quitar canales de
 * JSignalWorkbench.
 * @modificar
 */
public class MainWindow extends JFrame {
    private JSignalMonitor jSignalMonitor;
    private JSMDataSource jSMDataSource;
    private Date d = new Date();
    private JPanel jPanel1 = new JPanel();
    private JButton botonECG = new JButton();
    private JButton botonHR = new JButton();
    private JSlider jSlider1 = new JSlider();
    public MainWindow() {
        jSMDataSource = JSMDataSource.getJSWBManagerInstance();
        jSignalMonitor = new JSignalMonitor(jSMDataSource);
        addECG();
        this.add(jSignalMonitor.getJSignalMonitorPanel());
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }



        jSlider1.setValue(6);
    }

    private void addECG() {
        //creamos las propiedades de un canal con nombre "ECG", cuyo instante
        //de inicio es el tiempo actual, con la frecuencia de muestreo del ECG,
        //e indicamos el tama�o de los datos del canal
        ChannelProperties cp = new ChannelProperties("ECG", d.getTime(),
                jSMDataSource.getEcgSamplingRate(), jSMDataSource.getEcg().length);
        float min = jSMDataSource.getEcgRangeMinimum();
        float max = jSMDataSource.getEcgRangeMax();
        //indicamos el rango minimo y maximo de visualizacin
        cp.setVisibleRange(min, max);
        jSignalMonitor.addChannel("ECG", cp);
    }

    private void addHR() {
        ChannelProperties cp = new ChannelProperties("Hearth rate", d.getTime(),
                jSMDataSource.getHrSamplingRate(), jSMDataSource.getHeartRate().length);
        float min = jSMDataSource.getHeartRateRangeMinimum();
        float max = jSMDataSource.getHeartRateRangeMax();
        cp.setVisibleRange(min, max);
        jSignalMonitor.removeChannel("ECG"); //eliminamos el canal de ECG
        jSignalMonitor.addChannel("Hearth rate", cp);
    }

    public static void main(String[] args) {
        FrameConversorMIT f = new FrameConversorMIT(null);
        f.setVisible(true);
        MainWindow mainwindow = new MainWindow();
        mainwindow.setSize(500, 500);
        mainwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainwindow.setVisible(true);

    }

    private void jbInit() throws Exception {
        botonECG.addActionListener(new MainWindow_jCheckBoxECG_actionAdapter(this));
        botonHR.addActionListener(new MainWindow_jCheckBoxHR_actionAdapter(this));
        jSlider1.setMaximum(60);
        jSlider1.setValue(20);
        jSlider1.setPaintLabels(true);
        jSlider1.setPaintTicks(true);
        jSlider1.setPreferredSize(new Dimension(400, 24));
        jSlider1.addChangeListener(new MainWindow_jSlider1_changeAdapter(this));
        jPanel1.setPreferredSize(new Dimension(562, 70));
        this.getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);
        botonHR.setText("Heart Rate");
        jPanel1.add(botonECG);
        jPanel1.add(botonHR);
        jPanel1.add(jSlider1);
        botonECG.setText("ECG");
    }

    public void jCheckBoxECG_actionPerformed(ActionEvent e) {
        this.addECG();
    }

    public void jCheckBoxHR_actionPerformed(ActionEvent e) {
        this.addHR();
    }

    public void jSlider1_stateChanged(ChangeEvent e) {

        float val = jSlider1.getValue();
        val -= 30;
        val /= 10;
        jSignalMonitor.setFrecuency((float) Math.pow(10, -val));
        jSignalMonitor.repaintAll();
    }
}


class MainWindow_jSlider1_changeAdapter implements ChangeListener {
    private MainWindow adaptee;
    MainWindow_jSlider1_changeAdapter(MainWindow adaptee) {
        this.adaptee = adaptee;
    }

    public void stateChanged(ChangeEvent e) {
        adaptee.jSlider1_stateChanged(e);
    }
}


class MainWindow_jCheckBoxHR_actionAdapter implements ActionListener {
    private MainWindow adaptee;
    MainWindow_jCheckBoxHR_actionAdapter(MainWindow adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jCheckBoxHR_actionPerformed(e);
    }
}


class MainWindow_jCheckBoxECG_actionAdapter implements ActionListener {
    private MainWindow adaptee;
    MainWindow_jCheckBoxECG_actionAdapter(MainWindow adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jCheckBoxECG_actionPerformed(e);
    }
}
