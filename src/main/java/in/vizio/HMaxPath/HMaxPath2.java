package in.vizio.HMaxPath;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import net.imagej.Dataset;
import net.imagej.ImageJ;

/*
Author: Benoit Lombardot,  2017

*/

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondTipsShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;





public class HMaxPath2<T extends RealType<T>> {
	
//	RandomAccessibleInterval<IntType> labelMap;
//	
//	public HMaxPath(RandomAccessibleInterval<T> input, float threshold)
//	{
//		int nDims = input.numDimensions();
//		long[] dims = new long[nDims];
//		input.dimensions(dims);
//		ImgFactory<IntType> imgFactoryIntType=null;
//		try {
//			imgFactoryIntType = ((Img<T>) input).factory().imgFactory( new IntType() );
//		} catch (IncompatibleTypeException e) {
//			e.printStackTrace();
//		}
//		
//		if ( imgFactoryIntType != null )
//		{
//			this.labelMap = imgFactoryIntType.create(dims, new IntType(0));
//			Cursor<IntType> c_label = Views.iterable( labelMap ).cursor();
//			RandomAccess<T> ra_input = input.randomAccess();
//			while( c_label.hasNext() )
//			{
//				c_label.fwd();
//				ra_input.setPosition( c_label );
//				c_label.get().setInteger( (int) ra_input.get().getRealFloat() );
//			}
//		}
//		
//	}
	
	
	// 1. 	detect the saddles and keep a list of saddle up points
	// 2.	from each saddle up point trace the maximum ascent path till the maxima // on plato use breadth first search // see stream watershed of cousty and colleagues
	// 		while tracing keep track of the delta of altitude (peak dynamics of the delta of all path arriving at that maxima)
	// 		As a result each saddle is associated with 2 max and 2 h these are the edge of the max graph
	// 3.	build a graph of maxima where each edge is associated the weight min(h1,h2) 
	// 4.	Build a hierarchy of maxima ( 3 possibilities )
	//		a.	Union Find going in the decreasing order of saddle intensity (take care that we want to keep track of the tree)
	//		b.	modified IFT (prob equivalent to union find)
	//		c.	merge the minimum edge of the min graph, update edges and iterate (min graph is the graph composed on min weight edge of each node)
	//			this approach is potentially simpler as there should be no uprooting of the newly merged branch
	//
	

	
	
	
	
	public List<Point> saddle_v3( final RandomAccessibleInterval< T > source, final boolean[] isSaddle )
	{
		final List<Point> saddlePoints = new ArrayList<Point>();
		
		final int numNeighbor = 8;
		final int[] vals = new int[numNeighbor];
		for(int i=0; i< numNeighbor; i++) {
			vals[i]= (int)Math.pow(2, i);
		}
		
		final RectangleShape shape = new RectangleShape(1,true);
		final RandomAccessible<T> sourceX = Views.extendBorder( source );
		final RandomAccess<Neighborhood<T>> neighs = shape.neighborhoodsRandomAccessible(sourceX).randomAccess();
		final Cursor<T> sourceCursor = Views.iterable( source ).cursor();
		while( sourceCursor.hasNext() )
		{
			final T t = sourceCursor.next();
			neighs.setPosition( sourceCursor );
			final Cursor<T> c = neighs.get().cursor();
			int idx = 0;
			for(int i=0; i< numNeighbor; i++)
				if ( c.next().compareTo(t) > 0 )
					idx += vals[i];
			if ( isSaddle[idx] )
				saddlePoints.add(new Point( sourceCursor ) );
		}
	
		return saddlePoints;
	}
	
	

	

