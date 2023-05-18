// kn9558
// ai6358

import scala.Tuple2;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.api.java.JavaSparkContext;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// additional imports
import java.util.Scanner;
import java.io.*;

public final class WordGraph {

	/*
	* The main function needs to create a word graph of the text files provided in arg[0]
	* The output of the word graph should be written to arg[1]
	*/
    public static void main(String[] args) throws Exception {
        // brute force solution without Spark functionality
        // wordGraph(args);

        // initialize spark session
        SparkSession spark = SparkSession.builder().appName("JavaWordGraphHW4").getOrCreate();

        // create a context
        String inputPath = args[0] + "/*";
        JavaRDD<String> rdd = new JavaSparkContext(spark.sparkContext()).textFile(inputPath, 10);

        // extract all words from args[0]
	    JavaRDD<String> extracted_words = rdd.flatMap(line -> {
			ArrayList<String> output = new ArrayList<>();
			String[] words = line.split("(?![\\p{Punct}\\s])[\\W]+");
			return Arrays.asList(words).iterator();
		});
	
        // format the words as specified
	    JavaRDD<String> formatted_words = extracted_words.map(w -> w.replaceAll("[^a-zA-Z0-9]+", " ").toLowerCase()).filter(s -> !s.isEmpty());

        // count the number of occurences of each adjacent word pairing
	    JavaPairRDD<String, Tuple2<String, Integer>> pairs = formatted_words.flatMapToPair(line -> {
		    String[] words = line.split(" ");
		    ArrayList<Tuple2<String, Tuple2<String, Integer>>> list = new ArrayList<>();

            int index = 0;
            int length = words.length - 1;
            while (index < length) {
                if (words[index].isEmpty() || words[index + 1].isEmpty()) { 
                    index++;
                    continue; 
                }
                Tuple2<String, Integer> inner = new Tuple2<>(words[index + 1], 1);
                Tuple2<String, Tuple2<String, Integer>> outer = new Tuple2<>(words[index], inner);
                list.add(outer);
                index++;
            }

		    return list.iterator();
	    });

        // group each pairing by the predecessor word
	    JavaPairRDD<Tuple2<String, String>, Integer> counts = pairs
            .mapToPair(p -> new Tuple2<>(new Tuple2<>(p._1, p._2._1), p._2._2))
            .reduceByKey((c1, c2) -> c1 + c2);

	    JavaPairRDD<String, Iterable<Tuple2<String, Integer>>> merged = counts
    	    .mapToPair(p -> new Tuple2<>(p._1()._1(), new Tuple2<>(p._1()._2(), p._2())))
    	    .groupByKey();

        
        // generate output strings after calculating weights
	    JavaRDD<String> output = merged.map(e -> {
		    int num_successors = 0;
		    double total_cooccurences = 0;
            double round = Math.pow(10, 3);
		    for (Tuple2<String, Integer> tuple : e._2()){
                total_cooccurences += tuple._2();
			    num_successors++;
            }

            StringBuilder output_builder = new StringBuilder();
            output_builder.append(e._1() + " " + num_successors + "\n");
            for (Tuple2<String, Integer> tuple : e._2()){
			    if (tuple._2() != total_cooccurences) {
				    double weight = ((double) (tuple._2()))/(total_cooccurences);
				    double rounded_weight = Math.round(round * weight) / round;
				    output_builder.append("<" + tuple._1() + ", " + rounded_weight + ">\n");
			    } else {
				    output_builder.append("<" + tuple._1() + ", 1>\n");
			    }
            }
		    // System.out.println("OUTPUT\n" + result);
            return output_builder.toString();
        });

        // write outputs to args[1]
        String output_path = args[1];
        String parent = new File(output_path).getParent();
	    File output_directory = new File(parent);
		if (!output_directory.exists()) { output_directory.mkdirs(); }
		
        File output_file = new File(output_path);
		if (!output_file.exists()) { output_file.createNewFile(); }
		
        FileWriter file_writer = new FileWriter(output_file);
		BufferedWriter buffered_writer = new BufferedWriter(file_writer);
        buffered_writer.write(output.reduce((s1, s2) -> s1 + s2));
        buffered_writer.close();

        spark.stop();
    }

    // method used to format string --> NOT USED because we will use Spark methods
    private static String formatString(String str) {
        str = str.toLowerCase();
        StringBuilder sb = new StringBuilder(str);
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (!(Character.isDigit(ch) || Character.isLetter(ch))) {
                sb.setCharAt(i, ' ');
            }
        }
        return sb.toString();
    }

    // method that computes wordGraph --> NOT USED because doesn't make use of Spark functions
    private static void wordGraph(String[] args) throws Exception {
        File outputFile = new File("out.txt");
        FileOutputStream fos = new FileOutputStream(outputFile);
        // System.out.println("Args[0] = " + args[0]);
        // System.out.println("Args[1] = " + args[1]);
		String path = args[0];
        ArrayList<String> listWords = new ArrayList<>();
        HashMap<String, HashMap<String, Integer>> wordGraph = new HashMap<>();
        HashMap<String, Integer> count = new HashMap<>();

        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.isFile()) { continue; }
                    
                Scanner sc = new Scanner(f);
                while (sc.hasNextLine()) {
                    String ln = sc.nextLine();
                    String newString = formatString(ln);
                    String[] words = newString.split("\\s+");
                    System.out.println(Arrays.toString(words));
                    for (int i = 0; i < words.length - 1; i++) {
                        String first = words[i];
                        String second = words[i + 1];

                        if (wordGraph.containsKey(first)) {
                            HashMap<String, Integer> val = wordGraph.get(first);
                            if (val.containsKey(second)) {
                                int curr = val.get(second);
                                val.put(second, curr + 1);
                            } else {
                                val.put(second, 1);
                            }
                            int curr = count.get(first);
                            count.put(first, curr + 1);
                        } else {
                            listWords.add(first);
                            HashMap<String, Integer> newVal = new HashMap<>();
                            newVal.put(second, 1);
                            wordGraph.put(first, newVal);
                            count.put(first, 1);
                        }
                    }
                }
                sc.close();
            }
        }

        path = args[1];
        for (String key: wordGraph.keySet()) {
            HashMap<String, Integer> vals = wordGraph.get(key);
            int c = count.get(key);
            // System.out.println(key + " " + c);
            String keyString = key + " " + c + "\n";
            fos.write(keyString.getBytes());

            for (String k: vals.keySet()) {
                // System.out.println("<" + k + ", " + (double)(vals.get(k)) / c + ">");
                String valString = "<" + k + ", " + (double)(vals.get(k)) / c + ">\n";
                fos.write(valString.getBytes());
            }
        }
        fos.close();
    }

}
