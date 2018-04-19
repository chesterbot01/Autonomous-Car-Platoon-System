package components;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.encog.ConsoleStatusReportable;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.csv.CSVFormat;

import components.CarPlatoonFuzzyControl.Animator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class CarPlatoonFNN extends JApplet implements ActionListener {
	final int STABLE_COUNT = 250;
	int[] data_d2 = new int[STABLE_COUNT];
	int[] data_d3 = new int[STABLE_COUNT];
	double[] data_a1 = new double[STABLE_COUNT];
	double[] data_v1 = new double[STABLE_COUNT];
	double[] data_a2 = new double[STABLE_COUNT];
	double[] data_v2 = new double[STABLE_COUNT];
	double[] data_a3 = new double[STABLE_COUNT];
	double[] data_v3 = new double[STABLE_COUNT];
    String dir;         //the directory relative to the codebase
                        //from which the images are loaded

    Timer timer;		//the timer animating the images

    int pause;          //the length of the pause between revs

    int speed;          //animation speed
    int nimgs;          //number of images to animate
    int width;          //width of the applet's content pane
    Animator animator;  //the applet's content pane

    ImageIcon imgs[];   //the images
    int maxWidth;       //width of widest image
    JLabel statusLabel;
    int numOfTrainingSample = 0;
    //_________
    double pos1 = 0;
    double pos2 = 0;//pos1+dis_ini1
    double pos3 = 0;//pos2+dis_ini2
    
    double delta_1 = 0;
    double delta_2 = 0;
    double delta_3 = 0;
    
    double a1 = 0;
    double a2 = 0;
    double a3 = 0;
    
    
    JPanel controlPanel = new JPanel();
    GridLayout grid_layout = new GridLayout(12,2);
	
	JLabel vel_label = new JLabel();
	JLabel dis_label = new JLabel();
	JLabel dis_ini1_label = new JLabel();
	JLabel dis_ini2_label = new JLabel();
	JLabel vel_ini_label = new JLabel();
	
	NumberFormat nf = NumberFormat.getNumberInstance();//Note:Only the format of number is acceptable
	
	JFormattedTextField vel_text = new JFormattedTextField(nf);
	JFormattedTextField dis_text = new JFormattedTextField(nf);
	JFormattedTextField dis_ini1_text = new JFormattedTextField(nf);
	JFormattedTextField dis_ini2_text = new JFormattedTextField(nf);
	JFormattedTextField vel_ini_text = new JFormattedTextField(nf);
	
	double v_desired = -1;
	double d_desired = -1;
	double d_ini1 = -1;
	double d_ini2 = -1;
	double v_ini = -1;
	
	double v1 = 0;
	double v2 = 0;
	double v3 = 0;
	
	JButton setPara_button = new JButton("Confirm Setting");
    JButton start_button = new JButton("Run Using FNN!");
    
    JLabel v1_label = new JLabel();
    JLabel v2_label = new JLabel();
    JLabel v3_label = new JLabel();
    JLabel d2_label = new JLabel();
    JLabel d3_label = new JLabel();
    JLabel time_step_lb = new JLabel();
    
    JLabel v1_value = new JLabel();
    JLabel v2_value = new JLabel();
    JLabel v3_value = new JLabel();
    JLabel d2_value = new JLabel();
    JLabel d3_value = new JLabel();
    JLabel time_step_value = new JLabel();
    
    boolean isStart = false;
    boolean isSet = false;
    //________
    DecimalFormat df = new DecimalFormat("#.000");
    File file1;
    File file2;
    CSVFormat format = new CSVFormat('.','\t');
    NormalizationHelper helper1;
    NormalizationHelper helper2;
    MLRegression bestMethod1;
    MLRegression bestMethod2;
    long total_time1 = 0;
    long total_time2 = 0;
    double te1 = 0.0;
    double ve1 = 0.0;
    double te2 = 0.0;
    double ve2 = 0.0;
    //Called by init.
    protected void loadAppletParameters() {
        //Get the applet parameters.
        String at = getParameter("img");
        dir = (at != null) ? at : "images/tumble";
        at = getParameter("pause");
        pause = (at != null) ? Integer.valueOf(at).intValue() : 1900;
        at = getParameter("speed");
        speed = (at != null) ? (1000 / Integer.valueOf(at).intValue()) : 100;
        at = getParameter("nimgs");
        nimgs = (at != null) ? Integer.valueOf(at).intValue() : 3;
        at = getParameter("maxwidth");
        maxWidth = (at != null) ? Integer.valueOf(at).intValue() : 0;
    }

    /**
     * Create the GUI. For thread safety, this method should
     * be invoked from the event-dispatching thread.
     */
    private void createGUI() {
        //Animate from right to left if offset is negative.
        width = getSize().width;
        //Custom component to draw the current image
        //at a particular offset.
        animator = new Animator();
        animator.setOpaque(true);
        animator.setBackground(Color.white);
        setContentPane(animator);
        
        //Put a "Loading Images..." label in the middle of
        //the content pane.  To center the label's text in
        //the applet, put it in the center part of a
        //BorderLayout-controlled container, and center-align
        //the label's text.
        statusLabel = new JLabel("Loading Images...",
                                 JLabel.CENTER);
        animator.add(statusLabel, BorderLayout.CENTER);
        //_______
        vel_label.setText("v_desired:");
		dis_label.setText("d_desired:");
		dis_ini1_label.setText("d_initial1:");
		dis_ini2_label.setText("d_initial2:");
		vel_ini_label.setText("v_initial:");
		
		v1_label.setText("v_1 = ");
	    v2_label.setText("v_2 = ");
	    v3_label.setText("v_3 = ");
	    d2_label.setText("d_1 = ");
	    d3_label.setText("d_2 = ");
	    time_step_lb.setText("time steps = ");
        controlPanel.setLayout(grid_layout);

        controlPanel.add(vel_label);
		controlPanel.add(vel_text);
		controlPanel.add(dis_label);
		controlPanel.add(dis_text);
		controlPanel.add(dis_ini1_label);
		controlPanel.add(dis_ini1_text);
		controlPanel.add(dis_ini2_label);
		controlPanel.add(dis_ini2_text);
		controlPanel.add(vel_ini_label);
		controlPanel.add(vel_ini_text);
		controlPanel.add(setPara_button);
		controlPanel.add(start_button);
		
		controlPanel.add(v1_label);
		controlPanel.add(v1_value);
		controlPanel.add(v2_label);
		controlPanel.add(v2_value);
		controlPanel.add(v3_label);
		controlPanel.add(v3_value);
		controlPanel.add(d2_label);
		controlPanel.add(d2_value);
		controlPanel.add(d3_label);
		controlPanel.add(d3_value);
		controlPanel.add(time_step_lb);
		controlPanel.add(time_step_value);

		start_button.addActionListener(  
                new ActionListener(){  
                    public void actionPerformed(ActionEvent e) {  
                        isStart = !isStart;
                        if(isStart)
                        	start_button.setText("Pause");
                        else
                        	start_button.setText("Run Using FNN!");
                    }  
                });
		setPara_button.addActionListener(  
                new ActionListener(){  
                    public void actionPerformed(ActionEvent e) {
                        //Note:initialize the conditions
                    	v_desired = 1.0*Integer.valueOf(vel_text.getText()).intValue();
                    	d_desired = 1.0*Integer.valueOf(dis_text.getText()).intValue();
                    	d_ini1 = 1.0*Integer.valueOf(dis_ini1_text.getText()).intValue();
                    	d_ini2 = 1.0*Integer.valueOf(dis_ini2_text.getText()).intValue();
                    	v_ini = 1.0*Integer.valueOf(vel_ini_text.getText()).intValue();
                    	pos3 = 0.0;
                    	pos2 = pos3 + d_ini2;
                    	pos1 = pos2 + d_ini1;
                    	v1 = v_ini;
                    	v2 = v_ini;
                    	v3 = v_ini;
                    	isSet = true; 
                    }  
                });
        animator.add(controlPanel,BorderLayout.PAGE_END);
    }

    //Background task for loading images.
    SwingWorker worker = new SwingWorker<ImageIcon[], Void>() {
        @Override
        public ImageIcon[] doInBackground() {
            final ImageIcon[] innerImgs = new ImageIcon[nimgs];
            for (int i = 0; i < nimgs; i++) {
                innerImgs[i] = loadImage(i + 1);
            }
            return innerImgs;
        }

        @Override
        public void done() {
            //Remove the "Loading images" label.
            animator.remove(statusLabel);//Note:È¥µô¡°loading¡±µÄlabel
            try {
                imgs = get();
            } catch (InterruptedException ignore) {}
            catch (java.util.concurrent.ExecutionException e) {
                String why = null;
                Throwable cause = e.getCause();
                if (cause != null) {
                    why = cause.getMessage();
                } else {
                    why = e.getMessage();
                }
                System.err.println("Error retrieving file: " + why);
            }
        }
    };

    //Called when this applet is loaded into the browser.
    public void init() {
        loadAppletParameters();
        //train 2 network here
        file1 = new File(this.getClass().getResource("/").getPath()+"n1_dataset");
        file2 = new File(this.getClass().getResource("/").getPath()+"n2_dataset");
        network1_train();
        network2_train();
        System.out.println("time1: "+total_time1);
        System.out.println("training error1 "+te1);
        System.out.println("validation error1 "+ve1);
        System.out.println("time2: "+total_time2);
        System.out.println("training error2 "+te2);
        System.out.println("validation error2 "+ve2);
        //Execute a job on the event-dispatching thread:
        //creating this applet's GUI.
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createGUI();
                }
            });
        } catch (Exception e) { 
            System.err.println("createGUI didn't successfully complete");
        }

        //Set up timer to drive animation events.
        timer = new Timer(speed, this);
        timer.setInitialDelay(pause);
        timer.start(); 

        //Start loading the images in the background.
        worker.execute();
    }

    //The component that actually presents the GUI.
    public class Animator extends JPanel {
        public Animator() {
            super(new BorderLayout());
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (worker.isDone()) {
                if (imgs != null) {
                	if(isSet || isStart){
	                	imgs[0].paintIcon(this, g, (int)Math.round(pos1), 0);
	                	imgs[1].paintIcon(this, g, (int)Math.round(pos2), 0);
	                	imgs[2].paintIcon(this, g, (int)Math.round(pos3), 0);
                	}
                	else{
                		imgs[0].paintIcon(this, g, 80, 0);
	                	imgs[1].paintIcon(this, g, 40, 0);
	                	imgs[2].paintIcon(this, g, 0, 0);
                	}
                }
            }
        }
    }

    //Handle timer event.
    public void actionPerformed(ActionEvent e) {
        //If still loading, can't animate.
        if (!worker.isDone()) {
            return;
        }

        if(pos1>1560.0){
    		double diff = pos3;
    		pos3 = 0.0;
    		pos2 = pos2 - diff;
    		pos1 = pos1 - diff;
    	}
        //
        if(isStart){
        	v1_value.setText(""+Math.round(v1));
            v2_value.setText(""+Math.round(v2));
            v3_value.setText(""+Math.round(v3));
            d2_value.setText(""+Math.round(pos1-pos2));
            d3_value.setText(""+Math.round(pos2-pos3));
            time_step_value.setText(""+numOfTrainingSample);

        	//using FNN to approximate a1 a2 a3
            String[] input1 = new String[2];
            MLData FNN_input1 = helper1.allocateInputVector();
            input1[0] = df.format(v1);
            input1[1] = df.format(v_desired);
            helper1.normalizeInputVector(input1,FNN_input1.getData(),false);
            MLData FNN_output1 = bestMethod1.compute(FNN_input1);
            String predicted_a1 = helper1.denormalizeOutputVectorToString(FNN_output1)[0];
            a1 = Double.parseDouble(predicted_a1);
            
            double d2 = pos1 - pos2;
            String[] input2 = new String[4];
            MLData FNN_input2 = helper2.allocateInputVector();
            input2[0] = df.format(v2);
            input2[1] = df.format(v1);
            input2[2] = df.format(d2);
            input2[3] = df.format(d_desired);
            helper2.normalizeInputVector(input2,FNN_input2.getData(),false);
            MLData FNN_output2 = bestMethod2.compute(FNN_input2);
            String predicted_a2 = helper2.denormalizeOutputVectorToString(FNN_output2)[0];
            a2 = Double.parseDouble(predicted_a2);
            
            double d3 = pos2 - pos3;
            String[] input3 = new String[4];
            MLData FNN_input3 = helper2.allocateInputVector();
            input3[0] = df.format(v3);
            input3[1] = df.format(v2);
            input3[2] = df.format(d3);
            input3[3] = df.format(d_desired);
            helper2.normalizeInputVector(input3,FNN_input3.getData(),false);
            MLData FNN_output3 = bestMethod2.compute(FNN_input3);
            String predicted_a3 = helper2.denormalizeOutputVectorToString(FNN_output3)[0];
            a3 = Double.parseDouble(predicted_a3);
            
            if(numOfTrainingSample<STABLE_COUNT){
                data_d2[numOfTrainingSample] = (int) Math.round(d2);
                data_d3[numOfTrainingSample] = (int) Math.round(d3);
                data_a1[numOfTrainingSample] = a1;
                data_a2[numOfTrainingSample] = a2;
                data_a3[numOfTrainingSample] = a3;
                data_v1[numOfTrainingSample] = v1;
                data_v2[numOfTrainingSample] = v2;
                data_v3[numOfTrainingSample] = v3;
            }
            
            if(numOfTrainingSample == STABLE_COUNT){
        		System.out.println("v1=");
            	for(int i=0; i<STABLE_COUNT; i++){
            		System.out.print(data_v1[i]+",");
            	}
            	System.out.println("");
            	
            	System.out.println("v2=");
            	for(int i=0; i<STABLE_COUNT; i++){
            		System.out.print(data_v2[i]+",");
            	}
            	System.out.println("");
            	
            	System.out.println("v3=");
            	for(int i=0; i<STABLE_COUNT; i++){
            		System.out.print(data_v3[i]+",");
            	}
            	System.out.println("");
            	System.out.println("d2=");
            	for(int i=0; i<STABLE_COUNT; i++){
            		System.out.print(data_d2[i]+",");
            	}
            	System.out.println("");
            	
            	System.out.println("d3=");
            	for(int i=0; i<STABLE_COUNT; i++){
            		System.out.print(data_d3[i]+",");
            	}
            	System.out.println("");
        	}
            numOfTrainingSample++;
            //Note:t_delta = 1 calculate the change of the position
	        delta_1 = v1 + a1/2;
	        delta_2 = v2 + a2/2;
	        delta_3 = v3 + a3/2;
	        
	        pos1 += delta_1;
	        pos2 += delta_2;
	        pos3 += delta_3;
	        
	        //Note:change the velocity
	        v1 += a1;
	        v2 += a2;
	        v3 += a3;
        }
        
        animator.repaint();
    }
    

    public void start() {
        if (worker.isDone() && (nimgs > 1)) {
        	//System.out.println("********");//Note:invoked once
            timer.restart();
        }
    }

    public void stop() {
        timer.stop();
    }

    /**
     * Load the image for the specified frame of animation. Since
     * this runs as an applet, we use getResourceAsStream for 
     * efficiency and so it'll work in older versions of Java Plug-in.
     */
    protected ImageIcon loadImage(int imageNum) {
        String path = dir + "/car" + imageNum + ".gif";
        int MAX_IMAGE_SIZE = 1000;  //Note: Change this to the size of
                                     //your biggest image, in bytes.
        int count = 0;
        BufferedInputStream imgStream = new BufferedInputStream(
           this.getClass().getResourceAsStream(path));
        if (imgStream != null) {
            byte buf[] = new byte[MAX_IMAGE_SIZE];
            try {
                count = imgStream.read(buf);
                imgStream.close();
            } catch (java.io.IOException ioe) {
                System.err.println("Couldn't read stream from file: " + path);
                System.out.println(ioe);
                return null;
            }
            if (count <= 0) {
                System.err.println("Empty file: " + path);
                return null;
            }
            return new ImageIcon(Toolkit.getDefaultToolkit().createImage(buf));
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public String getAppletInfo() {
        return "Title: CarPlatoon v1.2, 1 Aug 2017\n"
               + "Author: Hongming Wang\n"
               + "Control the car platoon using feedforward neural network";
    }
    //car1 uses network1
    private void network1_train(){
    	VersatileDataSource source = new CSVDataSource(file1, false, format);
    	VersatileMLDataSet data = new VersatileMLDataSet(source);
    	data.getNormHelper().setFormat(format);
		ColumnDefinition column_v1 = data.defineSourceColumn("v1", 0, ColumnType.continuous);
		ColumnDefinition column_v_desired = data.defineSourceColumn("v_desired", 1, ColumnType.continuous);
    	ColumnDefinition column_a1 = data.defineSourceColumn("a1", 2, ColumnType.continuous);
    	data.analyze();
    	data.defineSingleOutputOthersInput(column_a1);
    	EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		model.setReport(new ConsoleStatusReportable());
		data.normalize();
		model.holdBackValidation(0.3, true, 1001);
		model.selectTrainingType(data);
		long sysDate1 = System.currentTimeMillis();
		bestMethod1 = (MLRegression)model.crossvalidate(5, true);
		total_time1 = System.currentTimeMillis()-sysDate1;
		te1 = model.calculateError(bestMethod1, model.getTrainingDataset());
		ve1 = model.calculateError(bestMethod1, model.getValidationDataset());
		//System.out.println( "Training error: " + model.calculateError(bestMethod1, model.getTrainingDataset()));
		//System.out.println( "Validation error: " + model.calculateError(bestMethod1, model.getValidationDataset()));
		helper1 = data.getNormHelper();
		System.out.println(helper1.toString());
		System.out.println("Final model for network1: " + bestMethod1);
		//EncogDirectoryPersistence.saveObject(new File("trainedFNN_1"), bestMethod1);
    }
    //car2 & car3 share the same network
    private void network2_train(){
    	VersatileDataSource source = new CSVDataSource(file2, false, format);
    	VersatileMLDataSet data = new VersatileMLDataSet(source);
    	data.getNormHelper().setFormat(format);
    	ColumnDefinition column_v_current = data.defineSourceColumn("v_current", 0, ColumnType.continuous);
    	ColumnDefinition column_v_front = data.defineSourceColumn("v_front", 1, ColumnType.continuous);
    	ColumnDefinition column_distance_current = data.defineSourceColumn("distance_current", 2, ColumnType.continuous);
    	ColumnDefinition column_distance_desired = data.defineSourceColumn("distance_desired", 3, ColumnType.continuous);
    	ColumnDefinition column_a = data.defineSourceColumn("a", 4, ColumnType.continuous);
    	data.analyze();
    	data.defineSingleOutputOthersInput(column_a);
    	EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		model.setReport(new ConsoleStatusReportable());
		data.normalize();
		model.holdBackValidation(0.3, true, 1001);
		model.selectTrainingType(data);
		long sysDate1 = System.currentTimeMillis();
		bestMethod2 = (MLRegression)model.crossvalidate(5, true);
		total_time2 = System.currentTimeMillis()-sysDate1;
		te2 = model.calculateError(bestMethod2, model.getTrainingDataset());
		ve2 = model.calculateError(bestMethod2, model.getValidationDataset());
		//System.out.println( "Training error: " + te1);
		//System.out.println( "Validation error: " + model.calculateError(bestMethod2, model.getValidationDataset()));
		helper2 = data.getNormHelper();
		System.out.println(helper2.toString());
		System.out.println("Final model for network2: " + bestMethod2);
		//EncogDirectoryPersistence.saveObject(new File("trainedFNN_2"), bestMethod2);
    }
}