	public Collection<Point> getMinPathStart3( Point saddle , final RandomAccessibleInterval< T > source ) {

		
		final RectangleShape shape8C = new RectangleShape(1,true);
		final RandomAccess<Neighborhood<T>> neigh8CRA = shape8C.neighborhoodsRandomAccessible( Views.extendBorder(source) ).randomAccess();
		final DiamondTipsShape shape4C = new DiamondTipsShape(1);
		final RandomAccess<Neighborhood<T>> neigh4CRA = shape4C.neighborhoodsRandomAccessible( Views.extendBorder(source) ).randomAccess();
		
		
		///////////////////////////////////////////////////////////////
		// label min regions of the saddle
		
		final int[] prev = new int[] { 0 , 0 , 1 , 0 , 2 , 3 , 5 , 6 } ;
		
		RandomAccessible<T> sourceX = Views.extendBorder( source );
		final RandomAccess<T> sourceRA = sourceX.randomAccess();
		sourceRA.setPosition(saddle);
		final T saddleT = sourceRA.get();
		
		neigh8CRA.setPosition( saddle );
		final Cursor<T> neigh8C_saddle = neigh8CRA.get().cursor();
		Map<Integer, List<Point>> labelToPoints = new HashMap<Integer,List<Point>>();
		int[] labels = new int[8];
		int ii=0;
		int newLabel = 0;
		while( neigh8C_saddle.hasNext() ) {
			if( neigh8C_saddle.next().compareTo(saddleT) < 0 ) {
				int prev_label = labels[prev[ii]];
				if( prev_label > 0 ) { 
					labels[ii] = prev_label;
					labelToPoints.get(prev_label).add( new Point( neigh8C_saddle ) );
				}
				else {
					newLabel++;
					labels[ii] = newLabel ;
					List<Point> labelPoints = new ArrayList<Point>();
					labelPoints.add( new Point( neigh8C_saddle ) );
					labelToPoints.put(newLabel, labelPoints );
				}
			}
			ii++;
		}
		int lab7 = labels[7];
		int lab4 = labels[4];
		
		if( lab7>0 && lab4>0 ) {
			int labMin = Math.min(lab7, lab4);
			int labMax = Math.max(lab7, lab4);
			labelToPoints.get(labMin).addAll( labelToPoints.get(labMax) );
			labelToPoints.remove(labMax);
		}
		
		
		////////////////////////////////////////////////////////////////////////
		// get the min path start of each region
		
		Map<Integer,Point> minPathStarts = new HashMap< Integer, Point >();
		for( Entry<Integer,List<Point>> entry : labelToPoints.entrySet() ) {
			T minT = sourceRA.get().createVariable();
			minT.setReal( Double.MAX_VALUE);
			Point minPos = null;
			for( Point pt : entry.getValue()) {
				sourceRA.setPosition(pt);
				T currentT =  sourceRA.get(); 
				if( currentT.compareTo(minT)<0 ) {
					minT.set(currentT);
					minPos = new Point( sourceRA );
				}
			}
			minPathStarts.put( entry.getKey(), minPos );
		}
		
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// test if one of the min path start is a 4C minimum and whether its 8C continuation goes in a distinct min region 
		
		
		Map<Integer,Boolean> discard = new HashMap<Integer,Boolean>();
		for( Entry<Integer,Point> entry : minPathStarts.entrySet() ) {
			
			discard.put(entry.getKey(), false );
			
			Point step0 = entry.getValue();
			
			sourceRA.setPosition( step0 );
			float step0Val = sourceRA.get().getRealFloat();
			
			// get the next point in the min path
			Point step1 = new Point( step0 ); // if step0 is a minimum then it will be detected has the end of the path
			float minVal = step0Val;
			
			// test if step0 is a 4C minima
			neigh4CRA.setPosition( step0  );
			final Cursor<T> neighborhood = neigh4CRA.get().cursor();
			while( neighborhood.hasNext() ) {
				float val = neighborhood.next().getRealFloat();
				if( val < minVal ) {
					minVal = val;
					step1.setPosition( neighborhood );
				}	
			}
			
			if( ! step1.equals( step0 ) ) { // if step0 is a 4C minimum
				continue;
			}
				
			neigh8CRA.setPosition( step0  );
			final Cursor<T> neighborhood2 = neigh8CRA.get().cursor();
			while( neighborhood2.hasNext() ) {
				float val = neighborhood2.next().getRealFloat();
				if( val < minVal ) {
					minVal = val;
					step1.setPosition( neighborhood2 );
				}	
			}
			
			if( !step1.equals(step0) ) {
				
				// check if step1 belongs to a min-region different from step0
				for( Entry<Integer,List<Point>> entry2 : labelToPoints.entrySet() ) {
					if( entry2.getKey() != entry.getKey() ) {
						
						if( entry2.getValue().contains( step1 ) ) {
							discard.put(entry.getKey(), true);
							break;
						}
					}
				}
				
			}
			
		}
		
		// debug
		for( Entry<Integer,Boolean> entry : discard.entrySet() )
			if( entry.getValue() )
				minPathStarts.remove( entry.getKey() );

		/* debug
		System.out.println("=====================================");
		
		System.out.println("saddle: " + saddle.toString() );
		System.out.println("-------------------------------------");
		
		for( Integer label : labelToPoints.keySet() ){
			System.out.println("Label: " + label );
			System.out.println("points: " + labelToPoints.get(label).toString() );
			System.out.println("discard: " + discard.get( label ) );
			if( ! discard.get( label ) )
				System.out.println("min-point: " + minPathStarts.get(label).toString() );

			
			System.out.println("-------------------------------------");
		}
		*/
		
		return minPathStarts.values();
	}
		
	

	
	

	
	
//	protected static void getPosFromIdx(long idx, long[] position, long[] dimensions)
//	{
//		for ( int i = 0; i < dimensions.length; i++ )
//		{
//			position[ i ] = ( int ) ( idx % dimensions[ i ] );
//			idx /= dimensions[ i ];
//		}
//	}
	

