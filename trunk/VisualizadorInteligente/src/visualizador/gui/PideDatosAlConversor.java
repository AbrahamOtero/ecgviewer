package visualizador.gui;

import java.util.*;

import javax.swing.*;

import es.usc.gsi.conversorDatosMIT.ficheros.*;
import es.usc.gsi.conversorDatosMIT.interfaz.*;
import visualizador.JSMDataSource;

/**
 * <p>Title: Herraienta de monitorizacion</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 1999</p>
 * <p>Company: GSI</p>
 * @author Abraham Otero
 * @version 0.2
 */
public class PideDatosAlConversor extends Thread {

    /**
     * La aplicaci�n para importar datos desde el formato MIT  invocar� a este
     * m�todo con todos los datos que el usuario haya seleccionado en la
     * herramienta.
     *
     * @param parametros Array de {@link Parametro}; El resto de la informaci�n
     *   que se le pasa al mtodo realmente es redundante; esto contiene todos
     *   los datos que se han extra�do del fichero MIT. Estoy pasando tambi�n
     *   los otros par�metros para que le sea m�s f�cil comenzar a jugar con
     *   esto.
     * @param datos Esta matriz contiene en cada una de sus filas cada uno de
     *   los arrays de datos que han sido importados.
     * @param fs En cada una de sus posiciones contiene la frecuencia de
     *   muestreo del array correspondiente de la matriz anterior; es decir, en
     *   la posici�n 0 de este array est� la frecuencia de muestreo del array
     *   de la primera fila de la matriz que se pasa como segundo par�metro.
     * @modificar
     */
    private void importDataFromMIT(Parametro[] parametros, float[][] datos, float[] fs) {
        JSMDataSource jSMDataSource = JSMDataSource.getJSWBManagerInstance();

        for (int i = 0; i < parametros.length; i++) {
            if (fs[i] > 50) {
                jSMDataSource.setEcg(datos[i]);
                jSMDataSource.setEcgSamplingRate(fs[i]);
                jSMDataSource.setEcgRangeMax(100);
                jSMDataSource.setEcgRangeMinimum(-100);
            } else {
                jSMDataSource.setHeartRate(datos[i]);
                jSMDataSource.setHrSamplingRate(fs[i]);
                jSMDataSource.setHeartRateRangeMinimum(0);
                jSMDataSource.setHeartRateRangeMax(100);
            }
        }
    }
    private float[][] rangos;
    private int max_numero_datos;
    private PanelPrincipal conversor;
    public static final String error_quedarse_sin_memoria = "<html>" +
            "<body text=\"#000000\">" +
            "<p align=\"center\"><font color=\"#FF0000\" size=\"5\">Error al exportar los datos </font></p></p>" +
            "<p><font size=\"4\" color=\"#0000FF\">El equipo no tiene memoria virtual suficiente </font></p>" +
            "<p><font size=\"4\" color=\"#0000FF\">para importar todos los datos que usted ha</font></p>" +
            "<p><font size=\"4\" color=\"#0000FF\">selecionado. Importe un intervalo temporal</font></p>" +
            "<p><font size=\"4\" color=\"#0000FF\">menos o disminuya la frecuecia de muestreo</font></p>" +
            "<p><font size=\"4\" color=\"#0000FF\">de las se&ntilde;ales que est&aacute; adquiriendo.</font></p>" +
            "</body>" +
            "</html>";

    // private CuadroDeEspera espera;
    private float paso;
    int datos_por_aaray;

    public PideDatosAlConversor(PanelPrincipal conversor) {
        this.conversor = conversor;

    }

