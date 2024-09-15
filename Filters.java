package com.github.lovasoa.bloomfilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;
//import static org.simmetrics.metrics.Math.intersection;


public class Filters{
	/**
	 *
	 */
	public static final int NUMBER_RANGE = 100_000; // random number range
	public static int fixed_bitsize  = (int) (NUMBER_RANGE);

	public static int min = 1000;
    public static int max = NUMBER_RANGE;
    public static Random random1 = new Random();
    public static int E = 1000; //Number of files
    public static int topdocs = 10;
    
    public static void printMatrix(Object[][] matrix){
		for (Object[] row : matrix)
		{
			// traverses through number of rows
			for (Object element : row)
			{
				// 'element' has current element of row index
				System.out.print( element  + "\t");
			}
			System.out.println();
		}
	}
    
	public static void main(String[] args) {
		
		//Random random3 = new Random();
		int [] myRNG = new int[E];
		for (int r=0; r <E; r++) {
			myRNG[r] = random1.nextInt((max - min)+1) + min;
		}
		
		System.out.println("# elements in set 1 : "+ myRNG[0]);
		
		
		BloomFilter [] myFilters = new BloomFilter[E];
	    for (int f = 0; f<E; f++){
	    	myFilters[f] = new BloomFilter (fixed_bitsize, myRNG[f]);
	    	//System.out.println(myFilters[f]);
	    }
	    
		
	    HashSet<Integer>[] sets= new HashSet[E];
	    for(int i=0; i<E; i++) {
	      sets[i]= new HashSet<Integer>();
	    }

	    
		Random random = new Random();
		for (int h=0; h<E; h++) { //adding elements into specific sets 
			sets[h] = new HashSet<>(myRNG[h]); //converting array into hashset of size = myRNG[i]. soo i can use Collections method
			while(sets[h].size()< myRNG[h]) {
				while (!sets[h].add(random.nextInt(max)));
			}
			assert sets[h].size() == myRNG[h];	    
			myFilters[h].addAll(sets[h]);
		}
		//System.out.println("set1:" + sets[0]);
		//System.out.println("set2:" + sets[1]);
		//System.out.println("set3:" + sets[2]);

		
        //System.out.println("filter "+ myFilters[0].bloom);
        System.out.println("filter size: "+ myFilters[0].bloom.size());

	    
        //*****************************intersection and union of plain sets********************************************************************
        
        Files[] unhashedfiles = new Files[E-1]; //can't compare to itself
        ArrayList<Integer> topk1 = new ArrayList<>(); 
        ArrayList<Integer> topk2 = new ArrayList<>(); 

        
        for(int c = 0; c<E-1; c++ ) {
        	Set<Integer> intersection = Sets.intersection(sets[0], sets[c+1]);
        	Set<Integer> union = Sets.union(sets[0], sets[c+1]);
        	double ActualJac= Double.valueOf(intersection.size()) / Double.valueOf(union.size());
    		//System.out.println("COMPARING FILE 0 TO FILE "+ c);
    		unhashedfiles[c] = new Files(c+2, ActualJac);
            //System.out.println("/nJaccard Coefficient of Plaintext:                  " + ActualJac);
        }
        
        Arrays.sort(unhashedfiles, new ScoreComparator());
        //System.out.println(Arrays.toString(unhashedfiles));
        for(int i = Math.max(0,unhashedfiles.length-topdocs); i <unhashedfiles.length;i++) {
        	topk1.add( unhashedfiles[i].getId());
        	System.out.println(unhashedfiles[i]+" ");
        }
        //System.out.println(topk1);
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        
        
        Object [][] matrix = new Object[E][NUMBER_RANGE];
        for(int m=0;m<E;m++) {
        matrix[m]= myFilters[m].bloom.toArray();
        }
        
        int j = 0;
		//System.out.println("\nnumber of cols: " + matrix[j].length);
		//System.out.println("number of rows: " + matrix.length);

        //printMatrix(matrix);


        //**************************************hashed sets***********************************************************************************
        double ClosestJac = 0;
        Files[] hashedfiles = new Files[E-1]; //can't compare to itself
        for (int m= 1; m < matrix.length; m++) { //iterate and compare from the 1st row to the row. not including 1st vs 1st
		//System.out.println("COMPARING FILE 1 TO FILE "+ m);
		//double ClosestJac = 10;
        	
        int numzeros = 0;
        for(int p = 0; p < matrix[j].length; p++ ) { //iterate through the columns to find where both are zero 
        	if (matrix[0][p].equals(matrix[m][p]) && matrix[0][p].equals(0)) {
       			numzeros++;
       		}
       	}
        float intervalue = 0;
        for(int q = 0; q < matrix[j].length; q++ ) { //iterate through the columns to find where both are equal to 1
        		if (matrix[0][q].equals(1) && matrix[m][q].equals(1)) {
        			intervalue++;
        		}
        }
		float unionvalue = 0;
		for(int u = 0; u < matrix[j].length; u++ ) { // iterate through the j columns to find where either are equal to 1 and count++
			if (matrix[0][u].equals(1) || matrix[m][u].equals(1)) {
				unionvalue++;
			}
		}
		
		float filter1size = 0;
		for(int x = 0; x < matrix[0].length; x++ ) { //iterate through matrix [0] or only the first row and count the number of 1's
				if (matrix[0][x].equals(1)) {
		        	filter1size++;
				}
		}
		float filter2size = 0;
		for(int y = 0; y < matrix[m].length; y++ ) { //iterate through the matrix[m] or the rows after the 1st and count the number of 1's
				if (matrix[m][y].equals(1)) {
					filter2size++;
				}
		}

		float Jac= (intervalue/unionvalue);
        //System.out.println("\nNumber of zero pairs: "+ numzeros);

        final float ApproxUnion= myFilters[0].bloom.size() - (numzeros); // |Bx U By| = Length - (# of zero pairs)
        final float ApproxInter= (filter1size + filter2size) - ApproxUnion; // |Bx|+|By|-|Bx U By|

        // also |A U B| = |A| + |B| - |A n B|

        //System.out.println("\nApproximate Union:        "+ ApproxUnion);
        //System.out.println("Approximate Intersection: "+ ApproxInter);

        float ApproxJac= (ApproxInter/ApproxUnion);
        //System.out.println("Jaccard Coefficient of BF using Algorithm          " + ApproxJac);
        
        hashedfiles[m-1] = new Files(m+1, ApproxJac);
        
        

        //double PE= (Math.abs(Jac - ActualJac)/ ActualJac)*100;
        //double Accuracynew = Jac/ActualJac;
        //double accuracy= 100.0 - PE;
        //double Percentdif = ((Math.abs(Jac-ActualJac) / ((Jac+ActualJac)/2)) *100);
        //System.out.println("\nPercent Difference: " + Percentdif);
        //System.out.println("PE:      "+ PE);
        //System.out.println("Accuracy:"+accuracy);
        //System.out.println("New Accuracy:"+Accuracynew);
        if(ClosestJac<ApproxJac) {
        	ClosestJac = ApproxJac;
        }
        //System.out.println("\nFile that is most similar to file 0 has a JAC of "+ ClosestJac);
        //System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        
        }    
        
        
        
        Arrays.sort(hashedfiles, new ScoreComparator());
        //System.out.println(Arrays.toString(hashedfiles));
        for(int i = Math.max(0,hashedfiles.length-topdocs); i <hashedfiles.length;i++) {
        	topk2.add(hashedfiles[i].getId());
        	System.out.println(hashedfiles[i]+" ");
        }
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        //System.out.println(topk1);
        //System.out.println(topk2);
        
        topk1.retainAll(topk2);
        double dsize = topk1.size();
        double intOftopks = dsize / topdocs;
        
        System.out.println(topk1);

        System.out.println( "Intersection accuracy: " +intOftopks);

}
} 