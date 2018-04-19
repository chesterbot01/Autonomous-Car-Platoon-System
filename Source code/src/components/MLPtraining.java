package components;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.encog.ConsoleStatusReportable;
import org.encog.Encog;
import org.encog.bot.BotUtil;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.missing.MeanMissingHandler;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;
public class MLPtraining {
	//This is a demo for training network1. Full training processes are integrated in CarPlatoonFNN.java.
	public static void main(final String args[]) {
		/*try (BufferedReader br = new BufferedReader(new FileReader("#1_20_60_180_300_10_n1"))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       String[] str_ch = line.split("\t");
		       for(int i=0; i<3; i++){
		    	   System.out.print(Double.parseDouble(str_ch[i])+"\t");
		       }
		       System.out.println("");
		    }
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
 
		File file = new File("n1_dataset");
		
		CSVFormat format = new CSVFormat('.','\t'); // decimal point and tab separated
		VersatileDataSource source = new CSVDataSource(file, false, format);
		
		VersatileMLDataSet data = new VersatileMLDataSet(source);
		data.getNormHelper().setFormat(format);
		
		ColumnDefinition column_a1 = data.defineSourceColumn("a1", 2, ColumnType.continuous);
		ColumnDefinition column_v1 = data.defineSourceColumn("v1", 0, ColumnType.continuous);
		ColumnDefinition column_v_desired = data.defineSourceColumn("v_desired", 1, ColumnType.continuous);
		
		// Analyze the data, determine the min/max/mean/sd of every column.
		data.analyze();
		
		// Map the prediction column to the output of the model, and all
		// other columns to the input.
		data.defineSingleOutputOthersInput(column_a1);
		
		// Create feedforward neural network as the model type. MLMethodFactory.TYPE_FEEDFORWARD.
		// You could also other model types, such as:
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		
		// Send any output to the console.
		model.setReport(new ConsoleStatusReportable());
					
		// Now normalize the data.  Encog will automatically determine the correct normalization
		// type based on the model you chose in the last step.
		data.normalize();
		
		// Hold back 30% of data for a final validation.
		// Shuffle the data into a random ordering.
		// Use a seed of 1001 so that we always use the same holdback and will get more consistent results.
		model.holdBackValidation(0.3, true, 1001);
		
		// Choose whatever is the default training type for this model.
		model.selectTrainingType(data);
					
		// Use a 5-fold cross-validated train.  Return the best method found.
		MLRegression bestMethod = (MLRegression)model.crossvalidate(5, true);
		
		// Display the training and validation errors.
		System.out.println( "Training error: " + model.calculateError(bestMethod, model.getTrainingDataset()));
		System.out.println( "Validation error: " + model.calculateError(bestMethod, model.getValidationDataset()));
					
		// Display our normalization parameters.
		NormalizationHelper helper = data.getNormHelper();
		System.out.println(helper.toString());
					
		// Display the final model.
		System.out.println("Final model: " + bestMethod);
		EncogDirectoryPersistence.saveObject(new File("trainedFNN_1"), bestMethod);
		serializeAddress(helper);
		/*double test_v1 = 24.864;
		double test_v_desired = 27.0;
		
		MLData input = helper.allocateInputVector();*/
	}
	
	public static void serializeAddress(NormalizationHelper helper) {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream("n1_helper"));
			oos.writeObject(helper);
			System.out.println("Done");
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		finally {
			if (oos != null) {
				try {
					oos.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
