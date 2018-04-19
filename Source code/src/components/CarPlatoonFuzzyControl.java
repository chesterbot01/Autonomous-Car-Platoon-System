
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/*
 * CarPlatoonFuzzyControl.java requires these files:
 *   all the images in the images/tumble directory
 *     (or, if specified in the applet tag, another directory [dir]
 *     with images named T1.gif ... Tx.gif, where x is the total
 *     number of images [nimgs])
 *   the appropriate code to specify that the applet be executed,
 *     such as the HTML code in CarPlatoonFuzzyControl.html or CarPlatoonFuzzyControl.atag,
 *     or the JNLP code in CarPlatoonFuzzyControl.jnlp
 *
 */
public class CarPlatoonFuzzyControl extends JApplet
                        implements ActionListener {
	
	final int STABLE_COUNT = 125;
	int numOfTrainingSample = 0;
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
	
	NumberFormat nf = NumberFormat.getNumberInstance();//Note:only the format of number is acceptable
	
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
    JButton start_button = new JButton("Run Using Fuzzy Logic!");
    
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
    
    
    //Called by init.
    protected void loadAppletParameters() {
    	//Note:just setting some default values
    	
        //Get the applet parameters.
        String at = getParameter("img");
        dir = (at != null) ? at : "images/tumble";
        at = getParameter("pause");
        pause = (at != null) ? Integer.valueOf(at).intValue() : 1900;
        //at = getParameter("offset");
        //offset = (at != null) ? Integer.valueOf(at).intValue() : 0;
        at = getParameter("speed");
        speed = (at != null) ? (1000 / Integer.valueOf(at).intValue()) : 100;//the smaller than faster
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
        //System.out.println(width); //Note:width of the screen
        //System.out.println("&&&&&&&");//Note:invoked only once
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
                        	start_button.setText("Run Using Fuzzy Logic!");
                    }  
                });
		setPara_button.addActionListener(  
                new ActionListener(){  
                    public void actionPerformed(ActionEvent e) {
                        //Note:initialize the conditions specified by the user
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
                    	//System.out.println("^^^"+v_desired);
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
            animator.remove(statusLabel);
            //System.out.println("!!!!!!!");//Note:invoked only once
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
                    //Note:important! [0,1560]
                	if(isSet || isStart){
	                	imgs[0].paintIcon(this, g, (int)Math.round(pos1), 0);
	                	imgs[1].paintIcon(this, g, (int)Math.round(pos2), 0);
	                	imgs[2].paintIcon(this, g, (int)Math.round(pos3), 0);
                	}
                	else{//Note:Just the initial display
                		imgs[0].paintIcon(this, g, 80, 0);
	                	imgs[1].paintIcon(this, g, 40, 0);
	                	imgs[2].paintIcon(this, g, 0, 0);
                	}
                	//System.out.println("+++++++++");//Note:invoked multiple times
                }
            }
        }
    }

    //Handle timer event.
    public void actionPerformed(ActionEvent e) {
    	//Note:update the state here
    	
        //If still loading, can't animate.
        if (!worker.isDone()) {
            return;
        }

        if(pos1>1560.0){//Note:roll-back mechanism, relocate pos1 pos2 pos3
    		double diff = pos3;
    		pos3 = 0.0;
    		pos2 = pos2 - diff;
    		pos1 = pos1 - diff;
    	}
        //
        if(isStart){
        	//Note:control strategies here, a1 a2 a3 are determined
        	
        	v1_value.setText(""+Math.round(v1));
            v2_value.setText(""+Math.round(v2));
            v3_value.setText(""+Math.round(v3));
            d2_value.setText(""+Math.round(pos1-pos2));
            d3_value.setText(""+Math.round(pos2-pos3));
            time_step_value.setText(""+numOfTrainingSample);
            //System.out.println(numOfTrainingSample);
        	
        	//rules for car1 
        	//if(!isStable1){
        		if(v1>=v_desired+16){//very fast
        			a1 = -4.0;
        		}
        		else if(v1<v_desired+16 && v1>=v_desired+8){
        			a1 = (v_desired+16-v1)/8.0*(-2)+(v1-v_desired-8)/8.0*(-4);
        		}
        		else if(v1<v_desired+8 && v1>=v_desired){
        			a1 = (v1-v_desired)/8.0*(-2);
        		}
        		else if(v1<v_desired && v1>=v_desired-8){
        			a1 = (v_desired-v1)/8.0*2;
        		}
        		else if(v1<v_desired-8 && v1>=v_desired-16){
        			a1 = (v1-v_desired+16)/8.0*2+(v_desired-8-v1)/8.0*4;
        		}
        		else{
        			a1 = 4.0;
        		}
        	//}
        	//else
        		//a1 = 0.0;
        	//rules for car2
        	//if(!isStable2){//2 input, 1 outout
        		double d2 = pos1 - pos2;
        		if(v2>=v1+16){//very fast
        			if(d2>=4*d_desired){//very far
        				a2 = 0.0;
        			}
        			else if(d2<4*d_desired && d2>=2*d_desired){
        				a2 = -2.0;//no need for defuzzi, because car2 is too fast
        			}
        			else{//d2<2*d_desired
        				a2 = -4.0;
        			}
        		}
        		else if(v2<v1+16 && v2>=v1+8){//fast
        			if(d2>=4*d_desired){
        				a2 = 0;
        			}
        			else if(d2<4*d_desired && d2>=2*d_desired){
        				a2 = Math.max((v1+16-v2)/8.0, (4*d_desired-d2)/2.0/d_desired)*(-2);
        			}
        			else if(d2<2*d_desired && d2>=d_desired){
        				a2 = Math.max((v1+16-v2)/8.0,(2*d_desired-d2)/1.0/d_desired)*(-4);
        			}
        			else{
        				a2 = -4.0;
        			}
        		}
        		else if(v2<v1+8 && v2>=v1){
        			if(d2>=4*d_desired){
        				a2 = 2.0;//Note:acclerate to catch up
        			}
        			else if(d2<4*d_desired && d2>=2*d_desired){
        				a2 = Math.max((v1+8-v2)/8.0, (4*d_desired-d2)/2.0/d_desired)*(2);
        			}
        			else if(d2<2*d_desired && d2>=d_desired){
        				if(Math.abs(v2-v1)<0.01)
        					a2 = Math.max((v2-v1)/8.0,(d2-d_desired)/1.0/d_desired)*(2);
        				else
        					a2 = Math.min((v2-v1)/8.0,(d2-d_desired)/1.0/d_desired)*(2);
        			}
        			else if(d2<d_desired && d2>=0.75*d_desired)
        				a2 = Math.max((v2-v1)/8.0,(d_desired-d2)/0.25/d_desired)*(-2);
        			else if(d2<0.75*d_desired && d2>=0.5*d_desired)
        				a2 = Math.max((v2-v1)/8.0,(d_desired*0.75-d2)/0.25/d_desired)*(-4);
        			else
        				a2 = -4.0;
        		}
        		else if(v2<v1 && v2>=v1-8){
        			if(d2>=4*d_desired){
        				a2 = 4.0;//Note:acclerate to catch up
        			}
        			else if(d2<4*d_desired && d2>=2*d_desired){
        				a2 = Math.max((v2-v1+8)/8.0, (d2-4*d_desired)/2.0/d_desired)*(4);
        			}
        			else if(d2<2*d_desired && d2>=d_desired){
        				if(Math.abs(v2-v1)<0.1)
        					a2 = Math.max((v1-v2)/8.0,(2*d_desired-d2)/1.0/d_desired)*(2)/5;//k=5
        				else
        					a2 = Math.min((v1-v2)/8.0,(2*d_desired-d2)/1.0/d_desired)*(2);
        			}
        			else if(d2<d_desired && d2>=0.75*d_desired){
        				if(Math.abs(v2-v1)<0.1)
        					a2 = (d_desired-d2)/0.25/d_desired*(-2);
        				else
        					a2 = 0;
        			}
        			else if(d2<0.75*d_desired && d2>=0.5*d_desired){
        				a2 = (d_desired*0.75-d2)/0.25/d_desired*(-4);
        			}
        			else
        				a2 = -4.0;
        		}
        		else if(v2<v1-8 && v2>=v1-16){
        			if(d2>=4*d_desired){
        				a2 = 4.0;//Note:acclerate to catch up
        			}
        			else if(d2<4*d_desired && d2>=2*d_desired){
        				a2 = Math.max((v2-v1+8)/8.0, (d2-4*d_desired)/2.0/d_desired)*(4);
        			}
        			else if(d2<2*d_desired && d2>=d_desired){
        				a2 = Math.max((v1-v2)/8.0,(2*d_desired-d2)/1.0/d_desired)*(4);
        			}
        			else{
        				a2 = 0;//Note:not proper to accelerate within the secure distance
        			}
        		}
        		else{//Note:the slowest case//
        			if(d2>=d_desired)
        				a2 = 4.0;
        			else{
        				a2 = 0;//not proper to accelerate within the secure distance
        			}
        		}
        	//}
        	//else
        		//a2 = 0;
        	//rules for car3, similar as car2
        	//if(!isStable3){
        		double d3 = pos2 - pos3;
        		if(v3>=v2+16){
        			if(d3>=4*d_desired){//very far
        				a3 = 0;
        			}
        			else if(d3<4*d_desired && d3>=2*d_desired){
        				a3 = -2.0;
        			}
        			else{
        				a3 = -4.0;
        			}
        		}
        		else if(v3<v2+16 && v3>=v2+8){
        			if(d3>=4*d_desired){
        				a3 = 0;
        			}
        			else if(d3<4*d_desired && d3>=2*d_desired){
        				a3 = Math.max((v2+16-v3)/8.0, (4*d_desired-d3)/2.0/d_desired)*(-2);
        			}
        			else if(d3<2*d_desired && d3>=d_desired){
        				a3 = Math.max((v2+16-v3)/8.0,(2*d_desired-d3)/1.0/d_desired)*(-4);
        			}
        			else{
        				a3 = -4.0;
        			}
        		}
        		else if(v3<v2+8 && v3>=v2){
        			if(d3>=4*d_desired){
        				a3 = 2.0;//Note:acclerate to catch up
        			}
        			else if(d3<4*d_desired && d3>=2*d_desired){
        				a3 = Math.max((v2+8-v3)/8.0, (4*d_desired-d3)/2.0/d_desired)*(2);
        			}
        			else if(d3<2*d_desired && d3>=d_desired){
        				if(Math.abs(v3-v2)<0.01)
        					a3 = Math.max((v3-v2)/8.0,(d3-d_desired)/1.0/d_desired)*(2);
        				else
        					a3 = Math.min((v3-v2)/8.0,(d3-d_desired)/1.0/d_desired)*(2);
        			}
        			else if(d3<d_desired && d3>=0.75*d_desired)
        				a3 = Math.max((v3-v2)/8.0,(d_desired-d3)/0.25/d_desired)*(-2);
        			else if(d3<0.75*d_desired && d3>=0.5*d_desired)
        				a3 = Math.max((v3-v2)/8.0,(d_desired*0.75-d3)/0.25/d_desired)*(-4);
        			else
        				a3 = -4.0;
        		}
        		else if(v3<v2 && v3>=v2-8){
        			if(d3>=4*d_desired){
        				a3 = 4.0;//Note:acclerate to catch up
        			}
        			else if(d3<4*d_desired && d3>=2*d_desired){
        				a3 = Math.max((v3-v2+8)/8.0, (d3-4*d_desired)/2.0/d_desired)*(4);
        			}
        			else if(d3<2*d_desired && d3>=d_desired){
        				if(Math.abs(v3-v2)<0.1)
        					a3 = Math.max((v2-v3)/8.0,(2*d_desired-d3)/1.0/d_desired)*(2)/5;//k=5
        				else
        					a3 = Math.min((v2-v3)/8.0,(2*d_desired-d3)/1.0/d_desired)*(2);
        			}
        			else if(d3<d_desired && d3>=0.75*d_desired){
        				if(Math.abs(v3-v2)>0.1)
        					a3 = 0;
        				else
        					a3 = (d_desired-d3)/0.25/d_desired*(-2);
        			}
        			else if(d3<0.75*d_desired && d3>=0.5*d_desired){
        				a3 = (d_desired*0.75-d3)/0.25/d_desired*(-4);
        			}
        			else
        				a3 = -4.0;
        		}
        		else if(v3<v2-8 && v3>=v2-16){
        			if(d3>=4*d_desired){
        				a3 = 4.0;//Note:acclerate to catch up
        			}
        			else if(d3<4*d_desired && d3>=2*d_desired){
        				a3 = Math.max((v3-v2+8)/8.0, (d3-4*d_desired)/2.0/d_desired)*(4);
        			}
        			else if(d3<2*d_desired && d3>=d_desired){
        				a3 = Math.max((v2-v3)/8.0,(2*d_desired-d3)/1.0/d_desired)*(4);
        			}
        			else{
        				a3 = 0;//not proper to accelerate within the secure distance
        			}
        		}
        		else{
        			if(d3>=d_desired)
        				a3 = 4.0;
        			else{
        				a3 = 0;//not proper to accelerate within the secure distance
        			}
        		}
        	//}
        	//else
        		//a3 = 0;
        	
        	//only for exporting data
        	if(numOfTrainingSample<STABLE_COUNT){
                data_d2[numOfTrainingSample] = (int) Math.round(d2);
                data_d3[numOfTrainingSample] = (int) Math.round(d3);
                data_a1[numOfTrainingSample] = a1;
                data_a2[numOfTrainingSample] = a2;
                data_a3[numOfTrainingSample] = a3;
                data_v1[numOfTrainingSample] = v1;
                data_v2[numOfTrainingSample] = v2;
                data_v3[numOfTrainingSample] = v3;
                /*if(numOfTrainingSample == STABLE_COUNT){
                	//Note:to record and export data
                	BufferedWriter outputWriter1 = null;
                	BufferedWriter outputWriter2 = null;
                	DecimalFormat df = new DecimalFormat("#.000"); //
                	try{
	                	outputWriter1 = new BufferedWriter(new FileWriter("#40_10_89_396_373_6"+"_n1"));
	                	outputWriter2 = new BufferedWriter(new FileWriter("#40_10_89_396_373_6"+"_n2"));
	                	for (int i = 0; i < STABLE_COUNT; i++) {
	                	  //n1
	                	  outputWriter1.write(df.format(data_v1[i])+"	"+v_desired+"	"+df.format(data_a1[i]));
	                	  outputWriter1.newLine();
	                	  //n2
	                	  outputWriter2.write(df.format(data_v2[i])+"	"+df.format(data_v1[i])+"	"+df.format(data_d2[i])+"	"+d_desired+"	"+df.format(data_a2[i]));
	                	  outputWriter2.newLine();
	                	  outputWriter2.write(df.format(data_v3[i])+"	"+df.format(data_v2[i])+"	"+df.format(data_d3[i])+"	"+d_desired+"	"+df.format(data_a3[i]));
	                	  outputWriter2.newLine();
	                	}
	                	outputWriter1.flush();  
	                	outputWriter1.close();
	                	outputWriter2.flush();  
	                	outputWriter2.close();
	                	System.out.println("done!");
                	}
                	catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                }*/
            }
        	//for matlab plot only
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
        	//Note:t_delta = 1 ¼ÆËãx_delta
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
        	//System.out.println("********");
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
        return "Title: CarPlatoon v1.1, 23 Jul 2017\n"
               + "Author: Hongming Wang\n"
               + "Control the car platoon using fuzzy logic";
    }
}
