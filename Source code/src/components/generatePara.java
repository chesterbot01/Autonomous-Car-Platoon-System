package components;

import java.util.Random;

public class generatePara {
	public static void main(String[] args){
		int max_v_desired = 30;
		int min_v_desired = 5;
		int max_d_desired = 100;
		int min_d_desired = 50;
		double mult1 = 0.0;
		double mult2 = 0.0;
		int max_v_ini = 30;
		int min_v_ini = 0;
		Random random = new Random();
		for(int i=0; i<60; i++){
			int v_desired = random.nextInt(max_v_desired-min_v_desired+1)+min_v_desired;
			int d_desired = random.nextInt(max_d_desired-min_d_desired+1)+min_d_desired;
			int d2 = (int) Math.round((2+random.nextDouble()*(5-2))*d_desired);
			int d3 = (int) Math.round((2+random.nextDouble()*(5-2))*d_desired);
			int v_ini = random.nextInt(max_v_ini-min_v_ini+1)+min_v_ini;
			System.out.println("#"+i+"_"+v_desired+"_"+d_desired+"_"+d2+"_"+d3+"_"+v_ini);
		}
	}
}