    /**
     * m�todo que coge los datos del conversor y los carga en la interface.
     */
    public boolean cargarDatos() {
        max_numero_datos = Integer.MIN_VALUE;

//LLAMADA A C�digo de CONVERSOTMIT
        Parametro[] parametros = null;
        try {
            parametros = conversor.getParametros();
        } catch (OutOfMemoryError ex) {
            JOptionPane.showMessageDialog(conversor, error_quedarse_sin_memoria,
                    "ERROR!!!",
                    JOptionPane.ERROR_MESSAGE);
        }

//Si no cancel� la operaci�n
        if (parametros != null) {
            if (parametros.length != 2) {
                JOptionPane.showMessageDialog(null, "Error, no se han seleccionado dos parmetros",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            float[][] datos = new float[parametros.length][];
            String[] nombresSenales = new String[parametros.length];
            String[] unidadesSenales = new String[parametros.length];
            float[] fs = new float[parametros.length];
            rangos = new float[parametros.length + 1][2];

            //Esto es para estimar que % se ha pasado:
            paso = 100 / parametros.length * parametros[0].getValores().length;
            paso = paso / 400;
            datos_por_aaray = parametros[0].getValores().length;
            //Recojo los datos del array de par�metros
            for (int i = 0; i < parametros.length; i++) {
                datos[i] = arrayIntToFloat(parametros[i].getValores(),
                        parametros[i].getGanancia(), i);
                nombresSenales[i] = parametros[i].getNombreParametro();
                fs[i] = parametros[i].getFrecuenciaMuestreo();
                unidadesSenales[i] = parametros[i].getUnidades();

            }
            rangos[parametros.length][0] = 0;
            rangos[parametros.length][1] = 100;
            //Configuramos la fecha base del registro:
            //Asumo que todos tiene la misma fecha de incio y cojo la del priemr parametro
            String fechaBaseConversor = parametros[0].getFechaInicio();
            long fecha = obtenerFecha(fechaBaseConversor);
            importDataFromMIT(parametros, datos, fs);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @todo ojo, no tengo claro si en el formato MIT los dis y meses se empiezan
     * a contar en 0 o en 1. He supuesto que se empiezan a contar en 1.
     * @param fechaBaseConversor String
     * @return long
     */
    private long obtenerFecha(String fechaBaseConversor) {
        StringTokenizer tk = new StringTokenizer(fechaBaseConversor);
        String diaMesAno = tk.nextToken();
        String horaMinSeg = tk.nextToken();
        tk = new StringTokenizer(horaMinSeg, ":", false);
        int hora = 0, minutos = 0, segundos = 0;
        try {
            hora = Integer.parseInt(tk.nextToken());
            minutos = Integer.parseInt(tk.nextToken());
            segundos = Integer.parseInt(tk.nextToken());
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            return 0;
        }
        tk = new StringTokenizer(diaMesAno, "/", false);
        int ano = 0, mes = 0, dia = 0;
        try {
            dia = Integer.parseInt(tk.nextToken());
            mes = Integer.parseInt(tk.nextToken());
            ano = Integer.parseInt(tk.nextToken());
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            return 0;
        }

        Calendar d = new GregorianCalendar(ano, mes, dia, hora, minutos,
                segundos);
        return d.getTime().getTime();
    }

    /**
     * Se le pasa una array de int y lo devuelve como array de float
     * @param datos_int
     * @return
     */
    private final float[] arrayIntToFloat(int[] datos_int, float ganancia,
            int senal) {
        float[] datos = new float[datos_int.length];
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (int i = 0; i < datos_int.length; i++) {
            datos[i] = datos_int[i];
            //@todo modificado hace poco datos[i] /= ganancia;
            if (datos[i] > max) {
                max = datos[i];
            }
            if (datos[i] < min) {
                min = datos[i];
            }

            //Para el progrss bar
            if (i % 400 == 0) {
                //    espera.actulizaProgressBar((int)((senal*datos_por_aaray + i)* paso));
            }
        }
        if (max_numero_datos < datos.length) {
            max_numero_datos = datos.length;
        }

        rangos[senal][0] = Math.round(min);
        rangos[senal][1] = Math.round(max);
        return datos;
    }

    public void run() {
        setPriority(Thread.MIN_PRIORITY);
        this.cargarDatos();
    //   espera.cambiaEstado();

    }
}
