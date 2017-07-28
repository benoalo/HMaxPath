package in.vizio.HMaxPath;

import java.util.ArrayList;
import java.util.List;

public class Util {

	public static boolean[] saddleConfiguration()
	{
		boolean[] isSaddle = new boolean[256];
		for(int i=0; i<256; i++)
			isSaddle[i] = false;
		List< int[]		> pattern_list = new ArrayList< int[]     >();
		
		pattern_list.add(new int[] { 1, 0, 1, 2, 2, 2, 0, 2});
		pattern_list.add(new int[] { 1, 0, 2, 2, 1, 2, 0, 2});
		pattern_list.add(new int[] { 1, 0, 2, 2, 2, 2, 0, 1});
		pattern_list.add(new int[] { 2, 0, 2, 1, 1, 2, 0, 2});
		pattern_list.add(new int[] { 1, 0, 1, 2, 0, 2, 2, 2});
		pattern_list.add(new int[] { 2, 0, 1, 1, 0, 2, 2, 2});
		pattern_list.add(new int[] { 2, 0, 1, 2, 0, 1, 2, 2});
		
		List< int[] > filler_list = new ArrayList< int[] >();
		filler_list.add( new int[] {0,0,0,0} );
		filler_list.add( new int[] {0,0,0,1} );
		filler_list.add( new int[] {0,0,1,0} );
		filler_list.add( new int[] {0,0,1,1} );
		filler_list.add( new int[] {0,1,0,0} );
		filler_list.add( new int[] {0,1,0,1} );
		filler_list.add( new int[] {0,1,1,0} );
		filler_list.add( new int[] {0,1,1,1} );
		filler_list.add( new int[] {1,0,0,0} );
		filler_list.add( new int[] {1,0,0,1} );
		filler_list.add( new int[] {1,0,1,0} );
		filler_list.add( new int[] {1,0,1,1} );
		filler_list.add( new int[] {1,1,0,0} );
		filler_list.add( new int[] {1,1,0,1} );
		filler_list.add( new int[] {1,1,1,0} );
		filler_list.add( new int[] {1,1,1,1} );
		
		// pour chaque pattern
		for( int[] pattern : pattern_list) {
			List<int[]> pattern_variants = getFilledPattern( pattern, filler_list);
			// pour chaque variante du pattern
			for( int[] variant : pattern_variants) {
				List<int[]> pattern_transformed = getTransformedList(variant);
				// pour chaque transformation
				for( int[] transformed : pattern_transformed) {
					// get index and indicate that it is a saddle
					isSaddle[getIndex(transformed)] = true;
				}
		
			}
		}
		
		int count = 0;
		for( boolean val : isSaddle )
			count = val ? count+1 : count;
		System.out.println("Number of saddle patter is: " + count);
		
		return isSaddle;
	}
	
	public static int getIndex(int[] pattern ) {
		
		final int[] val = new int[] {1,2,4,8,16,32,64,128};
		
		int sum = 0;
		for(int i=0; i<8 ; i++) {
			sum += pattern[i] * val[i] ;
		}
		return sum;
	}
	
	
	public static List<int[]> getTransformedList( int[] pattern) {
		List<int[]> transformedPatterns = new ArrayList<int[]>();
		
		int[] rot0 = pattern;
		int[] rot1 = rotation( rot0 );
		int[] rot2 = rotation( rot1  );
		int[] rot3 = rotation( rot2  );
		int[] sym_rot0 = symmetry( pattern  );
		int[] sym_rot1 = rotation( sym_rot0 );
		int[] sym_rot2 = rotation( sym_rot1 );
		int[] sym_rot3 = rotation( sym_rot2 );
		
		transformedPatterns.add( rot0 		);
		transformedPatterns.add( rot1 		);
		transformedPatterns.add( rot2 		);
		transformedPatterns.add( rot3 		);
		transformedPatterns.add( sym_rot0 	);
		transformedPatterns.add( sym_rot1 	);
		transformedPatterns.add( sym_rot2 	);
		transformedPatterns.add( sym_rot3 	);
		
		return transformedPatterns;
	}
	
	public static int[] symmetry( int[] pattern) {
		int[] pattern2 = new int[8];
		int[] perm = new int[] {2,1,0,4,3,7,6,5}; // permutation of pixels to obtain a symmetry along the vertical
		
		for(int i=0; i<8; i++)
			pattern2[i] = pattern[ perm[i] ];
		
		return pattern2;
	}
	
	public static int[] rotation( int[] pattern) {
		int[] pattern2 = new int[8];
		int[] perm = new int[] {5,3,0,6,1,7,4,2}; // permutation of pixels to obtain a clockwise 90 degree rotation
		
		for(int i=0; i<8; i++)
			pattern2[i] = pattern[ perm[i] ];
		
		return pattern2;
	}
	
	public static List<int[]> getFilledPattern( int[] pattern, List<int[]> fillers){
		
		List<int[]> filledPatterns = new ArrayList<int[]>();
		
		for( int i=0; i<fillers.size() ; i++) {
			int[] filler = fillers.get(i);
			int[] pattern2 = new int[8];
			int count=0;
			for( int j=0; j<8; j++) {
				if(pattern[j]==2) {
					pattern2[j] = filler[count];
					count++;
				}
				else {
					pattern2[j]=pattern[j];
				}
			}
			filledPatterns.add(pattern2);
		}
		
		return filledPatterns;
	}
	
	

	
}
