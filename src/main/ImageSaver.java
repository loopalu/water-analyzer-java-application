package main;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * Saves chart data into an image
 */
class ImageSaver extends ApplicationFrame {
    private int step = 0;
    private ArrayList data;

    /**
     * Constructs a new demonstration application.
     *
     * @param title  the frame title.
     * @param imageName the name of image.
     */
    private ImageSaver(final String title, ArrayList arrayList, String imageName) {
        super(title);
        this.data = arrayList;
        final JFreeChart chart = createCombinedChart();
        final ChartPanel panel = new ChartPanel(chart, true, true, true, false, true);
        panel.setPreferredSize(new java.awt.Dimension(1000, 500));
        setContentPane(panel);
        try {
            OutputStream out = new FileOutputStream(imageName);
            ChartUtilities.writeChartAsPNG(out, chart, 1000, 500);
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(ImageSaver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates a combined chart.
     *
     * @return The combined chart.
     */
    private JFreeChart createCombinedChart() {
        final XYDataset data = createDataset();
        final XYItemRenderer renderer = new StandardXYItemRenderer();
        final NumberAxis rangeAxis = new NumberAxis("");
        rangeAxis.setAutoRangeIncludesZero(false);
        final XYPlot subplot = new XYPlot(data, null, rangeAxis, renderer);
        subplot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);

        // parent plot...
        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis(""));
        plot.setGap(10.0);

        // add the subplots...
        plot.add(subplot, 1);
        plot.setOrientation(PlotOrientation.VERTICAL);

        // return a new chart containing the overlaid plot...
        return new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, false);
    }

    /**
     * Creates a sample dataset.
     *
     * @return A sample dataset.
     */
    private XYDataset createDataset() {
        final XYSeries series = new XYSeries("Capacity");
        while (!data.isEmpty()) {
            series.add(step++, (int) data.remove(0));
        }
        return new XYSeriesCollection(series);
    }

    /**
     * Saves image.
     *
     * @param testData The data from test.
     * @param fileName The name of file.
     */
    public static void saveImage(final ArrayList testData, String fileName) {
        final ImageSaver demo = new ImageSaver("", testData, fileName);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(false);
        //System.exit(1);
    }
}