	// a saddle detector by turning around a pixel could do detection and test together
	// search for min reg
	

	
	@SuppressWarnings("unchecked")
	public static void main(final String... args) throws IOException
	{
		ImageJ ij = new ImageJ();
		ij.ui().showUI();
		
		Dataset dataset = (Dataset) ij.io().open("/Users/lombardo/workspace/test_images/blobs_smooth.tif");
		Img<FloatType> img = (Img<FloatType>) dataset.getImgPlus();
		
		Img<FloatType> img2 = ij.op().convert().float32( img );
		img2 = (Img<FloatType>) ij.op().filter().gauss(img2, 1.0); //.run("Gauss", img,  5.0 );
		img2 = (Img<FloatType>) ij.op().math().multiply(img2, new FloatType(-1) );
		//img2 = (Img<FloatType>) ij.op().math().add(img2, new FloatType(255));
		//List<Point> saddlePoints0 = saddle_v0( img2 );
		//List<Point> saddlePoints1 = saddle_v1( img2 );
		//final Img<ByteType> img3 = ij.op().convert().int8( img );
		
		List<Point> saddlePoints2=null ;
		final boolean[] isSaddle = Util.saddleConfiguration();
		HMaxPath2<FloatType> detector = new HMaxPath2<FloatType>();
		for(int n=0;n<20; n++)
		{
			saddlePoints2 = detector.saddle_v3( img2 , isSaddle );
		}
		int nIter = 100;
		long start = System.nanoTime();
		for(int n=0;n<nIter; n++)
		{
			saddlePoints2 = detector.saddle_v3( img2 , isSaddle );
		}
		long dt = System.nanoTime()-start;
		System.out.println("dt " + (dt/nIter));
	

		
		List<Point> saddlePoints3 = new ArrayList<Point>();
		for( Point saddle : saddlePoints2 ) {
			Collection<Point> minPathStarts = detector.getMinPathStart3( saddle , img2 );
			if( minPathStarts.size() >=2 ) {
				saddlePoints3.add(saddle);
			}
		}
		
		final Overlay overlay = new Overlay();
		saddlePoints3.forEach( saddle -> {
			overlay.add( new PointRoi( saddle.getIntPosition( 0 ) + 0.5, saddle.getIntPosition( 1 ) + 0.5 ) );
		} );
		//saddlePoints1.forEach( saddle -> {
		//	overlay.add( new PointRoi( saddle.getIntPosition( 0 ) + 0.5, saddle.getIntPosition( 1 ) + 0.5 ) );
		//} );
		final ImagePlus imp2 = ImageJFunctions.show( img2 );
		imp2.setOverlay( overlay );
	
		ij.ui().show( imp2 );
		
		
	}
	

}
